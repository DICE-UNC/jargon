package org.irods.jargon.core.pub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MountedFilesystemCollectionAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemCollectionAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testAddAvuMetadata() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String testDirName = "testAddAvuMetadataMountedFileSystem";
		String expectedAttribName = "testAddAvuMetadataMountedFileSystem-attrib1";
		String expectedAttribValue = "testAddAvuMetadataMountedFileSystem-value1";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		mountedCollectionTargetFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionTargetFile.mkdirs();

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		AvuData dataToAdd = AvuData.instance(expectedAttribName,
				expectedAttribValue, "");
		collectionAO.deleteAVUMetadata(targetIrodsCollection, dataToAdd);
		collectionAO.addAVUMetadata(targetIrodsCollection, dataToAdd);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, expectedAttribName));

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL,
				expectedAttribValue));

		List<MetaDataAndDomainData> result = collectionAO
				.findMetadataValuesByMetadataQuery(queryElements);
		Assert.assertFalse("no query result returned", result.isEmpty());

	}

	@Test
	public final void testSetInherit() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		String testDirName = "testSetInherit";

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR)
				+ "/"
				+ testDirName;

		File localCollectionFile = new File(localCollectionAbsolutePath);
		localCollectionFile.delete();
		localCollectionFile.mkdirs();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		mountedCollectionTargetFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionTargetFile.mkdirs();

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO.setAccessPermissionInherit("", targetIrodsCollection,
				false);

		boolean isInherit = collectionAO
				.isCollectionSetForPermissionInheritance(targetIrodsCollection);

		Assert.assertTrue("collection should have inherit set", isInherit);

	}

	@Test
	public final void testIsAccessForCollectionForUserWhenRead()
			throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		String testDirName = "testIsAccessForCollectionForUserWhenRead";

		String localCollectionAbsolutePath = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR)
				+ "/"
				+ testDirName;

		File localCollectionFile = new File(localCollectionAbsolutePath);
		localCollectionFile.delete();
		localCollectionFile.mkdirs();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		IRODSFile mountedCollectionTargetFile = irodsFileSystem
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		mountedCollectionTargetFile.delete();

		mountedCollectionAO.unmountACollection(targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		mountedCollectionTargetFile.mkdirs();

		mountedCollectionAO.createMountedFileSystemCollection(
				localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());

		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();
		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		boolean hasPermission = collectionAO
				.isUserHasAccess(
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));
		Assert.assertTrue("did not get expected permission", hasPermission);

	}

}
