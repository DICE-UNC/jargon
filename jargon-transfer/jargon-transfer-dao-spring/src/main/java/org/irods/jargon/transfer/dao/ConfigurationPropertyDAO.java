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

	public void save(ConfigurationProperty configurationProperty)
			throws TransferDAOException;

	public ConfigurationProperty findById(Long id) throws TransferDAOException;

	public ConfigurationProperty findById(Long id, boolean error)
			throws TransferDAOException;

	public List<ConfigurationProperty> findAll() throws TransferDAOException;

	public void delete(ConfigurationProperty configurationProperty)
			throws TransferDAOException;

}
