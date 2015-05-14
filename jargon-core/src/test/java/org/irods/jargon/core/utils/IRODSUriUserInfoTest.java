package org.irods.jargon.core.utils;

import org.junit.Assert;
import org.junit.Test;


/**
 * Created by tedgin on 5/7/15.
 */
public final class IRODSUriUserInfoTest {

    @Test
    public void testInstance() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", "zone",
                "password");
        Assert.assertEquals("user.zone:password", info.toString());
    }

    @Test
    public void testLocalInstance() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.localInstance("user",
                "password");
        Assert.assertEquals("user:password", info.toString());
    }

    @Test
    public void testUnauthenticatedInstance() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.unauthenticatedInstance(
                "user", "zone");
        Assert.assertEquals("user.zone", info.toString());
    }

    @Test
    public void testUnauthenticatedLocalInstance() {
        final IRODSUriUserInfo info
                = IRODSUriUserInfo.unauthenticatedLocalInstance("user");
        Assert.assertEquals("user", info.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceWithBlankUser() {
        IRODSUriUserInfo.instance("", "zone", "password");
    }

    @Test
    public void testInstanceWithBlankZone() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", "",
                "password");
        Assert.assertEquals(null, info.getZone());
    }

    @Test
    public void testInstanceWithBlankPassword() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", "zone",
                "");
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringNull() {
        Assert.assertNull(IRODSUriUserInfo.fromString(null));
    }

    @Test
    public void testFromStringUserOnly() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.fromString("user");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals(null, info.getZone());
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringUserAndPassword() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.fromString(
                "user:password");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals(null, info.getZone());
        Assert.assertEquals("password", info.getPassword());
    }

    @Test
    public void testFromStringUserAndZone() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.fromString("user.zone");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals("zone", info.getZone());
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringUserZoneAndPassword() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.fromString(
                "user.zone:password");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals("zone", info.getZone());
        Assert.assertEquals("password", info.getPassword());
    }

    @Test
    public void testFromStringWithEscape() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.fromString(
                "%00user.zone%3c:pass%c4%80word");
        Assert.assertEquals("\u0000user", info.getUserName());
        Assert.assertEquals("zone<", info.getZone());
        Assert.assertEquals("pass\u0100word", info.getPassword());
    }

    @Test
    public void testToStringIntoNoEscaping() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("0123456789",
                "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ-_!~*'();&=+$,");
        final String expected =
                "0123456789.abcdefghijklmnopqrstuvwxyz:ABCDEFGHIJKLMNOPQRSTUVWXYZ-_!~*'();&=+$,";
        Assert.assertEquals(expected,  info.toString());
    }

    @Test
    public void testToStringIntoEscaping() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance(".user",
                "zone:", "pass@\u0080word");
        Assert.assertEquals("%2Euser.zone%3A:pass%40%C2%80word",
                info.toString());
    }

    @Test
    public void testToStringIntoUserOnly() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", null,
                null);
         Assert.assertEquals("user", info.toString());
    }

    @Test
    public void testToStringIntoUserAndZone() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", "zone",
                null);
        Assert.assertEquals("user.zone", info.toString());
    }

    @Test
    public void testToStringIntoUserAndPassword() {
        final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", null,
                "password");
        Assert.assertEquals("user:password", info.toString());
    }

}
