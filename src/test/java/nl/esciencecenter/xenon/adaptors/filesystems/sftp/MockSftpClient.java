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
package nl.esciencecenter.xenon.adaptors.filesystems.sftp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.client.subsystem.sftp.extensions.SftpClientExtension;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.subsystem.sftp.SftpConstants;
import org.apache.sshd.common.subsystem.sftp.SftpException;

public class MockSftpClient implements SftpClient {

    @Override
    public ClientChannel getClientChannel() {
        return null;
    }

    @Override
    public ClientSession getSession() {
        return null;
    }

    @Override
    public ClientSession getClientSession() {
        return null;
    }

    @Override
    public Channel getChannel() {
        return null;
    }

    @Override
    public void close() throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public Charset getNameDecodingCharset() {
        return null;
    }

    @Override
    public void setNameDecodingCharset(Charset cs) {

    }

    @Override
    public NavigableMap<String, byte[]> getServerExtensions() {
        return null;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public CloseableHandle open(String path, Collection<OpenMode> options) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void close(Handle handle) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void remove(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void rename(String oldPath, String newPath, Collection<CopyMode> options) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public int read(Handle handle, long fileOffset, byte[] dst, int dstOffset, int len, AtomicReference<Boolean> eofSignalled) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void write(Handle handle, long fileOffset, byte[] src, int srcOffset, int len) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void mkdir(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void rmdir(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public CloseableHandle openDir(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public List<DirEntry> readDir(Handle handle, AtomicReference<Boolean> eolIndicator) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public String canonicalPath(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public Attributes stat(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public Attributes lstat(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public Attributes stat(Handle handle) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void setStat(String path, Attributes attributes) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void setStat(Handle handle, Attributes attributes) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public String readLink(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void link(String linkPath, String targetPath, boolean symbolic) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void lock(Handle handle, long offset, long length, int mask) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public void unlock(Handle handle, long offset, long length) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public Iterable<DirEntry> readDir(String path) throws IOException {
        throw new SftpException(SftpConstants.SSH_FX_FAILURE, "This is a test");
    }

    @Override
    public <E extends SftpClientExtension> E getExtension(Class<? extends E> extensionType) {

        return null;
    }

    @Override
    public SftpClientExtension getExtension(String extensionName) {

        return null;
    }

}
