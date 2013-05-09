package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.InvalidResourceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceAOTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ResourceAOTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

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

	@Test
	public final void testListResourceNames() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<String> resources = resourceAO.listResourceNames();
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	/**
	 * Test a listing that has the resource names and resource group name
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testListResourceAndResourceGroupNames() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<String> resources = resourceAO.listResourceAndResourceGroupNames();
		Assert.assertTrue("no resources returned", resources.size() > 0);
		// look for the resource group name
		String expected = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_GROUP_KEY);

		boolean found = false;
		for (String actual : resources) {
			if (actual.equals(expected)) {
				found = true;
				break;
			}
		}

		Assert.assertTrue("did not find the resource group in the results",
				found);
	}

	/**
	 * Listing resources, providing null zone name
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testListResourcesNullZone() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		resourceAO.listResourcesInZone(null);

	}

	@Test
	public final void testListResources() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<Resource> resources = resourceAO
				.listResourcesInZone(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	@Test
	public final void testGetFirstResourceForFile() throws Exception {
		String testFileName = "testGetFirstResourceForFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		dto.putOperation(fileNameAndPath.toString(), targetIrodsCollection, "",
				null, null);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.getFirstResourceForIRODSFile(irodsFile);
		Assert.assertNotNull("no resource returned", resource);
	}

	@Test(expected = JargonException.class)
	public final void testGetFirstResourceForCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		resourceAO.getFirstResourceForIRODSFile(irodsFile);

	}

	@Test
	public final void testFindAll() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<Resource> resources = resourceAO.findAll();
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	@Test
	public final void testFindByName() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.findByName(testResource);
		Assert.assertEquals(
				"resource not returned that matches given resource name",
				testResource, resource.getName());
	}

	@Test
	public final void testFindById() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.findByName(testResource);
		Resource resourceById = resourceAO.findById(resource.getId());
		Assert.assertEquals("did not find correct resource by id",
				resource.getName(), resourceById.getName());
		Assert.assertEquals(
				"resource not returned that matches given resource name",
				testResource, resource.getName());
	}

	@Test
	public final void testFindWhere() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		sb.append(" = '");
		sb.append(testResource);
		sb.append("'");

		List<Resource> resources = resourceAO.findWhere(sb.toString());
		Assert.assertTrue(
				"should have gotten the one resource that matches my query",
				resources.size() == 1);
		Assert.assertEquals(
				"resource not returned that matches given resource name",
				testResource, resources.get(0).getName());
	}

	@Test
	public final void testFindMetadataValuesByMetadataQuery() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

		resourceAO.addAVUMetadata(testResource, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = resourceAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());

	}

	@Test(expected = InvalidResourceException.class)
	public final void testAddResourceMetadataBadResource() throws Exception {
		String testResource = "Imabadresource";

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.addAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddResourceNullResource() throws Exception {
		String testResource = null;

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.addAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddResourceBlankResource() throws Exception {
		String testResource = "";

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.addAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddResourceNullAvu() throws Exception {
		String testResource = "xx";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = null;

		resourceAO.addAVUMetadata(testResource, avuData);

	}

	@Test
	public final void testListResourceMetadata() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

		resourceAO.addAVUMetadata(testResource, avuData);

		List<AvuData> actual = resourceAO.listResourceMetadata(testResource);

		Assert.assertFalse("no query result returned", actual.isEmpty());
		AvuData avuDataItem = null;

		for (AvuData foundItem : actual) {
			if (foundItem.getAttribute().equals(expectedAttribName)) {
				avuDataItem = foundItem;
				break;
			}
		}

		Assert.assertNotNull("did not find the testing attrib in the resource",
				avuDataItem);
		Assert.assertEquals("did not get expected attrib", expectedAttribName,
				avuDataItem.getAttribute());
		Assert.assertEquals("did not get expected value", expectedAttribValue,
				avuDataItem.getValue());

	}

	@Test
	public final void testDeleteResourceMetadata() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

		resourceAO.addAVUMetadata(testResource, avuData);

		List<AvuData> actual = resourceAO.listResourceMetadata(testResource);

		Assert.assertFalse("no query result returned", actual.isEmpty());
		resourceAO.deleteAVUMetadata(testResource, avuData);
		actual = resourceAO.listResourceMetadata(testResource);
		Assert.assertTrue("resource avu there after the delete",
				actual.isEmpty());
		resourceAO.deleteAVUMetadata(testResource, avuData);

	}

	@Test(expected = InvalidResourceException.class)
	public final void testDeleteResourceMetadataBadResource() throws Exception {
		String testResource = "veryBadResource";
		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testDeleteResourceMetadataNullResource() throws Exception {
		String testResource = null;
		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testDeleteResourceMetadataBlankResource()
			throws Exception {
		String testResource = "";
		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);

		resourceAO.deleteAVUMetadata(testResource, avuData);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testDeleteResourceMetadataNullAvuData() throws Exception {
		String testResource = "xxx";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		AvuData avuData = null;

		resourceAO.deleteAVUMetadata(testResource, avuData);

	}

}
