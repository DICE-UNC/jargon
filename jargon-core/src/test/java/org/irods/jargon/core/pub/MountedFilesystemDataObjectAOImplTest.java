package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.InvalidArgumentException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedFilesystemDataObjectAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemDataObjectAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testChecksum() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testPutOneFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024);
		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		String computedChecksum = dataObjectAO
				.computeMD5ChecksumOnDataObject(testFile);
		Assert.assertTrue("did not return a checksum",
				computedChecksum.length() > 0);
	}

	@Test(expected = InvalidArgumentException.class)
	public final void testSetWrite() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		// generate a local scratch file
		String testFileName = "testSetWrite.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024);
		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO
				.setAccessPermissionWrite(
						"",
						targetIrodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsFile);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public void testAddAVUMetadataToDataObject() throws Exception {
		String testFileName = "mtdtestAddAVUMetadataToDataObject.txt";
		String expectedAttribName = "mtdtestAddAVUMetadataToDataObject";
		String expectedValueName = "mtdtestval1";

		// generate a local scratch file
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024);
		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile unmountFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		unmountFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");

		dataObjectAO.addAVUMetadata(targetIrodsFile, avuData);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO
				.findDomainByMetadataQuery(avuQueryElements);
		Assert.assertTrue(dataObjects.size() >= 1);
	}

}
