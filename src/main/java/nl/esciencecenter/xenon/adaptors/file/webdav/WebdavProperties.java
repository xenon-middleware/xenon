package nl.esciencecenter.xenon.adaptors.file.webdav;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class WebdavProperties {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "webdav";

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The webdav file adaptor implements file access to remote webdav servers.";

    /** The schemes supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("http");

    /** The locations supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("[user@]host[:port]");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + "webdav.";

    /** List of properties supported by this FTP adaptor */
    public static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = 
            new ImmutableArray<XenonPropertyDescription>();
	
}
