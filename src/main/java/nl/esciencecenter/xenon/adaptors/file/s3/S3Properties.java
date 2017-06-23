package nl.esciencecenter.xenon.adaptors.file.s3;


import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.XenonEngine;
import nl.esciencecenter.xenon.engine.XenonPropertyDescriptionImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.XenonPropertyDescription.*;

import java.util.EnumSet;

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
    public static final ImmutableArray<String> ADAPTOR_LOCATIONS = new ImmutableArray<>("s3://host[:port]/bucketName");

    /** All our own properties start with this prefix. */
    public static final String PREFIX = XenonEngine.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** Probably only useful when communicating with actual Amazon, not useful for S3-compatible sevices.
     * Amazon uses a couple of regions (i.e. places with datacenters) such as eu-central-1 (Frankfurt).
     * The signing region is a region that is included when messages are signed and should be the same as
     * the region of the endpoint (I think). Default is "" (empty string) */
    public static final String SIGNING_REGION = PREFIX + "signingRegion";


    /** List of properties supported by this SSH adaptor */
    protected static final ImmutableArray<XenonPropertyDescription> VALID_PROPERTIES = new ImmutableArray<XenonPropertyDescription>(
            new XenonPropertyDescriptionImplementation(SIGNING_REGION, Type.STRING, EnumSet.of(Component.FILESYSTEM),
                    "", "Probably only useful when communicating with actual Amazon, not useful for S3-compatible sevices.\n" +
                    "     * Amazon uses a couple of regions (i.e. places with datacenters) such as eu-central-1 (Frankfurt).\n" +
                    "     * The signing region is a region that is included when messages are signed and should be the same as\n" +
                    "     * the region of the endpoint (I think). ")
    );

}
