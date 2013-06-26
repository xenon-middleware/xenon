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

package nl.esciencecenter.octopus.adaptors.local;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import nl.esciencecenter.octopus.adaptors.GenericJobAdaptorTest;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class LocalJobAdaptorTest extends GenericJobAdaptorTest {
    
    @BeforeClass
    public static void prepareLocalJobAdaptorTest() throws Exception { 
        GenericJobAdaptorTest.prepareClass(new LocalJobTestConfig());
    }

    @AfterClass
    public static void cleanupAltLocalJobsTest() throws Exception { 
        GenericJobAdaptorTest.cleanupClass();
    }
}
