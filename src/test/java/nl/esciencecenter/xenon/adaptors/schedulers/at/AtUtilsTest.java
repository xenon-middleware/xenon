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
package nl.esciencecenter.xenon.adaptors.schedulers.at;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AtUtilsTest {

    @Test
    public void test_atq_parser_null() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_empty() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo("", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_whiteSpace() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo("  ", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_single_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("11"));

        Map<String, String> v = result.get("11");

        assertEquals("=", v.get("queue"));
        assertEquals("jason", v.get("user"));
        assertEquals("11", v.get("jobID"));
        assertEquals("Mon Jul 2 10:22:00 2018", v.get("startDate"));
    }

    @Test
    public void test_atq_parser_multi_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_multi_line_empty() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n\n\n16    Wed Jul  4 16:00:00 2018 a jason";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_multi_line_whitespace() {
        String tmp = "  \n11 Mon Jul 2 10:22:00 2018 = jason\n  \n  \n16    Wed Jul  4 16:00:00 2018 a jason\n  \n  \n";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_atq_parser_invalid() {
        AtUtils.parseJobInfo("Hello World!", null);
    }

    @Test
    public void test_atq_parser_select_queue_one() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Set<String> queues = new HashSet<String>();
        queues.add("a");

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, queues);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("16"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_select_queue_none() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Set<String> queues = new HashSet<String>();
        queues.add("b");

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, queues);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

}
