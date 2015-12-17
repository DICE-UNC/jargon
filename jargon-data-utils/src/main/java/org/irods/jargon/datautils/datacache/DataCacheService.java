package org.irods.jargon.datautils.datacache;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Interface to a service that can encrypt and cache information in temporary
 * iRODS files. This cache can hold serialized objects or strings, or other
 * types of data as defined in the interface. A
 * {@link CacheServiceConfiguration} controls the behavior of the cache.
 *
 * @author mikeconway
 *
 */
public interface DataCacheService {

	/**
	 * Given a <code>String</code> value, and a key value, encrypt the value by
	 * the key, and store the data in the appropriate iRODS file. This will be
	 * determined by the given {@link CacheServiceConfiguration}.
	 *
	 * @param stringToCache
	 *            <code>String</code> with the data to be cached
	 * @param key
	 *            <code>String</code> with the key that will be used to encrypt
	 *            the data
	 * @return <code>String</code> with the absolute path to the iRODS file that
	 *         will hold the encrypted data cache
	 * @throws JargonException
	 */
	String putStringValueIntoCache(final String stringToCache, final String key)
			throws JargonException;

	/**
	 * Put the given object into a special place in the users home directory.
	 * Note that this method uses the
	 *
	 * @param informationObject
	 *            <code>Object</code> that will be serialized and stored in
	 *            encrypted form in the appropriate cache directory
	 * @param key
	 *            <code>String</code> with the key that will be used to encrypt,
	 *            and store the data
	 * @return <code>String</code> with the absolute path to the cache file
	 * @throws JargonException
	 */
	String putSerializedEncryptedObjectIntoCache(
			final Object informationObject, final String key)
					throws JargonException;

	/**
	 * Given a user name and key, retrieve the iRODS file that contains a
	 * serialize object. The file is located based on the parameters, and the
	 * settings in the {@link CacheServiceConfiguration}.
	 *
	 * @param userName
	 *            <code>String</code> with the user name
	 * @param key
	 *            <code>String</code> with the key that was used to encrypt the
	 *            object
	 * @return <code>Object</code> that was serialized
	 * @throws JargonException
	 */
	Object retrieveObjectFromCache(final String userName, final String key)
			throws JargonException;

	/**
	 * Purge accounts (per the configured {@link CacheServiceConfiguration}
	 *
	 * @throws JargonException
	 */
	void purgeOldRequests() throws JargonException;

	/**
	 * @return the irodsAccessObjectFactory
	 */
	IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory);

	/**
	 * @return the irodsAccount
	 */
	IRODSAccount getIrodsAccount();

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	void setIrodsAccount(final IRODSAccount irodsAccount);

	/**
	 * @param cacheServiceConfiguration
	 *            the cacheServiceConfiguration to set
	 */
	void setCacheServiceConfiguration(
			CacheServiceConfiguration cacheServiceConfiguration);

	/**
	 * @return the cacheServiceConfiguration
	 */
	CacheServiceConfiguration getCacheServiceConfiguration();

	/**
	 * For a given user and key, retrieve the value that was stored in the cache
	 * as an encrypted <code>String</code>
	 *
	 * @param userName
	 *            <code>String</code> with the user name for which the data was
	 *            stored
	 * @param key
	 *            <code>String</code> with the key used to encrypt and store the
	 *            data
	 * @return <code>String</code> with the decrypted value from the cache
	 * @throws JargonException
	 */
	String retrieveStringValueFromCache(String userName, String key)
			throws JargonException;

}