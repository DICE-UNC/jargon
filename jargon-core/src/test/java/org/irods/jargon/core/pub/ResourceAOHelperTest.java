package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourceAOHelperTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSAccount mockAccount;
	private static IRODSAccessObjectFactory mockAccessObjectFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		mockAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		mockAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);

	}

	@Test
	public void testFormatImmediateChildrenNoChild() throws Exception {
		String testString = "";

		ResourceAOHelper resourceAOHelper = new ResourceAOHelper(mockAccount, mockAccessObjectFactory);
		List<String> resources = resourceAOHelper.formatImmediateChildren(testString);
		Assert.assertTrue("should be empty children", resources.isEmpty());

	}

	@Test
	public void testFormatImmediateChildrenOneChild() throws Exception {
		String testString = "resource1{}";

		ResourceAOHelper resourceAOHelper = new ResourceAOHelper(mockAccount, mockAccessObjectFactory);
		List<String> resources = resourceAOHelper.formatImmediateChildren(testString);
		Assert.assertEquals("should have 1 entry", 1, resources.size());
		Assert.assertEquals("resc1 first", "resource1", resources.get(0));

	}

	@Test
	public void testFormatImmediateChildren() throws Exception {
		String testString = "resource1{};resource2{}";

		ResourceAOHelper resourceAOHelper = new ResourceAOHelper(mockAccount, mockAccessObjectFactory);
		List<String> resources = resourceAOHelper.formatImmediateChildren(testString);
		Assert.assertEquals("should have two enries", 2, resources.size());
		Assert.assertEquals("resc1 first", "resource1", resources.get(0));
		Assert.assertEquals("resc2 second", "resource2", resources.get(1));

	}

}
