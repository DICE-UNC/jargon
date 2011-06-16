package org.irods.jargon.datautils.accountcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: this is a first prototype implementation and should not be treated as
 * stable.
 * 
 * Service to provide an account cache. This allows information to be serialized
 * by a key and stored as an iRODS file in an encrypted format, and later
 * retrieved.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AccountCacheServiceImpl {
	
	String xform = "DES/ECB/PKCS5Padding";

	public static final Logger log = LoggerFactory
			.getLogger(AccountCacheServiceImpl.class);

	/**
	 * Factory to create necessary Jargon access objects, which interact with
	 * the iRODS server
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * Describes iRODS server and account information
	 */
	private IRODSAccount irodsAccount;
	
	/**
	 * Configuration controls behavior of the cache.  This can be set, or can just use the defaults, which
	 * cache in the users home dir and do cleanups as part of request processing.
	 */
	private CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();

	// need option for using above account, which could be a proxy, or using a
	// specific method that serializes irods account and uses that info

	/**
	 * Put the given object into a special place in the users home directory.
	 * Note that this method uses the
	 * 
	 * @param informationObject <code>Object</code> that will be serialized and stored in encrypted form in the appropriate cache directory
	 * @param key <code>String</code> with the key that will be used to encrypt, and store the data
	 * @return <code>String</code> with the absolute path to the cache file
	 * @throws JargonException
	 */
	public String putInformationIntoCache(final Object informationObject,
			final String key) throws JargonException {

		if (informationObject == null) {
			throw new IllegalArgumentException("null informationObject");
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}

		checkContracts();
		log.info("putInformationIntoCache()");
		
		// clean up old files? (or in a sep thread?) make an option based on
		// created date and window
		
		log.info("checking on purge of old requests...");
		
		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}
		
		int keyHash = key.hashCode();
		log.info("generated hash for key:{}", keyHash);
		
		// serialize and encrypt object
		log.info("serializing object to byte buffer...");
		byte[] serializedObject = serializeObjectToByteStream(informationObject, key);
		log.info("object serialized into:{} bytes", serializedObject.length);
		
		log.info("encrypting...");
		
		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		byte[] encrypted = cacheEncryptor.encrypt(serializedObject);
		log.info("bytes now encrypted for length:{}", encrypted.length);
		// store in file
		
		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash, irodsAccount.getUserName());
		log.info("storing to file at absolute path: {}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsFileAbsolutePath);
		try {
			cacheFile.createNewFile();
		} catch (IOException e) {
			throw new JargonException("error creating new cache file");
		}
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2StreamAO.streamBytesToIRODSFile(encrypted, cacheFile);
		
		log.info("done...");
		return irodsFileAbsolutePath;

	}
	
	/**
	 * Given a user name and key, retrieve the iRODS file that contains a serialize object.  The file is located based
	 * on the parameters, and the settings in the {@link CacheServiceConfiguration}.
	 * @param userName <code>String</code> with the user name
	 * @param key <code>String</code> with the key that was used to encrypt the object
	 * @return <code>Object</code> that was serialized
	 * @throws JargonException
	 */
	public Object retrieveObjectFromCache(final String userName, final String key) throws JargonException {
		
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}
		
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null userName");
		}
		
		log.info("retrieveObjectFromCache() user name:{}",userName);
		log.info("key", key);
		
		checkContracts();
		
		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}
		
		// build hash of key and look for file
		int keyHash = key.hashCode();
		log.info("generated hash for key:{}", keyHash);
		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash, irodsAccount.getUserName());
		log.info("looking for cache file at path:{}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsFileAbsolutePath);
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		byte[] fileBytes = stream2StreamAO.streamFileToByte(cacheFile);
		log.info("decrypting data based on provided key....");
		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		fileBytes = cacheEncryptor.decrypt(fileBytes);
		
		log.info("streamed file into bytes for length of: {}", fileBytes.length);
		log.info("deserialzing...");
		return deserializeStreamToObject(fileBytes, key);
		
	}

	/**
	 * Based on the configuration, come up with an absolute path to the file name for the cache file
	 * @param keyHash
	 * @param userName
	 * @return
	 */
	private String buildIRODSFileAbsolutePath(int keyHash, String userName) {
		StringBuilder sb = new StringBuilder();
		if (cacheServiceConfiguration.isCacheInHomeDir()) {
			log.info("building home dir cache file");
			sb.append("/");
			sb.append(irodsAccount.getZone());
			sb.append("/home/");
			sb.append(userName);
			sb.append("/");
			sb.append(cacheServiceConfiguration.getCacheDirPath());
		} else {
			log.debug("building cache based on config provided absolute path");
			sb.append(cacheServiceConfiguration.getCacheDirPath());
		}
		
		sb.append("/");
		sb.append(userName);
		sb.append("-");
		sb.append(keyHash);
		sb.append(".dat");
		
		return sb.toString();
		
	}

	private byte[] serializeObjectToByteStream(
			final Object informationObject, final String key) throws JargonException {
		
		log.info("serialzeObjectToByteStream(");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			log.info("creating output stream and writing object...");
			out = new ObjectOutputStream(bos);
			out.writeObject(informationObject);
			log.info("object written...");
		} catch (IOException e) {
			log.error("error serializing object:{}", informationObject, e);
			throw new JargonException("error serializing object", e);
		}   
		return bos.toByteArray(); 
	}
	
	private Object deserializeStreamToObject(
			final byte[] objectBuffer, final String key) throws JargonException {
		
		log.info("deserializeStreamToObject(");
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(objectBuffer);

		ObjectInput oi;
		try {
			oi = new ObjectInputStream(byteInputStream);
			Object obj = oi.readObject();
			return obj;
		} catch (Exception e) {
			log.error("error deserializing object", e);
			throw new JargonException("error deserializing object", e);
		} 
	}

	/**
	 * Purge accounts (per the configured {@link CacheServiceConfiguration}
	 * @throws JargonException
	 */
	public void purgeOldRequests() throws JargonException {
		
		
	}

	/**
	 * Check for correct dependencies
	 */
	private void checkContracts() throws JargonRuntimeException {
		if (irodsAccessObjectFactory == null) {
			throw new JargonRuntimeException("missing irodsAccessObjectFactory");
		}
		
		if (irodsAccount == null) {
			throw new JargonRuntimeException("irodsAccount is null");
		}
		
		if (cacheServiceConfiguration == null) {
			throw new JargonRuntimeException("null cacheServiceConfiguration");
		}
		
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @param cacheServiceConfiguration the cacheServiceConfiguration to set
	 */
	public void setCacheServiceConfiguration(CacheServiceConfiguration cacheServiceConfiguration) {
		this.cacheServiceConfiguration = cacheServiceConfiguration;
	}

	/**
	 * @return the cacheServiceConfiguration
	 */
	public CacheServiceConfiguration getCacheServiceConfiguration() {
		return cacheServiceConfiguration;
	}

}
