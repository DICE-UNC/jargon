package org.irods.jargon.ticket;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TicketAdminServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static final String IRODS_TEST_SUBDIR_PATH = "ticketAdminServiceImplTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private String lastTicketId = null;
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
	
	
	private IRODSFile createFileByName(String fileName, IRODSAccount irodsAccount,
			IRODSAccessObjectFactory accessObjectFactory) throws Exception {
		
		String absPath = scratchFileUtils
			.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
			.generateFileOfFixedLengthGivenName(absPath, fileName, 100);


		String targetIrodsCollection = testingPropertiesHelper
			.buildIRODSCollectionAbsolutePathFromTestProperties(
					testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
			.getDataTransferOperations(irodsAccount);
		dataTransferOperations.putOperation(
			localFileName,
			targetIrodsCollection,
			testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
			null, null);

		IRODSFile targetFile = irodsFileSystem
			.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
					targetIrodsCollection + "/" + fileName);
		
		irodsFileSystem.closeAndEatExceptions();
		
		return targetFile;
	}
	
	
	@Test
	public void testCreateTicketForDataObjectExists() throws Exception {
		
		String testFileName = "testCreateTicketForDataObjectExists.txt";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetFile = createFileByName(testFileName, irodsAccount, accessObjectFactory);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, null);
		
		IRODSQueryResultSetInterface resultSet = ticketSvc.listTicketByTicketString(ticketId);

		Assert.assertEquals(1, resultSet.getResults().size());
		
		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}
	
	
	@Test(expected = DataNotFoundException.class)
	public void testCreateTicketForDataObjectDoesNotExist() throws Exception {
		
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

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(accessObjectFactory, irodsAccount);

		String ticketId = ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, null);

		//Assert.assertEquals(0, resultSet.getResults().size());
		
		// delete ticket after done
		ticketSvc.deleteTicket(ticketId);

	}
	
	
	@Test
	public void testDeleteTicketForDataObjectExists() throws Exception {
		
		String testFileName = "testDeleteTicketForDataObjectExists.txt";
		IRODSQueryResultSetInterface resultSet = null;
		String ticketId = "deleteMe";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		
		IRODSFile targetFile = createFileByName(testFileName, irodsAccount, accessObjectFactory);
		

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(accessObjectFactory, irodsAccount);
		
		String id = ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, ticketId);
		resultSet = ticketSvc.listTicketByTicketString(ticketId);
		Assert.assertEquals(1, resultSet.getResults().size());

		ticketSvc.deleteTicket(ticketId);
		resultSet = ticketSvc.listTicketByTicketString(ticketId);
		Assert.assertEquals(0, resultSet.getResults().size());

	}

}
