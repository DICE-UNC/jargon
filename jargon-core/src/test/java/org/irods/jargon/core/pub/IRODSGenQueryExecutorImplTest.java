/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSGenQueryExecutorImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsGenQueryExecutorImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		String testFilePrefix = "IRODSGenQueryExcecutorImplTest";
		String testFileSuffix = ".txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + collDir, testFilePrefix, testFileSuffix, 2000, 5, 10);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		ImkdirCommand iMkdirCommand = new ImkdirCommand();
		iMkdirCommand.setCollectionName(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(iMkdirCommand);

		// put the files by putting the collection
		IputCommand iputCommand = new IputCommand();
		iputCommand.setForceOverride(true);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setLocalFileName(absPath + collDir);
		iputCommand.setRecursive(true);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIRODSGenQueryExecutorImpl() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		Assert.assertNotNull(irodsGenQueryExecutor);
		irodsSession.closeSession();
	}

	@Test
	public final void testExecuteIRODSQuery() throws Exception {

		String queryString = "select "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName()
				+ " ,"
				+ RodsGenQueryEnum.COL_R_ZONE_NAME.getName()
				+ " where "
				+ RodsGenQueryEnum.COL_R_ZONE_NAME.getName()
				+ " = "
				+ "'"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY)
				+ "'";

		IRODSQuery irodsQuery = IRODSQuery.instance(queryString, 100);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);
		irodsSession.closeSession();

		Assert.assertNotNull(resultSet);
	}

	@Test
	public final void testExecuteIRODSQueryManyTimes() throws Exception {

		int count = 1000;
		String queryString = "select "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName()
				+ " ,"
				+ RodsGenQueryEnum.COL_R_ZONE_NAME.getName()
				+ " where "
				+ RodsGenQueryEnum.COL_R_ZONE_NAME.getName()
				+ " = "
				+ "'"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY)
				+ "'";

		IRODSQuery irodsQuery = IRODSQuery.instance(queryString, 100);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = null;
		for (int i = 0; i < count; i++) {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		}
		irodsSession.closeSession();

		Assert.assertNotNull(resultSet);
	}

	@Test
	public final void testExecuteIRODSQueryForResource() throws Exception {

		String queryString = "select "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName()
				+ " where "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName()
				+ " = "
				+ "'"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY)
				+ "'";

		IRODSQuery irodsQuery = IRODSQuery.instance(queryString, 100);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		irodsSession.closeSession();

		Assert.assertNotNull("null result set", resultSet);
		Assert.assertFalse("empty result set", resultSet.getResults().isEmpty());
		String returnedResourceName = resultSet.getFirstResult().getColumn(0);
		Assert.assertEquals("did not get expected result", testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				returnedResourceName);

	}

	@Test
	public final void testGetMoreResults() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ collDir);

		String queryString = "select "
				+ RodsGenQueryEnum.COL_COLL_NAME.getName() + " ,"
				+ RodsGenQueryEnum.COL_DATA_NAME.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_NAME.getName() + " = '"
				+ targetIrodsCollection + "'";

		IRODSQuery irodsQuery = IRODSQuery.instance(queryString, 1000);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertTrue("did not get expected continuation",
				resultSet.isHasMoreRecords());

		// now requery and get a new result set

		resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);

		irodsSession.closeSession();
		Assert.assertNotNull("result set was null", resultSet);
		Assert.assertTrue("did not get expected continuation",
				resultSet.isHasMoreRecords());
		Assert.assertTrue("no results, some expected", resultSet.getResults()
				.size() > 0);
	}

}
