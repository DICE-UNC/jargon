package org.irods.jargon.usertagging;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.usertagging.domain.IRODSTagGrouping;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.irods.jargon.usertagging.domain.TagQuerySearchResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class FreeTaggingServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FreeTaggingServiceImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		irodsFileSystem.close();
		TestCase.assertNotNull(freeTaggingService);
	}

	@Test
	public final void testInstanceProvidingATagUpdateService() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSTaggingService irodsTaggingService = Mockito
				.mock(IRODSTaggingService.class);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instanceProvidingATagUpdateService(irodsAccessObjectFactory,
						irodsAccount, irodsTaggingService);
		TestCase.assertNotNull(freeTaggingService);
	}

	@Test
	public final void testGetTagsForDataObjectInFreeTagForm() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSTaggingService irodsTaggingService = Mockito
				.mock(IRODSTaggingService.class);

		String dataObjectExpectedPath = "/a/path/tofile.txt";

		List<IRODSTagValue> valuesToReturnFromTagService = new ArrayList<IRODSTagValue>();
		valuesToReturnFromTagService.add(new IRODSTagValue("tag1", irodsAccount
				.getUserName()));
		valuesToReturnFromTagService.add(new IRODSTagValue("tag2", irodsAccount
				.getUserName()));
		valuesToReturnFromTagService.add(new IRODSTagValue("tag3", irodsAccount
				.getUserName()));

		Mockito
				.when(
						irodsTaggingService
								.getTagsOnDataObject(dataObjectExpectedPath))
				.thenReturn(valuesToReturnFromTagService);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instanceProvidingATagUpdateService(irodsAccessObjectFactory,
						irodsAccount, irodsTaggingService);

		IRODSTagGrouping irodsTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(dataObjectExpectedPath);

		TestCase.assertEquals(MetadataDomain.DATA, irodsTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(irodsAccount.getUserName(), irodsTagGrouping
				.getUserName());
		TestCase.assertEquals(dataObjectExpectedPath, irodsTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals("tag1 tag2 tag3", irodsTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}

	@Test
	public final void testGetTagsForCollectionInFreeTagForm() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSTaggingService irodsTaggingService = Mockito
				.mock(IRODSTaggingService.class);

		String collectionExpectedPath = "/a/path/tocollection";

		List<IRODSTagValue> valuesToReturnFromTagService = new ArrayList<IRODSTagValue>();
		valuesToReturnFromTagService.add(new IRODSTagValue("tag1", irodsAccount
				.getUserName()));
		valuesToReturnFromTagService.add(new IRODSTagValue("tag2", irodsAccount
				.getUserName()));
		valuesToReturnFromTagService.add(new IRODSTagValue("tag3", irodsAccount
				.getUserName()));

		Mockito
				.when(
						irodsTaggingService
								.getTagsOnCollection(collectionExpectedPath))
				.thenReturn(valuesToReturnFromTagService);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instanceProvidingATagUpdateService(irodsAccessObjectFactory,
						irodsAccount, irodsTaggingService);

		IRODSTagGrouping irodsTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(collectionExpectedPath);

		TestCase.assertEquals(MetadataDomain.COLLECTION, irodsTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(irodsAccount.getUserName(), irodsTagGrouping
				.getUserName());
		TestCase.assertEquals(collectionExpectedPath, irodsTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals("tag1 tag2 tag3", irodsTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}

	@Test
	public final void testAddTagToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection()
			throws Exception {

		String testCollection = "testAddTagToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection";
		String expectedTagName = "testAddTagToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, targetIrodsCollection,
				expectedTagName, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		// now get the tags, should be a string with just the expected tag name
		// in iRODS
		IRODSTagGrouping actualTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);
		irodsFileSystem.close();

		TestCase.assertNotNull(actualTagGrouping);
		TestCase.assertEquals(targetIrodsCollection, actualTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals(MetadataDomain.COLLECTION, actualTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(expectedTagName, actualTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}

	@Test
	public final void testAddTagToLiveCollectionViaFreeTagsWithMultipleLeadingSpaces()
			throws Exception {

		String testCollection = "testAddTagToLiveCollectionViaFreeTagsWithMultipleLeadingSpaces";
		String expectedTagName = "testAddTagToLiveCollectionViaFreeTagsWithMultipleLeadingSpaces";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, targetIrodsCollection, "    "
						+ expectedTagName, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		// now get the tags, should be a string with just the expected tag name
		// in iRODS
		IRODSTagGrouping actualTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);
		irodsFileSystem.close();

		TestCase.assertNotNull(actualTagGrouping);
		TestCase.assertEquals(targetIrodsCollection, actualTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals(MetadataDomain.COLLECTION, actualTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(expectedTagName, actualTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}

	@Test
	public final void testAddTwoTagsToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection()
			throws Exception {

		String testCollection = "testAddTwoTagsToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection";
		String expectedTagName = "testAddTwoTagsToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection1";
		String expectedTagName2 = "testAddTwoTagsToLiveCollectionViaFreeTagsWithNoTagsAlreadyInCollection2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, targetIrodsCollection,
				expectedTagName + " " + expectedTagName2, irodsAccount
						.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		// now get the tags, should be a string with just the expected tag name
		// in iRODS
		IRODSTagGrouping actualTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);
		irodsFileSystem.close();

		TestCase.assertNotNull(actualTagGrouping);
		TestCase.assertEquals(targetIrodsCollection, actualTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals(MetadataDomain.COLLECTION, actualTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(expectedTagName + " " + expectedTagName2,
				actualTagGrouping.getSpaceDelimitedTagsForDomain());

	}

	@Test
	public final void testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollection()
			throws Exception {

		String testCollection = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollection";
		String expectedTagName = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollection";
		String expectedNewTagName = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollectionNewTag1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		// I have a tag, now add a new tag via free tags, including the old one

		String newFreeTagArea = expectedTagName + " " + expectedNewTagName;
		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, targetIrodsCollection,
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);
		IRODSTagGrouping actualIRODSTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);
		irodsFileSystem.close();

		TestCase
				.assertTrue(actualIRODSTagGrouping
						.getSpaceDelimitedTagsForDomain().indexOf(
								expectedTagName) > -1);
		TestCase
				.assertTrue(actualIRODSTagGrouping
						.getSpaceDelimitedTagsForDomain().indexOf(
								expectedNewTagName) > -1);
	}

	@Test
	public final void testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollectionTwoSpacesBetweenTags()
			throws Exception {

		String testCollection = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollectionTwoSpacesBetweenTags";
		String expectedTagName = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollectionTwoSpacesBetweenTags";
		String expectedNewTagName = "testAddTagToLiveCollectionViaFreeTagsWithOneTagAlreadyInCollectionTwoSpacesBetweenTags1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		IRODSTagValue irodsTagValue = new IRODSTagValue(expectedTagName,
				irodsAccount.getUserName());

		IRODSTaggingService irodsTaggingService = IRODSTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		irodsTaggingService.addTagToCollection(targetIrodsCollection,
				irodsTagValue);

		// I have a tag, now add a new tag via free tags, including the old one

		String newFreeTagArea = expectedTagName + "  " + expectedNewTagName;
		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, targetIrodsCollection,
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);
		IRODSTagGrouping actualIRODSTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);

		irodsFileSystem.close();

		TestCase
				.assertTrue(actualIRODSTagGrouping
						.getSpaceDelimitedTagsForDomain().indexOf(
								expectedTagName) > -1);
		TestCase
				.assertTrue(actualIRODSTagGrouping
						.getSpaceDelimitedTagsForDomain().indexOf(
								expectedNewTagName) > -1);

	}

	@Test
	public final void testAddFourTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThem()
			throws Exception {

		// multi-step integration type testing for data object free tagging

		String testCollection = "testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThem";
		String testFileName = "addThreeDeleteOne.txt";
		String expectedTagName1 = "testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThem1";
		String expectedTagName2 = "testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThem2";
		String expectedTagName3 = "testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThem3";
		String expectedTagNameDeleteMe = "testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteOneOfThemDeleteMe";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		// add all four

		String newFreeTagArea = expectedTagName1 + " " + expectedTagName2
				+ "        " + expectedTagNameDeleteMe
				+ "                         " + expectedTagName3;

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.DATA, targetIrodsDataObject, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);
		IRODSTagGrouping actualIRODSTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);

		// check the tags

		String tagsAsAdded = actualIRODSTagGrouping
				.getSpaceDelimitedTagsForDomain();

		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName1) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName2) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName3) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagNameDeleteMe) > -1);

		// now do a new free tag ommitting the deleteme tag and verify delete of
		// that one tag

		newFreeTagArea = expectedTagName1 + " " + expectedTagName2 + "        "
				+ expectedTagName3;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA,
				targetIrodsDataObject, newFreeTagArea, irodsAccount
						.getUserName());

		freeTaggingService.updateTags(irodsTagGrouping);
		actualIRODSTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);

		String tagsAsUpdated = actualIRODSTagGrouping
				.getSpaceDelimitedTagsForDomain();

		irodsFileSystem.close();

		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName1) > -1);
		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName2) > -1);
		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName3) > -1);
		TestCase
				.assertTrue(tagsAsUpdated.indexOf(expectedTagNameDeleteMe) == -1);

	}

	@Test
	public final void testAddThreeTagsLiveDataObjectViaFreeTagsThenDeleteAllOfThem()
			throws Exception {

		// multi-step integration type testing for data object free tagging

		String testCollection = "testAddFourTagsLiveDataObjectViaFreeTagsThenDeleteAllOfThem";
		String testFileName = "addThreeDeleteOne.txt";
		String expectedTagName1 = "testAddFourTagsLiveDataObjectViaFreeTagsThenDeleteAllOfThem1";
		String expectedTagName2 = "testAddFourTagsLiveDataObjectViaFreeTagsThenDeleteAllOfThem2";
		String expectedTagName3 = "testAddFourTagsLiveDataObjectViaFreeTagsThenDeleteAllOfThem";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		String newFreeTagArea = expectedTagName1 + " " + expectedTagName2

		+ "                         " + expectedTagName3;

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.DATA, targetIrodsDataObject, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);
		IRODSTagGrouping actualIRODSTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);

		// check the tags

		String tagsAsAdded = actualIRODSTagGrouping
				.getSpaceDelimitedTagsForDomain();

		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName1) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName2) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName3) > -1);

		// now do a new free tag ommitting the deleteme tag and verify delete of
		// that one tag

		newFreeTagArea = "     ";

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA,
				targetIrodsDataObject, newFreeTagArea, irodsAccount
						.getUserName());

		freeTaggingService.updateTags(irodsTagGrouping);
		actualIRODSTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);

		String tagsAsUpdated = actualIRODSTagGrouping
				.getSpaceDelimitedTagsForDomain();

		irodsFileSystem.close();

		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName1) == -1);
		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName2) == -1);
		TestCase.assertTrue(tagsAsUpdated.indexOf(expectedTagName3) == -1);
	}

	@Test
	public void addSeveralCollectionsAndSeveralDataObjectsThenQueryOnOneTag()
			throws Exception {
		int collCount = 3;
		int dataObjectCount = 3;
		String collectionNameBase = "addSeveralCollectionsAndSeveralDataObjectsThenQueryOnOneTag";
		String fileNameBase = "addSeveralCollectionsAndSeveralDataObjectsThenQueryOnOneTag.csv";
		String tag1 = "abcxxx1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile collectionFile = null;
		String targetIrodsCollection = null;
		AvuData avuData = null;

		for (int i = 0; i < collCount; i++) {
			targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
									+ collectionNameBase + i);
			collectionFile = irodsFileFactory
					.instanceIRODSFile(targetIrodsCollection);
			collectionFile.mkdirs();
			avuData = AvuData.instance(tag1, irodsAccount.getUserName(),
					UserTaggingConstants.TAG_AVU_UNIT);
			collectionAO.addAVUMetadata(collectionFile.getAbsolutePath(),
					avuData);
		}

		IRODSFile targetIrodsFile = null;
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		String fileNameOrig = null;
		String absPath = null;

		for (int i = 0; i < dataObjectCount; i++) {
			absPath = scratchFileUtils
					.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
			fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
					absPath, fileNameBase + i, 1);

			targetIrodsFile = irodsFileFactory
					.instanceIRODSFile(targetIrodsCollection + fileNameBase + i);
			dto.putOperation(new File(fileNameOrig), targetIrodsFile, null,
					null);

			avuData = AvuData.instance(tag1, irodsAccount.getUserName(),
					UserTaggingConstants.TAG_AVU_UNIT);
			dataObjectAO.addAVUMetadata(targetIrodsFile.getAbsolutePath(),
					avuData);
		}
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
		.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		
		TagQuerySearchResult tagQuerySearchResult = freeTaggingService.searchUsingFreeTagString(tag1);
		irodsFileSystem.close();
		
		//TestCase.assertEquals("did not find the same number of files and collections as I tagged", dataObjectCount + collCount, tagQuerySearchResult.getQueryResultEntries().size());
		TestCase.assertEquals("did not preserve the given tags in the result object", tag1, tagQuerySearchResult.getSearchTags());
		
		// spot check data tags
		int countActualDataObjects = 0;
		int countActualCollections = 0;
		
		for (CollectionAndDataObjectListingEntry entry : tagQuerySearchResult.getQueryResultEntries()) {
			if (entry.getObjectType().equals(ObjectType.DATA_OBJECT)) {
				countActualDataObjects++;
				TestCase.assertTrue("this is not the right data object", entry.getPathOrName().indexOf(fileNameBase) > -1);
				TestCase.assertTrue("did not set the data object parent", entry.getParentPath().indexOf(IRODS_TEST_SUBDIR_PATH) > -1);
			} else {
				countActualCollections++;
				TestCase.assertTrue("this is not the right data object", entry.getPathOrName().indexOf(collectionNameBase) > -1);
			}
			
		}
		
		//TestCase.assertEquals("did not get right count of data objects", dataObjectCount, countActualDataObjects);
		//TestCase.assertEquals("did not get right count of collections", collCount, countActualCollections);

	}
	
	@Test
	public final void updateTagsWhenCollection()
			throws Exception {

		String testCollection = "updateTagsWhenCollection";
		String expectedTagName = "updateTagsWhenCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		targetIrodsFile.mkdirs();

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection(targetIrodsFile.getAbsolutePath(), irodsAccount.getUserName(), expectedTagName);

		// now get the tags, should be a string with just the expected tag name
		// in iRODS
		IRODSTagGrouping actualTagGrouping = freeTaggingService
				.getTagsForCollectionInFreeTagForm(targetIrodsCollection);
		irodsFileSystem.close();

		TestCase.assertNotNull(actualTagGrouping);
		TestCase.assertEquals(targetIrodsCollection, actualTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals(MetadataDomain.COLLECTION, actualTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(expectedTagName, actualTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}
	
	@Test
	public final void updateTagsWhenDataObject()
			throws Exception {

		String testCollection = "updateTagsWhenDataObject";
		String testFileName = "updateTagsWhenDataObject.txt";
		String expectedTagName = "updateTagsWhenDataObject";
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection(targetIrodsDataObject, irodsAccount.getUserName(), expectedTagName);

		// now get the tags, should be a string with just the expected tag name
		// in iRODS
		IRODSTagGrouping actualTagGrouping = freeTaggingService.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);
				
		irodsFileSystem.close();

		TestCase.assertNotNull(actualTagGrouping);
		TestCase.assertEquals(targetIrodsDataObject, actualTagGrouping
				.getDomainUniqueName());
		TestCase.assertEquals(MetadataDomain.DATA, actualTagGrouping
				.getMetadataDomain());
		TestCase.assertEquals(expectedTagName, actualTagGrouping
				.getSpaceDelimitedTagsForDomain());

	}
	
	@Test(expected=JargonException.class)
	public final void updateTagsWhenDataObjectNotExists()
			throws Exception {

		String testCollection = "updateTagsWhenDataObjectNotExists";
		String testFileName = "updateTagsWhenDataObjectNotExists.txt";
		String expectedTagName = "updateTagsWhenDataObjectNotExists";
		
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection(targetIrodsCollection + "/" + testFileName, irodsAccount.getUserName(), expectedTagName);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void updateTagsNullPath()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsAccessObjectFactory,
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection(null, irodsAccount.getUserName(), "tag");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void updateTagsBlankPath()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsAccessObjectFactory,
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection("", irodsAccount.getUserName(), "tag");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void updateTagsNullUser()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsAccessObjectFactory,
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection("file",null, "tag");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public final void updateTagsBlankUser()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsAccessObjectFactory,
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection("file","", "tag");
	}

	@Test(expected=IllegalArgumentException.class)
	public final void updateTagsNullTags()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito.mock(IRODSAccessObjectFactory.class);
		
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsAccessObjectFactory,
						irodsAccount);

		freeTaggingService.updateTagsForUserForADataObjectOrCollection("file","user", null);
	}

	/**
	 * [#677] strip quotes, commas from tags
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testTagsWithQuotesAndCommas() throws Exception {

		// multi-step integration type testing for data object free tagging

		String testCollection = "testTagsWithQuotesAndCommas";
		String testFileName = "testTagsWithQuotesAndCommas.txt";
		String expectedTagName1 = "testTagsWithQuotesAndCommas";
		String expectedTagName2 = "testTagsWithQuotesAndCommas2";
		String expectedTagName3 = "testTagsWithQuotesAndCommas3";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
						irodsAccount);

		// add tags

		String newFreeTagArea = expectedTagName1 + ", " + "\""
				+ expectedTagName2 + "                         "
				+ expectedTagName3 + "\"";

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.DATA, targetIrodsDataObject, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);
		IRODSTagGrouping actualIRODSTagGrouping = freeTaggingService
				.getTagsForDataObjectInFreeTagForm(targetIrodsDataObject);

		// check the tags

		String tagsAsAdded = actualIRODSTagGrouping
				.getSpaceDelimitedTagsForDomain();

		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName1) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName2) > -1);
		TestCase.assertTrue(tagsAsAdded.indexOf(expectedTagName3) > -1);

	}

}
