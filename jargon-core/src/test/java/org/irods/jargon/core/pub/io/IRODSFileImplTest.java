/**
 * 
 */
package org.irods.jargon.core.pub.io;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSFileImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * [#1575] jargon-core permissions issue
	 */
	@Test
	public final void testCanReadViaGroup() throws Exception {
		String testFileName = "testCanReadViaGroup.xls";
		String testUserGroup = "testCanReadViaGroup";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testUserGroup,
				secondaryIrodsAccount.getUserName(), null);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(new File(fileNameOrig), irodsFile, null, null);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testUserGroup);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);

		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (props.isEirods()) {
			return;
		}
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(secondaryIrodsAccount);
		IRODSFile actual = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue(actual.canRead());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#canRead()}.
	 */
	@Test
	public final void testCanRead() throws Exception {
		String testFileName = "testCanRead.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue(irodsFile.canRead());
	}

	/**
	 * Bug [#1575] jargon-core permissions issue
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCanReadCollectionViaGroupMembership()
			throws Exception {
		String testColl = "testCanReadCollectionViaGroupMembershipx";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testColl);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.deleteWithForceOption();
		irodsFile.reset();
		irodsFile.mkdirs();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testColl);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testColl, secondaryAccount.getUserName(),
				null);

		collectionAO.removeAccessPermissionForUser(irodsAccount.getZone(),
				targetIrodsCollection, testColl, false);

		collectionAO.setAccessPermissionRead(irodsAccount.getZone(),
				targetIrodsCollection, testColl, false);

		IRODSFile actual = accessObjectFactory.getIRODSFileFactory(
				secondaryAccount).instanceIRODSFile(targetIrodsCollection);
		Assert.assertTrue(
				"user cannot read file even though he has group permissions",
				actual.canRead());
	}

	/**
	 * Bug [#1575] jargon-core permissions issue
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCanReadCollectionViaGroupMembershipWhenRoot()
			throws Exception {

		String targetIrodsCollection = "/";
		String testGroup = "testCanReadCollectionViaGroupMembershipWhenRoot";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserGroupAO userGroupAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserGroupAO(irodsAccount);

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroup);
		userGroupAO.addUserGroup(userGroup);

		userGroupAO.addUserToGroup(testGroup, irodsAccount.getUserName(), null);

		IRODSFile actual = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		// really making sure I don't get an error in the zone lookup (blank)
		// and query
		Assert.assertFalse("user should not be able to read file",
				actual.canRead());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#canWrite()} .
	 */
	@Test
	public final void testCanWrite() throws Exception {
		String testFileName = "testCanWrite.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue(irodsFile.canWrite());

	}

	@Test
	public final void testCanExecute() throws Exception {
		String testFileName = "testCanExecute.sh";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);
		File localFile = new File(fileNameAndPath.toString());
		localFile.setExecutable(true);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue("file should be executable", irodsFile.canExecute());

	}

	@Ignore
	// doesn't test much esp on windows
	public final void testCanExecuteWhenNot() throws Exception {
		String testFileName = "testCanExecuteWhenNot.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		File localFile = new File(fileNameAndPath.toString());
		localFile.setExecutable(false);

		// some os's (win) can have a hard time setting to not execute
		if (localFile.canExecute()) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertFalse("file should not be executable",
				irodsFile.canExecute());

	}

	@Test
	public final void testExistsFileWithParentAndBlankChild() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, "");

		Assert.assertTrue("file should exist", irodsFile.exists());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#exists()}.
	 */
	@Test
	public final void testExists() throws Exception {
		String testFileName = "testExists.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue(irodsFile.exists());
	}

	@Test
	public final void testRootExists() throws Exception {
		String testFileName = "/";

		// now get an irods file and see if it is readable, it should be

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(testFileName);

		Assert.assertTrue(irodsFile.exists());
		irodsSession.closeSession();
	}

	/*
	 * Bug [#351] IRODSFIle.getParentFile() should return null when at root
	 */
	@Test
	public final void testGetParentFileWhenRootGivesNull() throws Exception {
		String testFileName = "/";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(testFileName);

		Assert.assertNull(irodsFile.getParentFile());
	}

	/*
	 * Bug [#352] file.listFiles() when root should not list root as child
	 */
	@Test
	public final void testChildrenWhenRootDoesNotContainRoot() throws Exception {
		String testFileName = "/";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(testFileName);

		File[] children = irodsFile.listFiles();
		Assert.assertTrue("should have some children", children.length > 0);
		for (File child : children) {
			Assert.assertFalse("child of root given as root", child.getName()
					.equals("/"));
		}

	}

	@Test
	public final void testExistsNoFile() throws Exception {
		String testFileName = "testExistsNoFile.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		Assert.assertFalse(irodsFile.exists());
		irodsFileSystem.close();

	}

	@Test
	public final void testDeleteAFileQuotesInShorterFileName() throws Exception {
		String testFileName = "quote'infilename1.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSFile, null,
				null);

		targetIRODSFile.delete();

		targetIRODSFile.reset();

		Assert.assertFalse(targetIRODSFile.exists());
	}

	@Test
	public final void testDeleteAFileQuotesInLongerFileName() throws Exception {
		String testFileName = "oobie doobie ooobie doobie do-wah do-wah do-wah quote ' infilename1.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSFile, null,
				null);

		targetIRODSFile.delete();

		targetIRODSFile.reset();

		Assert.assertFalse(targetIRODSFile.exists());
	}

	@Test
	public final void testDeleteACollectionWithAFileWithQuotesInLongerFileName()
			throws Exception {
		String testCollectionSubdir = "testSubdirFortestDeleteACollectionWithAFileWithQuotesInLongerFileName";
		String testFileName = "oobie doobie ooobie doobie do-wah do-wah do-wah quote ' infilename1.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionSubdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetIRODSCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIRODSCollection.mkdirs();

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + testFileName);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSFile, null,
				null);

		targetIRODSCollection.delete();

		targetIRODSFile.reset();

		Assert.assertFalse("file should not still exist",
				targetIRODSFile.exists());
	}

	/**
	 * Delete a file with spaces in the name, test for: [#690] error in mass
	 * delete on ll
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testDeleteACollectionWithSpacesInTheNameName()
			throws Exception {
		String testCollectionSubdir = "testDeleteACollectionWithSpacesInTheNameName and there are spaces here --- see the spaces?";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionSubdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetIRODSCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIRODSCollection.mkdirs();
		targetIRODSCollection.delete();
		targetIRODSCollection.reset();
		Assert.assertFalse("file should not still exist",
				targetIRODSCollection.exists());
	}

	@Test
	public final void testExistsQuotesInFileName() throws Exception {
		String testFileName = "testExistsQuote'infilename.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null,
				null);

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue("file should exist", irodsFile.exists());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isDirectory()}.
	 */
	@Test
	public final void testIsDirectory() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		boolean isDir = irodsFile.isDirectory();
		Assert.assertTrue("this should be a collection", isDir);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isDirectory()}.
	 */
	@Test
	public final void testIsFileWhenDirectory() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		boolean isFile = irodsFile.isFile();
		Assert.assertFalse("this should be a File, not a dir", isFile);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isDirectory()}.
	 */
	@Test
	public final void testIsFileWhenNotExists() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "/blah");
		boolean isFile = irodsFile.isFile();
		Assert.assertFalse("this should not be a file, it does not exist",
				isFile);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isDirectory()}.
	 */
	@Test
	public final void testIsDirWhenNotExists() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "/blah");
		boolean isDir = irodsFile.isDirectory();
		Assert.assertFalse("this should not be a dir, it does not exist", isDir);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#isFile()}.
	 */
	@Test
	public final void testIsFile() throws Exception {
		String testFileName = "testIsFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		Assert.assertTrue("this should be a file", irodsFile.isFile());
	}

	@Test
	public final void testIsFileManyTimes() throws Exception {
		int count = 500;
		String testFileName = "testIsFileManyTimes.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = null;

		for (int i = 0; i < count; i++) {
			irodsFile = irodsFileFactory
					.instanceIRODSFile(targetIrodsCollection + '/'
							+ testFileName);

			Assert.assertTrue("this should be a file", irodsFile.isFile());
		}
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#lastModified()}.
	 */
	@Test
	public final void testLastModified() throws Exception {
		String testFileName = "testLastModified.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		long modDate = irodsFile.lastModified();
		Assert.assertTrue("mod date should be gt 0", modDate > 0);

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#lastModified()}.
	 */
	@Test
	public final void testLength() throws Exception {
		String testFileName = "testSizeFile.txt";
		int expectedSize = 8;
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				expectedSize);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		long size = irodsFile.length();
		Assert.assertEquals("size does not match", expectedSize, size);

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#lastModified()}.
	 */
	@Test
	public final void testLengthFileNotInIRODSYet() throws Exception {
		String testFileName = "testLengthFileNotInIRODSYet.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		long size = irodsFile.length();
		Assert.assertEquals("size should be zero", 0, size);

	}

	@Test
	public final void testLengthSpacesInName() throws Exception {
		String testFileName = "testSize File.zip";
		int expectedSize = 8;
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				expectedSize);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(absPath + testFileName,
				targetIrodsCollection, "", null, null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		long size = irodsFile.length();
		irodsFileSystem.close();
		Assert.assertEquals("size does not match", expectedSize, size);

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#createNewFile()}.
	 */
	@Test
	public final void testCreateNewFile() throws Exception {
		String testFileName = "testCreateNewFile.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		boolean success = irodsFile.createNewFile();

		irodsSession.closeSession();

		Assert.assertTrue("file creation not successful", success);
	}

	@Test
	public final void testCreateNewFileTwice() throws Exception {
		String testFileName = "testCreateNewFileTwice.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		boolean success = irodsFile.createNewFile();
		success = irodsFile.createNewFile();

		irodsSession.closeSession();

		Assert.assertFalse("file creation was successful", success);
	}

	@Test
	public final void testCreateNewCollectionGivingParentAndChild()
			throws Exception {
		String testNewChild = "testCreateNewCollectionGivingParentAndChild";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile delFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "/" + testNewChild);
		delFile.deleteWithForceOption();
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testNewChild);
		boolean success = irodsFile.mkdirs();
		Assert.assertEquals("path set correctly", targetIrodsCollection + "/"
				+ testNewChild, irodsFile.getAbsolutePath());
		Assert.assertTrue("new collection does not exist", irodsFile.exists());
		Assert.assertTrue("file creation not successful", success);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#createNewFile()}.
	 */
	@Test
	public final void testCreateNewFileAlreadyExists() throws Exception {
		String testFileName = "testCreateNewFileAlreadyExists.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		boolean success = irodsFile.createNewFile();

		Assert.assertFalse("file creation should not be successful", success);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#delete()}.
	 */
	@Test
	public final void testDeleteFile() throws Exception {
		String testFileName = "testDeleteFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		boolean result = irodsFile.delete();

		Assert.assertTrue("did not get a true result from the file delete",
				result);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(), accessObjectFactory, irodsAccount);
	}

	@Test
	public final void testDeleteFileNotExists() throws Exception {
		String testFileName = "testDeleteFileNotExists.txt";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		boolean result = irodsFile.delete();
		Assert.assertTrue("did not get a true result from the file delete",
				result);

	}

	@Test
	public final void testDeleteFileWithForce() throws Exception {
		String testFileName = "testDeleteFileWithForce.txt";
		String testCollectionName = "subcollForTestDeleteFileWithForce";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		// targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null,
				null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		boolean result = irodsFile.deleteWithForceOption();
		Assert.assertTrue("did not get a true result from the file delete",
				result);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testDeleteCollectionContainingFilesWithForce()
			throws Exception {
		String testFileName = "testDeleteCollectionContainingFilesWithForce.txt";
		String testCollectionName = "testDeleteCollectionContainingFilesWithForceSubcoll";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		File sourceFile = new File(absPath + testFileName);
		IRODSFile targetIRODSColl = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIRODSColl.deleteWithForceOption();
		targetIRODSColl.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperations.putOperation(sourceFile, targetIRODSColl, null,
				null);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		boolean result = targetIRODSColl.deleteWithForceOption();
		Assert.assertTrue("did not get a true result from the file delete",
				result);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testDeleteFileWhenCollection() throws Exception {
		String testDirName = "testDeleteFileWhenCollection";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		boolean result = irodsFile.delete();

		Assert.assertTrue(
				"did not get a true result from the collection delete", result);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testDeleteFileWhenCollectionWithForce() throws Exception {
		String testDirName = "testDeleteFileWhenCollectionWithForce";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		boolean result = irodsFile.deleteWithForceOption();

		Assert.assertTrue(
				"did not get a true result from the collection delete", result);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testFileClose() throws Exception {
		String testFileName = "testFileClose.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		irodsFile.open();
		irodsFile.close();
		// no error is success
	}

	@Test
	public final void testFileCloseTwice() throws Exception {
		String testFileName = "testFileCloseTwice.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		irodsFile.open();
		irodsFile.close();
		irodsFile.close();
		// no error is success
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#mkdir()}.
	 */
	@Test
	public final void testMkdir() throws Exception {
		String testDir = "testMkdir";
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsSession.closeSession();
		assertionHelper.assertIrodsFileOrCollectionExists(
				targetIrodsCollection,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	@Test
	public final void testMkdirTwice() throws Exception {
		String testDir = "testMkdir";
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		boolean secondTime = irodsFile.mkdir();
		irodsSession.closeSession();
		Assert.assertFalse(secondTime);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#mkdirs()}.
	 */
	@Test
	public final void testMkdirs() throws Exception {
		String testDir = "testMkdirs/andanother";
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDir);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();
		irodsSession.closeSession();
		assertionHelper.assertIrodsFileOrCollectionExists(
				targetIrodsCollection,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getTotalSpace()}.
	 */
	@Ignore
	public final void testGetTotalSpace() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getFreeSpace()}.
	 */
	@Ignore
	public final void testGetFreeSpace() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getUsableSpace()}.
	 */
	@Ignore
	public final void testGetUsableSpace() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#IRODSFile(java.lang.String, org.irods.jargon.core.pub.IRODSFileSystemAO)}
	 * .
	 */
	@Test
	public final void testIRODSFileStringIRODSFileSystemAO() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileSystemAO fileSystemAO = accessObjectFactory
				.getIRODSFileSystemAO(irodsAccount);
		Assert.assertNotNull("null fileSystem from factory", fileSystemAO);

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#compareTo(java.io.File)}
	 * .
	 */
	@Test
	public final void testCompareToFile() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String testFileName = "testCompareTo1.txt";
		String testFileName2 = "testCompareTo2.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile irodsFile2 = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName2);

		int comp = irodsFile.compareTo(irodsFile2);

		irodsSession.closeSession();

		Assert.assertEquals("compare fails", -1, comp);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public final void testEqualsObject() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String testFileName = "testEquals.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ '/' + testFileName;

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsFile2 = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);

		irodsSession.closeSession();

		Assert.assertEquals(irodsFile, irodsFile2);
	}

	@Test
	public final void testNotEqualsObject() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		String testFileName = "testEquals.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ '/' + testFileName;

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		IRODSFile irodsFile2 = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + "xxx");

		irodsSession.closeSession();

		Assert.assertFalse("files should not be equal",
				irodsFile.equals(irodsFile2));
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getAbsoluteFile()}.
	 */
	@Test
	public final void testGetAbsoluteFile() throws Exception {
		String testFileName = "testAbsolute.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile newIRODSFile = (IRODSFile) irodsFile.getAbsoluteFile();

		Assert.assertNotNull("my new file does not exist", newIRODSFile);
		Assert.assertEquals("absolute paths must match",
				irodsFile.getAbsolutePath(), newIRODSFile.getAbsolutePath());

	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getAbsolutePath()}.
	 */
	@Test
	public final void testGetAbsolutePath() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testAbsolute.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String actualPath = irodsFile.getAbsolutePath();
		Assert.assertEquals("paths do not match", targetIrodsCollection + '/'
				+ testFileName, actualPath);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getCanonicalFile()}.
	 */
	@Test
	public final void testGetCanonicalFile() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetCanonicalPath.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		File irodsCanonicalFile = irodsFile.getCanonicalFile();
		Assert.assertEquals("files", irodsCanonicalFile, irodsFile);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getCanonicalPath()}.
	 */
	@Test
	public final void testGetCanonicalPath() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetCanonicalPath.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String actualPath = irodsFile.getCanonicalPath();
		Assert.assertEquals("paths do not match", targetIrodsCollection + '/'
				+ testFileName, actualPath);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getName()}.
	 */
	@Test
	public final void testGetName() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetName.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String actualName = irodsFile.getName();
		Assert.assertEquals("names do not match", testFileName, actualName);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getParent()}.
	 */
	@Test
	public final void testGetParent() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetParent.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String actualParent = irodsFile.getParent();
		Assert.assertEquals("parent names do not match", targetIrodsCollection,
				actualParent);
	}

	/*
	 * [#353] getParent on folder beneath root should result in file '/'
	 */
	@Test
	public final void testGetParentWhenSubdirBelowRoot() throws Exception {

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile("/"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));
		String actualParent = irodsFile.getParent();
		Assert.assertNotNull("null parent, should have been root", actualParent);
		Assert.assertEquals("should have gotten root as parent", "/",
				actualParent);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#getParentFile()}.
	 */
	@Test
	public final void testGetParentFile() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetCanonicalPath.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		File irodsParentFile = irodsFile.getParentFile();
		Assert.assertEquals("files", irodsFile.getParent(),
				irodsParentFile.getAbsolutePath());
	}

	/**
	 * Test method for {@link org.irods.jargon.core.pub.io.IRODSFileImpl#list()}
	 * .
	 */
	@Test
	public final void testList() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] subdirs = irodsFile.list();
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.length > 0);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#list(java.io.FilenameFilter)}
	 * .
	 */
	@Test
	public final void testListFilenameFilter() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		String[] subdirs = irodsFile.list(new IRODSAcceptAllFileNameFilter());
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.length > 0);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#list(java.io.FilenameFilter)}
	 * .
	 */
	@Test
	public final void testListFileFilter() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] subdirs = irodsFile.listFiles(new IRODSAcceptAllFileFilter());
		Assert.assertNotNull(subdirs);
		Assert.assertTrue("no results", subdirs.length > 0);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#listFiles()}.
	 */
	@Test
	public final void testListFiles() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] irodsFiles = irodsFile.listFiles();
		Assert.assertNotNull(irodsFiles);
		Assert.assertTrue("no results", irodsFiles.length > 0);
		for (File irodsFile2 : irodsFiles) {
			Assert.assertTrue("this is not an instance of IRODSFileImpl",
					irodsFile2 instanceof IRODSFile);
		}
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#listFiles(java.io.FilenameFilter)}
	 * .
	 */
	@Test
	public final void testListFilesFilenameFilter() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, "");

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		File[] irodsFiles = irodsFile
				.listFiles(new IRODSAcceptAllFileNameFilter());
		Assert.assertNotNull(irodsFiles);
		Assert.assertTrue("no results", irodsFiles.length > 0);
		for (File irodsFile2 : irodsFiles) {
			Assert.assertTrue("this is not an instance of IRODSFileImpl",
					irodsFile2 instanceof IRODSFile);
		}
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#renameTo(java.io.File)}
	 * .
	 */
	@Test
	public final void testRenameToFileFile() throws Exception {
		String testFileName = "testRenameFileToFile.txt";
		String testRenamedFileName = "testRenamedFileToFile.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile irodsRenameFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/'
						+ testRenamedFileName);

		irodsFile.renameTo(irodsRenameFile);
		assertionHelper.assertIrodsFileOrCollectionDoesNotExist(
				irodsFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		assertionHelper.assertIrodsFileOrCollectionExists(
				irodsRenameFile.getAbsolutePath(),
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#renameTo(java.io.File)}
	 * .
	 */
	@Test
	public final void testRenameToFileFilePhyMove() throws Exception {
		String testFileName = "testRenameFileToFilePhyMove.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFile irodsRenameFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		irodsRenameFile
				.setResource(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));

		irodsFile.renameTo(irodsRenameFile);

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);

		DataObject dataObject = dataObjectAO
				.findByAbsolutePath(targetIrodsCollection + '/' + testFileName);

		Assert.assertEquals(
				"file is not in new resource",
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY),
				dataObject.getResourceName());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.io.IRODSFileImpl#toURI()}.
	 */
	@Test
	public final void testToURI() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testGetURI.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		String actualString = irodsFile.toString();
		String expectedString = "irods://" + irodsAccount.getUserName() + "@"
				+ irodsAccount.getHost() + ":" + irodsAccount.getPort()
				+ irodsFile.getAbsolutePath();
		Assert.assertEquals("to string does not match expected",
				expectedString, actualString);
	}

	@Test
	public final void testToString() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String testFileName = "testToString.txt";

		// now get an irods file and see if it is readable, it should be

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		URI uri = irodsFile.toURI();
		String actualHost = uri.getHost();
		int actualPort = uri.getPort();
		String actualPath = uri.getPath();
		Assert.assertEquals("host not equal", irodsAccount.getHost(),
				actualHost);
		Assert.assertEquals("port not equal", irodsAccount.getPort(),
				actualPort);
		Assert.assertEquals("path not equal", irodsFile.getAbsolutePath(),
				actualPath);
	}

	@Test
	public final void testGetResourceDirectory() throws Exception {
		String testDirName = "testGetResourceDirectory";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile dirFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		dirFile.mkdirs();

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testDirName);

		String resource = irodsFile.getResource();

		Assert.assertEquals("did not get expected resource for directory", "",
				resource);
	}

}
