package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.StartupResponseData;
import org.junit.Test;

import junit.framework.Assert;

public class AuthReqPluginRequestInpTest {

	@Test
	public void testNormalPam() throws Exception {
		String userName = "user";
		String password = "password";
		int ttl = 0;

		StartupResponseData startupResponseData = new StartupResponseData(0, "rods4.2.0", "d", 0, " ", " ");
		AuthReqPluginRequestInp pi = AuthReqPluginRequestInp.instancePam(userName, password, ttl, startupResponseData);

		String actual = pi.getParsedTags();

		Assert.assertNotNull("did not get correct pi back", actual);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamNullUser() throws Exception {
		String userName = null;
		String password = "password";
		int ttl = 0;

		StartupResponseData startupResponseData = new StartupResponseData(0, "rods4.2.0", "d", 0, " ", " ");
		AuthReqPluginRequestInp.instancePam(userName, password, ttl, startupResponseData);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamBlankUser() throws Exception {
		String userName = "";
		String password = "password";
		int ttl = 0;

		StartupResponseData startupResponseData = new StartupResponseData(0, "rods4.2.0", "d", 0, " ", " ");
		AuthReqPluginRequestInp.instancePam(userName, password, ttl, startupResponseData);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamNullPassword() throws Exception {
		String userName = "xxx";
		String password = null;
		int ttl = 0;

		StartupResponseData startupResponseData = new StartupResponseData(0, "rods4.2.0", "d", 0, " ", " ");
		AuthReqPluginRequestInp.instancePam(userName, password, ttl, startupResponseData);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamBlankPassword() throws Exception {
		String userName = "xxx";
		String password = "";
		int ttl = 0;

		StartupResponseData startupResponseData = new StartupResponseData(0, "rods4.2.0", "d", 0, " ", " ");
		AuthReqPluginRequestInp.instancePam(userName, password, ttl, startupResponseData);

	}

}
