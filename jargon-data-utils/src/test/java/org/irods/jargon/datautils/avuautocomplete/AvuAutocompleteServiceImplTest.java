package org.irods.jargon.datautils.avuautocomplete;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.After;
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

	@After
	public void afterEach() throws Exception {
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

	@Test
	public void testGatherAvailableAttributesForDataObjNoPrefix() throws Exception {
		String testFileName = "testGatherAvailableAttributesForDataObjNoPrefix.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableAttributesForDataObjNoPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableAttributesForDataObjNoPrefix-testmdvalue1";
		String expectedAttribUnits = "testGatherAvailableAttributesForDataObjNoPrefix-testmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now test
		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableAttributes("%", 0, AvuTypeEnum.DATA_OBJECT);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	@Test
	public void testGatherAvailableAttributesForDataObjWithPrefix() throws Exception {
		String testFileName = "testGatherAvailableAttributesForDataObjWithPrefix.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableAttributesForDataObjWithPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableAttributesForDataObjWithPrefix-testmdvalue1";
		String expectedAttribUnits = "testGatherAvailableAttributesForDataObjWithPrefix-testmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now test
		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableAttributes("testGatherAvailableAttributesForDataObjWithPrefix%",
				0, AvuTypeEnum.DATA_OBJECT);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	/*
	 * @Test public void testGatherAvailableAttributesForBothNoPrefix() throws
	 * Exception { IRODSAccount irodsAccount =
	 * testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties
	 * ); IRODSAccessObjectFactory accessObjectFactory =
	 * irodsFileSystem.getIRODSAccessObjectFactory();
	 *
	 * // Test instance for collection String testDirName =
	 * "testGatherAvailableAttributesForBothCollNoPrefix"; String
	 * targetIrodsCollection =
	 * testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
	 * testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);
	 *
	 * // Adding collection IRODSFile irodsFile =
	 * accessObjectFactory.getIRODSFileFactory(irodsAccount)
	 * .instanceIRODSFile(targetIrodsCollection); irodsFile.mkdirs();
	 *
	 * CollectionAO collectionAO =
	 * accessObjectFactory.getCollectionAO(irodsAccount);
	 *
	 * // initialize the collection AVU data String expectedAttribName =
	 * "testGatherAvailableAttributesForBothCollNoPrefix-testmdattrib1"; String
	 * expectedAttribValue =
	 * "testGatherAvailableAttributesForBothCollNoPrefix-testmdvalue1"; String
	 * expectedAttribUnits =
	 * "testGatherAvailableAttributesForBothCollNoPrefix-test1mdunits";
	 *
	 * AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue,
	 * expectedAttribUnits); collectionAO.deleteAVUMetadata(targetIrodsCollection,
	 * avuData); collectionAO.addAVUMetadata(targetIrodsCollection, avuData);
	 *
	 * // Test instance for DataObj String testFileName =
	 * "testGatherAvailableAttributesForDataObjWithPrefix.dat"; String absPath =
	 * scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
	 * String localFileName =
	 * FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);
	 *
	 * // Adding DataObj String dataObjectAbsPath = targetIrodsCollection + '/' +
	 * testFileName; DataTransferOperations dto =
	 * irodsFileSystem.getIRODSAccessObjectFactory()
	 * .getDataTransferOperations(irodsAccount); dto.putOperation(localFileName,
	 * targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);
	 *
	 * DataObjectAO dataObjectAO =
	 * irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
	 *
	 * // initialize the AVU data String expectedAttribName2 =
	 * "testGatherAvailableAttributesForBothDataObjNoPrefix-testmdattrib1"; String
	 * expectedAttribValue2 =
	 * "testGatherAvailableAttributesForBothDataObjNoPrefix-testmdvalue1"; String
	 * expectedAttribUnits2 =
	 * "testGatherAvailableAttributesForBothDataObjNoPrefix-testmdunits";
	 *
	 * avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2,
	 * expectedAttribUnits2);
	 *
	 * dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
	 * dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);
	 *
	 * // now test AvuAutocompleteService service = new
	 * AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
	 * irodsAccount); AvuSearchResult actual =
	 * service.gatherAvailableAttributes("%", 0, AvuTypeEnum.BOTH);
	 * Assert.assertNotNull("null result returned", actual); }
	 */

	@Test
	public void testGatherAvailableValuesForCollNoPrefix() throws Exception {
		String testDirName = "testGatherAvailableValuesForCollNoPrefix";
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile irodsFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory.getCollectionAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableValuesForCollNoPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableValuesForCollNoPrefix-testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		String expectedAttribName2 = "testGatherAvailableValuesForCollNoPrefix-testmdattrib1";
		String expectedAttribValue2 = "testGatherAvailableValuesForCollNoPrefix-testmdvalue2";

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now test

		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableValues("testGatherAvailableValuesForCollNoPrefix-testmdattrib1",
				"%", 0, AvuTypeEnum.COLLECTION);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	@Test
	public void testGatherAvailableValuesForCollWithPrefix() throws Exception {
		String testDirName = "testGatherAvailableValuesForCollWithPrefix";
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile irodsFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory.getCollectionAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableValuesForCollWithPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableValuesForCollWithPrefix-testmdvalue1";
		String expectedAttribUnits = "testGatherAvailableValuesForCollWithPrefix-test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		String expectedAttribName2 = "testGatherAvailableValuesForCollWithPrefix-testmdattrib1";
		String expectedAttribValue2 = "testGatherAvailableValuesForCollWithPrefix-testmdvalue2";

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now test

		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableValues(
				"testGatherAvailableValuesForCollWithPrefix-testmdattrib1",
				"testGatherAvailableValuesForCollWithPrefix%", 0, AvuTypeEnum.COLLECTION);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	@Test
	public void testGatherAvailableValuesForDataObjNoPrefix() throws Exception {
		String testFileName = "testGatherAvailableValuesForDataObjNoPrefix.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableValuesForDataObjNoPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableValuesForDataObjNoPrefix-testmdvalue1";
		String expectedAttribUnits = "testGatherAvailableValuesForDataObjNoPrefix-testmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now test
		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableValues(
				"testGatherAvailableValuesForDataObjNoPrefix-testmdattrib1", "%", 0, AvuTypeEnum.DATA_OBJECT);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}

	@Test
	public void testGatherAvailableValuesForDataObjWithPrefix() throws Exception {
		String testFileName = "testGatherAvailableValuesForDataObjWithPrefix.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testGatherAvailableValuesForDataObjWithPrefix-testmdattrib1";
		String expectedAttribValue = "testGatherAvailableValuesForDataObjWithPrefix-testmdvalue1";
		String expectedAttribUnits = "testGatherAvailableValuesForDataObjWithPrefix-testmdunits";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		// now test
		AvuAutocompleteService service = new AvuAutocompleteServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		AvuSearchResult actual = service.gatherAvailableValues(
				"testGatherAvailableValuesForDataObjWithPrefix-testmdattrib1",
				"testGatherAvailableValuesForDataObjWithPrefix%", 0, AvuTypeEnum.DATA_OBJECT);
		Assert.assertNotNull("null result returned", actual);
		Assert.assertFalse("no results", actual.getElements().isEmpty());

	}
}
