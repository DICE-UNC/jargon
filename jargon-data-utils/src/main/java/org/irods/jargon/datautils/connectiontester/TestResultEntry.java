/**
 * 
 */
package org.irods.jargon.datautils.connectiontester;

import org.irods.jargon.datautils.connectiontester.ConnectionTester.TestType;

/**
 * Entry for a connection test result
 * 
 * @author Mike Conway - DICE
 * 
 */
public class TestResultEntry {

	/**
	 * Type of test
	 */
	private TestType testType = TestType.SMALL;

	public enum OperationType {
		GET, PUT
	}

	/**
	 * Type of action (get or put of file)
	 */
	private OperationType operationType = OperationType.GET;

	/**
	 * Total number of milliseconds
	 */
	private long totalMilliseconds = 0L;
	/**
	 * bytes per second for transfer
	 */
	private int transferRateBytesPerSecond = 0;
	/**
	 * Was this successful?
	 */
	private boolean success = true;
	/**
	 * Total bytes in file
	 */
	private long totalBytes = 0L;

	/**
	 * Any available exception (may be null)
	 */
	private Throwable exception = null;

	/**
	 * 
	 */
	public TestResultEntry() {
	}

	/**
	 * @return the testType
	 */
	public TestType getTestType() {
		return testType;
	}

	/**
	 * @param testType
	 *            the testType to set
	 */
	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	/**
	 * @return the totalMilliseconds
	 */
	public long getTotalMilliseconds() {
		return totalMilliseconds;
	}

	/**
	 * @param totalMilliseconds
	 *            the totalMilliseconds to set
	 */
	public void setTotalMilliseconds(long totalMilliseconds) {
		this.totalMilliseconds = totalMilliseconds;
	}

	/**
	 * @return the transferRateBytesPerSecond
	 */
	public int getTransferRateBytesPerSecond() {
		return transferRateBytesPerSecond;
	}

	/**
	 * @param transferRateBytesPerSecond
	 *            the transferRateBytesPerSecond to set
	 */
	public void setTransferRateBytesPerSecond(int transferRateBytesPerSecond) {
		this.transferRateBytesPerSecond = transferRateBytesPerSecond;
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

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * @param exception
	 *            the exception to set
	 */
	public void setException(Throwable exception) {
		this.exception = exception;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestResultEntry [");
		if (testType != null) {
			builder.append("testType=");
			builder.append(testType);
			builder.append(", ");
		}
		if (operationType != null) {
			builder.append("operationType=");
			builder.append(operationType);
			builder.append(", ");
		}
		builder.append("totalMilliseconds=");
		builder.append(totalMilliseconds);
		builder.append(", transferRateBytesPerSecond=");
		builder.append(transferRateBytesPerSecond);
		builder.append(", success=");
		builder.append(success);
		builder.append(", totalBytes=");
		builder.append(totalBytes);
		builder.append(", ");
		if (exception != null) {
			builder.append("exception=");
			builder.append(exception);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the totalBytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * @param totalBytes
	 *            the totalBytes to set
	 */
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	/**
	 * @return the operationType
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * @param operationType
	 *            the operationType to set
	 */
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

}
