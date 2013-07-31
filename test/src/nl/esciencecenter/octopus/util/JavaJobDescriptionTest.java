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
package nl.esciencecenter.octopus.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;

/**
 * @author Niels Drost
 *
 */
public class JavaJobDescriptionTest {

    @org.junit.Test
    public void test_new() throws Exception {
        new JavaJobDescription();
    }

    
//    private final List<String> javaOptions = new ArrayList<String>();
//
//    private final Map<String, String> javaSystemProperties = new HashMap<String, String>();
//
//    private String javaMain = null;
//
//    private final List<String> javaArguments = new ArrayList<String>();
//
//    private final List<String> javaClassPath = new ArrayList<String>();
    
    @org.junit.Test
    public void test_setters_getters() throws Exception {
        JavaJobDescription j = new JavaJobDescription();

        //options
        
        j.setJavaOptions("a", "b", "c");
        j.addJavaOption("d");
        List<String> optionsList = j.getJavaOptions();
        assertTrue(optionsList != null);
        assertTrue(optionsList.size() == 4);
        assertTrue(Arrays.equals(optionsList.toArray(new String[4]), new String[] { "a", "b", "c", "d" }));
        
        //system properties
        
        Map<String, String> inMap = new HashMap<String, String>();
        inMap.put("some.key",  "some.value");
        j.setJavaSystemProperties(inMap);
        j.addJavaSystemProperty("other.key", "other.value");

        //make input map equal to expected output map
        inMap.put("other.key",  "other.value");
        
        Map<String, String> outMap = j.getJavaSystemProperties();
        assertTrue(outMap != null);
        assertTrue(outMap.size() == 2);
        assertTrue(inMap.equals(outMap));
        
        //main class
        
        j.setJavaMain("aap");
        String main = j.getJavaMain();
        assertTrue(main.equals("aap"));
        
        //arguments
        
        j.setJavaArguments("d", "e", "f");
        j.addJavaArgument("g");
        List<String> argumentList = j.getJavaArguments();
        assertTrue(argumentList != null);
        assertTrue(argumentList.size() == 4);
        assertTrue(Arrays.equals(argumentList.toArray(new String[4]), new String[] { "d", "e", "f", "g" }));

        //class path
        
        j.setJavaClasspath("h", "i", "j");
        j.addJavaClasspathElement("k");
        List<String> classpathList = j.getJavaClasspath();
        assertTrue(classpathList != null);
        assertTrue(classpathList.size() == 4);
        assertTrue(Arrays.equals(classpathList.toArray(new String[4]), new String[] { "h", "i", "j", "k" }));
    }
    
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addOption_Null_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaOption(null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addOption_Empty_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaOption("");
    }
    
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addArgument_Null_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaArgument(null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addArgument_Empty_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaArgument("");
    }


    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addClasspath_Null_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaClasspathElement(null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addClasspath_Empty_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaClasspathElement("");
    }
    
    @org.junit.Test(expected = OctopusRuntimeException.class)
    public void test_setArguments_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.setArguments("some","arguments");
    }
    
    

    
}
