package org.irods.jargon.core.remoteexecute;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAOImpl;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class RemoteExecuteServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RemoteExecuteServiceImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "host";
		String absPath = "/an/abs/path";

		IRODSCommands irodsCommands = Mockito.mock(IRODSCommands.class);

		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);
		Assert.assertNotNull(remoteExecuteService);

	}

	@Test
	public final void testExecuteHello() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";
		String absPath = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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
		irodsFileSystem.close();

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());

	}

	@Test
	public final void testExecuteHelloABunchOfTimes() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";
		String absPath = "";

		int nbrTimes = 50;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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
		irodsFileSystem.close();

	}

	// FIXME: currently ignored..potential iRODS bug
	@Ignore
	public final void testExecuteHelloWithStreamingOnSmallResultWillNotCauseStreaming()
			throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";
		String absPath = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();
		
		
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());

	}

	@Test
	public final void testExecuteHelloWithPath() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testFileName = "testExecuteHelloWithPath.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(localFileName);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, targetIrodsFile);

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
		irodsFileSystem.close();

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());

	}

	@Test
	public final void testExecuteHelloWithHost() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "localhost";
		String absPath = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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
		irodsFileSystem.close();

		Assert.assertEquals("did not successfully execute hello command",
				"Hello world  from irods".trim(), result.trim());

	}

	@Test(expected = JargonException.class)
	public final void testExecuteHelloWithBadHost() throws Exception {

		String cmd = "hello";
		String args = "";
		String host = "ImNotAHostWhyAreYouLookingAtMe";
		String absPath = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		CollectionAOImpl collectionAOImpl = (CollectionAOImpl) collectionAO;
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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
		IRODSCommands irodsCommands = collectionAOImpl.getIRODSProtocol();
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(irodsCommands, cmd, args, host, absPath);

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
		irodsFileSystem.close();

		Assert.assertEquals(
				"I should not have returned anything as the path was bad", "",
				result.trim());

	}

}
