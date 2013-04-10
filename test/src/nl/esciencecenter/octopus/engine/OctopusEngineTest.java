package nl.esciencecenter.octopus.engine;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Properties;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

public class OctopusEngineTest {

    @Test
    public void testNewEngineWithNulls() throws OctopusException {
        Octopus octopus = OctopusEngine.newEngine(null);
        assertThat(octopus.getDefaultProperties(), is(new Properties()));
    }

    @Test
    public void testNewEngineWithWithProperties() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        Octopus octopus = OctopusEngine.newEngine(properties);
        assertThat(octopus.getDefaultProperties(), is(properties));
    }

    public OctopusEngine getEngineWithOnlyLocalAdaptor() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("octopus.adaptors.load", "local");
        Octopus octopus = null;
        octopus = OctopusEngine.newEngine(properties);
        return (OctopusEngine) octopus;
    }

    @Test
    public void testGetAdaptorInfo() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        AdaptorInfo adaptorInfo = octopus.getAdaptorInfo("local");
        assertThat(adaptorInfo.getName(), is("local"));
    }

    @Test
    public void testGetAdaptorInfo_UnknownAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        try {
            octopus.getAdaptorInfo("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("could not find adaptor named hupsefluts"));
        }
    }

    @Test
    public void testGetAdaptorFor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        Adaptor adaptor = octopus.getAdaptorFor("file");
        assertThat(adaptor.getName(), is("local"));
    }

    @Test
    public void testGetAdaptorFor_UnknownScheme() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        try {
            octopus.getAdaptorFor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("cannot find adaptor for scheme hupsefluts"));
        }
    }

    @Test
    public void testGetAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        Adaptor adaptor = octopus.getAdaptor("local");
        assertThat(adaptor.getName(), is("local"));
    }

    @Test
    public void testGetAdaptor_UnknownAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newEngine(null);
        try {
            octopus.getAdaptor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("could not find adaptor named hupsefluts"));
        }
    }


}
