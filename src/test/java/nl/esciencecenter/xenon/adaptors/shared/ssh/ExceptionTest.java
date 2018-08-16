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
package nl.esciencecenter.xenon.adaptors.shared.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExceptionTest {

    private void testException(Exception e, String name, String message, Throwable cause) {

        if (name == null) {
            assertEquals(message, e.getMessage());
        } else {
            assertEquals(name + " adaptor: " + message, e.getMessage());
        }
        assertTrue(e.getCause() == cause);
    }

    private void testException(Exception e, Throwable cause) {
        testException(e, "name", "message", cause);
    }

    private void testException(Exception e) {
        testException(e, "name", "message", null);
    }

    @Test
    public void CredentialNotFoundException() throws Exception {
        testException(new CredentialNotFoundException("name", "message"));
    }

    @Test
    public void CredentialNotFoundExceptionWithCause() throws Exception {
        Throwable t = new Throwable();
        testException(new CredentialNotFoundException("name", "message", t), t);
    }

    @Test
    public void CertificateNotFoundException() throws Exception {
        testException(new CertificateNotFoundException("name", "message"));
    }

    @Test
    public void CertificateNotFoundExceptionWithCause() throws Exception {
        Throwable t = new Throwable();
        testException(new CertificateNotFoundException("name", "message", t), t);
    }

}
