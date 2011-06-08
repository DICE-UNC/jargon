package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.Test;

public class ModAccessControlInpTest {

	@Test
	public void testInstanceGoodNoZone() throws Exception {
		ModAccessControlInp pi = ModAccessControlInp.instanceForSetPermission(true, "", "path",
				"userName", "own");
		Assert.assertEquals(true, pi.isRecursive());
		Assert.assertEquals("", pi.getZone());
		Assert.assertEquals("path", pi.getAbsolutePath());
		Assert.assertEquals("userName", pi.getUserName());
		Assert.assertEquals("own", pi.getPermission());
		Assert.assertEquals(ModAccessControlInp.MOD_ACESS_CONTROL_API_NBR,
				pi.getApiNumber());
	}
	
	@Test
	public void testInstanceInherit() throws Exception {
		ModAccessControlInp pi = ModAccessControlInp.instanceForSetInheritOnACollection(true, "", "path");
		Assert.assertEquals(true, pi.isRecursive());
		Assert.assertEquals("", pi.getZone());
		Assert.assertEquals("path", pi.getAbsolutePath());
		Assert.assertEquals("", pi.getUserName());
		Assert.assertEquals("inherit", pi.getPermission());
		Assert.assertEquals(ModAccessControlInp.MOD_ACESS_CONTROL_API_NBR,
				pi.getApiNumber());
	}
	
	@Test
	public void testInstanceNoInherit() throws Exception {
		ModAccessControlInp pi = ModAccessControlInp.instanceForSetNoInheritOnACollection(true, "", "path");
		Assert.assertEquals(true, pi.isRecursive());
		Assert.assertEquals("", pi.getZone());
		Assert.assertEquals("path", pi.getAbsolutePath());
		Assert.assertEquals("", pi.getUserName());
		Assert.assertEquals("noinherit", pi.getPermission());
		Assert.assertEquals(ModAccessControlInp.MOD_ACESS_CONTROL_API_NBR,
				pi.getApiNumber());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullZone() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, null, "path", "userName",
				"write");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankPath() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "", "userName",
				"read");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullPath() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", null, "userName",
				"read");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankUser() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "path", "",
				"read");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullUser() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "path", null,
				"read");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankPermission() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "path", "user",
				"");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullPermission() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "path", "user",
				null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInstanceInvalidPermission() throws Exception {
		ModAccessControlInp.instanceForSetPermission(true, "zone", "path", "user",
				"nope");
	}
	
	@Test
	public final void testGetParsedTags() throws Exception {
		ModAccessControlInp pi = ModAccessControlInp.instanceForSetPermission(true, "", "path",
				"userName", "read");
		String actualTags = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<modAccessControlInp_PI><recursiveFlag>1</recursiveFlag>\n");
		sb.append("<accessLevel>read</accessLevel>\n");
		sb.append("<userName>userName</userName>\n");
		sb.append("<zone></zone>\n");
		sb.append("<path>path</path>\n");
		sb.append("</modAccessControlInp_PI>\n");

		Assert.assertEquals("invalid packing instruction", sb.toString(),
				actualTags);
	}

	@Test
	public final void testGetParsedTagsAdminMode() throws Exception {
		ModAccessControlInp pi = ModAccessControlInp.instanceForSetPermissionInAdminMode(true, "", "path",
				"userName", "read");
		String actualTags = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<modAccessControlInp_PI><recursiveFlag>1</recursiveFlag>\n");
		sb.append("<accessLevel>admin:read</accessLevel>\n");
		sb.append("<userName>userName</userName>\n");
		sb.append("<zone></zone>\n");
		sb.append("<path>path</path>\n");
		sb.append("</modAccessControlInp_PI>\n");

		Assert.assertEquals("invalid packing instruction", sb.toString(),
				actualTags);
	}
	
}
