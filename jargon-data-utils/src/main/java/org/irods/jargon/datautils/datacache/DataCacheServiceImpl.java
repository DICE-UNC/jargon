package org.irods.jargon.datautils.datacache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
 * Service to provide a secure data cache. This allows information to be serialized
 * by a key and stored as an iRODS file in an encrypted format, and later
 * retrieved.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataCacheServiceImpl implements DataCacheService {

	String xform = "DES/ECB/PKCS5Padding";

	public static final Logger log = LoggerFactory
			.getLogger(DataCacheServiceImpl.class);

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
	 * Configuration controls behavior of the cache. This can be set, or can
	 * just use the defaults, which cache in the users home dir and do cleanups
	 * as part of request processing.
	 */
	private CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#putStringValueIntoCache(java.lang.String, java.lang.String)
	 */
	@Override
	public String putStringValueIntoCache(final String stringToCache,
			final String key) throws JargonException {

		if (stringToCache == null) {
			throw new IllegalArgumentException("null stringToCache");
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}

		checkContracts();
		log.info("putStringValueIntoCache()");

		log.info("checking on purge of old requests...");

		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}

		int keyHash = key.hashCode();
		log.info("generated hash for key:{}", keyHash);
		byte[] stringData = stringToCache.getBytes();
		log.info("encrypting...");

		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		byte[] encrypted = cacheEncryptor.encrypt(stringData);
		log.info("bytes now encrypted for length:{}", encrypted.length);
		// store in file

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash,
				irodsAccount.getUserName());
		log.info("storing to file at absolute path: {}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		createCacheFileAndCacheDir(cacheFile);
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory()
				.getStream2StreamAO(irodsAccount);
		stream2StreamAO.streamBytesToIRODSFile(encrypted, cacheFile);

		log.info("done...");
		return irodsFileAbsolutePath;

	}

	/**
	 * @param cacheFile
	 * @throws JargonException
	 */
	private void createCacheFileAndCacheDir(IRODSFile cacheFile)
			throws JargonException {
		try {
			cacheFile.getParentFile().mkdirs();
			cacheFile.createNewFile();
		} catch (IOException e) {
			throw new JargonException("error creating new cache file");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.DataCacheService#retrieveStringValueFromCache(java.lang.String, java.lang.String)
	 */
	@Override
	public String retrieveStringValueFromCache(final String userName,
			final String key) throws JargonException {

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null userName");
		}

		log.info("retrieveStringValueFromCache() user name:{}", userName);

		checkContracts();

		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}

		// build hash of key and look for file
		int keyHash = key.hashCode();
		log.info("generated hash for key:{}", keyHash);
		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash,
				irodsAccount.getUserName());
		log.info("looking for cache file at path:{}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory()
				.getStream2StreamAO(irodsAccount);
		byte[] fileBytes = stream2StreamAO.streamFileToByte(cacheFile);
		log.info("decrypting data based on provided key....");
		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		fileBytes = cacheEncryptor.decrypt(fileBytes);

		log.info("streamed file into bytes for length of: {}", fileBytes.length);
		log.info("deserialzing...");
		return new String(fileBytes);

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#putSerializedEncryptedObjectIntoCache(java.lang.Object, java.lang.String)
	 */
	@Override
	public String putSerializedEncryptedObjectIntoCache(
			final Object informationObject, final String key)
			throws JargonException {

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
		byte[] serializedObject = serializeObjectToByteStream(
				informationObject, key);
		log.info("object serialized into:{} bytes", serializedObject.length);

		log.info("encrypting...");

		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		byte[] encrypted = cacheEncryptor.encrypt(serializedObject);
		log.info("bytes now encrypted for length:{}", encrypted.length);
		// store in file

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash,
				irodsAccount.getUserName());
		log.info("storing to file at absolute path: {}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		createCacheFileAndCacheDir(cacheFile);

		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory()
				.getStream2StreamAO(irodsAccount);
		stream2StreamAO.streamBytesToIRODSFile(encrypted, cacheFile);

		log.info("done...");
		return irodsFileAbsolutePath;

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#retrieveObjectFromCache(java.lang.String, java.lang.String)
	 */
	@Override
	public Object retrieveObjectFromCache(final String userName,
			final String key) throws JargonException {

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null userName");
		}

		log.info("retrieveObjectFromCache() user name:{}", userName);
		log.info("key", key);

		checkContracts();

		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}

		// build hash of key and look for file
		int keyHash = key.hashCode();
		log.info("generated hash for key:{}", keyHash);
		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(keyHash,
				irodsAccount.getUserName());
		log.info("looking for cache file at path:{}", irodsFileAbsolutePath);
		IRODSFile cacheFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory()
				.getStream2StreamAO(irodsAccount);
		byte[] fileBytes = stream2StreamAO.streamFileToByte(cacheFile);
		log.info("decrypting data based on provided key....");
		CacheEncryptor cacheEncryptor = new CacheEncryptor(key);
		fileBytes = cacheEncryptor.decrypt(fileBytes);

		log.info("streamed file into bytes for length of: {}", fileBytes.length);
		log.info("deserialzing...");
		return deserializeStreamToObject(fileBytes, key);

	}

	/**
	 * Based on the configuration, come up with an absolute path to the file
	 * name for the cache file
	 * 
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

	private byte[] serializeObjectToByteStream(final Object informationObject,
			final String key) throws JargonException {

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

	private Object deserializeStreamToObject(final byte[] objectBuffer,
			final String key) throws JargonException {

		log.info("deserializeStreamToObject(");
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
				objectBuffer);

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

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#purgeOldRequests()
	 */
	@Override
	public void purgeOldRequests() throws JargonException {
		log.info("purgeOldRequests()");
		long minToMillis = this.getCacheServiceConfiguration().getLifetimeInMinutes() * 60 * 1000;
		long millisNow = System.currentTimeMillis();
		long purgeThreshold = millisNow - minToMillis;
		log.info("purge threshold:{}", purgeThreshold);
		log.info("millis now:{}", millisNow);
	
		// list the files in the cache and purge any expired
		
		IRODSFile cacheDir = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(cacheServiceConfiguration.getCacheDirPath());
		
		for (File irodsFile : cacheDir.listFiles()) {
			if (irodsFile.lastModified() < purgeThreshold) {
				log.info("purging:{}", irodsFile.getAbsolutePath());
				irodsFile.delete();
			}
		}
		
		log.info("purge complete");

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

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#getIrodsAccessObjectFactory()
	 */
	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#setIrodsAccessObjectFactory(org.irods.jargon.core.pub.IRODSAccessObjectFactory)
	 */
	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#getIrodsAccount()
	 */
	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#setIrodsAccount(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#setCacheServiceConfiguration(org.irods.jargon.datautils.datacache.CacheServiceConfiguration)
	 */
	@Override
	public void setCacheServiceConfiguration(
			CacheServiceConfiguration cacheServiceConfiguration) {
		this.cacheServiceConfiguration = cacheServiceConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#getCacheServiceConfiguration()
	 */
	@Override
	public CacheServiceConfiguration getCacheServiceConfiguration() {
		return cacheServiceConfiguration;
	}

}
