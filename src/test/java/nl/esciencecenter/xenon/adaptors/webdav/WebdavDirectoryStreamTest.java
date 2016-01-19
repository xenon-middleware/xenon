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

package nl.esciencecenter.xenon.adaptors.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * @author Christiaan Meijer <C.Meijer@esciencecenter.nl>
 *
 */
public class WebdavDirectoryStreamTest {
    @Test
    public void clean_empty_returnsNoElements() {
        String path = "";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals(0, cleaned.size());
    }

    @Test
    public void clean_onlyDoubleSlash_returnsNoElements() {
        String path = "//";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals(0, cleaned.size());
    }

    @Test
    public void clean_onlySlash_returnsNoElements() {
        String path = "/";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals(0, cleaned.size());
    }

    @Test
    public void clean_slashText_returns1Element() {
        String path = "/text";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals(1, cleaned.size());
    }

    @Test
    public void clean_slashText_returnsCorrectElement() {
        String path = "/text";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals("text", cleaned.get(0));
    }

    @Test
    public void clean_completeUrl_returnsCorrectNumberOfElements() {
        String path = "http://stackoverflow.com/questions/13565876/remove-all-occurrences-of-an-element-from-arraylist";
        List<String> cleaned = WebdavDirectoryStream.clean(path);
        assertEquals(4, cleaned.size());
    }

    @Test
    public void isSame_sameString_returnTrue() {
        String element = new String("a");
        List<String> entryElements = new LinkedList<String>(Arrays.asList(element));
        List<String> parentElements = new LinkedList<String>(Arrays.asList(element));
        boolean cleaned = WebdavDirectoryStream.isSame(entryElements, parentElements);
        assertTrue(cleaned);
    }

    @Test
    public void isSame_differentStringsInstancesSameValue_returnTrue() {
        List<String> entryElements = new LinkedList<String>(Arrays.asList(new String("a")));
        List<String> parentElements = new LinkedList<String>(Arrays.asList(new String("a")));
        boolean cleaned = WebdavDirectoryStream.isSame(entryElements, parentElements);
        assertTrue(cleaned);
    }

    @Test
    public void next_responseOtherAsPath_correctResult() throws XenonException {
        Path dir = new PathImplementation(getDummyFileSystem(), new RelativePath("/public/xenon/"));
        List<MultiStatusResponse> listing = new LinkedList<MultiStatusResponse>();
        listing.add(new MultiStatusResponse("/public/xenon/sub1/", 200));
        WebdavDirectoryStream webdavDirectoryStream = new WebdavDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER, listing);
        Path path = webdavDirectoryStream.next();
        assertEquals("RelativePath [element=[public, xenon, sub1], seperator=/]", path.getRelativePath().toString());
    }

    @Test(expected = NoSuchElementException.class)
    public void next_responseSameAsPath_zeroResults() throws XenonException {
        Path dir = new PathImplementation(getDummyFileSystem(), new RelativePath("/public/xenon/"));
        List<MultiStatusResponse> listing = new LinkedList<MultiStatusResponse>();
        listing.add(new MultiStatusResponse("/public/xenon/", 200));
        WebdavDirectoryStream webdavDirectoryStream = new WebdavDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER, listing);
        webdavDirectoryStream.next();
    }

    @Test
    public void next_responseNestedSimilarAsPath_oneResult() throws XenonException {
        Path dir = new PathImplementation(getDummyFileSystem(), new RelativePath("/public/xenon/"));
        List<MultiStatusResponse> listing = new LinkedList<MultiStatusResponse>();
        listing.add(new MultiStatusResponse("/public/xenon/public/xenon/", 200));
        WebdavDirectoryStream webdavDirectoryStream = new WebdavDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER, listing);
        webdavDirectoryStream.next();
    }

    @Test
    public void next_responseSubstringOfPath_oneResult() throws XenonException {
        Path dir = new PathImplementation(getDummyFileSystem(), new RelativePath("/public/xenon/public/xenon/"));
        List<MultiStatusResponse> listing = new LinkedList<MultiStatusResponse>();
        listing.add(new MultiStatusResponse("/public/xenon/", 200));
        WebdavDirectoryStream webdavDirectoryStream = new WebdavDirectoryStream(dir, FilesEngine.ACCEPT_ALL_FILTER, listing);
        Path path = webdavDirectoryStream.next();
    }

    private FileSystem getDummyFileSystem() {
        return new FileSystem() {

            @Override
            public String getScheme() {
                return null;
            }

            @Override
            public Map<String, String> getProperties() {
                return null;
            }

            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public Path getEntryPath() {
                return null;
            }

            @Override
            public String getAdaptorName() {
                return null;
            }
        };
    }

}
