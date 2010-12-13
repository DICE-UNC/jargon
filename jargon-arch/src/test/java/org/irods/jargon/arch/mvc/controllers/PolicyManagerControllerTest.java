package org.irods.jargon.arch.mvc.controllers;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.arch.mvc.controllers.forms.PolicyRepositoryForm;
import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

public class PolicyManagerControllerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static PolicyManagerController controller = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		controller = new PolicyManagerController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		
	}
	
	@Test
	public void testShowOverallViewAction() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);
		Mockito
				.when(
						archServiceFactory
								.instancePolicyManager((IRODSAccount) Matchers
										.any())).thenReturn(manager);
		Mockito.when(manager.listPolicyRepositories()).thenReturn(
				new ArrayList<PolicyDrivenServiceListingEntry>());

		ModelAndView mav = controller.showOverallViewAction();
		Assert.assertNotNull("mav was null from controller", mav);
		Assert.assertEquals("wrong view returned", mav.getViewName(),
				"policyadmin_policydir_view");
	}
	
	@Test
	public void ajaxPolicyRepositoryList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);
		Mockito.when(archServiceFactory.instancePolicyManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.listPolicyRepositories()).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.ajaxPolicyRepositoryList();
		List<PolicyDrivenServiceListingEntry> policyRepositoryListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyRepositoryListing");
		TestCase.assertNotNull("no service listing returned", policyRepositoryListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_policy_repository_list");
	}
	
	@Test
	public void ajaxPolicyList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);
		Mockito.when(archServiceFactory.instancePolicyManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.listPoliciesInPolicyRepository(Matchers.anyString())).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.ajaxPolicyList("policy");
		List<PolicyDrivenServiceListingEntry> policyRepositoryListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyListing");
		TestCase.assertNotNull("no service listing returned", policyRepositoryListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_policy_list");
		Mockito.verify(manager).listPoliciesInPolicyRepository("policy");
	}
	
	@Test
	public void testAddPolicyRepository() throws Exception {
		
		String testRepName = "policyrep1";
		String testPath = "/a/path/to/somepolicy";
		String comment = "comment here";
		
		@SuppressWarnings("unused")
		PolicyDrivenServiceListingEntry entry = PolicyDrivenServiceListingEntry.instance(testRepName, testPath, comment);
		
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);		
		Mockito.when(archServiceFactory.instancePolicyManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		
		PolicyRepositoryForm form = new PolicyRepositoryForm();
		form.setComment(comment);
		form.setRepositoryName(testRepName);
		form.setRepositoryPath(testPath);
		
		ModelAndView mav = controller.addActionUpdateFromPost(form, Mockito.mock(BindingResult.class));
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"redirect:/policyadmin/policydir/view");
		Mockito.verify(manager,  Mockito.atLeastOnce()).addPolicyRepository(Mockito.any(PolicyDrivenServiceListingEntry.class));
	}
	
	@Test
	public void testAddPolicySetup() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);
		Mockito.when(archServiceFactory.instancePolicyManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		
		ModelAndView mav = controller.addOrUploadPolicy();
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"policyadmin_policy_add");
	}
	
	
	@Test
	public void testShowPolicyList() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		PolicyManager manager = mock(PolicyManager.class);
		Mockito.when(archServiceFactory.instancePolicyManager((IRODSAccount) Mockito.any())).thenReturn(manager);
		Mockito.when(manager.listPolicyRepositories()).thenReturn(new ArrayList<PolicyDrivenServiceListingEntry>());
		
		ModelAndView mav = controller.showPolicyList();
		List<PolicyDrivenServiceListingEntry> policyRepositoryListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyRepositoryListing");
		TestCase.assertNotNull("no policy listing returned", policyRepositoryListings);
		TestCase.assertNotNull("mav was null from controller", mav);
		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"policyadmin_policy_list_view");
	}
	
}
