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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class TransferClientInputStreamTest {

    class DummyInputStream extends InputStream {

        boolean markCalled = false;
        boolean resetCalled = false;
        long skipped = 0;

        @Override
        public int read() throws IOException {
            return 42;
        }

        @Override
        public int available() throws IOException {
            return 33;
        }

        @Override
        public void mark(int readlimit) {
            markCalled = true;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        public boolean isMarkCalled() {
            return markCalled;
        }

        @Override
        public void reset() throws IOException {
            resetCalled = true;
        }

        public boolean isResetCalled() {
            return resetCalled;
        }

        @Override
        public long skip(long n) throws IOException {
            skipped = n;
            return n;
        }

        public long skipped() {
            return skipped;
        }

        @Override
        public String toString() {
            return "Hello World!";
        }
    }

    @Test
    public void test_close() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);
        tin.close();

        assertTrue(c.isClosed());
    }

    @Test
    public void test_available() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);
        int avail = tin.available();

        tin.close();

        assertEquals(in.available(), avail);
    }

    @Test
    public void test_markSupported() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);
        boolean sup = tin.markSupported();

        tin.close();

        assertEquals(in.markSupported(), sup);
    }

    @Test
    public void test_read() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        int value = tin.read();
        tin.close();

        assertEquals(42, value);
        assertTrue(c.isClosed());
    }

    @Test
    public void test_mark() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        tin.mark(42);
        tin.close();

        assertTrue(in.isMarkCalled());
    }

    @Test
    public void test_reset() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        tin.mark(42);
        tin.reset();
        tin.close();

        assertTrue(in.isResetCalled());
    }

    @Test
    public void test_skip() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        tin.skip(42);
        tin.close();

        assertEquals(42, in.skipped());
    }

    @Test
    public void test_toString() throws IOException {

        TestClient c = new TestClient();

        DummyInputStream in = new DummyInputStream();

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        assertEquals(in.toString(), tin.toString());

        tin.close();
    }

    @Test
    public void test_read_array() throws IOException {

        TestClient c = new TestClient();

        byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        byte[] tmp = new byte[8];

        int count = tin.read(tmp);

        tin.close();

        assertEquals(count, 8);
        assertArrayEquals(data, tmp);
        assertTrue(c.isClosed());
    }

    @Test
    public void test_read_slice() throws IOException {

        TestClient c = new TestClient();

        byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        TransferClientInputStream tin = new TransferClientInputStream(in, c);

        byte[] tmp = new byte[8];

        int count = tin.read(tmp, 4, 4);

        tin.close();

        byte[] expected = new byte[] { 0, 0, 0, 0, 0, 1, 2, 3 };

        assertEquals(count, 4);
        assertArrayEquals(expected, tmp);
        assertTrue(c.isClosed());
    }

}
