/**
 * 
 */
package org.irods.jargon.transfer.engine;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.datautils.datacache.CacheEncryptor;
import org.irods.jargon.transfer.TransferEngineException;
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
 * Service to manage the stored account information in the transfer engine. This
 * information allows storage of iRODS accounts for transfers, and for
 * remembering logins.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional
public class GridAccountServiceImpl implements GridAccountService {

	/**
	 * Injected dependency on {@link GridAccountDAO}
	 */
	private GridAccountDAO gridAccountDAO;

	/**
	 * Injected dependency on {@link KeyStoreDAO}
	 */
	private KeyStoreDAO keyStoreDAO;

	/**
	 * Stored pass phrase, this is accessed in a thread-safe manner.
	 */
	private String cachedPassPhrase = "";

	private CacheEncryptor cacheEncryptor = null;

	private final Logger log = LoggerFactory
			.getLogger(GridAccountServiceImpl.class);

	/**
	 * Default constructor. Note that the <code>GridAccountDAO</code> needs to
	 * be injected
	 */
	public GridAccountServiceImpl() {
	}

	/**
	 * @return the gridAccountDAO
	 */

	public GridAccountDAO getGridAccountDAO() {
		return gridAccountDAO;
	}

	/**
	 * @param gridAccountDAO
	 *            the gridAccountDAO to set
	 */
	@Override
	public void setGridAccountDAO(GridAccountDAO gridAccountDAO) {
		this.gridAccountDAO = gridAccountDAO;
	}

	/**
	 * @return the keyStoreDAO
	 */
	@Override
	public KeyStoreDAO getKeyStoreDAO() {
		return keyStoreDAO;
	}

	/**
	 * @param keyStoreDAO
	 *            the keyStoreDAO to set
	 */
	@Override
	public void setKeyStoreDAO(KeyStoreDAO keyStoreDAO) {
		this.keyStoreDAO = keyStoreDAO;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.GridAccountService#storePassPhrase(java
	 * .lang.String)
	 */
	@Override
	public synchronized KeyStore storePassPhrase(final String passPhrase)
			throws PassPhraseInvalidException, TransferEngineException {
		// TODO: add code to update stored grids

		log.info("storePassPhrase()");
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null passPhrase");
		}

		log.info("looking up keyStore..");

		KeyStore keyStore;
		try {
			keyStore = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		} catch (TransferDAOException e) {
			log.error("unable to look up prior key store", e);
			throw new TransferEngineException(
					"error looking up prior key store", e);
		}

		String hashOfPassPhrase;
		try {
			hashOfPassPhrase = MiscIRODSUtils
					.computeMD5HashOfAStringValue(passPhrase);
		} catch (JargonException e) {
			log.error("unable to create hash of pass phrase", e);
			throw new TransferEngineException(
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
			throw new TransferEngineException(
					"error looking up prior key store", e);
		}

		log.info("key store saved");
		log.info("new key store, consider it validated and cache it");
		this.cachedPassPhrase = passPhrase;
		return keyStore;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.GridAccountService#
	 * addOrUpdateGridAccountBasedOnIRODSAccount
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public synchronized GridAccount addOrUpdateGridAccountBasedOnIRODSAccount(
			final IRODSAccount irodsAccount) throws PassPhraseInvalidException,
			TransferEngineException {

		log.info("addOrUpdateGridAccountBasedOnIRODSAccount");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("irodsAccount:{}", irodsAccount);

		if (this.cachedPassPhrase == null || this.cachedPassPhrase.isEmpty()) {
			throw new PassPhraseInvalidException("invalid pass phrase");
		}

		log.info("checking if the grid account exists");
		GridAccount gridAccount = null;
		try {
			gridAccount = gridAccountDAO.findByHostZoneAndUserName(
					irodsAccount.getHost(), irodsAccount.getZone(),
					irodsAccount.getUserName());
		} catch (TransferDAOException e) {
			log.error("exception accessing grid account data", e);
			throw new TransferEngineException("error getting grid account", e);
		}
		
		if (cacheEncryptor == null) {
			cacheEncryptor = new CacheEncryptor(this.getCachedPassPhrase());
		}

		if (gridAccount == null) {
			log.info("no grid account, create a new one");
			log.info("creating grid account and enrypting password");
			gridAccount = new GridAccount(irodsAccount);
		}
		
		try {
			gridAccount.setPassword(new String(cacheEncryptor
					.encrypt(irodsAccount.getPassword())));
		} catch (JargonException e) {
			log.error("error encrypting password with pass phrase", e);
			throw new TransferEngineException(e);
		}

		try {
			gridAccountDAO.save(gridAccount);
		} catch (TransferDAOException e) {
			log.error("error saving grid account:{}", gridAccount, e);
			throw new TransferEngineException("error saving grid account", e);
		}
		log.info("grid account saved:{}", gridAccount);
		return gridAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.GridAccountService#validatePassPhrase
	 * (java.lang.String)
	 */
	@Override
	public synchronized void validatePassPhrase(final String passPhrase)
			throws PassPhraseInvalidException, TransferEngineException {

		log.info("validatePassPhrase()");

		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null or empty passPhrase");
		}

		// look up the existing pass phrase, if no pass phrase is found, this is
		// treated as an exception
		log.info("looking up existing pass phrase");
		KeyStore keyStore;
		try {
			keyStore = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
			log.info("keyStore found...");
		} catch (TransferDAOException e) {
			log.error("error finding pass phrase in key store", e);
			throw new TransferEngineException(
					"unable to find pass phrase in key store");
		}

		/*
		 * If there is no KeyStore value, just use the current pass phrase and
		 * store it in the database
		 */

		if (keyStore == null) {
			log.info("pass phrase is note stored, go ahead and just store it.  The called method will cache it");
			this.storePassPhrase(passPhrase);
			return;
		}

		// have the key store, verify the pass phrase by generating a hash and
		// comparing to stored

		String hashOfPassPhrase;
		try {
			hashOfPassPhrase = MiscIRODSUtils
					.computeMD5HashOfAStringValue(passPhrase);
		} catch (JargonException e) {
			log.error("error computing hash of supplied passPhrase", e);
			throw new TransferEngineException("error computing pass phrase", e);
		}
		log.info("hash of supplied pass phrase:{}", hashOfPassPhrase);
		
		if (!hashOfPassPhrase.equals(keyStore.getValue())) {
			log.error("invalid pass phrase supplied");
			throw new PassPhraseInvalidException("invalid pass phrase supplied");
		}
		
		log.info("pass phrase valid, save it");
		this.cachedPassPhrase = passPhrase;
		
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

	/**
	 * @param cachedPassPhrase
	 *            the cachedPassPhrase to set
	 */
	public synchronized void setCachedPassPhrase(String cachedPassPhrase) {
		this.cachedPassPhrase = cachedPassPhrase;
	}

	public synchronized void purgeRememberedGrids()
			throws TransferEngineException {
		log.info("purgeRememberedGrids()");
	}

}
