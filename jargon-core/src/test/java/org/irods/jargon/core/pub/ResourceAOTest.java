package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
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
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaAddCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImetaRemoveCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
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
	}

	@Test
	public final void testListResourceNames() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<String> resources = resourceAO.listResourceNames();
		irodsSession.closeSession();
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	/**
	 * Test a listing that has the resource names and resource group name
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testListResourceAndResourceGroupNames() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<String> resources = resourceAO.listResourceAndResourceGroupNames();
		irodsSession.closeSession();
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

		TestCase.assertTrue("did not find the resource group in the results",
				found);
	}

	/**
	 * Listing resources, providing null zone name
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testListResourcesNullZone() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		resourceAO.listResourcesInZone(null);

	}

	@Test
	public final void testListResources() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<Resource> resources = resourceAO
				.listResourcesInZone(testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY));
		irodsSession.closeSession();
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	@Test
	public final void testGetFirstResourceForFile() throws Exception {
		String testFileName = "testGetFirstResourceForFile.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				8);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.getFirstResourceForIRODSFile(irodsFile);
		irodsSession.closeSession();
		Assert.assertNotNull("no resource returned", resource);
	}

	@Test(expected = JargonException.class)
	public final void testGetFirstResourceForCollection() throws Exception {

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsCollection);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		resourceAO.getFirstResourceForIRODSFile(irodsFile);

	}

	@Test
	public final void testFindAll() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		List<Resource> resources = resourceAO.findAll();
		irodsSession.closeSession();
		Assert.assertTrue("no resources returned", resources.size() > 0);
	}

	@Test
	public final void testFindByName() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.findByName(testResource);
		irodsSession.closeSession();
		Assert.assertEquals(
				"resource not returned that matches given resource name",
				testResource, resource.getName());
	}

	@Test
	public final void testFindById() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		Resource resource = resourceAO.findByName(testResource);
		Resource resourceById = resourceAO.findById(resource.getId());
		irodsSession.closeSession();
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
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		sb.append(" = '");
		sb.append(testResource);
		sb.append("'");

		List<Resource> resources = resourceAO.findWhere(sb.toString());
		irodsSession.closeSession();
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

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		// imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.RESOURCE_META);
		imetaRemoveCommand.setObjectPath(testResource);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.RESOURCE_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(testResource);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> result = resourceAO
				.findMetadataValuesByMetadataQuery(queryElements);
		irodsSession.closeSession();
		Assert.assertFalse("no query result returned", result.isEmpty());

	}

	@Test
	public final void testListResourceMetadata() throws Exception {
		String testResource = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		// initialize the AVU data
		String expectedAttribName = "testattrib1";
		String expectedAttribValue = "testvalue1";
		String expectedAttribUnits = "test1units";
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		ImetaRemoveCommand imetaRemoveCommand = new ImetaRemoveCommand();
		imetaRemoveCommand.setAttribName(expectedAttribName);
		imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setAttribUnits(expectedAttribUnits);
		// imetaRemoveCommand.setAttribValue(expectedAttribValue);
		imetaRemoveCommand.setMetaObjectType(MetaObjectType.RESOURCE_META);
		imetaRemoveCommand.setObjectPath(testResource);
		invoker.invokeCommandAndGetResultAsString(imetaRemoveCommand);

		ImetaAddCommand imetaAddCommand = new ImetaAddCommand();
		imetaAddCommand.setMetaObjectType(MetaObjectType.RESOURCE_META);
		imetaAddCommand.setAttribName(expectedAttribName);
		imetaAddCommand.setAttribValue(expectedAttribValue);
		imetaAddCommand.setAttribUnits(expectedAttribUnits);
		imetaAddCommand.setObjectPath(testResource);
		invoker.invokeCommandAndGetResultAsString(imetaAddCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		ResourceAO resourceAO = accessObjectFactory.getResourceAO(irodsAccount);

		List<AvuData> avuData = resourceAO.listResourceMetadata(testResource);

		irodsSession.closeSession();
		Assert.assertFalse("no query result returned", avuData.isEmpty());
		AvuData avuDataItem = null;

		for (AvuData foundItem : avuData) {
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

}
