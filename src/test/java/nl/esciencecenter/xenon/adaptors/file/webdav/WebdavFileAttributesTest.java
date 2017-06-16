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
package nl.esciencecenter.xenon.adaptors.file.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.file.webdav.WebdavFileAttributes;

public class WebdavFileAttributesTest {
    private WebdavFileAttributes defaultFileAttributes;
    private final DateTime defaultDateTime = new DateTime("2015-06-18T09:29:39Z");

    @Test(expected = XenonException.class)
    public void construct_null_throwException() throws XenonException {
        new WebdavFileAttributes(null);
    }

    @Test
    public void permissions_none_containsZeroPermissions() throws XenonException {
        DavPropertySet testProperties = getNewDefaultProperties();

        WebdavFileAttributes fileAttributes = new WebdavFileAttributes(testProperties);

        assertEquals(0, fileAttributes.permissions().size());
    }

    @Test
    public void equals_itself_true() {
        assertTrue(defaultFileAttributes.equals(defaultFileAttributes));
    }

    @Test
    public void equals_null_false() {
        assertFalse(defaultFileAttributes.equals(null));
    }

    @Test
    public void equals_string_false() {
        assertFalse(defaultFileAttributes.equals("test"));
    }

    @Test
    public void creationDate_none_return0() throws XenonException {
        WebdavFileAttributes attributes = new WebdavFileAttributes(new DavPropertySet());
        assertEquals(0, attributes.creationTime());
    }

    @Test
    public void equals_otherDate_false() throws XenonException {
        DateTime otherTime = defaultDateTime.plusHours(5);
        DavPropertySet otherProperties = getNewProperties(otherTime);
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(otherProperties);

        assertFalse(defaultFileAttributes.equals(otherAttributes));
    }

    @Test
    public void size_length5_return5() throws XenonException {
        Object contentLength = "5";
        DavPropertySet otherProperties = getNewProperties(contentLength);
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(otherProperties);

        assertEquals(5, otherAttributes.size());
    }

    @Test
    public void size_lengthInvalid_return0() throws XenonException {
        Object contentLength = " not an integer ";
        DavPropertySet otherProperties = getNewProperties(contentLength);
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(otherProperties);

        assertEquals(0, otherAttributes.size());
    }

    @Test
    public void getHash_sameAttributes_sameHashes() throws XenonException {
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(getNewDefaultProperties());

        assertEquals(defaultFileAttributes.hashCode(), otherAttributes.hashCode());
    }

    @Before
    public void setUp() throws XenonException {
        defaultFileAttributes = new WebdavFileAttributes(getNewDefaultProperties());
    }

    private DavPropertySet getNewDefaultProperties() {
        DateTime dateTime = defaultDateTime;
        return getNewProperties(dateTime);
    }

    private DavPropertySet getNewProperties(DateTime dateTime) {
        DavPropertySet davPropertySet = new DavPropertySet();
        DavProperty<String> property = new DefaultDavProperty<String>(DavPropertyName.create("creationdate"),
                dateTime.toString());
        davPropertySet.add(property);
        return davPropertySet;
    }

    private DavPropertySet getNewProperties(Object contentLength) {
        DavPropertySet davPropertySet = new DavPropertySet();
        DavProperty<String> property = new DefaultDavProperty<String>(DavPropertyName.create("getcontentlength"),
                contentLength.toString());
        davPropertySet.add(property);
        return davPropertySet;
    }

}
