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

package nl.esciencecenter.xenon.adaptors.ssh;

import org.junit.BeforeClass;
import org.junit.Ignore;

import nl.esciencecenter.xenon.adaptors.GenericCredentialsAdaptorTestParent;

/**
 * Test creating SSH credentials.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
public class ITSSHCredentialTest extends GenericCredentialsAdaptorTestParent {
    @BeforeClass
    public static void prepareLocalFileAdaptorTest() throws Exception {
        GenericCredentialsAdaptorTestParent.prepareClass(new SSHCredentialTestConfig());
    }

    @Ignore("Already tested in unit tests")
    @Override
    public void test01_newCertificateCredential_WrongCertificate() throws Exception {
        super.test01_newCertificateCredential_WrongCertificate();
    }
    @Ignore("Already tested in unit tests")
    @Override
    public void test02_newCertificateCredential_Unsupported() throws Exception {
        super.test02_newCertificateCredential_Unsupported();
    }
    @Ignore("Already tested in unit tests")
    @Override
    public void test04_newPasswordCredential_OK() throws Exception {
        super.test04_newPasswordCredential_OK();
    }
    @Ignore("Already tested in unit tests")
    @Override
    public void test05_newPasswordCredential_Unsupported() throws Exception {
        super.test05_newPasswordCredential_Unsupported();
    }
}
