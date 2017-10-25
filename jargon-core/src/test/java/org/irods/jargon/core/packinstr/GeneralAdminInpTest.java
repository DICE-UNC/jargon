package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralAdminInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testAddUser() throws Exception {
		User user = new User();
		user.setName("test");
		user.setUserDN("dn");
		user.setUserType(UserTypeEnum.RODS_USER);

		GeneralAdminInp pi = GeneralAdminInp.instanceForAddUser(user);

		Assert.assertNotNull(pi);

	}

	@Test
	public void testDeleteUser() throws Exception {
		Assert.assertNotNull(GeneralAdminInp.instanceForDeleteUser("test"));
	}

	@Test(expected = JargonException.class)
	public void testDeleteUserNullName() throws Exception {
		GeneralAdminInp.instanceForDeleteUser(null);
	}

	@Test(expected = JargonException.class)
	public void testModifyUserCommentNullUser() throws Exception {
		GeneralAdminInp.instanceForModifyUserComment(null, "hello");
	}

	@Test(expected = JargonException.class)
	public void testModifyUserCommentNullComment() throws Exception {
		GeneralAdminInp.instanceForModifyUserComment("user", null);
	}

	@Test
	public void testModifyUserCommentBlankComment() throws Exception {
		GeneralAdminInp.instanceForModifyUserComment("user", "");
	}

	@Test(expected = JargonException.class)
	public void testModifyUserInfoNullUser() throws Exception {
		GeneralAdminInp.instanceForModifyUserInfo(null, "hello");
	}

	@Test(expected = JargonException.class)
	public void testModifyUserInfoNullInfo() throws Exception {
		GeneralAdminInp.instanceForModifyUserInfo("user", null);
	}

	@Test
	public void testModifyUserInfoBlankInfo() throws Exception {
		GeneralAdminInp.instanceForModifyUserInfo("user", "");
	}

	@Test(expected = JargonException.class)
	public void testModifyUserTypeNullUser() throws Exception {
		GeneralAdminInp
				.instanceForModifyUserType(null, UserTypeEnum.RODS_ADMIN);
	}

	@Test(expected = JargonException.class)
	public void testModifyUserTypeNullType() throws Exception {
		GeneralAdminInp.instanceForModifyUserType("user", null);
	}

	@Test(expected = JargonException.class)
	public void testModifyUserTypeUnknownType() throws Exception {
		GeneralAdminInp.instanceForModifyUserType("user",
				UserTypeEnum.RODS_UNKNOWN);
	}

	@Test(expected = JargonException.class)
	public void testModifyUserZoneNullUser() throws Exception {
		GeneralAdminInp.instanceForModifyUserZone(null, "hello");
	}

	@Test(expected = JargonException.class)
	public void testModifyUserZoneNullZone() throws Exception {
		GeneralAdminInp.instanceForModifyUserZone("user", null);
	}

	@Test
	public void testModifyUserZoneBlankZone() throws Exception {
		GeneralAdminInp.instanceForModifyUserZone("user", "");
	}

	@Test
	public void testAddUserCheckXML() throws Exception {
		User user = new User();
		user.setName("test");
		user.setUserDN("dn");
		user.setUserType(UserTypeEnum.RODS_USER);

		GeneralAdminInp pi = GeneralAdminInp.instanceForAddUser(user);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>add</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>rodsuser</arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testModifyUserCommentCheckXML() throws Exception {
		String userName = "test";
		String comment = "this is a comment, and this is a very good comment\n it even has line breaks";

		GeneralAdminInp pi = GeneralAdminInp.instanceForModifyUserComment(
				userName, comment);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>comment</arg3>\n");
		sb.append("<arg4>this is a comment, and this is a very good comment\n it even has line breaks</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testSetUserQuotaTotal() throws Exception {
		String userName = "test";
		long quota = 123L;

		GeneralAdminInp pi = GeneralAdminInp.instanceForSetUserQuotaTotal(
				userName, quota);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>set-quota</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>total</arg3>\n");
		sb.append("<arg4>123</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testSetUserGroupQuotaTotal() throws Exception {
		String userName = "test";
		long quota = 123L;

		GeneralAdminInp pi = GeneralAdminInp.instanceForSetUserGroupQuotaTotal(
				userName, quota);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>set-quota</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>total</arg3>\n");
		sb.append("<arg4>123</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testCalculateTotalQuotaUsage() throws Exception {

		GeneralAdminInp pi = GeneralAdminInp.instanceForCalculateQuotaUsage();
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>calculate-usage</arg0>\n");
		sb.append("<arg1></arg1>\n");
		sb.append("<arg2></arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testSetUserQuotaForResource() throws Exception {
		String userName = "test";
		String resourceName = "testResc";
		long quota = 123L;

		GeneralAdminInp pi = GeneralAdminInp
				.instanceForSetUserQuotaForResource(userName, resourceName,
						quota);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>set-quota</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>testResc</arg3>\n");
		sb.append("<arg4>123</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testSetUserGroupQuotaForResource() throws Exception {
		String userName = "test";
		String resourceName = "testResc";
		long quota = 123L;

		GeneralAdminInp pi = GeneralAdminInp
				.instanceForSetUserGroupQuotaForResource(userName,
						resourceName, quota);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>set-quota</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>testResc</arg3>\n");
		sb.append("<arg4>123</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testModifyUserTypeCheckXML() throws Exception {
		String userName = "test";
		UserTypeEnum userType = UserTypeEnum.RODS_ADMIN;

		GeneralAdminInp pi = GeneralAdminInp.instanceForModifyUserType(
				userName, userType);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>type</arg3>\n");
		sb.append("<arg4>rodsadmin</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testModifyUserInfoCheckXML() throws Exception {
		String userName = "test";
		String info = "this is info, and this is a very good info\n it even has line breaks";

		GeneralAdminInp pi = GeneralAdminInp.instanceForModifyUserInfo(
				userName, info);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>info</arg3>\n");
		sb.append("<arg4>this is info, and this is a very good info\n it even has line breaks</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testModifyUserZoneCheckXML() throws Exception {
		String userName = "test";
		String zone = "zonezonezone";
		GeneralAdminInp pi = GeneralAdminInp.instanceForModifyUserZone(
				userName, zone);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>zone</arg3>\n");
		sb.append("<arg4>zonezonezone</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testRmUserCheckXML() throws Exception {
		String testUserName = "testUser";

		GeneralAdminInp pi = GeneralAdminInp
				.instanceForDeleteUser(testUserName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>rm</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>testUser</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testAddUserCheckAPIType() throws Exception {
		User user = new User();
		user.setName("test");
		user.setUserDN("dn");
		user.setUserType(UserTypeEnum.RODS_USER);

		GeneralAdminInp pi = GeneralAdminInp.instanceForAddUser(user);
		Assert.assertEquals("incorrect api type",
				GeneralAdminInp.GEN_ADMIN_INP_API_NBR, pi.getApiNumber());
	}

	@Test(expected = JargonException.class)
	public void testAddUserBlankUserData() throws Exception {
		User user = new User();

		GeneralAdminInp.instanceForAddUser(user);
	}

	@Test(expected = JargonException.class)
	public void testAddUserNullUser() throws Exception {
		GeneralAdminInp.instanceForAddUser(null);
	}

	@Test(expected = JargonException.class)
	public void testAddUseUnknownType() throws Exception {
		User user = new User();
		user.setName("test");
		user.setUserDN("dn");
		GeneralAdminInp.instanceForAddUser(user);

	}

	@Test
	public void testModifyUserPassword() throws Exception {
		String userName = "test";
		String password = "testpassword";

		GeneralAdminInp pi = GeneralAdminInp.instanceForModifyUserPassword(
				userName, password);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>user</arg1>\n");
		sb.append("<arg2>test</arg2>\n");
		sb.append("<arg3>password</arg3>\n");
		sb.append("<arg4>testpassword</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = JargonException.class)
	public void testModifyUserPasswordBlankUser() throws Exception {
		String userName = "";
		String password = "testpassword";

		GeneralAdminInp.instanceForModifyUserPassword(userName, password);

	}

	@Test(expected = JargonException.class)
	public void testModifyUserPasswordNullUser() throws Exception {
		String userName = null;
		String password = "testpassword";

		GeneralAdminInp.instanceForModifyUserPassword(userName, password);

	}

	@Test(expected = JargonException.class)
	public void testModifyUserPasswordBlankPassword() throws Exception {
		String userName = "user";
		String password = "";

		GeneralAdminInp.instanceForModifyUserPassword(userName, password);

	}

	@Test(expected = JargonException.class)
	public void testModifyUserPasswordNullPassword() throws Exception {
		String userName = "user";
		String password = null;

		GeneralAdminInp.instanceForModifyUserPassword(userName, password);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupNullUser() throws Exception {
		GeneralAdminInp.instanceForAddUserToGroup(null, "test", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupBlankUser() throws Exception {
		GeneralAdminInp.instanceForAddUserToGroup("", "test", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupNullGroup() throws Exception {
		GeneralAdminInp.instanceForAddUserToGroup("test", null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUserToGroupBlankGroup() throws Exception {
		GeneralAdminInp.instanceForAddUserToGroup("test", "", null);
	}

	@Test
	public void testAddUserToGroupNullZone() throws Exception {
		GeneralAdminInp actual = GeneralAdminInp.instanceForAddUserToGroup(
				"userGroup", "userName", null);
		String tagOut = actual.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>userGroup</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>userName</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);

	}

	@Test
	public void testAddUserToGroupSpacesZone() throws Exception {
		GeneralAdminInp actual = GeneralAdminInp.instanceForAddUserToGroup(
				"userGroup", "userName", "");
		String tagOut = actual.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>userGroup</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>userName</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);

	}

	@Test
	public void testAddUserToGroupWithZone() throws Exception {
		GeneralAdminInp actual = GeneralAdminInp.instanceForAddUserToGroup(
				"userGroup", "userName", "zone");
		String tagOut = actual.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>userGroup</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>userName#zone</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupNullUser() throws Exception {
		GeneralAdminInp.instanceForRemoveUserFromGroup(null, "test", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupBlankUser() throws Exception {
		GeneralAdminInp.instanceForRemoveUserFromGroup("", "test", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupNullGroup() throws Exception {
		GeneralAdminInp.instanceForRemoveUserFromGroup("test", null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveUserFromGroupBlankGroup() throws Exception {
		GeneralAdminInp.instanceForRemoveUserFromGroup("test", "", null);
	}

	@Test
	public void testRemoveUserFromGroupNullZone() throws Exception {
		GeneralAdminInp actual = GeneralAdminInp
				.instanceForRemoveUserFromGroup("userGroup", "userName", null);
		String tagOut = actual.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>userGroup</arg2>\n");
		sb.append("<arg3>remove</arg3>\n");
		sb.append("<arg4>userName</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);

	}

	@Test
	public void testRemoveUserFromGroupSpacesZone() throws Exception {
		GeneralAdminInp actual = GeneralAdminInp
				.instanceForRemoveUserFromGroup("userGroup", "userName", "");
		String tagOut = actual.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>modify</arg0>\n");
		sb.append("<arg1>group</arg1>\n");
		sb.append("<arg2>userGroup</arg2>\n");
		sb.append("<arg3>remove</arg3>\n");
		sb.append("<arg4>userName</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);

	}

}
