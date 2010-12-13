package org.irods.jargon.part.policydriven;

import static org.mockito.Mockito.mock;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolicyManagerImplRequiredMetadataTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "PolicyManagerImplRequiredMetadataTest";
	public static final String POLICY_DRIVEN_SERVICE_NAME = "PolicyManagerImplRequiredMetadataTest";
	public static final String POLICY_DRIVEN_APP_SUBDIR = "policyDrivenAppSubdir";
	public static final String POLICY_REPOSITORY_SUBDIR = "policyRepositorySubdir";
	public static final String POLICY_REPOSITORY_NAME = "PolicyManagerImplRequiredMetadataTestPolicyRepos";
	public static final String POLICY_NAME = "testPolicy1";
	public static final String ARCHIVE_ROOT_NAME = "archiveRoot";
	public static final String BOUND_COLLECTION_SUBDIR = "boundCollection1";

	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;

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
		assertionHelper = new AssertionHelper();

		// create a framework for testing with a policy driven app, a policy
		// rep, a policy with required metadata values, and a bound series

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		PolicyDrivenServiceManager policyDrivenServiceManager = new PolicyDrivenServiceManagerImpl(
				irodsAccessObjectFactory, irodsAccount);
		
		String targetIrodsFile = testingPropertiesHelper
		.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH);
		
		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceRootPath(targetIrodsFile + "/" + POLICY_DRIVEN_APP_SUBDIR);
		policyDrivenServiceConfig.setServiceName(POLICY_DRIVEN_SERVICE_NAME);
		policyDrivenServiceManager.addPolicyDrivenService(policyDrivenServiceConfig);
		
		// add policy repo
		
		PolicyManager policyManager = new PolicyManagerImpl(irodsAccessObjectFactory, irodsAccount);
		PolicyDrivenServiceListingEntry reposEntry = PolicyDrivenServiceListingEntry.instance(POLICY_REPOSITORY_NAME, 
				targetIrodsFile + "/" + POLICY_REPOSITORY_SUBDIR, "added from test");
		policyManager.addPolicyRepository(reposEntry);
		
		// add a policy with required metadata values
		Policy policy = new Policy();
		policy.setPolicyName(POLICY_NAME);

		PolicyRequiredMetadataValue policyRequiredMetadataValue;

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("creator");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Name of creator of this document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("latitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Latitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("longitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Longitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);
		
		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("allowPublicSharing");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Can this image be publicly shared");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_BOOLEAN);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);


		ObjectToXMLMarshaller objectToXMLMarshaller = new ObjectToXMLMarshaller();
		policyManager.addPolicyToRepository(POLICY_REPOSITORY_NAME, policy, objectToXMLMarshaller);
		
		// bind the policy to a collection
		SeriesManager seriesManager = new SeriesManagerImpl(irodsAccessObjectFactory, irodsAccount);
		Series series = new Series();
		series.setBoundPolicyName(POLICY_NAME);
		series.setCollectionAbsolutePath(targetIrodsFile + "/" + ARCHIVE_ROOT_NAME + "/" + BOUND_COLLECTION_SUBDIR);
		series.setContainingServiceName(POLICY_DRIVEN_SERVICE_NAME);
		series.setName(BOUND_COLLECTION_SUBDIR);
	
		XMLToObjectUnmarshaller unmarshaller = new XMLToObjectUnmarshaller();
		seriesManager.addSeriesToApplication(series, unmarshaller);
		
	}

	@Test
	public void testSerializePolicyWithRequiredMetadataValues()
			throws Exception {
		String policyName = "testPolicy";
		String testFileName = policyName + ".xml";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		Policy policy = new Policy();
		policy.setPolicyName(policyName);

		PolicyRequiredMetadataValue policyRequiredMetadataValue;

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("creator");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Name of creator of this document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("latitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Latitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("longitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Longitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		// now serialize this policy
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileWriter irodsFileWriter = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFileWriter(targetIrodsFile);
		ObjectToXMLMarshaller marshaller = new ObjectToXMLMarshaller();
		marshaller.marshallPolicyToXML(irodsFileWriter, policy);
		TestCase.assertTrue(true);
		// no errors means a passing case here..
	}

	@Test
	public void testBindRequiredMetadataPolicyToSeries() throws Exception {
		String policyName = "testPolicy";
		String testFileName = policyName + ".xml";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testFileName);

		Policy policy = new Policy();
		policy.setPolicyName(policyName);

		PolicyRequiredMetadataValue policyRequiredMetadataValue;

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("creator");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Name of creator of this document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("latitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Latitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("longitude");
		policyRequiredMetadataValue.setMetaDataPromptAsText("Longitude");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		policy.getPolicyRequiredMetadataValues().add(
				policyRequiredMetadataValue);

		// now serialize this policy
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileWriter irodsFileWriter = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFileWriter(targetIrodsFile);
		ObjectToXMLMarshaller marshaller = new ObjectToXMLMarshaller();
		marshaller.marshallPolicyToXML(irodsFileWriter, policy);
		TestCase.assertTrue(true);
		// no errors means a passing case here..
	}

}
