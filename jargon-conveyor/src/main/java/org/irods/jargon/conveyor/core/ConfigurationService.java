package org.irods.jargon.conveyor.core;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
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
	ConfigurationProperty findConfigurationPropertyByKey(String configurationKey)
			throws ConveyorExecutionException;

	/**
	 * Build a jargon structure that controls transfers based on the available
	 * configuration.
	 * <p/>
	 * This service method is set up so as to be able to access the elements
	 * that can effect the transfer option settings.
	 * 
	 * @param restartPath
	 *            <code>String</code> that can be blank (not null), indicating
	 *            the last good path in case of a restart
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} representing the current
	 *            Jargon connection settings and configuration
	 * 
	 * @return {@link TransferControlBlock} structure based on conveyor service
	 *         configuration
	 * @throws ConveyorExecutionException
	 */
	TransferControlBlock buildDefaultTransferControlBlockBasedOnConfiguration(
			final String restartPath,
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws ConveyorExecutionException;

	/**
	 * Method retrieves a plain object that reflects the cached configuration
	 * state exposed as easy to use java methods. This service is responsible
	 * for maintaining the cache, which saves database access and is easy to use
	 * by other services.
	 * 
	 * @return
	 * @throws ConveyorExecutionException
	 */
	CachedConveyorConfigurationProperties getCachedConveyorConfigurationProperties()
			throws ConveyorExecutionException;

	/**
	 * Indicates whether the conveyor service is in 'tear off' mode, which means
	 * that it initializes fresh each time instead of caching data
	 * 
	 * @return <code>boolean</code> of true if the configuration indicates tear
	 *         off mode
	 * @throws ConveyorExecutionException
	 */
	boolean isInTearOffMode() throws ConveyorExecutionException;

}