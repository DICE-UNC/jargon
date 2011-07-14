package org.irods.jargon.transfer.engine;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage configuration information for transfer engine. The
 * configuration is maintained as key/value pair properties. As such, it is
 * intended that clients that are built on top of the transfer engine library
 * can utilize this for application specific configuration storage.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConfigurationServiceImpl implements ConfigurationService {

	private ConfigurationPropertyDAO configurationPropertyDAO;

	private final Logger log = LoggerFactory
			.getLogger(ConfigurationServiceImpl.class);

	/**
	 * @return the configurationPropertyDAO
	 */
	public ConfigurationPropertyDAO getConfigurationPropertyDAO() {
		return configurationPropertyDAO;
	}

	/**
	 * @param configurationPropertyDAO
	 *            the configurationPropertyDAO to set
	 */
	public void setConfigurationPropertyDAO(
			final ConfigurationPropertyDAO configurationPropertyDAO) {
		this.configurationPropertyDAO = configurationPropertyDAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.ConfigurationService#
	 * listConfigurationProperties()
	 */
	@Override
	@Transactional
	public List<ConfigurationProperty> listConfigurationProperties()
			throws TransferEngineException {
		log.info("listConfigurationProperties()");
		try {
			return configurationPropertyDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new TransferEngineException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.ConfigurationService#
	 * findConfigurationServiceByKey(java.lang.String)
	 */
	@Override
	@Transactional
	public ConfigurationProperty findConfigurationServiceByKey(
			final String configurationKey) throws TransferEngineException {
		if (configurationKey == null || configurationKey.isEmpty()) {
			throw new IllegalArgumentException(
					"configurationKey is null or empty");
		}
		log.info("findConfigurationServiceByKey() key =", configurationKey);
		try {
			return configurationPropertyDAO.findByPropertyKey(configurationKey);
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new TransferEngineException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.ConfigurationService#
	 * deleteConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	@Transactional
	public void deleteConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws TransferEngineException {

		if (configurationProperty == null) {
			throw new TransferEngineException("null configurationProperty");
		}

		log.info("deleteConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.delete(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new TransferEngineException(e);
		}

		log.info("configuration deleted");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.ConfigurationService#
	 * addConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	@Transactional
	public ConfigurationProperty addConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws TransferEngineException {

		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		if (configurationProperty.getId() != 0) {
			throw new IllegalArgumentException(
					"attempting to add a configurationProperty that already is in the database");
		}

		log.info("addConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.saveOrUpdate(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("dao error updating configuration", e);
			throw new TransferEngineException(e);
		}

		log.info("configuration property added");
		return configurationProperty;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.ConfigurationService#
	 * updateConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	@Transactional
	public void updateConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws TransferEngineException {

		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("updateConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.saveOrUpdate(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("dao error updating configuration", e);
			throw new TransferEngineException(e);
		}

		log.info("configuration property updated");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.ConfigurationService#exportProperties()
	 */
	@Override
	@Transactional
	public Properties exportProperties() throws TransferEngineException {
		log.info("exportProperties()");
		Properties properties = new Properties();

		List<ConfigurationProperty> configurationProperties = null;
		try {
			configurationProperties = configurationPropertyDAO.findAll();
		} catch (TransferDAOException e) {
		}

		for (ConfigurationProperty configurationProperty : configurationProperties) {
			log.info("adding configuration property from database: {}",
					configurationProperty);
			properties.put(configurationProperty.getPropertyKey(),
					configurationProperty.getPropertyValue());
		}

		return properties;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.ConfigurationService#importProperties
	 * (java.util.Properties)
	 */
	@Override
	@Transactional
	public void importProperties(final Properties propertiesToImport)
			throws TransferEngineException {

		if (propertiesToImport == null) {
			throw new IllegalArgumentException("null propertiesToImport");
		}

		log.info("importing properties: {}", propertiesToImport);

		/*
		 * log.info("deleting old properties..."); try {
		 * configurationPropertyDAO.deleteAllProperties(); } catch
		 * (TransferDAOException e) {
		 * log.error("error deleting all configuration properties", e); throw
		 * new TransferEngineException(e); }
		 */

		ConfigurationProperty configurationProperty = null;
		log.info("adding/updating properties based on passed in values");

		String propVal;
		String propKey;

		for (Object key : propertiesToImport.keySet()) {
			propVal = (String) propertiesToImport.get(key);
			propKey = (String) key;
			configurationProperty = findConfigurationServiceByKey(propKey);

			if (configurationProperty == null) {
				configurationProperty = new ConfigurationProperty();
				configurationProperty.setCreatedAt(new Date());
				configurationProperty.setPropertyKey((String) key);
				configurationProperty.setPropertyValue((String) propVal);
			} else {
				configurationProperty.setPropertyValue(propVal);
				configurationProperty.setUpdatedAt(new Date());
			}
			try {
				configurationPropertyDAO.saveOrUpdate(configurationProperty);
			} catch (TransferDAOException e) {
				log.error("error adding property: {}", configurationProperty, e);
				throw new TransferEngineException(e);
			}

			log.debug(
					"added/updated configuration property from provided properties:{}",
					configurationProperty);

		}

	}
}
