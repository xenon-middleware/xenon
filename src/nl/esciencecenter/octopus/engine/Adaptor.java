package nl.esciencecenter.octopus.engine;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.engine.credentials.CredentialsAdaptor;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more create functions of the Octopus interface,
 * depending on which functionality they provide.
 * 
 * @author Niels Drost
 * 
 */
public interface Adaptor extends AdaptorInfo {

    // Adaptor(TypedProperties properties, OctopusEngine engine)

    /**
     * Returns if this adaptor supports the given scheme.
     * 
     * @param scheme
     * @return if this adaptor supports the given scheme.
     */
    public boolean supports(String scheme);

    public JobsAdaptor jobsAdaptor();

    public FilesAdaptor filesAdaptor();

    public CredentialsAdaptor credentialsAdaptor();

    public void end();
}
