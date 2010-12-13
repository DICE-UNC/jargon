package org.irods.jargon.part.policy.parameter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class UserGroupParameterResolverTest {
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
	public final void testUserGroupParameterResolver() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		UserGroupParameterResolver userGroupParameterResolver = new UserGroupParameterResolver(
				irodsAccessObjectFactory, irodsAccount);
		TestCase.assertNotNull(userGroupParameterResolver
				.getIrodsAccessObjectFactory());
		TestCase.assertNotNull(userGroupParameterResolver.getIrodsAccount());
	}

	@Test
	public final void testResolve() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		
		List<UserGroup> userGroups = new ArrayList<UserGroup>();
		UserGroupAO userGroupAO = Mockito.mock(UserGroupAO.class);
		Mockito.when(userGroupAO.findUserGroupsForUser(Mockito.anyString())).thenReturn(userGroups);
		
		Mockito.when(irodsAccessObjectFactory.getUserGroupAO(irodsAccount)).thenReturn(userGroupAO);

		UserGroupParameterResolver userGroupParameterResolver = new UserGroupParameterResolver(
				irodsAccessObjectFactory, irodsAccount);
		DynamicPropertyValues value = userGroupParameterResolver.resolve();
		TestCase.assertNotNull(value);
		TestCase.assertEquals("USER_GROUP", value.getDynamicPropertyType());
		TestCase.assertNotNull(value.getDynamicProperties());

	}

}
