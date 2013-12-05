package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.CachedConveyorConfigurationProperties;
import org.irods.jargon.conveyor.core.ConfigurationPropertyConstants;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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
@Transactional(rollbackFor = { ConveyorExecutionException.class })
public class ConfigurationServiceImpl extends AbstractConveyorComponentService
		implements ConfigurationService {

	private ConfigurationPropertyDAO configurationPropertyDAO;
	private CachedConveyorConfigurationProperties cachedConveyorConfigurationProperties = null;
	private Object propsLockObject = new Object();

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
		updateCachedConveyorConfigurationProperties();

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
			Date theDate = new Date();

			if (configurationProperty.getCreatedAt() == null) {
				configurationProperty.setCreatedAt(theDate);
			}

			configurationProperty.setUpdatedAt(theDate);

			configurationPropertyDAO.saveOrUpdate(configurationProperty);
		} catch (TransferDAOException e) {
			log.error("dao error updating configuration", e);
			throw new ConveyorExecutionException(e);
		}

		log.info("configuration property added");
		updateCachedConveyorConfigurationProperties();

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
		updateCachedConveyorConfigurationProperties();

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
			updateCachedConveyorConfigurationProperties();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * buildDefaultTransferControlBlockBasedOnConfiguration(java.lang.String,
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory)
	 */
	@Override
	public TransferControlBlock buildDefaultTransferControlBlockBasedOnConfiguration(
			final String restartPath,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws ConveyorExecutionException {

		log.info("buildDefaultTransferControlBlockBasedOnConfiguration()");
		String myRestartPath;

		if (restartPath == null) {
			myRestartPath = "";
		} else {
			myRestartPath = restartPath;
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		try {
			TransferControlBlock tcb;

			tcb = irodsAccessObjectFactory
					.buildDefaultTransferControlBlockBasedOnJargonProperties();
			tcb.setRestartAbsolutePath(myRestartPath);

			synchronized (propsLockObject) {
				tcb.setMaximumErrorsBeforeCanceling(getCachedConveyorConfigurationProperties()
						.getMaxErrorsBeforeCancel());
			}
			return tcb;
		} catch (JargonException e) {
			log.error("error building transfer control block", e);
			throw new ConveyorExecutionException(
					"cannot build transfer control block", e);
		}
	}

	/**
	 * This should be called whenever properties are updated or altered, this
	 * translates the key/value properties into a POJO and caches it for quick
	 * access
	 * 
	 * @throws ConveyorExecutionException
	 */
	private void updateCachedConveyorConfigurationProperties()
			throws ConveyorExecutionException {

		List<ConfigurationProperty> props = listConfigurationProperties();
		CachedConveyorConfigurationProperties cachedProps = new CachedConveyorConfigurationProperties();

		for (ConfigurationProperty property : props) {
			log.info("property:{}", property);
			if (property.getPropertyKey().equals(
					ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY)) {
				cachedProps.setLogSuccessfulTransfers(property
						.propertyValueAsBoolean());
			} else if (property
					.getPropertyKey()
					.equals(ConfigurationPropertyConstants.MAX_ERRORS_BEFORE_CANCEL_KEY)) {
				cachedProps.setMaxErrorsBeforeCancel(property
						.propertyValueAsInt());
			}
		}

		synchronized (propsLockObject) {
			this.cachedConveyorConfigurationProperties = cachedProps;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConfigurationService#
	 * getCachedConveyorConfigurationProperties()
	 */
	@Override
	public synchronized CachedConveyorConfigurationProperties getCachedConveyorConfigurationProperties()
			throws ConveyorExecutionException {

		log.info("getCachedConveyorConfigurationProperties");
		synchronized (propsLockObject) {

			if (this.cachedConveyorConfigurationProperties == null) {
				log.info("need to initialize configuration properties");
				updateCachedConveyorConfigurationProperties();
			}
			return cachedConveyorConfigurationProperties;
		}

	}

}
