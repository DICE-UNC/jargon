package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedCollectionAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedCollectionAOImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Create a soft link to an iRODS collection in nominal mode, target does
	 * not exist and will be created
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateASoftLink() throws Exception {

		String sourceCollectionName = "testCreateASoftLinkSource";
		String targetCollectionName = "testCreateASoftLinkTarget";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue("target collection does not exist",
				mountedCollectionTargetFile.exists());

	}

	/**
	 * create a soft link where the source file does not exist
	 * 
	 * @throws Exception
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testCreateASoftLinkSourceNotExists() throws Exception {

		String sourceCollectionName = "testCreateASoftLinkSourceNotExistsSource";
		String targetCollectionName = "testCreateASoftLinkSourceNotExistsTarget";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// create the soft link

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

	}

	/**
	 * create a soft link where the source file is a data object
	 * 
	 * @throws Exception
	 */
	@Test(expected = JargonException.class)
	public final void testCreateASoftLinkSourceIsIRODSFile() throws Exception {

		String sourceCollectionName = "testCreateASoftLinkSourceIsIRODSFile.txt";
		String targetCollectionName = "testCreateASoftLinkSourceNotExistsTarget";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String sourceIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSFile sourceAsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(sourceIrodsFile);
		sourceAsFile.createNewFile();

		// create the soft link

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink(sourceIrodsFile,
				targetIrodsCollection);

	}

	/**
	 * create a soft link where the source file is null
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCreateASoftLinkSourceIsNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink(null, "hello");

	}

	/**
	 * create a soft link where the source file is blank
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCreateASoftLinkSourceIsBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink("", "hello");

	}

	/**
	 * create a soft link where the target file is null
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCreateASoftLinkTargetIsNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink("hello", null);

	}

	/**
	 * create a soft link where the target file is blank
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCreateASoftLinkTargetIsBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		mountedCollectionAO.createASoftLink("hello", "");

	}

}
