package org.irods.jargon.datautils.indexer;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.datautils.indexer.NodeVisitLogEntry.VisitTypeEnum;
import org.irods.jargon.datautils.visitor.IrodsVisitedComposite;
import org.irods.jargon.datautils.visitor.IrodsVisitedLeaf;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractIndexerVisitorTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "AbstractIndexerVisitorTest";
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testVisitEnterWithMetadata() throws Exception {
		String testParentDir = "testVisitEnterWithMetadataParent";
		String testChildDir = "testVisitEnterWithMetadataChild";
		String avuAttribName = "avuAssociatedWith";

		String testFileName = "testVisitEnterWithMetadata.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsParentCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testParentDir);
		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testParentDir + "/" + testChildDir);

		IRODSFile parentDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsParentCollection);
		parentDir.mkdirs();

		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		AvuData avuData = AvuData.instance(avuAttribName, parentDir.getName(), "");

		collectionAO.addAVUMetadata(targetIrodsParentCollection, avuData);

		IRODSFile targetDir = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		targetDir.mkdirs();

		avuData = AvuData.instance(avuAttribName, targetDir.getName(), "");

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);
		avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);
		IRODSFileImpl irodsFile = (IRODSFileImpl) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dataObjectAbsPath);

		IrodsVisitedComposite composite = new IrodsVisitedComposite(irodsFile);
		TestIndexVisitor visitor = new TestIndexVisitor(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		composite.accept(visitor);
		NodeVisitLog log = visitor.getNodeVisitLog();
		Assert.assertFalse("no log entries", log.getLogEntries().isEmpty());
		NodeVisitLogEntry actual = log.getLogEntries().get(0);
		Assert.assertEquals(VisitTypeEnum.ENTER, actual.getVisitType());
		Assert.assertEquals(targetIrodsParentCollection, actual.getNodeAbsolutePath());
		Assert.assertEquals(1, actual.getMetadataThisLevel().size());
		MetaDataAndDomainData md = actual.getMetadataThisLevel().get(0);
		Assert.assertEquals(expectedAttribName, md.getAvuAttribute());
		Assert.assertEquals(testParentDir, md.getAvuValue());
	}

	@Test
	public void testVisitLeaveWithMetadata() {
		fail("Not yet implemented");
	}

	@Test
	public void testVisitWithMetadata() throws Exception {
		String testFileName = "testVisitWithMetadata.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);
		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);
		IRODSFileImpl irodsFile = (IRODSFileImpl) irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dataObjectAbsPath);

		IrodsVisitedLeaf leaf = new IrodsVisitedLeaf(irodsFile);
		TestIndexVisitor visitor = new TestIndexVisitor(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		leaf.accept(visitor);
		NodeVisitLog log = visitor.getNodeVisitLog();
		Assert.assertFalse("no log entries", log.getLogEntries().isEmpty());
		NodeVisitLogEntry actual = log.getLogEntries().get(0);
		Assert.assertEquals(VisitTypeEnum.VISIT_LEAF, actual.getVisitType());
		Assert.assertEquals(dataObjectAbsPath, actual.getNodeAbsolutePath());
		Assert.assertEquals(testFileName, actual.getNodeName());
		Assert.assertEquals(1, actual.getMetadataThisLevel().size());
		MetaDataAndDomainData md = actual.getMetadataThisLevel().get(0);
		Assert.assertEquals(expectedAttribName, md.getAvuAttribute());
	}

}
