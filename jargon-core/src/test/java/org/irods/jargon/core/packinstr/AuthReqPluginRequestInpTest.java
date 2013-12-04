package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.Test;

public class AuthReqPluginRequestInpTest {

	@Test
	public void testNormalPam() throws Exception {
		String userName = "user";
		String password = "password";
		int ttl = 0;

		AuthReqPluginRequestInp pi = AuthReqPluginRequestInp.instancePam(
				userName, password, ttl);

		String actual = pi.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<authPlugReqInp_PI><auth_scheme_>PAM</auth_scheme_>\n");
		sb.append("<context_>a_user=user;a_pw=password;a_ttl=0</context_>\n");
		sb.append("</authPlugReqInp_PI>\n");

		Assert.assertEquals("did not get correct pi back", sb.toString(),
				actual);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamNullUser() throws Exception {
		String userName = null;
		String password = "password";
		int ttl = 0;

		AuthReqPluginRequestInp.instancePam(userName, password, ttl);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamBlankUser() throws Exception {
		String userName = "";
		String password = "password";
		int ttl = 0;

		AuthReqPluginRequestInp.instancePam(userName, password, ttl);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamNullPassword() throws Exception {
		String userName = "xxx";
		String password = null;
		int ttl = 0;

		AuthReqPluginRequestInp.instancePam(userName, password, ttl);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPamBlankPassword() throws Exception {
		String userName = "xxx";
		String password = "";
		int ttl = 0;

		AuthReqPluginRequestInp.instancePam(userName, password, ttl);

	}

}
