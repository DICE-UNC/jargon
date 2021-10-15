/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.irods.jargon.core.exception.ReplicaTokenLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author conwaymc
 *
 *         A manager of replica tokens by iRODS logical path, this thread-safe
 *         cache is responsible for monitoring when a replica token has been
 *         acquired, acquiring a replica token the first time and then reusing
 *         that token for subsequent opens. Finally, when closing a file, this
 *         cache will let the caller know that a particular stream is the last
 *         stream.
 */
public class ReplicaTokenCacheManager {

	static Logger log = LoggerFactory.getLogger(ReplicaTokenCacheManager.class);

	private ConcurrentHashMap<ReplicaTokenCacheKey, ReplicaTokenCacheEntry> replicaTokenCache = new ConcurrentHashMap<>();

	/**
	 * Idempotent method to get a reference to a {@link Lock}. A valid lock must be
	 * acquired before attempting to read, set, or clear a replica token entry
	 *
	 * @param logicalPath {@code String} which is the iRODS path which is protected
	 *                    by the lock
	 * @param userName    {@code String} which is hte user name
	 * @return {@link Lock} object that can be tried in order to read or manipulate
	 *         a replica token
	 */
	public Lock obtainReplicaTokenLock(final String logicalPath, final String userName) {

		log.info("obtainReplicaTokenLock()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		replicaTokenCache.putIfAbsent(cacheKey, new ReplicaTokenCacheEntry(logicalPath));
		return replicaTokenCache.get(cacheKey).getLock();

	}

	/**
	 * After obtaining a lock, this should be the initial method called, it will
	 * check if there is an existing replica token. If this method returns an empty
	 * token, the calling initializer that is doing the open should call an open and
	 * obtain the replica token from iRODS and then initialize the cache with that
	 * token using the {@code addReplicaToken} method.
	 * 
	 * @param logicalPath {@code String} which is the iRODS path which is protected
	 *                    by the lock
	 * @param userName    {@code String} which is the user name
	 * @return {@code String} with the replica token string
	 *
	 * @throws ReplicaTokenLockException
	 */
	public String claimExistingReplicaToken(final String logicalPath, final String userName)
			throws ReplicaTokenLockException {
		log.info("obtainExistingReplicaToken()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		ReplicaTokenCacheEntry replicaTokenCacheEntry = replicaTokenCache.get(cacheKey);
		if (replicaTokenCacheEntry == null) {
			log.error("no cache entry found, perhaps lock was not acquired?");
			throw new ReplicaTokenLockException("null cache entry, was lock acquired before calling this method?");
		}

		replicaTokenCacheEntry.incrementOpenCount();

		return replicaTokenCacheEntry.getReplicaToken();

	}

	/**
	 * This method requires the caller to first call {@code obtainReplicaTokenLock}
	 * with a {@code tryLock()} before updating with a valid replica token. This
	 * method is called when the open is the first open for a file. On this first
	 * open a call is made to obtain a replica token which will then be added to the
	 * cache via the {@code addReplicaToken()} method.
	 *
	 * This method is used
	 *
	 * @param logicalPath  {@code String} which is the iRODS path which is protected
	 *                     by the lock
	 * @param userName     {@code String} which is the user name
	 * @param replicaToken {@code String} with the replica token string
	 * @throws ReplicaTokenLockException
	 */
	public void addReplicaToken(final String logicalPath, final String userName, final String replicaToken,
			final String replicaNumber) throws ReplicaTokenLockException {

		log.info("addReplicaToken()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (replicaToken == null || replicaToken.isEmpty()) {
			throw new IllegalArgumentException("null or empty replicaToken");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		ReplicaTokenCacheEntry replicaTokenCacheEntry = replicaTokenCache.get(cacheKey);
		if (replicaTokenCacheEntry == null) {
			log.error("no cache entry found, perhaps lock was not acquired?");
			throw new ReplicaTokenLockException("null cache entry, was lock acquired before calling this method?");
		}

		/*
		 * ensure that an error has not occurred where another thread has set the token
		 */

		if (!replicaTokenCacheEntry.getReplicaToken().isEmpty()) {
			log.error("A token already exists, was lock called?");
			throw new ReplicaTokenLockException("A token already exists, was lock called?");

		}

		replicaTokenCacheEntry.setReplicaToken(replicaToken);
		replicaTokenCacheEntry.setOpenCount(1);

	}

	/**
	 * This method requires the caller to first call {@code obtainReplicaTokenLock}
	 * with a {@code tryLock()}.
	 *
	 *
	 * This method is called when a file is being closed, and where a replica token
	 * was obtained. This method will decrement the count, when it gets to zero all
	 * file handles are closed and the cache entry is removed. It is up to the
	 * caller to adjust the close parameters to update the catalog, compute
	 * checksums, and carry out all iRODS close operations with the desired close
	 * flags.
	 *
	 * @param logicalPath {@code String} with the logical path to the file to be
	 *                    closed * @param userName {@code String} which is the user
	 *                    name
	 * @return {@code boolean} with a value of {@code true} when this is the last
	 *         reference to the file
	 */
	public boolean closeReplicaToken(final String logicalPath, final String userName) {

		log.info("closeReplicaToken()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("logicalPath:{}", logicalPath);
		log.info("userName:{}", userName);

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		ReplicaTokenCacheEntry replicaTokenCacheEntry = replicaTokenCache.get(cacheKey);
		replicaTokenCacheEntry.decrementOpenCount();
		if (replicaTokenCacheEntry.getOpenCount() < 0) {
			log.error("replica count less than zero, this is an unexpected condition that indicates a system problem");
			throw new IllegalStateException("replica count should never be less than zero");
		} else if (replicaTokenCacheEntry.getOpenCount() == 0) {
			replicaTokenCache.remove(cacheKey);
			return true;
		} else {
			return false;
		}

	}

}
