/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.RunningStatus;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.SynchronizationManagerService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.FrequencyType;
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

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
// @Transactional(propagation = Propagation.REQUIRED)
public class BasicSynchronizationServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
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
	public void testWiredIntoConveyorService() throws Exception {

		SynchronizationManagerService actual = conveyorService
				.getSynchronizationManagerService();
		Assert.assertNotNull("synch service not wired in", actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddSynchronizationMissingSynch() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(null);

	}

	@Test
	public void testAddSynchronization() throws Exception {
		String rootCollection = "testAddSynchronization";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		Assert.assertNotNull("did not persist", synchronization.getId());

	}

	@Test(expected = ConveyorExecutionException.class)
	public void testAddSynchronizationNullIRODS() throws Exception {
		String rootCollection = "testAddSynchronization";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(null);
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);
	}

	@Test(expected = ConveyorExecutionException.class)
	public void testAddSynchronizationNullLocal() throws Exception {
		String rootCollection = "testAddSynchronization";

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(null);
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

	}

	@Test
	public void testUpdateSynchronization() throws Exception {
		String rootCollection = "testUpdateSynchronization";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		synchronization.setFrequencyType(FrequencyType.EVERY_WEEK);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		Synchronization actual = conveyorService
				.getSynchronizationManagerService().findById(
						synchronization.getId());

		Assert.assertNotNull("did not find", actual);

	}

	@Test
	public void testListAll() throws Exception {
		String rootCollection = "testListAll";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		List<Synchronization> actual = conveyorService
				.getSynchronizationManagerService().listAllSynchronizations();

		Assert.assertFalse("no synchs listed", actual.isEmpty());

	}

	@Test
	public void testDeleteSynchronization() throws Exception {
		String rootCollection = "testDeleteSynchronization";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_IRODS_TO_LOCAL);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		conveyorService.getSynchronizationManagerService()
				.deleteSynchronization(synchronization);

		Synchronization actual = conveyorService
				.getSynchronizationManagerService().findById(
						synchronization.getId());

		Assert.assertNull("did not delete", actual);

	}

	@Test
	public void testEnqueueSynchronization() throws Exception {
		String rootCollection = "testEnqueueSynchronization";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.mkdirs();

		File localFile = new File(localCollectionAbsolutePath);
		localFile.mkdirs();

		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Synchronization synchronization = new Synchronization();
		Date now = new Date();
		synchronization.setCreatedAt(now);
		synchronization.setFrequencyType(FrequencyType.EVERY_DAY);
		synchronization.setGridAccount(conveyorService.getGridAccountService()
				.findGridAccountByIRODSAccount(irodsAccount));
		synchronization.setIrodsSynchDirectory(destFile.getAbsolutePath());
		synchronization.setLocalSynchDirectory(localFile.getAbsolutePath());
		synchronization.setName(rootCollection);
		synchronization
				.setSynchronizationMode(SynchronizationType.ONE_WAY_LOCAL_TO_IRODS);
		synchronization.setUpdatedAt(now);
		conveyorService.getSynchronizationManagerService()
				.addOrUpdateSynchronization(synchronization);

		Assert.assertNotNull("did not persist", synchronization.getId());

		conveyorService.getSynchronizationManagerService()
				.triggerSynchronizationNow(synchronization);

		Thread.sleep(1000);

		while (conveyorService.getConveyorExecutorService().getRunningStatus() != RunningStatus.IDLE) {
			Thread.sleep(1000);
		}

		Synchronization postSynch = conveyorService
				.getSynchronizationManagerService().findById(
						synchronization.getId());
		Assert.assertNotNull("did not get post synch data", postSynch);

	}

}
