/**
 * 
 */
package org.irods.jargon.pool.conncache.functtest;

/**
 * @author conwaymc
 *
 */
public class PoolCallResult {

	private long time = 0;
	private boolean success = true;

	/**
	 * 
	 */
	public PoolCallResult() {
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success
	 *            the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PoolCallResult [time=").append(time).append(", success=").append(success).append("]");
		return builder.toString();
	}

}
