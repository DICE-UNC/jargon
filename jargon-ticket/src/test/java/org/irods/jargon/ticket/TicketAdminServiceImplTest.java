package org.irods.jargon.ticket;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}
	
	
//	private IRODSFile createFileByName(String fileName, IRODSAccount irodsAccount,
//			IRODSAccessObjectFactory accessObjectFactory) throws Exception {
//		
//		IRODSFile irodsFile = null;
//
//		String absPath = scratchFileUtils
//				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
//		String localFileName = FileGenerator
//					.generateFileOfFixedLengthGivenName(absPath, fileName, 10);

//		String getFileName = "testGetResult.txt";
//		String getResultLocalPath = scratchFileUtils
//				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/')
//				+ getFileName;
//		File localFile = new File(getResultLocalPath);
//
//		String targetIrodsCollection = testingPropertiesHelper
//				.buildIRODSCollectionAbsolutePathFromTestProperties(
//						testingProperties, IRODS_TEST_SUBDIR_PATH);
//
//		DataTransferOperations dataTransferOperations = accessObjectFactory
//				.getDataTransferOperations(irodsAccount);
//		dataTransferOperations
//				.putOperation(
//						localFileName,
//						targetIrodsCollection,
//						testingProperties
//								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
//						null, null);
//		
//		DataObjectAO dataObjectAO = accessObjectFactory.getDataObjectAO(irodsAccount);
//		irodsFile = dataObjectAO.instanceIRODSFileForPath(targetIrodsCollection + '/'+ fileName);
//		
//		irodsFileSystem.closeAndEatExceptions();
//		
//		return irodsFile;
//	}
	
	
	@Test
	public void testCreateTicketForDataObjectExists() throws Exception {
		
		String testFileName = "testCreateTicketForDataObjectExists.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		DataTransferOperations dataTransferOperations = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dataTransferOperations
				.putOperation(
						localFileName,
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
						null, null);
		
		IRODSFile targetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + testFileName);

		TicketAdminService ticketSvc = new TicketAdminServiceImpl(accessObjectFactory, irodsAccount);

		ticketSvc.createTicket(TicketCreateModeEnum.TICKET_CREATE_READ,
				targetFile, null);

	}

}
