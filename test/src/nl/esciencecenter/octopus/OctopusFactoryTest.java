package nl.esciencecenter.octopus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

public class OctopusFactoryTest {

    @Test
    public void testNewOctopus() throws OctopusException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        assertThat(octopus.getDefaultProperties(), is(new Properties()));
    }
}
