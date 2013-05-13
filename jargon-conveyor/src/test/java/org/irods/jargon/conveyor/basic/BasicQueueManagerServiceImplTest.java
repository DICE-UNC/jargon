package org.irods.jargon.conveyor.basic;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.unittest.utils.TransferTestRunningUtilities;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
public class BasicQueueManagerServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "BasicQueueManagerServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@Autowired
	private ConveyorService conveyorService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Rollback(true)
	public void testEnqueuePutTransferOperationAndWaitUntilDone()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);
		String rootCollection = "testEnqueuePutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 1,
						1, 1, "testFile", ".txt", 2, 2, 1, 2);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, -1);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferState.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());
		TransferItem items[] = new TransferItem[attempt.getTransferItems()
				.size()];
		items = attempt.getTransferItems().toArray(items);
		Assert.assertEquals("should be 1 item", 1, items.length);

	}
}
