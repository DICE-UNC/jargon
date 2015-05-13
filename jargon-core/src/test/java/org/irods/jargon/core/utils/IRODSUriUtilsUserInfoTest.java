package org.irods.jargon.core.utils;

import org.irods.jargon.core.utils.IRODSUriUtils.UserInfo;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by tedgin on 5/7/15.
 */
public final class IRODSUriUtilsUserInfoTest {

    @Test
    public void testInstance() {
        final UserInfo info = UserInfo.instance("user", "zone", "password");
        Assert.assertEquals("user.zone:password", info.toString());
    }

    @Test
    public void testLocalInstance() {
        final UserInfo info = UserInfo.localInstance("user", "password");
        Assert.assertEquals("user:password", info.toString());
    }

    @Test
    public void testUnauthenticatedInstance() {
        final UserInfo info = UserInfo.unauthenticatedInstance("user", "zone");
        Assert.assertEquals("user.zone", info.toString());
    }

    @Test
    public void testUnauthenticatedLocalInstance() {
        final UserInfo info = UserInfo.unauthenticatedLocalInstance("user");
        Assert.assertEquals("user", info.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceWithBlankUser() {
        UserInfo.instance("", "zone", "password");
    }

    @Test
    public void testInstanceWithBlankZone() {
        final UserInfo info = UserInfo.instance("user", "", "password");
        Assert.assertEquals(null, info.getZone());
    }

    @Test
    public void testInstanceWithBlankPassword() {
        final UserInfo info = UserInfo.instance("user", "zone", "");
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringNull() {
        Assert.assertNull(UserInfo.fromString(null));
    }

    @Test
    public void testFromStringUserOnly() {
        final UserInfo info = UserInfo.fromString("user");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals(null, info.getZone());
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringUserAndPassword() {
        final UserInfo info = UserInfo.fromString("user:password");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals(null, info.getZone());
        Assert.assertEquals("password", info.getPassword());
    }

    @Test
    public void testFromStringUserAndZone() {
        final UserInfo info = UserInfo.fromString("user.zone");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals("zone", info.getZone());
        Assert.assertEquals(null, info.getPassword());
    }

    @Test
    public void testFromStringUserZoneAndPassword() {
        final UserInfo info = UserInfo.fromString("user.zone:password");
        Assert.assertEquals("user", info.getUserName());
        Assert.assertEquals("zone", info.getZone());
        Assert.assertEquals("password", info.getPassword());
    }

    @Test
    public void testFromStringWithEscape() {
        final UserInfo info = UserInfo.fromString("%00user.zone%3c:pass%c4%80word");
        Assert.assertEquals("\u0000user", info.getUserName());
        Assert.assertEquals("zone<", info.getZone());
        Assert.assertEquals("pass\u0100word", info.getPassword());
    }

    @Test
    public void testToStringIntoNoEscaping() {
        final UserInfo info = UserInfo.instance("0123456789",
                "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ-_!~*'();&=+$,");
        final String expected =
                "0123456789.abcdefghijklmnopqrstuvwxyz:ABCDEFGHIJKLMNOPQRSTUVWXYZ-_!~*'();&=+$,";
        Assert.assertEquals(expected,  info.toString());
    }

    @Test
    public void testToStringIntoEscaping() {
        final UserInfo info = UserInfo.instance(".user", "zone:",
                "pass@\u0080word");
        Assert.assertEquals("%2Euser.zone%3A:pass%40%C2%80word", info.toString());
    }

    @Test
    public void testToStringIntoUserOnly() {
        final UserInfo info = UserInfo.instance("user", null, null);
         Assert.assertEquals("user", info.toString());
    }

    @Test
    public void testToStringIntoUserAndZone() {
        final UserInfo info = UserInfo.instance("user", "zone", null);
        Assert.assertEquals("user.zone", info.toString());
    }

    @Test
    public void testToStringIntoUserAndPassword() {
        final UserInfo info = UserInfo.instance("user", null, "password");
        Assert.assertEquals("user:password", info.toString());
    }

}
