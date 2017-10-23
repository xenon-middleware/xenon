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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
//import org.apache.commons.io.Charsets;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.IncompatibleVersionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GridEngineXmlParserTest {

    private static String readFile(String pathName) {
        InputStream is = GridEngineXmlParserTest.class.getResourceAsStream(pathName);
        // we read until end of file, delimited by \\A
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Test
    public void test01a_checkVersion() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseDocument(input);
    }

    @Test(expected = IncompatibleVersionException.class)
    public void test01b_checkVersion_NoSchema_Exception() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-no-schema.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseDocument(input);
    }

    @Test(expected = IncompatibleVersionException.class)
    public void test01d_checkVersion_WrongSchema_ExceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-wrong-schema.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseDocument(input);
    }

    @Test
    public void test01d_checkVersion_WrongSchemaIgnoreVersion_Ignored() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-wrong-schema.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(true);

        parser.parseDocument(input);
    }

    @Test(expected = XenonException.class)
    public void test01c_checkVersion_EmptyFile_ExceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-empty.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseDocument(input);
    }

    @Test
    public void test02a_parseQueueInfo_SomeQueues_Result() throws Throwable {

        String input = readFile("/fixtures/gridengine/queues.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        Map<String, Map<String, String>> result = parser.parseQueueInfos(input);

        String[] queues = result.keySet().toArray(new String[result.size()]);
        Arrays.sort(queues);

        assertArrayEquals(new Object[] { "all.q", "das3.q", "disabled.q", "fat.q", "gpu.q" }, queues);
    }

    @Test(expected = XenonException.class)
    public void test02b_parseQueueInfo_NoQueues_ExceptionThrown() throws Throwable {

        String input = readFile("/fixtures/gridengine/queues-no-queues.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseQueueInfos(input);
    }

    @Test(expected = XenonException.class)
    public void test02c_parseQueueInfo_NoQueues_ExceptionThrown() throws Throwable {

        String input = readFile("/fixtures/gridengine/queues-no-queues.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseQueueInfos(input);
    }

    @Test(expected = XenonException.class)
    public void test02d_parseQueueInfo_queueEmptyName_exceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/queues-queue-empty-name.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseQueueInfos(input);
    }

    @Test(expected = XenonException.class)
    public void test02e_parseQueueInfo_queueWithoutName_exceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/queues-queue-without-name.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseQueueInfos(input);
    }

    @Test
    public void test03a_parseJobInfo_SomeJobs_Result() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        String[] expectedJobIDs = new String[] { "583111", "583235", "583238", "583244", "583246", "583296", "583320", "583325", "583302" };
        Arrays.sort(expectedJobIDs);

        Map<String, Map<String, String>> result = parser.parseJobInfos(input);

        assertNotNull(result);
        String[] resultJobIDs = result.keySet().toArray(new String[result.size()]);
        Arrays.sort(resultJobIDs);

        assertArrayEquals(expectedJobIDs, resultJobIDs);
    }

    @Test(expected = XenonException.class)
    public void test03b_parseJobInfo_jobEmptyJobNumber_exceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-empty-jobnumber.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseJobInfos(input);
    }

    @Test(expected = XenonException.class)
    public void test03c_parseJobInfo_jobWithoutJobNumber_exceptionThrown() throws Throwable {
        String input = readFile("/fixtures/gridengine/jobs-without-jobnumber.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.parseJobInfos(input);
    }
}
