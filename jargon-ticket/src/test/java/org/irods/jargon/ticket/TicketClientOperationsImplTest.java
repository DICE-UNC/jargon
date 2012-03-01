package org.irods.jargon.ticket;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.NoAccessException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TicketClientOperationsImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static final String IRODS_TEST_SUBDIR_PATH = "TicketClientOperationsImplTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static boolean testTicket = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		testTicket = testingPropertiesLoader
				.isTestRemoteExecStream(testingProperties);
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (testTicket) {
			irodsFileSystem.closeAndEatExceptions();
		}
	}

	@Test
	public final void testTicketClientOperationsImpl() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		TicketClientOperations ticketClientOperations = new TicketClientOperationsImpl(
				accessObjectFactory, irodsAccount);
		Assert.assertNotNull("null ticketClientOperations",
				ticketClientOperations); // really just looking for no errors

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testTicketClientOperationsImplNullAccessObjectFactory()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = null;
		new TicketClientOperationsImpl(accessObjectFactory, irodsAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testTicketClientOperationsImplNullAccount()
			throws Exception {
		IRODSAccount irodsAccount = null;
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		new TicketClientOperationsImpl(accessObjectFactory, irodsAccount);

	}

	@Test
	public final void testPutFileToIRODSUsingTicket() throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testPutFileToIRODSUsingTicket";
		String testFileName = "testPutFileToIRODSUsingTicket.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollection);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);
		ticketSvc.deleteTicket(testCollection);
		IRODSFile targetFileToCleanUp = accessObjectFactory
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		targetFileToCleanUp.deleteWithForceOption();
		targetFile.mkdirs();

		String ticketString = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile,
				testCollection);

		TicketClientOperations ticketClientService = new TicketClientOperationsImpl(
				accessObjectFactory, secondaryAccount);

		ticketClientService.putFileToIRODSUsingTicket(ticketString, localFile,
				targetFile, null, null);

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("target file not written", actualFile.exists());
		ticketSvc.deleteTicket(testCollection);

	}

	/**
	 * Put a file to irods, then put to it as a secondary user with a ticket
	 * using overwrite, giving that existing file name, and using a force option
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testPutFileToIRODSUsingTicketExistingFileSpecifyFile()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testPutFileToIRODSUsingTicketExistingFileSpecifyFile";
		String testFileName = "testPutFileToIRODSUsingTicketExistingFileSpecifyFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollection);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetFile.mkdirs();
		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFile, targetFile, null, null);

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);
		ticketSvc.deleteTicket(testCollection);
		IRODSFile targetFileToCleanUp = accessObjectFactory
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);
		targetFileToCleanUp.deleteWithForceOption();

		String ticketString = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile,
				testCollection);

		TicketClientOperations ticketClientService = new TicketClientOperationsImpl(
				accessObjectFactory, secondaryAccount);

		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		ticketClientService.putFileToIRODSUsingTicket(ticketString, localFile,
				targetFile, null, tcb);

		IRODSFile actualFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetFile.getAbsolutePath(), testFileName);
		Assert.assertTrue("target file not written", actualFile.exists());
		ticketSvc.deleteTicket(testCollection);

	}

	@Test(expected = NoAccessException.class)
	public final void testPutFileToIRODSUsingInvalidTicket() throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testPutFileToIRODSUsingInvalidTicket";
		String testFileName = "testPutFileToIRODSUsingInvalidTicket.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		File localFile = new File(localFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testCollection);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetFile.mkdirs();

		String ticketString = "testPutFileToIRODSUsingInvalidTicket";

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		TicketClientOperations ticketClientService = new TicketClientOperationsImpl(
				accessObjectFactory, secondaryAccount);

		ticketClientService.putFileToIRODSUsingTicket(ticketString, localFile,
				targetFile, null, null);

	}

}
