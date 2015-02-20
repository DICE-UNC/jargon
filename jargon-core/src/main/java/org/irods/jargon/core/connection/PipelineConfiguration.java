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
	private final boolean parallelTcpKeepAlive;
	private final int parallelTcpSendWindowSize;
	private final int parallelTcpReceiveWindowSize;
	private final int parallelTcpPerformancePrefsConnectionTime;
	private final int parallelTcpPerformancePrefsLatency;
	private final int parallelTcpPerformancePrefsBandwidth;
	private final boolean primaryTcpKeepAlive;
	private final int primaryTcpSendWindowSize;
	private final int primaryTcpReceiveWindowSize;
	private final int primaryTcpPerformancePrefsConnectionTime;
	private final int primaryTcpPerformancePrefsLatency;
	private final int primaryTcpPerformancePrefsBandwidth;

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

		this.parallelTcpKeepAlive = jargonProperties.isParallelTcpKeepAlive();
		this.parallelTcpPerformancePrefsBandwidth = jargonProperties
				.getParallelTcpPerformancePrefsBandwidth();
		this.parallelTcpPerformancePrefsConnectionTime = jargonProperties
				.getParallelTcpPerformancePrefsConnectionTime();
		this.parallelTcpPerformancePrefsLatency = jargonProperties
				.getParallelTcpPerformancePrefsLatency();
		this.parallelTcpReceiveWindowSize = jargonProperties
				.getParallelTcpReceiveWindowSize();
		this.parallelTcpSendWindowSize = jargonProperties
				.getParallelTcpSendWindowSize();

		this.primaryTcpKeepAlive = jargonProperties.isPrimaryTcpKeepAlive();
		this.primaryTcpPerformancePrefsBandwidth = jargonProperties
				.getPrimaryTcpPerformancePrefsBandwidth();
		this.primaryTcpPerformancePrefsConnectionTime = jargonProperties
				.getPrimaryTcpPerformancePrefsConnectionTime();
		this.primaryTcpPerformancePrefsLatency = jargonProperties
				.getPrimaryTcpPerformancePrefsLatency();
		this.primaryTcpReceiveWindowSize = jargonProperties
				.getPrimaryTcpReceiveWindowSize();
		this.primaryTcpSendWindowSize = jargonProperties
				.getPrimaryTcpSendWindowSize();

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

	public boolean isParallelTcpKeepAlive() {
		return parallelTcpKeepAlive;
	}

	public int getParallelTcpSendWindowSize() {
		return parallelTcpSendWindowSize;
	}

	public int getParallelTcpReceiveWindowSize() {
		return parallelTcpReceiveWindowSize;
	}

	public int getParallelTcpPerformancePrefsConnectionTime() {
		return parallelTcpPerformancePrefsConnectionTime;
	}

	public int getParallelTcpPerformancePrefsLatency() {
		return parallelTcpPerformancePrefsLatency;
	}

	public int getParallelTcpPerformancePrefsBandwidth() {
		return parallelTcpPerformancePrefsBandwidth;
	}

	public boolean isPrimaryTcpKeepAlive() {
		return primaryTcpKeepAlive;
	}

	public int getPrimaryTcpSendWindowSize() {
		return primaryTcpSendWindowSize;
	}

	public int getPrimaryTcpReceiveWindowSize() {
		return primaryTcpReceiveWindowSize;
	}

	public int getPrimaryTcpPerformancePrefsConnectionTime() {
		return primaryTcpPerformancePrefsConnectionTime;
	}

	public int getPrimaryTcpPerformancePrefsLatency() {
		return primaryTcpPerformancePrefsLatency;
	}

	public int getPrimaryTcpPerformancePrefsBandwidth() {
		return primaryTcpPerformancePrefsBandwidth;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PipelineConfiguration [irodsSocketTimeout=");
		builder.append(irodsSocketTimeout);
		builder.append(", irodsParallelSocketTimeout=");
		builder.append(irodsParallelSocketTimeout);
		builder.append(", internalInputStreamBufferSize=");
		builder.append(internalInputStreamBufferSize);
		builder.append(", internalOutputStreamBufferSize=");
		builder.append(internalOutputStreamBufferSize);
		builder.append(", internalCacheBufferSize=");
		builder.append(internalCacheBufferSize);
		builder.append(", sendInputStreamBufferSize=");
		builder.append(sendInputStreamBufferSize);
		builder.append(", localFileInputStreamBufferSize=");
		builder.append(localFileInputStreamBufferSize);
		builder.append(", localFileOutputStreamBufferSize=");
		builder.append(localFileOutputStreamBufferSize);
		builder.append(", ");
		if (defaultEncoding != null) {
			builder.append("defaultEncoding=");
			builder.append(defaultEncoding);
			builder.append(", ");
		}
		builder.append("inputToOutputCopyBufferByteSize=");
		builder.append(inputToOutputCopyBufferByteSize);
		builder.append(", reconnect=");
		builder.append(reconnect);
		builder.append(", reconnectTimeInMillis=");
		builder.append(reconnectTimeInMillis);
		builder.append(", instrument=");
		builder.append(instrument);
		builder.append(", forcePamFlush=");
		builder.append(forcePamFlush);
		builder.append(", parallelTcpKeepAlive=");
		builder.append(parallelTcpKeepAlive);
		builder.append(", parallelTcpSendWindowSize=");
		builder.append(parallelTcpSendWindowSize);
		builder.append(", parallelTcpReceiveWindowSize=");
		builder.append(parallelTcpReceiveWindowSize);
		builder.append(", parallelTcpPerformancePrefsConnectionTime=");
		builder.append(parallelTcpPerformancePrefsConnectionTime);
		builder.append(", parallelTcpPerformancePrefsLatency=");
		builder.append(parallelTcpPerformancePrefsLatency);
		builder.append(", parallelTcpPerformancePrefsBandwidth=");
		builder.append(parallelTcpPerformancePrefsBandwidth);
		builder.append(", primaryTcpKeepAlive=");
		builder.append(primaryTcpKeepAlive);
		builder.append(", primaryTcpSendWindowSize=");
		builder.append(primaryTcpSendWindowSize);
		builder.append(", primaryTcpReceiveWindowSize=");
		builder.append(primaryTcpReceiveWindowSize);
		builder.append(", primaryTcpPerformancePrefsConnectionTime=");
		builder.append(primaryTcpPerformancePrefsConnectionTime);
		builder.append(", primaryTcpPerformancePrefsLatency=");
		builder.append(primaryTcpPerformancePrefsLatency);
		builder.append(", primaryTcpPerformancePrefsBandwidth=");
		builder.append(primaryTcpPerformancePrefsBandwidth);
		builder.append("]");
		return builder.toString();
	}

}
