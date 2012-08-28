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
	private final String defaultEncoding = "utf-8"; // FIXME: put into
													// jargon.properties and
													// propogate
	private final int inputToOutputCopyBufferByteSize;
	private final boolean reconnect;
	private final long reconnectTimeInMillis;
	private final boolean instrument;

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

		this.irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		this.irodsParallelSocketTimeout = jargonProperties
				.getIRODSParallelTransferSocketTimeout();
		this.internalInputStreamBufferSize = jargonProperties
				.getInternalInputStreamBufferSize();
		this.internalOutputStreamBufferSize = jargonProperties
				.getInternalOutputStreamBufferSize();
		this.internalCacheBufferSize = jargonProperties
				.getInternalCacheBufferSize();
		this.sendInputStreamBufferSize = jargonProperties
				.getSendInputStreamBufferSize();
		this.localFileInputStreamBufferSize = jargonProperties
				.getLocalFileInputStreamBufferSize();
		this.localFileOutputStreamBufferSize = jargonProperties
				.getLocalFileOutputStreamBufferSize();
		this.inputToOutputCopyBufferByteSize = jargonProperties
				.getInputToOutputCopyBufferByteSize();
		this.instrument = jargonProperties.isInstrument();
		this.reconnect = jargonProperties.isReconnect();
		this.reconnectTimeInMillis = jargonProperties
				.getReconnectTimeInMillis();
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

}
