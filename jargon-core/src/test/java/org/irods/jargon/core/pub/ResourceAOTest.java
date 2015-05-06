package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidResourceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	// OK41
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
	// OK41
	public final void testListResourceAndResourceGroupNames() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

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

	@Test
	// OK41
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
	// OK41
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
	// OK41
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
	// OK41
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
	// bug #104
	public final void testFindMetadataValuesByMetadataQuery() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testrattrib1";
		String expectedAttribValue = "testrvalue1";
		String expectedAttribUnits = "testr1units";

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
	// OK41
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
	// OK41
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
	// OK41
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
	// OK41
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
	// OK401
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

	@Ignore
	// TODO: see https://github.com/DICE-UNC/jargon/issues/97
	public final void testDeleteResourceMetadata() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		// initialize the AVU data
		String expectedAttribName = "testDeleteResourceMetadataattrib1";
		String expectedAttribValue = "testDeleteResourceMetadatavalue1";
		String expectedAttribUnits = "testDeleteResourceMetadataunits";

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

	@Test
	public final void testRemoveBogusResource() throws Exception {

		String rescName = "testRemoveBogusResource";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);

		resourceAO.deleteResource(rescName);

		// should silently fail

	}

	@Test
	public final void testRemoveDeferredResource() throws Exception {

		String rescName = "testRemoveDeferredResource";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resourceAO.deleteResource(rescName);
		boolean deleted = false;

		try {
			resourceAO.findByName(rescName);
		} catch (Exception e) {
			deleted = true;
		}

		Assert.assertTrue("didn't delete", deleted);

	}

	@Test(expected = DuplicateDataException.class)
	public final void testAddDuplicateResource() throws Exception {

		String rescName = "testAddDuplicateResource";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			throw new DuplicateDataException(
					"skip but maintain expectations of test");
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);
		resourceAO.addResource(resource);

	}

	@Test
	public final void testAddParentDeferredResource() throws Exception {

		String rescName = "testAddParentDeferredResource";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		Resource actual = resourceAO.findByName(rescName);
		Assert.assertNotNull("didn't find resource", actual);

	}

	@Test
	public final void testAddChildToParent() throws Exception {

		String rescName = "testAddChildToParent";
		String childName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.removeChildFromResource(rescName, childName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resourceAO.addChildToResource(rescName, childName, "");

		Resource actual = resourceAO.findByName(rescName);
		Assert.assertNotNull("didn't find resource", actual);

	}

	@Test
	public final void testAddTwoChildToParentAndThenListAll() throws Exception {

		String rescName = "testAddTwoChildToParentAndThenListAll";
		String child1Suffix = "-child1";
		String child2Suffix = "-child2";

		String child1Name = rescName + child1Suffix;
		String child2Name = rescName + child2Suffix;
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.removeChildFromResource(rescName, child1Name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.removeChildFromResource(rescName, child2Name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.deleteResource(child1Name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.deleteResource(child2Name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resource = new Resource();
		resource.setContextString("");
		resource.setName(child1Name);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resource = new Resource();
		resource.setContextString("");
		resource.setName(child2Name);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resourceAO.addChildToResource(rescName, child1Name, "");
		resourceAO.addChildToResource(rescName, child2Name, "");

		List<Resource> actual = resourceAO.findAll();
		Assert.assertNotNull("didn't find resources", actual.isEmpty());
		boolean foundParent = false;

		for (Resource actualResource : actual) {
			if (actualResource.getName().equals(rescName)) {
				foundParent = true;
				Assert.assertEquals("parent should hove two children", 2,
						actualResource.getImmediateChildren().size());

			}

		}

		Assert.assertTrue("found the parent", foundParent);

	}

	@Test
	// Bug 104
	public final void testAddChildToParentDuplicate() throws Exception {

		String rescName = "testAddChildToParentDuplicate";
		String childName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.removeChildFromResource(rescName, childName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		resourceAO.addChildToResource(rescName, childName, "");
		resourceAO.addChildToResource(rescName, childName, "");
		// expect to silently ignore, no error

	}

	@Test
	// Bug 105
	// FIXME: waits for resolution of https://github.com/irods/irods/issues/2325
	public final void testAddMissingChildToParent() throws Exception {

		String rescName = "testAddMissingChildToParentxxxx";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAdminAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		if (!accessObjectFactory.getIRODSServerProperties(irodsAccount)
				.isEirods()) {
			return;
		}

		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		try {
			resourceAO.deleteResource(rescName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Resource resource = new Resource();
		resource.setContextString("");
		resource.setName(rescName);
		resource.setType("deferred");
		resourceAO.addResource(resource);

		String child = "reallybogusresourceherebroxxx";
		resourceAO.addChildToResource(rescName, child, "");

	}

}
