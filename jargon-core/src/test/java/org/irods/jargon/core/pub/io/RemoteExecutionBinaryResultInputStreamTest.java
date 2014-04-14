package org.irods.jargon.core.pub.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAOImpl;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImpl;
import org.irods.jargon.core.remoteexecute.RemoteExecutionService;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class RemoteExecutionBinaryResultInputStreamTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RemoteExecutionBinaryResultInputStreamTest";
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

	@Test
	public void testRead() throws Exception {

		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		// threshold is 1M, this is 2M
		int testLen = 1597152;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

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

		if (props.isEirods()) {
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

		// just do 99 reads to see if everything is cool...no way to read the
		// whole thing byte by byte
		int totalRead = 0;
		while ((inputStream.read()) > -1 && totalRead < 100) {
			totalRead++;
		}

		inputStream.close();
		irodsFileSystem.close();

		Assert.assertEquals("did not get expected data length", 100, totalRead);
	}

	@Test
	public void testSkip() throws Exception {
		if (!testingPropertiesHelper.isTestRemoteExecStream(testingProperties)) {
			return;
		}

		// threshold is 1M, this is 2M
		int testLen = 2000000;
		int skipLen = 1500000;

		String cmd = "test_execstream.py";
		String args = String.valueOf(testLen);
		String host = "";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

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

		if (props.isEirods()) {
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
		inputStream.skip(skipLen);
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

		Assert.assertEquals("did not get expected data length", testLen
				- skipLen, result.length());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testMark() throws Exception {
		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);
		RemoteExecutionBinaryResultInputStream bis = new RemoteExecutionBinaryResultInputStream(
				irodsCommands, 1);
		bis.mark(100);
		bis.close();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReset() throws Exception {
		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);
		RemoteExecutionBinaryResultInputStream bis = new RemoteExecutionBinaryResultInputStream(
				irodsCommands, 1);
		bis.reset();
		bis.close();
	}

	@Test
	public void testMarkSupported() throws Exception {
		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);
		RemoteExecutionBinaryResultInputStream bis = new RemoteExecutionBinaryResultInputStream(
				irodsCommands, 1);
		bis.close();
		Assert.assertFalse("mark should not be supported", bis.markSupported());
	}

	@Test
	public void testRemoteExecutionBinaryResultInputStream() throws Exception {

		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);
		RemoteExecutionBinaryResultInputStream bis = new RemoteExecutionBinaryResultInputStream(
				irodsCommands, 1);
		bis.close();

		Assert.assertEquals("did not set file descriptor", 1,
				bis.getFileDescriptor());

	}

	@SuppressWarnings("resource")
	@Test(expected = IllegalArgumentException.class)
	public void testRemoteExecutionBinaryResultInputStreamNullCommands()
			throws Exception {

		AbstractIRODSMidLevelProtocol irodsCommands = null;
		new RemoteExecutionBinaryResultInputStream(irodsCommands, 1);

	}

	@SuppressWarnings("resource")
	@Test(expected = IllegalArgumentException.class)
	public void testRemoteExecutionBinaryResultInputStreamZeroDescriptor()
			throws Exception {

		AbstractIRODSMidLevelProtocol irodsCommands = Mockito
				.mock(IRODSMidLevelProtocol.class);
		new RemoteExecutionBinaryResultInputStream(irodsCommands, 0);

	}

}
