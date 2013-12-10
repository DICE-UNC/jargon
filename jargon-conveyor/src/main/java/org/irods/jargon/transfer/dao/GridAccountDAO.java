package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.GridAccount;

/**
 * DAO interface for <code>GridAccount</code> managing a cache of iRODS accounts
 * and related configuration.
 * <p/>
 * The <code>GridAccount</code> preserves account information for transfers and
 * synchs, and also allows preserving and automatically logging in to remembered
 * grids. Note that this uses a scheme of encrypted passwords based on a global
 * 'pass phrase' which must be provided for the various operations. In this way,
 * passwords are always encrypted for all operations.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface GridAccountDAO {

	/**
	 * Save the given <code>GridAccount</code> to the database
	 * 
	 * @param gridAccount
	 *            {@link GridAccount} with login information
	 * @throws TransferDAOException
	 */
	void save(GridAccount gridAccount) throws TransferDAOException;

	/**
	 * List all grid accounts
	 * 
	 * @return <code>List</code> of {@link GridAccount} in the database
	 * @throws TransferDAOException
	 */
	List<GridAccount> findAll() throws TransferDAOException;

	/**
	 * Find a <code>GridAccount</code> based on its primary key (id)
	 * 
	 * @param id
	 *            <code>Long</code> with the primary key
	 * @return {@link GridAccount} or <code>null</code> if record not found
	 * @throws TransferDAOException
	 */
	GridAccount findById(Long id) throws TransferDAOException;

	/**
	 * Find the unique <code>GridAccount</code> based on the unique
	 * host/zone/user
	 * 
	 * @param host
	 *            <code>String</code> with the host name
	 * @param zone
	 *            <code>String</code> with the zone name
	 * @param userName
	 *            <code>String</code> with the user name
	 * @return {@link GridAccount} or <code>null</code> if not found
	 * @throws TransferDAOException
	 */
	GridAccount findByHostZoneAndUserName(String host, String zone,
			String userName) throws TransferDAOException;

	/**
	 * Delete the given grid account
	 * 
	 * @param gridAccount
	 *            {@link GridAccount} that will be deleted
	 * @throws TransferDAOException
	 */
	void delete(GridAccount gridAccount) throws TransferDAOException;

	/**
	 * Delete all grid accounts in the database
	 * 
	 * @throws TransferDAOException
	 */
	void deleteAll() throws TransferDAOException;

	/**
	 * Delete the given <code>GridAccount</code> and all related information
	 * from the grid account data.
	 * 
	 * @param gridAccount
	 *            {@link GridAccount} to be deleted
	 * @throws TransferDAOException
	 */
	void deleteGridAccount(GridAccount gridAccount) throws TransferDAOException;

}