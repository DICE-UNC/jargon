package org.irods.jargon.core.utils;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class IRODSUriUtilsTest {

	@Test
	public void testGetUserNameFromURI() throws Exception {
		final URI uri = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		final String actual = IRODSUriUtils.getUserNameFromURI(uri);
		Assert.assertNotNull("null user name", actual);
		Assert.assertEquals("did not derive user name from URI", "user",
				actual);
	}

	@Test
	public void testGetPasswordFromURI() throws Exception {
		final URI uri = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		final String actual = IRODSUriUtils.getPasswordFromURI(uri);
		Assert.assertNotNull("null password", actual);
		Assert.assertEquals("did not derive password from URI", "password",
				actual);
	}

	@Test
	public void testGetPasswordFromURINoPassword() throws Exception {
		final URI testURI = new URI(
				"irods://user.zone@host.domain:10000/zone/home/user/afile.txt");
		String actual = IRODSUriUtils.getPasswordFromURI(testURI);
		Assert.assertNull("password should be null", actual);
	}

	@Test
	public void testGetZoneFromURI() throws Exception {
		final URI uri = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		final String actual = IRODSUriUtils.getZoneFromURI(uri);
		Assert.assertNotNull("null zone", actual);
		Assert.assertEquals("did not derive zone from URI", "zone", actual);
	}

	@Test
	public void testGetHostFromURI() throws Exception {
		final URI uri = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		final String actual = IRODSUriUtils.getHostFromURI(uri);
		Assert.assertNotNull("null host", actual);
		Assert.assertEquals("did not derive host from URI", "host.domain",
				actual);
	}

	@Test
	public void testGetPortFromURI() throws Exception {
		final URI uri = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		final int actual = IRODSUriUtils.getPortFromURI(uri);
		Assert.assertEquals("did not derive port from URI", 10000, actual);
	}

	@Test
	public void testGetAbsolutePathFromURI() throws Exception {
		final URI testURI = new URI(
				"irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt"
		);
		String actual = IRODSUriUtils.getAbsolutePathFromURI(testURI);
		Assert.assertNotNull("no path returned", actual);
	}

	@Test
	public void testIsIRODSURISchemeFalse() throws Exception {
		final URI uri = new URI("http://host");
		Assert.assertFalse(IRODSUriUtils.isIRODSURIScheme(uri));
	}

	@Test
	public void testIsIRODSURISchemeTrue() throws Exception {
		final URI uri = new URI("irods://host");
		Assert.assertTrue(IRODSUriUtils.isIRODSURIScheme(uri));
	}

	@Test
	public void testBuildBaseURI() throws Exception {
		final URI expectedURI = new URI("irods://user@host.domain:10000/");
		final URI actualURI = IRODSUriUtils.buildBaseURI("host.domain", 10000,
				"user");
		Assert.assertEquals("uri not computed correctly", expectedURI,
				actualURI);
	}

	@Test
	public void testBuildURIUserName() throws Exception {
		final URI expectedURI = new URI(
				"irods://user@host.domain:10000/path/to/entity");
		final URI actualURI = IRODSUriUtils.buildURI("host.domain", 10000,
				"user", "/path/to/entity");
		Assert.assertEquals(expectedURI, actualURI);
	}

	@Test
	public void testBuildURIUserInfo() throws Exception {
		final URI expectedURI = new URI(
				"irods://user.zone:password@host.domain:10000/path/to/entity");
		final IRODSUriUtils.UserInfo info = IRODSUriUtils.UserInfo.instance(
				"user", "zone", "password");
		final URI actualURI = IRODSUriUtils.buildURI("host.domain", 10000, info,
				"/path/to/entity");
		Assert.assertEquals(expectedURI, actualURI);
	}

	@Test
	public void testBuildAnonymousURI() {
		final URI uri = IRODSUriUtils.buildAnonymousURI("host.domain", 10000,
				"/zone/home/user/afile.txt");
		Assert.assertNull("URI contains user info", uri.getUserInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildAnonymouuseURINoPath() {
		IRODSUriUtils.buildAnonymousURI("host.domain", 10000, null);
	}

	@Test
	public void testEcoding() throws Exception {
		final URI expectedURI = new URI(
				"irods://us%5ber@host.domain:10000/path/t%20o/entity%7B");
		final URI actualURI = IRODSUriUtils.buildURI("host.domain", 10000,
				"us[er", "/path/t o/entity{");
		Assert.assertEquals(expectedURI, actualURI);
	}

}
