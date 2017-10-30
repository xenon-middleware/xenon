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

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.CredentialMap;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public class S3FileSystemSimpleTests {

    @Test(expected = InvalidCredentialException.class)
    public void test_invalid_credential_type() throws XenonException {
        new S3FileAdaptor().createFileSystem("localhost", new CredentialMap(), null);
    }

    @Test(expected = InvalidCredentialException.class)
    public void test_credential_null() throws XenonException {
        new S3FileAdaptor().createFileSystem("localhost", null, null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_location_null() throws XenonException {
        new S3FileAdaptor().createFileSystem(null, new PasswordCredential("aap", "noot".toCharArray()), null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test_location_empty() throws XenonException {
        new S3FileAdaptor().createFileSystem("", new PasswordCredential("aap", "noot".toCharArray()), null);
    }
}
