package org.irods.jargon.part.policydriven;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class PolicyDrivenServiceManagerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();

	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testNullIRODSAccount() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		AbstractPolicyDrivenManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, null);
	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testNullIAccessObjectFactory() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		new PolicyDrivenServiceManagerImpl(null, irodsAccount);
	}

	@Test
	public void testFindPolicyDrivenServiceNames() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", "ServiceNameHere", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyDrivenServiceManager
				.findPolicyDrivenServiceNames("hello");
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals("ServiceNameHere", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());

	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testFindPolicyDrivenServiceNamesBlankName() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", "ServiceNameHere", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyDrivenServiceManager
				.findPolicyDrivenServiceNames("");
	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testFindPolicyDrivenServiceNamesNullName() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", "ServiceNameHere", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyDrivenServiceManager
				.findPolicyDrivenServiceNames(null);
	}

	@Test
	public void testGetPolicyDrivenServiceConfigFromServiceName()
			throws Exception {

		String testServiceName = "archiveService1";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData.add(MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "/collection/path",
				"PolicyDrivenService", testServiceName, "CommentHere"));

		// pre-stage for service lookup
		// query for specific policy-driven service entry
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();

		elements
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE));
		elements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// pre-stage rule repositories in mock
		metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/rule/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE,
								"Rule Mapping 1", "CommentHere"));

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE);
		elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// pre-stage policy repositories in mock
		metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/policy/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"Policy Mapping 1", "CommentHere"));
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"2",
								"/policy/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"Policy Mapping 2", "CommentHere"));

		element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		PolicyDrivenServiceConfig config = policyDrivenServiceManager
				.getPolicyDrivenServiceConfigFromServiceName(testServiceName);
		Assert.assertNotNull("no config returned", config);

		Assert.assertEquals("wrong config name", testServiceName, config
				.getServiceName());
		Assert.assertEquals("wrong number of rule mappings", 1, config
				.getRuleMetadataRepositoryPath().size());
		Assert.assertEquals("wrong number of policy repository mappings", 2,
				config.getPolicyDefinitionRepositoryPath().size());
	}

	@Test
	public void testGetRuleMappingsFromServiceName() throws Exception {

		String testServiceName = "archiveService1";

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
								"/rule/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE,
								"Rule Mapping 1", "CommentHere"));

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE);
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// preset rule repository lookup code
		// query for specific policy-driven service entry
		elements = new ArrayList<AVUQueryElement>();

		elements
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_MAPPING_MARKER_ATTRIBUTE));
		elements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		List<PolicyDrivenServiceListingEntry> ruleEntries = policyDrivenServiceManager
				.findServiceRuleRepositories(testServiceName);
		Assert.assertNotNull("no rule mappings returned", ruleEntries);
		Assert.assertTrue("no rule entries returned", ruleEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = ruleEntries.get(0);
		Assert.assertEquals("Rule Mapping 1", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/rule/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());
	}

	@Test
	public void testGetPolicyMappingsFromServiceName() throws Exception {

		String testServiceName = "archiveService1";

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
								"/policy/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"Policy Mapping 1", "CommentHere"));
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"2",
								"/policy/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE,
								"Policy Mapping 2", "CommentHere"));

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		// preset rule repository lookup code
		// query for specific policy-driven service entry
		elements = new ArrayList<AVUQueryElement>();

		elements
				.add(AVUQueryElement
						.instanceForValueQuery(
								AVUQueryPart.ATTRIBUTE,
								AVUQueryOperatorEnum.EQUAL,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE));
		elements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
				AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		List<PolicyDrivenServiceListingEntry> policyEntries = policyDrivenServiceManager
				.findServicePolicyRepositories(testServiceName);
		Assert.assertNotNull("no policy mappings returned", policyEntries);
		Assert.assertTrue("expected two policy entries",
				policyEntries.size() == 2);
		PolicyDrivenServiceListingEntry entry1 = policyEntries.get(0);
		Assert.assertEquals("Policy Mapping 1", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/policy/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());
	}

	@Test
	public void testAddValidServiceConfigWhenNoneExists() throws Exception {
		String testServiceName = "archiveService1";
		String testRootPath = "/this/is/a/path";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRootPath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceRootPath(testRootPath);
		policyDrivenServiceConfig.setServiceName(testServiceName);

		policyDrivenServiceManager
				.addPolicyDrivenService(policyDrivenServiceConfig);
		Mockito.verify(collectionFile, Mockito.atLeast(1)).mkdirs();


	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testAddDuplicateServiceConfigWhenAVUEntryExists()
			throws Exception {
		String testServiceName = "archiveService1";
		String testRootPath = "/this/is/a/path";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData.add(MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "domainObjectId",
						"domainObjectUniqueName", "avuAttribute", "avuValue",
						"avuUnit"));

		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE));
		elements.add(AVUQueryElement
				.instanceForValueQuery(AVUQueryPart.VALUE,
						AVUQueryOperatorEnum.EQUAL, testServiceName));

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRootPath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceRootPath(testRootPath);
		policyDrivenServiceConfig.setServiceName(testServiceName);

		policyDrivenServiceManager
				.addPolicyDrivenService(policyDrivenServiceConfig);

	}

	@Test(expected = PolicyDrivenServiceConfigException.class)
	public void testAddValidServiceConfigWhenRootPathMissing() throws Exception {
		String testServiceName = "archiveService1";
		String testRootPath = "/this/is/a/path";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRootPath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceRootPath("");
		policyDrivenServiceConfig.setServiceName(testServiceName);

		policyDrivenServiceManager
				.addPolicyDrivenService(policyDrivenServiceConfig);

	}
	
	@Test
	public void testAddValidServiceConfigWhenDirectoryExistsButNotAVU() throws Exception {
		String testServiceName = "archiveService1";
		String testRootPath = "/this/is/a/path";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();

		AVUQueryElement element = AVUQueryElement
				.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE,
						AVUQueryOperatorEnum.EQUAL,
						PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_POLICY_REPOSITORY_MARKER_ATTRIBUTE);
		List<AVUQueryElement> elements = new ArrayList<AVUQueryElement>();
		elements.add(element);

		Mockito.when(collectionAO.findMetadataValuesByMetadataQuery(elements))
				.thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRootPath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.isDirectory()).thenReturn(true);
		Mockito.when(collectionFile.exists()).thenReturn(true);

		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceRootPath(testRootPath);
		policyDrivenServiceConfig.setServiceName(testServiceName);

		policyDrivenServiceManager
				.addPolicyDrivenService(policyDrivenServiceConfig);
		
		Mockito.verify(collectionFile, Mockito.atMost(0)).mkdirs();
	}

}
