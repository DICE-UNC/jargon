/**
 * 
 */
package org.irods.jargon.core.unittest.functionaltest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mikeconway
 * 
 */
public class FileOperationSequences {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileOperationSequences";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Mutli-threaded test of multiple operations for a get with shared access
	 * objects between threads [#1065] [iROD-Chat:9047] Java
	 * ClosedChannelException in Jargon 3.2.1
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMultiThreadSharingOfDataAOBug1065() throws Exception {
		// generate a local scratch file
		String testFileName = "testMultiThreadSharingOfDataAOBug1065.txt";

		int nbrThreads = 6;
		int nbrIterations = 50;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		for (int j = 0; j < nbrIterations; j++) {

			// build the thread pool using the given files

			ExecutorService exec = Executors.newFixedThreadPool(nbrThreads);

			List<Future<DataObject>> futures = new ArrayList<Future<DataObject>>(
					nbrThreads);
			DataObjectAO dataObjectAO = irodsFileSystem
					.getIRODSAccessObjectFactory()
					.getDataObjectAO(irodsAccount);
			DataTransferOperations dto = irodsFileSystem
					.getIRODSAccessObjectFactory().getDataTransferOperations(
							irodsAccount);

			for (int i = 0; i < nbrThreads; i++) {
				String localFileNameForGet = scratchFileUtils
						.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
								+ "/" + i + testFileName);
				futures.add(exec.submit(new MultiThreadSharingOfDataAOBug1065(
						dataObjectAO, dto, destFile.getAbsolutePath(),
						localFileNameForGet)));
			}

			for (Future<DataObject> future : futures) {
				future.get();
			}

			exec.shutdown();
		}

	}

	/**
	 * Share an IRODSRandomAccess file between threads. Currently ignored, as
	 * the originating use case may be suspect. Bug [#1066] Auth Exception on
	 * seek
	 * 
	 * @throws Exception
	 */
	@Ignore
	public void testShareIRODSRandomAccessFileBetweenThreadsBug1066()
			throws Exception {
		String testFileName = "testShareIRODSRandomAccessFileBetweenThreadsBug1066.txt";

		int nbrThreads = 4;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		ExecutorService exec = Executors.newFixedThreadPool(nbrThreads);

		List<Future<Object>> futures = new ArrayList<Future<Object>>(nbrThreads);

		IRODSRandomAccessFile randomAccessFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSRandomAccessFile(destFile);

		for (int i = 0; i < nbrThreads; i++) {
			futures.add(exec
					.submit(new ShareIRODSRandomAccessFileBetweenThreadsBug1066(
							randomAccessFile, irodsFileSystem)));
		}

		for (Future<Object> future : futures) {
			future.get();
		}

		exec.shutdown();

	}

}

class ShareIRODSRandomAccessFileBetweenThreadsBug1066 implements
		Callable<Object> {

	IRODSRandomAccessFile randomAccessFile;
	IRODSFileSystem irodsFileSystem;

	ShareIRODSRandomAccessFileBetweenThreadsBug1066(
			final IRODSRandomAccessFile randomAccessFile,
			final IRODSFileSystem irodsFileSystem) {
		super();
		this.randomAccessFile = randomAccessFile;
		this.irodsFileSystem = irodsFileSystem;
	}

	@Override
	public Object call() throws Exception {
		randomAccessFile.seek(40, SeekWhenceType.SEEK_CURRENT);
		irodsFileSystem.closeAndEatExceptions();
		return null;
	}

}

class MultiThreadSharingOfDataAOBug1065 implements Callable<DataObject> {

	public MultiThreadSharingOfDataAOBug1065(final DataObjectAO dataObjectAO,
			final DataTransferOperations dataTransferOperations,
			final String sourceAbsolutePath, final String targetAbsolutePath) {
		super();
		this.dataObjectAO = dataObjectAO;
		this.dataTransferOperations = dataTransferOperations;
		this.sourceAbsolutePath = sourceAbsolutePath;
		this.targetAbsolutePath = targetAbsolutePath;
	}

	private DataObjectAO dataObjectAO;
	private DataTransferOperations dataTransferOperations;
	private String sourceAbsolutePath;
	private String targetAbsolutePath;

	@Override
	public DataObject call() throws Exception {
		DataObject obj = dataObjectAO.findByAbsolutePath(sourceAbsolutePath);
		TransferControlBlock tcb = dataTransferOperations
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dataTransferOperations.getOperation(sourceAbsolutePath,
				targetAbsolutePath, "", null, tcb);
		dataTransferOperations.getIRODSAccessObjectFactory()
				.closeSessionAndEatExceptions();
		return obj;
	}

}
