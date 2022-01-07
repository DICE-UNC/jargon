/**
 *
 */
package org.irods.jargon.core.connection;

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
	
	public static class ReplicaTokenLock
	{
	    private final Lock lock = new ReentrantLock();
	    private int refCount = 0;
	    
	    public void tryLock(int timeoutInSeconds, boolean incrementOnOpen)
	    {
            try {
                if (!lock.tryLock(timeoutInSeconds, TimeUnit.SECONDS)) {
                    log.error("timeout trying to lock replica token cache");
                    throw new JargonRuntimeException("timeout obtaining replica token lock");
                }
                
                if (incrementOnOpen) {
                    ++refCount;
                }
            }
            catch (InterruptedException e) {
                log.info("interrupted", e);
                throw new JargonRuntimeException("replica token cache tryLock interrupted", e);
            }
	    }
	    
	    public void unlock(boolean decrementOnClose)
	    {
	        if (decrementOnClose) {
	            --refCount;
	        }
	        
	        lock.unlock();
	    }
	    
	    public int getRefCount()
	    {
	        return refCount;
	    }
	}

	private ConcurrentHashMap<ReplicaTokenCacheKey, ReplicaTokenLock> replicaTokenLockCache = new ConcurrentHashMap<>();
	private ConcurrentHashMap<ReplicaTokenCacheKey, ReplicaTokenCacheEntry> replicaTokenCache = new ConcurrentHashMap<>();

	/**
	 * Idempotent method to get a reference to a {@code ReplicaTokenLock}. A valid
	 * lock must be acquired before attempting to read, set, or clear a replica
	 * token entry.
	 *
	 * @param logicalPath {@code String} which is the iRODS path which is protected
	 *                    by the lock
	 * @param userName    {@code String} which is hte user name
	 * @return {@code ReplicaTokenLock} object that can be tried in order to read or
	 *                     manipulate a replica token
	 */
	public ReplicaTokenLock obtainReplicaTokenLock(final String logicalPath, final String userName) {

		log.info("obtainReplicaTokenLock()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

		return replicaTokenLockCache.computeIfAbsent(cacheKey, k -> new ReplicaTokenLock());

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 * 
	 * Creates a new entry in the cache if (logicalPath, userName) are not mapped
	 * to an existing entry.
	 *
	 * @param logicalPath  {@code String} which is the iRODS path which is protected
	 *                     by the lock
	 * @param userName     {@code String} which is the user name
	 * @return {@code ReplicaTokenCacheEntry} mapped to (logicalPath, userName)
	 */
	public ReplicaTokenCacheEntry insertReplicaTokenEntry(final String logicalPath, final String userName) {

		log.info("insertReplicaTokenEntry()");

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		ReplicaTokenCacheKey cacheKey = ReplicaTokenCacheKey.instance(logicalPath, userName);

        return (ReplicaTokenCacheEntry) replicaTokenCache
            .computeIfAbsent(cacheKey, k -> new ReplicaTokenCacheEntry(logicalPath));

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock} before updating with a valid replica token.
	 * 
	 * This method is called when the open is the first open for a file. On this
	 * first open, a call is made to obtain a replica token which will then be
	 * added to the cache via the {@code addReplicaToken()} method.
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
		replicaTokenCacheEntry.setOpenCount(1);

	}

	/**
	 * This method requires the caller to acquire the lock returned from
	 * {@code obtainReplicaTokenLock}.
	 *
	 * This method is called when a file is being closed, and where a replica token
	 * was obtained. This method will decrement the open count and report whether
	 * it is the final stream associated with the replica token.
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
        }
        else if (replicaTokenCacheEntry.getOpenCount() < 0){
            log.error("replica count less than zero, this is an unexpected condition that indicates a system problem");
        }

	}

}
