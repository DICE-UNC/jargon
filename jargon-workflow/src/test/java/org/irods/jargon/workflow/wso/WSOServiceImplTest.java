package org.irods.jargon.workflow.wso;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WSOServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "WSOServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

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
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/*
	 * FIXME: add tests invalid, no runs, with runs, add suite
	 */
	@Test
	public void testFindWSOForCollectionPath() throws Exception {
		String targetCollectionName = "testFindWSOForCollectionPath";
		String subMountCollection = "testFindWSOForCollectionPathMounted";
		String mssoFile = "/msso/eCWkflow.mss";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName);

		String mountedCollectionPath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ targetCollectionName + "/"
								+ subMountCollection);

		IRODSFile parentCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		parentCollection.mkdirs();

		// put the test msso out there
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		Stream2StreamAO stream2Stream = irodsFileSystem
				.getIRODSAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2Stream.streamClasspathResourceToIRODSFile(mssoFile,
				workflowFileTransferredToIrods);

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount

		mountedCollectionAO.createAnMSSOMount(workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		WorkflowStructuredObject actual = wsoService
				.findWSOForCollectionPath(mountedCollectionPath);
		Assert.assertNotNull("null wso returned", actual);

	}

}
