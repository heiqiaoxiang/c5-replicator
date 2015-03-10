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

package c5db.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JUnit TestRule intended to be used as a callback for an instance of {@link ExceptionHandlingBatchExecutor},
 * which in turns allows creating fibers such that uncaught exceptions are gathered by this rule. After a test
 * method runs, this rule will rethrow any caught exceptions. They are rethrown in the context of the thread
 * in which the test runs.
 */
public class JUnitRuleFiberExceptions implements TestRule, Consumer<Throwable> {
  private final List<Throwable> throwables = Collections.synchronizedList(new ArrayList<Throwable>());

  public JUnitRuleFiberExceptions() {

  }

  @Override
  public void accept(Throwable throwable) {
    throwables.add(throwable);
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } catch (Throwable t) {
          throwables.add(t);
        }
        MultipleFailureException.assertEmpty(throwables);
      }
    };
  }
}
