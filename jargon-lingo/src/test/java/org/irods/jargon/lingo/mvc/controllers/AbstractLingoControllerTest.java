package org.irods.jargon.lingo.mvc.controllers;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

public class AbstractLingoControllerTest {
	
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetIRODSAuthenticationToken() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(irodsAccount);
	    SecurityContextHolder.getContext().setAuthentication(token);
	    UsersController userController = new UsersController();
	    IRODSAuthenticationToken actualToken = userController.getIRODSAuthenticationToken();
	    Assert.assertNotNull("no auth token found by controller", actualToken);
	
	}

	@Test(expected=JargonException.class)
	public final void testGetIRODSAuthenticationTokenNull() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
	    SecurityContextHolder.getContext().setAuthentication(null);
	    UsersController userController = new UsersController();
	   userController.getIRODSAuthenticationToken();
	
	}

	@Test
	public final void testGetAuthenticatedIRODSAccount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(irodsAccount);
	    SecurityContextHolder.getContext().setAuthentication(token);
	    UsersController userController = new UsersController();
	    IRODSAccount actualAccount = userController.getAuthenticatedIRODSAccount();
	    Assert.assertNotNull("no irodsAccount found", actualAccount);
	}
	
	@Test(expected=JargonRuntimeException.class)
	public final void testGetAuthenticatedIRODSAccountNullAccountInToken() throws Exception {
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(null);
	    SecurityContextHolder.getContext().setAuthentication(token);
	    UsersController userController = new UsersController();
	    userController.getAuthenticatedIRODSAccount();
	}

}
