package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelTransferOperationsTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ParallelTransferOperationsTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		scratchFileUtils.createDirectoryUnderScratch(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();

		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void setUp() throws Exception {
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties();
		settableJargonProperties.setLongTransferRestart(false);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);

	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * test runs 1 transfer by default, can be tweaked (nbrTimes) to do this
	 * repeatedly
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testParallelFilePutThenGet() throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGet.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetRetrieved.txt";
		long testFileLength = 1 * 1024 * 1024 * 2014;

		int nbrTimes = 1;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(false);
		jargonProperties.setLongTransferRestart(false);
		jargonProperties.setComputeAndVerifyChecksumAfterTransfer(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);

		for (int i = 0; i < nbrTimes; i++) {
			destFile.deleteWithForceOption();

			dataTransferOperationsAO.putOperation(localSourceFile, destFile,
					null, null);

			irodsFileFactory = irodsFileSystem
					.getIRODSFileFactory(irodsAccount);
			destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);

			File retrievedLocalFile = new File(absPath + "/"
					+ testRetrievedFileName);
			retrievedLocalFile.delete();
			dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
					null, null);

			assertionHelper.assertLocalScratchFileLengthEquals(
					IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
					testFileLength);
		}
	}

	@Test
	public final void testParallelFilePutWithRestartNoDefaultManager()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutWithRestartNoDefaultManager.txt";
		long testFileLength = ConnectionConstants.MIN_FILE_RESTART_SIZE * 60;
		SettableJargonProperties props = (SettableJargonProperties) irodsFileSystem
				.getJargonProperties();
		props.setMaxParallelThreads(4);
		props.setLongTransferRestart(true);
		props.setComputeAndVerifyChecksumAfterTransfer(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);

		dataTransferOperationsAO.putOperation(localSourceFile, destFile, null,
				null);

	}

	@Test
	public final void testParallelFilePutThenenGetUsingExecutor()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGetUsingExecutor.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetUsingExecutor.txt";
		long testFileLength = 40000 * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(true);
		jargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);

		jargonProperties.setTransferThreadPoolTimeoutMillis(30000);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);

		dataTransferOperationsAO.putOperation(localSourceFile, destFile, null,
				null);

		System.out.println("closing irodsfilesystem for put");
		irodsFileSystem.close();

		System.out.println("new file system for get");
		irodsFileSystem = IRODSFileSystem.instance();
		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File retrievedLocalFile = new File(absPath + "/"
				+ testRetrievedFileName);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, tcb);

		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
				testFileLength);
	}

	@Ignore
	public final void testParallelFilePutThenGetUsingExecutorMultipleClients()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGetUsingExecutorMultipleClients.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetUsingExecutorRetrieved.txt";
		long testFileLength = 400000 * 1024;
		int numberOfClients = 3;

		for (int i = 0; i < numberOfClients; i++) {
			String absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			FileGenerator.generateFileOfFixedLengthGivenName(absPath, i
					+ testFileName, testFileLength);
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(true);
		jargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);

		jargonProperties.setTransferThreadPoolTimeoutMillis(30000);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		ExecutorService executorService = Executors
				.newFixedThreadPool(numberOfClients);
		final List<PutThenGetTester> testers = new ArrayList<PutThenGetTester>();

		for (int i = 0; i < numberOfClients; i++) {
			String absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			PutThenGetTester putThenGetTester = new PutThenGetTester(i
					+ testFileName, absPath + i + testFileName, absPath + i
					+ testRetrievedFileName, irodsAccount, irodsFileSystem);
			testers.add(putThenGetTester);
		}

		executorService.invokeAll(testers);
		executorService.shutdown();

		for (int i = 0; i < numberOfClients; i++) {
			assertionHelper.assertLocalScratchFileLengthEquals(
					IRODS_TEST_SUBDIR_PATH + "/" + i + testRetrievedFileName,
					testFileLength);
		}
	}

	@Ignore
	public final void testParallelFilePutThenGetUsingExecutorMultipleClientsMoreClientsLessPool()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGetUsingExecutorMultipleClientsMoreClientsLessPool.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetUsingExecutorMultipleClientsMoreClientsLessPoolRetrieved.txt";
		long testFileLength = 400000 * 1024;
		int numberOfClients = 3;
		for (int i = 0; i < numberOfClients; i++) {
			String absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			FileGenerator.generateFileOfFixedLengthGivenName(absPath, i
					+ testFileName, testFileLength);
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(true);
		jargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);

		jargonProperties.setTransferThreadPoolTimeoutMillis(30000);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);

		ExecutorService executorService = Executors
				.newFixedThreadPool(numberOfClients);
		final List<PutThenGetTester> testers = new ArrayList<PutThenGetTester>();

		for (int i = 0; i < numberOfClients; i++) {
			String absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			PutThenGetTester putThenGetTester = new PutThenGetTester(i
					+ testFileName, absPath + i + testFileName, absPath + i
					+ testRetrievedFileName, irodsAccount, irodsFileSystem);
			testers.add(putThenGetTester);
		}

		executorService.invokeAll(testers);

		for (int i = 0; i < numberOfClients; i++) {
			assertionHelper.assertLocalScratchFileLengthEquals(
					IRODS_TEST_SUBDIR_PATH + "/" + i + testRetrievedFileName,
					testFileLength);
		}
	}

	/**
	 * Manipulate the jargon properties to set connection restarting with an
	 * unnaturally short reconnect time to make sure it reconnects
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testParallelFilePutWithConnectionRestarting()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutWithConnectionRestarting.tdf";
		String testRetrievedFileName = "testParallelFilePutWithConnectionRestartingRetrieved.tdf";
		long testFileLength = 100 * 1024 * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						testFileLength);
		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties jargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		jargonProperties.setReconnect(true);
		jargonProperties.setReconnectTimeInMillis(60000);
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localSourceFile = new File(localFileName);

		dataTransferOperationsAO.putOperation(localSourceFile, destFile, null,
				null);

		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
		dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File retrievedLocalFile = new File(absPath + "/"
				+ testRetrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
				testFileLength);
	}

	class PutThenGetTester implements Callable<Object> {

		private final String testFileName;
		private final String localFileName;
		private final String retrievedFileName;
		private final IRODSAccount irodsAccount;
		private final IRODSFileSystem irodsFileSystem;

		public PutThenGetTester(final String testFileName,
				final String localFileName, final String retrievedFileName,
				final IRODSAccount irodsAccount,
				final IRODSFileSystem irodsFileSystem) {
			this.testFileName = testFileName;
			this.irodsAccount = irodsAccount;
			this.irodsFileSystem = irodsFileSystem;
			this.localFileName = localFileName;
			this.retrievedFileName = retrievedFileName;
		}

		@Override
		public Object call() throws Exception {
			String targetIrodsFile = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
									+ testFileName);

			IRODSFileFactory irodsFileFactory = irodsFileSystem
					.getIRODSFileFactory(irodsAccount);
			IRODSFile destFile = irodsFileFactory
					.instanceIRODSFile(targetIrodsFile);
			DataTransferOperations dataTransferOperationsAO = irodsFileSystem
					.getIRODSAccessObjectFactory().getDataTransferOperations(
							irodsAccount);

			File localSourceFile = new File(localFileName);

			dataTransferOperationsAO.putOperation(localSourceFile, destFile,
					null, null);

			irodsFileFactory = irodsFileSystem
					.getIRODSFileFactory(irodsAccount);
			destFile = irodsFileFactory.instanceIRODSFile(targetIrodsFile);
			dataTransferOperationsAO = irodsFileSystem
					.getIRODSAccessObjectFactory().getDataTransferOperations(
							irodsAccount);

			File retrievedLocalFile = new File(retrievedFileName);
			dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
					null, null);
			return "ok";
		}

	}
}
