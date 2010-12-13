package org.irods.jargon.lingo.mvc.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

public class UserGroupsControllerTest {
	
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();

	}

	@Test
	public void testIndexAction() throws Exception {
		UserGroupsController userGroupsController = new UserGroupsController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		userGroupsController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		ModelAndView mav = userGroupsController.indexAction();
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"user_groups");
	}

}
