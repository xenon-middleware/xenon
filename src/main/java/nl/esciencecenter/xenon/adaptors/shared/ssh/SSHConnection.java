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

import java.io.IOException;

import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;

import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil.Tunnel;
import org.apache.sshd.client.subsystem.sftp.SftpClientFactory;

public class SSHConnection implements AutoCloseable {

    private ClientSession[] sessions;
    private Tunnel[] tunnels;
    private final int hops;
    private boolean closed = false;

    private ClientSession session;

    protected SSHConnection(int hops) {
        this.hops = hops;
        sessions = new ClientSession[hops];
        tunnels = new Tunnel[hops];
    }

    protected void addHop(int hop, ClientSession session, Tunnel tunnel) {
        sessions[hop] = session;
        tunnels[hop] = tunnel;
    }

    protected void setSession(ClientSession session) {
        this.session = session;
    }

    public SftpClient createSftpClient() throws IOException {
        return SftpClientFactory.instance().createSftpClient(session);
    }

    public ClientSession getSession() {
        return session;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {

        if (closed) {
            return;
        }

        closed = true;

        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                // ignored?
            }
        }

        for (int i = hops - 1; i >= 0; i--) {
            if (tunnels[i] != null) {
                try {
                    tunnels[i].close();
                } catch (Exception e) {
                    // ignored?
                }
            }
            if (sessions[i] != null) {
                try {
                    sessions[i].close();
                } catch (Exception e) {
                    // ignored?
                }
            }
        }
    }
}
