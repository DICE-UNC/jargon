package org.irods.jargon.datautils.avuautocomplete;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AvuAutocompleteServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "AvuAutocompleteServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testGatherAvailableAttributesForCollNoPrefix() throws Exception {
		String testDirName = "testGatherAvailableAttributesForCollNoPrefix";
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile irodsFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory.getCollectionAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableAttributesForCollNoPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableAttributesForCollNoPrefix-testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		String expectedAttribName2 = "testGatherAvailableAttributesForCollNoPrefix-testmdattrib2";
		String expectedAttribValue2 = "testGatherAvailableAttributesForCollNoPrefix-testmdvalue2";

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now test

		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableAttributes("%", 0, AvuTypeEnum.COLLECTION);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	@Test
	public void testGatherAvailableAttributesForCollWithPrefix() throws Exception {
		String testDirName = "testGatherAvailableAttributesForCollWithPrefix";
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile irodsFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory.getCollectionAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableAttributesForCollWithPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableAttributesForCollWithPrefix-testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		String expectedAttribName2 = "testGatherAvailableAttributesForCollWithPrefix-testmdattrib2";
		String expectedAttribValue2 = "testGatherAvailableAttributesForCollWithPrefix-testmdvalue2";

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now test

		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableAttributes("testGatherAvailableAttributesForCollWithPrefix%", 0,
				AvuTypeEnum.COLLECTION);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

		for (String attrib : actual.getElements()) {
			Assert.assertTrue("did not find expected attrib prefix!", attrib.contains(testDirName));
		}

	}

}
