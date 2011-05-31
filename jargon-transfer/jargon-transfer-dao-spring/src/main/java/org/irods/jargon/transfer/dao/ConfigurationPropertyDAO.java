package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;

/**
 * DAO-style interface for managing KVP configuration properties for the
 * transfer engine. This is a generic property store that can also be used on
 * apps built on top of transfer engine. Care should be taken to namespace
 * define key names in case multiple apps desire to share this property store.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConfigurationPropertyDAO {

	/**
	 * Save the property to the store.  Note that the key of the key/value pair is a unique database value, so that duplicate properties cannot be added
	 * @param configurationProperty {@link ConfigurationProperty} that represents an entry of a configuration value in the database.
	 * @throws TransferDAOException
	 */
	void saveOrUpdate(ConfigurationProperty configurationProperty)
			throws TransferDAOException;

	/**
	 * Find a property based on the database id key
	 * @param id <code>Long</code> that contains the id of the desired record
	 * @return {@link ConfigurationProperty} that represents an entry of a configuration value in the database.
	 * @throws TransferDAOException
	 */
	ConfigurationProperty findById(Long id) throws TransferDAOException;

	/**
	 * Get all of the configuration properties stored in the database
	 * @return <code>List</code> of  {@link ConfigurationProperty} representing the configuration properties store as key/value pairs
	 * @throws TransferDAOException
	 */
	List<ConfigurationProperty> findAll() throws TransferDAOException;

	/**
	 * Delete the given property
	 * @param configurationProperty
	 * @throws TransferDAOException
	 */
	void delete(ConfigurationProperty configurationProperty)
			throws TransferDAOException;

	/**
	 * Clear all of the properties in the config database
	 * @throws TransferDAOException
	 */
	void deleteAllProperties() throws TransferDAOException;
	
}
