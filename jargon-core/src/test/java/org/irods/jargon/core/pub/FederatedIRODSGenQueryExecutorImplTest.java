/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Testing for gen query across zones, requires a two-zone setup per the
 * fedTestSetup.txt file in the test-scripts directory.
 * <p>
 * If the testing property for federated zone testing is not configured, these
 * tests will be bypassed
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FederatedIRODSGenQueryExecutorImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FederatedIRODSGenQueryExecutorImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		String testFilePrefix = "FederatedIRODSGenQueryExecutorImplTest";
		String testFileSuffix = ".txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForFederatedZoneFromTestProperties(testingProperties);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH + '/' + collDir, testFilePrefix,
				testFileSuffix, 100, 5, 10);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH);
		IRODSFile putSubdir = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
		putSubdir.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = irodsFileSystem.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dataTransferOperations.putOperation(absPath + collDir, targetIrodsCollection, "", null, tcb);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.close();
	}

	@Test
	public final void testExecuteBasicIRODSQueryOnFederatedZoneAndClose() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		String queryString = "select " + RodsGenQueryEnum.COL_R_RESC_NAME.getName() + " ,"
				+ RodsGenQueryEnum.COL_R_ZONE_NAME.getName();

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery,
				0, testingProperties.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY));

		Assert.assertNotNull(resultSet);
		Assert.assertTrue("no results in result set, query failed", resultSet.getResults().size() > 0);
	}

	@Test
	public void testExecuteCollectionQueryAcrossZone() throws Exception {

		if (!testingPropertiesHelper.isTestFederatedZone(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromFederatedZoneReadTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH);

		String queryString = "select " + RodsGenQueryEnum.COL_COLL_NAME.getName() + " WHERE "
				+ RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName() + " = '" + targetIrodsCollection + "'";

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQueryInZone(irodsQuery, 0,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_FEDERATED_ZONE_KEY));
		irodsGenQueryExecutor.closeResults(resultSet);

		Assert.assertNotNull(resultSet);
		Assert.assertTrue("no results in result set, query failed", resultSet.getResults().size() > 0);

	}

}
