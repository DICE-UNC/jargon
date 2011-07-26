package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.SimpleQuery;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SimpleQueryExecutorAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "SimpleQueryExecutorAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";
	public static IRODSAccount irodsAccount;
	public static IRODSFileSystem irodsFileSystem;

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
		irodsFileSystem = IRODSFileSystem.instance();
		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testInstance() throws Exception {

		SimpleQueryExecutorAO simpleQueryExecutorAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getSimpleQueryExecutorAO(
						irodsAccount);
		Assert.assertNotNull("Null simpleQueryExecutor returned",
				simpleQueryExecutorAO);
	}

	@Ignore
	// FIXME: failing
	public void testRescQueryNoArgs() throws Exception {

		String querySQL = "select resc_name from R_RESC_MAIN";
		SimpleQueryExecutorAO simpleQueryExecutorAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getSimpleQueryExecutorAO(
						irodsAccount);

		SimpleQuery simpleQuery = SimpleQuery.instanceWithNoArguments(querySQL,
				0);
		IRODSQueryResultSetInterface resultSet = simpleQueryExecutorAO
				.executeSimpleQuery(simpleQuery);
		Assert.assertNotNull("got a null result et from the query", resultSet);

	}

	@Test
	public void testRescQueryOneArgMultipleValsInSelect() throws Exception {

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		String querySQL = "select R_RESC_GROUP.resc_group_name, R_RESC_GROUP.resc_id, resc_name, R_RESC_GROUP.create_ts, R_RESC_GROUP.modify_ts from R_RESC_MAIN, R_RESC_GROUP where R_RESC_MAIN.resc_id = R_RESC_GROUP.resc_id and resc_group_name=?";
		String resourceGroup = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY);

		SimpleQueryExecutorAO simpleQueryExecutorAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getSimpleQueryExecutorAO(
						irodsAccount);

		SimpleQuery simpleQuery = SimpleQuery.instanceWithOneArgument(querySQL,
				resourceGroup, 0);
		IRODSQueryResultSetInterface resultSet = simpleQueryExecutorAO
				.executeSimpleQuery(simpleQuery);
		Assert.assertNotNull("got a null result et from the query", resultSet);
		Assert.assertEquals("did not set the column names", 5, resultSet
				.getColumnNames().size());
		Assert.assertEquals("did not get the one result row", 1, resultSet
				.getResults().size());
		IRODSQueryResultRow testResultRow = resultSet.getResults().get(0);
		testResultRow.getQueryResultColumns().size();

	}

}
