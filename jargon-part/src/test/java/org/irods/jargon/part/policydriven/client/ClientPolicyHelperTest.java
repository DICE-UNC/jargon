package org.irods.jargon.part.policydriven.client;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.PolicyManagerFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class ClientPolicyHelperTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetRelevantPolicy() throws Exception {

		String collectionName = "/a/test/collection";
		String policyName = "policyName";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		
		IRODSFileFactory irodsFileFactory = mock(IRODSFileFactory.class);
		IRODSFile mockFile = mock(IRODSFile.class);
		Mockito.when(mockFile.isDirectory()).thenReturn(true);
		Mockito.when(mockFile.getAbsolutePath()).thenReturn(collectionName);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)).thenReturn(irodsFileFactory);
		Mockito.when(irodsFileFactory.instanceIRODSFile(collectionName)).thenReturn(mockFile);


		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								collectionName,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_POLICY_MARKER_ATTRIBUTE,
								policyName, "CommentHere"));

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(Matchers
						.anyList(), Matchers.eq(collectionName), Matchers.eq(0))).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		Policy testPolicy = new Policy();
		PolicyManager policyManager = mock(PolicyManager.class);
		
		Mockito.when(
				policyManager.getPolicyFromPolicyRepository(Matchers.eq(policyName),
						Matchers.any(XMLToObjectUnmarshaller.class))).thenReturn(
				testPolicy);
		PolicyManagerFactory policyManagerFactory = mock(PolicyManagerFactory.class);
		Mockito.when(policyManagerFactory.getPolicyManager()).thenReturn(
				policyManager);

		ClientPolicyHelper clientPolicyHelper = new ClientPolicyHelperImpl(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);
		Policy returnedPolicy = clientPolicyHelper.getRelevantPolicy(collectionName);
		Assert.assertNotNull("no policy returned", returnedPolicy);
		
	}
	
	@Test
	public final void testGetRelevantPolicyGivingDataObject() throws Exception {

		String collectionName = "/a/test/collection";
		String policyName = "policyName";
		String parentName = "parent";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		IRODSFileFactory irodsFileFactory = mock(IRODSFileFactory.class);
		IRODSFile mockFile = mock(IRODSFile.class);
		Mockito.when(mockFile.isDirectory()).thenReturn(false);
		Mockito.when(mockFile.getParent()).thenReturn(parentName);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)).thenReturn(irodsFileFactory);
		Mockito.when(irodsFileFactory.instanceIRODSFile(collectionName)).thenReturn(mockFile);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		metaDataAndDomainData
				.add(MetaDataAndDomainData
						.instance(
								MetadataDomain.COLLECTION,
								"1",
								parentName,
								PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_POLICY_MARKER_ATTRIBUTE,
								policyName, "CommentHere"));

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(Matchers
						.anyList(), Matchers.eq(parentName), Matchers.eq(0))).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		Policy testPolicy = new Policy();
		PolicyManager policyManager = mock(PolicyManager.class);
		
		Mockito.when(
				policyManager.getPolicyFromPolicyRepository(Matchers.eq(policyName),
						Matchers.any(XMLToObjectUnmarshaller.class))).thenReturn(
				testPolicy);
		PolicyManagerFactory policyManagerFactory = mock(PolicyManagerFactory.class);
		Mockito.when(policyManagerFactory.getPolicyManager()).thenReturn(
				policyManager);

		ClientPolicyHelper clientPolicyHelper = new ClientPolicyHelperImpl(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);
		Policy returnedPolicy = clientPolicyHelper.getRelevantPolicy(collectionName);
		Assert.assertNotNull("no policy returned", returnedPolicy);
		
	}
	
	@Test
	public final void testGetRelevantPolicyNoPolicyToFind() throws Exception {

		String collectionName = "/a/test/collection";
		String policyName = "policyName";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = mock(IRODSAccessObjectFactory.class);
		CollectionAO collectionAO = mock(CollectionAO.class);
		
		IRODSFileFactory irodsFileFactory = mock(IRODSFileFactory.class);
		IRODSFile mockFile = mock(IRODSFile.class);
		Mockito.when(mockFile.isDirectory()).thenReturn(true);
		Mockito.when(mockFile.getAbsolutePath()).thenReturn(collectionName);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)).thenReturn(irodsFileFactory);
		Mockito.when(irodsFileFactory.instanceIRODSFile(collectionName)).thenReturn(mockFile);

		List<MetaDataAndDomainData> metaDataAndDomainData = new ArrayList<MetaDataAndDomainData>();
		
		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(Matchers
						.anyList(), Matchers.eq(collectionName), Matchers.eq(0))).thenReturn(metaDataAndDomainData);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		Policy testPolicy = new Policy();
		PolicyManager policyManager = mock(PolicyManager.class);
		
		PolicyManagerFactory policyManagerFactory = mock(PolicyManagerFactory.class);
		Mockito.when(policyManagerFactory.getPolicyManager()).thenReturn(
				policyManager);

		ClientPolicyHelper clientPolicyHelper = new ClientPolicyHelperImpl(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);
		Policy returnedPolicy = clientPolicyHelper.getRelevantPolicy(collectionName);
		Assert.assertNull("no policy, null should return", returnedPolicy);
		
	}

}
