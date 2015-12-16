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
package nl.esciencecenter.xenon.engine.files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.engine.files.PathAttributesPairImplementation;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class PathAttributesPairImplementationTest {

    private Xenon xenon;
    private Files files;
    private FileSystem filesystem;
    private Path root;
    private FileAttributes att;

    @Before
    public void prepare() throws XenonException, XenonException {

        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        root = Utils.getLocalCWD(files);
        filesystem = root.getFileSystem();
        att = files.getAttributes(root);
    }

    @After
    public void cleanup() throws XenonException, XenonException {
        files.close(filesystem);
        XenonFactory.endXenon(xenon);
    }

    @Test
    public void test_equals() throws XenonException {

        PathAttributesPairImplementation tmp = new PathAttributesPairImplementation(root, att);
        PathAttributesPairImplementation tmp2 = new PathAttributesPairImplementation(root, null);
        PathAttributesPairImplementation tmp3 = new PathAttributesPairImplementation(null, att);

        PathAttributesPairImplementation tmp4 = new PathAttributesPairImplementation(root, att);

        PathAttributesPairImplementation tmp5 = new PathAttributesPairImplementation(root, null);
        PathAttributesPairImplementation tmp6 = new PathAttributesPairImplementation(null, att);

        assertFalse(tmp.equals(null));
        assertFalse(tmp.equals("AAP"));
        assertTrue(tmp.equals(tmp));
        assertFalse(tmp.equals(tmp2));
        assertFalse(tmp2.equals(tmp));
        assertTrue(tmp2.equals(tmp5));
        assertFalse(tmp.equals(tmp3));
        assertFalse(tmp3.equals(tmp));
        assertTrue(tmp3.equals(tmp6));
        assertTrue(tmp.equals(tmp4));
    }

    @Test
    public void test_hashCode() throws XenonException {

        final int prime = 31;
        int result = 1;
        result = prime * result + att.hashCode();
        result = prime * result + root.hashCode();

        assertTrue(result == new PathAttributesPairImplementation(root, att).hashCode());

        result = 1;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + root.hashCode();

        assertTrue(result == new PathAttributesPairImplementation(root, null).hashCode());

        result = 1;
        result = prime * result + att.hashCode();
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;

        assertTrue(result == new PathAttributesPairImplementation(null, att).hashCode());

        result = 1;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;

        assertTrue(result == new PathAttributesPairImplementation(null, null).hashCode());

        PathAttributesPairImplementation tmp1 = new PathAttributesPairImplementation(root, att);
        PathAttributesPairImplementation tmp2 = new PathAttributesPairImplementation(root, att);

        assertTrue(tmp1.equals(tmp2));
        assertTrue(tmp1.hashCode() == tmp2.hashCode());
    }

}
