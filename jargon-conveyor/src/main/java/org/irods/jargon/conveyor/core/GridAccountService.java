/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.KeyStore;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;

/**
 * Manager of <code>GridAccount</code> which represents the cache of identities
 * used in the system.
 * <p/>
 * The underlying implementation should properly synchronize access to the data,
 * and furthermore, should properly maintain locks on the underlying transfer
 * queue so as not to process updates that could effect a running transfer.
 * <p/>
 * In order to protect currently running transfers, this locking is done by
 * checking the running status of the queue, and throwing a
 * <code>ConveyorBusy</code> exception if the queue is currently not in an idle
 * or paused state. Clients should check for these exceptions and forward advice
 * to users to retry the operation when the queue is not busy.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface GridAccountService {

	/**
	 * Return the pass phrase used to encrypt/decrypt the password information
	 * cached in the transfer database. This is the clear text password rather
	 * than the hashed value stored in the database key store.
	 * 
	 * @return <code>String</code> with the clear text pass phrase for the grid
	 *         account cache information
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
	 * @throws ConveyorBusyException
	 *             if the operation cannot currently be done, with the queue
	 *             data currently processing. The operation may be retried after
	 *             the queue is paused or idle
	 * @throws PassPhraseinvalidException
	 *             if the existing pass phrase was not properly validated before
	 *             setting a new one.
	 * @throws ConveyorExecutionException
	 */
	KeyStore changePassPhraseWhenAlreadyValidated(String passPhrase)
			throws ConveyorBusyException, PassPhraseInvalidException,
			ConveyorExecutionException;

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
	 * @throws ConveyorBusyException
	 *             if the operation cannot currently be done, with the queue
	 *             data currently processing. The operation may be retried after
	 *             the queue is paused or idle
	 * @throws PassPhraseInvalidException
	 *             if the pass phrase is not validated
	 * @throws ConveyorExecutionException
	 */
	void validatePassPhrase(String passPhrase) throws ConveyorBusyException,
			PassPhraseInvalidException, ConveyorExecutionException;

	/**
	 * Delete the given <code>GridAccount</code>, including all child transfers
	 * and synchronizations
	 * 
	 * @param gridAccount
	 *            {@link GridAccount} to delete
	 * @throws ConveyorBusyException
	 *             if the operation cannot currently be done, with the queue
	 *             data currently processing. The operation may be retried after
	 *             the queue is paused or idle
	 * @throws ConveyorExecutionException
	 */
	void deleteGridAccount(GridAccount gridAccount)
			throws ConveyorBusyException, ConveyorExecutionException;

	/**
	 * Find the <code>GridAccount</code> corresponding to the given iRODS
	 * account
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return {@link GridAccount} that corresponds to the iRODS account, or
	 *         <code>null</code> if no result is available
	 * @throws ConveyorExecutionException
	 */
	GridAccount findGridAccountByIRODSAccount(IRODSAccount irodsAccount)
			throws ConveyorExecutionException;

	/**
	 * Return a list of grid accounts in host/zone/userName order
	 * 
	 * @return <code>List</code> of {@link GridAccount}
	 * @throws ConveyorExecutionException
	 */
	List<GridAccount> findAll() throws ConveyorExecutionException;

	/**
	 * Given a <code>GridAccount</code> return the corresponding iRODS account
	 * with the password decrypted
	 * 
	 * @param gridAccount
	 *            {@link GridAccount} containing cached account info
	 * @return {@link IRODSAccount} based on the <code>GridAccount</code>
	 * @throws ConveyorExecutionException
	 */
	IRODSAccount irodsAccountForGridAccount(final GridAccount gridAccount)
			throws ConveyorExecutionException;

	/**
	 * Purge all grid accounts and related information from the store
	 * 
	 * @throws ConveyorBusyException
	 *             if the operation cannot currently be done, with the queue
	 *             data currently processing. The operation may be retried after
	 *             the queue is paused or idle
	 * @throws ConveyorExecutionException
	 */
	void deleteAllGridAccounts() throws ConveyorBusyException,
			ConveyorExecutionException;

	/**
	 * Get a reference to the conveyor executor service that actually runs the
	 * underlying transfer operations
	 * 
	 * @return
	 */
	ConveyorExecutorService getConveyorExecutorService();

	/**
	 * Get rid of all accounts, and clear the pass phrase. This allows a client
	 * using this library to 'forget' the key and reset the entire application
	 * 
	 * @throws ConveyorBusyException
	 *             if the operation cannot currently be done, with the queue
	 *             data currently processing. The operation may be retried after
	 *             the queue is paused or idle
	 * @throws ConveyorExecutionException
	 */
	void resetPassPhraseAndAccounts() throws ConveyorBusyException,
			ConveyorExecutionException;

	/**
	 * Checks if a pass phrase has been stored.
	 * 
	 * @return <code>boolean</code> of <code>true</code> if the pass phrase has
	 *         been stored
	 * @throws ConveyorExecutionException
	 */
	boolean isPassPhraseStoredAlready() throws ConveyorExecutionException;

	/**
	 * Associate the choice for default storage resource with the grid account
	 * 
	 * @param resourceName
	 *            <code>String</code> with a valid resource name, or blank
	 * @param irodsAccount
	 *            {@link IRODSAccount} to save
	 * @throws ConveyorExecutionException
	 */
	void rememberDefaultStorageResource(String resourceName,
			IRODSAccount irodsAccount) throws ConveyorExecutionException;

	/**
	 * Update the stored grid account, including optional
	 * <code>AuthResponse</code> data from an authentication. This allows
	 * caching of a secondary runAs identity. This is useful in authentication
	 * scenarios where a temporary account may be generated upon login and used,
	 * such as during PAM authentication.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} to save
	 * @param authResponse
	 *            {@link AuthResponse} to save, this may be left
	 *            <code>null</code>, and if supplied will be cached in the grid
	 *            account information
	 * @return {@link GridAccount} that corresponds to the iRODS account
	 * @throws PassPhraseInvalidException
	 * @throws ConveyorExecutionException
	 */
	GridAccount addOrUpdateGridAccountBasedOnIRODSAccount(
			final IRODSAccount irodsAccount, final AuthResponse authResponse)
			throws PassPhraseInvalidException, ConveyorExecutionException;

}
