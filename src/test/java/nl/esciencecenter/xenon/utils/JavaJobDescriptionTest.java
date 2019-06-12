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
package nl.esciencecenter.xenon.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonRuntimeException;

public class JavaJobDescriptionTest {
    private JavaJobDescription description;

    @Before
    public void setUp() {
        description = new JavaJobDescription();
    }

    @Test
    public void getExecutable_defaultsToJava() throws Exception {
        String executable = description.getExecutable();

        String expected = "java";
        assertEquals(expected, executable);
    }

    @Test
    public void setExecutable_customPath() throws Exception {
        description.setExecutable("/opt/bin/java");

        String executable = description.getExecutable();
        String expected = "/opt/bin/java";
        assertEquals(expected, executable);
    }

    @Test
    public void setJavaMain_mainClass_inArguments() throws Exception {
        String mainClass = "nl.esciencecenter.projectx.joby";

        description.setJavaMain(mainClass);

        List<String> args = description.getArguments();
        List<String> expected = Collections.singletonList(mainClass);
        assertEquals(expected, args);
    }

    @Test
    public void setJavaArguments_mainClassWithArg_inArguments() throws Exception {
        String mainClass = "nl.esciencecenter.projectx.joby";
        description.setJavaMain(mainClass);

        description.setJavaArguments("arg1");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList(mainClass, "arg1");
        assertEquals(expected, args);
    }

    @Test
    public void addJavaArgument_mainClassWithArg_inArguments() throws Exception {
        String mainClass = "nl.esciencecenter.projectx.joby";
        description.setJavaMain(mainClass);

        description.addJavaArgument("arg1");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList(mainClass, "arg1");
        assertEquals(expected, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaArgument_emptyString_exception() {
        description.addJavaArgument("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaArgument_null_exception() {
        description.addJavaArgument(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaClasspathElement_emptyString_exception() {
        description.addJavaClasspathElement("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaClasspathElement_null_exception() {
        description.addJavaClasspathElement(null);
    }

    @Test(expected = XenonRuntimeException.class)
    public void setArguments_throwsException() {
        description.setArguments("arg1");
    }

    @Test
    public void addJavaOption_jarFile_inArguments() {
        description.addJavaOption("-jar app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Collections.singletonList("-jar app.jar");
        assertEquals(expected, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaOption_emptyString_exception() {
        description.addJavaOption("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJavaOption_null_exception() {
        description.addJavaOption(null);
    }

    @Test
    public void setJavaOptions_jarFile_inArguments() {
        description.setJavaOptions("-jar app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Collections.singletonList("-jar app.jar");
        assertEquals(expected, args);
    }

    @Test
    public void addJavaSystemProperty_verboseProp_inArguments() {
        description.addJavaSystemProperty("verbose", "very");

        List<String> args = description.getArguments();
        List<String> expected = Collections.singletonList("-Dverbose=very");
        assertEquals(expected, args);
    }

    @Test
    public void setJavaSystemProperties_verboseProp_inArguments() {
        Map<String, String> props = new HashMap<>();
        props.put("verbose", "very");

        description.setJavaSystemProperties(props);

        List<String> args = description.getArguments();
        List<String> expected = Collections.singletonList("-Dverbose=very");
        assertEquals(expected, args);
    }

    @Test
    public void addJavaClasspathElement_jarFile_inArguments() {
        description.addJavaClasspathElement("/app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList("-classpath", "/app.jar");
        assertEquals(expected, args);
    }

    @Test
    public void setJavaClasspathElement_jarFile_inArguments() {
        description.setJavaClasspath("/app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList("-classpath", "/app.jar");
        assertEquals(expected, args);
    }

    @Test
    public void setJavaClasspathElement_jarFiles_inArguments() {
        assumeFalse(LocalFileSystemUtils.isWindows());
        description.setJavaClasspath("/deps.jar", "/app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList("-classpath", "/deps.jar:/app.jar");
        assertEquals(expected, args);
    }

    @Test
    public void setJavaClasspathElement_jarFiles_inArguments_windows() {
        assumeTrue(LocalFileSystemUtils.isWindows());
        description.setJavaClasspath("/deps.jar", "/app.jar");

        List<String> args = description.getArguments();
        List<String> expected = Arrays.asList("-classpath", "/deps.jar;/app.jar");
        assertEquals(expected, args);
    }

    @Test
    public void toString_default() {
        String result = description.toString();

        String expected = "JavaJobDescription [javaOptions=[], javaSystemProperties={}, javaMain=null, javaArguments=[], javaClassPath=[], queueName=null, executable=java, stdin=null, stdout=null, stderr=null, workingDirectory=null, environment={}, tasks=1, coresPerTask=1, maxTime=-1]";
        assertEquals(expected, result);
    }

    @Test
    public void toString_kitchenSink() {
        // configure base description
        description.setWorkingDirectory("/workdir");
        description.addEnvironment("CI", "1");
        // configure java description
        description.setJavaClasspath("/opt/xenon.jar", "/opt/xenon-cli.jar");
        description.setJavaMain("nl.esciencecenter.xenon.cli.Main");
        description.setJavaArguments("filesystem", "file", "list", "/etc");
        description.setJavaOptions("-server");
        description.addJavaSystemProperty("xenon.someprop", "somevalue");

        String result = description.toString();

        String expected = "JavaJobDescription [javaOptions=[-server], javaSystemProperties={xenon.someprop=somevalue}, javaMain=nl.esciencecenter.xenon.cli.Main, javaArguments=[filesystem, file, list, /etc], javaClassPath=[/opt/xenon.jar, /opt/xenon-cli.jar], queueName=null, executable=java, stdin=null, stdout=null, stderr=null, workingDirectory=/workdir, environment={CI=1}, tasks=1, coresPerTask=1, maxTime=-1]";
        assertEquals(expected, result);
    }

    @Test
    public void equals_default() {
        assertTrue(description.equals(new JavaJobDescription()));
    }

    @Test
    public void equals_self() {
        assertTrue(description.equals(description));
    }

    @Test
    public void equals_diffArg() {
        JavaJobDescription description2 = new JavaJobDescription();
        description2.addJavaArgument("arg1");

        assertFalse(description.equals(description2));
    }

    @Test
    public void equals_string() {
        assertFalse(description.equals("foo"));
    }

    @Test
    public void hashcode_self() {
        assertEquals(description.hashCode(), description.hashCode());
    }

    @Test
    public void hashcode_diffArg() {
        JavaJobDescription description2 = new JavaJobDescription();
        description2.addJavaArgument("arg1");

        assertNotEquals(description2.hashCode(), description.hashCode());
    }
}
