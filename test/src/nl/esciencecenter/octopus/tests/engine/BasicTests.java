package nl.esciencecenter.octopus.tests.engine;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.*;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;

public class BasicTests {

    @org.junit.Test
    public void test1() throws Exception {

        Octopus octopus = OctopusFactory.newOctopus(null);

        octopus.end();
    }

    @org.junit.Test
    public void test2() throws Exception {

        Properties properties = new Properties();

        properties.setProperty("some.key", "some.value");

        Octopus octopus = OctopusFactory.newOctopus(properties);

        assertEquals(octopus.getDefaultProperties().get("some.key"), "some.value");

        octopus.end();
    }

    @org.junit.Test
    public void test3() throws Exception {

        Octopus octopus = OctopusFactory.newOctopus(null);

        //test if the local adaptor exists
        AdaptorInfo localInfo = octopus.getAdaptorInfo("local");

        System.out.println(localInfo.getName());
        System.out.println(localInfo.getDescription());
        System.out.println(Arrays.toString(localInfo.getSupportedSchemes()));
        System.out.println(localInfo.getSupportedProperties());

        octopus.end();
    }
}
