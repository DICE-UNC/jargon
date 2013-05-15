/**
 * 
 */
package org.irods.jargon.transfer.dao;

import org.irods.jargon.transfer.dao.domain.KeyStore;

/**
 * DAO for <code>KeyStore</code> managing the stored 'pass phrase' for the
 * transfer database.
 * <p/>
 * The <code>GridAccount</code> preserves account information for transfers and
 * synchs, and also allows preserving and automatically logging in to remembered
 * grids. Note that this uses a scheme of encrypted passwords based on a global
 * 'pass phrase' which must be provided for the various operations. In this way,
 * passwords are always encrypted for all operations.
 * <p/>
 * This <code>KeyStore</code> holds a hash of the pass phrase used by the
 * transfer manager user, and can verify the correct pass phrase. Note the
 * actual pass phrase, and any unencrypted password information, is not found in
 * the transfer database.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface KeyStoreDAO {

	/**
	 * Save the <code>KeyStore</code> entry in the transfer database
	 * 
	 * @param keyStore
	 *            {@link KeyStore} entry containing the hash of the pass phrase
	 * @throws TransferDAOException
	 */
	void save(KeyStore keyStore) throws TransferDAOException;

	/**
	 * Find the <code>KeyStore</code> associated with the given key. Note that
	 * <code>null</code> will be returned if it cannot be found.
	 * 
	 * @param id
	 *            <code>String</code> with the desired key
	 * @return {@link KeyStore} associated with the key
	 * @throws TransferDAOException
	 *             if the record cannot be found
	 */
	KeyStore findById(String id) throws TransferDAOException;

	/**
	 * Delete the given <code>KeyStore</code>
	 * 
	 * @param keyStore
	 *            {@link KeyStore} to be deleted
	 * @throws TransferDAOException
	 */
	void delete(KeyStore keyStore) throws TransferDAOException;

}
