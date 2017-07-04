/**
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
package nl.esciencecenter.xenon.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.adaptors.NotConnectedException;
import nl.esciencecenter.xenon.adaptors.PropertyTypeException;
import nl.esciencecenter.xenon.adaptors.filesystems.ConnectionLostException;
import nl.esciencecenter.xenon.adaptors.filesystems.EndOfFileException;
import nl.esciencecenter.xenon.adaptors.filesystems.PermissionDeniedException;
import nl.esciencecenter.xenon.adaptors.schedulers.BadParameterException;
import nl.esciencecenter.xenon.adaptors.schedulers.IncompatibleVersionException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.adaptors.schedulers.local.CommandNotFoundException;
import nl.esciencecenter.xenon.adaptors.shared.ssh.CertificateNotFoundException;
import nl.esciencecenter.xenon.filesystems.AttributeNotSupportedException;
import nl.esciencecenter.xenon.filesystems.DirectoryNotEmptyException;
import nl.esciencecenter.xenon.filesystems.FileSystemClosedException;
import nl.esciencecenter.xenon.filesystems.InvalidOptionsException;
import nl.esciencecenter.xenon.filesystems.InvalidPathException;
import nl.esciencecenter.xenon.filesystems.InvalidResumeTargetException;
import nl.esciencecenter.xenon.filesystems.NoSuchCopyException;
import nl.esciencecenter.xenon.filesystems.NoSuchPathException;
import nl.esciencecenter.xenon.filesystems.PathAlreadyExistsException;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidAdaptorException;
import nl.esciencecenter.xenon.schedulers.InvalidCredentialException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidLocationException;
import nl.esciencecenter.xenon.schedulers.InvalidPropertyException;
import nl.esciencecenter.xenon.schedulers.NoSuchJobException;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.UnknownPropertyException;
import nl.esciencecenter.xenon.schedulers.UnsupportedJobDescriptionException;

import org.junit.Test;

/**
 * 
 */
public class ExceptionsTest {

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
    public void testXenonException1() throws Exception {
        testException(new XenonException("name", "message"));
    }

    @Test
    public void testXenonException2() throws Exception {
        Throwable t = new Throwable();
        testException(new XenonException("name", "message", t), t);
    }

    @Test
    public void testXenonException3() throws Exception {
        testException(new XenonException(null, "message"), null, "message", null);
    }

    @Test
    public void testXenonRuntimeException1() throws Exception {
        testException(new XenonRuntimeException("name", "message"));
    }

    @Test
    public void testXenonRuntimeException2() throws Exception {
        Throwable t = new Throwable();
        testException(new XenonRuntimeException("name", "message", t), t);
    }

    @Test
    public void testXenonIOException3() throws Exception {
        testException(new XenonException(null, "message"), null, "message", null);
    }

    @Test
    public void testXenonIOException1() throws Exception {
        testException(new XenonException("name", "message"));
    }

    @Test
    public void testXenonIOException2() throws Exception {
        Throwable t = new Throwable();
        testException(new XenonException("name", "message", t), t);
    }

    @Test
    public void testXenonRuntimeException3() throws Exception {
        testException(new XenonRuntimeException(null, "message"), null, "message", null);
    }

    @Test
    public void testAttributeNotSupportedException1() throws Exception {
        testException(new AttributeNotSupportedException("name", "message"));
    }

    @Test
    public void testAttributeNotSupportedException2() throws Exception {
        Throwable t = new Throwable();
        testException(new AttributeNotSupportedException("name", "message", t), t);
    }

    @Test
    public void testBadParameterException1() throws Exception {
        testException(new BadParameterException("name", "message"));
    }

    @Test
    public void testBadParameterException2() throws Exception {
        Throwable t = new Throwable();
        testException(new BadParameterException("name", "message", t), t);
    }

    @Test
    public void testCommandNotFoundException1() throws Exception {
        testException(new CommandNotFoundException("name", "message"));
    }

    @Test
    public void testCommandNotFoundException2() throws Exception {
        Throwable t = new Throwable();
        testException(new CommandNotFoundException("name", "message", t), t);
    }

    @Test
    public void testConnectionLostException1() throws Exception {
        testException(new ConnectionLostException("name", "message"));
    }

    @Test
    public void testConnectionLostException2() throws Exception {
        Throwable t = new Throwable();
        testException(new ConnectionLostException("name", "message", t), t);
    }

    @Test
    public void testDirectoryNotEmptyException1() throws Exception {
        testException(new DirectoryNotEmptyException("name", "message"));
    }

    @Test
    public void testDirectoryNotEmptyException2() throws Exception {
        Throwable t = new Throwable();
        testException(new DirectoryNotEmptyException("name", "message", t), t);
    }

    @Test
    public void testEndOfFileException1() throws Exception {
        testException(new EndOfFileException("name", "message"));
    }

    @Test
    public void testEndOfFileException2() throws Exception {
        Throwable t = new Throwable();
        testException(new EndOfFileException("name", "message", t), t);
    }

    @Test
    public void testFileAlreadyExistsException1() throws Exception {
        testException(new PathAlreadyExistsException("name", "message"));
    }

    @Test
    public void testFileAlreadyExistsException2() throws Exception {
        Throwable t = new Throwable();
        testException(new PathAlreadyExistsException("name", "message", t), t);
    }

    @Test
    public void testIllegalSourcePathException1() throws Exception {
        testException(new InvalidPathException("name", "message"));
    }

    @Test
    public void testIllegalSourcePathException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidPathException("name", "message", t), t);
    }

    @Test
    public void testIncompatibleVersionException1() throws Exception {
        testException(new IncompatibleVersionException("name", "message"));
    }

    @Test
    public void testIncompatibleVersionException2() throws Exception {
        Throwable t = new Throwable();
        testException(new IncompatibleVersionException("name", "message", t), t);
    }

    @Test
    public void testIncompleteJobDescriptionException1() throws Exception {
        testException(new IncompleteJobDescriptionException("name", "message"));
    }

    @Test
    public void testIncompleteJobDescriptionException2() throws Exception {
        Throwable t = new Throwable();
        testException(new IncompleteJobDescriptionException("name", "message", t), t);
    }

    @Test
    public void testInvalidCredentialException1() throws Exception {
        testException(new InvalidCredentialException("name", "message"));
    }

    @Test
    public void testInvalidCredentialException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidCredentialException("name", "message", t), t);
    }

    @Test
    public void testCertificateNotFoundException1() throws Exception {
        testException(new CertificateNotFoundException("name", "message"));
    }

    @Test
    public void testCertificateNotFoundException2() throws Exception {
        Throwable t = new Throwable();
        testException(new CertificateNotFoundException("name", "message", t), t);
    }
    
    @Test
    public void testInvalidDataException1() throws Exception {
        testException(new InvalidResumeTargetException("name", "message"));
    }

    @Test
    public void testInvalidDataException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidResumeTargetException("name", "message", t), t);
    }

    @Test
    public void testInvalidJobDescriptionException1() throws Exception {
        testException(new InvalidJobDescriptionException("name", "message"));
    }

    @Test
    public void testInvalidJobDescriptionException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidJobDescriptionException("name", "message", t), t);
    }

    @Test
    public void testInvalidLocationException1() throws Exception {
        testException(new InvalidLocationException("name", "message"));
    }

    @Test
    public void testInvalidLocationException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidLocationException("name", "message", t), t);
    }

    @Test
    public void testInvalidPropertyException1() throws Exception {
        testException(new InvalidPropertyException("name", "message"));
    }

    @Test
    public void testInvalidPropertyException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidPropertyException("name", "message", t), t);
    }

    @Test
    public void testNoSuchCopyException1() throws Exception {
        testException(new NoSuchCopyException("name", "message"));
    }

    @Test
    public void testNoSuchCopyException2() throws Exception {
        Throwable t = new Throwable();
        testException(new NoSuchCopyException("name", "message", t), t);
    }

    @Test
    public void testNoSuchFileException1() throws Exception {
        testException(new NoSuchPathException("name", "message"));
    }

    @Test
    public void testNoSuchFileException2() throws Exception {
        Throwable t = new Throwable();
        testException(new NoSuchPathException("name", "message", t), t);
    }

    @Test
    public void testNoSuchJobException1() throws Exception {
        testException(new NoSuchJobException("name", "message"));
    }

    @Test
    public void testNoSuchJobException2() throws Exception {
        Throwable t = new Throwable();
        testException(new NoSuchJobException("name", "message", t), t);
    }

    @Test
    public void testNoSuchQueueException1() throws Exception {
        testException(new NoSuchQueueException("name", "message"));
    }

    @Test
    public void testNoSuchQueueException2() throws Exception {
        Throwable t = new Throwable();
        testException(new NoSuchQueueException("name", "message", t), t);
    }

    @Test
    public void testNotConnectedException1() throws Exception {
        testException(new NotConnectedException("name", "message"));
    }

    @Test
    public void testNotConnectedException2() throws Exception {
        Throwable t = new Throwable();
        testException(new NotConnectedException("name", "message", t), t);
    }

    @Test
    public void testPermissionDeniedException1() throws Exception {
        testException(new PermissionDeniedException("name", "message"));
    }

    @Test
    public void testPermissionDeniedException2() throws Exception {
        Throwable t = new Throwable();
        testException(new PermissionDeniedException("name", "message", t), t);
    }

    @Test
    public void testUnknownPropertyException1() throws Exception {
        testException(new UnknownPropertyException("name", "message"));
    }

    @Test
    public void testUnknownPropertyException2() throws Exception {
        Throwable t = new Throwable();
        testException(new UnknownPropertyException("name", "message", t), t);
    }

    @Test
    public void testUnsupportedJobDescriptionException1() throws Exception {
        testException(new UnsupportedJobDescriptionException("name", "message"));
    }

    @Test
    public void testUnsupportedJobDescriptionException2() throws Exception {
        Throwable t = new Throwable();
        testException(new UnsupportedJobDescriptionException("name", "message", t), t);
    }

    @Test
    public void testInvalidOptionsException1() throws Exception {
        testException(new InvalidOptionsException("name", "message"));
    }

    @Test
    public void testInvalidOptionsException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidOptionsException("name", "message", t), t);
    }

    @Test
    public void testJobCanceledException1() throws Exception {
        testException(new JobCanceledException("name", "message"));
    }

    @Test
    public void testJobCanceledException2() throws Exception {
        Throwable t = new Throwable();
        testException(new JobCanceledException("name", "message", t), t);
    }

    @Test
    public void testPropertyTypeException1() throws Exception {
        testException(new PropertyTypeException("name", "message"));
    }
    
    @Test
    public void testPropertyTypeException2() throws Exception {
        Throwable t = new Throwable();
        testException(new PropertyTypeException("name", "message", t), t);
    }

    @Test
    public void testInvalidSchemeException1() throws Exception {
        testException(new InvalidAdaptorException("name", "message"));
    }

    @Test
    public void testInvalidSchemeException2() throws Exception {
        Throwable t = new Throwable();
        testException(new InvalidAdaptorException("name", "message", t), t);
    }

    @Test
    public void testFileSystemClosedException1() throws Exception {
        testException(new FileSystemClosedException("name", "message"));
    }

    @Test
    public void testFileSystemClosedException2() throws Exception {
        Throwable t = new Throwable();
        testException(new FileSystemClosedException("name", "message", t), t);
    }

    
}
