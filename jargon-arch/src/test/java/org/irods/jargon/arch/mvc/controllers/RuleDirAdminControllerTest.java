package org.irods.jargon.arch.mvc.controllers;


import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.arch.mvc.controllers.forms.RuleRepositoryForm;
import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

public class RuleDirAdminControllerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static RuleDirAdminController controller = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		controller = new RuleDirAdminController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testShowOverallViewAction() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenRulesManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.findRuleRepositories()).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.showOverallViewAction();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ruleadmin_ruledir_view");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildAjaxServiceOptions() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenServiceManager manager = mock(PolicyDrivenServiceManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenServiceManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(archServiceFactory.instancePolicyDrivenServiceManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.findPolicyDrivenServiceNames(Mockito.anyString())).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.buildAjaxServiceOptions();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_select_services");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAjaxRepositoryRuleList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);
		Mockito.when(archServiceFactory.instancePolicyDrivenRulesManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.findRuleRepositories()).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.ajaxRuleRepositoryList();
		List<PolicyDrivenServiceListingEntry> serviceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("serviceListing");
		TestCase.assertNotNull("no service listing returned", serviceListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_rule_repository_list");
	}
	
	@Test
	public void testAddRuleRepository() throws Exception {
		
		String testRuleRepName = "rulerep1";
		String testRulePath = "/a/path/to/somerules";
		String comment = "comment here";
		
		@SuppressWarnings("unused")
		PolicyDrivenServiceListingEntry entry = PolicyDrivenServiceListingEntry.instance(testRuleRepName, testRulePath, comment);
		
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);		
		Mockito.when(archServiceFactory.instancePolicyDrivenRulesManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		
		RuleRepositoryForm form = new RuleRepositoryForm();
		form.setComment(comment);
		form.setRuleRepositoryName(testRuleRepName);
		form.setRuleRepositoryPath(testRulePath);
		
		ModelAndView mav = controller.addActionUpdateFromPost(form, Mockito.mock(BindingResult.class));
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"redirect:/ruleadmin/ruledir/view");
		Mockito.verify(manager,  Mockito.atLeastOnce()).addRuleRepository(Mockito.any(PolicyDrivenServiceListingEntry.class));
	}
	
	@Test
	public void testAddRuleRepositoryValidationError() throws Exception {
		
		String testRuleRepName = "rulerep1";
		String testRulePath = "/a/path/to/somerules";
		String comment = "comment here";
		
		@SuppressWarnings("unused")
		PolicyDrivenServiceListingEntry entry = PolicyDrivenServiceListingEntry.instance(testRuleRepName, testRulePath, comment);
		
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyDrivenRulesManager manager = mock(PolicyDrivenRulesManager.class);		
		Mockito.when(archServiceFactory.instancePolicyDrivenRulesManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		
		RuleRepositoryForm form = new RuleRepositoryForm();
		form.setComment(comment);
		form.setRuleRepositoryName(testRuleRepName);
		form.setRuleRepositoryPath(testRulePath);
		
		BindingResult mockResult = Mockito.mock(BindingResult.class);
		Mockito.when(mockResult.hasErrors()).thenReturn(true);
		
		ModelAndView mav = controller.addActionUpdateFromPost(form, mockResult);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ruleadmin_ruledir_add");
		Mockito.verify(manager,  Mockito.atMost(0)).addRuleRepository(Mockito.any(PolicyDrivenServiceListingEntry.class));
	}


}
