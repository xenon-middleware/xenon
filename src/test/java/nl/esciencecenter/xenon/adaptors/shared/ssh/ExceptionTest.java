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
