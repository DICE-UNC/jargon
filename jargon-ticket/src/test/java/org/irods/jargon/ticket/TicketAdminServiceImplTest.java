package org.irods.jargon.ticket;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
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

		IRODSQueryResultSetInterface resultSet = ticketSvc
				.getTicketQueryResultForSpecifiedTicketString(ticketId,
						Ticket.TicketObjectType.DATA_OBJECT);

		Assert.assertEquals(1, resultSet.getResults().size());

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
		TestCase.assertTrue("tickets array has no values", tickets.size() > 0);

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
		TestCase.assertTrue("tickets array has no values", tickets.size() > 0);

		boolean foundIt = false;
		// find my ticket
		for (Ticket actualTicket : tickets) {
			if (actualTicket.getIrodsAbsolutePath().equals(
					targetIrodsCollection)) {
				foundIt = true;
				TestCase.assertEquals("ticket wrong type",
						TicketCreateModeEnum.TICKET_CREATE_READ,
						actualTicket.getType());
				TestCase.assertEquals("wrong object type",
						Ticket.TicketObjectType.COLLECTION,
						actualTicket.getObjectType());
			}
		}

		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);
		TestCase.assertTrue("did not find my test ticket", foundIt);

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

		IRODSQueryResultSetInterface resultSet = ticketSvc
				.getTicketQueryResultForSpecifiedTicketString(ticketId,
						Ticket.TicketObjectType.COLLECTION);

		Assert.assertEquals(1, resultSet.getResults().size());

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

		IRODSQueryResultSetInterface resultSet = ticketSvc
				.getTicketQueryResultForSpecifiedTicketString(ticketId,
						Ticket.TicketObjectType.DATA_OBJECT);

		Assert.assertEquals(1, resultSet.getResults().size());

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
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
						null, null);

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

	@Test(expected = DataNotFoundException.class)
	public void testDeleteTicketForDataObjectExists() throws Exception {

		if (!testTicket) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
		String testFileName = "testDeleteTicketForDataObjectExists.txt";
		IRODSQueryResultSetInterface resultSet = null;
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
		resultSet = ticketSvc.getTicketQueryResultForSpecifiedTicketString(
				ticketId, Ticket.TicketObjectType.DATA_OBJECT);
		Assert.assertEquals(1, resultSet.getResults().size());

		ticketSvc.deleteTicket(ticketId);
		ticketSvc.getTicketQueryResultForSpecifiedTicketString(ticketId,
				Ticket.TicketObjectType.DATA_OBJECT);
	}

	@Test(expected = DataNotFoundException.class)
	public void testDeleteTicketForDataObjectDoesNotExist() throws Exception {

		if (!testTicket) {
			return;
		}

		String testFileName = "testDeleteTicketForDataObjectDoesNotExist.txt";
		IRODSQueryResultSetInterface resultSet = null;
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

		resultSet = ticketSvc.getTicketQueryResultForSpecifiedTicketString(
				ticketId, Ticket.TicketObjectType.DATA_OBJECT);
		Assert.assertEquals(1, resultSet.getResults().size());

		targetFile.deleteWithForceOption();

		ticketSvc.deleteTicket(ticketId);
		ticketSvc.getTicketQueryResultForSpecifiedTicketString(ticketId,
				Ticket.TicketObjectType.DATA_OBJECT);

	}

	@Test(expected = DataNotFoundException.class)
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
		ticketSvc.deleteTicket(ticketId);
	}

	// for tests of list tickets - list-all non admin user types will only see
	// their own
	// also when running ls or ls-all as rodsadmin user, do both (ls and ls-all)
	// always return all tickets?
	// (even those tickets for data objects or collections that no longer
	// exist??)

}
