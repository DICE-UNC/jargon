/**
 *
 */
package org.irods.jargon.core.connection;

/**
 * Defines an immutable callback of status of invocation of an iRODS function.
 * This includes the ability to provide progress on an underlying stream for get
 * and put operations.
 * <p/>
 * This is new development, and can eventually include the ability to provide
 * callbacks on deletes and other operations.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConnectionProgressStatus {

	public enum CallbackType {
		SEND_PROGRESS, RECEIVE_PROGRESS, OPERATIONAL_MESSAGE
	}

	private final long byteCount;
	private final CallbackType callbackType;

	/**
	 * @return the byteCount for the operation. Note that this does not
	 *         aggregate, only sends the count at each read/write
	 */
	public long getByteCount() {
		return byteCount;
	}

	/**
	 * @return the callbackType enum value
	 */
	public CallbackType getCallbackType() {
		return callbackType;
	}

	/**
	 * Create an immutable callback for progress sending data (PUT)
	 * 
	 * @param byteCount
	 *            <code>long</code> with the number of bytes instantaneously
	 *            sent.
	 * @return
	 */
	public static ConnectionProgressStatus instanceForSend(final long byteCount) {
		return new ConnectionProgressStatus(CallbackType.SEND_PROGRESS,
				byteCount);
	}

	/**
	 * Create an immutable callback for progress receiving data (GET)
	 * 
	 * @param byteCount
	 *            <code>long</code> with the number of bytes instantaneously
	 *            sent.
	 * @return
	 */
	public static ConnectionProgressStatus instanceForReceive(
			final long byteCount) {
		return new ConnectionProgressStatus(CallbackType.RECEIVE_PROGRESS,
				byteCount);
	}

	private ConnectionProgressStatus(final CallbackType callbackType,
			final long byteCount) {

		if (callbackType == null) {
			throw new IllegalArgumentException("null callbackType");
		}

		this.callbackType = callbackType;
		this.byteCount = byteCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ConnectionProgressStatus");
		sb.append("\n   callbackType:");
		sb.append(callbackType);
		sb.append("\n   byteCount:");
		sb.append(byteCount);
		return sb.toString();
	}

}
