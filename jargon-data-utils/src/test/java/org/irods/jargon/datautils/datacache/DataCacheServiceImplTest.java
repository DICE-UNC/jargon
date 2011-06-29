package org.irods.jargon.datautils.datacache;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.datacache.DataCacheServiceImpl;
import org.irods.jargon.datautils.datacache.CacheServiceConfiguration;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class DataCacheServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "DataCacheServiceImplTest";
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
		DataCacheService accountCacheService = new DataCacheServiceImpl();
		accountCacheService.setIrodsAccessObjectFactory(Mockito
				.mock(IRODSAccessObjectFactory.class));
		accountCacheService.putSerializedEncryptedObjectIntoCache("obj", "key");
	}

	@Test(expected = JargonRuntimeException.class)
	public void testPutInformationIntoCacheMissingIRODSAccessObjectFactory()
			throws Exception {
		DataCacheService accountCacheService = new DataCacheServiceImpl();
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		accountCacheService.putSerializedEncryptedObjectIntoCache("obj", "key");
	}

	@Test
	public void testPutStringIntoCacheUsingDefaultsUserHomeDirCache()
			throws Exception {
		String testData = "testDataStringToEncrypt for method testPutStringIntoCacheUsingDefaultsUserHomeDirCache";
		String testKey = "this is a test key for the encryption";
		IRODSAccount irodsAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
		DataCacheService accountCacheService = new DataCacheServiceImpl();
		CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();
		String testDir = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY) + "/" + IRODS_TEST_SUBDIR_PATH;
		cacheServiceConfiguration.setCacheDirPath(testDir);
		accountCacheService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		accountCacheService.setCacheServiceConfiguration(cacheServiceConfiguration);
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		
		String cacheFilePath = accountCacheService.putStringValueIntoCache(testData, testKey);
		IRODSFile cacheFile = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(irodsAccount).instanceIRODSFile(cacheFilePath);
		TestCase.assertTrue("cache file not created", cacheFile.exists());
	}
	
	@Test
	public void testPutAndRetrieveStringIntoCacheUsingDefaultsUserHomeDirCache()
			throws Exception {
		String testData = "testDataStringToEncrypt for method testPutAndRetrieveStringIntoCacheUsingDefaultsUserHomeDirCache";
		String testKey = "testPutAndRetrieveStringIntoCacheUsingDefaultsUserHomeDirCache";
		IRODSAccount irodsAccount = testingPropertiesHelper
		.buildIRODSAccountFromTestProperties(testingProperties);
		DataCacheService accountCacheService = new DataCacheServiceImpl();
		CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();
		String testDir = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY) + "/" + IRODS_TEST_SUBDIR_PATH;
		cacheServiceConfiguration.setCacheDirPath(testDir);
		accountCacheService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		accountCacheService.setCacheServiceConfiguration(cacheServiceConfiguration);
		accountCacheService.setIrodsAccount(testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties));
		
		String cacheFilePath = accountCacheService.putStringValueIntoCache(testData, testKey);
		IRODSFile cacheFile = irodsFileSystem.getIRODSAccessObjectFactory().getIRODSFileFactory(irodsAccount).instanceIRODSFile(cacheFilePath);
		TestCase.assertTrue("cache file not created", cacheFile.exists());
		
		String retrievedData = accountCacheService.retrieveStringValueFromCache(irodsAccount.getUserName(), testKey);
		TestCase.assertEquals("did not get expected data that was cached", testData, retrievedData);
		
	}
	
	@Test
	public void testPutInformationIntoCacheUsingDefaultsUserHomeDirCache()
			throws Exception {
		DataCacheService accountCacheService = new DataCacheServiceImpl();
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
		DataCacheService accountCacheService = new DataCacheServiceImpl();
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
