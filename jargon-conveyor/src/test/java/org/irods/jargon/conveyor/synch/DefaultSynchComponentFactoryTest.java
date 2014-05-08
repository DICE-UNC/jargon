package org.irods.jargon.conveyor.synch;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.SynchronizationType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class DefaultSynchComponentFactoryTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "BasicSynchronizationServiceImplTest";
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

	}

	@After
	public void tearDown() throws Exception {
		conveyorService.getQueueManagerService().purgeAllFromQueue();
	}

	@Test
	public void testWiredIn() throws Exception {
		SynchComponentFactory actual = conveyorService
				.getSynchComponentFactory();
		Assert.assertNotNull("synch factory not wired in", actual);
	}

	@Test
	public void testGetDiffProcessorForIrodsToLocal() throws Exception {
		Synchronization synch = new Synchronization();
		synch.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		LocalToIRODSDiffProcessor processor = (LocalToIRODSDiffProcessor) conveyorService
				.getSynchComponentFactory().instanceDiffProcessor(synch, tcb);
		Assert.assertNotNull("no processor returned", processor);

	}
}
