package nl.esciencecenter.octopus.tests.engine;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

public class OctopusEngineTest {

    @Test
    public void testNewEngineWithNulls() throws OctopusException {
        Octopus octopus = OctopusEngine.newEngine(null, null);
        assertThat(octopus.getDefaultProperties(), is(new Properties()));
    }

    @Test
    public void testNewEngineWithWithProperties() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        Octopus octopus = OctopusEngine.newEngine(properties, null);
        assertThat(octopus.getDefaultProperties(), is(properties));
    }

    public OctopusEngine getEngineWithOnlyLocalAdaptor() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("octopus.adaptors.load", "local");
        Octopus octopus = null;
        octopus = OctopusEngine.newEngine(properties, null);
        return (OctopusEngine) octopus;
    }

    @Test
    public void testGetAdaptorFor() throws OctopusException {
        OctopusEngine octopus = getEngineWithOnlyLocalAdaptor();
        Adaptor adaptor = octopus.getAdaptorFor("file");
        assertThat(adaptor.getName(), is("local"));
    }

    @Test
    public void testGetAdaptor() throws OctopusException {
        OctopusEngine octopus = getEngineWithOnlyLocalAdaptor();
        Adaptor adaptor = octopus.getAdaptor("local");
        assertThat(adaptor.getName(), is("local"));
    }
}
