/**
 *
 */
package org.irods.jargon.datautils.filesampler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.SettableJargonPropertiesMBean;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE
 *
 */
public class FileSamplerServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileSamplerServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonPropertiesMBean settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		settableJargonProperties.setInternalCacheBufferSize(-1);
		settableJargonProperties.setInternalOutputStreamBufferSize(65535);
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.filesampler.FileSamplerServiceImpl#sampleToByteArray(java.lang.String, int)}
	 * .
	 */
	@Test
	public void testSampleToByteArray() throws Exception {

		// generate a local scratch file
		String testFileName = "testSampleToByteArray.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 100 * 1024);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);

		dto.putOperation(localFileName, targetIrodsCollection, "", null, null);

		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		int sampleSize = 10 * 1024;
		byte[] actual = service.sampleToByteArray(targetIrodsCollection + "/" + testFileName, sampleSize);
		Assert.assertFalse("empty result", actual.length == 0);
		Assert.assertEquals("sampleSize not as requested", sampleSize, actual.length);

	}

	@Test(expected = FileNotFoundException.class)
	public void testStringFromFileMissing() throws Exception {
		String testFileName = "testStringFromFileMissing.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		service.convertFileContentsToString(targetFile.getAbsolutePath(), 0);

	}

	@Test
	public void testStringFromFile() throws Exception {
		String testFileName = "testStringFromFile.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		File localText = LocalFileUtils.getClasspathResourceAsFile("/text/test1.txt");

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localText, targetFile, null, null);
		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		String actual = service.convertFileContentsToString(targetFile.getAbsolutePath(), 0);
		Assert.assertFalse("no data returned", actual.isEmpty());

	}

	@Test(expected = FileTooLargeException.class)
	public void testStringFromFileTooLarge() throws Exception {
		String testFileName = "testStringFromFileTooLarge.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		File localText = LocalFileUtils.getClasspathResourceAsFile("/text/test1.txt");

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localText, targetFile, null, null);
		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		service.convertFileContentsToString(targetFile.getAbsolutePath(), 1);

	}

	@Test
	public void testStringToFileTwice() throws Exception {
		String testFileName = "testStringToFileTwice.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		String hello = "hello there from jargon";
		service.saveStringToFile(hello, targetFile.getAbsolutePath());
		service.saveStringToFile(hello, targetFile.getAbsolutePath());

		String helloFromIrods = service.convertFileContentsToString(targetFile.getAbsolutePath(), 0);
		Assert.assertEquals(hello, helloFromIrods);

	}

	/**
	 * Test for https://github.com/DICE-UNC/jargon/issues/232
	 *
	 * @throws Exception
	 */
	@Test
	public void testStringToFileTwiceSaveMetadataBug232() throws Exception {
		String testFileName = "testStringToFileTwice.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		String hello = "hello there from jargon";
		service.saveStringToFile(hello, targetFile.getAbsolutePath());

		String expectedAttribName = "testStringToFileTwiceSaveMetadataBug232";
		String expectedValueName = "testval1";
		AvuData avuData = AvuData.instance(expectedAttribName, expectedValueName, "");
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		dataObjectAO.addAVUMetadata(targetFile.getAbsolutePath(), avuData);

		service.saveStringToFile(hello, targetFile.getAbsolutePath());

		String helloFromIrods = service.convertFileContentsToString(targetFile.getAbsolutePath(), 0);
		Assert.assertEquals(hello, helloFromIrods);

		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, expectedAttribName));

		List<DataObject> dataObjects = dataObjectAO.findDomainByMetadataQuery(avuQueryElements);

		int nonTrashCount = 0;

		for (DataObject dataObject : dataObjects) {
			if (!dataObject.getAbsolutePath().contains("trash")) {
				nonTrashCount++;
			}
		}

		if (nonTrashCount == 0) {
			Assert.fail("avus were not preserved");
		}

	}

	@Test
	public void testStringToFile() throws Exception {
		String testFileName = "testStringToFile.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection, testFileName);

		FileSamplerService service = new FileSamplerServiceImpl(accessObjectFactory, irodsAccount);
		String hello = "hello there from jargon";
		service.saveStringToFile(hello, targetFile.getAbsolutePath());
		String helloFromIrods = service.convertFileContentsToString(targetFile.getAbsolutePath(), 0);
		Assert.assertEquals(hello, helloFromIrods);

	}

}
