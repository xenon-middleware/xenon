/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenonTestWatcher extends TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(XenonTestWatcher.class);

    @Override
    public void starting(Description description) {
        logger.info("Running test {}", description.getMethodName());
    }

    @Override
    public void failed(Throwable reason, Description description) {
        logger.info("Test {} failed due to exception", description.getMethodName(), reason);
    }

    @Override
    public void succeeded(Description description) {
        logger.info("Test {} succeeded", description.getMethodName());
    }

    @Override
    public void skipped(AssumptionViolatedException reason, Description description) {
        logger.info("Test {} skipped due to failed assumption", description.getMethodName(), reason);
    }
}
