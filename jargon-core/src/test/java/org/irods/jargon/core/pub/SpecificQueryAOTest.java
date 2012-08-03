package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.domain.SpecificQuery;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecificQueryAOTest {
	
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private String query = "select * from table";
	private String alias = "neato_query";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}
	
	@Test
	public void testGetSpecficQueryAO() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		SpecificQueryAO queryAO = accessObjectFactory.getSpecificQueryAO(irodsAccount);
		Assert.assertNotNull("queryAO is null", queryAO);
	}
	
	@Test
	public void testAddSpecificQuery() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
			.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
			.getIRODSAccessObjectFactory();
		
		SpecificQuery specificQuery = new SpecificQuery(this.query, this.alias);
		SpecificQueryAO queryAO = accessObjectFactory.getSpecificQueryAO(irodsAccount);
		queryAO.addSpecificQuery(specificQuery);
		
		// just make sure we got here for now
		Assert.assertTrue(true);
	}
	
	@Test
	public void testRemoveSpecificQuery() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
			.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
			.getIRODSAccessObjectFactory();
		
		SpecificQuery specificQuery = new SpecificQuery(this.query, this.alias);
		SpecificQueryAO queryAO = accessObjectFactory.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQuery(specificQuery);
		
		// just make sure we got here for now
		Assert.assertTrue(true);
	}

}
