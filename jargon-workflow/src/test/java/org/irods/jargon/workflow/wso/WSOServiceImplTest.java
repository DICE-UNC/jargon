package org.irods.jargon.workflow.wso;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.MountedCollectionAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.workflow.mso.exception.WSONotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WSOServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "WSOServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static boolean testWorkflow = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		testWorkflow = testingPropertiesHelper
				.isTestWorkflow(testingProperties);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testCreateAndRemoveMountedWSSO() throws Exception {

		if (!testWorkflow) {
			return;
		}

		String targetCollectionName = "testFindWSOForCollectionPath";
		String subMountCollection = "testFindWSOForCollectionPathMounted";
		String mssoFile = "/msso/eCWkflow.mss";
		String mssoFileName = "testCreateAndRemoveMountedWSSO.mss";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		wsoService.createNewWorkflow(mssoAsFile.getAbsolutePath(),
				targetIrodsCollection + "/" + mssoFileName,
				mountedCollectionPath);

		// now look up workflow
		WorkflowStructuredObject wso = wsoService
				.findWSOForCollectionPath(mountedCollectionPath);

		TestCase.assertEquals("wsso does not have correct mss file reference",
				targetIrodsCollection + "/" + mssoFileName,
				wso.getMssFileAbsolutePath());

		// now delete
		wsoService
				.removeWorkflowFileAndMountedCollection(mountedCollectionPath);

		boolean found = true;
		try {
			wsoService.findWSOForCollectionPath(mountedCollectionPath);
		} catch (WSONotFoundException wsnf) {
			found = false;
		}

		Assert.assertFalse("should not have found wsso", found);

	}

	/*
	 * FIXME: add tests invalid, no runs, with runs, add suite
	 */
	@Test
	public void testFindWSOForCollectionPath() throws Exception {

		if (!testWorkflow) {
			return;
		}

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		WorkflowStructuredObject actual = wsoService
				.findWSOForCollectionPath(mountedCollectionPath);
		Assert.assertNotNull("null wso returned", actual);

	}

	@Test
	public void testSubmitLocalParamterFileToWorkflow() throws Exception {

		if (!testWorkflow) {
			return;
		}

		String targetCollectionName = "testSubmitLocalParamterFileToWorkflow";
		String subMountCollection = "testSubmitLocalParamterFileToWorkflowMounted";
		String mssoFile = "/msso/mssotest.mss";
		String paramFile = "/msso/mssotest.mpf";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		File wpfFile = LocalFileUtils.getClasspathResourceAsFile(paramFile);

		wsoService.ingestLocalParameterFileIntoWorkflow(
				wpfFile.getAbsolutePath(), mountedCollectionPath);

	}

	@Test
	public void testSubmitLocalParameterFileToWorkflowViaStream()
			throws Exception {

		if (!testWorkflow) {
			return;
		}

		String targetCollectionName = "testSubmitLocalParameterFileToWorkflowViaStream";
		String subMountCollection = "testSubmitLocalParameterFileToWorkflowViaStreamMounted";
		String mssoFile = "/msso/mssotest.mss";
		String paramFile = "/msso/mssotest.mpf";
		String paramFileName = "testSubmitLocalParameterFileToWorkflowViaStreamParam1.mpf";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		File wpfFile = LocalFileUtils.getClasspathResourceAsFile(paramFile);
		InputStream localInputStream = new FileInputStream(wpfFile);

		wsoService.ingestLocalParameterFileIntoWorkflow(paramFileName,
				localInputStream, mountedCollectionPath);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubmitLocalParameterFileToWorkflowViaStreamNotMpf()
			throws Exception {

		if (!testWorkflow) {
			throw new IllegalArgumentException("throw to keep expected");
		}

		String targetCollectionName = "testSubmitLocalParameterFileToWorkflowViaStreamNotMpf";
		String subMountCollection = "testSubmitLocalParameterFileToWorkflowViaStreamNotMpfMounted";
		String mssoFile = "/msso/mssotest.mss";
		String paramFile = "/msso/mssotest.mpf";
		String paramFileName = "testSubmitLocalParameterFileToWorkflowViaStreamNotMpf";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		File wpfFile = LocalFileUtils.getClasspathResourceAsFile(paramFile);
		InputStream localInputStream = new FileInputStream(wpfFile);

		wsoService.ingestLocalParameterFileIntoWorkflow(paramFileName,
				localInputStream, mountedCollectionPath);

	}

	@Test
	public void testListParamAndRunInWorkflowMountedDir() throws Exception {

		if (!testWorkflow) {
			return;
		}

		String targetCollectionName = "testListParamAndRunInWorkflowMountedDir";
		String subMountCollection = "testListParamAndRunInWorkflowMountedDirMounted";
		String mssoFile = "/msso/mssotest.mss";
		String paramFile = "/msso/mssotest.mpf";

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

		// do an initial unmount
		MountedCollectionAO mountedCollectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getMountedCollectionAO(
						irodsAccount);

		mountedCollectionAO.unmountACollection(mountedCollectionPath,
				irodsAccount.getDefaultStorageResource());

		// create the msso workflow mount
		File mssoAsFile = LocalFileUtils.getClasspathResourceAsFile(mssoFile);
		String workflowFileTransferredToIrods = targetIrodsCollection + "/"
				+ "eCWkflow.mss";
		mountedCollectionAO.createAnMSSOMountForWorkflow(
				mssoAsFile.getAbsolutePath(), workflowFileTransferredToIrods,
				mountedCollectionPath);

		WSOService wsoService = new WSOServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		File wpfFile = LocalFileUtils.getClasspathResourceAsFile(paramFile);

		wsoService.ingestLocalParameterFileIntoWorkflow(
				wpfFile.getAbsolutePath(), mountedCollectionPath);

		Thread.sleep(10000);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		List<CollectionAndDataObjectListingEntry> wsoContents = collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPath(mountedCollectionPath);

		Assert.assertFalse("no contents found", wsoContents.isEmpty());

	}
}
