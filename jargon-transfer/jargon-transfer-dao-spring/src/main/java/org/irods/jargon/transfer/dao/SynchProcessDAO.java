package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.SynchProcess;

/**
 * DAO-style interface for managing the
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface SynchProcessDAO {

	/**
	 * Save (via saveOrUpdate) the SynchProcess data
	 * 
	 * @param synchProcess
	 *            {@link SynchProcess} domain data
	 * @throws TransferDAOException
	 */
	void save(SynchProcess synchProcess) throws TransferDAOException;

	/**
	 * Find a <code>SynchProcess</code> by its unique database key
	 * 
	 * @param id
	 *            <code>Long</code> with the database key value
	 * @return {@link SynchProcess} with the given data, or <code>null</code> if
	 *         no such record exists
	 * @throws TransferDAOException
	 */
	SynchProcess findById(Long id) throws TransferDAOException;

	/**
	 * Return all <code>SynchProcess</code> entries in the database
	 * 
	 * @return <code>List</code> of {@link SynchProcess} in the database
	 * @throws TransferDAOException
	 */
	List<SynchProcess> findAll() throws TransferDAOException;

	/**
	 * Delete the given <code>SynchProcess</code> from the database
	 * 
	 * @param synchProcess
	 *            {@link SynchProcess}
	 * @throws TransferDAOException
	 */
	void delete(SynchProcess synchProcess) throws TransferDAOException;

	/**
	 * Find a <code>SynchProcess</code> by its unique database key and
	 * initialize any child records (avoiding proxy errors)
	 * 
	 * @param id
	 *            <code>Long</code> with the database key value
	 * @return {@link SynchProcess} with the given data, or <code>null</code> if
	 *         no such record exists
	 * @throws TransferDAOException
	 */
	SynchProcess findInitializedById(Long id) throws TransferDAOException;

}
