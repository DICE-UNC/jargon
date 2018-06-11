package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests of various types of gen query
 * 
 * @author conwaymc
 *
 */
public class GenQueryFunctionalTests {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "GenQueryFunctionalTests";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/*
	 * query for file names in a parent dir where file name in a list
	 */
	@Test
	public void testQueryIn() throws Exception {
		String subdirPrefix = "testQueryIn";
		String fileName = "file";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.deleteWithForceOption();
		irodsFile.mkdir();
		String myTarget;

		// put 5 files into the collection

		myTarget = targetIrodsCollection + "/" + fileName + "a";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "b";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "c";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "d";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "e";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		/*
		 * create a query for file a,b,d
		 */

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		List<String> list = new ArrayList<String>();
		list.add(fileName + "a");
		list.add(fileName + "b");
		list.add(fileName + "d");

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addConditionAsMultiValueCondition(RodsGenQueryEnum.COL_DATA_NAME, QueryConditionOperators.IN, list)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL,
						targetIrodsCollection);

		IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(5000);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);
		Assert.assertNotNull("no result", resultSet);

		boolean founda = false;
		boolean foundb = false;
		boolean foundd = false;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			if (row.getColumn(1).equals(fileName + "a")) {
				founda = true;
			} else if (row.getColumn(1).equals(fileName + "b")) {
				foundb = true;
			} else if (row.getColumn(1).equals(fileName + "d")) {
				foundd = true;
			}
		}

		Assert.assertTrue("didnt find a", founda);
		Assert.assertTrue("didnt find b", foundb);
		Assert.assertTrue("didnt find d", foundd);

	}

	/*
	 * query for file names in a parent dir where file name in a list
	 */
	@Test
	public void testQueryNotIn() throws Exception {
		String subdirPrefix = "testQueryNotIn";
		String fileName = "file";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + subdirPrefix);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.deleteWithForceOption();
		irodsFile.mkdir();
		String myTarget;

		// put 5 files into the collection

		myTarget = targetIrodsCollection + "/" + fileName + "a";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "b";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "c";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "d";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		myTarget = targetIrodsCollection + "/" + fileName + "e";
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
		irodsFile.createNewFile();

		/*
		 * create a query for file a,b,d
		 */

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		List<String> list = new ArrayList<String>();
		list.add(fileName + "a");
		list.add(fileName + "b");
		list.add(fileName + "d");

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addConditionAsMultiValueCondition(RodsGenQueryEnum.COL_DATA_NAME, QueryConditionOperators.NOT_IN, list)
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME, QueryConditionOperators.EQUAL,
						targetIrodsCollection);

		IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(5000);

		IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);
		Assert.assertNotNull("no result", resultSet);

		boolean founda = false;
		boolean foundb = false;
		boolean foundd = false;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			if (row.getColumn(1).equals(fileName + "a")) {
				founda = true;
			} else if (row.getColumn(1).equals(fileName + "b")) {
				foundb = true;
			} else if (row.getColumn(1).equals(fileName + "d")) {
				foundd = true;
			}
		}

		Assert.assertTrue("found a", founda);
		Assert.assertTrue("found b", foundb);
		Assert.assertTrue("found d", foundd);

	}

}
