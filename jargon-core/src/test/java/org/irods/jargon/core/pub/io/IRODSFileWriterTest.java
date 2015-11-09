package org.irods.jargon.core.pub.io;

import java.io.StringReader;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSFileWriterTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileWriterTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

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
		new org.irods.jargon.testutils.AssertionHelper();
	}

	@Test
	public void testWriteCharArray() throws Exception {

		String testFileName = "testWriterCharArray.doc";

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

		IRODSFileWriter irodsFileWriter = irodsFileFactory
				.instanceIRODSFileWriter(targetIrodsFile);

		String testString = "jfaeiiiiiiiiiiiiiiiiiiiiii838ejfiafjaskfjaisdfjaseij;ida8ehgasjfai'sjf;iadvajkdfgjasdl;jfasf";

		StringReader stringReader = new StringReader(testString);
		int buffLen = 10;
		char[] cbuff = new char[buffLen];
		int read = 0;

		while ((read = stringReader.read(cbuff)) != -1) {
			irodsFileWriter.write(cbuff, 0, read);
		}

		irodsFileWriter.close();

		Assert.assertNotNull("null irodsFileWriter", irodsFileWriter);
		irodsSession.closeSession();

		// now check the length
		irodsSession = IRODSSession.instance(irodsConnectionManager);

		irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile readbackFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		long length = readbackFile.length();
		irodsSession.closeSession();
		Assert.assertEquals(
				"file on irods is not same length as originating string",
				testString.length(), length);
	}

	@Test
	public void testIRODSFileWriter() throws Exception {
		String testFileName = "testCanWrite.txt";

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

		IRODSFileWriter irodsFileWriter = irodsFileFactory
				.instanceIRODSFileWriter(targetIrodsFile);

		Assert.assertNotNull("null irodsFileWriter", irodsFileWriter);
		irodsSession.closeSession();
	}

}
