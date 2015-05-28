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
package nl.esciencecenter.xenon.adaptors.torque;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueXmlParserTest {

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    public void test01a_checkVersion() throws Throwable {
        String input = readFile("test/fixtures/torque/jobs.xml");
        new TorqueXmlParser().parseDocument(input);
    }

    @Test(expected = XenonException.class)
    public void test01c_checkVersion_EmptyFile_ExceptionThrown() throws Throwable {
        String input = readFile("test/fixtures/torque/jobs-empty.xml");
        new TorqueXmlParser().parseDocument(input);
    }

    @Test
    public void test03a_parseJobInfo_SomeJobs_Result() throws Throwable {
        String input = readFile("test/fixtures/torque/jobs.xml");

        String[] expectedJobIDs = new String[] {
            "8921165.batch1.lisa.surfsara.nl",
            "8931330.batch1.lisa.surfsara.nl",
            "8938236.batch1.lisa.surfsara.nl",
            "8941161.batch1.lisa.surfsara.nl",
            "8941948.batch1.lisa.surfsara.nl",
            "8942464.batch1.lisa.surfsara.nl",
            "8954523.batch1.lisa.surfsara.nl",
            };
        Arrays.sort(expectedJobIDs);

        Map<String, Map<String, String>> result = new TorqueXmlParser().parseJobInfos(input);

        assertNotNull(result);
        String[] resultJobIDs = result.keySet().toArray(new String[result.size()]);
        Arrays.sort(resultJobIDs);

        assertArrayEquals(expectedJobIDs, resultJobIDs);
    }

    @Test(expected = XenonException.class)
    public void test03b_parseJobInfo_jobEmptyJobNumber_exceptionThrown() throws Throwable {
        String input = readFile("test/fixtures/torque/jobs-empty-jobnumber.xml");
        new TorqueXmlParser().parseJobInfos(input);
    }

    @Test(expected = XenonException.class)
    public void test03c_parseJobInfo_jobWithoutJobNumber_exceptionThrown() throws Throwable {
        String input = readFile("test/fixtures/torque/jobs-without-jobnumber.xml");
        new TorqueXmlParser().parseJobInfos(input);
    }

    @Test
    public void test04a_recursiveMap() throws Throwable {
        String input = readFile("test/fixtures/torque/propertymap.xml");
        TorqueXmlParser parser = new TorqueXmlParser();
        Document document = parser.parseDocument(input);
        Map<String, String> result = Utils.emptyMap(2);
        parser.recursiveMapFromElement(document.getDocumentElement(), result);
        
        Map<String, String> expectedResult = Utils.emptyMap(2);
        expectedResult.put("a", "1");
        expectedResult.put("b", "2");
        assertEquals(expectedResult, result);
    }

    @Test
    public void test04b_recursiveMapRecurse() throws Throwable {
        String input = readFile("test/fixtures/torque/propertymap-recurse.xml");
        TorqueXmlParser parser = new TorqueXmlParser();
        Document document = parser.parseDocument(input);
        Map<String, String> result = Utils.emptyMap(3);
        parser.recursiveMapFromElement(document.getDocumentElement(), result);
        
        Map<String, String> expectedResult = Utils.emptyMap(3);
        expectedResult.put("a", "1");
        expectedResult.put("b", "2");
        expectedResult.put("d", "3");
        assertEquals(expectedResult, result);
    }

    @Test
    public void test04c_recursiveMapOverwrite() throws Throwable {
        String input = readFile("test/fixtures/torque/propertymap-recurse-overwrite.xml");
        TorqueXmlParser parser = new TorqueXmlParser();
        Document document = parser.parseDocument(input);
        Map<String, String> result = Utils.emptyMap(2);
        parser.recursiveMapFromElement(document.getDocumentElement(), result);
        
        Map<String, String> expectedResult = Utils.emptyMap(2);
        expectedResult.put("a", "3");
        expectedResult.put("b", "2");
        assertEquals(expectedResult, result);
    }

    @Test
    public void test04d_recursiveMapWithValue() throws Throwable {
        String input = readFile("test/fixtures/torque/propertymap-recurse-with-value.xml");
        TorqueXmlParser parser = new TorqueXmlParser();
        Document document = parser.parseDocument(input);
        Map<String, String> result = Utils.emptyMap(3);
        parser.recursiveMapFromElement(document.getDocumentElement(), result);
        
        Map<String, String> expectedResult = Utils.emptyMap(3);
        expectedResult.put("a", "3");
        expectedResult.put("b", "2");
        expectedResult.put("c", "4");
        assertEquals(expectedResult, result);
    }
}
