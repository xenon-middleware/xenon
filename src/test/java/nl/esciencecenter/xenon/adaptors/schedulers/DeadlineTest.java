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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DeadlineTest {

    @Test
    public void test_create() {
        new Deadline();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_timeout() {
        Deadline.getDeadline(-1);
    }

    @Test
    public void test_infinite_timeout() {
        long deadline = Deadline.getDeadline(0);
        assertEquals(Long.MAX_VALUE, deadline);
    }

    @Test
    public void test_normal_timeout() {
        long now = System.currentTimeMillis();
        long deadline = Deadline.getDeadline(1000);
        long diff = deadline - now;

        // Diff should be 1000, but we may have some small error due to timing. Lets assume we should be within 10ms.
        assertTrue(diff <= 1000);
        assertTrue(diff >= 990);
    }

    @Test
    public void test_overflow_timeout() {
        long now = System.currentTimeMillis();
        long maxTime = Long.MAX_VALUE - now;
        long deadline = Deadline.getDeadline(maxTime + 10);

        assertEquals(Long.MAX_VALUE, deadline);
    }
}
