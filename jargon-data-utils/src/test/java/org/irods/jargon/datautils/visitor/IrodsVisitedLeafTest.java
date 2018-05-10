package org.irods.jargon.datautils.visitor;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.datautils.indexer.NodeVisitLog;
import org.irods.jargon.datautils.indexer.NodeVisitLogEntry;
import org.irods.jargon.datautils.indexer.NodeVisitLogEntry.VisitTypeEnum;
import org.irods.jargon.datautils.indexer.TestIndexVisitor;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class IrodsVisitedLeafTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsVisitedLeafTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testAccept() throws Exception {

		String testFileName = "testAccept.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);
		IRODSFileImpl irodsFile = (IRODSFileImpl) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dataObjectAbsPath);

		IrodsVisitedLeaf leaf = new IrodsVisitedLeaf(irodsFile);
		TestIndexVisitor visitor = new TestIndexVisitor(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		leaf.accept(visitor);
		NodeVisitLog log = visitor.getNodeVisitLog();
		Assert.assertFalse("no log entries", log.getLogEntries().isEmpty());
		NodeVisitLogEntry actual = log.getLogEntries().get(0);
		Assert.assertEquals(VisitTypeEnum.VISIT_LEAF, actual.getVisitType());

	}

}
