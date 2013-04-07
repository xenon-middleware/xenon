/**
 * This package contains classes and interfaces which are
 * required to secure software and hardware resources. Currently the only class in
 * this package is SecurityContext. An instance of the class SecurityContext
 * contains security information which can be used to secure software and hardware
 * resources.
 * <p>
 * At some point in the connection setup, an adaptor may need security information. 
 * It then asks its specific security utils class to provide this. What then happens
 * is illustrated by taking the SftpFileAdaptor as example adaptor.
 * <p>
 * The SftpFileAdaptor needs a SftpUserInfo object (containing a username, password
 * and a privatekey) to establish a connection. Therefore it asks its specific security
 * utils class (the SftpSecurityUtils class) to provide this object. The 
 * SftpSecurityUtils class forwards this request to the common SecurityUtils class
 * but tells it for which adaptor it is needed (and the location + port).
 * <p>
 * The common SecurityUtils class then checks whether there are valid security
 * contexts for this requests. A security context is valid if the notes added to
 * that security context don't prohibit the use of it for the given host-port
 * combination and adaptor. In the example below the security context is only
 * valid for the sftp adaptor on hosts <code>machine1.cs.vu.nl</code> and <code>machine2.cs.vu.nl</code>.
 * <p>
 * <code>
 * PasswordSecurityContext pwdSecContext = new PasswordSecurityContext(...);<br>
 * pwdSecContext.addNote("adaptors", "sftp");<br>
 * pwdSecContext.addNote("hosts", "machine1.cs.vu.nl,machine2.cs.vu.nl");<br>
 * </code>
 * <p>
 * For all valid security contexts the SecurityUtils class checks whether they 
 * already have a userdata object (in the case of Sftp this is the SftpUserInfo
 * object). If not, the SecurityUtils class uses the provided ContextCreator to
 * create such a userdata object for the security context. In the case of Sftp
 * this is the SftpContextCreator.
 * <p>
 * The ContextCreator inspects the security context and if it can be used to create
 * the userdata it will do that. The SftpContextCreator for instance can handle
 * PasswordSecurityContexts and CertificateSecurityContexts, but not 
 * CredentialSecurityContexts or MyProxySecurityContexts. It returns the userdata 
 * to the SecurityUtils class.
 * <p>
 * The SecurityUtils class then adds the userdata to the SecurityContexts so that
 * if the same userdata is needed a next time it's already there and doesn't have
 *  to be created twice. Then it returns the userdata.
 * <p>
 * It can happen that are no valid SecurityContexts for a request or that none of
 * the valid SecurityContexts could produce userdata (because the ContextCreator 
 * couldn't handle the SecurityContext type for instance). The SecurityUtils class
 * then tries to create a new default security context using the same 
 * ContextCreator. For the SftpContextCreator, it will look in various default 
 * locations for a .ssh directory with keyfiles and it will use the username of 
 * the system where the application runs. If the default security context can be 
 * created, the SecurityUtils class retrieves the userdata from it and returns it. 
 * Note that the default security context can be any security context type, it
 * is specified in the ContextCreator. For instance the SftpContextCreator 
 * creates a CertificateSecurityContext as default security context.
 */

package nl.esciencecenter.octopus.security;

