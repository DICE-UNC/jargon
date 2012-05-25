package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjectAOImplForSoftLinkTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataObjectAOImplForSoftLinkTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
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
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testFindByCollectionPathAndDataNameWhenSoftLink()
			throws Exception {
		String sourceCollectionName = "testFindByCollectionPathAndDataNameWhenSoftLinkSource";
		String targetCollectionName = "testFindByCollectionPathAndDataNameWhenSoftLinkTarget";
		String testFileName = "test.txt";

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

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(sourceIrodsCollection, testFileName);
		irodsFile.createNewFile();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getDataObjectAO(irodsAccount);

		// find by the soft link path

		DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(
				targetIrodsCollection, testFileName);
		TestCase.assertNotNull("did not find data object by soft link name",
				dataObject);
		TestCase.assertEquals("should have the requested col name",
				targetIrodsCollection, dataObject.getCollectionName());
		TestCase.assertEquals("shold reflect the canonical col in objPath",
				sourceIrodsCollection, dataObject.getObjectPath());
		TestCase.assertEquals("should be a special coll",
				SpecColType.LINKED_COLL, dataObject.getSpecColType());

	}


}
