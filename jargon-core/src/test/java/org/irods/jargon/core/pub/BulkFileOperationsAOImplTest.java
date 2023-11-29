package org.irods.jargon.core.pub;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.ResourceHierarchyException;
import org.irods.jargon.core.packinstr.StructFileExtAndRegInp.BundleType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BulkFileOperationsAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "BulkFileOperationsAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testGetAOFromFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);
		Assert.assertNotNull("null bulkFileOperationsAO from factory", bulkFileOperationsAO);
	}

	@Test(expected = JargonException.class)
	public void testCreateBundleNoOverwriteCollectionExists() throws Exception {
		String tarName = "testCreateBundleNoOverwriteCollectionExists.tar";
		String testSubdir = "testCreateBundleNoOverwriteCollectionExists";
		String bunSubdir = "testCreateBundleNoOverwriteCollectionExistsBunSubdir";
		String fileName = "fileName";
		int count = 200;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 2.5
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.5")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");
		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBundleNoOverwriteCollectionExistsNullBun() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(null, "target", "");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBundleNoOverwriteCollectionExistsBlankBun() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods("", "target", "");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateBundleNoOverwriteCollectionExistsNullResc() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods("xxxx", "target", null);

	}

	@Test(expected = JargonException.class)
	public void testCreateBundleNoOverwriteCollectionDoesNotExist() throws Exception {
		String tarName = "testCreateBundleNoOverwriteCollectionDoesNotExist.tar";
		String testSubdir = "testCreateBundleNoOverwriteCollectionDoesNotExist";
		String bunSubdir = "testCreateBundleNoOverwriteCollectionDoesNotExistBunSubdir";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");
	}

	@Test(expected = JargonException.class)
	public void testCreateBundleWhenTarFileAlreadyExists() throws Exception {

		String tarName = "testCreateBundleWhenTarFileAlreadyExists.tar";
		String testSubdir = "testCreateBundleWhenTarFileAlreadyExists";
		String bunSubdir = "testCreateBundleWhenTarFileAlreadyExistsBunSubdir";
		String fileName = "fileName";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		// create the tar file with the same name as the one I will want to
		// create later
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunFileAbsPath);
		irodsFile.createNewFile();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

	}

	@Test
	// FIXME: right now just using tar, need to see about unhandled algos
	// see https://github.com/DICE-UNC/jargon/issues/114
	public void testCreateBundleZip() throws Exception {

		String tarName = "testCreateBundleZip.tar";
		String testSubdir = "testCreateBundleZip";
		String bunSubdir = "testCreateBundleZipSubdir";
		String fileName = "fileName";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// Skip if iRODS 4.3.0.
		Assume.assumeFalse("Bug in iRODS 4.3.0 discovered post release", irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSServerProperties(irodsAccount).isVersion("rods4.3.0"));

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		// create the tar file with the same name as the one I will want to
		// create later
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunFileAbsPath);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection, "",
				BundleType.TAR);

	}

	@Test
	public void testCreateBundleWhenTarFileAlreadyExistsForceSpecified() throws Exception {

		if (!testingPropertiesHelper.isTestParallelTransfer(testingProperties)) {
			return;
		}

		String tarName = "testCreateBundleWhenTarFileAlreadyExistsForceSpecified.tar";
		String testSubdir = "testCreateBundleWhenTarFileAlreadyExistsForceSpecified";
		String bunSubdir = "testCreateBundleWhenTarFileAlreadyExistsForceSpecifiedBunSubdir";
		String fileName = "fileName";
		int count = 20;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		// create the tar file with the same name as the one I will want to
		// create later
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunFileAbsPath);
		irodsFile.createNewFile();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(targetBunFileAbsPath,
				targetIrodsCollection, "");
		assertionHelper.assertIrodsFileOrCollectionExists(targetBunFileAbsPath,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

	}

	@Test
	public void testExtractBundleNoOverwriteNoBulk() throws Exception {
		String tarName = "testExtractBundleNoOverwriteNoBulk.tar";
		String testSubdir = "testExtractBundleNoOverwriteNoBulk";
		String bunSubdir = "testExtractBundleNoOverwriteNoBulkBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleNoOverwriteNoBulkExtractTargetCollection";

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		// Skip if iRODS 4.3.0.
		Assume.assumeFalse("Bug in iRODS 4.3.0 discovered post release", props.isVersion("rods4.3.0"));

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		bulkFileOperationsAO.extractABundleIntoAnIrodsCollection(targetBunFileAbsPath, targetIrodsCollection, "");

		File targetColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		File sourceColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(sourceColl, targetColl);

	}

	@Test(expected = JargonException.class)
	public void testExtractBundleNoOverwriteNoBulkWhenTargetCollectionAlreadyExists() throws Exception {
		// gets a SYS_COPY_ALREADY_IN_RESC -46000
		String tarName = "testExtractBundleNoOverwriteNoBulkWhenTargetCollectionAlreadyExists.tar";
		String testSubdir = "testExtractBundleNoOverwriteNoBulkWhenTargetCollectionAlreadyExists";
		String bunSubdir = "testExtractBundleNoOverwriteNoBulkWhenTargetCollectionAlreadyExistsBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleNoOverwriteNoBulkWhenTargetCollectionAlreadyExistsTargetCollection";

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		bulkFileOperationsAO.extractABundleIntoAnIrodsCollection(targetBunFileAbsPath, targetIrodsCollection, "");
		// repeat the same operation, causing an overwrite situation, should get
		// an error
		bulkFileOperationsAO.extractABundleIntoAnIrodsCollection(targetBunFileAbsPath, targetIrodsCollection, "");
	}

	@Test
	// https://github.com/irods/irods/issues/2323
	public void testExtractBundleWithOverwriteNoBulkWhenTargetCollectionAlreadyExists() throws Exception {
		String tarName = "testExtractBundleWithOverwriteNoBulkWhenTargetCollectionAlreadyExists.tar";
		String testSubdir = "testExtractBundleWithOverwriteNoBulkWhenTargetCollectionAlreadyExists";
		String bunSubdir = "testExtractBundleWithOverwriteNoBulkWhenTargetCollectionAlreadyExistsBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleWithOverwriteNoBulkWhenTargetCollectionAlreadyExistsTargetCollection";

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		// Skip if iRODS 4.3.0.
		Assume.assumeFalse("Bug in iRODS 4.3.0 discovered post release", props.isVersion("rods4.3.0"));

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		bulkFileOperationsAO.extractABundleIntoAnIrodsCollection(targetBunFileAbsPath, targetIrodsCollection, "");
		// repeat the same operation, causing an overwrite situation, should get
		// an error
		bulkFileOperationsAO.extractABundleIntoAnIrodsCollectionWithForceOption(targetBunFileAbsPath,
				targetIrodsCollection, "");

		File targetColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		File sourceColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(sourceColl, targetColl);
	}

	@Test
	public void testExtractBundleNoOverwriteWithBulk() throws Exception {
		String tarName = "testExtractBundleNoOverwriteWithBulk.tar";
		String testSubdir = "testExtractBundleNoOverwriteWithBulk";
		String bunSubdir = "testExtractBundleNoOverwriteWithBulkBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleNoOverwriteWithBulkTargetCollection";

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				"");

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		bulkFileOperationsAO.extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization(targetBunFileAbsPath,
				targetIrodsCollection, "");

		File targetColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		File sourceColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(sourceColl, targetColl);

	}

	@Test
	public void testExtractBundleNoOverwriteWithBulkSpecifyResource() throws Exception {
		String tarName = "testExtractBundleNoOverwriteWithBulkSpecifyResource.tar";
		String testSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyResource";
		String bunSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyResourceBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyResourceTargetCollection";
		String testResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				testResource);

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		IRODSFile extractSubdir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		extractSubdir.mkdirs();
		extractSubdir.close();

		bulkFileOperationsAO.extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization(targetBunFileAbsPath,
				targetIrodsCollection, testResource);

		File targetColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		File sourceColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(sourceColl, targetColl);

	}

	@Ignore
	// (expected = DataNotFoundException.class)
	public void testExtractBundleNoOverwriteWithBulkSpecifyWrongResource() throws Exception {
		String tarName = "testExtractBundleNoOverwriteWithBulkSpecifyWrongResource.tar";
		String testSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyWrongResource";
		String bunSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyWrongResourceBunSubdir";
		String testExtractTargetSubdir = "testExtractBundleNoOverwriteWithBulkSpecifyWrongResourceTargetCollection";
		String testResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String fileName = "fileName.txt";
		int count = 5;

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile irodsFile = null;

		String targetBunIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + bunSubdir);
		String targetBunFileAbsPath = targetBunIrodsCollection + "/" + tarName;
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetBunIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdir();
		irodsFile.close();

		String myTarget = "";

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i) + fileName;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
			irodsFile.close();
		}

		BulkFileOperationsAO bulkFileOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getBulkFileOperationsAO(irodsAccount);

		bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(targetBunFileAbsPath, targetIrodsCollection,
				testResource);

		// extract the bun file now to a different subdir
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testExtractTargetSubdir);

		IRODSFile extractSubdir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		extractSubdir.mkdirs();
		extractSubdir.close();

		try {
			bulkFileOperationsAO.extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization(targetBunFileAbsPath,
					targetIrodsCollection,
					testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY) + "x");
		} catch (ResourceHierarchyException e) {
			// expected when 4.1+
			throw new DataNotFoundException(e);
		}

		File targetColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);
		File sourceColl = (File) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		assertionHelper.assertTwoFilesAreEqualByRecursiveTreeComparison(sourceColl, targetColl);

	}

}
