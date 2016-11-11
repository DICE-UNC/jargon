package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests handling of soft links in <code>CollectionAOImpl</code>
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CollectionAOImplForSoftLinkTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAOImplForSoftLinkTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public final void testFindMetadataValuesByMetadataQueryForCollectionSoftLinked()
			throws Exception {
		String sourceCollectionName = "testFindMetadataValuesByMetadataQueryForCollectionSoftLinkedSource";
		String targetCollectionName = "testFindMetadataValuesByMetadataQueryForCollectionSoftLinkedTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		// initialize the AVU data
		String expectedAttribName = "testFindMetadataValuesByMetadataQueryForCollectionSoftLinkedAttrib";
		String expectedAttribValue = "testFindMetadataValuesByMetadataQueryForCollectionSoftLinkedValue";
		String expectedAttribUnits = "testFindMetadataValuesByMetadataQueryForCollectionSoftLinkedUnit";

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		// make sure data is there, ask by source
		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection);
		Assert.assertFalse("no query result returned", result.isEmpty());

		// delete via the source path
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		// query by source and don't find the data
		result = collectionAO.findMetadataValuesByMetadataQueryForCollection(
				queryElements, targetIrodsCollection);
		Assert.assertTrue("avu not deleted", result.isEmpty());

	}

	/**
	 * This method tests out the independence of AVU metadata between a
	 * canonical path, and a soft linked path and shows that iRODS treats these
	 * things as different objects
	 *
	 * @throws Exception
	 */
	@Test
	public final void testFindMetadataValuesByMetadataQueryCompareSourceAndTarget()
			throws Exception {
		String sourceCollectionName = "testFindMetadataValuesByMetadataQueryCompareSourceAndTargetSource";
		String targetCollectionName = "testFindMetadataValuesByMetadataQueryCompareSourceAndTargetTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		// initialize the AVU data
		String expectedAttribName = "testFindMetadataValuesByMetadataQueryCompareSourceAndTargetAttrib";
		String expectedAttribValue = "testFindMetadataValuesByMetadataQueryCompareSourceAndTargetValue";
		String expectedAttribUnits = " testFindMetadataValuesByMetadataQueryCompareSourceAndTargetUnit";

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		// add to target and source
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);
		collectionAO.addAVUMetadata(sourceIrodsCollection, avuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		// make sure data is there, ask by source
		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertTrue("no query result returned", result.size() >= 2);
	}

	/**
	 * This method tests out the independence of AVU metadata between a
	 * canonical path, and a soft linked path and shows that iRODS treats these
	 * things as different objects
	 *
	 * @throws Exception
	 */
	@Test
	public final void testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTarget()
			throws Exception {
		String sourceCollectionName = "testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTargetSource";
		String targetCollectionName = "testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTargetTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		// initialize the AVU data
		String expectedAttribName = "testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTargetAttrib";
		String expectedAttribValue = "testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTargetValue";
		String expectedAttribUnits = "testFindDifferingMetadataValuesByMetadataQueryCompareSourceAndTargetUnit";
		String sourcePrefix = "source-";
		String targetPrefix = "target-";

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		AvuData sourceAvuData = AvuData.instance(sourcePrefix
				+ expectedAttribName, sourcePrefix + expectedAttribValue,
				sourcePrefix + expectedAttribUnits);
		AvuData targetAvuData = AvuData.instance(targetPrefix
				+ expectedAttribName, targetPrefix + expectedAttribValue,
				targetPrefix + expectedAttribUnits);

		collectionAO.deleteAVUMetadata(sourceIrodsCollection, sourceAvuData);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, targetAvuData);

		// add to target and source
		collectionAO.addAVUMetadata(targetIrodsCollection, targetAvuData);
		collectionAO.addAVUMetadata(sourceIrodsCollection, sourceAvuData);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		List<MetaDataAndDomainData> sourceMetadata = collectionAO
				.findMetadataValuesForCollection(sourceIrodsCollection);
		Assert.assertFalse("did not find metadata", sourceMetadata.isEmpty());

		List<MetaDataAndDomainData> targetMetadata = collectionAO
				.findMetadataValuesForCollection(targetIrodsCollection);
		Assert.assertFalse("did not find metadata", targetMetadata.isEmpty());

	}

	/**
	 * see https://github.com/DICE-UNC/jargon/issues/204
	 * 
	 * @throws Exception
	 */
	@Ignore
	public final void testSetReadForASoftLinkedCollection() throws Exception {

		String sourceCollectionName = "testSetReadForASoftLinkedCollectionSource";
		String targetCollectionName = "testSetReadForASoftLinkedCollectionTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// if (props.isAtLeastIrods410()) {
		// return;
		// }

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		// read on target
		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue("user cannot read",
				irodsFileForSecondaryUser.canRead());

		irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(
				secondaryAccount).instanceIRODSFile(sourceIrodsCollection);
		Assert.assertTrue(irodsFileForSecondaryUser.canRead());

		// write on source
		collectionAO
				.setAccessPermissionWrite(
						"",
						sourceIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		// log in as the secondary user and test access

		irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(
				secondaryAccount).instanceIRODSFile(sourceIrodsCollection);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	/**
	 * see https://github.com/DICE-UNC/jargon/issues/204
	 * 
	 * @throws Exception
	 */
	@Ignore
	public final void testSetWriteForASoftLinkedCollection() throws Exception {

		String sourceCollectionName = "testSetWriteForASoftLinkedCollectionSource";
		String targetCollectionName = "testSetWriteForASoftLinkedCollectionTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// if (props.isConsortiumVersion()) {
		// return;
		// }

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		// write on target
		collectionAO
				.setAccessPermissionWrite(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		IRODSFile irodsFileForSecondaryUser = irodsFileSystem
				.getIRODSFileFactory(secondaryAccount).instanceIRODSFile(
						targetIrodsCollection);
		Assert.assertTrue("user cannot read soft linked collection",
				irodsFileForSecondaryUser.canRead());

		irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(
				secondaryAccount).instanceIRODSFile(sourceIrodsCollection);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

		// write on source
		collectionAO
				.setAccessPermissionWrite(
						"",
						sourceIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		// log in as the secondary user and test read access

		irodsFileForSecondaryUser = irodsFileSystem.getIRODSFileFactory(
				secondaryAccount).instanceIRODSFile(targetIrodsCollection);
		Assert.assertTrue(irodsFileForSecondaryUser.canWrite());

	}

	@Test
	public final void testSetInheritForASoftLinkedCollection() throws Exception {

		String sourceCollectionName = "testSetInheritForASoftLinkedCollectionSource";
		String targetCollectionName = "testSetInheritForASoftLinkedCollectionTarget";

		String sourceIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ sourceCollectionName);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		// set up source collection
		IRODSFile sourceFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						sourceIrodsCollection);
		sourceFile.mkdirs();

		// create the soft link
		mountedCollectionAO.createASoftLink(sourceIrodsCollection,
				targetIrodsCollection);

		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionInherit("", targetIrodsCollection, true);
		Assert.assertTrue(
				"should return inherit asking via source collection",
				collectionAO
						.isCollectionSetForPermissionInheritance(sourceIrodsCollection));
		Assert.assertTrue(
				"should return inherit asking via target collection",
				collectionAO
						.isCollectionSetForPermissionInheritance(targetIrodsCollection));

	}

}
