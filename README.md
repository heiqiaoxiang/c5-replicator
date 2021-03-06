c5 replicator
====================
[![Build status](https://travis-ci.org/cloud-software-foundation/c5-replicator.svg)](https://travis-ci.org/cloud-software-foundation/c5-replicator) [![HuBoard badge](http://img.shields.io/badge/Hu-Board-7965cc.svg)](https://huboard.com/cloud-software-foundation/c5)
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/cloud-software-foundation/c5)

This is an implementation of the Raft consensus protocol. It can replicate arbitrary data between several servers.

Building
--------------------
To build this project, simply

    mvn install

Using in another project
-------------------
This project hosts its own maven snapshot repository on github. To access it, add these lines to your pom.xml:

    <repositories>
      <repository>
        <id>c5-replicator-mvn-repo</id>
        <url>https://raw.github.com/OhmData/c5-replicator/mvn-repo/</url>
        <snapshots>
          <enabled>true</enabled>
          <updatePolicy>always</updatePolicy>
        </snapshots>
      </repository>
    </repositories>

Then you'll be able to add the project's dependencies:

    <dependency>
      <groupId>c5db</groupId>
      <artifactId>c5-replicator-api</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>

Module overview
-------------------
- __c5-replicator-api__: Interfaces used by all other modules to communicate with each other,
                         as well as value-type and generated classes referred to by the interfaces
- __c5-replicator__: Implementation of the consensus protocol logic (see class ReplicatorInstance),
                     as well as networking and starting/stopping the server
- __c5-replicator-log__: Implementation of the (local disk) log used by the replicator (see class QuorumDelegatingLog)
- c5-replicator-proto: Generated classes and their .proto specs
- c5-replicator-util: Common utilities for implementations and tests
- __c5-general-replication__: Example usage of all preceding modules to host a three-replicator node all on localhost
- cat-olog: Log-reading utility

Example usage
-------------------
Example usage may be found in the test(s) in project module __c5-general-replication__.

Log-reading utility usage
-------------------
Provided is a utility for reading the logs generated by the replicator; the code for this utility is in module __cat-olog__,
and it may be run from the command line as:

    cat-olog/bin/cat_olog.sh <log file name>

Troubleshooting
--------------------
On Mac OSX:

    export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

More documentation
--------------------
For more information about the code and package structure, please see the package-info.java files in the
__c5-replicator__ module in package c5db.replication; and in the __c5-replicator-log__ module in package c5db.log, respectively.

C5 is hosted on GitHub at https://github.com/cloud-software-foundation/c5.

