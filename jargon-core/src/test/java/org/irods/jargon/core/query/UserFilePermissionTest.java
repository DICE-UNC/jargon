package org.irods.jargon.core.query;

import junit.framework.Assert;

import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.junit.Test;

public class UserFilePermissionTest {

	@Test
	public void testValidConstructor() {
		String userName = "user";
		String userId = "10";
		FilePermissionEnum fpe = FilePermissionEnum.OWN;
		UserFilePermission userFilePermission = new UserFilePermission(
				userName, userId, fpe, UserTypeEnum.RODS_USER);
		Assert.assertEquals("user name not valid", userName,
				userFilePermission.getUserName());
		Assert.assertEquals("user id not valid", userId,
				userFilePermission.getUserId());
		Assert.assertEquals("fpe invalid", fpe,
				userFilePermission.getFilePermissionEnum());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUserName() {
		String userName = null;
		String userId = "10";
		FilePermissionEnum fpe = FilePermissionEnum.OWN;
		new UserFilePermission(userName, userId, fpe, UserTypeEnum.RODS_USER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankUserName() {
		String userName = "";
		String userId = "10";
		FilePermissionEnum fpe = FilePermissionEnum.OWN;
		new UserFilePermission(userName, userId, fpe, UserTypeEnum.RODS_USER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUserId() {
		String userName = "xxx";
		String userId = null;
		FilePermissionEnum fpe = FilePermissionEnum.OWN;
		new UserFilePermission(userName, userId, fpe, UserTypeEnum.RODS_USER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlankUserId() {
		String userName = "";
		String userId = null;
		FilePermissionEnum fpe = FilePermissionEnum.OWN;
		new UserFilePermission(userName, userId, fpe, UserTypeEnum.RODS_USER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullFpe() {
		String userName = "xxx";
		String userId = "xx";
		FilePermissionEnum fpe = null;
		new UserFilePermission(userName, userId, fpe, UserTypeEnum.RODS_USER);
	}

}
