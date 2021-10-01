/**
 * 
 */
package org.irods.jargon.core.connection;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author conwaymc
 *
 */
public class ReplicaTokenCacheEntry {

	/**
	 * iRODS logical path associated with the replica token
	 */
	private String logicalPath = "";
	/**
	 * Replica token
	 */
	private String replicaToken = "";
	/**
	 * Count of open files
	 */
	private int openCount = 0;

	/**
	 * a reentrant lock that will be used by the open and close methods for streams
	 * that use replica tokens
	 */
	private final Lock lock = new ReentrantLock();

	public ReplicaTokenCacheEntry(final String logicalPath) {

		if (logicalPath == null || logicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty logicalPath");
		}

		this.logicalPath = logicalPath;
	}

	public String getLogicalPath() {
		return logicalPath;
	}

	public void setLogicalPath(String logicalPath) {
		this.logicalPath = logicalPath;
	}

	public String getReplicaToken() {
		return replicaToken;
	}

	public void setReplicaToken(String replicaToken) {
		this.replicaToken = replicaToken;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public void incrementOpenCount() {
		this.openCount = openCount + 1;
	}

	public void decrementOpenCount() {
		this.openCount = openCount - 1;
	}

	public Lock getLock() {
		return lock;
	}
}
