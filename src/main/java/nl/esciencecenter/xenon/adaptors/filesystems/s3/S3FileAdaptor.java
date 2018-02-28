/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.filesystems.s3;

import java.net.URI;
import java.util.Map;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.jclouds.JCloudsFileSytem;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

/**
 * Created by atze on 29-6-17.
 */
public class S3FileAdaptor extends FileAdaptor {

    /** The name of this adaptor */
    public static final String ADAPTOR_NAME = "s3";

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The JClouds adaptor uses Apache JClouds to talk to s3 and others";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";

    /** The buffer size to use when copying data. */
    public static final String BUFFER_SIZE = PREFIX + "bufferSize";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "[http://host[:port]]/bucketname[/workdir]" };

    /** List of properties supported by this FTP adaptor */
    private static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[] {
            new XenonPropertyDescription(BUFFER_SIZE, Type.SIZE, "64K", "The buffer size to use when copying files (in bytes).") };

    public S3FileAdaptor() {
        super("s3", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {

        // An S3 URI has the form:
        //
        // [http://host[:port]]/bucketname[/workdir]
        //
        // Note that it may only contain a bucketname (in which case it connects to amazon AWS automatically), or it may contain a server adres and bucketname.
        // In both cases, an optional workdir may be provided after the bucketname.

        if (location == null || location.isEmpty()) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Location may not be empty");
        }

        if (credential == null) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Credential may not be null.");
        }

        if (!(credential instanceof PasswordCredential) || credential instanceof DefaultCredential) {
            throw new InvalidCredentialException(ADAPTOR_NAME, "Credential type not supported");
        }

        String server = null;
        String bucket = null;
        String bucketPath = null;
        Path path = null;

        if (location.startsWith("http://")) {
            URI uri;

            try {
                uri = new URI(location);
            } catch (Exception e) {
                throw new InvalidLocationException(ADAPTOR_NAME, "Failed to parse location: " + location, e);
            }

            // Reconstruct the server address
            server = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
            bucketPath = uri.getPath();
        } else {
            bucketPath = location;
        }

        if (bucketPath == null || bucketPath.isEmpty() || bucketPath.equals("/")) {
            throw new InvalidLocationException(ADAPTOR_NAME, "Location does not contain bucket: " + location);
        }

        if (bucketPath.startsWith("/")) {
            bucketPath = bucketPath.substring(1);
        }

        int split = bucketPath.indexOf('/');

        if (split < 0) {
            bucket = bucketPath;
            path = new Path('/', "/");
        } else {
            // Split the bucket and the working dir in path.
            bucket = bucketPath.substring(0, split);
            path = new Path('/', bucketPath.substring(split));
        }

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        long bufferSize = xp.getSizeProperty(BUFFER_SIZE);

        if (bufferSize <= 0 || bufferSize >= Integer.MAX_VALUE) {
            throw new InvalidPropertyException(ADAPTOR_NAME,
                    "Invalid value for " + BUFFER_SIZE + ": " + bufferSize + " (must be between 1 and " + Integer.MAX_VALUE + ")");
        }

        BlobStoreContext context = null;

        if (credential instanceof PasswordCredential) {
            PasswordCredential pwUser = (PasswordCredential) credential;
            context = ContextBuilder.newBuilder("s3").endpoint(server).credentials(pwUser.getUsername(), new String(pwUser.getPassword()))
                    .buildView(BlobStoreContext.class);
        } else {
            // Default credentials, so we do not need to set the credentials for the server
            context = ContextBuilder.newBuilder("s3").endpoint(server).buildView(BlobStoreContext.class);
        }

        return new JCloudsFileSytem(getNewUniqueID(), ADAPTOR_NAME, server, credential, path, context, bucket, (int) bufferSize, xp);
    }

    @Override
    public boolean supportsReadingPosixPermissions() {
        return false;
    }

    @Override
    public boolean supportsSettingPosixPermissions() {
        return false;
    }

    @Override
    public boolean canAppend() {
        return false;
    }

    @Override
    public boolean canReadSymboliclinks() {
        return false;
    }

    @Override
    public boolean canCreateSymboliclinks() {
        return false;
    }

    @Override
    public boolean needsSizeBeforehand() {
        return true;
    }

    @Override
    public boolean supportsRename() {
        return false;
    }

    @Override
    public boolean isConnectionless() {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getSupportedCredentials() {
        // The S3 adaptor supports these credentials
        return new Class[] { DefaultCredential.class, PasswordCredential.class };
    }
}
