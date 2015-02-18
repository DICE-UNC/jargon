/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Immutable object represents the options controlling the behavior of the io
 * pipeline. Typically, these options are built based on the current state of
 * the {@link JargonProperties} at the time a connection is created.
 * <p/>
 * Note that this object does not have synchronization. Through typical usage,
 * this configuration is initialized at connection startup, and a connection is
 * confined to one thread, so this should be just fine.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PipelineConfiguration {

	private final int irodsSocketTimeout;
	private final int irodsParallelSocketTimeout;
	private final int internalInputStreamBufferSize;
	private final int internalOutputStreamBufferSize;
	private final int internalCacheBufferSize;
	private final int sendInputStreamBufferSize;
	private final int localFileInputStreamBufferSize;
	private final int localFileOutputStreamBufferSize;
	private final String defaultEncoding;
	private final int inputToOutputCopyBufferByteSize;
	private final boolean reconnect;
	private final long reconnectTimeInMillis;
	private final boolean instrument;
	private final boolean forcePamFlush;
	private final boolean tcpKeepAlive = true;

	public boolean isTcpKeepAlive() {
		return tcpKeepAlive;
	}

	private final int socketRecieveWindowSize = 16 * 1024 * 1024;

	public int getSocketRecieveWindowSize() {
		return socketRecieveWindowSize;
	}

	public int getSocketSendWindowSize() {
		return socketSendWindowSize;
	}

	private final int socketSendWindowSize = 16 * 1024 * 1024;

	/**
	 * Static initializer method will derive an immutable
	 * <code>PipelineConfiguration</code> based on the prevailing
	 * <code>JargonProperties</code> at the time the connection is created.
	 * 
	 * @param jargonProperties
	 * @return
	 */
	public static PipelineConfiguration instance(
			final JargonProperties jargonProperties) {
		return new PipelineConfiguration(jargonProperties);
	}

	private PipelineConfiguration(final JargonProperties jargonProperties) {

		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		irodsParallelSocketTimeout = jargonProperties
				.getIRODSParallelTransferSocketTimeout();
		internalInputStreamBufferSize = jargonProperties
				.getInternalInputStreamBufferSize();
		internalOutputStreamBufferSize = jargonProperties
				.getInternalOutputStreamBufferSize();
		internalCacheBufferSize = jargonProperties.getInternalCacheBufferSize();
		sendInputStreamBufferSize = jargonProperties
				.getSendInputStreamBufferSize();
		localFileInputStreamBufferSize = jargonProperties
				.getLocalFileInputStreamBufferSize();
		localFileOutputStreamBufferSize = jargonProperties
				.getLocalFileOutputStreamBufferSize();
		inputToOutputCopyBufferByteSize = jargonProperties
				.getInputToOutputCopyBufferByteSize();
		instrument = jargonProperties.isInstrument();
		reconnect = jargonProperties.isReconnect();
		reconnectTimeInMillis = jargonProperties.getReconnectTimeInMillis();
		defaultEncoding = jargonProperties.getEncoding();
		forcePamFlush = jargonProperties.isForcePamFlush();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PipelineConfiguration");
		sb.append("\n   irodsSocketTimeout:");
		sb.append(irodsSocketTimeout);
		sb.append("\n   irodsParallelSocketTimeout:");
		sb.append(irodsParallelSocketTimeout);
		sb.append("\n   internalInputStreamBufferSize:");
		sb.append(internalInputStreamBufferSize);
		sb.append("\n   internalOutputStreamBufferSize:");
		sb.append(internalOutputStreamBufferSize);
		sb.append("\n   internalCacheBufferSize:");
		sb.append(internalCacheBufferSize);
		sb.append("\n  localFileOutputStreamBufferSize:");
		sb.append(localFileOutputStreamBufferSize);
		sb.append("\n  localFileInputStreamBufferSize:");
		sb.append(localFileInputStreamBufferSize);
		sb.append("\n   defaultEncoding:");
		sb.append(defaultEncoding);
		sb.append("\n   inputToOutputCopyBufferByteSize:");
		sb.append(inputToOutputCopyBufferByteSize);
		sb.append("\n  instrument:");
		sb.append(instrument);
		sb.append("\n   reconnect:");
		sb.append(reconnect);
		sb.append("\n   reconnect time in millis:");
		sb.append(reconnectTimeInMillis);
		sb.append("\n   forcePamFlush:");
		sb.append(forcePamFlush);
		return sb.toString();
	}

	/**
	 * @return the internalInputStreamBufferSize
	 */
	public int getInternalInputStreamBufferSize() {
		return internalInputStreamBufferSize;
	}

	/**
	 * @return the internalOutputStreamBufferSize
	 */
	public int getInternalOutputStreamBufferSize() {
		return internalOutputStreamBufferSize;
	}

	/**
	 * @return the internalCacheBufferSize
	 */
	public int getInternalCacheBufferSize() {
		return internalCacheBufferSize;
	}

	/**
	 * @return the sendInputStreamBufferSize
	 */
	public int getSendInputStreamBufferSize() {
		return sendInputStreamBufferSize;
	}

	/**
	 * @return the localFileOutputStreamBufferSize
	 */
	public int getLocalFileOutputStreamBufferSize() {
		return localFileOutputStreamBufferSize;
	}

	/**
	 * @return the irodsSocketTimeout
	 */
	public int getIrodsSocketTimeout() {
		return irodsSocketTimeout;
	}

	/**
	 * @return the irodsParallelSocketTimeout
	 */
	public int getIrodsParallelSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

	/**
	 * @return the defaultEncoding
	 */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/**
	 * @return the inputToOutputCopyBufferByteSize
	 */
	public int getInputToOutputCopyBufferByteSize() {
		return inputToOutputCopyBufferByteSize;
	}

	/**
	 * @return the localFileInputStreamBufferSize
	 */
	public int getLocalFileInputStreamBufferSize() {
		return localFileInputStreamBufferSize;
	}

	/**
	 * @return <code>boolean</code> indicates whether to reconnect to avoid some
	 *         firewall issues. This is equivalent to the -T option on the
	 *         put/get operations in iCommands
	 */
	public boolean isReconnect() {
		return reconnect;
	}

	/**
	 * @return <code>boolean</code> indicates whether to incorporate detailed
	 *         statistics in the DEBUG log regarding performance metrics, useful
	 *         for tuning and optimization, with the potential to add overhead,
	 *         so typically not suitable for production
	 */
	public boolean isInstrument() {
		return instrument;
	}

	/**
	 * @return the reconnectTimeInMillis
	 */
	public synchronized long getReconnectTimeInMillis() {
		return reconnectTimeInMillis;
	}

	/**
	 * @return the forcePamFlush
	 */
	synchronized boolean isForcePamFlush() {
		return forcePamFlush;
	}

}
