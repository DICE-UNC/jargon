/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.KeyStore;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;

/**
 * Manager of <code>GridAccount</code> which represents the cache of identitiies
 * used in the system.
 * <p/>
 * The underlying implemmentation should properly synchronize access to the data, and furthermore, should
 * properly maintain locks on the underlying transfer queue so as not to process updates that could effect a
 * running transfer.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface GridAccountService {

	/**
	 * Return the pass phrase used to encrypt/decrypt the password information
	 * cached in the transfer database
	 * 
	 * @return
	 */
	String getCachedPassPhrase();

	/**
	 * Given a pass phrase (presented as clear next, not a hash), update all
	 * stored grid accounts to reflect the new pass phrase.
	 * <p/>
	 * This method only works if the original pass phrase is first validated,
	 * calling the <code>validatePassPhrase()</code> method of this class
	 * 
	 * @param passPhrase
	 * @return
	 * @throws PassPhraseinvalidException
	 *             if the existing pass phrase was not properly validated before
	 *             setting a new one.
	 * @throws ConveyorExecutionException
	 */
	KeyStore storePassPhrase(String passPhrase)
			throws PassPhraseInvalidException, ConveyorExecutionException;

	/**
	 * Given an <code>IRODSAccount</code>, add a new <code>GridAccount</code>,
	 * or update the underlying <code>GridAccount</code> with the information
	 * from the <code>IRODSAccount</code>. (default storage resource, default
	 * path, password).
	 * <p/>
	 * Note that the grid account is unique by host, zone, and user name.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that will be used to create or update the
	 *            <code>GridAccount</code>
	 * @return {@link GridAccount} that is equivalent to the
	 *         <code>IRODSAccount</code>. Note that the password in the returned
	 *         <code>GridAccount</code> is encrypted by the pass phrase set in
	 *         the <code>TransferManager</code>
	 * @throws PassPhraseInvalidException
	 * @throws ConveyorExecutionException
	 */
	GridAccount addOrUpdateGridAccountBasedOnIRODSAccount(
			IRODSAccount irodsAccount) throws PassPhraseInvalidException,
			ConveyorExecutionException;

	/**
	 * Compare a given pass phrase's hash value with the previously stored
	 * value. If they match, then the pass phrase is validated and cached. This
	 * allows <code>GridAccount</code> information containing stored passwords
	 * to be decrypted.
	 * 
	 * @param passPhrase
	 *            <code>String</code> with the pass phrase to be validated, in
	 *            clear text
	 * @throws PassPhraseInvalidException
	 *             if the pass phrase is not validated
	 * @throws ConveyorExecutionException
	 */
	void validatePassPhrase(String passPhrase)
			throws PassPhraseInvalidException, ConveyorExecutionException;

	/**
	 * Delete the given <code>GridAccount</code>, including all child transfers and synchronizations
	 * @param gridAccount {@link GridAccount} to delete
	 * @throws ConveyorExecutionException
	 */
	void deleteGridAccount(GridAccount gridAccount)
			throws ConveyorExecutionException;

	/**
	 * Find the <code>GridAccount</code> corresponding to the given iRODS account
	 * @param irodsAccount {@link IRODSAccount}
	 * @return {@link GridAccount} that corresponds to the iRODS account, or <code>null</code> if no result is available
	 * @throws ConveyorExecutionException
	 */
	GridAccount findGridAccountByIRODSAccount(IRODSAccount irodsAccount)
			throws ConveyorExecutionException;

	/**
	 * Return a list of grid accounts in host/zone/userName order
	 * @return <code>List</code> of {@link GridAccount} 
	 * @throws ConveyorExecutionException
	 */
	List<GridAccount> findAll() throws ConveyorExecutionException;

}
