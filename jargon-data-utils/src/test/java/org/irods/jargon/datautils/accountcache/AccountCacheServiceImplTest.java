package org.irods.jargon.datautils.accountcache;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class AccountCacheServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "AccountCacheServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test(expected = JargonRuntimeException.class)
	public void testPutInformationIntoCacheMissingIRODSAccount()
			throws Exception {
		AccountCacheServiceImpl accountCacheService = new AccountCacheServiceImpl();
		accountCacheService.setIrodsAccessObjectFactory(Mockito
				.mock(IRODSAccessObjectFactory.class));
		accountCacheService.putSerializedEncryptedObjectIntoCache("obj", "key");
	}

	@Test(expected = JargonRuntimeException.class)
	public void testPutInformationIntoCacheMissingIRODSAccessObjectFactory()
			throws Exception {
		AccountCacheServiceImpl accountCacheService = new AccountCacheServiceImpl();
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		accountCacheService.putSerializedEncryptedObjectIntoCache("obj", "key");
	}

	@Test
	public void testPutInformationIntoCacheUsingDefaultsUserHomeDirCache()
			throws Exception {
		AccountCacheServiceImpl accountCacheService = new AccountCacheServiceImpl();
		CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();
		String testDir = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY) + "/" + IRODS_TEST_SUBDIR_PATH;
		cacheServiceConfiguration.setCacheDirPath(testDir);
		accountCacheService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		accountCacheService.setCacheServiceConfiguration(cacheServiceConfiguration);
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		String cacheFilePath = accountCacheService.putSerializedEncryptedObjectIntoCache(irodsAccount, "key");
		IRODSFile cacheFile = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(irodsAccount).instanceIRODSFile(cacheFilePath);
		TestCase.assertTrue("cache file not created", cacheFile.exists());
	}
	
	@Test
	public void testPutInformationIntoCacheUsingDefaultsUserHomeDirCacheRoundTrip()
			throws Exception {
		String cacheKey = "testPutInformationIntoCacheUsingDefaultsUserHomeDirCacheRoundTrip";
		AccountCacheServiceImpl accountCacheService = new AccountCacheServiceImpl();
		CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();
		String testDir = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY) + "/" + IRODS_TEST_SUBDIR_PATH;
		cacheServiceConfiguration.setCacheDirPath(testDir);
		accountCacheService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		accountCacheService.setCacheServiceConfiguration(cacheServiceConfiguration);
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		accountCacheService.putSerializedEncryptedObjectIntoCache(irodsAccount, cacheKey);
		
		Object retrievedObject = accountCacheService.retrieveObjectFromCache(irodsAccount.getUserName(), cacheKey);
		TestCase.assertNotNull("no cached object returned");
		boolean isIRODSAccount = retrievedObject instanceof IRODSAccount;
		TestCase.assertTrue("not an instance of IRODSAccount", isIRODSAccount);
	
	}
}
