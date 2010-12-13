package org.irods.jargon.part.policy.parameter;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class DynamicPropertyResolverFactoryTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		DynamicPropertyResolverFactory factory = DynamicPropertyResolverFactory
				.instance(irodsAccessObjectFactory, irodsAccount);
		Assert.assertNotNull(factory);

	}

	@Test(expected = PartException.class)
	public final void testInstanceNullAccessObjectFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = null;
		DynamicPropertyResolverFactory.instance(irodsAccessObjectFactory,
				irodsAccount);

	}

	@Test(expected = PartException.class)
	public final void testInstanceNullAccount() throws Exception {
		IRODSAccount irodsAccount = null;
		IRODSAccessObjectFactory irodsAccessObjectFactory = null;
		DynamicPropertyResolverFactory.instance(irodsAccessObjectFactory,
				irodsAccount);
	}

	@Test
	public final void testInstanceUserGroupResolver() throws PartException {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		DynamicPropertyResolverFactory factory = DynamicPropertyResolverFactory
				.instance(irodsAccessObjectFactory, irodsAccount);
		DynamicPropertyResolver resolver = factory
				.getInstance(DynamicPropertyResolverFactory.USER_GROUP_PARAMETER);
		Assert.assertTrue(resolver instanceof UserGroupParameterResolver);
	}

}
