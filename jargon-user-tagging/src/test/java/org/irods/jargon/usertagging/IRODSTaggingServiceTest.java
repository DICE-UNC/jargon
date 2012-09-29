package org.irods.jargon.usertagging;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
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
		Assert.assertNotNull(irodsTaggingService);

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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

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

		Assert.assertEquals(0,
				irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject)
						.size());

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
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName1,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedAttribName2,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedAttribName3,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToDataObject(targetIrodsDataObject,
				irodsTagValue);

		List<IRODSTagValue> queryResultValues = irodsTaggingService
				.getTagsOnDataObject(targetIrodsDataObject);

		Assert.assertEquals("should have returned the three tags added", 3,
				queryResultValues.size());

	}

	@Test
	public final void testAddTagToLiveCollection() throws Exception {

		String testCollection = "testAddTagToLiveCollection";
		String expectedTagName = "testAddTagToLiveCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		Assert.assertTrue(true);
		// looking for no errors here, other tests query data back and
		// validate..

	}

	@Test
	public final void testAddDescriptionCollectionThenAddBlankToDelete()
			throws Exception {

		String testCollection = "testAddDescriptionCollectionThenAddBlankToDelete";
		String expectedTagName = "testAddDescriptionCollectionThenAddBlankToDelete";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);
		irodsTagValue = new IRODSTagValue("", irodsAccount.getUserName());
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		IRODSTagValue actualDescription = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNull("description should have been deleted",
				actualDescription);

	}

	@Test
	public final void testAddDescriptionToCollectionTwice() throws Exception {

		String testCollection = "testAddDescriptionToCollectionTwice";
		String expectedTagName = "testAddDescriptionToCollectionTwice";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		IRODSTagValue actualDescription = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNotNull("description should be present", actualDescription);

	}

	@Test
	public final void testAddDescriptionToLiveCollection() throws Exception {

		String testCollection = "testAddDescriptionToLiveCollection";
		String expectedTagName = "testAddDescriptionToLiveCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		IRODSTagValue actualDescription = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNotNull("null IRODSTagValue, no description was added");
		Assert.assertEquals("description not found in tag", expectedTagName,
				actualDescription.getTagData());

	}

	@Test
	public final void testAddDescriptionToLiveCollectionBlankDescriptionNoPrior()
			throws Exception {

		String testCollection = "testAddDescriptionToLiveCollectionBlankDescriptionNoPrior";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue("",
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		IRODSTagValue actual = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNull("should not be a description", actual);
	}

	@Test
	public final void testGetDescriptionOnLiveCollectionCollectionExistsNoDescription()
			throws Exception {

		String testCollection = "testGetDescriptionOnLiveCollectionCollectionExistsNoDescription";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		IRODSTagValue actualDescription = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNull("should have returned null when no collection found",
				actualDescription);

	}

	@Test(expected = JargonException.class)
	public final void testAddTagToLiveCollectionTwice() throws Exception {

		String testCollection = "testAddTagToLiveCollectionTwice";
		String expectedTagName = "testAddTagToLiveCollectionTwice";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

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
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedTagName2,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsOnCollection(targetIrodsCollection);

		Assert.assertEquals("should have returned the two tags added", 2,
				irodsTagValues.size());

		IRODSTagValue testTag1 = irodsTagValues.get(0);
		Assert.assertEquals("tag value is not what was expected",
				expectedTagName1, testTag1.getTagData());
		Assert.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag1.getTagUser());
		IRODSTagValue testTag2 = irodsTagValues.get(1);
		Assert.assertEquals("tag value is not what was expected",
				expectedTagName2, testTag2.getTagData());
		Assert.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag2.getTagUser());
	}

	@Test
	public final void testAddTwoTagsToACollectionThenDeleteOneTag()
			throws Exception {

		String testCollection = "testAddTwoTagsToACollectionThenDeleteOneTag";

		String expectedTagName1 = "testQueryTwoTagsOnLiveCollection1";
		String expectedTagName2 = "testQueryTwoTagsOnLiveCollection2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedTagName2,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		// now delete one of the tags (the second one)

		irodsTaggingService.deleteTagFromCollection(targetIrodsCollection,
				irodsTagValue);

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsOnCollection(targetIrodsCollection);

		Assert.assertEquals("should have returned only the first added tag", 1,
				irodsTagValues.size());

		IRODSTagValue testTag1 = irodsTagValues.get(0);
		Assert.assertEquals("tag value is not what was expected",
				expectedTagName1, testTag1.getTagData());
		Assert.assertEquals("tag user is not what was expected",
				irodsAccount.getUserName(), testTag1.getTagUser());

	}

	@Test
	public final void testGetTagsBasedOnMetadataDomainCollection()
			throws Exception {

		String testCollection = "testGetTagsBasedOnMetadataDomainCollection";

		String expectedTagName1 = "testGetTagsBasedOnMetadataDomainCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName1,
				irodsAccount.getUserName());
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsBasedOnMetadataDomain(MetadataDomain.COLLECTION,
						targetIrodsCollection);

		Assert.assertEquals("should have returned the tag added", 1,
				irodsTagValues.size());

	}

	@Test(expected = JargonException.class)
	public final void testGetTagsBasedOnMetadataDomainNotSuported()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.getTagsBasedOnMetadataDomain(
				MetadataDomain.RESOURCE, "test");

	}

	@Test
	public final void testRemoveTagFromLiveDataObjectByDomain()
			throws Exception {
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

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
		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue,
				MetadataDomain.DATA, targetIrodsDataObject);

		Assert.assertEquals(0,
				irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject)
						.size());

	}

	@Test
	public final void testRemoveTagFromLiveCollectionByDomain()
			throws Exception {

		String testCollection = "testRemoveTagFromLiveCollectionByDomain";
		String expectedTagName = "testRemoveTagFromLiveCollectionByDomain";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue,
				MetadataDomain.COLLECTION, targetIrodsCollection);

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsOnCollection(targetIrodsCollection);

		Assert.assertTrue("tag should not be in collection",
				irodsTagValues.isEmpty());

	}

	@Test(expected = JargonException.class)
	public final void testRemoveTagFromLiveCollectionNotExists()
			throws Exception {

		String testCollection = "testRemoveTagFromLiveCollectionNotExists";
		String expectedTagName = "testRemoveTagFromLiveCollectionNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.removeTagFromGivenDomain(irodsTagValue,
				MetadataDomain.COLLECTION, targetIrodsCollection);

	}

	@Test
	public final void testGetTagsFromLiveCollectionNotExists() throws Exception {

		String testCollection = "testGetTagsFromLiveCollectionNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsBasedOnMetadataDomain(MetadataDomain.COLLECTION,
						targetIrodsCollection);
		Assert.assertTrue("should have returned empty collection",
				irodsTagValues.isEmpty());

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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}

	@Test
	public final void testAddDescriptionToDataObjectTwice() throws Exception {
		String testFileName = "testAddDescriptionToDataObjectTwice.txt";
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		IRODSTagValue actual = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		TestCase.assertNotNull("did not find data object", actual);

	}

	@Test
	public final void testAddBlankDescriptionToDataObjectNoPrior()
			throws Exception {
		String testFileName = "testAddBlankDescriptionToDataObjectNoPrior.txt";

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

		IRODSTagValue irodsTagValue = new IRODSTagValue("",
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		IRODSTagValue actual = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		Assert.assertNull("should not be a description", actual);

	}

	@Test
	public final void testAddDescriptionThenAddBlankDescriptionToDelete()
			throws Exception {
		String testFileName = "testAddDescriptionThenAddBlankDescription.txt";
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTagValue = new IRODSTagValue("", irodsAccount.getUserName());
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		IRODSTagValue value = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		Assert.assertNull("should have deleted data object description", value);
	}

	@Test
	public final void testDeleteDescriptionFromLiveCollection()
			throws Exception {

		String testCollection = "delDescriptionFromLiveCollection";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToCollection(targetIrodsCollection,
				irodsTagValue);

		irodsTaggingService.deleteDescriptionFromCollection(
				targetIrodsCollection, irodsTagValue);

		IRODSTagValue actual = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsCollection);
		Assert.assertNull("should have an empty description", actual);

	}

	@Test
	public final void testAddDuplicateDescriptionToLiveDataObject()
			throws Exception {
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}

	@Test
	public final void testUpdateDescriptionDataObjectShouldAdd()
			throws Exception {
		String testFileName = "testUpdateDescriptionDataObjectShouldAdd.txt";
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.checkAndUpdateDescriptionOnDataObject(
				targetIrodsDataObject, irodsTagValue);
		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		Assert.assertNotNull("did not get tag value", actualTagValue);
		Assert.assertEquals("did not get same description", expectedAttribName,
				actualTagValue.getTagData());

	}

	@Test
	public final void testUpdateDescriptionDataObjectShouldUpdate()
			throws Exception {
		String testFileName = "testUpdateDescriptionDataObjectShouldUpdate.txt";
		String expectedAttribName = "testattrib1";
		String expectedNewAttribName = "testattrib1-new version";

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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.checkAndUpdateDescriptionOnDataObject(
				targetIrodsDataObject, irodsTagValue);

		irodsTagValue = new IRODSTagValue(expectedNewAttribName,
				irodsAccount.getUserName());

		irodsTaggingService.checkAndUpdateDescriptionOnDataObject(
				targetIrodsDataObject, irodsTagValue);

		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		Assert.assertNotNull("did not get tag value", actualTagValue);
		Assert.assertEquals("did not get same description",
				expectedNewAttribName, actualTagValue.getTagData());

	}

	@Test
	public final void testUpdateDescriptionDataObjectShouldDelete()
			throws Exception {
		String testFileName = "testUpdateDescriptionDataObjectShouldDelete.txt";
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

		irodsTagValue = new IRODSTagValue("", irodsAccount.getUserName());

		irodsTaggingService.checkAndUpdateDescriptionOnDataObject(
				targetIrodsDataObject, irodsTagValue);

		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnDataObjectForLoggedInUser(targetIrodsDataObject);
		Assert.assertNull("should have deleted tag", actualTagValue);

	}

	@Test
	public final void testUpdateDescriptionCollectionShouldAdd()
			throws Exception {
		String testFileName = "testUpdateDescriptionCollectionShouldAdd";
		String expectedAttribName = "testattrib1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsDataObject);
		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.checkAndUpdateDescriptionOnCollection(
				targetIrodsDataObject, irodsTagValue);
		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsDataObject);
		Assert.assertNotNull("did not get tag value", actualTagValue);
		Assert.assertEquals("did not get same description", expectedAttribName,
				actualTagValue.getTagData());

	}

	@Test
	public final void testUpdateDescriptionOnCollectionShouldUpdate()
			throws Exception {
		String testFileName = "testUpdateDescriptionOnCollectionShouldUpdate";
		String expectedAttribName = "testattrib1";
		String newAttribName = "testattrib2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsDataObject);
		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.addDescriptionToCollection(targetIrodsDataObject,
				irodsTagValue);
		irodsTagValue = new IRODSTagValue(newAttribName,
				irodsAccount.getUserName());
		irodsTaggingService.checkAndUpdateDescriptionOnCollection(
				targetIrodsDataObject, irodsTagValue);
		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsDataObject);
		Assert.assertNotNull("did not get tag value", actualTagValue);
		Assert.assertEquals("did not get same description", newAttribName,
				actualTagValue.getTagData());

	}

	@Test
	public final void testUpdateDescriptionOnCollectionShouldDelete()
			throws Exception {
		String testFileName = "testUpdateDescriptionOnCollectionShouldDelete";
		String expectedAttribName = "testattrib1";
		String newAttribName = "";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsDataObject);
		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.addDescriptionToCollection(targetIrodsDataObject,
				irodsTagValue);
		irodsTagValue = new IRODSTagValue(newAttribName,
				irodsAccount.getUserName());
		irodsTaggingService.checkAndUpdateDescriptionOnCollection(
				targetIrodsDataObject, irodsTagValue);
		IRODSTagValue actualTagValue = irodsTaggingService
				.getDescriptionOnCollectionForLoggedInUser(targetIrodsDataObject);
		Assert.assertNull("did not delete tag value", actualTagValue);

	}

	@Test(expected = DataNotFoundException.class)
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
		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedAttribName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsTaggingService.addDescriptionToDataObject(targetIrodsDataObject,
				irodsTagValue);

	}

	@Test
	public final void testRemoveDescriptionFromLiveDataObject()
			throws Exception {
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

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

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
		irodsTaggingService.deleteDescriptionFromDataObject(
				targetIrodsDataObject, irodsTagValue);

		Assert.assertEquals(0,
				irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject)
						.size());

	}

	@Test
	public final void testRemoveDescriptionFromMissingDataObject()
			throws Exception {
		String testFileName = "testRemoveDescriptionFromMissingDataObject.txt";
		String expectedTagName = "testRemoveDescriptionFromMissingDataObject";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		// non-existing description should silently fail in delete
		irodsTaggingService.deleteDescriptionFromDataObject(
				targetIrodsDataObject, irodsTagValue);

		Assert.assertEquals(0,
				irodsTaggingService.getTagsOnDataObject(targetIrodsDataObject)
						.size());

	}

}
