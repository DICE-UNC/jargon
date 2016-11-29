package org.irods.jargon.core.remoteexecute;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAOImpl;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class RemoteExecuteServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RemoteExecuteServiceImplTest";
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
	public final void testInstance() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "host";

		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);

		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);
		Assert.assertNotNull(remoteExecuteService);

	}

	@Test
	public final void testExecuteHello() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.execute();

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
	public final void testExecuteHelloABunchOfTimes() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";

		int nbrTimes = 50;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream;

		for (int i = 0; i < nbrTimes; i++) {
			inputStream = remoteExecuteService.execute();

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

	}

	@Test
	public final void testExecuteHelloWithStreamingOnSmallResultWillNotCauseStreaming()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		String cmd = "hello";
		String args = "";
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 FIXME: bump this up to the next
		// released version
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.executeAndStream();

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
	public final void testExecuteHelloWithPathExpectingToSetPhysPathInArg()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		String cmd = "hello";
		String args = "";
		String host = "";

		String testFileName = "testExecuteHelloWithPath.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
						+ testFileName);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				localFileName,
				targetIrodsCollection,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instanceWhenUsingAbsPathToSetCommandArg(irodsCommands, cmd,
						args, host, targetIrodsFile);

		InputStream inputStream = remoteExecuteService.execute();

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
				result.indexOf("Hello world") > -1);

		Assert.assertTrue(
				"did not successfully execute hello command, missing path info ",
				result.indexOf(testFileName) > -1);

	}

	@Test
	public final void testExecuteHelloWithPathUsingPost241APIToSetCommandLineArg()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 FIXME: bump this up to the next
		// released version
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		String cmd = "hello";
		String args = "";
		String host = "";

		String testFileName = "testExecuteHelloWithPathUsingPost241API.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
						+ testFileName);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				localFileName,
				targetIrodsCollection,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instanceWhenUsingAbsPathToSetCommandArg(irodsCommands, cmd,
						args, host, targetIrodsFile);

		InputStream inputStream = remoteExecuteService.executeAndStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();
		irodsFileSystem.close();

		Assert.assertTrue("did not successfully execute hello command",
				result.indexOf("Hello world") > -1);

		Assert.assertTrue(
				"did not successfully execute hello command, missing path info ",
				result.indexOf(testFileName) > -1);
	}

	@Test
	public final void testExecuteHelloWithPathUsingPost241APIToDetermineHost()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 FIXME: bump this up to the next
		// released version
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			return;
		}

		String cmd = "hello";
		String args = "";
		String host = "";

		String testFileName = "testExecuteHelloWithPathUsingPost241APIToDetermineHost.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

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

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instanceWhenUsingAbsPathToFindExecutionHost(irodsCommands,
						cmd, args, host, targetIrodsFile);

		InputStream inputStream = remoteExecuteService.executeAndStream();

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
				result.indexOf("Hello world") > -1);

		Assert.assertFalse("attempted to return path info",
				result.indexOf(testFileName) > -1);
	}

	@Test
	public final void testExecuteHelloWithHost() throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		String cmd = "hello";
		String args = "";
		String host = "localhost";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.execute();

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

	@Test(expected = JargonException.class)
	public final void testExecuteHelloWithBadHost() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "ImNotAHostWhyAreYouLookingAtMe";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		remoteExecuteService.execute();

	}

	@Test(expected = JargonException.class)
	public final void testExecuteHelloWithBadPath() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";
		String absPath = "/I/am/not/a/path.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instanceWhenUsingAbsPathToSetCommandArg(irodsCommands, cmd,
						args, host, absPath);

		InputStream inputStream = remoteExecuteService.execute();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals(
				"I should not have returned anything as the path was bad", "",
				result.trim());

	}

	@Test
	public final void testExecuteExecStreamTestScriptWithStreamingOnSmallResultWillNotCauseStreaming()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		int testLen = 300;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.executeAndStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals("did not get expected data length", testLen,
				result.length());

	}

	@Test
	public final void testExecuteExecStreamTestScriptWithStreamingOnLargeResultWillCauseStreaming()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		// threshold is 1M, this is 2M
		int testLen = 2097152;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 FIXME: bump this up to the next
		// released version
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		if (props.isAtLeastIrods410()) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.executeAndStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals("did not get expected data length", testLen,
				result.length());

	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testExecuteExecStreamTestScriptWithStreamingOnLargeResultWillCauseStreamingForEirods()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			throw new UnsupportedOperationException();
		}

		// threshold is 1M, this is 2M
		int testLen = 2097152;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (props.isAtLeastIrods410()) {
			irodsFileSystem.closeAndEatExceptions();
			throw new UnsupportedOperationException("match expects");
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		remoteExecuteService.executeAndStream();

	}

	@Test
	public final void testExecuteExecStreamTestScriptWithStreamingOnLargeResultButWillNotCauseStreaming()
			throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		// threshold is 64M
		int testLen = 997152;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 FIXME: bump this up to the next
		// released version
		if (!props
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(RemoteExecuteServiceImpl.STREAMING_API_CUTOFF)) {
			return;
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		AbstractIRODSMidLevelProtocol irodsCommands = collectionAOImpl
				.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host);

		InputStream inputStream = remoteExecuteService.executeAndStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		String result = sb.toString();

		Assert.assertEquals("did not get expected data length", testLen,
				result.length());

	}

}
