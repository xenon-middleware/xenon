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
package nl.esciencecenter.xenon.adaptors.ssh;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;

import nl.esciencecenter.xenon.XenonException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author joris
 */
public class OpenSSHConfigTest {
    private static OpenSSHConfig validConfig;

    private static Reader getFixture(String resourceName) {
        return new InputStreamReader(OpenSSHConfigTest.class.getResourceAsStream(resourceName));
    }

    @BeforeClass
    public static void beforeClass() throws IOException, XenonException {
        validConfig = new OpenSSHConfig(getFixture("/fixtures/ssh/ssh_config_file"));
    }
    
    @Test
    public void parseEmptyFile() throws IOException, XenonException {
        OpenSSHConfig conf = OpenSSHConfig.parse("");
        assertNotNull(conf);
        assertNotNull(conf.getConfig("any_host"));
        assertNull(conf.getConfig("anything").getHostname());
        assertNull(conf.getConfig("anything").getUser());
        assertEquals(-1, conf.getConfig("anything").getPort());
    }

    @Test
    public void wildcardPatternStar() {
        Pattern p = OpenSSHConfig.wildcardToPattern("*");
        assertTrue(p.matcher("").matches());
        assertTrue(p.matcher("*").matches());
        assertTrue(p.matcher("abab").matches());
    }
    
    @Test
    public void wildcardPatternQ() {
        Pattern p = OpenSSHConfig.wildcardToPattern("?");
        assertTrue(p.matcher("?").matches());
        assertTrue(p.matcher("a").matches());
        assertFalse(p.matcher("").matches());
        assertFalse(p.matcher("ab").matches());
    }
    
    @Test
    public void wildcardPatternCombined() {
        Pattern p = OpenSSHConfig.wildcardToPattern("this?that*");
        assertTrue(p.matcher("thisathat").matches());
        assertTrue(p.matcher("this.that too").matches());
        assertFalse(p.matcher("thisthat").matches());
        assertFalse(p.matcher("anything").matches());
        assertFalse(p.matcher("anything").matches());
    }

    @Test
    public void wildcardPatternSymbols() {
        Pattern p = OpenSSHConfig.wildcardToPattern(")][(|^$\\d");
        assertTrue(p.matcher(")][(|^$\\d").matches());
        assertFalse(p.matcher("anything").matches());
    }
    
    @Test
    public void parseNonExistentFile() throws IOException {
        assertNotNull(validConfig);
        assertNotNull(validConfig.getConfig("non_existent"));
        assertNull(validConfig.getConfig("non_existent").getHostname());
        assertNull(validConfig.getConfig("non_existent").getUser());
        assertEquals(-1, validConfig.getConfig("non_existent").getPort());
        assertEquals("that", validConfig.getConfig("non_existent").getValue("MyGlobal"));
        assertNull(validConfig.getConfig("non_existent").getValue("#"));
    }
    
    @Test
    public void parseHost() throws IOException {
        assertNotNull(validConfig.getConfig("this"));
        assertEquals("that", validConfig.getConfig("this").getHostname());
        assertEquals("1", validConfig.getConfig("this").getUser());
        assertEquals(2, validConfig.getConfig("this").getPort());
        assertEquals("that", validConfig.getConfig("this").getValue("myglobal"));
    }
    
    @Test
    public void parseEmbeddedComment() throws IOException {
        assertEquals("that #not_a_comment", validConfig.getConfig("that").getHostname());
    }
    
    @Test
    public void parseValues() throws IOException {
        assertArrayEquals(new String[] {"that", "#not_a_comment"}, validConfig.getConfig("that").getValues("Hostname"));
    }

    @Test
    public void parseWildcardStar() throws IOException {
        assertEquals("3", validConfig.getConfig("wild3card*").getUser());
        assertEquals("3", validConfig.getConfig("wild3card").getUser());
        assertEquals("3", validConfig.getConfig("wild3cardABRA").getUser());
        assertEquals("3", validConfig.getConfig("wild3card anything goes").getUser());
    }

    @Test
    public void parseWildcardQ() throws IOException {
        assertEquals("4", validConfig.getConfig("wild?card").getUser());
        assertEquals("4", validConfig.getConfig("wildBcard").getUser());
        assertNull(validConfig.getConfig("wildABcard").getUser());
        assertNull(validConfig.getConfig("wildcard").getUser());
    }

    @Test
    public void storeFirstParsedValue() throws IOException {
        assertEquals("4", validConfig.getConfig("wild5card").getUser());
        assertEquals("4", validConfig.getConfig("wild6card").getUser());
        assertEquals("4", validConfig.getConfig("wild7card").getUser());
        assertEquals("4", validConfig.getConfig("wild card").getUser());
        assertEquals("4", validConfig.getConfig("wild8card").getUser());
    }

    @Test
    public void negate() throws IOException {
        assertEquals("5", validConfig.getConfig("wild5card_C").getUser());
        assertEquals("5", validConfig.getConfig("wild5card_ABRA").getUser());
        assertNull(validConfig.getConfig("wild5card_A").getUser());
    }

    @Test
    public void negateMultiple() throws IOException {
        assertEquals("6", validConfig.getConfig("wild6card_C").getUser());
        assertNull(validConfig.getConfig("wild6card_A").getUser());
        assertNull(validConfig.getConfig("wild6card_ABRA").getUser());
        assertNull(validConfig.getConfig("wild6card_BRAA").getUser());
        assertEquals("6", validConfig.getConfig("wild6card_RAAB").getUser());
    }

    @Test
    public void negateInQuotes() throws IOException {
        assertEquals("7", validConfig.getConfig("wild card any").getUser());
        assertNull(validConfig.getConfig("wild card A").getUser());
    }

    @Test
    public void negateFirst() throws IOException {
        assertEquals("8", validConfig.getConfig("wild8card_C").getUser());
        assertNull(validConfig.getConfig("wild8card_A").getUser());
        assertNull(validConfig.getConfig("wild8card_ABRA").getUser());
    }

    @Test
    public void parseLowercase() throws IOException {
        assertEquals("that", validConfig.getConfig("lower").getHostname());
        assertEquals("9", validConfig.getConfig("lower").getUser());
    }

    @Test
    public void parseEquals() throws IOException {
        assertEquals("that", validConfig.getConfig("eq").getHostname());
        assertEquals("10", validConfig.getConfig("eq").getUser());
    }
    
    @Test(expected=XenonException.class)
    public void parseOneArgument() throws XenonException {
        OpenSSHConfig.parse("OneArgument");
    }

    @Test(expected=XenonException.class)
    public void parseFaultyQuotes() throws XenonException {
        OpenSSHConfig.parse("Argument \"quotesfaulty");
    }

    @Test(expected=XenonException.class)
    public void parseFaultyQuotesEnd() throws XenonException {
        OpenSSHConfig.parse("Argument quotesfaulty\"");
    }
}
