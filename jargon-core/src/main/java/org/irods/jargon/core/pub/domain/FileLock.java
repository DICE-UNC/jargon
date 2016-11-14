/**
 *
 */
package org.irods.jargon.core.pub.domain;

import org.irods.jargon.core.packinstr.DataObjInpForFileLock;

/**
 * Information regarding file locks in iRODS
 *
 * @author Mike Conway - DICE
 *
 */
public class FileLock {

	private String irodsAbsolutePath = "";
	private int fd = -1;
	private DataObjInpForFileLock.LockType lockType = null;
	private long approximateSystemTimeWhenLockObtained = 0L;

	/**
	 * @return the irodsAbsolutePath
	 */
	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	/**
	 * @param irodsAbsolutePath
	 *            the irodsAbsolutePath to set
	 */
	public void setIrodsAbsolutePath(final String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	/**
	 * @return the fd
	 */
	public int getFd() {
		return fd;
	}

	/**
	 * @param fd
	 *            the fd to set
	 */
	public void setFd(final int fd) {
		this.fd = fd;
	}

	/**
	 * @return the lockType
	 */
	public DataObjInpForFileLock.LockType getLockType() {
		return lockType;
	}

	/**
	 * @param lockType
	 *            the lockType to set
	 */
	public void setLockType(final DataObjInpForFileLock.LockType lockType) {
		this.lockType = lockType;
	}

	/**
	 * @return the approximateSystemTimeWhenLockObtained
	 */
	public long getApproximateSystemTimeWhenLockObtained() {
		return approximateSystemTimeWhenLockObtained;
	}

	/**
	 * @param approximateSystemTimeWhenLockObtained
	 *            the approximateSystemTimeWhenLockObtained to set
	 */
	public void setApproximateSystemTimeWhenLockObtained(
			final long approximateSystemTimeWhenLockObtained) {
		this.approximateSystemTimeWhenLockObtained = approximateSystemTimeWhenLockObtained;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileLock [");
		if (irodsAbsolutePath != null) {
			builder.append("irodsAbsolutePath=");
			builder.append(irodsAbsolutePath);
			builder.append(", ");
		}
		builder.append("fd=");
		builder.append(fd);
		builder.append(", ");
		if (lockType != null) {
			builder.append("lockType=");
			builder.append(lockType);
			builder.append(", ");
		}
		builder.append("approximateSystemTimeWhenLockObtained=");
		builder.append(approximateSystemTimeWhenLockObtained);
		builder.append("]");
		return builder.toString();
	}

}
