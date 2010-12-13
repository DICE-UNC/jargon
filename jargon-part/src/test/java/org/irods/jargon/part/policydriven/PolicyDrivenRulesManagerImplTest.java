package org.irods.jargon.part.policydriven;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.TranslatedIRODSQuery;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class PolicyDrivenRulesManagerImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	@Test
	public void testFindRuleRepositories() throws Exception {

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
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
								"RuleRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyDrivenRulesManager
				.findRuleRepositories();
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals("RuleRepository", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry1.getComment());
	}

	@Test
	public void testListRulesInRepository() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		
		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();		
		
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		

		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								"/collection/path",
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
								"RuleRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		
		// set up query response
		IRODSGenQueryExecutor queryExecutor = mock(IRODSGenQueryExecutor.class);
		
		IRODSQueryResultSet myResultSet = mock(IRODSQueryResultSet.class);
		TranslatedIRODSQuery translatedIRODSQuery = mock(TranslatedIRODSQuery.class);
		IRODSQueryResultRow irodsQueryResultRow = mock(IRODSQueryResultRow.class);
		Mockito.when(irodsQueryResultRow.getColumn(Mockito.anyInt())).thenReturn("hello");
		List<IRODSQueryResultRow> resultRows = new ArrayList<IRODSQueryResultRow>();
		resultRows.add(irodsQueryResultRow);
		
		Mockito.when(myResultSet.getResults()).thenReturn(resultRows);
		Mockito.when(queryExecutor.executeIRODSQuery(Mockito.any(IRODSQuery.class), Mockito.anyInt())).thenReturn(myResultSet);
		Mockito.when(irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount)).thenReturn(queryExecutor);
		
		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<PolicyDrivenServiceListingEntry> listingEntries = policyDrivenRulesManager.listRulesInRepository("repName");
		
		Assert.assertTrue(listingEntries.size() == 1);
		PolicyDrivenServiceListingEntry entry1 = listingEntries.get(0);
		Assert.assertEquals("hello", entry1
				.getPolicyDrivenServiceName());
		Assert.assertEquals("hello", entry1
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("hello", entry1.getComment());
	}
	
	@Test(expected=DataNotFoundException.class)
	public void testListRulesInRepositoryThatDoesNotExist() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
	
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);
		
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		
		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		policyDrivenRulesManager.listRulesInRepository("repName");
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddRepository() throws Exception {

		String testRuleRepName = "rulerep1";
		String testRulePath = "/a/path/to/somerules";
		String comment = "comment here";

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
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
								"RuleRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRulePath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		policyDrivenRulesManager
				.addRuleRepository(PolicyDrivenServiceListingEntry.instance(
						testRuleRepName, testRulePath, comment));
		Mockito.verify(collectionFile, Mockito.atLeast(1)).mkdirs();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = DuplicateDataException.class)
	public void testAddRepositoryDuplicate() throws Exception {

		String testRuleRepName = "rulerep1";
		String testRulePath = "/a/path/to/somerules";
		String comment = "comment here";

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
								testRulePath,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
								testRuleRepName, "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);

		IRODSFile collectionFile = mock(IRODSFile.class);
		Mockito.when(collectionAO.instanceIRODSFileForCollectionPath(testRulePath))
				.thenReturn(collectionFile);
		Mockito.when(collectionFile.exists()).thenReturn(false);
		Mockito.when(collectionFile.mkdirs()).thenReturn(true);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);

		policyDrivenRulesManager
				.addRuleRepository(PolicyDrivenServiceListingEntry.instance(
						testRuleRepName, testRulePath, comment));
	}

	@Test
	public void testFindRuleRepository() throws Exception {
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
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_RULE_REPOSITORY_MARKER_ATTRIBUTE,
								"RuleRepository", "CommentHere"));
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		PolicyDrivenServiceListingEntry entry = policyDrivenRulesManager
				.findRuleRepository("RuleRepository");
		Assert.assertEquals("RuleRepository", entry
				.getPolicyDrivenServiceName());
		Assert.assertEquals("/collection/path", entry
				.getPolicyDrivenServiceAbsolutePath());
		Assert.assertEquals("CommentHere", entry.getComment());

	}

	@Test(expected = DataNotFoundException.class)
	public void testFindRuleRepositoryNoneFound() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		PolicyDrivenRulesManager policyDrivenRulesManager = new PolicyDrivenRulesManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		policyDrivenRulesManager.findRuleRepository("RuleRepository");
	}

}
