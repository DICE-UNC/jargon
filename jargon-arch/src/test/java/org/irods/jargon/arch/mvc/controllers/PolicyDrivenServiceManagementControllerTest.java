package org.irods.jargon.arch.mvc.controllers;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

public class PolicyDrivenServiceManagementControllerTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static PolicyDrivenServiceManagementController controller = null;
	private static IRODSAccount irodsAccount = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		controller = new PolicyDrivenServiceManagementController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShowOverallViewActionMav() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenServiceManager manager = mock(PolicyDrivenServiceManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenServiceManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		ModelAndView mav = controller.showOverallViewAction();
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"appladmin_view");
	}
	
	@Test
	public void testAddAction() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenServiceManager manager = mock(PolicyDrivenServiceManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenServiceManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		ModelAndView mav = controller.addAction();
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"appladmin_add");
	}
	
	@Test
	public void testAjaxServiceDrivenAppsList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenServiceManager manager = mock(PolicyDrivenServiceManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenServiceManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.findPolicyDrivenServiceNames(Mockito.anyString())).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.ajaxServiceDrivenAppsList();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		
	}

}
