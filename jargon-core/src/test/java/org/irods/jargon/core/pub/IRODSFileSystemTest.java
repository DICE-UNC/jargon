/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSFileSystemTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileSystemTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.IRODSFileSystem#instance()}.
	 */
	@Test
	public void testInstance() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		Assert.assertNotNull(irodsFileSystem);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.IRODSFileSystem#getIRODSAccessObjectFactory()}
	 * .
	 */
	@Test
	public void testGetIRODSAccessObjectFactory() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		Assert.assertNotNull(irodsAccessObjectFactory);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.IRODSFileSystem#getIRODSAccessObjectFactory()}
	 * .
	 */
	@Test
	public void testGetIRODSAccessObjectFactoryTwice() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		Assert.assertNotNull(irodsAccessObjectFactory);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.IRODSFileSystem#getIRODSFileFactory(org.irods.jargon.core.connection.IRODSAccount)}
	 * .
	 */
	@Test
	public void testGetIRODSFileFactory() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		irodsFileSystem.close();
		Assert.assertNotNull(irodsFileFactory);
	}

	@Test(expected = JargonException.class)
	public void testGetIRODSFileFactoryNullAccount() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsFileSystem.getIRODSFileFactory(null);
	}

	/**
	 * Test method for {@link org.irods.jargon.core.pub.IRODSFileSystem#close()}
	 * .
	 */
	@Test
	public void testClose() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		irodsFileSystem.close();
		Assert.assertNull(irodsFileSystem.getConnectionMap());
	}

	/**
	 * Test method for {@link org.irods.jargon.core.pub.IRODSFileSystem#close()}
	 * .
	 */
	@Test
	public void testCloseTwice() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		irodsFileSystem.close();
		irodsFileSystem.close();
		Assert.assertNull(irodsFileSystem.getConnectionMap());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.core.pub.IRODSFileSystem#getIRODSFileFactory(org.irods.jargon.core.connection.IRODSAccount)}
	 * .
	 */
	@Test
	public void testGetIRODSFileFactoryTwoAccounts() throws Exception {
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount irodsAccount2 = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		irodsFileSystem.getIRODSFileFactory(irodsAccount);
		irodsFileSystem.getIRODSFileFactory(irodsAccount2);
		Assert.assertNotNull(irodsFileSystem.getConnectionMap());
		Assert.assertEquals(2, irodsFileSystem.getConnectionMap().values()
				.size());

		irodsFileSystem.close(irodsAccount);
		Assert.assertEquals(1, irodsFileSystem.getConnectionMap().values()
				.size());
		irodsFileSystem.close(irodsAccount2);

		Assert.assertNull(irodsFileSystem.getConnectionMap());
	}

}
