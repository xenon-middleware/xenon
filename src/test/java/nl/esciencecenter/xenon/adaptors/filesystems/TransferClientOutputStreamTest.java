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
package nl.esciencecenter.xenon.adaptors.filesystems;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

public class TransferClientOutputStreamTest {

    public class DummyOutputStream extends OutputStream {

        boolean flushed = false;

        @Override
        public void write(int arg0) throws IOException {
            // ignored
        }

        public void flush() {
            flushed = true;
        }

        public boolean isFlushed() {
            return flushed;
        }
    }

    @Test
    public void test_close() throws IOException {

        TestClient c = new TestClient();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);
        tout.close();
        assertTrue(c.isClosed());
    }

    @Test
    public void test_flush() throws IOException {

        TestClient c = new TestClient();

        DummyOutputStream out = new DummyOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);
        tout.flush();
        assertTrue(out.isFlushed());
    }

    @Test
    public void test_writeByte() throws IOException {

        TestClient c = new TestClient();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);

        tout.write(42);
        tout.close();

        byte[] b = out.toByteArray();

        assertNotNull(b);
        assertEquals(1, b.length);
        assertEquals(42, b[0]);
        assertTrue(c.isClosed());
    }

    @Test
    public void test_writeBytes() throws IOException {

        TestClient c = new TestClient();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);

        byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };

        tout.write(data);
        tout.close();

        byte[] b = out.toByteArray();

        assertNotNull(b);
        assertEquals(8, b.length);
        assertArrayEquals(data, b);
        assertTrue(c.isClosed());
    }

    @Test
    public void test_writeByteSlice() throws IOException {

        TestClient c = new TestClient();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);

        byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };

        tout.write(data, 4, 4);
        tout.close();

        byte[] b = out.toByteArray();
        byte[] expected = new byte[] { 4, 5, 6, 7 };

        assertNotNull(b);
        assertEquals(4, b.length);
        assertArrayEquals(expected, b);
        assertTrue(c.isClosed());
    }

    @Test
    public void test_toString() throws IOException {

        TestClient c = new TestClient();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransferClientOutputStream tout = new TransferClientOutputStream(out, c);

        assertEquals(out.toString(), tout.toString());

        tout.close();
    }

}
