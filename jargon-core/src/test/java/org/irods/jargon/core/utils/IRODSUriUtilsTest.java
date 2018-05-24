package org.irods.jargon.core.utils;

import static org.irods.jargon.core.utils.IRODSUriUtils.buildAnonymousURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.buildBaseURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.buildURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.buildURIForAnAccountAndPath;
import static org.irods.jargon.core.utils.IRODSUriUtils.buildURIForAnAccountWithNoUserInformationIncluded;
import static org.irods.jargon.core.utils.IRODSUriUtils.getAbsolutePathFromURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.getHostFromURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.getIRODSAccountFromURI;
import static org.irods.jargon.core.utils.IRODSUriUtils.getPassword;
import static org.irods.jargon.core.utils.IRODSUriUtils.getUserInfo;
import static org.irods.jargon.core.utils.IRODSUriUtils.getUserName;
import static org.irods.jargon.core.utils.IRODSUriUtils.getZone;
import static org.irods.jargon.core.utils.IRODSUriUtils.isIRODSURIScheme;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.InvalidIRODSUriException;
import org.irods.jargon.core.exception.JargonException;
import org.junit.Test;

public class IRODSUriUtilsTest {

	@Test(expected = InvalidIRODSUriException.class)
	public void testGetUserInfoFromInvalidURI() throws Exception {
		getUserInfo(new URI("http://localhost"));
	}

	@Test
	public void testGetUserInfoFromValidURI() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host:10000/");
		final IRODSUriUserInfo info = getUserInfo(uri);
		assertEquals("user", info.getUserName());
		assertEquals("zone", info.getZone());
		assertEquals("password", info.getPassword());
	}

	@Test
	public void testGetUserName() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		final String actual = getUserName(uri);
		assertEquals("did not derive user name from URI", "user", actual);
	}

	@Test
	public void testGetZone() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		assertEquals("did not derive zone from URI", "zone", getZone(uri));
	}

	@Test
	public void testGetZoneNoZone() throws Exception {
		final URI uri = new URI("irods://user:password@host.domain:10000/zone/home/user/afile.txt");
		assertNull(getZone(uri));
	}

	@Test
	public void testGetPassword() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		final String actual = getPassword(uri);
		assertEquals("did not derive password from URI", "password", actual);
	}

	@Test
	public void testGetPasswordNoPassword() throws Exception {
		final URI testURI = new URI("irods://user.zone@host.domain:10000/zone/home/user/afile.txt");
		assertNull("password should be null", getPassword(testURI));
	}

	@Test
	public void testGetHostFromURI() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		final String actual = getHostFromURI(uri);
		assertNotNull("null host", actual);
		assertEquals("did not derive host from URI", "host.domain", actual);
	}

	@Test
	public void testGetPortFromURI() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		final int actual = IRODSUriUtils.getPortFromURI(uri);
		assertEquals("did not derive port from URI", 10000, actual);
	}

	@Test
	public void testGetAbsolutePathFromURI() throws Exception {
		final URI testURI = new URI("irods://user.zone:password@host.domain:10000/zone/home/user/afile.txt");
		final String actual = getAbsolutePathFromURI(testURI);
		assertNotNull("no path returned", actual);
	}

	@Test(expected = InvalidIRODSUriException.class)
	public void testGetIRODSAccountFromURIInvalidURI() throws Exception {
		getIRODSAccountFromURI(new URI("http://host"));
	}

	@Test(expected = JargonException.class)
	public void testGetIRODSAccountFromURINoUserInfo() throws Exception {
		getIRODSAccountFromURI(new URI("irods://host:10000/"));
	}

	@Test(expected = JargonException.class)
	public void testGetIRODSAccountFromURINoZone() throws Exception {
		getIRODSAccountFromURI(new URI("irods://user:password@host:10000/"));
	}

	@Test(expected = JargonException.class)
	public void testGetIRODSAccountFromURINoPassword() throws Exception {
		getIRODSAccountFromURI(new URI("irods://user.zone@host:10000/"));
	}

	@Test
	public void testGetIRODSAccountFromURIGoodUserInfo() throws Exception {
		final URI uri = new URI("irods://user.zone:password@host:10000/");
		final IRODSAccount actual = getIRODSAccountFromURI(uri);
		final IRODSAccount expected = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user",
				"zone", "");
		assertEquals(expected, actual);
		assertEquals(expected.getZone(), actual.getZone());
		assertEquals(expected.getPassword(), actual.getPassword());
		assertEquals(expected.getHomeDirectory(), actual.getHomeDirectory());
		assertEquals(expected.getDefaultStorageResource(), actual.getDefaultStorageResource());
	}

	@Test
	public void testIsIRODSURISchemeFalse() throws Exception {
		final URI uri = new URI("http://host");
		assertFalse(isIRODSURIScheme(uri));
	}

	@Test
	public void testIsIRODSURISchemeTrue() throws Exception {
		final URI uri = new URI("irods://host");
		assertTrue(isIRODSURIScheme(uri));
	}

	@Test
	public void testBuildBaseURI() throws Exception {
		final URI expectedURI = new URI("irods://user@host.domain:10000/");
		final URI actualURI = buildBaseURI("host.domain", 10000, "user");
		assertEquals("uri not computed correctly", expectedURI, actualURI);
	}

	@Test
	public void testBuildURIUserName() throws Exception {
		final URI expectedURI = new URI("irods://user@host.domain:10000/path/to/entity");
		final URI actualURI = buildURI("host.domain", 10000, "user", "/path/to/entity");
		assertEquals(expectedURI, actualURI);
	}

	@Test
	public void testBuildURIUserInfo() throws Exception {
		final URI expectedURI = new URI("irods://user.zone:password@host.domain:10000/path/to/entity");
		final IRODSUriUserInfo info = IRODSUriUserInfo.instance("user", "zone", "password");
		final URI actualURI = buildURI("host.domain", 10000, info, "/path/to/entity");
		assertEquals(expectedURI, actualURI);
	}

	@Test
	public void testBuildAnonymousURI() {
		final URI uri = buildAnonymousURI("host.domain", 10000, "/zone/home/user/afile.txt");
		assertNull("URI contains user info", uri.getUserInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildAnonymouuseURINoPath() {
		buildAnonymousURI("host.domain", 10000, null);
	}

	@Test
	public void testEcoding() throws Exception {
		final URI expectedURI = new URI("irods://us%5ber@host.domain:10000/path/t%20o/entity%7B");
		final URI actualURI = buildURI("host.domain", 10000, "us[er", "/path/t o/entity{");
		assertEquals(expectedURI, actualURI);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildURIForAnAccountAndPathNoAccount() {
		buildURIForAnAccountAndPath(null, "/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildURIForAnAccountAndPathNoPath() throws Exception {
		final IRODSAccount acnt = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user", "zone",
				"");
		buildURIForAnAccountAndPath(acnt, null);
	}

	@Test
	public void testBuildURIForAnAccountAndAbsPath() throws Exception {
		final IRODSAccount acnt = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user", "zone",
				"");
		final URI actual = buildURIForAnAccountAndPath(acnt, "/");
		assertEquals(new URI("irods://user@host:10000/"), actual);
	}

	@Test
	public void testBuildURIForAnAccountAndRelPath() throws Exception {
		final IRODSAccount acnt = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user", "zone",
				"");
		final URI actual = buildURIForAnAccountAndPath(acnt, "file.txt");
		final URI expected = new URI("irods://user@host:10000/zone/home/user/file.txt");
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildURIForAnAccountWithNoUserInformationIncludedNoAccount() {
		buildURIForAnAccountWithNoUserInformationIncluded(null, "/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildURIForAnAccountWithNoUserInformationIncludedNoPath() throws Exception {
		final IRODSAccount acnt = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user", "zone",
				"");
		buildURIForAnAccountWithNoUserInformationIncluded(acnt, "");
	}

	@Test
	public void testBuildURIForAnAccountWithNoUserInformationIncluded() throws Exception {
		final IRODSAccount acnt = IRODSAccount.instance("host", 10000, "user", "password", "/zone/home/user", "zone",
				"");
		final URI actual = buildURIForAnAccountWithNoUserInformationIncluded(acnt, "/");
		assertEquals(new URI("irods://host:10000/"), actual);
	}
}
