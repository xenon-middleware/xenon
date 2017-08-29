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

import java.util.HashMap;
import java.util.Map;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.filesystems.jclouds.JCloudsFileSytem;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.filesystems.FileSystem;

/**
 * Created by atze on 29-6-17.
 */
public class S3FileAdaptor extends FileAdaptor {

    /** The default SSH port */
    protected static final int DEFAULT_PORT = 21;

    /** A description of this adaptor */
    private static final String ADAPTOR_DESCRIPTION = "The JClouds adaptor uses Apache JClouds to talk to s3 and others";

    /** The locations supported by this adaptor */
    private static final String[] ADAPTOR_LOCATIONS = new String[] { "s3://host[:port]" };

    /** All our own properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + "s3.";

    protected static final XenonPropertyDescription[] VALID_PROPERTIES = new XenonPropertyDescription[0];

    public S3FileAdaptor() {
        super("s3", ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES);
    }

    @Override
    public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties)
            throws XenonException {

        int split = location.lastIndexOf("/");

        if (split < 0) {
            throw new InvalidLocationException("s3", "No bucket found in url: " + location);
        }

        String server = location.substring(0, split);
        String bucket = location.substring(split + 1);

        XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

        if (!(credential instanceof PasswordCredential)) {
            throw new InvalidCredentialException("s3", "No secret key given for s3 connection.");
        }

        PasswordCredential pwUser = (PasswordCredential) credential;

        BlobStoreContext context = ContextBuilder.newBuilder("s3").endpoint(server)
                .credentials(pwUser.getUsername(), new String(pwUser.getPassword())).buildView(BlobStoreContext.class);

        return new JCloudsFileSytem(getNewUniqueID(), "s3", server, context, bucket, xp);
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
}
