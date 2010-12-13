package org.irods.jargon.arch.mvc.controllers;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.arch.mvc.controllers.forms.SeriesForm;
import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.SeriesManager;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

public class SeriesControllerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static SeriesController controller = null;
	private static IRODSAccount irodsAccount = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
		controller = new SeriesController();
		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
	}
	
	@Test
	public void testShowSeriesInAppView() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		SeriesManager manager = mock(SeriesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instanceSeriesManager((IRODSAccount) Matchers
										.any())).thenReturn(manager);
		
		PolicyDrivenServiceManager serviceManager = mock(PolicyDrivenServiceManager.class);
		List<PolicyDrivenServiceListingEntry> entries = new ArrayList<PolicyDrivenServiceListingEntry>();
		Mockito
				.when(
						archServiceFactory.instancePolicyDrivenServiceManager(irodsAccount)).thenReturn(serviceManager);
		Mockito.when(serviceManager.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE)).thenReturn(entries);

		ModelAndView mav = controller.showSeriesInAppView();
		List<PolicyDrivenServiceListingEntry> policyServiceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyDrivenServiceList");
		Assert.assertNotNull("mav was null from controller", mav);
		Assert.assertNotNull("no service listing found", policyServiceListings);
		Assert.assertEquals("wrong view returned", mav.getViewName(),
				"seriesadmin_seriesdir_view");
	}
	
	@Test
	public void testAjaxSeriesDirContents() throws Exception {
		String serviceDrivenApplicationName = "testapp";
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		SeriesManager manager = mock(SeriesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instanceSeriesManager((IRODSAccount) Matchers
										.any())).thenReturn(manager);
		
		List<PolicyDrivenServiceListingEntry> series = new ArrayList<PolicyDrivenServiceListingEntry>();
		Mockito
				.when(
						archServiceFactory.instanceSeriesManager(irodsAccount)).thenReturn(manager);
		Mockito.when(manager.listSeries(serviceDrivenApplicationName)).thenReturn(series);

		ModelAndView mav = controller.ajaxSeriesDirContents(serviceDrivenApplicationName);
		List<PolicyDrivenServiceListingEntry> policyServiceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("seriesList");
		Assert.assertNotNull("mav was null from controller", mav);
		Assert.assertNotNull("no series listing found", policyServiceListings);
		Assert.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_series_dir_contents");
	}
	
	@Test
	public void testAddSeriesNewAction() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		SeriesManager manager = mock(SeriesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instanceSeriesManager((IRODSAccount) Matchers
										.any())).thenReturn(manager);
		
		PolicyDrivenServiceManager serviceManager = mock(PolicyDrivenServiceManager.class);
		List<PolicyDrivenServiceListingEntry> entries = new ArrayList<PolicyDrivenServiceListingEntry>();
		Mockito
				.when(
						archServiceFactory.instancePolicyDrivenServiceManager(irodsAccount)).thenReturn(serviceManager);
		Mockito.when(serviceManager.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE)).thenReturn(entries);

		ModelAndView mav = controller.addSeriesNewAction();
		List<PolicyDrivenServiceListingEntry> policyServiceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyDrivenServiceList");
		List<PolicyDrivenServiceListingEntry> policyList = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyList");
		Assert.assertNotNull("mav was null from controller", mav);
		Assert.assertNotNull("no service listing found", policyServiceListings);
		Assert.assertEquals("wrong view returned", mav.getViewName(),
				"seriesadmin_series_dir_add");
	}
	
	@Test
	public void ajaxGetPolicyOptionsForService() throws Exception {
		String serviceDrivenApplicationName = "testapp";
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
		
		List<PolicyDrivenServiceListingEntry> series = new ArrayList<PolicyDrivenServiceListingEntry>();
		Mockito
				.when(
						archServiceFactory.instancePolicyManager(irodsAccount)).thenReturn(manager);
		Mockito.when(manager.listAllPolicies()).thenReturn(series);

		ModelAndView mav = controller.ajaxGetPolicyOptionsForService(serviceDrivenApplicationName);
		List<PolicyDrivenServiceListingEntry> policyServiceListings = (List<PolicyDrivenServiceListingEntry>) mav.getModelMap().get("policyList");
		Assert.assertNotNull("mav was null from controller", mav);
		Assert.assertNotNull("no series listing found", policyServiceListings);
		Assert.assertEquals("wrong view returned", mav.getViewName(),
				"seriesadmin_seriesdir_policy_options_for_service");
	}
	
	@Test
	public void testAddSeriesUpdateAction() throws Exception {
		ArchServiceFactory archServiceFactory = mock(ArchServiceFactory.class);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		controller.setArchServiceFactory(archServiceFactory);
		SeriesManager manager = mock(SeriesManager.class);
		Mockito
				.when(
						archServiceFactory
								.instanceSeriesManager((IRODSAccount) Matchers
										.any())).thenReturn(manager);
		
		PolicyDrivenServiceManager serviceManager = mock(PolicyDrivenServiceManager.class);
		List<PolicyDrivenServiceListingEntry> entries = new ArrayList<PolicyDrivenServiceListingEntry>();
		Mockito
				.when(
						archServiceFactory.instancePolicyDrivenServiceManager(irodsAccount)).thenReturn(serviceManager);
		Mockito.when(serviceManager.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE)).thenReturn(entries);
		
		SeriesManager seriesManager = mock(SeriesManager.class);
		Mockito.when(archServiceFactory.instanceSeriesManager(irodsAccount)).thenReturn(seriesManager);
		
		SeriesForm seriesForm = new SeriesForm();
		seriesForm.setBoundPolicyName("policyName");
		seriesForm.setCollectionAbsolutePath("/a/path/here");
		seriesForm.setContainingServiceName("a service name");
		seriesForm.setDescription("a description");
		seriesForm.setName("a name");
		
		BindingResult mockBindingResult = mock(BindingResult.class);
		
		XMLToObjectUnmarshaller marshaller = mock(XMLToObjectUnmarshaller.class);
		controller.addSeriesActionUpdateFromPost(seriesForm, mockBindingResult);
		Mockito.verify(seriesManager).addSeriesToApplication(Matchers.any(Series.class), Matchers.any(XMLToObjectUnmarshaller.class));
	}

}
