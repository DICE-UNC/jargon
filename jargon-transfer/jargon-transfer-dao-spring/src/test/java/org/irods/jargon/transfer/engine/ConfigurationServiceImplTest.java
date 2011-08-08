package org.irods.jargon.transfer.engine;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml",
		"classpath:test-beans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class ConfigurationServiceImplTest {

	@Autowired
	private ConfigurationService configurationService;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
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
				.findConfigurationServiceByKey(testKey);
		Assert.assertNotNull("did not find key for props just added", actual);

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

	public void setConfigurationService(
			final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

}
