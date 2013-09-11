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

package nl.esciencecenter.octopus.files;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class RelativePathIteratorTest {

    @Test
    public void test_ok() {

        String[] s = new String[] { "aap", "noot", "mies" };

        RelativePath path = new RelativePath(s);

        Iterator<RelativePath> itt = path.iterator();

        int i = 0;

        while (itt.hasNext()) {
            RelativePath tmp = itt.next();
            assert (s[i].equals(tmp.getRelativePath()));
            i++;
        }

        assert (i == s.length);
    }

    @Test
    public void test_empy() {
        RelativePath path = new RelativePath(new String[0]);
        Iterator<RelativePath> itt = path.iterator();
        assert (!itt.hasNext());
    }

    @Test
    public void test_ok2() {
        RelativePath path = new RelativePath("aap", "noot", "mies");
        Iterator<RelativePath> itt = path.iterator();
        itt.next();
        itt.next();
        itt.next();
    }

    @Test(expected = InvalidCopyOptionsException.class)
    public void test_remove() {
        RelativePath path = new RelativePath("aap", "noot", "mies");
        Iterator<RelativePath> itt = path.iterator();
        itt.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void test_one_next_too_many() {
        RelativePath path = new RelativePath("aap", "noot", "mies");
        Iterator<RelativePath> itt = path.iterator();
        itt.next();
        itt.next();
        itt.next();
        itt.next();
    }

}