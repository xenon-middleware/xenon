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
package nl.esciencecenter.xenon.adaptors.ssh;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.NoSuchPathException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * 
 */
public final class SshUtil {
    private SshUtil() {
        // do not use
    }

    @SuppressWarnings({ "PMD.CompareObjectsWithEquals", "PMD.NPathComplexity" })
    public static boolean equals(SftpATTRS a1, SftpATTRS a2) {
        // Handles case of aliased object + both null
        if (a1 == a2) {
            return true;
        }

        // Handle case of one of the two null
        if (a1 == null || a2 == null) {
            return false;
        }

        // Test all fields
        return a1.getATime() == a2.getATime()
                && a1.getMTime() == a2.getMTime()
                && a1.getFlags() == a2.getFlags()
                && a1.getGId() == a2.getGId()
                && a1.getUId() == a2.getUId()
                && a1.getPermissions() == a2.getPermissions()
                && a1.getSize() == a2.getSize();
    }
    
    
    /*
    SSH_FX_OK
       Indicates successful completion of the operation.
    SSH_FX_EOF
      indicates end-of-file condition; for SSH_FX_READ it means that no
        more data is available in the file, and for SSH_FX_READDIR it
       indicates that no more files are contained in the directory.
    SSH_FX_NO_SUCH_FILE
       is returned when a reference is made to a file which should exist
       but doesn't.
    SSH_FX_PERMISSION_DENIED
       is returned when the authenticated user does not have sufficient
       permissions to perform the operation.
    SSH_FX_FAILURE
       is a generic catch-all error message; it should be returned if an
       error occurs for which there is no more specific error code
       defined.
    SSH_FX_BAD_MESSAGE
       may be returned if a badly formatted packet or protocol
       incompatibility is detected.
    SSH_FX_NO_CONNECTION
       is a pseudo-error which indicates that the client has no
       connection to the server (it can only be generated locally by the
       client, and MUST NOT be returned by servers).
    SSH_FX_CONNECTION_LOST
       is a pseudo-error which indicates that the connection to the
       server has been lost (it can only be generated locally by the
       client, and MUST NOT be returned by servers).
    SSH_FX_OP_UNSUPPORTED
       indicates that an attempt was made to perform an operation which
       is not supported for the server (it may be generated locally by
       the client if e.g.  the version number exchange indicates that a
       required feature is not supported by the server, or it may be
       returned by the server if the server does not implement an
       operation).
    */
    public static XenonException sftpExceptionToXenonException(SftpException e) {
        switch (e.id) {
        case ChannelSftp.SSH_FX_OK:
            return new XenonException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        case ChannelSftp.SSH_FX_EOF:
            return new EndOfFileException(SshAdaptor.ADAPTOR_NAME, "Unexpected EOF", e);
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
            return new NoSuchPathException(SshAdaptor.ADAPTOR_NAME, "No such file", e);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
            return new PermissionDeniedException(SshAdaptor.ADAPTOR_NAME, "Permission denied", e);
        case ChannelSftp.SSH_FX_FAILURE:
            return new XenonException(SshAdaptor.ADAPTOR_NAME, "SSH gave an unknown error", e);
        case ChannelSftp.SSH_FX_BAD_MESSAGE:
            return new XenonException(SshAdaptor.ADAPTOR_NAME, "SSH received a malformed message", e);
        case ChannelSftp.SSH_FX_NO_CONNECTION:
            return new NotConnectedException(SshAdaptor.ADAPTOR_NAME, "SSH does not have a connection!", e);
        case ChannelSftp.SSH_FX_CONNECTION_LOST:
            return new ConnectionLostException(SshAdaptor.ADAPTOR_NAME, "SSH lost connection!", e);
        case ChannelSftp.SSH_FX_OP_UNSUPPORTED:
            return new UnsupportedIOOperationException(SshAdaptor.ADAPTOR_NAME, "Unsupported operation", e);
        default:
            return new XenonException(SshAdaptor.ADAPTOR_NAME, "Unknown SSH exception", e);
        }
    }
}
