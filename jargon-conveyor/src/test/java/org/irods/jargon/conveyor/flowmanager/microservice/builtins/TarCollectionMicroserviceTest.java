package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.flowmanager.microservice.ContainerEnvironment;
import org.irods.jargon.conveyor.flowmanager.microservice.InvocationContext;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TarCollectionMicroserviceTest {

	private static Properties testingProperties = new Properties();
	private static JargonProperties jargonOriginalProperties = null;
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TarCollectionMicroserviceTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		jargonOriginalProperties = settableJargonProperties;
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		// be sure that normal parallel stuff is set up
		irodsFileSystem.getIrodsSession().setJargonProperties(
				jargonOriginalProperties);
	}

	@Test
	public void testUntarCollectionInIRODS() throws Exception {

		String host = "test";
		String zone = "zone";
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);

		DefaultTransferControlBlock.instance();

		String rootCollection = "testUntarCollectionInIRODS";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, localCollectionAbsolutePath,
				"blah", "", 0, 0, 0, 0, 0, TransferState.OVERALL_INITIATION,
				"host", "zone");

		String tarParentCollection = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File tarFile = new File(tarParentCollection);
		tarFile = new File(tarFile.getParentFile(), "contents.tar");

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 200 * 1024);

		Microservice tarCollectionMicroservice = new TarCollectionMicroservice();
		InvocationContext invocationContext = new InvocationContext();
		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		tarCollectionMicroservice.setInvocationContext(invocationContext);
		tarCollectionMicroservice.setContainerEnvironment(containerEnvironment);
		ExecResult result = tarCollectionMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

		String sourcePath = (String) invocationContext.getSharedProperties()
				.get(EnqueueTransferMicroservice.LOCAL_FILE_NAME);
		Assert.assertNotNull("no source path put in shared context", sourcePath);

	}

}
