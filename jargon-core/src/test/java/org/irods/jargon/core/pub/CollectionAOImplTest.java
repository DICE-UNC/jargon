package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.UserFilePermission;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaAddCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaListCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaRemoveCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testCollectionAOImpl() throws Exception {
		

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		Assert.assertNotNull(collectionAO);
	}

	@Test
	public void testInstanceIRODSFileForCollectionPath() throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		// now get an irods file and see if it is readable, it should be
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection);
		Assert.assertNotNull(irodsFile);
		Assert.assertTrue(irodsFile.isDirectory());
	}

	@Test
	public void testInstanceIRODSFileForCollectionPathNotExists()
			throws Exception {
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile irodsFile = collectionAO
				.instanceIRODSFileForCollectionPath(targetIrodsCollection
						+ "/idontexistshere");
		Assert.assertFalse("this should not exist", irodsFile.exists());
	}

	@Test
	public void testFindAll() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		List<Collection> collections = collectionAO.findAll("/"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY)
				+ "/home");
		Assert.assertNotNull(collections);
		Assert.assertFalse(collections.isEmpty());

	}

	@Test
	public void testFindAllPartialStart() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		List<Collection> collections = collectionAO.findAll("/"
				+ testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY)
				+ "/home");

		// get count and partial start

		int firstCount = collections.size();
		int partialStart = firstCount / 2;

		List<Collection> moreCollections = collectionAO
				.findAll(
						"/"
								+ testingProperties
										.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY)
								+ "/home", partialStart);

		Assert.assertEquals("did not find right collection at partial start",
				collections.get(partialStart).getCollectionId(),
				moreCollections.get(0).getCollectionId());
		Assert.assertNotNull(collections);
		Assert.assertFalse(collections.isEmpty());

	}

	@Test
	public void testFindDomainByMetadataQuery() throws Exception {

		String testDirName = "testFindDomainByMetadataQuery";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<Collection> result = collectionAO
				.findDomainByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(targetIrodsCollection, result.get(0)
				.getCollectionName());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQuery() throws Exception {
		String testDirName = "testFindMetadataValuesByMetadataQuery";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQueryForCollection()
			throws Exception {
		String testDirName = "testFindMetadataValuesByMetadataQueryForCollection";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQueryTwoConditions()
			throws Exception {
		String testDirName = "testFindMetadataValuesByMetadataQueryTwoConditions";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdtwocondattrib1";
		String expectedAttribValue = "testmdtwocondvalue1";
		String expectedAttribUnits = "test1mdtwocondunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribValue));

		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
	}

	@Test
	public void testAddAvuMetadata() throws Exception {
		String testDirName = "testAddAvuMetadataDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);


		// verify the metadata was added
		// now get back the avu data and make sure it's there
		ImetaListCommand imetaList = new ImetaListCommand();
		imetaList.setAttribName(expectedAttribName);
		imetaList.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaList.setObjectPath(targetIrodsCollection);
		String metaValues = invoker
				.invokeCommandAndGetResultAsString(imetaList);
		Assert.assertTrue("did not find expected attrib name",
				metaValues.indexOf(expectedAttribName) > -1);
		Assert.assertTrue("did not find expected attrib value",
				metaValues.indexOf(expectedAttribValue) > -1);

	}
	
	@Test(expected=DataNotFoundException.class)
	public void testAddAvuMetadataMissingCollection() throws Exception {
		String testDirName = "testAddAvuMetadataMissingCollection";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);
	}
	
	@Test
	public void testAddDuplicateAvuMetadata() throws Exception {
		String testDirName = "testAddDuplicateAvuMetadata";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

	}
	
	@Test(expected=DuplicateDataException.class)
	public void testAddDuplicateAvuMetadataWithUnits() throws Exception {
		String testDirName = "testAddDuplicateAvuMetadata";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedUnitsValue = "testunits1";


		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedUnitsValue);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

	}
	
	@Test
	public void testAddAvuMetadataWithBarInVals() throws Exception {
		String testDirName = "testAddAvuMetadataWithBarInVals";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1|somemore";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		// verify the metadata was added
		// now get back the avu data and make sure it's there
		ImetaListCommand imetaList = new ImetaListCommand();
		imetaList.setAttribName(expectedAttribName);
		imetaList.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaList.setObjectPath(targetIrodsCollection);
		String metaValues = invoker
				.invokeCommandAndGetResultAsString(imetaList);
		Assert.assertTrue("did not find expected attrib name",
				metaValues.indexOf(expectedAttribName) > -1);
		Assert.assertTrue("did not find expected attrib value",
				metaValues.indexOf(expectedAttribValue) > -1);

	}
	
	@Test
	public void testAddAvuMetadataWithColonInArg() throws Exception {
		String testDirName = "testAddAvuMetadataWithColonInArg";
		String expectedAttribName = "testattrib1:testAttribAnother";
		String expectedAttribValue = "testvalue1|somemore";
		String expectedUnit = "test:thisHasAColon";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedUnit);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		// verify the metadata was added
		// now get back the avu data and make sure it's there
		ImetaListCommand imetaList = new ImetaListCommand();
		imetaList.setAttribName(expectedAttribName);
		imetaList.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaList.setObjectPath(targetIrodsCollection);
		String metaValues = invoker
				.invokeCommandAndGetResultAsString(imetaList);
		Assert.assertTrue("did not find expected attrib name",
				metaValues.indexOf(expectedAttribName) > -1);
		Assert.assertTrue("did not find expected attrib value",
				metaValues.indexOf(expectedAttribValue) > -1);

	}
	
	@Test
	public void testMkdirThenAddAvuMetadataWithColonInArg() throws Exception {
		String testDirName = "testMkdirThenAddAvuMetadataWithColonInArg";
		String expectedAttribName = "testattrib1:testAttribAnother";
		String expectedAttribValue = "testvalue1|somemore";
		String expectedUnit = "test:thisHasAColon";

		String subdir = "subdir";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);


		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile dirFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection + "/" + subdir);
		dirFile.mkdirs();

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedUnit);
		collectionAO.addAVUMetadata(dirFile.getAbsolutePath(), dataToAdd);
		List<MetaDataAndDomainData> actualMetadata = collectionAO.findMetadataValuesForCollection(dirFile.getAbsolutePath());

		TestCase.assertEquals("did not get back metadata",1, actualMetadata.size());

	}

	@Test
	public void testRemoveAvuMetadata() throws Exception {
		String testDirName = "testRemoveAvuMetadataTestingDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile targetCollectionAsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetCollectionAsFile.mkdirs();

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		collectionAO.deleteAVUMetadata(targetIrodsCollection, dataToAdd);

		List<MetaDataAndDomainData> metadata = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection, 0);

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertFalse("did not expect attrib name", metadataEntry
					.getAvuAttribute().equals(expectedAttribName));
		}

	}

	@Test
	public void testOverwriteAvuMetadata() throws Exception {
		String testDirName = "testOverwriteAvuMetadataTestingDir";
		String expectedAttribName = "testOverwriteAvuMetadataAttrib1";
		String expectedAttribValue = "testOverwriteAvuMetadataValue1";
		String expectedNewValue = "testOverwriteAvuMetadataValue1ThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile targetCollectionAsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetCollectionAsFile.mkdirs();

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, "");

		collectionAO.modifyAVUMetadata(targetIrodsCollection, dataToAdd, overwriteAvuData);

		List<MetaDataAndDomainData> metadata = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection, 0);
		
		TestCase.assertEquals("should only be one avu entry", 1, metadata.size());

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertEquals("did not find attrib name", expectedAttribName, metadataEntry
					.getAvuAttribute());
			Assert.assertEquals("did not find attrib val", expectedNewValue, metadataEntry
					.getAvuValue());
		}

	}
	
	@Test
	public void testOverwriteAvuMetadataGivenNameAndUnit() throws Exception {
		String testDirName = "testOverwriteAvuMetadataGivenNameAndUnit";
		String expectedAttribName = "testOverwriteAvuMetadataGivenNameAndUnitAttrib1";
		String expectedAttribValue = "testOverwriteAvuMetadataGivenNameAndUnitValue1";
		String expectedAttribUnit = "testOverwriteAvuMetadataGivenNameAndUnitUnit1";
		String expectedNewValue = "testOverwriteAvuMetadataGivenNameAndUnitThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile targetCollectionAsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetCollectionAsFile.mkdirs();

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnit);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);
		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, expectedAttribUnit);
		collectionAO.modifyAvuValueBasedOnGivenAttributeAndUnit(targetIrodsCollection, overwriteAvuData);
		List<MetaDataAndDomainData> metadata = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection, 0);
		
		TestCase.assertEquals("should only be one avu entry", 1, metadata.size());

		for (MetaDataAndDomainData metadataEntry : metadata) {
			Assert.assertEquals("did not find attrib name", expectedAttribName, metadataEntry
					.getAvuAttribute());
			Assert.assertEquals("did not find attrib val", expectedNewValue, metadataEntry
					.getAvuValue());
			Assert.assertEquals("did not find attrib unit", expectedAttribUnit, metadataEntry
					.getAvuUnit());
			
		}

	}
	
	@Test(expected=DataNotFoundException.class)
	public void testOverwriteAvuMetadataGivenNameAndUnitNoVals() throws Exception {
		String testDirName = "testOverwriteAvuMetadataGivenNameAndUnitNoVals";
		String expectedAttribName = "testOverwriteAvuMetadataGivenNameAndUnitAttrib1";
		String expectedAttribUnit = "testOverwriteAvuMetadataGivenNameAndUnitUnit1";
		String expectedNewValue = "testOverwriteAvuMetadataGivenNameAndUnitThatsOverwriten";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		IRODSFile targetCollectionAsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetCollectionAsFile.mkdirs();

		AvuData overwriteAvuData = AvuData.instance(expectedAttribName, expectedNewValue, expectedAttribUnit);
		collectionAO.modifyAvuValueBasedOnGivenAttributeAndUnit(targetIrodsCollection, overwriteAvuData);
		
	}
	
	
	@Test
	public void testRemoveAvuMetadataAvuDataDoesNotExist() throws Exception {
		String testDirName = "testRemoveAvuMetadataAvuDataDoesNotExistDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");

		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);
	}

	@Test(expected = DataNotFoundException.class)
	public void testRemoveAvuMetadataCollectionNotExists() throws Exception {
		String testDirName = "testRemoveAvuMetadataIDontExistDir";
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.deleteAVUMetadata(targetIrodsCollection, dataToAdd);

	}

	@Test
	public void testRewriteAvuMetadata() throws Exception {
		String testDirName = "testRewriteAvuMetadataDir";
		String expectedAttribName = "testrwattrib1";
		String expectedAttribValue = "testrwvalue1";
		String expectedNewAttribValue = "testrwvalue1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		AvuData.instance(expectedAttribValue, expectedNewAttribValue, "");

		// now get back the avu data and make sure it's there
		ImetaListCommand imetaList = new ImetaListCommand();
		imetaList.setAttribName(expectedAttribName);
		imetaList.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaList.setObjectPath(targetIrodsCollection);
		String metaValues = invoker
				.invokeCommandAndGetResultAsString(imetaList);
		Assert.assertTrue("did not find expected attrib name",
				metaValues.indexOf(expectedAttribName) > -1);
		Assert.assertTrue("did not find expected attrib value",
				metaValues.indexOf(expectedNewAttribValue) > -1);
	}

	@Test
	public final void findMetadataValuesByMetadataQueryWithAdditionalWhereCollectionLike()
			throws Exception {
		String testDirName = "testCollectionMetadataQueryWithLike";
		String testSubdir1 = "subdir1";
		String testSubdir2 = "subdir2";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection + "/"
				+ testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection + "/"
				+ testSubdir2);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection + "/"
				+ testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection + "/"
				+ testSubdir2);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand
				.setObjectPath(targetIrodsCollection + "/" + testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);
		imetaAddCommand
				.setObjectPath(targetIrodsCollection + "/" + testSubdir2);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(" LIKE ");
		sb.append("'");
		sb.append(targetIrodsCollection);
		sb.append("%");
		sb.append("'");

		List<MetaDataAndDomainData> queryResults = collectionAO
				.findMetadataValuesByMetadataQueryWithAdditionalWhere(
						queryElements, sb.toString());

		Assert.assertFalse("no query result returned", queryResults.isEmpty());
	}

	@Test
	public final void findMetadataValuesForCollection() throws Exception {
		String testDirName = "findMetadataValuesForCollection";
		String testSubdir1 = "subdir1";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection + "/"
				+ testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		// initialize the AVU data
		String expectedAttribName = "testmdforcollectionattrib1";
		String expectedAttribValue = "testmdforcollectionvalue1";
		String expectedAttribUnits = "test1mdforcollectionunits";

		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaRemoveCommand.setObjectPath(targetIrodsCollection + "/"
				+ testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.COLLECTION_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand
				.setObjectPath(targetIrodsCollection + "/" + testSubdir1);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		List<MetaDataAndDomainData> queryResults = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection + "/"
						+ testSubdir1, 0);

		Assert.assertFalse("no query result returned", queryResults.isEmpty());
	}

	@Test
	public void findByAbsolutePath() throws Exception {
		String testDirName = "findByAbsolutePath";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// put scratch collection into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(targetIrodsCollection);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(imkdirCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		Collection collection = collectionAO
				.findByAbsolutePath(targetIrodsCollection);
		TestCase.assertNotNull("did not find the collection, was null",
				collection);
	}

	@Test(expected=DataNotFoundException.class)
	public void findByAbsolutePathNotExists() throws Exception {
		String testDirName = "findByAbsolutePathNotExists";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		collectionAO
				.findByAbsolutePath(targetIrodsCollection);
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void findByAbsolutePathNullPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		collectionAO.findByAbsolutePath(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCountAllFilesUnderneathCollectionNullFile()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.countAllFilesUnderneathTheGivenCollection(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCountAllFilesUnderneathCollectionEmptyFile()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO.countAllFilesUnderneathTheGivenCollection("");

	}

	@Test
	public void testPutCollectionWithTwoFilesAndCountThem() throws Exception {

		String rootCollection = "testPutCollectionWithTwoFilesAndCountThem";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutCollectionWithTwoFiles", 1, 1, 1, "testFile",
						".txt", 2, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
		destFile.close();

		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ rootCollection);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		int count = collectionAO
				.countAllFilesUnderneathTheGivenCollection(destFile
						.getAbsolutePath());

		TestCase.assertEquals("did not get expected file count", 2, count);

	}

	@Test
	public void testFindDomainByMetadataQueryWithTwoAVUQueryElements()
			throws Exception {

		// FIXME: add check of version, don't test before 2.4.1 as this was a
		// bug in iRODS
		String testDirName = "testFindDomainByMetadataQueryWithTwoAVUQueryElements";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// initialize the AVU data
		String expectedAttribName = "avujfiejf1221";
		String expectedAttribValue = "avujfiejf1221value1";
		String expectedAttribUnits = "avujfiejf1221units";

		String expectedAttribName2 = "avujfiejf1222";
		String expectedAttribValue2 = "avujfiejf1221value2";
		String expectedAttribUnits2 = "avujfiejf1221units2";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for post 2.4.1 
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		IRODSFile testCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		testCollection.mkdirs();

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.addAVUMetadata(testCollection.getAbsolutePath(), avuData);

		avuData = AvuData.instance(expectedAttribName2, expectedAttribValue2,
				expectedAttribUnits2);
		collectionAO.addAVUMetadata(testCollection.getAbsolutePath(), avuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribValue));

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName2));

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribValue2));

		List<Collection> result = collectionAO
				.findDomainByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());
		Assert.assertEquals(targetIrodsCollection, result.get(0)
				.getCollectionName());

	}

	@Test
	public final void testSetRead() throws Exception {

		String testFileName = "testSetRead";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		// log in as the secondary user and test read access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		TestCase.assertTrue(irodsFileForSecondaryUser.canRead());

	}

	@Test(expected = JargonException.class)
	public final void testSetReadFileNotExist() throws Exception {

		String testFileName = "testSetReadIDontExist";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

	}

	@Test
	public final void testSetWrite() throws Exception {

		String testFileName = "testSetWrite";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionWrite(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						false);

		// log in as the secondary user and test write access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		TestCase.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetOwn() throws Exception {

		String testFileName = "testSetOwn";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionOwn(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						false);

		// log in as the secondary user and test write access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		IRODSFileSystemAO irodsFileSystemAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getIRODSFileSystemAO(
						secondaryAccount);
		int permissions = irodsFileSystemAO
				.getDirectoryPermissions(irodsFileForSecondaryUser);

		TestCase.assertTrue(permissions >= IRODSFile.OWN_PERMISSIONS);

	}

	@Test
	public final void testGetFilePermissionForOwn() throws Exception {

		String testFileName = "testGetFilePermissionForOwn";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionOwn(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						false);

		// log in as the secondary user and test write access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		CollectionAO collectionAOSecondary = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAO(secondaryAccount);
		FilePermissionEnum enumVal = collectionAOSecondary
				.getPermissionForCollection(targetIrodsCollection,
						secondaryAccount.getUserName(), "");

		TestCase.assertEquals("should have found own permissions",
				FilePermissionEnum.OWN, enumVal);

	}
	
	@Test
	public final void testGetFilePermissionForOtherUserWhoHasRead() throws Exception {

		String testFileName = "testGetFilePermissionForOtherUserWhoHasRead";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						false);

		// log in as the secondary user and test write access
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		FilePermissionEnum enumVal = collectionAO
				.getPermissionForCollection(targetIrodsCollection,
						secondaryAccount.getUserName(), "");

		TestCase.assertEquals("should have found read permissions",
				FilePermissionEnum.READ, enumVal);

	}

	@Test
	public final void testSetInherit() throws Exception {

		String testFileName = "testSetInherit";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO.setAccessPermissionInherit("", targetIrodsCollection,
				false);

		boolean isInherit = collectionAO
				.isCollectionSetForPermissionInheritance(targetIrodsCollection);

		TestCase.assertTrue("collection should have inherit set", isInherit);

	}

	@Test
	public final void testSetNoInherit() throws Exception {

		String testFileName = "testSetNoInherit";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO.setAccessPermissionInherit("", targetIrodsCollection,
				false);
		collectionAO.setAccessPermissionToNotInherit("", targetIrodsCollection,
				false);

		boolean isInherit = collectionAO
				.isCollectionSetForPermissionInheritance(targetIrodsCollection);

		TestCase.assertFalse("collection should have inherit turned back off",
				isInherit);

	}
	
	@Test
	public final void testGetPermissionsForCollection() throws Exception {

		String testCollectionName = "testGetPermissionsForCollection";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		List<UserFilePermission> userFilePermissions = collectionAO.listPermissionsForCollection(targetIrodsCollection);
		TestCase.assertNotNull("got a null userFilePermissions", userFilePermissions);
		TestCase.assertEquals("did not find the two permissions", 2, userFilePermissions.size());
	}


}
