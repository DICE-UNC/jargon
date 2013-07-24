package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FederatedSpecificQueryAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * @Ignored for now per Bug [#1594] cannot run specific query cross-zone -
	 *          -853000 which is an iRODS bug
	 * @throws Exception
	 */
	@Test
	public void testExecuteSpecificQueryLSCrossZone() throws Exception {
		

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		SpecificQuery specificQuery = SpecificQuery.instanceWithNoArguments(
				"ls", 0, "");

		SpecificQueryResultSet specificQueryResultSet = queryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						accessObjectFactory.getJargonProperties()
								.getMaxFilesAndDirsQueryMax());
		Assert.assertNotNull("null result set", specificQueryResultSet);
		Assert.assertFalse("no results returned, expected at least ls and lsl",
				specificQueryResultSet.getResults().isEmpty());

	}

}
