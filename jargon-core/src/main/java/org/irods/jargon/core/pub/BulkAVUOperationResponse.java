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

	private final AvuData avuData;
	private final ResultStatus resultStatus;
	private final String message;
	private final String path;

	public static BulkAVUOperationResponse instance(
			final ResultStatus resultStatus, final AvuData avuData,
			final String message) {
		return new BulkAVUOperationResponse(resultStatus, avuData, message);
	}
	
	public static BulkAVUOperationResponse instance(
			final ResultStatus resultStatus, final AvuData avuData,
			final String message, final String path) {
		return new BulkAVUOperationResponse(resultStatus, avuData, message, path);
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
		this.path = "";

	}
	
	private BulkAVUOperationResponse(final ResultStatus resultStatus,
			final AvuData avuData, final String message, final String path) {

		if (resultStatus == null) {
			throw new IllegalArgumentException("null resultStatus");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null avuData");
		}

		if (message == null) {
			throw new IllegalArgumentException("null message");
		}
		
		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		this.avuData = avuData;
		this.resultStatus = resultStatus;
		this.message = message;
		this.path = path;

	}

	
	/**
	 * @return the avuData
	 */
	public AvuData getAvuData() {
		return avuData;
	}

	/**
	 * @return the resultStatus
	 */
	public ResultStatus getResultStatus() {
		return resultStatus;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}

}
