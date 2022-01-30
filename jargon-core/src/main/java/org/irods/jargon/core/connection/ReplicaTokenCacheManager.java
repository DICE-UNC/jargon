/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.irods.jargon.core.exception.JargonRuntimeException;
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

	private Map<String, ReferenceCountedLockMap> replicaTokenLockCache = new ConcurrentHashMap<>();
	private Map<ReplicaTokenCacheKey, ReplicaTokenCacheEntry> replicaTokenCache = new ConcurrentHashMap<>();

	/**
	 * Idempotent method to get a reference to a {@link Lock}. A valid lock must be
	 * acquired before attempting to read, set, or clear a replica token entry.
	 *
	 * @param logicalPath {@code String} which is the iRODS path which is protected
	 *                    by the lock
	 * @param userName    {@code String} which is the user name
	 * @return {@code ReplicaTokenLock} object that can be tried in order to read or
	 *         manipulate a replica token
	 */
	public Lock obtainReplicaTokenLock(final String logicalPath, final String userName) {

		log.info("obtainReplicaTokenLock()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		return replicaTokenLockCache.computeIfAbsent(userName, k -> new ReferenceCountedLockMap()).get(logicalPath);

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 * 
	 * Creates a new entry in the cache if (logicalPath, userName) is not mapped to
	 * an existing entry.
	 *
	 * @param logicalPath {@code String} which is the iRODS path which is protected
	 *                    by the lock
	 * @param userName    {@code String} which is the user name
	 * @return {@code ReplicaTokenCacheEntry} mapped to (logicalPath, userName)
	 */
	public ReplicaTokenCacheEntry getReplicaTokenEntry(final String logicalPath, final String userName) {

		log.info("getReplicaTokenEntry()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		return (ReplicaTokenCacheEntry) replicaTokenCache.computeIfAbsent(cacheKey,
				k -> new ReplicaTokenCacheEntry(logicalPath));

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock} before updating with a valid replica token.
	 * 
	 * This method is called when the open is the first open for a file. On this
	 * first open, a call is made to obtain a replica token which will then be added
	 * to the cache via the {@code addReplicaToken()} method.
	 *
	 * @param logicalPath  {@code String} which is the iRODS path which is protected
	 *                     by the lock
	 * @param userName     {@code String} which is the user name
	 * @param replicaToken {@code String} with the replica token string
	 * @throws ReplicaTokenLockException
	 */
	public void addReplicaToken(final String logicalPath, final String userName, final String replicaToken,
			final int replicaNumber) throws ReplicaTokenLockException {

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
		replicaTokenCacheEntry.setReplicaNumber(String.valueOf(replicaNumber));
		replicaTokenCacheEntry.setThreadIdOfFirstStream(Thread.currentThread().getId());
		replicaTokenCacheEntry.setOpenCount(1);

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 *
	 * This method is called when a file is being closed, and where a replica token
	 * was obtained. This method will decrement the open count and report whether it
	 * is the final stream associated with the replica token.
	 *
	 * @param logicalPath {@code String} with the logical path to the file to be
	 *                    closed
	 * @param userName    {@code String} which is the user name
	 * @return {@code boolean} with a value of {@code true} when this is the last
	 *         reference to the file
	 */
	public boolean isFinalReferenceToReplicaToken(final String logicalPath, final String userName) {

		log.info("isFinalReferenceToReplicaToken()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("logicalPath:{}", logicalPath);
		log.info("userName:{}", userName);

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);
		ReplicaTokenCacheEntry cacheEntry = replicaTokenCache.get(cacheKey);

		return cacheEntry.getOpenCount() == 1;

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 *
	 * This method is called when a file is being closed, and where a replica token
	 * was obtained. Checks if the first coordinated stream invoked this method.
	 *
	 * @param logicalPath {@code String} with the logical path to the file to be
	 *                    closed
	 * @param userName    {@code String} which is the user name
	 * @return {@code boolean} with a value of {@code true} if the first coordinated
	 *         stream invoked this method
	 */
	public boolean isFirstStream(final String logicalPath, final String userName) {

		log.info("isFirstStream()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("logicalPath:{}", logicalPath);
		log.info("userName:{}", userName);

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);
		ReplicaTokenCacheEntry cacheEntry = replicaTokenCache.get(cacheKey);

		return cacheEntry.getThreadIdOfFirstStream() == Thread.currentThread().getId();

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 *
	 * This method is called when a file is being closed, and where a replica token
	 * was obtained. This method will remove the cache entry when the open count
	 * becomes zero.
	 *
	 * @param logicalPath {@code String} with the logical path to the file to be
	 *                    closed
	 * @param userName    {@code String} which is the user name
	 */
	public void removeReplicaToken(final String logicalPath, final String userName) {

		log.info("removeReplicaToken()");

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

		if (replicaTokenCacheEntry.decrementOpenCount() == 0) {
			replicaTokenCache.remove(cacheKey);
			replicaTokenLockCache.get(userName).remove(logicalPath);
		} else if (replicaTokenCacheEntry.getOpenCount() < 0) {
			log.error("replica count less than zero, this is an unexpected condition that indicates a system problem");
		}

	}

	/**
	 * A convenience function for attempting to acquire a lock.
	 * 
	 * @throws JargonRuntimeException If the timeout limit is reached or the wait
	 *                                operation is interrupted.
	 * 
	 * @param lock             The lock to try and acquire
	 * @param timeoutInSeconds The number of seconds to wait before timing out
	 */
	public static void tryLock(Lock lock, int timeoutInSeconds) {
		try {
			if (!lock.tryLock(timeoutInSeconds, TimeUnit.SECONDS)) {
				log.error("timeout trying to lock replica token cache");
				throw new JargonRuntimeException("timeout obtaining replica token lock");
			}
		} catch (InterruptedException e) {
			log.info("interrupted", e);
			throw new JargonRuntimeException("replica token cache tryLock interrupted", e);
		}
	}

	/**
	 * A map-like data type that allows tracking the number of references to Locks.
	 * Instances of this class are thread-safe. Only one thread is allowed access to
	 * the data at any time.
	 * 
	 * @author kory
	 */
	private static final class ReferenceCountedLockMap {

		private Map<String, RefCountedLock> map = new HashMap<>();
		private Lock lock = new ReentrantLock();

		/**
		 * Creates a new entry in the map if one does not exist and returns it to the
		 * caller. Increments the reference count.
		 * 
		 * @param key The logical path to a data object
		 * @return {@link Lock} A lock meant to enforce synchronous write access
		 */
		public Lock get(String key) {
			try {
				lock.lock();

				RefCountedLock entry = map.computeIfAbsent(key, k -> new RefCountedLock());
				++entry.refCount;

				return entry.lock;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Removes the mapping for the specified key from this map if present.
		 * Decrements the reference count.
		 * 
		 * @param key The logical path to a data object
		 */
		public void remove(String key) {
			try {
				lock.lock();

				RefCountedLock entry = map.get(key);

				if (null != entry && 0 == --entry.refCount) {
					map.remove(key);
				}
			} finally {
				lock.unlock();
			}
		}

		private static final class RefCountedLock {
			public Lock lock = new ReentrantLock();
			public int refCount = 0;
		}

	}

}
