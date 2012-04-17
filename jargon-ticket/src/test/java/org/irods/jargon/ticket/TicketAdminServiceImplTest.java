package org.irods.jargon.ticket;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TicketAdminServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static final String IRODS_TEST_SUBDIR_PATH = "ticketAdminServiceImplTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static boolean testTicket = false;
	private static final String DUPLICATE_ID = "duplicateid";

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

		// delete any tickets to start fresh
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);
		ticketSvc.deleteAllTicketsForThisUser();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (testTicket) {
			irodsFileSystem.closeAndEatExceptions();
		}
	}

	private IRODSFile createDataObjectByName(final String fileName,
			final String resource, final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory accessObjectFactory)
			throws Exception {

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, fileName, 1);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(localFileName,
				targetIrodsCollection, resource, null, null);

		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + fileName);

		return targetFile;
	}

	private IRODSFile createCollectionByName(final String collectionName,
			final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory accessObjectFactory)
			throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ collectionName);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile targetFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection);

		return targetFile;
	}

	@Test
	public void testCreateTicketForDataObjectExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(ticketId, ticket.getTicketString());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testCreateTicketForDataObjectAndThenListIt() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectAndThenListIt.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		List<Ticket> tickets = ticketSvc.listAllTicketsForDataObjects(0);
		Assert.assertTrue("tickets array has no values", tickets.size() > 0);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testCreateTicketForCollectionAndThenListIt() throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testCreateTicketForCollectionAndThenListIt";

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

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		List<Ticket> tickets = ticketSvc.listAllTicketsForCollections(0);
		Assert.assertTrue("tickets array has no values", tickets.size() > 0);

		boolean foundIt = false;
		// find my ticket
		for (Ticket actualTicket : tickets) {
			if (actualTicket.getIrodsAbsolutePath().equals(
					targetIrodsCollection)) {
				foundIt = true;
				Assert.assertEquals("ticket wrong type",
						TicketCreateModeEnum.TICKET_CREATE_READ,
						actualTicket.getType());
				Assert.assertEquals("wrong object type",
						Ticket.TicketObjectType.COLLECTION,
						actualTicket.getObjectType());
			}
		}

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);
		Assert.assertTrue("did not find my test ticket", foundIt);

	}

	@Test(expected = DataNotFoundException.class)
	public void testCreateTicketForDataObjectDoesNotExist() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectDoesNotExist";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketForDataObjectNullFile() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectNullFile.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, null, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketForDataObjectNullMode() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectNullMode.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(null, targetFile, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testCreateTicketForCollectionExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String collectionName = "testCreateTicketForCollectionExists";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collection = createCollectionByName(collectionName,
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, collection, null);

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(ticketId, ticket.getTicketString());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = DataNotFoundException.class)
	public void testCreateTicketForCollectionDoesNotExist() throws Exception {

		if (!testTicket) {
			return;
		}

		String collectionName = "testCreateTicketForCollectionDoesNotExist";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ collectionName);

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile collection = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, collection, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testCreateTicketForDataObjectInDifferentResource()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectInDifferentResource.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(ticketId, ticket.getTicketString());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = DataNotFoundException.class)
	public void testCreateTicketForDataObjectBelongingToDifferentUser()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectBelongingToDifferentUser.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount irodsAccount2 = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount2);
		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
						null, tcb);

		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = DuplicateDataException.class)
	public void testCreateTicketForDataObjectNonUniqueTicketString()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testCreateTicketForDataObjectNonUniqueTicketString.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile,
				DUPLICATE_ID);

		ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, DUPLICATE_ID);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);
	}

	@Test(expected = DuplicateDataException.class)
	public void testCreateTicketForDifferentDataObjectsNonUniqueTicketString()
			throws Exception {

		if (!testTicket) {
			return;
		}

		// need to do this because -890000 error (in previous test) seems to
		// leave iRODS in wierd state
		irodsFileSystem.closeAndEatExceptions();

		String testFileName1 = "testCreateTicketForDifferentDataObjectsNonUniqueTicketString1.txt";
		String testFileName2 = "testCreateTicketForDifferentDataObjectsNonUniqueTicketString2.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile1 = createDataObjectByName(
				testFileName1,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);
		IRODSFile targetFile2 = createDataObjectByName(
				testFileName2,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId1 = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile1,
				"hellothere");

		String ticketId2 = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile2,
				"hellothere");

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId1);
		ticketSvc.deleteTicket(ticketId2);
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteTicketForDataObjectExists() throws Exception {

		if (!testTicket) {
			return;
		}

		// need to do this because -890000 error (in previous test) seems to
		// leave iRODS in wierd state
		irodsFileSystem.closeAndEatExceptions();
		String testFileName = "testDeleteTicketForDataObjectExists.txt";
		String ticketId = "deleteMe" + System.currentTimeMillis();

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		File sourceFile = new File(localFileName);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileName);
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, ticketId);
		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(ticketId, ticket.getTicketString());

		ticketSvc.deleteTicket(ticketId);
		ticketSvc.getTicketForSpecifiedTicketString(ticketId);
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteTicketForDataObjectDoesNotExist() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testDeleteTicketForDataObjectDoesNotExist.txt";
		String ticketId = "deleteMe" + System.currentTimeMillis();

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 3);

		File sourceFile = new File(localFileName);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileName);
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, ticketId);

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);
		Assert.assertEquals(ticketId, ticket.getTicketString());

		targetFile.deleteWithForceOption();

		ticketSvc.deleteTicket(ticketId);
		ticketSvc.getTicketForSpecifiedTicketString(ticketId);

	}

	@Test
	public void testDeleteTicketForTicketDoesNotExist() throws Exception {

		if (!testTicket) {
			return;
		}
		String ticketId = "Idonotexist";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);
		boolean result = ticketSvc.deleteTicket(ticketId);
		Assert.assertFalse("ticket delete unsuccessful expected", result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTicketWithNullTicketString() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testDeleteTicketWithNullTicketString.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, null);

		ticketSvc.deleteTicket(null);
	}

	@Test
	public void testModifyTicketUsesLimitForTicketExists() throws Exception {

		if (!testTicket) {
			return;
		}

		int numberOfUses = 22;
		String testFileName = "testModifyTicketUsesLimitForDataObjectExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		Assert.assertTrue(ticketSvc.setTicketUsesLimit(ticketId, numberOfUses));

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(numberOfUses, ticket.getUsesLimit());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketUsesLimitForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberOfUses = 22;
		String testFileName = "testModifyTicketUsesLimitForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.setTicketUsesLimit(ticketId, numberOfUses));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketUsesLimitForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberOfUses = 22;
		String testFileName = "testModifyTicketUsesLimitForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		ticketSvc.setTicketUsesLimit(null, numberOfUses);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketUsesLimitForTicketExistsUsesLessThan0()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberOfUses = -1;
		String testFileName = "testModifyTicketUsesLimitForTicketExistsUsesLessThan0.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		ticketSvc.setTicketUsesLimit(ticketId, numberOfUses);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketFileWriteLimitForTicketExists()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberFileWrites = 102;
		String testFileName = "testModifyTicketFileWriteLimitForDataObjectExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.setTicketFileWriteLimit(ticketId,
				numberFileWrites));

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(numberFileWrites, ticket.getWriteFileLimit());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketFileWriteLimitForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberFileWrites = 102;
		String testFileName = "testModifyTicketFileWriteLimitForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.setTicketFileWriteLimit(ticketId,
				numberFileWrites));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketFileWriteLimitForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberFileWrites = 102;
		String testFileName = "testModifyTicketFileWriteLimitForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketFileWriteLimit(null, numberFileWrites);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketFileWriteLimitForTicketExistsLimitLessThan0()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberFileWrites = -1;
		String testFileName = "testModifyTicketFileWriteLimitForTicketExistsLimitLessThan0.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketFileWriteLimit(ticketId, numberFileWrites);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketByteWriteLimitForTicketExists()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberByteWrites = 100993;
		String testFileName = "testModifyTicketByteWriteLimitForDataObjectExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.setTicketByteWriteLimit(ticketId,
				numberByteWrites));

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		Assert.assertEquals(numberByteWrites, ticket.getWriteByteLimit());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketByteWriteLimitForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberByteWrites = 100993;
		String testFileName = "testModifyTicketByteWriteLimitForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.setTicketByteWriteLimit(ticketId,
				numberByteWrites));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketByteWriteLimitForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberByteWrites = 100993;
		String testFileName = "testModifyTicketByteWriteLimitForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketByteWriteLimit(null, numberByteWrites);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketByteWriteLimitForTicketExistsLimitLessThan0()
			throws Exception {

		if (!testTicket) {
			return;
		}

		int numberByteWrites = -1;
		String testFileName = "testModifyTicketByteWriteLimitForTicketExistsLimitLessThan0.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketByteWriteLimit(ticketId, numberByteWrites);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketExpirationForTicketExists() throws Exception {

		if (!testTicket) {
			return;
		}

		Date expireSoon = new Date();
		long now = expireSoon.getTime();
		expireSoon.setTime(now + 2000);
		String testFileName = "testModifyTicketExpirationForTicketExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.setTicketExpiration(ticketId, expireSoon));

		Ticket ticket = ticketSvc.getTicketForSpecifiedTicketString(ticketId);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		String formattedDate = df.format(expireSoon.getTime());
		Assert.assertEquals(formattedDate, ticket.getFormattedExpireTime());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testModifyTicketExpirationForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		Date expireSoon = new Date();
		long now = expireSoon.getTime();
		expireSoon.setTime(now + 2000);
		String testFileName = "testModifyTicketExpirationForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.setTicketExpiration(ticketId, expireSoon));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpirationForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		Date expireSoon = new Date();
		long now = expireSoon.getTime();
		expireSoon.setTime(now + 2000);
		String testFileName = "testModifyTicketExpirationForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketExpiration(null, expireSoon);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpirationForTicketExistsNullExpiration()
			throws Exception {

		if (!testTicket) {
			return;
		}

		Date expireSoon = null;
		String testFileName = "testModifyTicketExpirationForTicketExistsNullExpiration.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.setTicketExpiration(ticketId, expireSoon);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketUserRestrictionForTicketExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketUserRestrictionForTicketExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketUserRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketUserRestrictionForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketUserRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketUserRestrictionForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketUserRestriction(null, irodsAccount.getUserName());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketUserRestrictionForTicketExistsNullUserId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketUserRestrictionForTicketExistsNullUserId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketUserRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = InvalidUserException.class)
	public void testAddTicketUserRestrictionForTicketExistsInvalidUser()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String invalidUser = "me";

		String testFileName = "testAddTicketUserRestrictionForTicketExistsInvalidUser.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketUserRestriction(ticketId, invalidUser);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketUserRestrictionForTicketExists()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketUserRestrictionForTicketExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		Assert.assertTrue(ticketSvc.removeTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertTrue(users.isEmpty());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketUserRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketUserRestrictionForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.removeTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketUserRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketUserRestrictionForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		ticketSvc.removeTicketUserRestriction(null, irodsAccount.getUserName());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketUserRestrictionForTicketExistsNullUserId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketUserRestrictionForTicketExistsNullUserId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		ticketSvc.removeTicketUserRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = InvalidUserException.class)
	public void testRemoveTicketUserRestrictionForTicketExistsInvalidUser()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String invalidUser = "me";

		String testFileName = "testRemoveTicketUserRestrictionForTicketExistsInvalidUser.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketUserRestriction(ticketId,
				irodsAccount.getUserName()));
		List<String> users = ticketSvc
				.listAllUserRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(irodsAccount.getUserName(), users.get(0));

		ticketSvc.removeTicketUserRestriction(ticketId, invalidUser);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketGroupRestrictionForTicketExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketGroupRestrictionForTicketExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);
		
		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketGroupRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketGroupRestrictionForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketGroupRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketGroupRestrictionForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketGroupRestriction(null, testGroupName);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketGroupRestrictionForTicketExistsNullGroupId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketGroupRestrictionForTicketExistsNullGroupId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketGroupRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = InvalidGroupException.class)
	public void testAddTicketGroupRestrictionForTicketExistsInvalidGroup()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketGroupRestrictionForTicketExistsInvalidGroup.txt";
		String testGroupName = "wronggroop";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketGroupRestriction(ticketId, testGroupName);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketGroupRestrictionForTicketExists()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketGroupRestrictionForTicketExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		Assert.assertTrue(ticketSvc.removeTicketGroupRestriction(ticketId,
				testGroupName));
		groups = ticketSvc.listAllGroupRestrictionsForSpecifiedTicket(ticketId,
				0);
		Assert.assertTrue(groups.isEmpty());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketGroupRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketGroupRestrictionForTicketDoesNotExist.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.removeTicketGroupRestriction(ticketId,
				testGroupName));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketGroupRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketGroupRestrictionForTicketExistsNullTicketId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		ticketSvc.removeTicketGroupRestriction(null, testGroupName);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketGroupRestrictionForTicketExistsNullGroupId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketGroupRestrictionForTicketExistsNullGroupId.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);


		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		ticketSvc.removeTicketGroupRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = InvalidGroupException.class)
	public void testRemoveTicketGroupRestrictionForTicketExistsInvalidGroup()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketGroupRestrictionForTicketExistsInvalidGroup.txt";
		String wrongGroupName = "wronggroop";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		String testGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketGroupRestriction(ticketId,
				testGroupName));
		List<String> groups = ticketSvc
				.listAllGroupRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(testGroupName, groups.get(0));

		ticketSvc.removeTicketGroupRestriction(ticketId, wrongGroupName);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketHostRestrictionForTicketExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketHostRestrictionForTicketExists.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testAddTicketHostRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketHostRestrictionForTicketDoesNotExist.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketHostRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketHostRestrictionForTicketExistsNullTicketId.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketHostRestriction(null, localHost);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTicketHostRestrictionForTicketExistsNullHost()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketHostRestrictionForTicketExistsNullHost.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketHostRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	// TODO: change this to InvalidHostException (-855000) when that gets
	// implemented
	@Test(expected = JargonException.class)
	public void testAddTicketHostRestrictionForTicketExistsInvalidHost()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testAddTicketHostRestrictionForTicketExistsInvalidHost.txt";
		String invalidHost = "wrongipaddress";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		ticketSvc.addTicketHostRestriction(ticketId, invalidHost);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketHostRestrictionForTicketExists()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketHostRestrictionForTicketExists.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		Assert.assertTrue(ticketSvc.removeTicketHostRestriction(ticketId,
				localHost));
		hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertTrue(hosts.isEmpty());

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test
	public void testRemoveTicketHostRestrictionForTicketDoesNotExist()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketHostRestrictionForTicketDoesNotExist.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		// delete ticket
		Assert.assertTrue(ticketSvc.deleteTicket(ticketId));

		Assert.assertFalse(ticketSvc.removeTicketHostRestriction(ticketId,
				localHost));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketHostRestrictionForTicketExistsNullTicketId()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketHostRestrictionForTicketExistsNullTicketId.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		ticketSvc.removeTicketHostRestriction(null, localHost);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveTicketHostRestrictionForTicketExistsNullHost()
			throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testRemoveTicketHostRestrictionForTicketExistsNullHost.txt";
		String localHost = "127.0.0.1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		ticketSvc.removeTicketHostRestriction(ticketId, null);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	// TODO: change this to InvalidHostException (-855000) when that gets
	// implemented
	@Test(expected = JargonException.class)
	public void testRemoveTicketHostRestrictionForTicketExistsInvalidHost()
			throws Exception {

		if (!testTicket) {
			return;
		}
		String localHost = "127.0.0.1";
		String invalidHost = "wrongipaddress";

		String testFileName = "testRemoveTicketHostRestrictionForTicketExistsInvalidHost.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createDataObjectByName(
				testFileName,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_WRITE, targetFile, null);

		Assert.assertTrue(ticketSvc.addTicketHostRestriction(ticketId,
				localHost));
		List<String> hosts = ticketSvc
				.listAllHostRestrictionsForSpecifiedTicket(ticketId, 0);
		Assert.assertEquals(localHost, hosts.get(0));

		ticketSvc.removeTicketHostRestriction(ticketId, invalidHost);

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}

	/**
	 * create a ticket then make sure I can tell it's in use
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIsTicketInUseWhenExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testIsTicketInUseWhenExists";

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

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(
				TicketCreateModeEnum.TICKET_CREATE_READ, targetFile, null);

		boolean inUse = ticketSvc.isTicketInUse(ticketId);
		TestCase.assertTrue("ticket should be in use", inUse);

	}

	/**
	 * check that a ticket not in use does not exist
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIsTicketInUseWhenNotExists() throws Exception {

		if (!testTicket) {
			return;
		}

		String testCollection = "testIsTicketInUseWhenNotExists";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(
				accessObjectFactory, irodsAccount);

		boolean inUse = ticketSvc.isTicketInUse(testCollection);
		TestCase.assertFalse("ticket should not be in use", inUse);

	}

	// for tests of list tickets - list-all non admin user types will only see
	// their own
	// also when running ls or ls-all as rodsadmin user, do both (ls and ls-all)
	// always return all tickets?
	// (even those tickets for data objects or collections that no longer
	// exist??)

}
