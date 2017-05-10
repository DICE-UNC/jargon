package org.irods.jargon.datautils.metadatamanifest;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.BulkAVUOperationResponse;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.metadatamanifest.MetadataManifest.Action;
import org.irods.jargon.datautils.metadatamanifest.MetadataManifest.FailureMode;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetadataManifestProcessorImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MetadataManifestProcessorImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testManifestToJsonRoundTrip() throws Exception {
		final MetadataManifest manifest = new MetadataManifest();
		manifest.setFailureMode(FailureMode.FAIL_FAST);
		manifest.setParentIrodsTargetPath("parent/path");
		MetadataManifestOperation op;

		op = new MetadataManifestOperation();
		op.setAction(Action.ADD);
		op.setAttribute("attr1");
		op.setValue("val1");
		op.setUnit("");
		manifest.getOperation().add(op);

		op = new MetadataManifestOperation();
		op.setAction(Action.ADD);
		op.setAttribute("attr2");
		op.setValue("val2");
		op.setUnit("unit2");
		manifest.getOperation().add(op);

		final IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		final MetadataManifestProcessor impl = new MetadataManifestProcessorImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		final String json = impl.metadataManifestToJson(manifest);
		Assert.assertNotNull("null json", json);
		Assert.assertFalse("empty json", json.isEmpty());

		final MetadataManifest deserialized = impl.stringJsonToMetadataManifest(json);
		Assert.assertNotNull("null deserialized", deserialized);

	}

	@Test
	public void testProcessValidManifestFailFastRelativePath() throws Exception {
		final String testParentName = "testProcessValidManifestFailFastRelativePath";
		final String testFileName = "testProcessValidManifestFailFastRelativePath.txt";
		final String testFileName2 = "testProcessValidManifestFailFastRelativePath2.txt";

		final String expectedAttribName = "testProcessValidManifestFailFastRelativePath";
		final String expectedValueName = "testval1";

		final IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		final String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testParentName);

		final IRODSFile parentFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		parentFile.delete();
		parentFile.mkdirs();

		final String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		final String vaultAbsPath = absPath + testParentName;
		final String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(vaultAbsPath + "/", testFileName,
				2);
		final String fileNameOrig2 = FileGenerator.generateFileOfFixedLengthGivenName(vaultAbsPath + "/", testFileName2,
				2);

		final DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig), parentFile, null, null);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig2), parentFile, null, null);

		// create the metadata manifest and process it

		final MetadataManifest manifest = new MetadataManifest();

		manifest.setFailureMode(FailureMode.FAIL_FAST);
		manifest.setParentIrodsTargetPath(parentFile.getAbsolutePath());
		manifest.getOperation().add(
				new MetadataManifestOperation(expectedAttribName, expectedValueName, "", testFileName, Action.ADD));
		manifest.getOperation().add(
				new MetadataManifestOperation(expectedAttribName, expectedValueName, "", testFileName2, Action.ADD));

		final MetadataManifestProcessor impl = new MetadataManifestProcessorImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		final List<BulkAVUOperationResponse> response = impl.processManifest(manifest);
		Assert.assertNotNull("no response", response);

	}

}
