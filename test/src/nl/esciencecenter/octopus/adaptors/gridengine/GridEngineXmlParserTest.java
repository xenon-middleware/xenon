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
package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.IncompatibleVersionException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import org.junit.Test;

public class GridEngineXmlParserTest {

    @Test
    public void testCheckVersion() throws Throwable {
        File testFile = new File("test/fixtures/gridengine/jobs.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.checkVersion(testFile);
    }

    @Test(expected = IncompatibleVersionException.class)
    public void testCheckVersion_NoSchema_Exception() throws Throwable {
        File testFile = new File("test/fixtures/gridengine/jobs-no-schema.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.checkVersion(testFile);
    }

    @Test(expected = IncompatibleVersionException.class)
    public void testCheckVersion_WrongSchema_Exception() throws Throwable {
        File testFile = new File("test/fixtures/gridengine/jobs-wrong-schema.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.checkVersion(testFile);
    }

    @Test(expected = OctopusIOException.class)
    public void testCheckVersion_EmptyFile_Exception() throws Throwable {
        File testFile = new File("test/fixtures/gridengine/jobs-empty.xml");

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        parser.checkVersion(testFile);
    }

    @Test
    public void testParseQueueInfo() throws Throwable {

        byte[] encoded = Files.readAllBytes(Paths.get("test/fixtures/gridengine/queues.xml"));

        String content = new String(encoded);

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        Map<String, Map<String, String>> result = parser.parseQueueInfos(content);

        //FIXME: check equality fully, not only if there are info's at all...

        String[] queues = result.keySet().toArray(new String[0]);
        Arrays.sort(queues);

        assertArrayEquals(new Object[] { "all.q", "das3.q", "disabled.q", "fat.q", "gpu.q" }, queues);
    }

    @Test
    public void testParseJobInfo() throws Throwable {
        byte[] encoded = Files.readAllBytes(Paths.get("test/fixtures/gridengine/jobs.xml"));

        String content = new String(encoded);

        System.err.println("parsing queue info from: " + content);

        GridEngineXmlParser parser = new GridEngineXmlParser(false);

        Map<String, Map<String, String>> result = parser.parseJobInfos(content);

        //FIXME: check equality fully, not only if there are info's at all...
        assertEquals(9, result.size());

    }
}