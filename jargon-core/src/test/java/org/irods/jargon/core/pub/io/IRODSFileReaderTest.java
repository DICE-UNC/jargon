package org.irods.jargon.core.pub.io;

import java.io.StringReader;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSFileReaderTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileReaderTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

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
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	@Test(expected = JargonException.class)
	public void testIRODSFileReaderFileDoesNotExist() throws Exception {
		String testFileName = "testCanRead.txt";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
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

		int fileLength = 40;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String testFileAbsPath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(testFileAbsPath);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		IRODSFileReader reader = irodsFileFactory
				.instanceIRODSFileReader(targetIrodsFile);

		reader.close();
		irodsSession.closeSession();
		TestCase.assertNotNull("reader was not returned from factory", reader);

	}

	@Test
	public void testReadFromReader() throws Exception {
		String testFileName = "testReadFromReader.txt";

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);

		// write some chars
		IRODSFileWriter irodsFileWriter = irodsFileFactory
				.instanceIRODSFileWriter(targetIrodsFile);

		String testString =
				"jfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseij;ida8ehgasjfai'sjf;iadvajkdfgjasdl;jfasfjfaeiiiiiiiiiiiitsetseflyiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiispooniiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiisomewhereinthestringiiiiiiiiiconeiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiblarkiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiidangleiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskthisisthemiddleofthestringfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijjfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseijthisistheendofthestrring";
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

		TestCase.assertEquals("did not read and write same length as string",testString.length(), totalReadIn);
		
		irodsFileWriter.close();

		IRODSFileReader reader = irodsFileFactory
				.instanceIRODSFileReader(targetIrodsFile);
		TestCase.assertNotNull("reader was not returned from factory", reader);

		char[] readBuff = new char[buffLen];
		int readCtr = 0;
		int totalRead = 0;
		
		StringBuilder outputStringBuilder = new StringBuilder();
		
		while ((readCtr = reader.read(readBuff)) > -1) {
			totalRead += readCtr;
			outputStringBuilder.append(readBuff, 0, readCtr);
		}
		
		String actualReadBack = outputStringBuilder.toString();

		reader.close();
		irodsSession.closeSession();
		TestCase
		.assertEquals("did not read correct length", totalReadIn, testStringLength);
		TestCase.assertEquals("string read back does not match string originally written", testString, actualReadBack);
	}

}
