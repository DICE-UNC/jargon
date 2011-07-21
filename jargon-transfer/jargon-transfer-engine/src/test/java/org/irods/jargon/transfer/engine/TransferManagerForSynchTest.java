package org.irods.jargon.transfer.engine;

import java.io.FileReader;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TransferManagerForSynchTest {

	private static Properties testingProperties = new Properties();

	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;

	public static final String IRODS_TEST_SUBDIR_PATH = "TransferManagerForSynchTest";

	private static IRODSFileSystem irodsFileSystem;

	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		String databaseUrl = "jdbc:derby:" + System.getProperty("user.home")
				+ "/.idrop/target/database/transfer";
		DatabasePreparationUtils.clearAllDatabaseForTesting(databaseUrl,
				"transfer", "transfer"); // TODO: make a prop
		irodsFileSystem = IRODSFileSystem.instance();
		

	}
	/**
	 * @throws Exception
	 */
	@Before
	public void setUpEach() throws Exception {
		
	}

	@Ignore // FIXME: work in progress
	public void testEnqueueSynch() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance());

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String rootCollection = "testEnqueueSynch";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH
								+ "/rootCollection");
		IRODSFile irodsSynchFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount)
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		irodsSynchFile.mkdirs();

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, "testSubdir", 1, 2, 1,
						"testFile", ".txt", 3, 2, 2, 5);

		transferManager.pause();
		Synchronization synchronization = new Synchronization();
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setName(rootCollection);
		synchronization.setCreatedAt(new Date());
		synchronization.setDefaultResourceName(irodsAccount
				.getDefaultStorageResource());
		synchronization.setFrequencyType(FrequencyType.EVERY_FIFTEEN_MINUTES);
		synchronization.setIrodsHostName(irodsAccount.getHost());
		synchronization.setIrodsPassword(irodsAccount.getPassword());
		synchronization.setIrodsPort(irodsAccount.getPort());
		synchronization.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath);
		synchronization.setIrodsUserName(irodsAccount.getUserName());
		synchronization.setIrodsZone(irodsAccount.getZone());
		synchronization.setLocalSynchDirectory(rootCollection);
		SynchManagerService synchManagerService = transferManager
				.getTransferServiceFactory().instanceSynchManagerService();
		synchManagerService.createNewSynchConfiguration(synchronization);

		synchronization = synchManagerService.findByName(rootCollection);

		transferManager.enqueueASynch(synchronization, irodsAccount);
		
		// let synch run

		transferManager.processNextInQueueIfIdle();

		int waitCtr = 0;

		while (true) {
			if (waitCtr++ > 20) {
				Assert.fail("synch timed out");
			}
			Thread.sleep(1000);
			if (transferManager.getRunningStatus() == TransferManager.RunningStatus.IDLE) {
				break;
			}

		}

		Assert.assertEquals("should have been no errors",
				TransferManager.ErrorStatus.OK,
				transferManager.getErrorStatus());

	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEnqueueSynchNullSynch() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance());

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		transferManager.enqueueASynch(null, irodsAccount);

	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEnqueueSynchNullAccount() throws Exception {
		TransferManager transferManager = new TransferManagerImpl(
				IRODSFileSystem.instance());
		
		transferManager.enqueueASynch(new Synchronization(), null);

	}


}
