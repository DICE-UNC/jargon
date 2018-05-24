/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Structure that is the result of a client-server negotiation, analogous to the
 * cs_neg_t struct in iRODS, as read from the server
 *
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationStructNotifyServerOfResult extends AbstractIRODSPackingInstruction {

	public enum Outcome {
		CS_NEG_USE_TCP, CS_NEG_USE_SSL, CS_NEG_FAILURE
	}

	private int status = 0;
	private String result = "";

	public String getResult() {
		return result;
	}

	public void setResult(final String result) {
		this.result = result;
	}

	public static final String NEG_PI = "CS_NEG_PI";
	public static final int STATUS_FAILURE = 0;
	public static final int STATUS_SUCCESS = 1;
	public static final int API_NBR = 0;
	public static final String CS_NEG_RESULT_KW = "cs_neg_result_kw";

	/**
	 * Create an instance to send to iRODS for failure of SSL negotiation
	 *
	 * @return {@link ClientServerNegotiationStructNotifyServerOfResult}
	 */
	public static ClientServerNegotiationStructNotifyServerOfResult instanceForFailure() {
		return new ClientServerNegotiationStructNotifyServerOfResult(STATUS_FAILURE, Outcome.CS_NEG_FAILURE.name());
	}

	/**
	 * Create an instance to send to iRODS
	 *
	 * @param status
	 *            {@code int} with status to send
	 * @param result
	 *            {@code String} with result to send
	 * @return {@link ClientServerNegotiationStructNotifyServerOfResult}
	 */
	public static ClientServerNegotiationStructNotifyServerOfResult instance(final int status, final String result) {
		return new ClientServerNegotiationStructNotifyServerOfResult(status, result);
	}

	private ClientServerNegotiationStructNotifyServerOfResult(final int status, final String result) {

		if (result == null || result.isEmpty()) {
			throw new IllegalArgumentException("null or empty result");
		}

		this.status = status;
		this.result = result;
		setApiNumber(API_NBR);

	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final int status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientServerNegotiationStruct [status=").append(status).append(", ");
		if (result != null) {
			builder.append("result=").append(result);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Was this negotiation a success?
	 *
	 * @return {@code boolean}
	 */
	public boolean wasThisASuccess() {
		return getStatus() == 1;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		StringBuilder sb = new StringBuilder();
		sb.append(CS_NEG_RESULT_KW);
		sb.append('=');
		sb.append(result);
		sb.append(';');
		Tag message = new Tag(NEG_PI, new Tag[] { new Tag("status", status), new Tag("result", sb.toString()) });
		return message;
	}

}
