package org.irods.jargon.zipservice.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JargonZipServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "JargonZipServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static JargonProperties jargonOriginalProperties;

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
		irodsFileSystem = IRODSFileSystem.instance();
		jargonOriginalProperties = irodsFileSystem.getJargonProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		irodsFileSystem.getIrodsSession().setJargonProperties(
				jargonOriginalProperties);
	}

	@Test
	public void testObtainBundleAsIrodsFileGivenPaths() throws Exception {

		String rootCollection = "testObtainBundleAsIrodsFileGivenPaths";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 2, 2, "testFile",
						".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		ZipServiceConfiguration zipServiceConfiguration = new ZipServiceConfiguration();
		JargonZipService jargonZipService = new JargonZipServiceImpl(
				zipServiceConfiguration,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		List<String> paths = new ArrayList<String>();
		paths.add(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		IRODSFile bundle = jargonZipService
				.obtainBundleAsIrodsFileGivenPaths(paths);
		Assert.assertTrue("did not return bundle", bundle.exists());

	}

	@Test
	public void testObtainBundleAsInputStreamWithAdditionalMetadataGivenPaths()
			throws Exception {

		String rootCollection = "testObtainBundleAsInputStreamGivenPaths";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 2, 2, "testFile",
						".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		ZipServiceConfiguration zipServiceConfiguration = new ZipServiceConfiguration();
		JargonZipService jargonZipService = new JargonZipServiceImpl(
				zipServiceConfiguration,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		List<String> paths = new ArrayList<String>();
		paths.add(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		BundleStreamWrapper wrapper = jargonZipService
				.obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths(paths);
		Assert.assertNotNull("did not return bundle", wrapper);
		Assert.assertNotNull("no input stream", wrapper.getInputStream());
		Assert.assertFalse("length incorrect", wrapper.getLength() == 0);
		Assert.assertFalse("missing name", wrapper.getBundleFileName()
				.isEmpty());

	}

	@Test
	public void testObtainBundleAsInputStreamGivenPaths() throws Exception {

		String rootCollection = "testObtainBundleAsInputStreamGivenPaths";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 2, 2, "testFile",
						".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		ZipServiceConfiguration zipServiceConfiguration = new ZipServiceConfiguration();
		JargonZipService jargonZipService = new JargonZipServiceImpl(
				zipServiceConfiguration,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		List<String> paths = new ArrayList<String>();
		paths.add(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		InputStream bundle = jargonZipService
				.obtainBundleAsInputStreamGivenPaths(paths);
		Assert.assertNotNull("did not return bundle", bundle);
		bundle.close();
		// check for no errors

	}

	@Test
	public void testComputeBundleSizeInBytes() throws Exception {

		String rootCollection = "testComputeBundleSizeInBytes";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 2, 2, "testFile",
						".txt", 3, 2, 20, 200);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		ZipServiceConfiguration zipServiceConfiguration = new ZipServiceConfiguration();
		JargonZipService jargonZipService = new JargonZipServiceImpl(
				zipServiceConfiguration,
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		List<String> paths = new ArrayList<String>();
		paths.add(irodsCollectionRootAbsolutePath + "/" + rootCollection);

		long actual = jargonZipService.computeBundleSizeInBytes(paths);
		Assert.assertTrue("no file count found", actual > 0);
	}

}
