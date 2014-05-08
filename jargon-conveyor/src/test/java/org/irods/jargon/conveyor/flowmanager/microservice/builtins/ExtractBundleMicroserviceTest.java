package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.flowmanager.microservice.ContainerEnvironment;
import org.irods.jargon.conveyor.flowmanager.microservice.InvocationContext;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.datautils.filearchive.LocalTarFileArchiver;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ExtractBundleMicroserviceTest {
	private static Properties testingProperties = new Properties();
	private static JargonProperties jargonOriginalProperties = null;
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ExtractBundleMicroserviceTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

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
	public void testExtractBundle() throws Exception {
		String tarName = "testExtractBundle.tar";
		String bunSubdir = "testExtractBundle";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + bunSubdir);

		String tarParentCollection = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File tarFile = new File(tarParentCollection);
		tarFile = new File(tarFile.getParentFile(), tarName);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, bunSubdir, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 200 * 1024);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
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

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ bunSubdir);

		String extractBunIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ bunSubdir);

		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(extractBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		LocalTarFileArchiver archiver = new LocalTarFileArchiver(
				localCollectionAbsolutePath, tarFile.getAbsolutePath());

		File tarredFile = archiver.createArchive();

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(tarredFile.getAbsolutePath(), targetBunFileAbsPath,
				"", null, null);

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, localCollectionAbsolutePath,
				"blah", "", 0, 0, 0, 0, 0, TransferState.OVERALL_INITIATION,
				"host", "zone");

		Microservice extractBundleMicroservice = new ExtractBundleMicroservice();
		InvocationContext invocationContext = new InvocationContext();

		invocationContext.getSharedProperties().put(
				ExtractBundleMicroservice.BUNDLE_TO_EXTRACT,
				targetBunFileAbsPath);
		invocationContext.getSharedProperties().put(
				ExtractBundleMicroservice.TARGET_COLLECTION,
				extractBunIrodsCollection);

		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		containerEnvironment.setConveyorService(conveyorService);
		Mockito.when(conveyorService.getIrodsAccessObjectFactory()).thenReturn(
				irodsFileSystem.getIRODSAccessObjectFactory());
		invocationContext.setIrodsAccount(irodsAccount);
		extractBundleMicroservice.setInvocationContext(invocationContext);
		extractBundleMicroservice.setContainerEnvironment(containerEnvironment);

		ExecResult result = extractBundleMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

	}
}
