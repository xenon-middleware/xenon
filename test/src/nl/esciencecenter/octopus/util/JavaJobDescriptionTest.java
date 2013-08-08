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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.JobDescription;

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
        assertNotNull(optionsList);
        assertArrayEquals(new String[] { "a", "b", "c", "d" }, optionsList.toArray(new String[4]));

        //system properties

        Map<String, String> in = new HashMap<String, String>();
        in.put("some.key", "some.value");
        j.setJavaSystemProperties(in);
        j.addJavaSystemProperty("other.key", "other.value");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("some.key", "some.value");
        expected.put("other.key", "other.value");

        Map<String, String> out = j.getJavaSystemProperties();
        assertEquals(expected, out);

        //main class

        j.setJavaMain("aap");
        String main = j.getJavaMain();
        assertEquals("aap", main);

        //arguments

        j.setJavaArguments("d", "e", "f");
        j.addJavaArgument("g");
        List<String> argumentList = j.getJavaArguments();
        assertNotNull(argumentList);
        assertArrayEquals(new String[] { "d", "e", "f", "g" }, argumentList.toArray(new String[0]));

        //class path

        j.setJavaClasspath("h", "i", "j");
        j.addJavaClasspathElement("k");
        List<String> classpathList = j.getJavaClasspath();
        assertNotNull(classpathList);
        assertArrayEquals(new String[] { "h", "i", "j", "k" }, classpathList.toArray(new String[0]));
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
    public void test_addClasspathElement_Null_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaClasspathElement(null);
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void test_addClasspathElement_Empty_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.addJavaClasspathElement("");
    }

    @org.junit.Test(expected = OctopusRuntimeException.class)
    public void test_setArguments_AnyArgument_Exception() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.setArguments("some", "arguments");
    }

    @org.junit.Test
    public void test_setExecutable_Null_Java() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.setExecutable(null);

        String executable = j.getExecutable();

        assertEquals("java", executable);
    }

    @org.junit.Test
    public void test_setExecutable_Something_Something() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        j.setExecutable("something");

        String executable = j.getExecutable();

        assertEquals("something", executable);
    }

    @org.junit.Test
    public void test_getArguments() throws Exception {
        JavaJobDescription j = new JavaJobDescription();

        j.setJavaArguments("argument");
        j.addJavaSystemProperty("property.key", "property.value");
        j.setJavaMain("nl.esciencecenter.main.class");
        j.setJavaOptions("-Xtesting=true");
        j.setJavaClasspath("element1", "element2", "element3");

        //use a strange path separator to test if this is properly handled
        List<String> arguments = j.getArguments('_');

        String[] expected =
                new String[] { "-Xtesting=true", "-classpath", "element1_element2_element3", "-Dproperty.key=property.value",
                        "nl.esciencecenter.main.class", "argument" };

        assertNotNull(arguments);
        assertArrayEquals(expected, arguments.toArray(new String[0]));
    }

    @org.junit.Test
    public void test_getArguments_NoMain() throws Exception {
        JavaJobDescription j = new JavaJobDescription();

        j.setJavaMain(null);
        j.setJavaOptions("-jar", "somefile.jar");

        //use a strange path separator to test if this is properly handled
        List<String> arguments = j.getArguments();

        String[] expected = new String[] { "-jar", "somefile.jar" };

        assertNotNull(arguments);
        assertArrayEquals(expected, arguments.toArray(new String[0]));
    }

    @org.junit.Test
    public void test_toString() throws Exception {
        JavaJobDescription j = new JavaJobDescription();

        j.setJavaArguments("argument");
        j.addJavaSystemProperty("property.key", "property.value");
        j.setJavaMain("nl.esciencecenter.main.class");
        j.setJavaOptions("-Xtesting=true");
        j.setJavaClasspath("element1", "element2", "element3");

        j.setInteractive(true);
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdin(null);
        j.setStdout("stdout");
        j.setStderr("stderr");
        j.setExecutable("exec");

        String expected =
                "JavaJobDescription [javaOptions=[-Xtesting=true], javaSystemProperties={property.key=property.value},"
                        + " javaMain=nl.esciencecenter.main.class, javaArguments=[argument], javaClassPath=[element1, element2, element3],"
                        + " queueName=noot, executable=exec, stdin=null, stdout=stdout, stderr=stderr, workingDirectory=aap,"
                        + " environment={}, jobOptions={}, nodeCount=1, processesPerNode=1, maxTime=15, interactive=true]";

        assertEquals(expected, j.toString());
    }

    @org.junit.Test
    public void test_equals() throws Exception {
        JavaJobDescription one = new JavaJobDescription();
        JavaJobDescription other = new JavaJobDescription();

        assertTrue(one.equals(one));
        assertFalse(one.equals(null));
        assertFalse(one.equals("AAP"));
        assertFalse(one.equals(new JobDescription()));
        assertTrue(one.equals(other));
        
        one.setExecutable("bla");
        assertFalse(one.equals(other));
        other.setExecutable("bla");
        assertTrue(one.equals(other));
        
        one.setJavaOptions("some", "options");
        assertFalse(one.equals(other));
        other.setJavaOptions("some", "options");
        assertTrue(one.equals(other));

        one.setJavaArguments("some", "arguments");
        assertFalse(one.equals(other));
        other.setJavaArguments("some", "arguments");
        assertTrue(one.equals(other));

        one.addJavaSystemProperty("some.property", "some.value");
        assertFalse(one.equals(other));
        other.addJavaSystemProperty("some.property", "some.value");
        assertTrue(one.equals(other));

        one.setJavaMain("main.class");
        assertFalse(one.equals(other));
        other.setJavaMain("main.class");
        assertTrue(one.equals(other));
        
        one.setJavaMain(null);
        assertFalse(one.equals(other));
        other.setJavaMain(null);
        assertTrue(one.equals(other));
        
        one.setJavaClasspath("some", "elements");
        assertFalse(one.equals(other));
        other.setJavaClasspath("some", "elements");
        assertTrue(one.equals(other));

    }
    
    @org.junit.Test
    public void test_hashcode_empty() throws Exception {
        JavaJobDescription j = new JavaJobDescription();
        
        int prime = 31;
        
        //start with hashcode of super class
        int result = new JobDescription().hashCode();
        
        result = prime * result + new ArrayList<String>().hashCode();
        result = prime * result + new ArrayList<String>().hashCode();
        result = prime * result + 0;
        result = prime * result + new ArrayList<String>().hashCode();
        result = prime * result + new HashMap<String, String>().hashCode();

        assertEquals(result, j.hashCode());
    }
    
    @org.junit.Test
    public void test_hashcode_filled() throws Exception {
        String[] arguments = new String[] {"some", "arguments"};
        String[] options = new String[] {"some", "options"};
        String main = "main";
        String[] classpath = new String[] {"class", "path"};
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("some.key", "some.value");
        
        
        JavaJobDescription j = new JavaJobDescription();
        j.setJavaArguments(arguments);
        j.setJavaOptions(options);
        j.setJavaMain(main);
        j.setJavaClasspath(classpath);
        j.setJavaSystemProperties(properties);
        
        int prime = 31;

        //start with hashcode of super class
        int result = new JobDescription().hashCode();
        
        result = prime * result + Arrays.asList(arguments).hashCode();
        result = prime * result + Arrays.asList(classpath).hashCode();
        result = prime * result + main.hashCode();
        result = prime * result + Arrays.asList(options).hashCode();
        result = prime * result + properties.hashCode();

        assertEquals(result, j.hashCode());
    }

    
}
