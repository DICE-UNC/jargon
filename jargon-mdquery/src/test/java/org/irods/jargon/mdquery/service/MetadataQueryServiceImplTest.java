package org.irods.jargon.mdquery.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQuery.QueryType;
import org.irods.jargon.mdquery.MetadataQueryElement;
import org.irods.jargon.mdquery.serialization.MetadataQueryJsonService;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetadataQueryServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MetadataQueryServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
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
	public void testSimpleAvuQueryNoPathHint() throws Exception {
		String testDirName = "testSimpleAvuQueryNoPathHint";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// initialize the AVU data
		final String expectedAttribName = "testSimpleAvuQueryNoPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryNoPathHintvalue1";
		final String expectedAttribUnits = "FindDomainByMetadataQuerytest1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.COLLECTIONS);
		metadataQuery.getMetadataQueryElements().add(element);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", 1, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("unexpected collection",
				testFile.getAbsolutePath(), actual
						.getCollectionAndDataObjectListingEntries().get(0)
						.getFormattedAbsolutePath());
		Assert.assertEquals("incorrect collection count", 1, actual
				.getPagingAwareCollectionListingDescriptor().getCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());
		Assert.assertTrue("should show data objs complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());

	}

	@Test
	public void testSimpleAvuQueryCollectionWithPathHint() throws Exception {
		String testDirName = "testSimpleAvuQueryCollectionWithPathHint";
		String testDirName2 = "testSimpleAvuQueryCollectionWithPathHint2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		// initialize the AVU data
		final String expectedAttribName = "testSimpleAvuQueryCollectionWithPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryCollectionWithPathHintvalue1";
		final String expectedAttribUnits = "testSimpleAvuQueryCollectionWithPathHintunits";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		IRODSFile testFile2 = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection2);
		testFile2.deleteWithForceOption();
		testFile2.mkdirs();

		collectionAO.deleteAVUMetadata(targetIrodsCollection2, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection2, avuData);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.COLLECTIONS);
		metadataQuery.setPathHint(targetIrodsCollection);
		metadataQuery.getMetadataQueryElements().add(element);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", 1, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("unexpected collection",
				testFile.getAbsolutePath(), actual
						.getCollectionAndDataObjectListingEntries().get(0)
						.getFormattedAbsolutePath());
		Assert.assertEquals("incorrect collection count", 1, actual
				.getPagingAwareCollectionListingDescriptor().getCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());
		Assert.assertTrue("should show data objs complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());

	}

	@Test
	public void testMultiAvuQueryCollectionWithPathHint() throws Exception {
		String testDirName = "testSimpleAvuQueryCollectionWithPathHint";
		String testDirName2 = "testSimpleAvuQueryCollectionWithPathHint2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		// initialize the AVU data
		final String expectedAttribName = "testSimpleAvuQueryCollectionWithPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryCollectionWithPathHintvalue1";
		final String expectedAttribUnits = "testSimpleAvuQueryCollectionWithPathHintunits";

		final String expectedAttribName2 = "testSimpleAvuQueryCollectionWithPathHintattrib2";
		final String expectedAttribValue2 = "testSimpleAvuQueryCollectionWithPathHintvalue2";
		final String expectedAttribUnits2 = "testSimpleAvuQueryCollectionWithPathHintunits";

		final String expectedAttribName3 = "testSimpleAvuQueryCollectionWithPathHintattrib3";
		final String expectedAttribValue3 = "testSimpleAvuQueryCollectionWithPathHintvalue3";
		final String expectedAttribUnits3 = "testSimpleAvuQueryCollectionWithPathHintunits";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2,
				expectedAttribUnits2);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		avuData = AvuData.instance(expectedAttribName3, expectedAttribValue3,
				expectedAttribUnits3);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		IRODSFile testFile2 = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection2);
		testFile2.deleteWithForceOption();
		testFile2.mkdirs();

		collectionAO.deleteAVUMetadata(targetIrodsCollection2, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection2, avuData);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.getMetadataQueryElements().add(element);

		element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName2);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals2 = new ArrayList<String>() {
			{
				add(expectedAttribValue2);
			}
		};
		element.setAttributeValue(vals2);

		metadataQuery.getMetadataQueryElements().add(element);

		element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName3);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals3 = new ArrayList<String>() {
			{
				add(expectedAttribValue3);
			}
		};
		element.setAttributeValue(vals3);

		metadataQuery.getMetadataQueryElements().add(element);

		metadataQuery.setQueryType(QueryType.COLLECTIONS);
		metadataQuery.setPathHint(targetIrodsCollection);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", 1, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("unexpected collection",
				testFile.getAbsolutePath(), actual
						.getCollectionAndDataObjectListingEntries().get(0)
						.getFormattedAbsolutePath());
		Assert.assertEquals("incorrect collection count", 1, actual
				.getPagingAwareCollectionListingDescriptor().getCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());
		Assert.assertTrue("should show data objs complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());

	}

	/*
	 * add 2 avus query 3, should discriminate and find no data
	 */
	@Test
	public void testMultiAvuQueryCollectionWithPathHintShouldNotMatch()
			throws Exception {
		String testDirName = "testMultiAvuQueryCollectionWithPathHintShouldNotMatch";
		String testDirName2 = "testMultiAvuQueryCollectionWithPathHintShouldNotMatch2";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		// initialize the AVU data
		final String expectedAttribName = "testMultiAvuQueryCollectionWithPathHintShouldNotMatchattrib1";
		final String expectedAttribValue = "testMultiAvuQueryCollectionWithPathHintShouldNotMatchvalue1";
		final String expectedAttribUnits = "testSimpleAvuQueryCollectionWithPathHintunits";

		final String expectedAttribName2 = "testMultiAvuQueryCollectionWithPathHintShouldNotMatchattrib2";
		final String expectedAttribValue2 = "testMultiAvuQueryCollectionWithPathHintShouldNotMatchvalue2";
		final String expectedAttribUnits2 = "testSimpleAvuQueryCollectionWithPathHintunits";

		final String expectedAttribName3 = "testSimpleAvuQueryCollectionWithPathHintattrib3";
		final String expectedAttribValue3 = "testSimpleAvuQueryCollectionWithPathHintvalue3";
		final String expectedAttribUnits3 = "testSimpleAvuQueryCollectionWithPathHintunits";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2,
				expectedAttribUnits2);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		IRODSFile testFile2 = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection2);
		testFile2.deleteWithForceOption();
		testFile2.mkdirs();

		collectionAO.deleteAVUMetadata(targetIrodsCollection2, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection2, avuData);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.getMetadataQueryElements().add(element);

		element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName2);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals2 = new ArrayList<String>() {
			{
				add(expectedAttribValue2);
			}
		};
		element.setAttributeValue(vals2);

		metadataQuery.getMetadataQueryElements().add(element);

		element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName3);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals3 = new ArrayList<String>() {
			{
				add(expectedAttribValue3);
			}
		};
		element.setAttributeValue(vals3);

		metadataQuery.getMetadataQueryElements().add(element);

		metadataQuery.setQueryType(QueryType.COLLECTIONS);
		metadataQuery.setPathHint(targetIrodsCollection);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result expected", 0, actual
				.getCollectionAndDataObjectListingEntries().size());

	}

	@Test
	public void testSimpleAvuQueryBothWithNoPathHint() throws Exception {
		String testDirName = "testSimpleAvuQueryBothWithNoPathHint";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);
		String testFilePrefix = "testSimpleAvuQueryOneDataObjectNoPathHint-";
		String testFileSuffix = ".txt";
		int count = 1;

		// initialize the AVU data
		final String expectedAttribName = "testSimpleAvuQueryBothWithNoPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryBothWithNoPathHintvalue1";
		final String expectedAttribUnits = "testSimpleAvuQueryBothWithNoPathHintunits";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		IRODSFile testSubdir = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		DataObjectAO dAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath,
						"testFileForAVU.txt", 1);
		File sourceFile = new File(sourceFileAbsolutePath);

		IRODSFile dataFile = null;
		StringBuilder sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(testSubdir.getAbsolutePath(),
							sb.toString());
			dto.putOperation(sourceFile, dataFile, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue,
					"");
			dAO.addAVUMetadata(dataFile.getAbsolutePath(), avuData);

		}

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.BOTH);
		metadataQuery.getMetadataQueryElements().add(element);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", 2, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("unexpected collection",
				testFile.getAbsolutePath(), actual
						.getCollectionAndDataObjectListingEntries().get(0)
						.getFormattedAbsolutePath());
		Assert.assertEquals("second data object not found",
				CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT,
				actual.getCollectionAndDataObjectListingEntries().get(1)
						.getObjectType());
		Assert.assertEquals("incorrect collection count", 1, actual
				.getPagingAwareCollectionListingDescriptor().getCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());
		Assert.assertTrue("should show data objs complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());

	}

	@Test
	public void testSimpleAvuQueryAsJsonNoPathHint() throws Exception {
		String testDirName = "testSimpleAvuQueryAsJsonNoPathHint";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// initialize the AVU data
		final String expectedAttribName = "testSimpleAvuQueryAsJsonNoPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryAsJsonNoPathHintvalue1";
		final String expectedAttribUnits = "testSimpleAvuQueryAsJsonNoPathHintunits";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile testFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		testFile.deleteWithForceOption();
		testFile.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.COLLECTIONS);
		metadataQuery.getMetadataQueryElements().add(element);
		MetadataQueryJsonService jsonService = new MetadataQueryJsonService();
		String metadataQueryAsString = jsonService
				.jsonFromMetadataQuery(metadataQuery);
		System.out.println("query string:" + metadataQueryAsString);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQueryAsString);
		Assert.assertNotNull("null listing returned", actual);

	}

	@Test
	public void testSimpleAvuQueryOneDataObjectNoPathHint() throws Exception {
		String testCollName = "testSimpleAvuQueryOneDataObjectNoPathHint";
		String testFilePrefix = "testSimpleAvuQueryOneDataObjectNoPathHint-";
		String testFileSuffix = ".txt";
		int count = 200;
		String expectedAttribName = "testSimpleAvuQueryOneDataObjectNoPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryOneDataObjectNoPathHintvalue1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollName);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// generate some test files, first delete the test subdir

		IRODSFile testSubdir = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		testSubdir.deleteWithForceOption();
		testSubdir.mkdirs();

		DataObjectAO dAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		AvuData avuData = null;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath,
						"testFileForAVU.txt", 1);
		File sourceFile = new File(sourceFileAbsolutePath);

		IRODSFile dataFile = null;
		StringBuilder sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(testSubdir.getAbsolutePath(),
							sb.toString());
			dto.putOperation(sourceFile, dataFile, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue,
					"");
			dAO.addAVUMetadata(dataFile.getAbsolutePath(), avuData);

		}

		ArrayList<AVUQueryElement> avus = new ArrayList<AVUQueryElement>();
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.LIKE, expectedAttribValue + "%"));

		List<DataObject> files = dAO.findDomainByMetadataQuery(avus);
		Assert.assertNotNull("null files returned", files);
		Assert.assertTrue("did not get all of the files", files.size() >= count);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.DATA);
		metadataQuery.getMetadataQueryElements().add(element);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", count, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("incorrect count count", count, actual
				.getPagingAwareCollectionListingDescriptor()
				.getDataObjectsCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());
		Assert.assertTrue("should show colls complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());

	}

	@Test
	public void testSimpleAvuQueryOneDataObjectWithPathHint() throws Exception {
		String testCollName = "testSimpleAvuQueryOneDataObjectWithPathHint";
		String testCollName2 = "testSimpleAvuQueryOneDataObjectWithPathHint2";

		String testFilePrefix = "testSimpleAvuQueryOneDataObjectWithPathHint-";
		String testFileSuffix = ".txt";
		int count = 200;
		String expectedAttribName = "testSimpleAvuQueryOneDataObjectWithPathHintattrib1";
		final String expectedAttribValue = "testSimpleAvuQueryOneDataObjectWithPathHintvalue1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollName);

		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollName2);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// generate some test files, first delete the test subdir

		IRODSFile testSubdir = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		testSubdir.deleteWithForceOption();
		testSubdir.mkdirs();

		IRODSFile testSubdir2 = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection2);
		testSubdir2.deleteWithForceOption();
		testSubdir2.mkdirs();

		DataObjectAO dAO = accessObjectFactory.getDataObjectAO(irodsAccount);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		AvuData avuData = null;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String sourceFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath,
						"testFileForAVU.txt", 1);
		File sourceFile = new File(sourceFileAbsolutePath);

		IRODSFile dataFile = null;
		StringBuilder sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(testSubdir.getAbsolutePath(),
							sb.toString());
			dto.putOperation(sourceFile, dataFile, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue,
					"");
			dAO.addAVUMetadata(dataFile.getAbsolutePath(), avuData);

		}

		absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		IRODSFile dataFile2 = null;
		sb = null;
		for (int i = 0; i < count; i++) {
			sb = new StringBuilder();
			sb.append(testFilePrefix);
			sb.append(i);
			sb.append(testFileSuffix);
			dataFile2 = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(testSubdir2.getAbsolutePath(),
							sb.toString());
			dto.putOperation(sourceFile, dataFile2, null, null);
			avuData = AvuData.instance(expectedAttribName, expectedAttribValue,
					"");
			dAO.addAVUMetadata(dataFile2.getAbsolutePath(), avuData);

		}

		ArrayList<AVUQueryElement> avus = new ArrayList<AVUQueryElement>();
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));
		avus.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.LIKE, expectedAttribValue + "%"));

		List<DataObject> files = dAO.findDomainByMetadataQuery(avus);
		Assert.assertNotNull("null files returned", files);
		Assert.assertTrue("did not get all of the files", files.size() >= count);

		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQuery metadataQuery = new MetadataQuery();
		MetadataQueryElement element = new MetadataQueryElement();
		element.setAttributeName(expectedAttribName);
		element.setOperator(AVUQueryOperatorEnum.EQUAL);
		@SuppressWarnings("serial")
		List<String> vals = new ArrayList<String>() {
			{
				add(expectedAttribValue);
			}
		};
		element.setAttributeValue(vals);

		metadataQuery.setQueryType(QueryType.BOTH);
		metadataQuery.setPathHint(testSubdir2.getAbsolutePath());
		metadataQuery.getMetadataQueryElements().add(element);

		PagingAwareCollectionListing actual = metadataQueryService
				.executeQuery(metadataQuery);
		Assert.assertNotNull("null listing returned", actual);
		Assert.assertEquals("no result row", count, actual
				.getCollectionAndDataObjectListingEntries().size());
		Assert.assertEquals("incorrect count count", count, actual
				.getPagingAwareCollectionListingDescriptor()
				.getDataObjectsCount());
		Assert.assertTrue("should reflect end of colls", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());
		Assert.assertTrue("should show colls complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isCollectionsComplete());

	}

}
