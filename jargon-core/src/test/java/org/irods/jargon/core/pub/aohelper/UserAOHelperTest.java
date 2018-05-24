package org.irods.jargon.core.pub.aohelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class UserAOHelperTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetUserNameFromUserPoundZoneNoZone() {
		String user = "testuser";
		String actual = UserAOHelper.getUserNameFromUserPoundZone(user);
		Assert.assertEquals("did not get user properly", user, actual);
	}

	@Test
	public final void testGetUserNameFromUserPoundZoneWithZone() {
		String user = "testuser";
		String zone = "testzone";
		String fullUser = user + "#" + zone;
		String actual = UserAOHelper.getUserNameFromUserPoundZone(fullUser);
		Assert.assertEquals("did not get user properly", user, actual);
	}

	@Test
	public final void testGetZoneFromUserPoundZoneWithZone() {
		String user = "testuser";
		String zone = "testzone";
		String fullUser = user + "#" + zone;
		String actual = UserAOHelper.getZoneFromUserPoundZone(fullUser);
		Assert.assertEquals("did not get zone properly", zone, actual);
	}

	@Test
	public final void testGetZoneFromUserPoundZoneWithNoZone() {
		String user = "testuser";
		String actual = UserAOHelper.getZoneFromUserPoundZone(user);
		Assert.assertEquals("did not get zone properly", "", actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGetZoneFromUserPoundZoneNull() {
		UserAOHelper.getZoneFromUserPoundZone(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testGetZoneFromUserPoundZoneBlank() {
		UserAOHelper.getZoneFromUserPoundZone("");
	}

}
