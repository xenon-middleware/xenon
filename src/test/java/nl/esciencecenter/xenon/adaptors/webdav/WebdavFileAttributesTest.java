package nl.esciencecenter.xenon.adaptors.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.esciencecenter.xenon.XenonException;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class WebdavFileAttributesTest {
    private WebdavFileAttributes defaultFileAttributes;
    private DateTime defaultDateTime = new DateTime("2015-06-18T09:29:39Z");

    @Test(expected = XenonException.class)
    public void construct_null_throwException() throws XenonException {
        new WebdavFileAttributes(null);
    }

    @Test
    public void permissions_none_containsZeroPermissions() throws XenonException {
        // Arrange
        DavPropertySet testProperties = getNewDefaultProperties();

        // Act
        WebdavFileAttributes fileAttributes = new WebdavFileAttributes(testProperties);

        // Assert
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
        // Arrange
        DateTime otherTime = defaultDateTime.plusHours(5);
        DavPropertySet otherProperties = getNewProperties(otherTime);
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(otherProperties);

        // Assert
        assertFalse(defaultFileAttributes.equals(otherAttributes));
    }

    @Test
    public void getHash_sameAttributes_sameHashes() throws XenonException {
        // Arrange
        WebdavFileAttributes otherAttributes = new WebdavFileAttributes(getNewDefaultProperties());

        // Act & Assert
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
        DavProperty<String> property = new DefaultDavProperty<String>(DavPropertyName.create("creationdate"), dateTime.toString());
        davPropertySet.add(property);
        return davPropertySet;
    }

}
