package org.irods.jargon.datautils.filearchive;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalTarFileArchiverTest {

	private static Properties testingProperties = new Properties();
	private static JargonProperties jargonOriginalProperties = null;
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "LocalTarFileArchiverTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		jargonOriginalProperties = settableJargonProperties;
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

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		// be sure that normal parallel stuff is set up
		irodsFileSystem.getIrodsSession().setJargonProperties(
				jargonOriginalProperties);
	}

	@Test
	public void testTarUpCollection() throws Exception {

		String rootCollection = "testTarUpCollection";
		String targetTarFile = "targetTarFile.tar";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String tarParentCollection = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		File tarFile = new File(tarParentCollection, targetTarFile);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath, rootCollection, 2, 3, 2,
						"testFile", ".txt", 3, 2, 1, 200 * 1024);

		LocalTarFileArchiver archiver = new LocalTarFileArchiver(
				localCollectionAbsolutePath, tarFile.getAbsolutePath());

		File tarredFile = archiver.createArchive();
		Assert.assertNotNull("null tarFile returned", tarredFile);
		Assert.assertTrue("tar file not exists", tarredFile.exists());

	}

}
