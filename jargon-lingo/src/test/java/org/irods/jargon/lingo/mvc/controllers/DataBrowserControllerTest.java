package org.irods.jargon.lingo.mvc.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

public class DataBrowserControllerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();

	}

	@Test
	public void testIndexAction() throws Exception {
		DataBrowserController controller = new DataBrowserController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		ModelAndView mav = controller.indexAction();
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"data_browser");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAjaxBrowserDirRequest() throws Exception {
		DataBrowserController controller = new DataBrowserController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		
		CollectionAO collectionAO = mock(CollectionAO.class);
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		
		Mockito.when(collectionAO.listDataObjectsAndCollectionsUnderPath(Mockito.anyString())).thenReturn(entries);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount)).thenReturn(collectionAO);
		
		controller.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		String testDir = "/";
				
		ModelAndView mav = controller.ajaxBrowserDirRequest(testDir);
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"jsonView");
		
		List<CollectionAndDataObjectListingEntry> actualEntries =  (List<CollectionAndDataObjectListingEntry>) mav.getModelMap().get("directoryList");
		TestCase.assertNotNull("no entries recieved from controller with directory listing", actualEntries);
		
	}



}
