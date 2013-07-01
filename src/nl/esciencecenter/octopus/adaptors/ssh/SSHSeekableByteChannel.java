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

package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class SSHSeekableByteChannel implements SeekableByteChannel {
    
    private final boolean read;
    private final boolean write;
    private final boolean append;
    
    private String file;
    private ChannelSftp channel;
    private long offset = 0;
    
    protected SSHSeekableByteChannel(ChannelSftp channel, String file, boolean read, boolean write, boolean append) 
            throws IOException {
        
        this.file = file;
        this.channel = channel;
        this.read = read;
        this.write = write;
        this.append = append;
        
        if (write && !read && append) { 
            this.offset = size();
        }
    }
    
    @Override
    public synchronized void close() throws IOException {
        
        if (channel == null) { 
            return;
        }
        
        channel.disconnect();
        channel = null;
    }

    @Override
    public synchronized boolean isOpen() {
        return (channel != null);
    }

    @Override
    public synchronized long position() throws IOException {
        
        if (channel == null) { 
            throw new ClosedChannelException();
        }
        
        return offset;
    }

    @Override
    public synchronized SeekableByteChannel position(long newPosition) throws IOException {
        
        if (channel == null) { 
            throw new ClosedChannelException();
        }
        
        this.offset = newPosition;
        return this;
    }

    @Override
    public synchronized int read(ByteBuffer dst) throws IOException {
        
        if (channel == null) { 
            throw new ClosedChannelException();
        }
        
        if (!read) { 
            throw new IOException("Channel not opened for reading!");
        }
        
        int maxSize = dst.remaining();
        
        System.err.println("READING " + maxSize + " bytes");
        
        if (maxSize == 0) {
            return 0;
        }
        
        byte [] buffer = new byte[maxSize];
        int len = 0;
                
        InputStream in = null;
        
        try {
            in = channel.get(file, null, offset);
            len = in.read(buffer);
        } catch (SftpException e) {
            throw new IOException("Failed to read bytes from " + file, e);
        } finally { 
            in.close();
        }

        System.err.println("READ " + len + " bytes");
        
        if (len > 0) { 
            dst.put(buffer, 0, len);
            offset += len;
        }
        
        return len;
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {

        if (channel == null) { 
            throw new ClosedChannelException();
        }

        if (!write) { 
            throw new IOException("Channel not opened for reading!");
        }
        
        int maxSize = src.remaining();
        
        if (maxSize == 0) {
            return 0;
        }
        
        byte [] buffer = new byte[maxSize];

        src.get(buffer);
        
        OutputStream out = null;
        
        System.out.println("WRITING " + maxSize + " bytes to file " + file + " at offset " + offset);
        
        try {
            out = channel.put(file, null, ChannelSftp.OVERWRITE, offset);
            out.write(buffer);
        } catch (SftpException e) {
            throw new IOException("Failed to write bytes to " + file, e);
        } finally { 
            out.close();
        }

        offset += maxSize;
        return maxSize;
    }
    
    @Override
    public synchronized long size() throws IOException {
        
        if (channel == null) { 
            throw new ClosedChannelException();
        }
        
        try {
            SftpATTRS attr = channel.lstat(file);
            return attr.getSize();
        }  catch (SftpException e) {
            throw new IOException("Failed to retrieve size of " + file, e);
        }
    }

    @Override
    public synchronized SeekableByteChannel truncate(long size) throws IOException {
        
        if (channel == null) { 
            throw new ClosedChannelException();
        }

        try {
            SftpATTRS attr = channel.lstat(file);
            attr.setSIZE(size);
            channel.setStat(file, attr);
        }  catch (SftpException e) {
            throw new IOException("Failed to trucate " + file, e);
        }
        
        return this;
    }


}
