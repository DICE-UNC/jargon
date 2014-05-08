package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class ConfigurationServiceImplTest {

	@Autowired
	private ConfigurationService configurationService;

	@SuppressWarnings("unused")
	private static Properties testingProperties = new Properties();
	@SuppressWarnings("unused")
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;

	@Autowired
	private ConveyorService conveyorService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		conveyorService.getQueueManagerService().purgeAllFromQueue();
		conveyorService.getGridAccountService().resetPassPhraseAndAccounts();

	}

	@Test
	public void testListConfigurationProperties() throws Exception {
		String testKey1 = "testAddConfigurationProperty1";
		String testKey2 = "testAddConfigurationProperty2";
		String testValue = "testAddConfigurationPropertyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey1);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationService.addConfigurationProperty(configProperty);

		configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey2);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationService.addConfigurationProperty(configProperty);

		List<ConfigurationProperty> configurationProperties = configurationService
				.listConfigurationProperties();
		Assert.assertNotNull("null configurationProperties",
				configurationProperties);
		Assert.assertFalse("no props found", configurationProperties.isEmpty());

	}

	@Test
	public void testAddConfigurationProperty() throws Exception {
		String testKey = "testAddConfigurationProperty";
		String testValue = "testAddConfigurationPropertyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		ConfigurationProperty actual = configurationService
				.addConfigurationProperty(configProperty);
		Assert.assertNotNull("null configuration property", actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConfigurationPropertyBlankKey() throws Exception {
		String testKey = "";
		String testValue = "testAddConfigurationPropertyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationService.addConfigurationProperty(configProperty);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConfigurationPropertNull() throws Exception {
		configurationService.addConfigurationProperty(null);
	}

	@Test
	public void testDeleteConfigurationProperty() throws Exception {
		String testKey = "testDeleteConfigurationProperty";
		String testValue = "testDeleteConfigurationPropertyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		ConfigurationProperty actual = configurationService
				.addConfigurationProperty(configProperty);
		Assert.assertNotNull("null configuration property", actual);
		configurationService.deleteConfigurationProperty(actual);
		Assert.assertTrue(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteConfigurationPropertyNull() throws Exception {
		configurationService.deleteConfigurationProperty(null);
	}

	@Test
	public void testUpdateConfigurationProperty() throws Exception {
		String testKey = "testUpdateConfigurationProperty";
		String testValue = "testUpdateConfigurationPropertyVal";
		String testUpdatedValue = "testUpdateConfigurationPropertyValUpdated";

		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		ConfigurationProperty actual = configurationService
				.addConfigurationProperty(configProperty);
		Assert.assertNotNull("null configuration property", actual);

		actual.setPropertyValue(testUpdatedValue);
		configurationService.updateConfigurationProperty(actual);

		// no error means success
		Assert.assertTrue(true);

	}

	@Test
	public void testFindConfigurationPropertyByKey() throws Exception {
		String testKey = "testFindConfigurationPropertyByKeyProperty";
		String testValue = "testFindConfigurationPropertyByKeyVal";

		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationService.addConfigurationProperty(configProperty);

		ConfigurationProperty actual = configurationService
				.findConfigurationPropertyByKey(testKey);
		Assert.assertNotNull("did not find key for props just added", actual);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindConfigurationPropertyByKeyNull() throws Exception {
		configurationService.findConfigurationPropertyByKey(null);
	}

	@Test
	public void testExportConfigurationProperties() throws Exception {
		String testKey = "testGetConfigurationProperties";
		String testValue = "testGetConfigurationPropertiesPropertyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationService.addConfigurationProperty(configProperty);
		Properties properties = configurationService.exportProperties();
		Assert.assertNotNull("null configuration properties retrieved",
				properties);
		String actual = properties.getProperty(testKey);
		Assert.assertNotNull("test property not returned in properties", actual);
		Assert.assertEquals("property value not set", testValue, actual);
	}

	@Test
	public void testImportConfigurationProperties() throws Exception {
		Properties testProperties = new Properties();
		String testKey1 = "testAddConfigurationProperty1";
		String testKey2 = "testAddConfigurationProperty2";
		String testValue = "testAddConfigurationPropertyValue";

		testProperties.put(testKey1, testValue);
		testProperties.put(testKey2, testValue);
		configurationService.importProperties(testProperties);
		Properties properties = configurationService.exportProperties();
		Assert.assertNotNull("null configuration properties retrieved",
				properties);
		String actual = properties.getProperty(testKey1);
		Assert.assertNotNull("test property not returned in properties", actual);
		Assert.assertEquals("property value not set", testValue, actual);

	}

	@Test
	public void testBuildTransferControlBlockNullPath() throws Exception {
		TransferControlBlock actual = configurationService
				.buildDefaultTransferControlBlockBasedOnConfiguration(null,
						irodsFileSystem.getIRODSAccessObjectFactory());
		Assert.assertEquals("did not set restart", "",
				actual.getRestartAbsolutePath());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildTransferControlBlockNullAccessObjectFactory()
			throws Exception {
		configurationService
				.buildDefaultTransferControlBlockBasedOnConfiguration("/path",
						null);
	}

	@Test
	public void testBuildTransferControlBlock() throws Exception {
		TransferControlBlock actual = configurationService
				.buildDefaultTransferControlBlockBasedOnConfiguration("",
						irodsFileSystem.getIRODSAccessObjectFactory());
		Assert.assertEquals("did not set restart", "",
				actual.getRestartAbsolutePath());
	}

	@Test
	public void testBuildTransferControlBlockWithRestart() throws Exception {
		TransferControlBlock actual = configurationService
				.buildDefaultTransferControlBlockBasedOnConfiguration(
						"restart",
						irodsFileSystem.getIRODSAccessObjectFactory());
		Assert.assertEquals("did not set restart", "restart",
				actual.getRestartAbsolutePath());
	}

	public void setConfigurationService(
			final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

}
