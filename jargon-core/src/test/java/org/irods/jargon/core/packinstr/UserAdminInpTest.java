package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserAdminInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetTagValueForUserPasswordChange() throws Exception {
		String userName = "testuser";
		String obfuscatedPassword = "obfuscatedPassword";
		UserAdminInp userAdminInp = UserAdminInp.instanceForChangeUserPassword(
				userName, obfuscatedPassword);
		String tagValue = userAdminInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<userAdminInp_PI><arg0>userpw</arg0>\n");
		sb.append("<arg1>testuser</arg1>\n");
		sb.append("<arg2>password</arg2>\n");
		sb.append("<arg3>obfuscatedPassword</arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</userAdminInp_PI>\n");

		Assert.assertEquals("invalid tags generated", sb.toString(), tagValue);
	}

	@Test
	public final void testInstanceForChangeUserPassword() throws Exception {
		String userName = "testuser";
		String obfuscatedPassword = "obfuscatedPassword";
		UserAdminInp userAdminInp = UserAdminInp.instanceForChangeUserPassword(
				userName, obfuscatedPassword);
		Assert.assertNotNull("null userAdminInp", userAdminInp);
		Assert.assertEquals("incorrect API number", 714,
				userAdminInp.getApiNumber());
	}

}
