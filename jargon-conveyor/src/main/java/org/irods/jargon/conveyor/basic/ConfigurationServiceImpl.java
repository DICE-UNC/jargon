package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.core.transfer.TransferControlBlock;
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
@Transactional
public class ConfigurationServiceImpl extends AbstractConveyorComponentService
		implements ConfigurationService {

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
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * listConfigurationProperties()
	 */
	@Override
	public List<ConfigurationProperty> listConfigurationProperties()
			throws ConveyorExecutionException {
		log.info("listConfigurationProperties()");
		try {
			return configurationPropertyDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new ConveyorExecutionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * findConfigurationServiceByKey(java.lang.String)
	 */
	@Override
	public ConfigurationProperty findConfigurationServiceByKey(
			final String configurationKey) throws ConveyorExecutionException {
		if (configurationKey == null || configurationKey.isEmpty()) {
			throw new IllegalArgumentException(
					"configurationKey is null or empty");
		}
		log.info("findConfigurationServiceByKey() key = {}", configurationKey);
		try {
			return configurationPropertyDAO.findByPropertyKey(configurationKey);
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new ConveyorExecutionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * deleteConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void deleteConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException {

		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("deleteConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.delete(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("DAO exception", e);
			throw new ConveyorExecutionException(e);
		}

		log.info("configuration deleted");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConfigurationService#addConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public ConfigurationProperty addConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException {

		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("addConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.saveOrUpdate(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("dao error updating configuration", e);
			throw new ConveyorExecutionException(e);
		}

		log.info("configuration property added");
		return configurationProperty;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * updateConfigurationProperty
	 * (org.irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void updateConfigurationProperty(
			final ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException {

		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("updateConfigurationProperty(ConfigurationProperty) with: {}",
				configurationProperty);

		try {
			configurationPropertyDAO.saveOrUpdate(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("dao error updating configuration", e);
			throw new ConveyorExecutionException(e);
		}

		log.info("configuration property updated");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConfigurationService#exportProperties()
	 */
	@Override
	public Properties exportProperties() throws ConveyorExecutionException {
		log.info("exportProperties()");
		Properties properties = new Properties();

		List<ConfigurationProperty> configurationProperties = null;
		try {
			configurationProperties = configurationPropertyDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error finding all", e);
			throw new ConveyorExecutionException(e);
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
	 * org.irods.jargon.conveyor.core.ConfigurationService#importProperties(
	 * java.util.Properties)
	 */
	@Override
	public void importProperties(final Properties propertiesToImport)
			throws ConveyorExecutionException {

		if (propertiesToImport == null) {
			throw new IllegalArgumentException("null propertiesToImport");
		}

		log.info("importing properties: {}", propertiesToImport);

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
				configurationProperty.setPropertyValue(propVal);
			} else {
				configurationProperty.setPropertyValue(propVal);
				configurationProperty.setUpdatedAt(new Date());
			}
			try {
				configurationPropertyDAO.saveOrUpdate(configurationProperty);
			} catch (TransferDAOException e) {
				log.error("error adding property: {}", configurationProperty, e);
				throw new ConveyorExecutionException(e);
			}

			log.debug(
					"added/updated configuration property from provided properties:{}",
					configurationProperty);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * buildDefaultTransferControlBlockBasedOnConfiguration()
	 */
	@Override
	public TransferControlBlock buildDefaultTransferControlBlockBasedOnConfiguration() {
		// FIXME: implement this based on props
		return null;
	}
}
