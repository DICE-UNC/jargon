package org.irods.jargon.core.utils;

import java.net.URI;

import org.irods.jargon.core.connection.IRODSAccount;
import org.junit.Assert;
import org.junit.Test;

public class IRODSUriUtilsTest {

	private final String user = "user";
	private final String zone = "zone";
	private final String password = "password";
	private final String host = "host.domain";
	private final int port = 10000;
	private final String resource = "resource";

	private String home;
	private IRODSAccount irodsAccount;
	private String path;

	public IRODSUriUtilsTest() {
		home = "/" + zone + "/home/" + user;
		irodsAccount = new IRODSAccount(host, port, user, password, home, zone,
				resource);
		path = home + "/afile.txt";
	}

	private URI makeURI(final Object userInfo) throws Exception {
		final StringBuilder uriBuilder = new StringBuilder();
		uriBuilder.append("irods://");
		uriBuilder.append(userInfo).append("@");
		uriBuilder.append(host).append(":").append(port);
		uriBuilder.append(path);
		return new URI(uriBuilder.toString());
	}

	private URI makeFullURI() throws Exception {
		return makeURI(user + "." + zone + ":" + password);
	}

	@Test
	public void testGetUserNameFromURI() throws Exception {
		final String actual = IRODSUriUtils.getUserNameFromURI(makeFullURI());
		Assert.assertNotNull("null user name", actual);
		Assert.assertEquals("did not derive user name from URI", user, actual);
	}

	@Test
	public void testGetPasswordFromURI() throws Exception {
		final String actual = IRODSUriUtils.getPasswordFromURI(makeFullURI());
		Assert.assertNotNull("null password", actual);
		Assert.assertEquals("did not derive password from URI",
				password, actual);
	}

	@Test
	public void testGetPasswordFromURINoPassword() throws Exception {
		final URI testURI = makeURI(user + "." + zone);
		String actual = IRODSUriUtils.getPasswordFromURI(testURI);
		Assert.assertNull("password should be null", actual);
	}

	@Test
	public void testGetZoneFromURI() throws Exception {
		final String actual = IRODSUriUtils.getZoneFromURI(makeFullURI());
		Assert.assertNotNull("null zone", actual);
		Assert.assertEquals("did not derive zone from URI", zone, actual);
	}

	@Test
	public void testGetHostFromURI() throws Exception {
		final String actual = IRODSUriUtils.getHostFromURI(makeFullURI());
		Assert.assertNotNull("null host", actual);
		Assert.assertEquals("did not derive host from URI", host, actual);
	}

	@Test
	public void testGetPortFromURI() throws Exception {
		final int actual = IRODSUriUtils.getPortFromURI(makeFullURI());
		Assert.assertEquals("did not derive port from URI", port, actual);
	}

	@Test
	public void testGetAbsolutePathFromURI() throws Exception {
		final URI testURI = makeFullURI();
		String actual = IRODSUriUtils.getAbsolutePathFromURI(testURI);
		Assert.assertNotNull("no path returned", actual);
	}

	@Test
	public void testGetURIFromAccountAndPath() throws Exception {
		final URI testURI = makeURI(user);
		final URI actualURI = IRODSUriUtils.buildURIForAnAccountAndPath(
				irodsAccount, path);
		Assert.assertEquals("uri not computed correctly", testURI, actualURI);
	}

	@Test
	public void buildURIFromIRODSAccountAndThenBuildIRODSAccountFromThatURI()
			throws Exception {
		URI testURI = irodsAccount.toURI(true);
		IRODSAccount newAccount = IRODSUriUtils.getIRODSAccountFromURI(testURI);
		Assert.assertNotNull("null iRODS account", newAccount);
		Assert.assertEquals(irodsAccount.getHost(), newAccount.getHost());
		Assert.assertEquals(irodsAccount.getPort(), newAccount.getPort());
		Assert.assertEquals(irodsAccount.getZone(), newAccount.getZone());
		Assert.assertEquals(irodsAccount.getUserName(),
				newAccount.getUserName());
		Assert.assertEquals(irodsAccount.getPassword(),
				newAccount.getPassword());
	}

}
