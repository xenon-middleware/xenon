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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.junit.Test;

public class StreamForwarderTest {
    @Test
    public void test_it() {
        String input = "Some content to forward";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.defaultCharset()));
        OutputStream out = new ByteArrayOutputStream();
        StreamForwarder forwarder = new StreamForwarder(in, out);
        forwarder.terminate(1000);

        String output = out.toString();
        assertEquals(input, output);
    }
}
