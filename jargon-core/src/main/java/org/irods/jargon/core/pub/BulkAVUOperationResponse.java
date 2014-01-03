/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.pub.domain.AvuData;

/**
 * Represents a response to a bulk AVU operation, includes success or failure
 * for an AVU operation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BulkAVUOperationResponse {

	public enum ResultStatus {
		OK, DUPLICATE_AVU, MISSING_METADATA_TARGET, OTHER_ERROR
	}

	private AvuData avuData;
	private ResultStatus resultStatus;
	private String message;

	public static BulkAVUOperationResponse instance(
			final ResultStatus resultStatus, final AvuData avuData,
			final String message) {
		return new BulkAVUOperationResponse(resultStatus, avuData, message);
	}

	/**
	 * 
	 */
	private BulkAVUOperationResponse(final ResultStatus resultStatus,
			final AvuData avuData, final String message) {

		if (resultStatus == null) {
			throw new IllegalArgumentException("null resultStatus");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null avuData");
		}

		if (message == null) {
			throw new IllegalArgumentException("null message");
		}

		this.avuData = avuData;
		this.resultStatus = resultStatus;
		this.message = message;

	}

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("BulkAVUOperationResponse");
		sBuilder.append("\n\tavuData:");
		sBuilder.append(avuData);
		sBuilder.append("\n\tresultStatus:");
		sBuilder.append(resultStatus);
		sBuilder.append("\n\tmessage:");
		sBuilder.append(message);
		return sBuilder.toString();
	}

	/**
	 * @return the avuData
	 */
	public AvuData getAvuData() {
		return avuData;
	}

	/**
	 * @param avuData
	 *            the avuData to set
	 */
	public void setAvuData(AvuData avuData) {
		this.avuData = avuData;
	}

	/**
	 * @return the resultStatus
	 */
	public ResultStatus getResultStatus() {
		return resultStatus;
	}

	/**
	 * @param resultStatus
	 *            the resultStatus to set
	 */
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
