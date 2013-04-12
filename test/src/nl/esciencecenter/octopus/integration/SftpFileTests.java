package nl.esciencecenter.octopus.integration;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.FileSystem;

public class SftpFileTests extends AbstractFileTests
{
    public String getTestUser()
    {
        // actual test user 
        return System.getProperty("user.name");
    }
    
    public java.net.URI getTestLocation() throws Exception {
        
        String user = getTestUser();
        return new URI("sftp://"+user+"@localhost/tmp/"); 
    }
    
    public Credential getSSHCredentials() throws OctopusException {
        
        Credentials creds = octopus.credentials();
        String user = getTestUser();
        Credential cred = creds.newCertificateCredential("ssh", null, "/home/" + user + "/.ssh/id_rsa", 
                            "/home/" + user + "/.ssh/id_rsa.pub", user, "");
        return cred; 
    }
    
    /** 
     * Get actual FileSystem implementation to run test on. 
     * Test this before other tests: 
     */
    protected FileSystem getFileSystem() throws Exception {
        
        synchronized(this) {
            if (fileSystem==null) {
                URI uri=getTestLocation();
                URI fsURI=new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(), uri.getPort(),null, null, null);
                fileSystem=getFiles().newFileSystem(fsURI, getSSHCredentials(),null);
            }
            
            return fileSystem;
        }
    }
    
    // ===
    // Sftp Specific tests here: 
    // === 
    
    
}
