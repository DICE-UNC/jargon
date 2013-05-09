package org.irods.jargon.core.pub.io;

import java.io.StringReader;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSFileReaderTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileReaderTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

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
		new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test(expected = JargonException.class)
	public void testIRODSFileReaderFileDoesNotExist() throws Exception {
		String testFileName = "testIRODSFileReaderFileDoesNotExist.txt";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		irodsFileFactory.instanceIRODSFileReader(targetIrodsFile);
	}

	@Test
	public void testIRODSFileReaderFile() throws Exception {
		String testFileName = "testCanRead.txt";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFileReader reader = irodsFileFactory
				.instanceIRODSFileReader(targetIrodsFile);

		reader.close();
		Assert.assertNotNull("reader was not returned from factory", reader);

	}

	@Test
	public void testReadFromReader() throws Exception {
		String testFileName = "testReadFromReader.txt";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		// write some chars
		IRODSFileWriter irodsFileWriter = irodsFileFactory
				.instanceIRODSFileWriter(targetIrodsFile);

		String testString = "jfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseij;ida8ehgasjfai'sjf;iadvajkdfgjasdl;jfasfjfaeiiiiiiiiiiiitsetseflyiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiispooniiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiisomewhereinthestringiiiiiiiiiconeiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiblarkiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiidangleiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskthisisthemiddleofthestringfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijthisistheendofthestrring";
		StringReader stringReader = new StringReader(testString);
		int testStringLength = testString.length();

		int buffLen = 512;
		char[] cbuff = new char[buffLen];
		int read = 0;

		int totalReadIn = 0;

		while ((read = stringReader.read(cbuff)) != -1) {
			totalReadIn += read;
			irodsFileWriter.write(cbuff, 0, read);
		}

		Assert.assertEquals("did not read and write same length as string",
				testString.length(), totalReadIn);

		irodsFileWriter.close();

		IRODSFileReader reader = irodsFileFactory
				.instanceIRODSFileReader(targetIrodsFile);
		Assert.assertNotNull("reader was not returned from factory", reader);

		char[] readBuff = new char[buffLen];
		int readCtr = 0;
		StringBuilder outputStringBuilder = new StringBuilder();

		while ((readCtr = reader.read(readBuff)) > -1) {
			outputStringBuilder.append(readBuff, 0, readCtr);
		}

		String actualReadBack = outputStringBuilder.toString();

		reader.close();
		Assert.assertEquals("did not read correct length", totalReadIn,
				testStringLength);
		Assert.assertEquals(
				"string read back does not match string originally written",
				testString, actualReadBack);
	}

}
