package org.irods.jargon.part.policydriven;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class SeriesManagerImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	@Test
	public void testListSeries() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		String testServiceName = "testServiceName";

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE,
								testServiceName, "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		XMLToObjectUnmarshaller unmarshaller = Mockito
				.mock(XMLToObjectUnmarshaller.class);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		List<PolicyDrivenServiceListingEntry> listingEntries = seriesManager
				.listSeries(testServiceName);
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals(testServiceName, entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());

	}

	@Test
	public void testCheckIfSeriesIsUniqueWhenNameNotUnique() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		String testServiceName = "testServiceName";
		String testCollectionPath = "/a/collection/path/to/a/series";

		final List<AVUQueryElement> queryForSeriesName = new ArrayList<AVUQueryElement>();

		queryForSeriesName
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		queryForSeriesName.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		List<MetaDataAndDomainData> metaDataAndDomainDataForSeriesName = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForSeriesName
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/another/collection",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE,
								testServiceName, "CommentHere"));

		Mockito.when(
				collectionAO
						.findMetadataValuesByMetadataQuery(queryForSeriesName))
				.thenReturn(metaDataAndDomainDataForSeriesName);

		XMLToObjectUnmarshaller unmarshaller = Mockito
				.mock(XMLToObjectUnmarshaller.class);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		boolean unique = seriesManager.checkIfSeriesIsUnique(testServiceName,
				testCollectionPath);
		TestCase.assertFalse("duplicate name not detected", unique);

	}

	@Test
	public void testCheckIfSeriesIsUniqueWhenCollectionNotUnique()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		String testServiceName = "testServiceName";
		String testCollectionPath = "/a/collection/path/to/a/series";

		final List<AVUQueryElement> queryForSeriesName = new ArrayList<AVUQueryElement>();

		queryForSeriesName
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		queryForSeriesName.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		List<MetaDataAndDomainData> metaDataAndDomainDataForSeriesName = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO
						.findMetadataValuesByMetadataQuery(queryForSeriesName))
				.thenReturn(metaDataAndDomainDataForSeriesName);

		final List<AVUQueryElement> queryForCollection = new ArrayList<AVUQueryElement>();

		queryForCollection
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		List<MetaDataAndDomainData> metaDataAndDomainDataForCollection = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForSeriesName
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								testCollectionPath,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE,
								"another service", "CommentHere"));

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						queryForCollection, testCollectionPath)).thenReturn(
				metaDataAndDomainDataForCollection);

		XMLToObjectUnmarshaller unmarshaller = Mockito
				.mock(XMLToObjectUnmarshaller.class);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		boolean unique = seriesManager.checkIfSeriesIsUnique(testServiceName,
				testCollectionPath);
		TestCase.assertFalse("duplicate collection not detected", unique);

	}

	@Test
	public void testCheckIfSeriesIsUniqueWhenUnique() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		String testServiceName = "testServiceName";
		String testCollectionPath = "/a/collection/path/to/a/series";

		final List<AVUQueryElement> queryForSeriesName = new ArrayList<AVUQueryElement>();

		queryForSeriesName
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		queryForSeriesName.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		List<MetaDataAndDomainData> metaDataAndDomainDataForSeriesName = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO
						.findMetadataValuesByMetadataQuery(queryForSeriesName))
				.thenReturn(metaDataAndDomainDataForSeriesName);

		final List<AVUQueryElement> queryForCollection = new ArrayList<AVUQueryElement>();

		queryForCollection
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		List<MetaDataAndDomainData> metaDataAndDomainDataForCollection = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						queryForCollection, testCollectionPath)).thenReturn(
				metaDataAndDomainDataForCollection);

		XMLToObjectUnmarshaller unmarshaller = Mockito
				.mock(XMLToObjectUnmarshaller.class);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		boolean unique = seriesManager.checkIfSeriesIsUnique(testServiceName,
				testCollectionPath);
		TestCase.assertTrue("not identified as unique", unique);

	}

	@Test
	public void testAddUniqueSeries() throws Exception {		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		String testSeriesName = "testSeriesName";
		String testCollectionPath = "/a/collection/path/to/a/series";
		String testServiceName = "archiveService1";
		String testPolicy = "testPolicy";
		String testPolicyRepository = "testPolicyRepository";
		
		// initialize to find the service
		
		List<MetaDataAndDomainData> metaDataAndDomainDataForService = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForService.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", testServiceName, "CommentHere"));

		// pre-stage for service lookup
		// query for specific policy-driven service entry
		List<AVUQueryElement> queryForService = new ArrayList<AVUQueryElement>();

		queryForService
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE));
		queryForService.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(queryForService))
				.thenReturn(metaDataAndDomainDataForService);
		
		// find the policy
		DataObjectAO dataAO = mock(DataObjectAO.class);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
		.thenReturn(dataAO);
		
		final List<AVUQueryElement> avuQueryForPolicy = new ArrayList<AVUQueryElement>();


		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
		avuQueryForPolicy.add(element);
		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testPolicy);
		avuQueryForPolicy.add(element);

		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testPolicy);
		avuQueryForPolicy.add(element);

		List<MetaDataAndDomainData> metaDataAndDomainDataForPolicy = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForPolicy
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.DATA,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
								testPolicy, "CommentHere"));
		
		Mockito.when(dataAO.findMetadataValuesByMetadataQuery(avuQueryForPolicy))
				.thenReturn(metaDataAndDomainDataForPolicy);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataAO);
		
		IRODSFile policyFile = Mockito.mock(IRODSFile.class);
		Mockito.when(policyFile.getAbsolutePath()).thenReturn("/a/path/file.txt");
		Mockito.when(dataAO.instanceIRODSFileForPath(Matchers.anyString())).thenReturn(policyFile);
		IRODSFileFactory irodsFileFactory = Mockito.mock(IRODSFileFactory.class);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(Matchers.any(IRODSAccount.class))).thenReturn(irodsFileFactory);
		IRODSFileReader reader = Mockito.mock(IRODSFileReader.class);
		Mockito.when(irodsFileFactory.instanceIRODSFileReader(Matchers.anyString())).thenReturn(reader);
		XMLToObjectUnmarshaller unmarshaller = Mockito.mock(XMLToObjectUnmarshaller.class);
		Policy policy = new Policy();
		Mockito.when(unmarshaller.unmarshallXMLToPolicy(Matchers.any(IRODSFileReader.class))).thenReturn(policy);
		
		// series stuff to make it unique
	
		final List<AVUQueryElement> queryForSeriesName = new ArrayList<AVUQueryElement>();

		queryForSeriesName
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		queryForSeriesName.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		List<MetaDataAndDomainData> metaDataAndDomainDataForSeriesName = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO
						.findMetadataValuesByMetadataQuery(queryForSeriesName))
				.thenReturn(metaDataAndDomainDataForSeriesName);

		final List<AVUQueryElement> queryForCollection = new ArrayList<AVUQueryElement>();

		queryForCollection
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		List<MetaDataAndDomainData> metaDataAndDomainDataForCollection = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						queryForCollection, testCollectionPath)).thenReturn(
								metaDataAndDomainDataForSeriesName);
		
		// set up for the creation of the series collection
		// collection for repository exists and can write
		IRODSFile mockCollection = Mockito.mock(IRODSFile.class);
		Mockito.when(mockCollection.isDirectory()).thenReturn(true);
		Mockito.when(mockCollection.exists()).thenReturn(true);
		Mockito
				.when(
						collectionAO.instanceIRODSFileForCollectionPath(Mockito
								.anyString())).thenReturn(mockCollection);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		Series testSeries = new Series();
		testSeries.setBoundPolicyName(testPolicy);
		testSeries.setCollectionAbsolutePath(testCollectionPath);
		testSeries.setContainingServiceName(testServiceName);
		testSeries.setDescription("a test series");
		testSeries.setName(testSeriesName);
		
		seriesManager.addSeriesToApplication(testSeries, unmarshaller);
		
	}
	
	@Test
	public void testAddSeriesCheckSeriesAvu() throws Exception {		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		String testSeriesName = "testSeriesName";
		String testCollectionPath = "/a/collection/path/to/a/series";
		String testServiceName = "archiveService1";
		String testPolicy = "testPolicy";
		String testPolicyRepository = "testPolicyRepository";
		
		// initialize to find the service
		
		List<MetaDataAndDomainData> metaDataAndDomainDataForService = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForService.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", testServiceName, "CommentHere"));

		// pre-stage for service lookup
		// query for specific policy-driven service entry
		List<AVUQueryElement> queryForService = new ArrayList<AVUQueryElement>();

		queryForService
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE));
		queryForService.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(queryForService))
				.thenReturn(metaDataAndDomainDataForService);
		
		// find the policy
		DataObjectAO dataAO = mock(DataObjectAO.class);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
		.thenReturn(dataAO);
		
		final List<AVUQueryElement> avuQueryForPolicy = new ArrayList<AVUQueryElement>();


		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
		avuQueryForPolicy.add(element);
		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testPolicy);
		avuQueryForPolicy.add(element);

		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testPolicy);
		avuQueryForPolicy.add(element);

		List<MetaDataAndDomainData> metaDataAndDomainDataForPolicy = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainDataForPolicy
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.DATA,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
								testPolicy, "CommentHere"));
		
		Mockito.when(dataAO.findMetadataValuesByMetadataQuery(avuQueryForPolicy))
				.thenReturn(metaDataAndDomainDataForPolicy);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataAO);
		
		IRODSFile policyFile = Mockito.mock(IRODSFile.class);
		Mockito.when(policyFile.getAbsolutePath()).thenReturn("/a/path/file.txt");
		Mockito.when(dataAO.instanceIRODSFileForPath(Matchers.anyString())).thenReturn(policyFile);
		IRODSFileFactory irodsFileFactory = Mockito.mock(IRODSFileFactory.class);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(Matchers.any(IRODSAccount.class))).thenReturn(irodsFileFactory);
		IRODSFileReader reader = Mockito.mock(IRODSFileReader.class);
		Mockito.when(irodsFileFactory.instanceIRODSFileReader(Matchers.anyString())).thenReturn(reader);
		XMLToObjectUnmarshaller unmarshaller = Mockito.mock(XMLToObjectUnmarshaller.class);
		Policy policy = new Policy();
		policy.setRequireVirusScan(true);
		policy.setRequireChecksum(true);
		policy.setRetentionDays("11/12/2010");
		Mockito.when(unmarshaller.unmarshallXMLToPolicy(Matchers.any(IRODSFileReader.class))).thenReturn(policy);
		
		// series stuff to make it unique
	
		final List<AVUQueryElement> queryForSeriesName = new ArrayList<AVUQueryElement>();

		queryForSeriesName
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		queryForSeriesName.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		List<MetaDataAndDomainData> metaDataAndDomainDataForSeriesName = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO
						.findMetadataValuesByMetadataQuery(queryForSeriesName))
				.thenReturn(metaDataAndDomainDataForSeriesName);

		final List<AVUQueryElement> queryForCollection = new ArrayList<AVUQueryElement>();

		queryForCollection
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_MARKER_ATTRIBUTE));

		List<MetaDataAndDomainData> metaDataAndDomainDataForCollection = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						queryForCollection, testCollectionPath)).thenReturn(
								metaDataAndDomainDataForSeriesName);
		
		// set up for the creation of the series collection
		// collection for repository exists and can write
		IRODSFile mockCollection = Mockito.mock(IRODSFile.class);
		Mockito.when(mockCollection.isDirectory()).thenReturn(true);
		Mockito.when(mockCollection.exists()).thenReturn(true);
		Mockito
				.when(
						collectionAO.instanceIRODSFileForCollectionPath(Mockito
								.anyString())).thenReturn(mockCollection);

		SeriesManager seriesManager = new SeriesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		Series testSeries = new Series();
		testSeries.setBoundPolicyName(testPolicy);
		testSeries.setCollectionAbsolutePath(testCollectionPath);
		testSeries.setContainingServiceName(testServiceName);
		testSeries.setDescription("a test series");
		testSeries.setName(testSeriesName);
		
		seriesManager.addSeriesToApplication(testSeries, unmarshaller);
		
		AvuData requireChecksumAvu = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "requireChecksum", "true");
		AvuData requireVirusScanAvu = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "requireVirusScan", "true");
		AvuData retentionDaysAvu = AvuData.instance(SeriesManager.SERIES_ATTRIBUTE_MARKER_ATTRIBUTE, "retentionDays", String.valueOf(policy.getRetentionDays()));

		Mockito.verify(collectionAO, Mockito.times(5)).addAVUMetadata(Mockito.eq(testSeries.getCollectionAbsolutePath()), Mockito.any(AvuData.class));

		//Mockito.verify(collectionAO).addAVUMetadata(Mockito.eq(testSeries.getCollectionAbsolutePath()), Mockito.eq(requireChecksumAvu));
	//	Mockito.verify(collectionAO).addAVUMetadata(Mockito.eq(testSeries.getCollectionAbsolutePath()), Mockito.eq(requireVirusScanAvu));
		//Mockito.verify(collectionAO).addAVUMetadata(Mockito.eq(testSeries.getCollectionAbsolutePath()), Mockito.eq(retentionDaysAvu));



		
		
	}

}
