package nl.esciencecenter.xenon.adaptors.schedulers.at;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AtUtilsTest {

    @Test
    public void test_atq_parser_null() {
        Map<String, String> result = AtUtils.parseQueueInfo(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_empty() {
        Map<String, String> result = AtUtils.parseQueueInfo("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_whiteSpace() {
        Map<String, String> result = AtUtils.parseQueueInfo("  ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_single_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason";

        Map<String, String> result = AtUtils.parseQueueInfo(tmp);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("11"));
        assertEquals("=", result.get("11"));
    }

    @Test
    public void test_atq_parser_multi_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Map<String, String> result = AtUtils.parseQueueInfo(tmp);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertEquals("=", result.get("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("a", result.get("16"));
    }

    @Test
    public void test_atq_parser_multi_line_empty() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n\n\n16    Wed Jul  4 16:00:00 2018 a jason";

        Map<String, String> result = AtUtils.parseQueueInfo(tmp);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertEquals("=", result.get("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("a", result.get("16"));
    }

    @Test
    public void test_atq_parser_multi_line_whitespace() {
        String tmp = "  \n11 Mon Jul 2 10:22:00 2018 = jason\n  \n  \n16    Wed Jul  4 16:00:00 2018 a jason\n  \n  \n";

        Map<String, String> result = AtUtils.parseQueueInfo(tmp);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertEquals("=", result.get("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("a", result.get("16"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_atq_parser_invalid() {
        AtUtils.parseQueueInfo("Hello World!");
    }

}
