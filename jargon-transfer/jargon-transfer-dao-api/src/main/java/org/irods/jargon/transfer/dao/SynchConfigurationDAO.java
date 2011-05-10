package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.SynchConfiguration;

/**
 * DAO-style interface for managing the configuration of synchronization relationships for this transfer engine.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchConfigurationDAO {

	public void save(SynchConfiguration synchConfiguration)
			throws TransferDAOException;

	public SynchConfiguration findById(Long id) throws TransferDAOException;

	public SynchConfiguration findInitializedById(Long id)
			throws TransferDAOException;

	public SynchConfiguration findById(Long id, boolean error)
			throws TransferDAOException;

	public List<SynchConfiguration> findAll() throws TransferDAOException;

	public void delete(SynchConfiguration synchConfiguration)
			throws TransferDAOException;

}
