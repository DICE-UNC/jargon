package org.irods.jargon.datautils.indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferStatusCallbackListenerTestingImplementation;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class IndexerFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "IndexerFunctionalTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static ScratchFileUtils scratchFileUtils = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@Test
	public void testProvisionCollections() throws Exception {
		String rootCollection = "IndexerFunctionalTestRoot";

		TransferStatusCallbackListenerTestingImplementation listener = new TransferStatusCallbackListenerTestingImplementation();

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
						IRODS_TEST_SUBDIR_PATH + "/" + rootCollection);

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"indexer", 3, 4, 6, "testIndexedFile", ".txt", 4, 2, 10, 20);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, listener, null);
		List<AvuData> collectionAvus = new ArrayList<AvuData>();
		List<AvuData> dataAvus = new ArrayList<AvuData>();

		// data avus

		dataAvus.add(AvuData.instance("attr1", "val1", ""));
		dataAvus.add(AvuData.instance("attr1", "val2", ""));
		dataAvus.add(AvuData.instance("attr1", "val3", ""));
		dataAvus.add(AvuData.instance("attr1", "val4", ""));
		dataAvus.add(AvuData.instance("attr1", "val5", ""));
		dataAvus.add(AvuData.instance("attr1", "val6", ""));
		dataAvus.add(AvuData.instance("attr1", "val7", ""));
		dataAvus.add(AvuData.instance("attr1", "val8", ""));
		dataAvus.add(AvuData.instance("attr1", "val9", ""));
		dataAvus.add(AvuData.instance("attr1", "val10", ""));

		// coll avus

		collectionAvus.add(AvuData.instance("attr1", "val1", ""));
		collectionAvus.add(AvuData.instance("attr1", "val2", ""));
		collectionAvus.add(AvuData.instance("attr1", "val3", ""));
		collectionAvus.add(AvuData.instance("attr1", "val4", ""));
		collectionAvus.add(AvuData.instance("attr1", "val5", ""));
		collectionAvus.add(AvuData.instance("attr1", "val6", ""));
		collectionAvus.add(AvuData.instance("attr1", "val7", ""));
		collectionAvus.add(AvuData.instance("attr1", "val8", ""));
		collectionAvus.add(AvuData.instance("attr1", "val9", ""));
		collectionAvus.add(AvuData.instance("attr1", "val10", ""));

		irodsTestSetupUtilities.decorateDirWithMetadata(irodsCollectionRootAbsolutePath, dataAvus, collectionAvus, 85);
		Assert.assertTrue(true);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

}
