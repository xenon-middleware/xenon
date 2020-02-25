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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.XenonException;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptingParserTest {

    @Test
    public void test01a_parseKeyValuePairs_CorrectInput_ResultMap() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1=value1 key2=value2 key3=value3 key4=value4";

        Map<String, String> result = ScriptingParser.parseKeyValuePairs(input, "fake");

        assertEquals("parser does not handle simple key/value input correctly", expected, result);
    }

    @Test
    public void test01b_parseKeyValuePairs_ExtraWhiteSpace_Ignored() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");
        expected.put("key5", "value5");
        expected.put("key6", "value6");

        String input = "key1=value1    key2=value2\n    key3=value3\t\t\t\t\tkey4=value4\n" + "    key5=value5  key6=value6         ";

        Map<String, String> result = ScriptingParser.parseKeyValuePairs(input, "fake");

        assertEquals("parser does not handle whitespace correctly", expected, result);
    }

    @Test
    public void test01c_parseKeyValuePairs_EmptyLines_Ignored() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1=value1 key2=value2\n\n\n\n\n\n\n\n\nkey3=value3 key4=value4";

        Map<String, String> result = ScriptingParser.parseKeyValuePairs(input, "fake");

        assertEquals("parser does not handle empty lines correctly", expected, result);
    }

    @Test
    public void test01d_parseKeyValuePairs_IgnoredLines_Ignored() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1=value1 key2=value2\n\n\n\nignore this line\n\nplease ignore\n\n\nkey3=value3 key4=value4\nsorry, please ignore";

        Map<String, String> result = ScriptingParser.parseKeyValuePairs(input, "fake", "ignore");

        assertEquals("parser does not handle empty lines correctly", expected, result);
    }

    @Test(expected = XenonException.class)
    public void test01c_parseKeyValuePairs_SpaceInKeyValuePair_ExceptionThrown() throws Exception {
        String input = "key1 = value1";

        ScriptingParser.parseKeyValuePairs(input, "fake");
    }

    @Test
    public void test02_containsAny() {
        assertTrue(ScriptingParser.containsAny("this sentence contains some words", "contains", "other", "options"));

        assertTrue(ScriptingParser.containsAny("this sentence contains some words", "contains some words", "other", "options"));

        assertTrue(ScriptingParser.containsAny("contains", "contains"));

        assertFalse(ScriptingParser.containsAny("this sentence contains some words", "other", "options"));
    }

    @Test
    public void test03a_parseKeyValueLines_CorrectInput_ResultMap() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1 = value1\nkey2 = value2\nkey3 = value3\nkey4=value4\n";

        Map<String, String> result = ScriptingParser.parseKeyValueLines(input, ScriptingParser.EQUALS_REGEX, "fake", "ignore this line");

        assertEquals("parser does not handle simple key/value input lines correctly", expected, result);
    }

    @Test
    public void test03b_parseKeyValueLines_InputWithExtraSpaces_ResultMap() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1  =  value1\nkey2\t\t\t =   value2\n   key3 = value3  \n   key4   =value4    \n";

        Map<String, String> result = ScriptingParser.parseKeyValueLines(input, ScriptingParser.EQUALS_REGEX, "fake", "ignore this line");

        assertEquals("parser does not handle spaces in key/value input lines correctly", expected, result);
    }

    @Test
    public void test03c_parseKeyValueLines_SpaceSeparatedPairs_ResultMap() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1 value1\nkey2 value2\nkey3 value3\nkey4 value4\n";

        // note that we use a different regex in this test
        Map<String, String> result = ScriptingParser.parseKeyValueLines(input, ScriptingParser.WHITESPACE_REGEX, "fake", "ignore this line");

        assertEquals("parser does not handle space separated key/value input lines correctly", expected, result);
    }

    @Test
    public void test03d_parseKeyValueLines_IgnoredLines_Ignored() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1 = value1\nkey2 = value2\nignore this line\nignore\nignore\nkey3 = value3\nkey4 = value4\n";

        // note that we use a different regex in this test
        Map<String, String> result = ScriptingParser.parseKeyValueLines(input, ScriptingParser.EQUALS_REGEX, "fake", "ignore");

        assertEquals("parser does not handle space separated key/value input lines correctly", expected, result);
    }

    @Test
    public void test03e_parseKeyValueLines_EmptyLines_Ignored() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        expected.put("key4", "value4");

        String input = "key1 = value1\nkey2 = value2\n\n\n\n\n\n\n\nkey3 = value3\nkey4 = value4\n";

        Map<String, String> result = ScriptingParser.parseKeyValueLines(input, ScriptingParser.EQUALS_REGEX, "fake", "ignore");

        assertEquals("parser does not handle space separated key/value input lines correctly", expected, result);
    }

    @Test(expected = XenonException.class)
    public void test03f_parseKeyValueLines_KeyWithoutValue_ExceptionThrown() throws Exception {
        String input = "key1\nkey2 value2\nkey3 value3\nkey4 value4\n";

        ScriptingParser.parseKeyValueLines(input, ScriptingParser.WHITESPACE_REGEX, "fake", "ignore");
    }

    @Test
    public void test04a_testParseJobIDFromLine_SingleOption_Result() throws Exception {
        String input = "Some user submitted job 43424";

        String result = ScriptingParser.parseJobIDFromLine(input, "fake", "Some user submitted job");

        assertEquals("parser failed to get correct job ID from line", "43424", result);
    }

    @Test
    public void test04b_testParseJobIDFromLine_MultipleOptions_Result() throws Exception {
        String input = "Some user submitted job 43425";

        String result = ScriptingParser.parseJobIDFromLine(input, "fake", "Hey look a new job", "Are you sure you wanted to submit", "Some user submitted job",
                "Ow no another job");

        assertEquals("parser failed to get correct job ID from line", "43425", result);
    }

    @Test(expected = XenonException.class)
    public void test04c_testParseJobIDFromLine_IncorrectPrefix_ThrowsException() throws Exception {
        String input = "Some dude submitted job 43425";

        ScriptingParser.parseJobIDFromLine(input, "fake", "Some user submitted job", "Ow no another job");
    }

    @Test(expected = XenonException.class)
    public void test04d_testParseJobIDFromLine_NoJobID_ThrowsException() throws Exception {
        String input = "Some user submitted job  ";

        ScriptingParser.parseJobIDFromLine(input, "fake", "Some user submitted job");
    }

    @Test
    public void test05_cleanValue() {
        String input;
        String expected;
        String result;

        input = "somevalue*";
        expected = "somevalue";
        result = ScriptingParser.cleanValue(input, "*");
        assertEquals("values not properly cleaned", expected, result);

        input = "somevalue**";
        expected = "somevalue*";
        result = ScriptingParser.cleanValue(input, "*");
        assertEquals("suffixes should be cleaned exactly once", expected, result);

        input = "somevalue*";
        expected = "somevalue";
        result = ScriptingParser.cleanValue(input, "~", "bla", "*", "suffix");
        assertEquals("parser unable to detect suffix from multiple possible suffixes", expected, result);

        input = "    somevalue*      ";
        expected = "somevalue";
        result = ScriptingParser.cleanValue(input, "*");
        assertEquals("parser should trim result", expected, result);

        input = "    somevalue      ";
        expected = "somevalue";
        result = ScriptingParser.cleanValue(input, "*");
        assertEquals("parser should trim result even when no suffix present", expected, result);
    }

    @Test
    public void test06a_parseTable_SimpleTable_ResultMap() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1 key2 key3 key4\n" + "value1 value2 value3 value4\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test
    public void test06b_parseTable_MultipleRecords_ResultMap() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        Map<String, String> expectedRecord2 = new HashMap<>();
        expectedRecord2.put("key1", "value5");
        expectedRecord2.put("key2", "value6");
        expectedRecord2.put("key3", "value7");
        expectedRecord2.put("key4", "value8");
        expected.put("value5", expectedRecord2);

        String input = "key1 key2 key3 key4\n" + "value1 value2 value3 value4\n" + "value5 value6 value7 value8\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test
    public void test06c_parseTable_InputWithSuffixes_SuffixesRemoved() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1 key2 key3 key4\n" + "value1* value2$ value3~ value4&\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "*", "~", "$", "&");

        assertEquals(expected, result);
    }

    @Test
    public void test06d_parseTable_WhiteSpaceInInput_WhiteSpaceRemoved() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1    key2    key3    key4     \n" + "value1   value2   value3   value4     \n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test
    public void test06e_parseTable_AlternateSeparatorAndTrailingWhiteSpace_NormalResult() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1  |  key2  |  key3  |  key4     \n" + "value1 |  value2 |  value3  | value4    \n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.BAR_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test(expected = XenonException.class)
    public void test06f_parseTable_EmptyHeaderField_ThrowsException() throws Exception {
        String input = "key1||key2|key3|key4\n";

        ScriptingParser.parseTable(input, "key1", ScriptingParser.BAR_REGEX, "fake");
    }

    @Test(expected = XenonException.class)
    public void test06g_parseTable_EmptyInput_ThrowsException() throws Exception {
        String input = "";

        ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake");
    }

    @Test(expected = XenonException.class)
    public void test06h_parseTable_LessFieldsInRecordThanInHeader_ThrowsException() throws Exception {
        String input = "key1 key2 key3 key4\n" + "value1 value2 value3\n";

        ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake");
    }

    @Test(expected = XenonException.class)
    public void test06i_parseTable_MoreFieldsInRecordThanInHeader_ThrowsException() throws Exception {
        String input = "key1 key2 key3 key4\n" + "value1 value2 value3 value4 value5\n";

        ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake");
    }

    @Test(expected = XenonException.class)
    public void test06j_parseTable_GivenKeyFieldNotTable_ThrowsException() throws Exception {
        String input = "key1 key2 key3 key4\n" + "value1 value2 value3 value4\n";

        ScriptingParser.parseTable(input, "key0", ScriptingParser.WHITESPACE_REGEX, "fake");
    }

    @Test
    public void test06k_parseTable_HorizontalSeparator_NormalResult() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1    key2    key3    key4     \n" + "------  ------  ------  ----------\n" + "value1  value2  value3  value4    \n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test
    public void test06l_parseTable_DoubleHorizontalSeparator_NormalResult() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "==================================\n" + "key1    key2    key3    key4      \n" + "----------------------------------\n"
                + "value1  value2  value3  value4    \n" + "==================================\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "key1", ScriptingParser.WHITESPACE_REGEX, "fake", "$");

        assertEquals(expected, result);
    }

    @Test
    public void test06m_parseTable_RealSlurmTableWithTuple1() throws Exception {

        String input = "JOBID PARTITION NAME USER STATE TIME TIME_LIMIT NODES NODELIST(REASON) COMMENT\n"
                + "1100870 defq hostname jason COMPLETING 0:00 15:00 1 node019 d711f8a0-46bb-445c-84a7-acf0aa27496a\n";

        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord1 = new HashMap<>();
        expectedRecord1.put("JOBID", "1100870");
        expectedRecord1.put("PARTITION", "defq");
        expectedRecord1.put("NAME", "hostname");
        expectedRecord1.put("USER", "jason");
        expectedRecord1.put("STATE", "COMPLETING");
        expectedRecord1.put("TIME", "0:00");
        expectedRecord1.put("TIME_LIMIT", "15:00");
        expectedRecord1.put("NODES", "1");
        expectedRecord1.put("NODELIST(REASON)", "node019");
        expectedRecord1.put("COMMENT", "d711f8a0-46bb-445c-84a7-acf0aa27496a");

        expected.put("1100870", expectedRecord1);

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JOBID", ScriptingParser.WHITESPACE_REGEX, "slurm-test", "*", "~");

        assertEquals(expected, result);
    }

    @Test
    public void test06m_parseTable_RealSlurmTableWithTuple2() throws Exception {

        String input = "JOBID PARTITION NAME USER STATE TIME TIME_LIMIT NODES NODELIST(REASON) COMMENT\n"
                + "1091452 das4 prun-job mdn355 PENDING 0:00 7-00:01:00 1 (ReqNodeNotAvail, UnavailableNodes:node[070-071,076-084]) (null)\n";

        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord1 = new HashMap<>();
        expectedRecord1.put("JOBID", "1091452");
        expectedRecord1.put("PARTITION", "das4");
        expectedRecord1.put("NAME", "prun-job");
        expectedRecord1.put("USER", "mdn355");
        expectedRecord1.put("STATE", "PENDING");
        expectedRecord1.put("TIME", "0:00");
        expectedRecord1.put("TIME_LIMIT", "7-00:01:00");
        expectedRecord1.put("NODES", "1");
        expectedRecord1.put("NODELIST(REASON)", "(ReqNodeNotAvail, UnavailableNodes:node[070-071,076-084])");
        expectedRecord1.put("COMMENT", "(null)");

        expected.put("1091452", expectedRecord1);

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JOBID", ScriptingParser.WHITESPACE_REGEX, "slurm-test", "*", "~");

        assertEquals(expected, result);
    }

    @Test
    public void test06m_parseTable_RealSlurmTableWithTuple3() throws Exception {

        String input = "JOBID PARTITION NAME USER STATE TIME TIME_LIMIT NODES NODELIST(REASON) COMMENT\n"
                + "1090974 das4 prun-job mdn355 PENDING 0:00 7-00:01:00 1 (launch failed requeued held) (null)";

        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord1 = new HashMap<>();
        expectedRecord1.put("JOBID", "1090974");
        expectedRecord1.put("PARTITION", "das4");
        expectedRecord1.put("NAME", "prun-job");
        expectedRecord1.put("USER", "mdn355");
        expectedRecord1.put("STATE", "PENDING");
        expectedRecord1.put("TIME", "0:00");
        expectedRecord1.put("TIME_LIMIT", "7-00:01:00");
        expectedRecord1.put("NODES", "1");
        expectedRecord1.put("NODELIST(REASON)", "(launch failed requeued held)");
        expectedRecord1.put("COMMENT", "(null)");

        expected.put("1090974", expectedRecord1);

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JOBID", ScriptingParser.WHITESPACE_REGEX, "slurm-test", "*", "~");

        assertEquals(expected, result);
    }

    @Test
    public void test06m_parseTable_RealSlurmTableWithTuple4() throws Exception {

        String input = "JOBID PARTITION NAME USER STATE TIME TIME_LIMIT NODES NODELIST(REASON) COMMENT\n"
                + "1100838 defq prun-job sma2 RUNNING 5:26:45 1-00:01:00 50 node[009-018,029-068] (null)";

        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord1 = new HashMap<>();
        expectedRecord1.put("JOBID", "1100838");
        expectedRecord1.put("PARTITION", "defq");
        expectedRecord1.put("NAME", "prun-job");
        expectedRecord1.put("USER", "sma2");
        expectedRecord1.put("STATE", "RUNNING");
        expectedRecord1.put("TIME", "5:26:45");
        expectedRecord1.put("TIME_LIMIT", "1-00:01:00");
        expectedRecord1.put("NODES", "50");
        expectedRecord1.put("NODELIST(REASON)", "node[009-018,029-068]");
        expectedRecord1.put("COMMENT", "(null)");

        expected.put("1100838", expectedRecord1);

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JOBID", ScriptingParser.WHITESPACE_REGEX, "slurm-test", "*", "~");

        assertEquals(expected, result);
    }

    @Test
    public void test07a_checkIfContains_DoesContain_Index() throws XenonException {

        String input = "which one will it contain?";

        int result = ScriptingParser.checkIfContains(input, "fake", "probably", "one", "or", "the", "other");

        assertEquals(1, result);
    }

    @Test(expected = XenonException.class)
    public void test07b_checkIfContains_DoesNotContain_ExceptionThrown() throws XenonException {

        String input = "which one will it contain?";

        ScriptingParser.checkIfContains(input, "fake", "hopefully", "something");
    }

    @Test
    public void test08_parseList() {
        String input = "some string    with a \t\t\t lot \n\n\n\n\nof whitespace";
        String[] expected = new String[] { "some", "string", "with", "a", "lot", "of", "whitespace" };

        String[] result = ScriptingParser.parseList(input);

        assertArrayEquals("lists not parsed correctly", expected, result);
    }

    @Test
    public void test09a_parseKeyValueRecords_SingleRecord_ResultMap() throws XenonException {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1 = value1\nkey2 = value2\nkey3=value3\nkey4 = value4";

        Map<String, Map<String, String>> result = ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake");

        assertEquals(expected, result);
    }

    @Test
    public void test09b_parseKeyValueRecords_MultiRecord_ResultMap() throws XenonException {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        Map<String, String> expectedRecord2 = new HashMap<>();
        expectedRecord2.put("key1", "value5");
        expectedRecord2.put("key2", "value6");
        expectedRecord2.put("key3", "value7");
        expectedRecord2.put("key4", "value8");
        expected.put("value5", expectedRecord2);

        String input = "key1 = value1\nkey2 = value2\nkey3=value3\nkey4 = value4" + "\nkey1 = value5\nkey2 = value6\nkey3=value7\nkey4 = value8";

        Map<String, Map<String, String>> result = ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake");

        assertEquals(expected, result);
    }

    @Test
    public void test09c_parseKeyValueRecords_WhiteSpace_Ignored() throws XenonException {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        String input = "key1 = value1   \nkey2   =  value2\nkey3\t=\tvalue3  \n  key4 = value4    ";

        Map<String, Map<String, String>> result = ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake");

        assertEquals(expected, result);
    }

    @Test
    public void test09d_parseKeyValueRecords_EmptyLines_Ignored() throws XenonException {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        Map<String, String> expectedRecord2 = new HashMap<>();
        expectedRecord2.put("key1", "value5");
        expectedRecord2.put("key2", "value6");
        expectedRecord2.put("key3", "value7");
        expectedRecord2.put("key4", "value8");
        expected.put("value5", expectedRecord2);

        String input = "\n\n\n\n\n\n\n\nkey1 = value1\nkey2 = value2\nkey3=value3\nkey4 = value4\n\n"
                + "\nkey1 = value5\nkey2 = value6\nkey3=value7\nkey4 = value8\n\n\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake");

        assertEquals(expected, result);
    }

    @Test
    public void test09e_parseKeyValueRecords_IgnoreLines_Ignored() throws XenonException {
        Map<String, Map<String, String>> expected = new HashMap<>();

        Map<String, String> expectedRecord = new HashMap<>();
        expectedRecord.put("key1", "value1");
        expectedRecord.put("key2", "value2");
        expectedRecord.put("key3", "value3");
        expectedRecord.put("key4", "value4");
        expected.put("value1", expectedRecord);

        Map<String, String> expectedRecord2 = new HashMap<>();
        expectedRecord2.put("key1", "value5");
        expectedRecord2.put("key2", "value6");
        expectedRecord2.put("key3", "value7");
        expectedRecord2.put("key4", "value8");
        expected.put("value5", expectedRecord2);

        String input = "ignore this line as it is a header\nkey1 = value1\nkey2 = value2\nkey3=value3\nkey4 = value4\n"
                + "and this too as it is just status output" + "\nkey1 = value5\nkey2 = value6\nkey3=value7\nkey4 = value8";

        Map<String, Map<String, String>> result = ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake", "ignore this line",
                "and this too");

        assertEquals(expected, result);
    }

    @Test(expected = XenonException.class)
    public void test09f_parseKeyValueRecords_NoValueForKey_ThrowsException() throws XenonException {
        String input = "key1 = value1\nkey2";

        ScriptingParser.parseKeyValueRecords(input, "key1", ScriptingParser.EQUALS_REGEX, "fake");
    }

    @Test(expected = XenonException.class)
    public void test09g_parseKeyValueRecords_KeyNotOnFirstLine_ThrowsException() throws XenonException {
        String input = "key1 = value1\n";

        ScriptingParser.parseKeyValueRecords(input, "key0", ScriptingParser.EQUALS_REGEX, "fake");
    }

    @Test
    public void test_parseList_emptystring_emptylist() {
        String[] out = ScriptingParser.parseList("");
        assertArrayEquals(new String[0], out);
    }

    @Test
    public void test_RealSlurm1() throws Exception {

        String input = "JOBID PARTITION NAME USER STATE TIME TIME_LIMIT NODES NODELIST(REASON) COMMENT\n"
                + "2463618_[139-695%2] defq app_set.lst ajwijs PENDING 0:00 6:00:00 1 (JobArrayTaskLimit) (null)\n"
                + "2440177 defq prun-job uji300 RUNNING 14-20:53:27 30-00:01:00 1 node028 (null)\n"
                + "2442075 defq prun-job rutger RUNNING 8-02:42:04 12-00:01:00 1 node027 (null)\n"
                + "2457958 proq densenetvae ama228 RUNNING 2-18:20:54 20-20:00:00 1 node078 (null)\n"
                + "2457959 proq resnetvae ama228 RUNNING 2-18:20:51 20-20:00:00 1 node069 (null)\n"
                + "2457960 proq fullydensenetvae ama228 RUNNING 2-18:20:51 20-20:00:00 1 node070 (null)\n"
                + "2462709 proq acquire_cluster.sh mreisser RUNNING 23:25:02 3-00:00:00 1 node071 (null)\n"
                + "2463513 defq bash mreisser RUNNING 18:36:52 2-00:00:00 1 node005 (null)\n"
                + "2463618_138 defq app_set.lst ajwijs RUNNING 4:05:18 6:00:00 1 node067 (null)\n"
                + "2463618_137 defq app_set.lst ajwijs RUNNING 4:13:29 6:00:00 1 node053 (null)\n"
                + "2467231 defq bash sghiassi RUNNING 11:25:54 12:00:00 1 node001 (null)\n"
                + "2472928 defq prun-job pmms2014 RUNNING 2:43:01 5:01:00 1 node068 (null)\n"
                + "2478082 defq prun-job lps232 RUNNING 28:19 1:31:00 1 node024 (null)\n" + "2480244 defq prun-job rwr450 RUNNING 5:34 16:00 1 node025 (null)\n"
                + "2480326 defq prun-job pmms2070 RUNNING 3:57 2:01:00 1 node055 (null)\n"
                + "2480397 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node054 (null)\n"
                + "2480398 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node056 (null)\n"
                + "2480399 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node057 (null)\n"
                + "2480400 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node058 (null)\n"
                + "2480401 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node060 (null)\n"
                + "2480402 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node061 (null)\n"
                + "2480403 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node062 (null)\n"
                + "2480404 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node063 (null)\n"
                + "2480405 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node064 (null)\n"
                + "2480406 defq prun-job pmms2016 RUNNING 1:02 16:00 1 node065 (null)\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JOBID", ScriptingParser.WHITESPACE_REGEX, "slurm-test", "*", "~");

        assertNotNull(result);
        assertEquals(result.size(), 25);
    }

    @Test
    public void test_RealSlurm2() throws Exception {

        String input = "JobID|JobName|Partition|NTasks|Elapsed|State|ExitCode|AllocCPUS|DerivedExitCode|Submit|Suspended|Start|User|End|NNodes|Timelimit|Comment|Priority|\n"
                + "2440177|prun-job|defq||14-21:09:35|RUNNING|0:0|32|0:0|2020-02-10T13:53:51|00:00:00|2020-02-10T13:53:52|uji300|Unknown|1|30-00:01:00||4294248634|\n"
                + "2480397|prun-job|defq||00:01:03|CANCELLED by 2242|0:0|32|0:0|2020-02-25T10:46:16|00:00:00|2020-02-25T10:46:17|pmms2016|2020-02-25T10:47:20|1|00:16:00||4294209256|\n"
                + "2480405|prun-job|defq||00:01:04|CANCELLED by 2242|0:0|32|0:0|2020-02-25T10:46:16|00:00:00|2020-02-25T10:46:17|pmms2016|2020-02-25T10:47:21|1|00:16:00||4294209248|\n";

        Map<String, Map<String, String>> result = ScriptingParser.parseTable(input, "JobID", ScriptingParser.BAR_REGEX, "slurm-test", "*", "~");

        assertNotNull(result);
        assertEquals(result.size(), 3);
    }

    @Test
    public void test_RealSlurm3() throws Exception {

        String input = "JobId=2440177 JobName=prun-job\n" + "UserId=uji300(1246) GroupId=uji300(1246) MCS_label=N/A\n"
                + "Priority=4294248634 Nice=0 Account=uji300 QOS=normal\n" + "JobState=RUNNING Reason=None Dependency=(null)\n"
                + "Requeue=1 Restarts=0 BatchFlag=1 Reboot=0 ExitCode=0:0\n" + "RunTime=14-21:15:01 TimeLimit=30-00:01:00 TimeMin=N/A\n"
                + "SubmitTime=2020-02-10T13:53:51 EligibleTime=2020-02-10T13:53:51\n"
                + "StartTime=2020-02-10T13:53:52 EndTime=2020-03-11T13:54:52 Deadline=N/A\n" + "PreemptTime=None SuspendTime=None SecsPreSuspend=0\n"
                + "Partition=defq AllocNode:Sid=fs0:31914\n" + "ReqNodeList=(null) ExcNodeList=(null)\n" + "NodeList=node028\n" + "BatchHost=node028\n"
                + "NumNodes=1 NumCPUs=32 NumTasks=1 CPUs/Task=1 ReqB:S:C:T=0:0:*:*\n" + "TRES=cpu=32,node=1\n"
                + "Socks/Node=* NtasksPerN:B:S:C=1:0:*:* CoreSpec=*\n" + "MinCPUsNode=1 MinMemoryNode=0 MinTmpDiskNode=0\n"
                + "Features=gpunode DelayBoot=00:00:00\n" + "Gres=(null) Reservation=(null)\n"
                + "OverSubscribe=NO Contiguous=0 Licenses=(null) Network=(null)\n" + "Command=(null)\n" + "WorkDir=/home/uji300/OpenKE\n" + "StdErr=/dev/null\n"
                + "StdIn=/dev/null\n" + "StdOut=/dev/null\n" + "Power=\n";

        Map<String, String> result = ScriptingParser.parseKeyValuePairs(input, "slurm-test", "WorkDir=", "Command=");

        assertNotNull(result);
        assertEquals(result.get("JobId"), "2440177");
        assertEquals(result.get("JobState"), "RUNNING");
    }
}
