package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.PropertyValueException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
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
public class ConfigurationPropertyDAOTest {

	@Autowired
	private ConfigurationPropertyDAO configurationPropertyDAO;

	@Test
	public void testSave() throws Exception {

		String testKey = "testSave";
		String testValue = "testSaveValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);
		assertTrue(configProperty.getId() != null);

	}

	@Test
	public void testFindByKey() throws Exception {

		String testKey = "testFindByKey";
		String testValue = "testFindByKeyValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);
		assertTrue(configProperty.getId() != null);

		ConfigurationProperty actual = configurationPropertyDAO
				.findByPropertyKey(testKey);
		Assert.assertNotNull("did not find property by key", actual);

	}

	@Test
	public void testFindByKeyWhenNotExists() throws Exception {

		String testKey = "testFindByKeyWhenNotExists";
		ConfigurationProperty actual = configurationPropertyDAO
				.findByPropertyKey(testKey);
		Assert.assertNull("expected null result", actual);

	}

	@Test
	public void testSaveBlankKey() throws Exception {

		String testKey = "";
		String testValue = "testSaveValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());

		configurationPropertyDAO.saveOrUpdate(configProperty);
		assertTrue(configProperty.getId() != null);

	}

	@Test(expected = PropertyValueException.class)
	public void testSaveNullKey() throws Exception {

		String testKey = null;
		String testValue = "testSaveValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());

		configurationPropertyDAO.saveOrUpdate(configProperty);

		assertTrue(configProperty.getId() != null);

	}

	@Test
	public void testDelete() throws Exception {

		String testKey = "testDelete";
		String testValue = "testDeleteValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);

		assertTrue(configProperty.getId() != null);
		configurationPropertyDAO.delete(configProperty);
		configProperty = configurationPropertyDAO.findById(configProperty
				.getId());
		Assert.assertNull("should not get a config prop, was deleted",
				configProperty);

	}

	@Test
	public void testFindAll() throws Exception {

		String testKey1 = "testFindAll1";
		String testKey2 = "testFindAll2";

		List<ConfigurationProperty> configProperties = configurationPropertyDAO
				.findAll();

		String testValue = "testFindAllValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey1);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);

		configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey2);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);

		configProperties = configurationPropertyDAO
				.findAll();
		Assert.assertNotNull("did not find confg properties, was null",
				configProperties);
		Assert.assertFalse("empty config properties returned",
				configProperties.isEmpty());

	}

	@Test
	public void testDeleteAllProperties() throws Exception {

		String testKey1 = "testDeleteAllProperties1";
		String testKey2 = "testDeleteAllProperties2";

		String testValue = "testDeleteAllPropertiesValue";
		ConfigurationProperty configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey1);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);

		configProperty = new ConfigurationProperty();
		configProperty.setPropertyKey(testKey2);
		configProperty.setPropertyValue(testValue);
		configProperty.setCreatedAt(new Date());
		configurationPropertyDAO.saveOrUpdate(configProperty);

		List<ConfigurationProperty> configProperties = configurationPropertyDAO
				.findAll();
		Assert.assertNotNull("did not find confg properties, was null",
				configProperties);
		Assert.assertFalse("empty config properties returned",
				configProperties.isEmpty());

		configurationPropertyDAO.deleteAllProperties();
		configProperties = configurationPropertyDAO.findAll();
		Assert.assertNotNull("did not find confg properties, was null",
				configProperties);
		Assert.assertTrue("all properties should have been deleted",
				configProperties.isEmpty());
	}

}
