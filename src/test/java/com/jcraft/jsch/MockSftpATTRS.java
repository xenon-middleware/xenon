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

package com.jcraft.jsch;

import static com.jcraft.jsch.SftpATTRS.*;

/**
 * Used to create a SftpATTRS for nl.esciencecenter.xenon.adaptors.ssh.SSHUtilTest.
 */
public class MockSftpATTRS {
    private MockSftpATTRS() {
        // utility class
    }

    public static SftpATTRS fullySetAttrs() {
        Buffer b = new Buffer();
        int attrs = SSH_FILEXFER_ATTR_SIZE | SSH_FILEXFER_ATTR_UIDGID | SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME | SSH_FILEXFER_ATTR_EXTENDED;
        b.putInt(attrs);
        b.putLong(100); // size
        b.putInt(100); // uid
        b.putInt(100); // gid
        b.putInt(0644); // permissions
        b.putInt(100); // atime
        b.putInt(100); // mtime
        b.putInt(1); // extended count
        b.putString("this".getBytes()); // extended key
        b.putString("that".getBytes()); // extended value

        return SftpATTRS.getATTR(b);
    }

    public static SftpATTRS emptySetAttrs() {
        Buffer b = new Buffer();
        int attrs = 0;
        b.putInt(attrs);

        return SftpATTRS.getATTR(b);
    }
}
