/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Immutable object represents the options controlling the behavior of the io
 * pipeline. Typically, these options are built based on the current state of
 * the {@link JargonProperties} at the time a connection is created.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PipelineConfiguration {

	private final int irodsSocketTimeout;
	private final int irodsParallelSocketTimeout;
	private int internalInputStreamBufferSize;
	private int internalOutputStreamBufferSize;
	private int internalCacheBufferSize;
	private int sendInputStreamBufferSize;
	private int localFileOutputStreamBufferSize;

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
		this.localFileOutputStreamBufferSize = jargonProperties
				.getLocalFileOutputStreamBufferSize();

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
	public synchronized int getInternalCacheBufferSize() {
		return internalCacheBufferSize;
	}

	
	/**
	 * @return the sendInputStreamBufferSize
	 */
	public synchronized int getSendInputStreamBufferSize() {
		return sendInputStreamBufferSize;
	}

	/**
	 * @return the localFileOutputStreamBufferSize
	 */
	public synchronized int getLocalFileOutputStreamBufferSize() {
		return localFileOutputStreamBufferSize;
	}

	/**
	 * @return the irodsSocketTimeout
	 */
	public synchronized int getIrodsSocketTimeout() {
		return irodsSocketTimeout;
	}

	/**
	 * @return the irodsParallelSocketTimeout
	 */
	public synchronized int getIrodsParallelSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

}
