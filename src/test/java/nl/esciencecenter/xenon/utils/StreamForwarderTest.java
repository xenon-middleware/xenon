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