/*
 * Copyright 2014 WANdisco
 *
 *  WANdisco licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package c5db.log;

import c5db.LogConstants;
import c5db.util.CheckedSupplier;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * LogPersistenceService using FilePersistence objects (Files and FileChannels).
 */
public class LogFileService implements LogPersistenceService<FilePersistence> {
  private final Path logRootDir;

  public LogFileService(Path basePath) throws IOException {
    this.logRootDir = basePath.resolve(LogConstants.LOG_ROOT_DIRECTORY_RELATIVE_PATH);

    createDirectoryStructure();
  }

  @Nullable
  @Override
  public FilePersistence getCurrent(String quorumId) throws IOException {
    final Path currentLink = getCurrentLink(quorumId);
    if (currentLink == null) {
      return null;
    } else {
      return new FilePersistence(Files.readSymbolicLink(currentLink));
    }
  }

  @NotNull
  @Override
  public FilePersistence create(String quorumId) throws IOException {
    return new FilePersistence(getNewLogFilePath(quorumId));
  }

  @Override
  public void append(String quorumId, @NotNull FilePersistence persistence) throws IOException {
    final Path currentLink = getCurrentLink(quorumId);
    final long linkId;

    if (currentLink == null) {
      linkId = 1;
    } else {
      linkId = linkIdOfFile(currentLink.toFile()) + 1;
    }

    Files.createSymbolicLink(pathForLinkId(linkId, quorumId), persistence.path);
  }

  @Override
  public void truncate(String quorumId) throws IOException {
    Files.delete(getCurrentLink(quorumId));
  }

  @Override
  public ImmutableList<CheckedSupplier<FilePersistence, IOException>> getList(String quorumId) throws IOException {

    ImmutableList.Builder<CheckedSupplier<FilePersistence, IOException>> persistenceSupplierBuilder =
        ImmutableList.builder();

    for (Path path : getLinkPathMap(quorumId).descendingMap().values()) {
      persistenceSupplierBuilder.add(
          () -> new FilePersistence(Files.readSymbolicLink(path)));
    }

    return persistenceSupplierBuilder.build();
  }

  /**
   * Delete all the logs stored in the wal root directory.
   *
   * @throws IOException
   */
  public void clearAllLogs() throws IOException {
    Files.walkFileTree(logRootDir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!attrs.isDirectory()) {
          Files.delete(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Delete links to all logs for a given quorum except the current log, effectively
   * removing the past logs from the record but keeping the data.
   *
   * @throws IOException
   */
  public void archiveAllButCurrent(String quorumId) throws IOException {
    NavigableMap<Long, Path> linkPathMap = getLinkPathMap(quorumId);
    if (linkPathMap.isEmpty()) {
      return;
    }

    long lastLinkId = linkPathMap.lastEntry().getKey();
    for (Map.Entry<Long, Path> linkEntry : linkPathMap.entrySet()) {
      if (linkEntry.getKey() != lastLinkId) {
        Files.delete(linkEntry.getValue());
      }
    }
  }

  private Path getNewLogFilePath(String quorumId) throws IOException {
    String fileName = String.valueOf(System.nanoTime());
    createQuorumDirectoryIfNeeded(quorumId);
    return logFileDir(quorumId).resolve(fileName);
  }

  @Nullable
  private Path getCurrentLink(String quorumId) throws IOException {
    Map.Entry<Long, Path> lastEntry = getLinkPathMap(quorumId).lastEntry();
    if (lastEntry == null) {
      return null;
    } else {
      return lastEntry.getValue();
    }
  }

  private NavigableMap<Long, Path> getLinkPathMap(String quorumId) throws IOException {
    NavigableMap<Long, Path> linkPathMap = new TreeMap<>();

    for (File file : allFilesInDirectory(quorumDir(quorumId))) {
      if (!Files.isSymbolicLink(file.toPath())) {
        continue;
      }

      long fileId = linkIdOfFile(file);
      linkPathMap.put(fileId, file.toPath());
    }

    return linkPathMap;
  }

  private long linkIdOfFile(File file) {
    return Long.parseLong(file.getName());
  }

  private Path pathForLinkId(long linkId, String quorumId) {
    return quorumDir(quorumId).resolve(String.valueOf(linkId));
  }

  private Path quorumDir(String quorumId) {
    return logRootDir.resolve(quorumId);
  }

  private Path logFileDir(String quorumId) {
    return quorumDir(quorumId).resolve(LogConstants.LOG_FILE_SUBDIRECTORY_RELATIVE_PATH);
  }

  private void createDirectoryStructure() throws IOException {
    Files.createDirectories(logRootDir);
  }

  private void createQuorumDirectoryIfNeeded(String quorumId) throws IOException {
    Files.createDirectories(quorumDir(quorumId));
    Files.createDirectories(logFileDir(quorumId));
  }

  private static File[] allFilesInDirectory(Path dirPath) {
    File[] files = dirPath.toFile().listFiles();
    if (files == null) {
      return new File[]{};
    } else {
      return files;
    }
  }
}
