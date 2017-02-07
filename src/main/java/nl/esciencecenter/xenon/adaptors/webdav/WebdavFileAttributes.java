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
package nl.esciencecenter.xenon.adaptors.webdav;

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.joda.time.DateTime;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.AttributeNotSupportedException;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.PosixFilePermission;

public class WebdavFileAttributes implements FileAttributes {
    static private final String CREATION_DATE_KEY = "creationdate";
    static private final String MODIFIED_DATE_KEY = "getlastmodified";
    private static final String CONTENT_LENGTH = "getcontentlength";

    protected DavPropertySet properties;

    public WebdavFileAttributes(DavPropertySet properties) throws XenonException {
        if (properties == null) {
            throw new XenonException(WebdavAdaptor.ADAPTOR_NAME, "Cannot create webdav file attributes based on null");
        }

        this.properties = properties;
    }

    private Object getProperty(String name) {
        DavPropertyName propertyName = DavPropertyName.create(name);
        DavProperty<?> davProperty = properties.get(propertyName);
        return davProperty == null ? null : davProperty.getValue();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return false;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public long creationTime() {
        Object property = getProperty(CREATION_DATE_KEY);
        if (property == null) {
            return 0;
        }
        DateTime dateTime = DateTime.parse((String) property);
        return dateTime.getMillis();
    }

    @Override
    public long lastAccessTime() {
        return lastModifiedTime();
    }

    @Override
    public long lastModifiedTime() {
        DateTime dateTime = tryGetLastModifiedTime();
        if (dateTime == null) {
            return creationTime();
        }
        return dateTime.getMillis();
    }

    private DateTime tryGetLastModifiedTime() {
        Object property = getProperty(MODIFIED_DATE_KEY);
        if (property == null) {
            return null;
        }
        try {
            DateTime dateTime = DateTime.parse((String) property);
            return dateTime;
        } catch (IllegalArgumentException e) {
            // Failed to parse.
        }
        return null;
    }

    @Override
    public long size() {
        try {
            Object contentLength = getProperty(CONTENT_LENGTH);
            return Long.parseLong((String) contentLength);
        } catch (NumberFormatException e) {
            // Unable to determine size, return default.
            return 0;
        }
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public String group() throws AttributeNotSupportedException {
        return null;
    }

    @Override
    public String owner() throws AttributeNotSupportedException {
        return null;
    }

    @Override
    public Set<PosixFilePermission> permissions() throws AttributeNotSupportedException {
        return new HashSet<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WebdavFileAttributes) {
            WebdavFileAttributes other = (WebdavFileAttributes) obj;

            return areIdentical(this, other);
        }

        return false;
    }

    private boolean areIdentical(WebdavFileAttributes a, WebdavFileAttributes b) {
        return a.creationTime() == b.creationTime();
    }

    /**
     * It was necessary to overwrite hashCode() because equals() is overridden also.
     */
    @Override
    public int hashCode() {
        // Hash code is not designed because it is not planned to be used.
        return 0;
    }
}
