package org.irods.jargon.conveyor.synch;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class DefaultDiffCreatorTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DefaultDiffCreatorTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
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
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		conveyorService.getQueueManagerService().purgeAllFromQueue();
		conveyorService.getGridAccountService().resetPassPhraseAndAccounts();
		conveyorService.getConveyorExecutorService().requestResumeFromPause();

	}

	@After
	public void tearDown() throws Exception {
		conveyorService.getQueueManagerService().purgeAllFromQueue();
	}

	@Test
	public void testCreateDiff() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		String rootCollection = "testCreateDiff";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);

		File irodsRoot = (File) irodsFileSystem.getIRODSFileFactory(
				irodsAccount)
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		irodsRoot.mkdirs();

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + rootCollection, "test", ".doc", 20, 1, 2);

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		GridAccount gridAccount = conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount);

		AbstractSynchronizingDiffCreator diffCreator = new DefaultDiffCreator(
				conveyorService,
				irodsFileSystem
						.getIRODSAccessObjectFactory()
						.buildDefaultTransferControlBlockBasedOnJargonProperties());

		Date now = new Date();
		Synchronization synch = new Synchronization();
		synch.setGridAccount(gridAccount);
		synch.setFrequencyType(FrequencyType.EVERY_DAY);
		synch.setIrodsSynchDirectory(irodsCollectionRootAbsolutePath);
		synch.setLocalSynchDirectory(localCollectionAbsolutePath);
		synch.setName(rootCollection);
		synch.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synch.setCreatedAt(now);
		synch.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synch);

		Transfer synchTransfer = new Transfer();
		synchTransfer.setGridAccount(gridAccount);
		synchTransfer.setIrodsAbsolutePath(irodsCollectionRootAbsolutePath);
		synchTransfer.setLocalAbsolutePath(localCollectionAbsolutePath);
		synchTransfer.setSynchronization(synch);
		synchTransfer.setTransferState(TransferStateEnum.ENQUEUED);
		synchTransfer.setTransferType(TransferType.SYNCH);

		FileTreeModel diffModel = diffCreator.createDiff(synchTransfer);
		Assert.assertNotNull("null diff model", diffModel);

	}
}
