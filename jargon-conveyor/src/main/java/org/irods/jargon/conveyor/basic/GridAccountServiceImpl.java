/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorBusyException;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.datautils.datacache.CacheEncryptor;
import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.irods.jargon.transfer.dao.KeyStoreDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.KeyStore;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the underlying grid accounts (user identity) for the transfer manager
 * <p/>
 * Note that methods that rely on encryption of the password based on the cached
 * pass phrase are synchronized. In addition, any methods that could potentially
 * impact a running transfer (e.g. changing the password on an account that may
 * be referenced by a running transfer) are guarded by a lock on the transfer
 * execution queue. Such operations will return a
 * <code>ConveyorBusyException</code> as noted in the method signatures when
 * operations cannot be completed due to queue status. These operations may be
 * retried when the queue is idle.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional(rollbackFor = { ConveyorExecutionException.class })
public class GridAccountServiceImpl extends AbstractConveyorComponentService
		implements GridAccountService {

	/**
	 * Injected dependency on {@link KeyStoreDAO}
	 */
	private KeyStoreDAO keyStoreDAO;

	/**
	 * Stored pass phrase, this is accessed in a thread-safe manner.
	 */
	private String cachedPassPhrase = "";

	private CacheEncryptor cacheEncryptor = null;

	private static final Logger log = LoggerFactory
			.getLogger(GridAccountServiceImpl.class);

	/**
	 * Injected dependency on {@link GridAccountDAO}
	 */
	private GridAccountDAO gridAccountDAO;

	public GridAccountDAO getGridAccountDAO() {
		return gridAccountDAO;
	}

	public void setGridAccountDAO(final GridAccountDAO gridAccountDAO) {
		this.gridAccountDAO = gridAccountDAO;
	}

	public KeyStoreDAO getKeyStoreDAO() {
		return keyStoreDAO;
	}

	public void setKeyStoreDAO(final KeyStoreDAO keyStoreDAO) {
		this.keyStoreDAO = keyStoreDAO;
	}

	/**
	 * 
	 */
	public GridAccountServiceImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#storePassPhrase(java
	 * .lang.String)
	 */
	@Override
	public KeyStore changePassPhraseWhenAlreadyValidated(final String passPhrase)
			throws ConveyorBusyException, PassPhraseInvalidException,
			ConveyorExecutionException {

		log.info("storePassPhrase()");
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null passPhrase");
		}

		synchronized (this) {

			if (!isValidated()) {
				throw new PassPhraseInvalidException(
						"The current pass phrase is not validated, cannot replace");
			}

			try {
				getConveyorExecutorService().setBusyForAnOperation();
				return replacePassPhrase(passPhrase);
			} finally {
				getConveyorExecutorService().setOperationCompleted();
			}
		}

	}

	@Override
	public void rememberDefaultStorageResource(final String resourceName,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException {

		log.info("rememberDefaultStorageResource()");

		if (resourceName == null) {
			throw new IllegalArgumentException("null resourceName");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("resourceName:{}", resourceName);
		log.info("irodsAccount:{}", irodsAccount);

		GridAccount gridAccount = findGridAccountByIRODSAccount(irodsAccount);

		if (gridAccount == null) {
			log.error("cannot find grid account for irodsAccount:{}",
					irodsAccount);
			throw new ConveyorExecutionException(
					"cannot find grid account for iRODS account");
		}

		gridAccount.setDefaultResource(resourceName);
		log.info("default resource name is set");
	}

	/**
	 * Replace the current pass phrase with a new one, including resetting all
	 * stored grid accounts to the new phrase. Note that the queue should be
	 * locked (set to busy) before calling this operation, and unlocked
	 * afterwards.
	 * 
	 * @param passPhrase
	 * @return
	 * @throws ConveyorExecutionException
	 * @throws PassPhraseInvalidException
	 */
	private KeyStore replacePassPhrase(final String passPhrase)
			throws ConveyorExecutionException, PassPhraseInvalidException {
		log.info("looking up keyStore..");

		log.info("storePassPhrase()");
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null passPhrase");
		}

		String oldPassPhrase = getCachedPassPhrase();

		KeyStore keyStore = storeGivenPassPhraseInKeyStoreAndSetAsCached(passPhrase);

		log.info("refreshing cacheEncryptor with the new pass phrase...");
		cacheEncryptor = new CacheEncryptor(getCachedPassPhrase());

		log.info("updating stored grid accounts with new pass phrase...");
		updateStoredGridAccountsForNewPassPhrase(oldPassPhrase, passPhrase);
		log.info("stored grid accounts updated");

		return keyStore;
	}

	/**
	 * Make the given pass phrase the current one, and
	 * 
	 * @param passPhrase
	 * @return
	 * @throws ConveyorExecutionException
	 * @throws PassPhraseInvalidException
	 */
	private KeyStore storeGivenPassPhraseInKeyStoreAndSetAsCached(
			final String passPhrase) throws ConveyorExecutionException,
			PassPhraseInvalidException {
		log.info("looking up keyStore..");

		KeyStore keyStore;
		try {
			keyStore = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		} catch (TransferDAOException e) {
			log.error("unable to look up prior key store", e);
			throw new ConveyorExecutionException(
					"error looking up prior key store", e);
		}

		String hashOfPassPhrase;
		try {
			hashOfPassPhrase = MiscIRODSUtils
					.computeMD5HashOfAStringValue(passPhrase);
		} catch (JargonException e) {
			log.error("unable to create hash of pass phrase", e);
			throw new ConveyorExecutionException(
					"error creating hash of pass phrase to store", e);
		}

		log.info("update or add the KeyStore");
		if (keyStore == null) {
			log.debug("no keyStore found, create a new one");
			keyStore = new KeyStore();
			keyStore.setId(KeyStore.KEY_STORE_PASS_PHRASE);
			keyStore.setValue(hashOfPassPhrase);
		} else {
			// keystore already present, see if I had validated first
			if (!isValidated()) {
				log.error("cannot store, the pass phrase was never validated");
				throw new PassPhraseInvalidException(
						"cannot store pass phrase, need to validate first");
			}
			log.info("updating key store with new pass phrase");
			keyStore.setValue(hashOfPassPhrase);
		}

		try {
			keyStoreDAO.save(keyStore);
		} catch (TransferDAOException e) {
			log.error("unable to look up prior key store", e);
			throw new ConveyorExecutionException(
					"error looking up prior key store", e);
		}

		log.info("key store saved");
		log.info("new key store, consider it validated and cache it");
		cachedPassPhrase = passPhrase;
		return keyStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.GridAccountService#
	 * addOrUpdateGridAccountBasedOnIRODSAccount
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public GridAccount addOrUpdateGridAccountBasedOnIRODSAccount(
			final IRODSAccount irodsAccount) throws PassPhraseInvalidException,
			ConveyorExecutionException {

		log.info("addOrUpdateGridAccountBasedOnIRODSAccount");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("irodsAccount:{}", irodsAccount);

		if (!isValidated()) {
			throw new ConveyorExecutionException(
					"pass phrase has not been validated");
		}

		log.info("checking if the grid account exists");
		GridAccount gridAccount = null;
		try {
			gridAccount = gridAccountDAO.findByHostZoneAndUserName(
					irodsAccount.getHost(), irodsAccount.getZone(),
					irodsAccount.getUserName());
		} catch (TransferDAOException e) {
			log.error("exception accessing grid account data", e);
			throw new ConveyorExecutionException("error getting grid account",
					e);
		}

		if (cacheEncryptor == null) {
			cacheEncryptor = new CacheEncryptor(getCachedPassPhrase());
		}

		if (gridAccount == null) {
			log.info("no grid account, create a new one");
			log.info("creating grid account and enrypting password");
			gridAccount = new GridAccount(irodsAccount);
		}

		try {
			gridAccount.setPassword(new String(cacheEncryptor
					.encrypt(irodsAccount.getPassword())));
			gridAccount.setDefaultResource(irodsAccount
					.getDefaultStorageResource());
			gridAccount.setDefaultPath(irodsAccount.getHomeDirectory());
			gridAccount.setAuthScheme(irodsAccount.getAuthenticationScheme());
			gridAccount.setPort(irodsAccount.getPort());
		} catch (JargonException e) {
			log.error("error encrypting password with pass phrase", e);
			throw new ConveyorExecutionException(e);
		}

		try {
			gridAccountDAO.save(gridAccount);
		} catch (TransferDAOException e) {
			log.error("error saving grid account:{}", gridAccount, e);
			throw new ConveyorExecutionException("error saving grid account", e);
		}

		log.info("grid account saved:{}", gridAccount);
		return gridAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#validatePassPhrase(
	 * java.lang.String)
	 */
	@Override
	public void validatePassPhrase(final String passPhrase)
			throws ConveyorBusyException, PassPhraseInvalidException,
			ConveyorExecutionException {

		log.info("validatePassPhrase()");

		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null or empty passPhrase");
		}

		String hashOfPassPhrase;
		try {
			hashOfPassPhrase = MiscIRODSUtils
					.computeMD5HashOfAStringValue(passPhrase);
		} catch (JargonException e) {
			log.error("error computing hash of supplied passPhrase", e);
			throw new ConveyorExecutionException("error computing pass phrase",
					e);
		}
		log.info("hash of supplied pass phrase:{}", hashOfPassPhrase);

		/*
		 * look up the existing pass phrase, if no pass phrase is found, the new
		 * pass phrase is used and all accounts are cleared. You should not have
		 * accounts stored and no key store, but why not just gracefully handle
		 * a weird situation.
		 * 
		 * This method requires the queue to be idle and will set it to busy
		 * while the operation completes. This may be overkill, but seems neater
		 * conceptually.
		 */

		synchronized (this) {
			try {
				getConveyorExecutorService().setBusyForAnOperation();
				log.info("looking up existing pass phrase");
				KeyStore keyStore = keyStoreDAO
						.findById(KeyStore.KEY_STORE_PASS_PHRASE);
				if (keyStore == null) {
					log.info("no keystore found, save the pass phrase");
					keyStore = storeGivenPassPhraseInKeyStoreAndSetAsCached(passPhrase);
				} else {
					log.info("keyStore found...");
					if (!keyStore.getValue().equals(hashOfPassPhrase)) {
						log.error("pass phrase is invalid");
						cachedPassPhrase = "";
						throw new PassPhraseInvalidException(
								"invalid pass phrase");
					} else {
						cachedPassPhrase = passPhrase;
					}
				}

				log.info("refreshing cacheEncryptor with the new pass phrase...");
				cacheEncryptor = new CacheEncryptor(getCachedPassPhrase());

			} catch (TransferDAOException e) {
				log.error("error finding pass phrase in key store", e);
				throw new ConveyorExecutionException(
						"unable to find pass phrase in key store");
			} finally {
				getConveyorExecutorService().setOperationCompleted();
			}
		}

	}

	/**
	 * This method will take a changed pass phrase and re-encrypt the stored
	 * password information
	 * 
	 * @param previousPassPhrase
	 * @param passPhrase
	 * @throws ConveyorExecutionException
	 */
	private void updateStoredGridAccountsForNewPassPhrase(
			final String previousPassPhrase, final String passPhrase)
			throws ConveyorExecutionException {
		log.info("updateStoredGridAccountsForNewPassPhrase");
		try {
			List<GridAccount> gridAccounts = gridAccountDAO.findAll();

			if (!gridAccounts.isEmpty()) {
				if (previousPassPhrase == null || previousPassPhrase.isEmpty()) {
					throw new ConveyorExecutionException(
							"no cached pass phrase, and accounts already exist");
				}
			}

			/*
			 * Create an deryptor using the 'old' cached pass phrase, the stored
			 * passwords are decrypted and then re-enrypted using the new pass
			 * phrase.
			 * 
			 * Note that the instance level cacheEncryptor should be set to the
			 * new pass phrase before this method is called.
			 * 
			 * The methods in this class are synchronized, so we shouldn't have
			 * issues with multiple simultaneous operations on the cache
			 * encryptor or stored pass phrase.
			 */
			CacheEncryptor decryptingCacheEncryptor = new CacheEncryptor(
					previousPassPhrase);

			for (GridAccount gridAccount : gridAccounts) {
				log.info("updating:{}", gridAccount);
				String unencryptedPassword = decryptingCacheEncryptor
						.decrypt(gridAccount.getPassword());
				gridAccount.setPassword(cacheEncryptor
						.encrypt(unencryptedPassword));
				gridAccountDAO.save(gridAccount);
				log.info("password re-encrypted and saved");
			}

		} catch (TransferDAOException e) {
			log.error("error updating stored grid accounts with pass phrase", e);
			throw new ConveyorExecutionException(
					"error updated stored accounts", e);
		} catch (JargonException e) {
			log.error("error updating stored grid accounts with pass phrase", e);
			throw new ConveyorExecutionException(
					"error updated stored accounts", e);
		}

	}

	/**
	 * Basic check that determines if the pass phrase used to encrypt cached
	 * passwords is saved
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if the pass
	 *         phrase is validated
	 */
	private synchronized boolean isValidated() {
		if (cachedPassPhrase == null || cachedPassPhrase.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return the cachedPassPhrase which is a <code>String</code> that is used
	 *         to encrypt the passwords stored in the transfer queue.
	 */
	@Override
	public synchronized String getCachedPassPhrase() {
		return cachedPassPhrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.GridAccountService#
	 * findGridAccountByIRODSAccount
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public GridAccount findGridAccountByIRODSAccount(
			final IRODSAccount irodsAccount) throws ConveyorExecutionException {
		log.info("findGridAccountByIRODSAccount()");
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		log.info("irodsAccount:{}", irodsAccount);

		if (!isValidated()) {
			throw new ConveyorExecutionException(
					"pass phrase has not been validated");
		}

		try {
			return gridAccountDAO.findByHostZoneAndUserName(
					irodsAccount.getHost(), irodsAccount.getZone(),
					irodsAccount.getUserName());
		} catch (TransferDAOException e) {
			log.error("exception finding", e);
			throw new ConveyorExecutionException("error finding account", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#deleteGridAccount(org
	 * .irods.jargon.transfer.dao.domain.GridAccount)
	 */
	@Override
	public void deleteGridAccount(final GridAccount gridAccount)
			throws ConveyorBusyException, ConveyorExecutionException {
		log.info("deleteGridAccount()");

		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		log.info("gridAccount:{}", gridAccount);

		// lock queue so as not to delete an account involved with a running
		// transfer, deleting the account would delete the transfer data
		try {
			getConveyorExecutorService().setBusyForAnOperation();
			gridAccountDAO.delete(gridAccount);
		} catch (TransferDAOException e) {
			log.error("exception deleting", e);
			throw new ConveyorExecutionException("error deleting account", e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.GridAccountService#findAll()
	 */
	@Override
	public List<GridAccount> findAll() throws ConveyorExecutionException {
		log.info("findAll()");
		try {
			return gridAccountDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("exception deleting", e);
			throw new ConveyorExecutionException("error deleting account", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#irodsAccountForGridAccount
	 * (org.irods.jargon.transfer.dao.domain.GridAccount)
	 */
	@Override
	public synchronized IRODSAccount irodsAccountForGridAccount(
			final GridAccount gridAccount) throws ConveyorExecutionException {

		/*
		 * This method is synchronized as it depends on the cache encryptor
		 */

		log.info("irodsAccountForGridAccount()");
		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		if (!isValidated()) {
			throw new ConveyorExecutionException(
					"pass phrase has not been validated");
		}

		String decryptedPassword;
		try {
			decryptedPassword = cacheEncryptor.decrypt(gridAccount
					.getPassword());
			return IRODSAccount.instance(gridAccount.getHost(),
					gridAccount.getPort(), gridAccount.getUserName(),
					decryptedPassword, gridAccount.getDefaultPath(),
					gridAccount.getZone(), gridAccount.getDefaultResource(),
					gridAccount.getAuthScheme());
		} catch (JargonException e) {
			log.error("exception deleting", e);
			throw new ConveyorExecutionException("error decrypting account", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#deleteAllGridAccounts()
	 */
	@Override
	public void deleteAllGridAccounts() throws ConveyorBusyException,
			ConveyorExecutionException {
		log.info("deleteAllGridAccounts()");

		try {
			getConveyorExecutorService().setBusyForAnOperation();
			gridAccountDAO.deleteAll();
		} catch (TransferDAOException e) {
			log.error("exception deleting", e);
			throw new ConveyorExecutionException("error decrypting account", e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#resetPassPhraseAndAccounts
	 * ()
	 */
	@Override
	public synchronized void resetPassPhraseAndAccounts()
			throws ConveyorBusyException, ConveyorExecutionException {
		log.info("resetPassPhraseAndAccounts()");

		/*
		 * This method alters the cached pass phrase and cache encryptor
		 */
		try {
			getConveyorExecutorService().setBusyForAnOperation();
			doDeleteAllWithQueueLocked();
		} catch (TransferDAOException e) {
			log.error("exception resetting key store and accounts", e);
			throw new ConveyorExecutionException("error resetting", e);
		} finally {
			getConveyorExecutorService().setOperationCompleted();
		}

	}

	/**
	 * With the queue already locked, delete all grid accounts
	 * 
	 * @throws TransferDAOException
	 */
	private void doDeleteAllWithQueueLocked() throws TransferDAOException {
		gridAccountDAO.deleteAll();
		KeyStore keyStore = keyStoreDAO
				.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		if (keyStore != null) {
			log.info("deleting keystore entry for pass phrase");
			keyStoreDAO.delete(keyStore);
		}
		// deleted account and other info, clear the pass phrase, it will
		// need to be reset
		cachedPassPhrase = "";
		cacheEncryptor = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.GridAccountService#isPassPhraseStoredAlready
	 * ()
	 */
	@Override
	public boolean isPassPhraseStoredAlready()
			throws ConveyorExecutionException {
		log.info("isPassPhraseStoredAlready");

		KeyStore keyStore;
		try {
			keyStore = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		} catch (TransferDAOException e) {
			log.error("unable to look up prior key store", e);
			throw new ConveyorExecutionException(
					"error looking up prior key store", e);
		}

		if (keyStore == null) {
			log.info("no keystore found, will return false");
		}

		return (keyStore != null);

	}
}
