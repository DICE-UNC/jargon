/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSGenQueryExecutorImplBuiilderQueriesTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSGenQueryExecutorImplBuiilderQueriesTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.close();
	}

	/**
	 * Test of a query with count of collections with a given owner zone
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testExecuteQueryWithCount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_COLL_ID,
				SelectFieldTypes.COUNT).addConditionAsGenQueryField(
				RodsGenQueryEnum.COL_D_OWNER_ZONE,
				QueryConditionOperators.EQUAL, irodsAccount.getZone());

		IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(1);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(query, 0);

		IRODSQueryResultRow row = resultSet.getFirstResult();
		int actualCount = Integer.valueOf(row.getColumn(0));
		// not a great test as the count is indeterminate, really just looking
		// for exec errors and that something is returned
		Assert.assertTrue("count not produced", actualCount > 0);

	}

	/**
	 * Ask for a row count in the query results feature: [#1046] Add total row
	 * count to gen query out
	 * 
	 * @throws Exception
	 */

	@Test
	public final void testExecuteQueryWithRowCount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false,
				true, null);

		builder.addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_COLL_ID,
				SelectFieldTypes.COUNT).addConditionAsGenQueryField(
				RodsGenQueryEnum.COL_D_OWNER_ZONE,
				QueryConditionOperators.EQUAL, irodsAccount.getZone());

		IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(1);

		AbstractIRODSQueryResultSet resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(query, 0);

		Assert.assertTrue("did not get row count",
				resultSet.getTotalRecords() > 0);

	}

	/**
	 * Test a gen query with a numeric value test [#872] numeric gen query with
	 * datasize gives errors in condition
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testExecuteIRODSQueryBuilderQueryWithNumericCondition()
			throws Exception {

		// create 4 data objects with varying sizes so I can query on data size

		long smallSize = 50;
		long largeSize = 200;

		String file1 = "testExecuteIRODSQueryBuilderQueryWithNumericCondition1.txt";
		String file2 = "testExecuteIRODSQueryBuilderQueryWithNumericCondition2.txt";
		String file3 = "testExecuteIRODSQueryBuilderQueryWithNumericCondition3.txt";
		String file4 = "testExecuteIRODSQueryBuilderQueryWithNumericCondition4.txt";

		String subdir = "testExecuteIRODSQueryBuilderQueryWithNumericCondition";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + subdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ subdir);

		IRODSFile targetSubdir = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFile);
		targetSubdir.mkdirs();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);

		// file1 small
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, file1, smallSize);

		File localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file2 small
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file2, smallSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file3 large
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file3, largeSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file4 large
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file4, largeSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.EQUAL,
						targetSubdir.getAbsolutePath())
				.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO,
						smallSize)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO,
						largeSize);

		IRODSGenQueryFromBuilder query = builder
				.exportIRODSQueryFromBuilder(50);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(query, 0);

		Assert.assertEquals("should get all 4 files", 4, resultSet.getResults()
				.size());

		builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.EQUAL,
						targetSubdir.getAbsolutePath())
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_GREATER_THAN, smallSize)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO,
						largeSize);

		query = builder.exportIRODSQueryFromBuilder(50);

		resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);

		Assert.assertEquals("should get 2 files", 2, resultSet.getResults()
				.size());

		builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.EQUAL,
						targetSubdir.getAbsolutePath())
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_GREATER_THAN, largeSize);

		query = builder.exportIRODSQueryFromBuilder(50);

		resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);

		Assert.assertEquals("should get 0 files", 0, resultSet.getResults()
				.size());

	}

	/**
	 * Bug: [#1041] [iROD-Chat:8903] Jargon-core: query builder / SQL operators
	 * This is an iRODS bug being fixed in iRODS 3.2+
	 * 
	 * @throws Exception
	 */
	@Ignore
	public final void testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug1041()
			throws Exception {

		// create 4 data objects with varying sizes so I can query on data size

		long smallSize = 50;
		long largeSize = 200;

		String file1 = "testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug1041.txt";
		String file2 = "testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug1041b.txt";
		String file3 = "testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug1041c.txt";
		String file4 = "testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug10412.txt";

		String subdir = "testExecuteIRODSQueryBuilderQueryWithNumericGreaterThanOrEquaToBug1041";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + subdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ subdir);

		IRODSFile targetSubdir = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFile);
		targetSubdir.mkdirs();

		IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);

		// file1 small
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, file1, smallSize);

		File localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file2 small
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file2, smallSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file3 large
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file3, largeSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// file4 large
		localFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, file4, largeSize);

		localFile = new File(localFileName);
		dto.putOperation(localFile, targetSubdir, null, null);

		// true for upper case causes bug...iRODS fix being created post 3.2
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, true,
				null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
						QueryConditionOperators.EQUAL,
						targetSubdir.getAbsolutePath())
				.addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_DATA_SIZE,
						QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO,
						smallSize);

		IRODSGenQueryFromBuilder query = builder
				.exportIRODSQueryFromBuilder(50);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
				.executeIRODSQuery(query, 0);

		Assert.assertEquals("should get all 4 files", 4, resultSet.getResults()
				.size());

	}

}
