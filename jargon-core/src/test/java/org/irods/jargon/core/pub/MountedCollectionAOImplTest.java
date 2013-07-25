package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
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
	 * Unmount a soft link that does not exist
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testUnmountSoftLinkNotExists() throws Exception {
		String targetCollectionName = "testUnmountSoftLinkNotExists";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);
		boolean success = mountedCollectionAO.unmountACollection(
				targetIrodsCollection, "");
		Assert.assertFalse("should get no success", success);

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
		String subfileName = "testCreateASoftLink.txt";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();
		IRODSFile subFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, subfileName);
		subFile.createNewFile();

		// add a subfile to this collection

		// create the soft link

		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue("target collection does not exist",
				mountedCollectionTargetFile.exists());
		String softLinkedSourceFileName = mountedCollectionTargetFile
				.getAbsolutePath() + "/" + subFile.getName();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat statForSoftLinkedFile = listAndSearchAO
				.retrieveObjectStatForPath(softLinkedSourceFileName);

		Assert.assertEquals("did not set the objPath", targetIrodsCollection,
				statForSoftLinkedFile.getCollectionPath());
		Assert.assertEquals("did not identify as a linked coll",
				ObjStat.SpecColType.LINKED_COLL,
				statForSoftLinkedFile.getSpecColType());

		Assert.assertTrue("did not get the soft linked file",
				statForSoftLinkedFile.getObjectType() == ObjectType.DATA_OBJECT);

	}

	/**
	 * Create a soft link to an iRODS collection twice
	 * 
	 * @throws Exception
	 */
	@Test(expected = CollectionNotEmptyException.class)
	public final void testCreateASoftLinkTwice() throws Exception {

		String sourceCollectionName = "testCreateASoftLinkTwiceSource";
		String targetCollectionName = "testCreateASoftLinkTwiceTarget";
		String subfileName = "testCreateASoftLinkTwice.txt";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();
		IRODSFile subFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, subfileName);
		subFile.createNewFile();

		// add a subfile to this collection

		// create the soft link twice

		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue("target collection does not exist",
				mountedCollectionTargetFile.exists());
		String softLinkedSourceFileName = mountedCollectionTargetFile
				.getAbsolutePath() + "/" + subFile.getName();

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		ObjStat statForSoftLinkedFile = listAndSearchAO
				.retrieveObjectStatForPath(softLinkedSourceFileName);

		Assert.assertTrue("did not get the soft linked file",
				statForSoftLinkedFile.getObjectType() == ObjectType.DATA_OBJECT);

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

	@Test
	public void testCreateAndRemoveMountedFileSystem() throws Exception {

		String targetCollectionName = "testCreateAndRemoveMountedFileSystem";
		String localMountDir = "testCreateAndRemoveMountedFileSystemLocal";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localMountDir);

		FileGenerator.generateManyFilesInParentCollectionByAbsolutePath(
				localCollectionAbsolutePath,
				"testCreateAndRemoveMountedFileSystem", ".txt", 10, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// FIXME: right now no errors means success. will test further in
		// listing methods

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemBlankResource()
			throws Exception {

		String targetCollectionName = "testCreateAndRemoveMountedFileSystem";
		String localMountDir = "testCreateAndRemoveMountedFileSystemLocal";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localMountDir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection, "");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemBlankSource()
			throws Exception {

		String targetCollectionName = "testCreateAndRemoveMountedFileSystem";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection("",
				targetIrodsCollection, "");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemNullSource()
			throws Exception {

		String targetCollectionName = "testCreateAndRemoveMountedFileSystem";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection(null,
				targetIrodsCollection, "");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemBlankTarget()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection("source", "",
				"resc");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemNullTarget()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection("source", null,
				"resc");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateAndRemoveMountedFileSystemNullResource()
			throws Exception {

		String targetCollectionName = "testCreateAndRemoveMountedFileSystem";
		String localMountDir = "testCreateAndRemoveMountedFileSystemLocal";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + localMountDir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection, null);

	}

}
