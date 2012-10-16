package org.irods.jargon.transfer.dao;

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

}