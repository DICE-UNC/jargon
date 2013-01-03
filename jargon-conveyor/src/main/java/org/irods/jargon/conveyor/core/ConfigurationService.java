package org.irods.jargon.conveyor.core;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;

/**
 * Interface that describes the service layer for managing configuration
 * information for transfer engine, and any application built on top of transfer
 * engine. The configuration information is stored as key/value pairs.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConfigurationService {

	public static final String VERSION_PROPERTY = "version";

	/**
	 * Retrieve the stored configuration information.
	 * 
	 * @return <code>List</code> of {@link ConfgurationProperty} which
	 *         represents the stored configuration information.
	 * @throws ConveyorExecutionException
	 */
	List<ConfigurationProperty> listConfigurationProperties()
			throws ConveyorExecutionException;

	/**
	 * Add a new configuration property to the configuration store.
	 * 
	 * @param configurationProperty
	 *            {@link ConfigurationProperty} that will be added to the store
	 * @return {@link ConfigurationProperty} that has been stored
	 * @throws ConveyorExecutionException
	 */
	ConfigurationProperty addConfigurationProperty(
			ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException;

	/**
	 * Update an existing configuration property
	 * 
	 * @param configurationProperty
	 *            {@link ConfigurationProperty} that will be updated in the
	 *            store
	 * @throws ConveyorExecutionException
	 */
	void updateConfigurationProperty(ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException;

	/**
	 * Delete the given property from the config store.
	 * 
	 * @param configurationProperty
	 *            {@link ConfigurationProperty} that will be deleted from the
	 *            store
	 * @throws ConveyorExecutionException
	 */
	void deleteConfigurationProperty(ConfigurationProperty configurationProperty)
			throws ConveyorExecutionException;

	/**
	 * Given a set of properties, add or update the database properties. This
	 * method will retain any other existing properties in the database
	 * unaltered.
	 * 
	 * @param propertiesToImport
	 *            {@link Properties} that contains the configuration information
	 *            in key/value format. This will replace the contents of the
	 *            ConveyorExecutionException
	 * @throws TransferEngineException
	 */
	void importProperties(Properties propertiesToImport)
			throws ConveyorExecutionException;

	/**
	 * Export the properties in the configuration properties database as a set
	 * of <code>Properties</code>.
	 * 
	 * @return
	 * @throws ConveyorExecutionException
	 */
	Properties exportProperties() throws ConveyorExecutionException;

	/**
	 * Given a key, find the configuration information for that key, or
	 * <code>null</code> if no such configuration property exists
	 * 
	 * @param configurationKey
	 *            <code>String</code> with the key for the given configuration
	 * @return {@link ConfigurationProperty} for the key, or <code>null</code>
	 *         if not exists
	 * @throws ConveyorExecutionException
	 */
	ConfigurationProperty findConfigurationServiceByKey(String configurationKey)
			throws ConveyorExecutionException;

}