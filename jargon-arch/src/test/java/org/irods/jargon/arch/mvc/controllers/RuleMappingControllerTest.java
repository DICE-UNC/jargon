package org.irods.jargon.arch.mvc.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

public class RuleMappingControllerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static RuleMappingController controller = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		controller = new RuleMappingController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testListRuleMappings() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyDrivenRulesManager((IRODSAccount) Mockito
										.any())).thenReturn(manager);
		Mockito.when(manager.listRulesInRepository(Mockito.anyString())).thenReturn(
				new ArrayList<PolicyDrivenServiceListingEntry>());

		ModelAndView mav = controller.listRuleMappings();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav
				.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ruleadmin_rulemapping_view");
	}
	
	@Test
	public void testBuildAjaxRuleMappingDirContentsList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyDrivenRulesManager((IRODSAccount) Mockito
										.any())).thenReturn(manager);
		Mockito.when(manager.listRulesInRepository("a Repository")).thenReturn(
				new ArrayList<PolicyDrivenServiceListingEntry>());

		ModelAndView mav = controller.buildAjaxRuleMappingDirContentsList("ruleName");
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav
				.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_rule_mapping_dir_contents");
	}
	
	@Test(expected=ArchException.class)
	public void testBuildAjaxRuleMappingDirContentsListMissingName() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyDrivenRulesManager((IRODSAccount) Mockito
										.any())).thenReturn(manager);
		Mockito.when(manager.listRulesInRepository("ruleName")).thenThrow(new DataNotFoundException("blah"));

		ModelAndView mav = controller.buildAjaxRuleMappingDirContentsList("ruleName");
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav
				.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_rule_mapping_list");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddOrUploadRuleMapping() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyDrivenRulesManager((IRODSAccount) Mockito
										.any())).thenReturn(manager);
		Mockito.when(manager.listRulesInRepository(Mockito.anyString())).thenReturn(
				new ArrayList<PolicyDrivenServiceListingEntry>());

		ModelAndView mav = controller.addOrUploadRuleMapping();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav
				.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ruleadmin_rulemapping_add");
	}
	
	@Test
	public void buildAjaxRuleMappingDirDetails() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyDrivenRulesManager((IRODSAccount) Mockito
										.any())).thenReturn(manager);
		Mockito.when(manager.listRulesInRepository(Mockito.anyString())).thenReturn(
				new ArrayList<PolicyDrivenServiceListingEntry>());

		ModelAndView mav = controller.addOrUploadRuleMapping();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav
				.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ruleadmin_rulemapping_add");
	}

}
