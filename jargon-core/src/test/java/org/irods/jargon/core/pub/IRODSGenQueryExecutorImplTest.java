/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
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
	private static IRODSFileSystem irodsFileSystem = null;

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

		irodsFileSystem = IRODSFileSystem.instance();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.close();
	}

	@Test
	public final void testIRODSGenQueryExecutorImpl() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		Assert.assertNotNull(irodsGenQueryExecutor);
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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertNotNull(resultSet);
	}

	@Test(expected=IllegalArgumentException.class)
	public final void testExecuteIRODSQueryNullQuery() throws Exception {

		IRODSGenQuery irodsQuery = null;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);

	}

	@Test
	public final void testExecuteIRODSQueryWithPagingSupplySameZone() throws Exception {

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQueryWithPagingInZone(irodsQuery, 0, testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

		Assert.assertNotNull(resultSet);
	}
	
	@Test
	public final void testExecuteIRODSQuerySupplySameZone() throws Exception {

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQueryInZone(irodsQuery, 0, testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

		Assert.assertNotNull(resultSet);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testExecuteIRODSQuerySupplySameZoneNegativeContinuation() throws Exception {

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		irodsGenQueryExecutor
				.executeIRODSQueryInZone(irodsQuery, -1, testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testExecuteIRODSQueryAndCloseSupplySameZoneNegativeContinuation() throws Exception {

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		irodsGenQueryExecutor
				.executeIRODSQueryAndCloseResultInZone(irodsQuery, -1, testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void testExecuteIRODSQueryAndCloseSupplySameZoneNullQuery() throws Exception {

		IRODSGenQuery irodsQuery = null;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		irodsGenQueryExecutor
				.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));

	}


	@Test
	public final void testExecuteIRODSQuerySupplyBlankZone() throws Exception {

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQueryInZone(irodsQuery, 0, "");

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = null;
		for (int i = 0; i < count; i++) {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		}

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertNotNull("null result set", resultSet);
		Assert.assertFalse("empty result set", resultSet.getResults().isEmpty());
		String returnedResourceName = resultSet.getFirstResult().getColumn(0);
		Assert.assertEquals("did not get expected result", testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				returnedResourceName);

	}

	@Test
	public final void testExecuteIRODSQueryForResourceUsingNotLike()
			throws Exception {

		String queryString = "select "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName() + " where "
				+ RodsGenQueryEnum.COL_R_RESC_NAME.getName() + " NOT LIKE "
				+ "'joebobnotaresource'";

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertNotNull("null result set", resultSet);
		Assert.assertFalse("empty result set", resultSet.getResults().isEmpty());
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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 1000);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertTrue("did not get expected continuation",
				resultSet.isHasMoreRecords());

		// now requery and get a new result set

		resultSet = irodsGenQueryExecutor.getMoreResults(resultSet);

		Assert.assertNotNull("result set was null", resultSet);
		Assert.assertTrue("did not get expected continuation",
				resultSet.isHasMoreRecords());
		Assert.assertTrue("no results, some expected", resultSet.getResults()
				.size() > 0);
	}

	@Test
	public final void testQueryHasContinuationCloseItBeforeFinished()
			throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ collDir);

		String queryString = "select "
				+ RodsGenQueryEnum.COL_COLL_NAME.getName() + " ,"
				+ RodsGenQueryEnum.COL_DATA_NAME.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_NAME.getName() + " = '"
				+ targetIrodsCollection + "'";

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 1000);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		Assert.assertTrue("did not get expected continuation",
				resultSet.isHasMoreRecords());

		// now close
		irodsGenQueryExecutor.closeResults(resultSet);

		// no error considered success
		Assert.assertTrue(true);
	}

	/*
	 * [#125] 80600 sql error
	 */
	@Test
	public void testFetchInfoManyTimes() throws Exception {
		String testDirPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory iff = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		int nbrTimes = 100;

		for (int i = 0; i < nbrTimes; i++) {
			// get connection and file
			IRODSFile f = null;
			try {

				f = iff.instanceIRODSFile(testDirPath);
			} catch (Exception e) {
				Assert.fail(e.getLocalizedMessage());
			}

			if (!f.exists()) {
				Assert.fail("This directory should exist");
			}
			if (f.isDirectory()) {

				StringBuilder q = new StringBuilder();
				q.append("select ");
				q.append(RodsGenQueryEnum.COL_COLL_ID.getName()).append(", ");
				q.append(RodsGenQueryEnum.COL_COLL_NAME.getName()).append(", ");
				q.append(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
				q.append(" where ");
				q.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
				q.append(" = '").append(testDirPath).append("'");

				IRODSGenQuery irodsQuery;
				IRODSAccessObjectFactory aof = irodsFileSystem
						.getIRODSAccessObjectFactory();
				irodsQuery = IRODSGenQuery.instance(q.toString(), 1);

				// execute query
				IRODSGenQueryExecutor irodsGenQueryExecutor = aof
						.getIRODSGenQueryExecutor(irodsAccount);
				IRODSQueryResultSet resultSet = irodsGenQueryExecutor
						.executeIRODSQueryAndCloseResult(irodsQuery, 0);

				// set the file info object from the query result
				IRODSQueryResultRow r = null;
				r = resultSet.getFirstResult();
				r.getColumn(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
				r.getColumn(RodsGenQueryEnum.COL_COLL_NAME.getName());

			} else {
				Assert.fail("This is a directory, not a file.");
			}
		}
	}

	/*
	 * [#126] every call to r.getColumn( X ) returns the name of the file as a
	 * string
	 */
	@Test
	public void testColQueryThenAccessColByName() throws Exception {
		String testDirPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String testFileName = "testColQueryThenAccessColByName.jpg";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		// now put the file
		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		dataObjectAO.putLocalDataObjectToIRODS(localFile, destFile, true, null,
				null);

		// build query
		StringBuilder q = new StringBuilder();
		q.append("select ");
		q.append(RodsGenQueryEnum.COL_DATA_NAME.getName()).append(",");
		q.append(RodsGenQueryEnum.COL_D_DATA_ID.getName()).append(",");
		q.append(RodsGenQueryEnum.COL_D_MODIFY_TIME.getName()).append(",");
		q.append(RodsGenQueryEnum.COL_DATA_SIZE.getName());
		q.append(" where ");
		q.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		q.append(" = '").append(testDirPath).append("'");
		q.append(" and ");
		q.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		q.append(" = '").append(testFileName).append("'");

		System.out.println("fetchDataInfo query: " + q.toString());

		IRODSGenQuery irodsQuery;
		irodsQuery = IRODSGenQuery.instance(q.toString(), 1);

		// execute query
		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		IRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(irodsQuery, 0);

		// set the file info object from the query result
		IRODSQueryResultRow r = resultSet.getFirstResult();
		String modified = r.getColumn(RodsGenQueryEnum.COL_D_MODIFY_TIME
				.getName());
		Integer.parseInt(modified); // millisecond timestamp
		String size = r.getColumn(RodsGenQueryEnum.COL_DATA_SIZE.getName());
		Integer.parseInt(size);
		irodsGenQueryExecutor.closeResults(resultSet);

		Assert.assertEquals("did not find modified where expected", modified,
				r.getColumn(2));
		Assert.assertEquals("did not find size where expected", size,
				r.getColumn(3));

	}

}
