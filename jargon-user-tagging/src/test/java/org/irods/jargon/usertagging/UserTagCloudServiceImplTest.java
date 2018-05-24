package org.irods.jargon.usertagging;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.usertagging.domain.IRODSTagGrouping;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.irods.jargon.usertagging.domain.TagCloudEntry;
import org.irods.jargon.usertagging.domain.UserTagCloudView;
import org.irods.jargon.usertagging.tags.FreeTaggingService;
import org.irods.jargon.usertagging.tags.FreeTaggingServiceImpl;
import org.irods.jargon.usertagging.tags.IRODSTaggingServiceImpl;
import org.irods.jargon.usertagging.tags.UserTagCloudService;
import org.irods.jargon.usertagging.tags.UserTagCloudServiceImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class UserTagCloudServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "UserTagCloudServiceImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		UserTagCloudService userTagCloudService = UserTagCloudServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		irodsFileSystem.close();
		Assert.assertNotNull(userTagCloudService);

	}

	@Test
	public final void testDataObjectCloudTwoObjectsSomeSharedTags() throws Exception {
		String testCollection = "testDataObjectCloudTwoObjectsSomeSharedTagsb";
		String testFileName = "testDataObjectCloudTwoObjectsSomeSharedTags1b.txt";
		String testFileName2 = "testDataObjectCloudTwoObjectsSomeSharedTags2b.txt";

		String expectedTagName1 = "testDataObjectCloudTwoObjectsSomeSharedTags1File1b";
		String expectedTagName2 = "testDataObjectCloudTwoObjectsSomeSharedTags2File2b";
		String expectedTagName3 = "testDataObjectCloudTwoObjectsSomeSharedTags3File2b";
		String expectedTagNameShared = "testDataObjectCloudTwoObjectsSomeSharedTagsSharedTagb";

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;
		String targetIrodsDataObject2 = targetIrodsCollection + "/" + testFileName2;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String fileNameOrig2 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName2, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.setResource(irodsAccount.getDefaultStorageResource());
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig2), targetIrodsFile, null, null);

		IRODSTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		String newFreeTagArea = expectedTagName1 + " " + expectedTagNameShared;

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject,
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		newFreeTagArea = expectedTagName2 + " " + expectedTagNameShared + " " + expectedTagName3;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject2, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		// now get the user tag cloud I just built

		UserTagCloudService userTagCloudService = UserTagCloudServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		UserTagCloudView view = userTagCloudService.getTagCloudForDataObjects();
		Assert.assertNotNull(view);

		// find the count for the shared tag and verify

		Assert.assertEquals(irodsAccount.getUserName(), view.getUserName());
		Collection<TagCloudEntry> tagCloudEntries = view.getTagCloudEntries().values();
		for (TagCloudEntry entry : tagCloudEntries) {
			if (entry.getIrodsTagValue().getTagData().equals(expectedTagNameShared) && entry.getCountOfFiles() == 2) {
			}
		}

		// Assert.assertTrue("did not find shared tag with correct count",
		// foundSharedWithCorrectCount);

	}

	@Test
	public void testGetDataCloudForCollections() throws Exception {
		int collCount = 20;
		String testCollectionBase = "testGetDataCloudForCollectionsc";
		String expectedTagNameBase = "testGetDataCloudForCollectionsTagc";
		String expectedTagNameSharedBase = "testGetDataCloudForCollectionsTagSharedc";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		// each collection has it's own tag and a shared tag

		for (int i = 0; i < collCount; i++) {
			String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
					testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollectionBase + i);
			IRODSFile targetCollectionFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection);
			targetCollectionFile.mkdirs();

			String tag1 = expectedTagNameBase + i;
			String tag2 = expectedTagNameSharedBase;

			String freeTags = tag1 + " " + tag2;

			IRODSTagGrouping tagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, targetIrodsCollection,
					freeTags, irodsAccount.getUserName());
			freeTaggingService.updateTags(tagGrouping);

		}

		// setup done, now get collection cloud
		UserTagCloudService userTagCloudService = UserTagCloudServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		UserTagCloudView view = userTagCloudService.getTagCloudForCollections();
		Assert.assertNotNull(view);

		// find the count for the shared tag and verify

		Assert.assertEquals(irodsAccount.getUserName(), view.getUserName());

		Collection<TagCloudEntry> tagCloudEntries = view.getTagCloudEntries().values();
		for (TagCloudEntry entry : tagCloudEntries) {
			if (entry.getIrodsTagValue().getTagData().equals(expectedTagNameSharedBase)
					&& entry.getCountOfCollections() == collCount) {
			}
		}
	}

	@Test
	public void testSearchTagCloudWithFileTagsAndCollectionTags() throws Exception {

		String testCollection = "testGetTagCloudWithFileTagsAndCollectionTags";
		String testFileName = "testGetTagCloudWithFileTagsAndCollectionTags1.txt";
		String testFileName2 = "testGetTagCloudWithFileTagsAndCollectionTags2.txt";

		String expectedTagName1 = "testSearchTagCloudWithFileTagsAndCollectionTags1File1";
		String expectedTagName2 = "testSearchTagCloudWithFileTagsAndCollectionTagsFile2";

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;
		String targetIrodsDataObject2 = targetIrodsCollection + "/" + testFileName2;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String fileNameOrig2 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName2, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig2), targetIrodsFile, null, null);

		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		String newFreeTagArea = expectedTagName1;

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject,
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		newFreeTagArea = expectedTagName2;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject2, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		String testCollectionBase = "testSearchTagCloudWithFileTagsAndCollectionTagsColl";
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollectionBase + 1);
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetCollectionFile.mkdirs();
		newFreeTagArea = expectedTagName1 + " " + expectedTagName2;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, targetCollectionFile.getAbsolutePath(),
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollectionBase + 2);
		targetCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetCollectionFile.mkdirs();

		newFreeTagArea = expectedTagName2;
		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, targetCollectionFile.getAbsolutePath(),
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		UserTagCloudService userTagCloudService = UserTagCloudServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		UserTagCloudView searchView = userTagCloudService
				.searchForTagsForDataObjectsAndCollectionsUsingSearchTermForTheLoggedInUser(
						"SearchTagCloudWithFileTagsAndCollectionTags");
		Assert.assertNotNull("searchView is null, no tags returned from search", searchView);

		irodsFileSystem.close();

	}

	@Test
	public void testGetTagCloudWithFileTagsAndCollectionTags() throws Exception {

		String testCollection = "testGetTagCloudWithFileTagsAndCollectionTagsa";
		String testFileName = "testGetTagCloudWithFileTagsAndCollectionTagsa1.txt";
		String testFileName2 = "testGetTagCloudWithFileTagsAndCollectionTagsa2.txt";

		String expectedTagName1 = "testGetTagCloudWithFileTagsAndCollectionTags1File1a";
		String expectedTagName2 = "testGetTagCloudWithFileTagsAndCollectionTags2File2a";

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFile targetIrodsCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);

		targetIrodsCollectionFile.mkdirs();

		String targetIrodsDataObject = targetIrodsCollection + "/" + testFileName;
		String targetIrodsDataObject2 = targetIrodsCollection + "/" + testFileName2;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig2 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName2, 2);

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), targetIrodsFile, null, null);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig2), targetIrodsFile, null, null);

		IRODSTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		FreeTaggingService freeTaggingService = FreeTaggingServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		String newFreeTagArea = expectedTagName1;

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject,
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		newFreeTagArea = expectedTagName2;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, targetIrodsDataObject2, newFreeTagArea,
				irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		String testCollectionBase = "testGetTagCloudWithFileTagsAndCollectionTagsColl";
		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollectionBase + 1);
		IRODSFile targetCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetCollectionFile.mkdirs();
		newFreeTagArea = expectedTagName1 + " " + expectedTagName2;

		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, targetCollectionFile.getAbsolutePath(),
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testCollectionBase + 2);
		targetCollectionFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetCollectionFile.mkdirs();

		newFreeTagArea = expectedTagName2;
		irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, targetCollectionFile.getAbsolutePath(),
				newFreeTagArea, irodsAccount.getUserName());
		freeTaggingService.updateTags(irodsTagGrouping);

		UserTagCloudService userTagCloudService = UserTagCloudServiceImpl
				.instance(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		UserTagCloudView userTagCloudView = userTagCloudService.getTagCloud();
		Assert.assertNotNull(userTagCloudView);

		// find the tag1 entry, which should have 1 file and 1 collection
		IRODSTagValue tagValue = new IRODSTagValue(expectedTagName2, irodsAccount.getUserName());
		Assert.assertNotNull(tagValue);

		irodsFileSystem.close();

	}

}
