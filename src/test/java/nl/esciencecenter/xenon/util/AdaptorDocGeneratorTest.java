package nl.esciencecenter.xenon.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

public class AdaptorDocGeneratorTest {
    private String output;

    @Before
    public void setUp() {
        AdaptorDocGenerator generator = new AdaptorDocGenerator();
        StringWriter out = new StringWriter();
        generator.generate(new PrintWriter(out));
        out.flush();
        output = out.toString();
    }

    @Test
    public void testGenerate_hasHeader() {
        assertThat(output, containsString("Adaptor Documentation"));
    }

    @Test
    public void testGenerate_hasSupportedProps() {
        assertThat(output, containsString("Supported properties"));
    }
}