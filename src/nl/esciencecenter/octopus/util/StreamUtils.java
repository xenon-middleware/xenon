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

package nl.esciencecenter.octopus.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * StreamUtils contains various stream utilities that are used in Octopus itself, by other utilities, in examples, etc.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0 
 */
public final class StreamUtils {
    
    /** 
     * The default buffer size to use for copy operations.
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    private StreamUtils() { 
        // DO NOT USE
    }
    
    /**
     * Copy all bytes from an input stream to an output stream.
     * 
     * A temporary buffer of size <code>bufferSize</code> is used. If <code>bufferSize <= 0</code> then the 
     * {@link #DEFAULT_BUFFER_SIZE} will be used.  
     * <p>
     * NOTE: <code>in</code> and <code>out</code> will NOT be explicitly closed once the end of the stream is reached.
     * </p>
     * 
     * @param in
     *          the InputStream to read from.
     * @param out
     *          the OutputStream to write to.
     * @param bufferSize
     *          the size of the temporary buffer, or <= 0 to use the {@link #DEFAULT_BUFFER_SIZE}. 
     * @return
     *          the number of bytes copied.
     * @throws IOException
     *          if an I/O error occurs during the copy operation. 
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        
        long bytes = 0;

        if (bufferSize <= 0) { 
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        
        int len = in.read(buffer);

        while (len != -1) {
            bytes += len;
            out.write(buffer, 0, len);            
            len = in.read(buffer);
        }
        
        return bytes;
    }

    
    /**
     * Read all bytes from the input stream and return them in a byte array.
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * @param in 
     *          the input stream to read. 
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static byte [] readAllBytes(InputStream in) throws IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

        copy(in, buffer, DEFAULT_BUFFER_SIZE);
        
        return buffer.toByteArray();
    }

    /**
     * Read all bytes from the input stream and return them in as a single String.
     * 
     * The bytes are converted to a String using Charset <code>cs</code>. 
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * @param in 
     *          the input stream to read.
     * @param cs
     *          the Charset to use          
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static String readToString(InputStream in, Charset cs) throws IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 

        copy(in, buffer, DEFAULT_BUFFER_SIZE);
        
        return buffer.toString(cs.name());
    }

    /**
     * Read all bytes from the input stream and return them in as a single String. 
     * 
     * The bytes are converted to a String using the default Charset. 
     * <p> 
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * 
     * @param in 
     *          the input stream to read.
     * @return 
     *          a byte array containing all bytes that the input stream produced. 
     * @throws IOException 
     *          if an I/O error was produced while reading the stream. 
     */
    public static String readToString(InputStream in) throws IOException {
        return readToString(in, Charset.defaultCharset());
    }
    
    /**
     * Read all lines from a InputStream and return them in a {@link java.util.List}.
     * 
     * <p>
     * NOTE: <code>in</code> will NOT be explicitly closed once the end of the stream is reached.  
     * </p>
     * 
     * @param in
     *          the InputStream to read from 
     * @param cs
     *          the Charset to use.
     * @return 
     *          a <code>List<String></code> containing all lines in the file.
     * @throws IOException
     *          if an I/O error was produced while reading the stream. 
     */
    public static List<String> readLines(InputStream in, Charset cs) throws IOException {

        ArrayList<String> result = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, cs));

        while (true) {
            String line = reader.readLine();

            if (line == null) {
                return result;
            }

            result.add(line);
        }
    }

    /**
     * Write lines of text to a file.
     * 
     * @param lines 
     *          the lines of text to write.
     * @param cs 
     *          the Charset to use.
     * @param out
     *          the output stream to write to
     * @throws IOException
     *          if an I/O error was produced while writing the stream. 
     */ 
    public static void writeLines(Iterable<? extends CharSequence> lines, Charset cs, OutputStream out) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, cs));
        
        for (CharSequence line : lines) {
            writer.write(line.toString());
            writer.newLine();
        }
        
        writer.flush();
    }
}
