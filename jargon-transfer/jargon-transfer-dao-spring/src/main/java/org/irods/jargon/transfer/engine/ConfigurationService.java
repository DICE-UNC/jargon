package org.irods.jargon.transfer.engine;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;

/**
 * Interface that describes the service layer for managing configuration information for transfer engine, and any application built on top of transfer engine.  The configuration 
 * information is stored as key/value pairs.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ConfigurationService {
	
	public static final String VERSION_PROPERTY = "version";

	/**
	 * Retrieve the stored configuration information.
	 * @return <code>List</code> of {@link ConfgurationProperty} which represents the stored configuration information.
	 * @throws TransferEngineException
	 */
	List<ConfigurationProperty> listConfigurationProperties()
			throws TransferEngineException;

	/**
	 * Add a new configuration property to the configuration store.
	 * @param configurationProperty {@link ConfigurationProperty} that will be added to the store
	 * @return {@link ConfigurationProperty} that has been stored
	 * @throws TransferEngineException
	 */
	ConfigurationProperty addConfigurationProperty(ConfigurationProperty configurationProperty)
			throws TransferEngineException;

	/**
	 * Update an existing configuration property
	 * @param configurationProperty {@link ConfigurationProperty} that will be updated in the store
	 * @throws TransferEngineException
	 */
	void updateConfigurationProperty(ConfigurationProperty configurationProperty)
			throws TransferEngineException;

	/**
	 * Delete the given property from the config store.
	 * @param configurationProperty  {@link ConfigurationProperty} that will be deleted from the store
	 * @throws TransferEngineException
	 */
	void deleteConfigurationProperty(ConfigurationProperty configurationProperty)
			throws TransferEngineException;

	/**
	 * Given a set of properties, clear the config database and set to the provided property set
	 * @param propertiesToImport {@link Properties} that contains the configuration information in key/value format.  This will replace the contents of the database
	 * @throws TransferEngineException
	 */
	void importProperties(Properties propertiesToImport)
			throws TransferEngineException;

	/**
	 * Export the properties in the configuration properties database as a set of <code>Properties</code>.
	 * @return
	 * @throws TransferEngineException
	 */
	Properties exportProperties() throws TransferEngineException;

}