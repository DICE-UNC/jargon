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
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service to provide an insecure data cache. The data in the cache is not
 * encrypted and stored as plain text.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class NoEncryptDataCacheServiceImpl extends AbstractDataUtilsServiceImpl implements DataCacheService {

	/**
	 * Configuration controls behavior of the cache. This can be set, or can just
	 * use the defaults, which cache in the users home dir and do cleanups as part
	 * of request processing.
	 */
	CacheServiceConfiguration cacheServiceConfiguration = new CacheServiceConfiguration();

	public static final Logger log = LogManager.getLogger(NoEncryptDataCacheServiceImpl.class);

	/**
	 * Constructor with required dependencies
	 *
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} that can
	 *                                 create necessary objects
	 * @param irodsAccount             {@link IRODSAccount} that contains the login
	 *                                 information
	 */
	public NoEncryptDataCacheServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Default (no-values) constructor.
	 */
	public NoEncryptDataCacheServiceImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#
	 * putStringValueIntoCache(java.lang.String, java.lang.String)
	 */
	@Override
	public String putStringValueIntoCache(final String stringToCache, final String key) throws JargonException {

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

		byte[] stringData = stringToCache.getBytes();

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(key, irodsAccount.getUserName());
		log.info("storing to file at absolute path: {}", irodsFileAbsolutePath);
		IRODSFile cacheFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);

		createCacheFileAndCacheDir(cacheFile);
		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2StreamAO.streamBytesToIRODSFile(stringData, cacheFile);

		log.info("done...");
		return irodsFileAbsolutePath;

	}

	/**
	 * @param cacheFile
	 * @throws JargonException
	 */
	private void createCacheFileAndCacheDir(final IRODSFile cacheFile) throws JargonException {
		// try {
		cacheFile.getParentFile().mkdirs();
		// cacheFile.createNewFile();
		// } catch (IOException e) {
		// throw new JargonException("error creating new cache file");
		// }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.DataCacheService#
	 * retrieveStringValueFromCache(java.lang.String, java.lang.String)
	 */
	@Override
	public String retrieveStringValueFromCache(final String userName, final String key)
			throws DataNotFoundException, JargonException {

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null key");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null userName");
		}

		log.info("retrieveStringValueFromCache() user name:{}", userName);

		checkContracts();

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(key, irodsAccount.getUserName());
		log.info("looking for cache file at path:{}", irodsFileAbsolutePath);
		IRODSFile cacheFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		byte[] fileBytes = stream2StreamAO.streamFileToByte(cacheFile);

		log.info("streamed file into bytes for length of: {}", fileBytes.length);
		log.info("deserialzing...");
		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}
		return new String(fileBytes);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#
	 * putSerializedEncryptedObjectIntoCache(java.lang.Object, java.lang.String)
	 */
	@Override
	public String putSerializedEncryptedObjectIntoCache(final Object informationObject, final String key)
			throws JargonException {

		if (informationObject == null) {
			throw new IllegalArgumentException("null informationObject");
		}

		checkContracts();
		log.info("putSerializedEncryptedObjectIntoCache()");

		// clean up old files? (or in a sep thread?) make an option based on
		// created date and window

		log.info("checking on purge of old requests...");

		if (getCacheServiceConfiguration().isDoCleanupDuringRequests()) {
			purgeOldRequests();
		}

		byte[] serializedObject = serializeObjectToByteStream(informationObject, key);
		log.info("object serialized into:{} bytes", serializedObject.length);

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(key, irodsAccount.getUserName());
		log.info("storing to file at absolute path: {}", irodsFileAbsolutePath);
		IRODSFile cacheFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		createCacheFileAndCacheDir(cacheFile);

		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		stream2StreamAO.streamBytesToIRODSFile(serializedObject, cacheFile);

		log.info("done...");
		return irodsFileAbsolutePath;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#
	 * retrieveObjectFromCache(java.lang.String, java.lang.String)
	 */
	@Override
	public Object retrieveObjectFromCache(final String userName, final String key) throws JargonException {

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

		String irodsFileAbsolutePath = buildIRODSFileAbsolutePath(key, irodsAccount.getUserName());
		log.info("looking for cache file at path:{}", irodsFileAbsolutePath);
		IRODSFile cacheFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsFileAbsolutePath);
		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory().getStream2StreamAO(irodsAccount);
		byte[] fileBytes = stream2StreamAO.streamFileToByte(cacheFile);

		log.info("streamed file into bytes for length of: {}", fileBytes.length);
		log.info("deserialzing...");
		return deserializeStreamToObject(fileBytes, key);

	}

	/**
	 * Based on the configuration, come up with an absolute path to the file name
	 * for the cache file
	 *
	 * @param keyHash  {@code int}
	 * @param userName {@code String}
	 * @return {@code String}
	 */
	private String buildIRODSFileAbsolutePath(final String key, final String userName) {
		StringBuilder sb = computeCacheDirPathFromHomeDirFromUserAndZone(userName);

		sb.append("/");
		sb.append(userName);
		sb.append("-");
		sb.append(key);
		sb.append(".dat");

		return sb.toString();

	}

	/**
	 * Given a user and zone, come up with the absolute path to the cache directorh
	 * parent directory based on the standard /zone/home/user/cacheDir as configured
	 * in the CacheDirConfig
	 *
	 * @param userName {@code String} with the name of the user for whom the home
	 *                 cache directory will be computed
	 * @return {@code StringBuilder} with computed part of the abs path
	 */
	private StringBuilder computeCacheDirPathFromHomeDirFromUserAndZone(final String userName) {
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
		return sb;
	}

	private byte[] serializeObjectToByteStream(final Object informationObject, final String key)
			throws JargonException {

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

	private Object deserializeStreamToObject(final byte[] objectBuffer, final String key) throws JargonException {

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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.datacache.AccountCacheService#purgeOldRequests ()
	 */
	@Override
	public void purgeOldRequests() throws JargonException {
		log.info("purgeOldRequests()");
		long daysToMillis = (long) getCacheServiceConfiguration().getLifetimeInDays() * 60 * 1000 * 60 * 24;
		long millisNow = System.currentTimeMillis();
		long purgeThreshold = millisNow - daysToMillis;
		log.info("purge threshold:{}", purgeThreshold);
		log.info("millis now:{}", millisNow);

		IRODSFile cacheDir = null;
		if (cacheServiceConfiguration.isCacheInHomeDir()) {
			StringBuilder sb = computeCacheDirPathFromHomeDirFromUserAndZone(irodsAccount.getUserName());
			log.info("built home dir automatically for cache using account zone and user name:{}", sb.toString());
			cacheDir = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(sb.toString());
		} else {
			cacheDir = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(cacheServiceConfiguration.getCacheDirPath());

		}

		// list the files in the cache and purge any expired
		log.info("cache dir path:{}", cacheDir.getAbsolutePath());

		if (!cacheDir.exists()) {
			log.info("cache dir does not exist, do not purge");
			return;
		}

		for (File irodsFile : cacheDir.listFiles()) {
			if (irodsFile.lastModified() < purgeThreshold) {
				log.info("purging:{}", irodsFile.getAbsolutePath());
				boolean delSuccess = irodsFile.delete();
				if (!delSuccess) {
					log.warn("error deleting file logged and ignored");
				}
			}
		}

		log.info("purge complete");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#
	 * setCacheServiceConfiguration
	 * (org.irods.jargon.datautils.datacache.CacheServiceConfiguration)
	 */
	@Override
	public void setCacheServiceConfiguration(final CacheServiceConfiguration cacheServiceConfiguration) {
		this.cacheServiceConfiguration = cacheServiceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.datacache.AccountCacheService#
	 * getCacheServiceConfiguration()
	 */
	@Override
	public CacheServiceConfiguration getCacheServiceConfiguration() {
		return cacheServiceConfiguration;
	}

}
