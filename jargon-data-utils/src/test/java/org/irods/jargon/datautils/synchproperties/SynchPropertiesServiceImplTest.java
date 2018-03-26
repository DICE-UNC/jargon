package org.irods.jargon.datautils.synchproperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class SynchPropertiesServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test
	public void testGetUserSynchTargetForUserAndAbsolutePath() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 949493049304L;
		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		UserSynchTarget userSynchTarget = synchPropertiesService
				.getUserSynchTargetForUserAndAbsolutePath(testUserName,
						testDeviceName, testIrodsPath);
		Assert.assertNotNull("null userSynchTarget returned", userSynchTarget);
		Assert.assertEquals("invalid user", testUserName,
				userSynchTarget.getUserName());
		Assert.assertEquals("invalid device", testDeviceName,
				userSynchTarget.getDeviceName());
		Assert.assertEquals("invalid irods path", testIrodsPath,
				userSynchTarget.getIrodsSynchRootAbsolutePath());
		Assert.assertEquals("invalid local path", expectedLocalPath,
				userSynchTarget.getLocalSynchRootAbsolutePath());
		Assert.assertEquals("invalid local timestamp", expectedLocalTimestamp,
				userSynchTarget.getLastLocalSynchTimestamp());
		Assert.assertEquals("invalid irods timestamp", expectedIrodsTimestamp,
				userSynchTarget.getLastIRODSSynchTimestamp());
	}

	@Test(expected = JargonException.class)
	public void testGetUserSynchTargetNoAccount() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = null;
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.getUserSynchTargetForUserAndAbsolutePath(
				testUserName, testDeviceName, testIrodsPath);

	}

	@Test(expected = JargonException.class)
	public void testGetUserSynchTargetNoAccessObjectFactory() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = null;

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.getUserSynchTargetForUserAndAbsolutePath(
				testUserName, testDeviceName, testIrodsPath);

	}

	@Test(expected = JargonException.class)
	public void testGetUserSynchTargetForUserAndAbsolutePathMultipleResults()
			throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 949493049304L;
		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		queryResults.add(testResult);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.getUserSynchTargetForUserAndAbsolutePath(
				testUserName, testDeviceName, testIrodsPath);

	}

	@Test(expected = JargonException.class)
	public void testGetUserSynchTargetForUserAndAbsolutePathNonNumericIrodsTimestamp()
			throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append("1121212xx");
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.getUserSynchTargetForUserAndAbsolutePath(
				testUserName, testDeviceName, testIrodsPath);

	}

	@Test(expected = JargonException.class)
	public void testGetUserSynchTargetForUserAndAbsolutePathNonNumericLocalTimestamp()
			throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append("1121212");
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append("484848d");
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.getUserSynchTargetForUserAndAbsolutePath(
				testUserName, testDeviceName, testIrodsPath);

	}

	@Test(expected = DuplicateDataException.class)
	public void testAddUserSynchTargetWhenAlreadyExists() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 949493049304L;
		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.addSynchDeviceForUserAndIrodsAbsolutePath(
				testUserName, testDeviceName, testIrodsPath, expectedLocalPath);
	}

	@Test
	public void testAddUserSynchTarget() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 0L;
		long expectedLocalTimestamp = 0L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		// mock out lookup of file, which will exist here
		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);
		Mockito.when(irodsFile.exists()).thenReturn(true);

		Mockito.when(irodsFileFactory.instanceIRODSFile(testIrodsPath))
				.thenReturn(irodsFile);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.addSynchDeviceForUserAndIrodsAbsolutePath(
				testUserName, testDeviceName, testIrodsPath, expectedLocalPath);

		AvuData expectedAvuData = AvuData.instance(testUserName + ":"
				+ testDeviceName, 0 + "~" + 0 + "~" + expectedLocalPath,
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		Mockito.verify(collectionAO).addAVUMetadata(testIrodsPath,
				expectedAvuData);

	}

	@Test
	public void testSynchTimestamps() throws Exception {

		long expectedIrodsTimestamp = 949493049304L;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		EnvironmentalInfoAO environmentalInfoAO = Mockito
				.mock(EnvironmentalInfoAO.class);
		Mockito.when(
				irodsAccessObjectFactory.getEnvironmentalInfoAO(irodsAccount))
				.thenReturn(environmentalInfoAO);
		Mockito.when(environmentalInfoAO.getIRODSServerCurrentTime())
				.thenReturn(expectedIrodsTimestamp);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		SynchTimestamps synchTimestamps = synchPropertiesService
				.getSynchTimestamps();
		Assert.assertNotNull("null synchTimestamps returned", synchTimestamps);
		Assert.assertEquals("invalid irods timestamp", expectedIrodsTimestamp,
				synchTimestamps.getIrodsSynchTimestamp());

	}

	@Test
	public void testUpdateTimestampsToCurrent() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 949493049304L;
		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL,
				userDevAttrib.toString());
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						avuQuery, testIrodsPath)).thenReturn(queryResults);

		EnvironmentalInfoAO environmentalInfoAO = Mockito
				.mock(EnvironmentalInfoAO.class);
		Mockito.when(
				irodsAccessObjectFactory.getEnvironmentalInfoAO(irodsAccount))
				.thenReturn(environmentalInfoAO);
		Mockito.when(environmentalInfoAO.getIRODSServerCurrentTime())
				.thenReturn(expectedIrodsTimestamp);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		synchPropertiesService.updateTimestampsToCurrent(testUserName,
				testDeviceName, testIrodsPath);
		Mockito.verify(collectionAO)
				.modifyAvuValueBasedOnGivenAttributeAndUnit(
						Matchers.eq(testIrodsPath), Matchers.any(AvuData.class));

	}

	@Test
	public void testGetUserSynchTargets() throws Exception {

		String testUserName = "testUser";
		String testDeviceName = "testDevice";
		String testIrodsPath = "/path/to/irods";

		long expectedIrodsTimestamp = 949493049304L;
		long expectedLocalTimestamp = 8483483948394L;
		String expectedLocalPath = "/a/local/path";

		StringBuilder userDevAttrib = new StringBuilder();
		userDevAttrib.append(testUserName);
		userDevAttrib.append(":");
		userDevAttrib.append(testDeviceName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// build expected query
		List<AVUQueryElement> avuQuery = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.UNITS,
						QueryConditionOperators.EQUAL,
						SynchPropertiesService.USER_SYNCH_DIR_TAG);
		avuQuery.add(avuQueryElement);
		avuQueryElement = AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, QueryConditionOperators.LIKE,
				testUserName + ":%");
		avuQuery.add(avuQueryElement);

		StringBuilder anticipatedAvuValue = new StringBuilder();
		anticipatedAvuValue.append(expectedIrodsTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalTimestamp);
		anticipatedAvuValue.append("~");
		anticipatedAvuValue.append(expectedLocalPath);

		List<MetaDataAndDomainData> queryResults = new ArrayList<MetaDataAndDomainData>();
		MetaDataAndDomainData testResult = MetaDataAndDomainData.instance(
				MetaDataAndDomainData.MetadataDomain.COLLECTION, "1",
				testIrodsPath, 1, userDevAttrib.toString(),
				anticipatedAvuValue.toString(),
				SynchPropertiesService.USER_SYNCH_DIR_TAG);
		queryResults.add(testResult);
		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(avuQuery))
				.thenReturn(queryResults);

		SynchPropertiesServiceImpl synchPropertiesService = new SynchPropertiesServiceImpl();
		synchPropertiesService
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		synchPropertiesService.setIrodsAccount(irodsAccount);
		List<UserSynchTarget> userSynchTargets = synchPropertiesService
				.getUserSynchTargets(testUserName);

		Assert.assertNotNull("null userSynchTarget returned", userSynchTargets);
		Assert.assertEquals("should be one synch target", 1,
				userSynchTargets.size());
		UserSynchTarget userSynchTarget = userSynchTargets.get(0);
		Assert.assertEquals("invalid user", testUserName,
				userSynchTarget.getUserName());
		Assert.assertEquals("invalid device", testDeviceName,
				userSynchTarget.getDeviceName());
		Assert.assertEquals("invalid irods path", testIrodsPath,
				userSynchTarget.getIrodsSynchRootAbsolutePath());
		Assert.assertEquals("invalid local path", expectedLocalPath,
				userSynchTarget.getLocalSynchRootAbsolutePath());
		Assert.assertEquals("invalid local timestamp", expectedLocalTimestamp,
				userSynchTarget.getLastLocalSynchTimestamp());
		Assert.assertEquals("invalid irods timestamp", expectedIrodsTimestamp,
				userSynchTarget.getLastIRODSSynchTimestamp());
	}

}
