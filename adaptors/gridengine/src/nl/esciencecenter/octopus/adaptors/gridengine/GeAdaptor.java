package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;

public class GeAdaptor extends Adaptor {

    private static final String ADAPTOR_NAME = "gridengine";

    private static final String ADAPTOR_DESCRIPTION =
            "The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local "
                    + "or the ssh adaptor to gain access to the scheduler machine.";

    private static final String[] ADAPTOR_SCHEME = new String[] { "ge" };

    /** List of {NAME, DESCRIPTION, DEFAULT_VALUE} for properties. */
    private static final String[][] validPropertiesList = new String[][] { {}, {} };

    private final GeJobsAdaptor jobsAdaptor;

    public GeAdaptor(OctopusProperties properties, OctopusEngine octopusEngine) throws OctopusException {
        super(octopusEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, validPropertiesList, properties);

        this.jobsAdaptor = new GeJobsAdaptor(properties, octopusEngine);
    }

    void checkURI(URI location) throws OctopusException {
        if (!supports(location.getScheme())) {
            throw new OctopusException("SGE adaptor does not support scheme " + location.getScheme(), "sge", location);
        }
    }

    @Override
    public String getName() {
        return "local";
    }

    @Override
    public String getDescription() {
        return "The SGE Adaptor submits jobs to a (Sun/Ocacle/Univa) Grid Engine scheduler. This adaptor uses either the local "
                + "or the ssh adaptor to gain access to the scheduler machine.";
    }

    @Override
    public Map<String, String> getSupportedProperties() {
        return new HashMap<String, String>();
    }

    @Override
    public FilesAdaptor filesAdaptor() throws OctopusException {
        throw new OctopusException("The SGE adaptor does not support files");
    }

    @Override
    public GeJobsAdaptor jobsAdaptor() {
        return jobsAdaptor;
    }

    @Override
    public void end() {
        jobsAdaptor.end();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public CredentialsAdaptor credentialsAdaptor() throws OctopusException {
        throw new OctopusException("The SGE adaptor does not support credentials");
    }

}
