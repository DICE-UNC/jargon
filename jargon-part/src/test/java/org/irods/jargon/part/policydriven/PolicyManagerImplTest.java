package org.irods.jargon.part.policydriven;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.TranslatedIRODSQuery;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class PolicyManagerImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	@Test
	public void testListPolicyRepositories() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"PolicyRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyManager
				.listPolicyRepositories();
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals("PolicyRepository", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());
	}

	public void testAddPolicyRepository() {
		// FIXME: implement??
	}

	@Test
	public void testListPoliciesInRepository() throws Exception {

		String policyRepoName = "PolicyRepository";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		// set up the policy repository
		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								policyRepoName, "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// set up query response
		IRODSGenQueryExecutor queryExecutor = mock(IRODSGenQueryExecutor.class);

		IRODSQueryResultSet myResultSet = mock(IRODSQueryResultSet.class);
		TranslatedIRODSQuery translatedIRODSQuery = mock(TranslatedIRODSQuery.class);
		IRODSQueryResultRow irodsQueryResultRow = mock(IRODSQueryResultRow.class);
		Mockito.when(irodsQueryResultRow.getColumn(Mockito.anyInt()))
				.thenReturn("hello");
		List<IRODSQueryResultRow> resultRows = new ArrayList<IRODSQueryResultRow>();
		resultRows.add(irodsQueryResultRow);

		Mockito.when(myResultSet.getResults()).thenReturn(resultRows);
		Mockito.when(
				queryExecutor.executeIRODSQuery(Mockito.any(IRODSQuery.class),
						Mockito.anyInt())).thenReturn(myResultSet);
		Mockito
				.when(
						irodsAccessObjectFactory
								.getIRODSGenQueryExecutor(irodsAccount))
				.thenReturn(queryExecutor);

		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyManager
				.listPoliciesInPolicyRepository(policyRepoName);

		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals("hello", entry1.getPolicyDrivenServiceName());
		// collection name should be munged with data object name
		Assert.assertEquals("hello/hello", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("hello", entry1.getComment());
	}

	@Test
	public void testAddPolicy() throws Exception {

		String testPolicyRepName = "policyrep1";
		String testPathPath = "/a/path/to/somepolicy";
		String comment = "comment here";
		String policyName = "A Policy Name";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		DataObjectAO dataObjectAO = mock(DataObjectAO.class);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		// policy repo

		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		elements.add(element);

		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testPolicyRepName);
		elements.add(element);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								testPathPath,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								testPolicyRepName, comment));
		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		// policy in repo should be unique
		elements = new ArrayList<AVUQueryElement>();

		element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE);
		elements.add(element);

		element = AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, policyName);
		elements.add(element);

		metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		// collection for repository exists and can write
		IRODSFile mockCollection = Mockito.mock(IRODSFile.class);
		Mockito.when(mockCollection.isDirectory()).thenReturn(true);
		Mockito.when(mockCollection.exists()).thenReturn(true);
		Mockito
				.when(
						collectionAO.instanceIRODSFileForCollectionPath(Mockito
								.anyString())).thenReturn(mockCollection);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testPathPath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		ObjectToXMLMarshaller marshaller = mock(ObjectToXMLMarshaller.class);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		IRODSFileFactory mockFactory = mock(IRODSFileFactory.class);
		Mockito
				.when(
						irodsAccessObjectFactory
								.getIRODSFileFactory(irodsAccount)).thenReturn(
						mockFactory);
		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		Policy policy = new Policy();
		policy.setPolicyName(policyName);
		IRODSFileWriter irodsFileWriter = Mockito.mock(IRODSFileWriter.class);

		policyManager.addPolicyToRepository(testPolicyRepName, policy,
				marshaller);

	}

	@Test
	public void testFindPolicyRepository() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		DataObjectAO dataObjectAO = mock(DataObjectAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"PolicyRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		PolicyDrivenServiceListingEntry entry = policyManager
				.findPolicyRepository("PolicyRepository");
		Assert.assertEquals("PolicyRepository", entry
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry.getComment());
	}

	@Test
	public void testFindPolicyBasicData() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		DataObjectAO dataAO = mock(DataObjectAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.DATA,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
								"PolicyName", "CommentHere"));
		
		Mockito.when(dataAO.findMetadataValuesByMetadataQuery(Matchers.anyList()))
				.thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataAO);

		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		PolicyDrivenServiceListingEntry entry = policyManager
				.findPolicyBasicData("PolicyName");
		Assert.assertEquals("PolicyName", entry
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry.getComment());
	}

	@Test
	public void testGetPolicyFromPolicyRepository() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		DataObjectAO dataAO = mock(DataObjectAO.class);
		
		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.DATA,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
								"PolicyName", "CommentHere"));
		
		Mockito.when(dataAO.findMetadataValuesByMetadataQuery(Matchers.anyList()))
				.thenReturn(metaDataAndDomainData);

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
		
		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		Policy returnedPolicy = policyManager.getPolicyFromPolicyRepository("apolicy", unmarshaller);
		Mockito.verify(irodsFileFactory).instanceIRODSFileReader("/a/path/file.txt");
		
		}
	
	@Test
	public void testListAllPolicies() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		DataObjectAO dataObjectAO = mock(DataObjectAO.class);
		
		String testPolicyName = "Policy Here";
		String testPolicyPath = "/a/path/data.xml";

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.DATA,
								"1",
								testPolicyPath,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_MARKER_ATTRIBUTE,
								testPolicyName, "CommentHere"));
		Mockito.when(
				dataObjectAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		PolicyManager policyManager = new PolicyManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyManager
				.listAllPolicies();
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals(testPolicyName, entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals(testPolicyPath, entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());
	}

}
