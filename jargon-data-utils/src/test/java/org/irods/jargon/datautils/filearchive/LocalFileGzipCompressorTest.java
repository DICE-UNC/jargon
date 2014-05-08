package org.irods.jargon.datautils.filearchive;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileGzipCompressorTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "LocalFileGzipCompressorTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		irodsFileSystem.getIrodsSession().setJargonProperties(
				settableJargonProperties);
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
		new org.irods.jargon.testutils.AssertionHelper();
	}

	@Test
	public void testUnzipAGzipFile() throws Exception {

		String rootCollection = "testUnzipAGzipFile";
		String targetTarFile = "testUnzipAGzipFile.tar";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String tarParentCollection = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File tarFile = new File(tarParentCollection, targetTarFile);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 200);

		LocalTarFileArchiver archiver = new LocalTarFileArchiver(
				localCollectionAbsolutePath, tarFile.getAbsolutePath());

		File tarredFile = archiver.createArchive();

		LocalFileGzipCompressor localFileGzipCompressor = new LocalFileGzipCompressor();
		File zippedFile = localFileGzipCompressor.compress(tarredFile
				.getAbsolutePath());

		tarredFile.delete();
		File unzippedFile = localFileGzipCompressor.uncompress(zippedFile
				.getAbsolutePath());

		Assert.assertNotNull("no file", unzippedFile);
		Assert.assertTrue("unzippedFile does not exist", unzippedFile.exists());
		Assert.assertEquals("name should have .tar", targetTarFile,
				unzippedFile.getName());

	}

	@Test
	public void testTarAndGzip() throws Exception {

		String rootCollection = "testTarAndGzip";
		String targetTarFile = "testTarAndGzip.tar";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String tarParentCollection = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File tarFile = new File(tarParentCollection, targetTarFile);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 200);

		LocalTarFileArchiver archiver = new LocalTarFileArchiver(
				localCollectionAbsolutePath, tarFile.getAbsolutePath());

		File tarredFile = archiver.createArchive();

		LocalFileGzipCompressor localFileGzipCompressor = new LocalFileGzipCompressor();
		File zippedFile = localFileGzipCompressor.compress(tarredFile
				.getAbsolutePath());

		Assert.assertNotNull("no file", zippedFile);
		Assert.assertTrue("compressed file does not exist", zippedFile.exists());
		Assert.assertEquals("name should have .gzip appended", targetTarFile
				+ ".gzip", zippedFile.getName());

	}
}
