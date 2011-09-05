package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testParallelFilePutThenGet() throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGet.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetRetrieved.txt";
		long testFileLength = 4294967296L;
		
		
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

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setUseTransferThreadsPool(false);
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
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		irodsFileSystem.close();
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
				testFileLength);
	}

	@Test
	public final void testParallelFilePutThenGetUsingExecutor()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testParallelFilePutThenGetUsingExecutor.txt";
		String testRetrievedFileName = "testParallelFilePutThenGetUsingExecutor.txt";
		long testFileLength = 400000 * 1024;

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

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		irodsFileSystem.close();
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

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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

		irodsFileSystem.close();

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
		int numberOfThreads = 2;

		for (int i = 0; i < numberOfClients; i++) {
			String absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			FileGenerator.generateFileOfFixedLengthGivenName(absPath, i
					+ testFileName, testFileLength);
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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

		irodsFileSystem.close();

		for (int i = 0; i < numberOfClients; i++) {
			assertionHelper.assertLocalScratchFileLengthEquals(
					IRODS_TEST_SUBDIR_PATH + "/" + i + testRetrievedFileName,
					testFileLength);
		}
	}

	@Test
	public final void testParallelFilePutThenGetExtraFileExtension()
			throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "BR-0001-Normal.cov.tdf";
		String testRetrievedFileName = "BR-0001-Normal.cov.Retrieved.tdf";
		long testFileLength = 1718730902;

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

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		irodsFileSystem.close();
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
				testFileLength);
	}

	/**
	 * Was not working locally on original install of IRODS, may be an error due
	 * to running on VirtualBox (ports). This test is running when local to the
	 * server
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGet() throws Exception {
		// make up a test file that triggers parallel transfer
		String testFileName = "testGet.txt";
		String testRetrievedFileName = "testGetRetrieved.txt";
		long testFileLength = 2000000 * 1024;

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
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		System.out.println("closing irodsfilesystem for put");

		File retrievedLocalFile = new File(absPath + "/"
				+ testRetrievedFileName);
		dataTransferOperationsAO.getOperation(destFile, retrievedLocalFile,
				null, null);

		irodsFileSystem.close();
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + "/" + testRetrievedFileName,
				testFileLength);
	}

	class PutThenGetTester implements Callable<Object> {

		private String testFileName;
		private String localFileName;
		private String retrievedFileName;
		private IRODSAccount irodsAccount;
		private IRODSFileSystem irodsFileSystem;

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
