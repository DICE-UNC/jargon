package org.irods.jargon.core.packinstr;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PamAuthRequestInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testValidTag() throws Exception {
		PamAuthRequestInp request = PamAuthRequestInp.instance("user",
				"password");
		String tagVal = request.getParsedTags();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<pamAuthRequestInp_PI><pamUser>user</pamUser>\n");
		sb.append("<pamPassword>password</pamPassword>\n");
		sb.append("</pamAuthRequestInp_PI>\n");
		
		Assert.assertEquals("did not get correct tag format", sb.toString(),
				tagVal);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserNull() throws Exception {
		PamAuthRequestInp.instance(null, "password");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testUserBlankl() throws Exception {
		PamAuthRequestInp.instance("", "password");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testPasswordNull() throws Exception {
		PamAuthRequestInp.instance("user", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testPasswordBlankl() throws Exception {
		PamAuthRequestInp.instance("user", "");
	}

}

