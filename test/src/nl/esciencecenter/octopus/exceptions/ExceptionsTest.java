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

package nl.esciencecenter.octopus.exceptions;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
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
    public void testOctopusException1() throws Exception {
        testException(new OctopusException("name", "message"));
    }
    
    @Test
    public void testOctopusException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new OctopusException("name", "message", t), t);
    }

    @Test
    public void testOctopusException3() throws Exception {
        testException(new OctopusException(null, "message"), null, "message", null);
    }
    
    @Test
    public void testOctopusRuntimeException1() throws Exception {
        testException(new OctopusRuntimeException("name", "message"));
    }
    
    @Test
    public void testOctopusRuntimeException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new OctopusRuntimeException("name", "message", t), t);
    }

    @Test
    public void testOctopusIOException3() throws Exception {
        testException(new OctopusIOException(null, "message"), null, "message", null);
    }

    
    @Test
    public void testOctopusIOException1() throws Exception {
        testException(new OctopusIOException("name", "message"));
    }
    
    @Test
    public void testOctopusIOException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new OctopusIOException("name", "message", t), t);
    }

    @Test
    public void testOctopusRuntimeException3() throws Exception {
        testException(new OctopusRuntimeException(null, "message"), null, "message", null);
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
    public void testDirectoryIteratorException1() throws Exception {
        testException(new DirectoryIteratorException("name", "message"));
    }
    
    @Test
    public void testDirectoryIteratorException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new DirectoryIteratorException("name", "message", t), t);
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
        testException(new FileAlreadyExistsException("name", "message"));
    }
    
    @Test
    public void testFileAlreadyExistsException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new FileAlreadyExistsException("name", "message", t), t);
    }

    @Test
    public void testIllegalPropertyException1() throws Exception {
        testException(new IllegalPropertyException("name", "message"));
    }
    
    @Test
    public void testIllegalPropertyException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new IllegalPropertyException("name", "message", t), t);
    }

    @Test
    public void testIllegalSourcePathException1() throws Exception {
        testException(new IllegalSourcePathException("name", "message"));
    }
    
    @Test
    public void testIllegalSourcePathException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new IllegalSourcePathException("name", "message", t), t);
    }

    @Test
    public void testIllegalTargetPathException1() throws Exception {
        testException(new IllegalTargetPathException("name", "message"));
    }
    
    @Test
    public void testIllegalTargetPathException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new IllegalTargetPathException("name", "message", t), t);
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
    public void testInvalidCloseException1() throws Exception {
        testException(new InvalidCloseException("name", "message"));
    }
    
    @Test
    public void testInvalidCloseException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new InvalidCloseException("name", "message", t), t);
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
    public void testInvalidCredentialsException1() throws Exception {
        testException(new InvalidCredentialsException("name", "message"));
    }
    
    @Test
    public void testInvalidCredentialsException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new InvalidCredentialsException("name", "message", t), t);
    }

    @Test
    public void testInvalidDataException1() throws Exception {
        testException(new InvalidDataException("name", "message"));
    }
    
    @Test
    public void testInvalidDataException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new InvalidDataException("name", "message", t), t);
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
    public void testInvalidOpenOptionsException1() throws Exception {
        testException(new InvalidOpenOptionsException("name", "message"));
    }
    
    @Test
    public void testInvalidOpenOptionsException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new InvalidOpenOptionsException("name", "message", t), t);
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
        testException(new NoSuchFileException("name", "message"));
    }
    
    @Test
    public void testNoSuchFileException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new NoSuchFileException("name", "message", t), t);
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
    public void testNoSuchOctopusException1() throws Exception {
        testException(new NoSuchOctopusException("name", "message"));
    }
    
    @Test
    public void testNoSuchOctopusException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new NoSuchOctopusException("name", "message", t), t);
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
    public void testNoSuchSchedulerException1() throws Exception {
        testException(new NoSuchSchedulerException("name", "message"));
    }
    
    @Test
    public void testNoSuchSchedulerException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new NoSuchSchedulerException("name", "message", t), t);
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
    public void testUnsupportedIOOperationException1() throws Exception {
        testException(new UnsupportedIOOperationException("name", "message"));
    }
    
    @Test
    public void testUnsupportedIOOperationException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new UnsupportedIOOperationException("name", "message", t), t);
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
    public void testUnsupportedOperationException1() throws Exception {
        testException(new UnsupportedOperationException("name", "message"));
    }
    
    @Test
    public void testUnsupportedOperationException2() throws Exception {
        Throwable t = new Throwable();        
        testException(new UnsupportedOperationException("name", "message", t), t);
    }
   
  }
