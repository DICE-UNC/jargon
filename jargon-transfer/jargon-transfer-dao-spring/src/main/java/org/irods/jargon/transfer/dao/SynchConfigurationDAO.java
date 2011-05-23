package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.SynchConfiguration;

/**
 * DAO-style interface for managing the configuration of synchronization
 * relationships for this transfer engine.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchConfigurationDAO {

	void save(SynchConfiguration synchConfiguration)
			throws TransferDAOException;

	SynchConfiguration findById(Long id) throws TransferDAOException;

	List<SynchConfiguration> findAll() throws TransferDAOException;

	void delete(SynchConfiguration synchConfiguration)
			throws TransferDAOException;

	SynchConfiguration findInitializedById(Long id) throws TransferDAOException;

}
