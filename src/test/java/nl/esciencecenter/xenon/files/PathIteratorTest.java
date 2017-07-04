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
package nl.esciencecenter.xenon.files;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import nl.esciencecenter.xenon.filesystems.Path;

/**
 * 
 */
public class PathIteratorTest {

    @Test
    public void test_ok() {

        String[] s = new String[] { "aap", "noot", "mies" };

        Path path = new Path(s);

        Iterator<Path> itt = path.iterator();

        int i = 0;

        while (itt.hasNext()) {
            Path tmp = itt.next();
            assertEquals(s[i], tmp.getFileNameAsString());
            i++;
        }

        assertEquals(i, s.length);
    }

    @Test
    public void test_iterable() {
        String[] s = new String[] { "aap", "noot", "mies" };
        Path path = new Path(s);
        int i = 0;

        for (Path tmp : path) {
            assertEquals(s[i], tmp.getFileNameAsString());
            i++;
        }

        assertEquals(i, s.length);
    }

    @Test
    public void test_empy() {
        Path path = new Path(new String[0]);
        Iterator<Path> itt = path.iterator();
        assert (!itt.hasNext());
    }

    @Test
    public void test_ok2() {
        Path path = new Path("aap", "noot", "mies");
        Iterator<Path> itt = path.iterator();
        itt.next();
        itt.next();
        itt.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_remove() {
        Path path = new Path("aap", "noot", "mies");
        Iterator<Path> itt = path.iterator();
        itt.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void test_one_next_too_many() {
        Path path = new Path("aap", "noot", "mies");
        Iterator<Path> itt = path.iterator();
        itt.next();
        itt.next();
        itt.next();
        itt.next();
    }

}