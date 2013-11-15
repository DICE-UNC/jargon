package org.irods.jargon.core.pub;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImpl;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RemoteExecutionOfCommandsAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RemoteExecutionOfCommandsAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testRemoteExecutionOfCommandsAOImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);
		Assert.assertNotNull("no remote commands executer found",
				remoteExecutionOfCommandsAO);
	}

	@Test
	public final void testExecuteARemoteCommandAndGetStreamGivingCommandNameAndArgs()
			throws Exception {

		String cmd = "hello";
		String args = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);

		InputStream inputStream = remoteExecutionOfCommandsAO
				.executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(cmd,
						args);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());
	}

	@Test
	public final void testExecuteARemoteCommandAndGetStreamGivingCommandNameAndArgsAndHost()
			throws Exception {
		String cmd = "hello";
		String args = "";
		String host = "localhost";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);

		InputStream inputStream = remoteExecutionOfCommandsAO
				.executeARemoteCommandAndGetStreamGivingCommandNameAndArgsAndHost(
						cmd, args, host);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());
	}

	@Test
	public final void testExecuteARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost()
			throws Exception {
		String cmd = "hello";
		String args = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testFileName = "testExecuteARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(
				localFileName,
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);

		InputStream inputStream = remoteExecutionOfCommandsAO
				.executeARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost(
						cmd, args, targetIrodsFile);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertTrue("did not successfully execute hello command",
				"Hello world  from irods".trim().equals(result.trim()));
		Assert.assertFalse(
				"should not have responded with file name in response",
				result.indexOf(testFileName) > -1);
	}

	@Test
	public final void testExecuteARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToAddPhysPathToCommandArgs()
			throws Exception {
		String cmd = "hello";
		String args = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testFileName = "testExecuteARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToAddPhysPathToCommandArgs.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				localFileName,
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);

		InputStream inputStream = remoteExecutionOfCommandsAO
				.executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(
						cmd, args, targetIrodsFile);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertFalse("did not successfully execute hello command",
				"Hello world  from irods".trim().equals(result.trim()));
		Assert.assertTrue("should have responded with file name in response",
				result.indexOf(testFileName) > -1);
	}

	@Test
	public final void testExecuteARemoteCommandAndGetStreamGivingCommandNameAndArgsTriggerLargeStream()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		int testLen = 2097152;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);

		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (props.isEirods()) {
			return;
		}

		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getRemoteExecutionOfCommandsAO(
						irodsAccount);

		InputStream inputStream = remoteExecutionOfCommandsAO
				.executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(cmd,
						args);

		Assert.assertTrue("should have gotten a SequenceInputStream",
				(inputStream instanceof SequenceInputStream));

		inputStream.close();
	}

}
