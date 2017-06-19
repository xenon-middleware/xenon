package nl.esciencecenter.xenon.adaptors.file.s3;


import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

public class S3Properties {
    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "s3";

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 9000;

    /** A description of this adaptor */
    public static final String ADAPTOR_DESCRIPTION = "The S3 adaptor implements all file access to Amazon S3-compatible servers, such as Amazon itself, or Minio";

    /** The schemes supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_SCHEME = new ImmutableArray<>("s3");

    /** The locations supported by this adaptor */
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("s3://host[:port]");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";


    /** List of properties supported by this SSH adaptor */
    protected static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>();

}
