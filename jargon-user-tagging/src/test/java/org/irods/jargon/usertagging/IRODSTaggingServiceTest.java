package org.irods.jargon.usertagging;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.usertagging.domain.IRODSDescriptionValue;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class IRODSTaggingServiceTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSTaggingServiceTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
private static IRODSFileSystem irodsFileSystem = null;
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

	@Test
	public final void testInstance() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		TestCase.assertNotNull(irodsTaggingService);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullAccessObjectFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSTaggingServiceImpl.instance(null, irodsAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNullAccount() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSTaggingServiceImpl.instance(irodsAccessObjectFactory, null);
	}

	@Test
	public final void testAddTagToLiveDataObject() throws Exception {
		String testFileName = "testAddTagToLiveDataObject.txt";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName, irodsAccount.getUserName());
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}

	@Test
	public final void testRemoveTagFromLiveDataObject() throws Exception {
		String testFileName = "testRemoveTagFromLiveDataObject.txt";
		String expectedTagName = "testRemoveTagFromLiveDataObject";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.deleteTagFromDataObject(targetIrodsDataObject,
				irodsTagValue);
		
		TestCase.assertEquals(0, irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject).size());

	}

	@Test
	public final void testQueryThreeTagsFromLiveDataObject() throws Exception {
		String testFileName = "testQueryThreeTagsFromLiveDataObject.doc";
		String expectedAttribName1 = "testQueryThreeTagsFromLiveDataObject1";
		String expectedAttribName2 = "testQueryThreeTagsFromLiveDataObject2";
		String expectedAttribName3 = "testQueryThreeTagsFromLiveDataObject3";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName1, irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedAttribName2, irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedAttribName3, irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		List<IRODSTagValue> queryResultValues = irodsTaggingService
				.getTagsOnDataObject(targetIrodsDataObject);

		TestCase.assertEquals("should have returned the three tags added", 3,
				queryResultValues.size());

		IRODSTagValue testTag1 = queryResultValues.get(0);
		TestCase.assertEquals("tag value is not what was expected",
				expectedAttribName1, testTag1.getTagData());

	}
	
	@Test
	public final void testAddTagToLiveCollection() throws Exception {
		
		String testCollection = "testAddTagToLiveCollection";
		String expectedTagName = "testAddTagToLiveCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		
		TestCase.assertTrue(true);
		// looking for no errors here, other tests query data back and validate..
		
	}
	
	@Test(expected=JargonException.class)
	public final void testAddTagToLiveCollectionTwice() throws Exception {
		
		String testCollection = "testAddTagToLiveCollectionTwice";
		String expectedTagName = "testAddTagToLiveCollectionTwice";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
	
	}
	
	@Test
	public final void testQueryTwoTagsOnLiveCollection() throws Exception {
		
		String testCollection = "testQueryTwoTagsOnLiveCollection";

		String expectedTagName1 = "testQueryTwoTagsOnLiveCollection1";
		String expectedTagName2 = "testQueryTwoTagsOnLiveCollection2";


		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
		.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1, irodsAccount.getUserName());		
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		
		 irodsTagValue = new IRODSTagValue(expectedTagName2, irodsAccount.getUserName());		
			irodsTaggingService.addTagToCollection(targetIrodsCollection,
					irodsTagValue);
		
		List<IRODSTagValue> irodsTagValues = irodsTaggingService.getTagsOnCollection(targetIrodsCollection);
			
		
		TestCase.assertEquals("should have returned the two tags added", 2,
				irodsTagValues.size());

		IRODSTagValue testTag1 = irodsTagValues.get(0);
		TestCase.assertEquals("tag value is not what was expected",
				expectedTagName1, testTag1.getTagData());
		TestCase.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag1.getTagUser());
		IRODSTagValue testTag2 = irodsTagValues.get(1);
		TestCase.assertEquals("tag value is not what was expected",
				expectedTagName2, testTag2.getTagData());
		TestCase.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag2.getTagUser());
	}
	
	@Test
	public final void testAddTwoTagsToACollectionThenDeleteOneTag() throws Exception {
		
		String testCollection = "testAddTwoTagsToACollectionThenDeleteOneTag";

		String expectedTagName1 = "testQueryTwoTagsOnLiveCollection1";
		String expectedTagName2 = "testQueryTwoTagsOnLiveCollection2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
		.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1, irodsAccount.getUserName());		
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		
		 irodsTagValue = new IRODSTagValue(expectedTagName2, irodsAccount.getUserName());		
			irodsTaggingService.addTagToCollection(targetIrodsCollection,
					irodsTagValue);
			
		// now delete one of the tags (the second one)
			
		irodsTaggingService.deleteTagFromCollection(targetIrodsCollection, irodsTagValue);
			
		List<IRODSTagValue> irodsTagValues = irodsTaggingService.getTagsOnCollection(targetIrodsCollection);
					
		TestCase.assertEquals("should have returned only the first added tag", 1,
				irodsTagValues.size());

		IRODSTagValue testTag1 = irodsTagValues.get(0);
		TestCase.assertEquals("tag value is not what was expected",
				expectedTagName1, testTag1.getTagData());
		TestCase.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag1.getTagUser());
		
	}
	
	@Test
	public final void testGetTagsBasedOnMetadataDomainCollection() throws Exception {
		
		String testCollection = "testGetTagsBasedOnMetadataDomainCollection";

		String expectedTagName1 = "testGetTagsBasedOnMetadataDomainCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
		.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1, irodsAccount.getUserName());		
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		
		
		List<IRODSTagValue> irodsTagValues = irodsTaggingService.getTagsBasedOnMetadataDomain(MetadataDomain.COLLECTION, targetIrodsCollection);
								
		TestCase.assertEquals("should have returned the tag added", 1,
				irodsTagValues.size());

	}
	
	@Test(expected=JargonException.class)
	public final void testGetTagsBasedOnMetadataDomainNotSuported() throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
		.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		
		irodsTaggingService.getTagsBasedOnMetadataDomain(MetadataDomain.RESOURCE, "test");
		
	}
	
	@Test
	public final void testRemoveTagFromLiveDataObjectByDomain() throws Exception {
		String testFileName = "testRemoveTagFromLiveDataObjectByDomain.txt";
		String expectedTagName = "testRemoveTagFromLiveDataObjectByDomain";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue, MetadataDomain.DATA, targetIrodsDataObject);
		
		TestCase.assertEquals(0, irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject).size());

	}
	
	@Test
	public final void testRemoveTagFromLiveCollectionByDomain() throws Exception {
		
		String testCollection = "testRemoveTagFromLiveCollectionByDomain";
		String expectedTagName = "testRemoveTagFromLiveCollectionByDomain";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		
		targetIrodsFile.mkdirs();
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);
		
		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue, MetadataDomain.COLLECTION, targetIrodsCollection);
		
		List<IRODSTagValue> irodsTagValues = irodsTaggingService.getTagsOnCollection(targetIrodsCollection);
		
		TestCase.assertTrue("tag should not be in collection", irodsTagValues.isEmpty());
		
	}
	
	@Test(expected=JargonException.class)
	public final void testRemoveTagFromLiveCollectionNotExists() throws Exception {
		
		String testCollection = "testRemoveTagFromLiveCollectionNotExists";
		String expectedTagName = "testRemoveTagFromLiveCollectionNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName, irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		
		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue, MetadataDomain.COLLECTION, targetIrodsCollection);
		
	}
	
	@Test
	public final void testGetTagsFromLiveCollectionNotExists() throws Exception {
		
		String testCollection = "testRemoveTagFromLiveCollectionNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);		
	
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		
		List<IRODSTagValue> irodsTagValues = irodsTaggingService.getTagsBasedOnMetadataDomain(MetadataDomain.COLLECTION, targetIrodsCollection);
		TestCase.assertTrue("should have returned empty collection", irodsTagValues.isEmpty());
		
	}

	
	@Test
	public final void testAddDescriptionToLiveDataObject() throws Exception {
		String testFileName = "testAddDescriptionToLiveDataObject.txt";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);
		
		IRODSDescriptionValue irodsTagValue = new IRODSDescriptionValue(expectedAttribName, irodsAccount.getUserName());
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}
	
	@Test(expected=DuplicateDataException.class)
	public final void testAddDuplicateDescriptionToLiveDataObject() throws Exception {
		String testFileName = "testAddDuplicateDescriptionToLiveDataObject.txt";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);
		
		IRODSDescriptionValue irodsTagValue = new IRODSDescriptionValue(expectedAttribName, irodsAccount.getUserName());
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}
	
	@Test(expected=DataNotFoundException.class)
	public final void testAddDescriptionToMissingDataObject() throws Exception {
		String testFileName = "testAddDescriptionToMissingDataObject.txt";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSDescriptionValue irodsTagValue = new IRODSDescriptionValue(expectedAttribName, irodsAccount.getUserName());
		
		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}
	
	@Test
	public final void testRemoveDescriptionFromLiveDataObject() throws Exception {
		String testFileName = "testRemoveDescriptionFromLiveDataObject.txt";
		String expectedTagName = "testRemoveDescriptionFromLiveDataObject";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSDescriptionValue irodsTagValue = new IRODSDescriptionValue(expectedTagName, irodsAccount.getUserName());

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.deleteDescriptionFromDataObject(targetIrodsDataObject,
				irodsTagValue);
		
		TestCase.assertEquals(0, irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject).size());

	}
	
	@Test
	public final void testRemoveDescriptionFromMissingDataObject() throws Exception {
		String testFileName = "testRemoveDescriptionFromMissingDataObject.txt";
		String expectedTagName = "testRemoveDescriptionFromMissingDataObject";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSDescriptionValue irodsTagValue = new IRODSDescriptionValue(expectedTagName, irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
	
		// non-existing description should silently fail in delete
		irodsTaggingService.deleteDescriptionFromDataObject(targetIrodsDataObject,
				irodsTagValue);
		
		TestCase.assertEquals(0, irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject).size());

	}


}
