package org.irods.jargon.core.pub.io;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class MountedFilesystemIRODSFileOutputStreamTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "MountedFilesystemIRODSFileOutputStreamTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
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
	public final void testWriteByteArray() throws Exception {

		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		String testFileName = "testWriteByteArray.csv";

		String targetCollectionSubdir = "testWriteByteArray";

		String localCollectionAbsolutePath = testingProperties.getProperty(TestingPropertiesHelper.IRODS_REG_BASEDIR);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + targetCollectionSubdir);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getMountedCollectionAO(irodsAccount);

		mountedCollectionAO.unmountACollection(targetIrodsCollection, irodsAccount.getDefaultStorageResource());

		mountedCollectionAO.createMountedFileSystemCollection(localCollectionAbsolutePath, targetIrodsCollection,
				irodsAccount.getDefaultStorageResource());
		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);
		IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);

		// get a simple byte array
		String myBytes = "ajjjjjjjjjjjjjjjjjjjjjjjjfeiiiiiiiiiiiiiii54454545";
		byte[] myBytesArray = myBytes.getBytes();

		irodsFileOutputStream.write(myBytesArray);
		irodsFileOutputStream.write(myBytesArray);

		irodsFile.close();

		irodsFile = irodsFileFactory.instanceIRODSFile(targetIrodsCollection + '/' + testFileName);

		long length = irodsFile.length();

		Assert.assertEquals("file length does not match bytes written", myBytesArray.length * 2, length);

		irodsFileSystem.closeAndEatExceptions();
	}

}
