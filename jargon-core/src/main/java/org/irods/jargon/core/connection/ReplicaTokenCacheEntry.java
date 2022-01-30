/**
 * 
 */
package org.irods.jargon.core.connection;

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
	 * {@code String} with the replica number
	 */
	private String replicaNumber = "";
	/**
	 * Count of open files
	 */
	private int openCount = 0;
	/**
	 * The thread id of the first coordinated stream
	 */
	private long firstStreamThreadId = -1;

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
	
	public int incrementOpenCount() {
		return ++openCount;
	}

	public int decrementOpenCount() {
		return --openCount;
	}
	
	public void setThreadIdOfFirstStream(long threadId) {
		this.firstStreamThreadId = threadId;
	}
	
	public long getThreadIdOfFirstStream() {
		return firstStreamThreadId;
	}

	/**
	 * @return the replicaNumber
	 */
	public String getReplicaNumber() {
		return replicaNumber;
	}

	/**
	 * @param replicaNumber the replicaNumber to set
	 */
	public void setReplicaNumber(String replicaNumber) {
		this.replicaNumber = replicaNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReplicaTokenCacheEntry [");
		if (logicalPath != null) {
			builder.append("logicalPath=").append(logicalPath).append(", ");
		}
		if (replicaToken != null) {
			builder.append("replicaToken=").append(replicaToken).append(", ");
		}
		if (replicaNumber != null) {
			builder.append("replicaNumber=").append(replicaNumber).append(", ");
		}
		builder.append("openCount=").append(openCount);
		builder.append("threadId=").append(firstStreamThreadId);
		return builder.append("]").toString();
	}
}
