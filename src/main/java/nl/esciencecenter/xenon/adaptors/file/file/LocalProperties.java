package nl.esciencecenter.xenon.adaptors.file.file;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class LocalProperties {

    /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = "file";

    /** Local properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + "file.";
    
    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "This is the local file adaptor that implements"
            + " file functionality for local access.";
    
    /** The schemes supported by the adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("file");

    /** The locations supported by the adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("(null)", "(empty string)", "/");
    
    /** The properties supported by this adaptor */
    public static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<>();
}
