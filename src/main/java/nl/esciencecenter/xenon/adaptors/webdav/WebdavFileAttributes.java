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

    protected DavPropertySet properties;

    public WebdavFileAttributes(DavPropertySet properties) throws XenonException {
        if (properties == null) {
            throw new XenonException(WebdavAdaptor.ADAPTOR_NAME, "Cannot create webdav file attributes based on null");
        }

        this.properties = properties;

        printProperties(properties);
    }

    private void printProperties(DavPropertySet properties) {
        System.out.println("***** Printing out properties *****");
        for (DavPropertyName propertyName : properties.getPropertyNames()) {
            DavProperty<?> davProperty = properties.get(propertyName);
            String name = propertyName == null ? "null" : propertyName.getName();
            String valueDescription = davProperty.getValue() == null ? "nullvalue" : davProperty.getValue().toString();
            String classDescription = davProperty.getValue() == null ? "nullClass" : davProperty.getValue().getClass().toString();
            System.out.println(name + " with value " + valueDescription + " and class " + classDescription);
        }
        System.out.println("***** End *****");
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
        return 0;
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
        if (a.creationTime() != b.creationTime()) {
            return false;
        }
        return true;
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
