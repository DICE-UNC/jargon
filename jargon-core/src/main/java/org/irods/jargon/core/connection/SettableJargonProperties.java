/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Implementation of the <code>JargonProperties</code> interface that is
 * suitable for user-definition and injection into the <code>IRODSession</code>.
 * Typically, properties that control Jargon are pulled from a default
 * jargon.properties file. This class would allow, for example, the wiring of
 * property options via Spring through various setters.
 * <p/>
 * Some of these properties serve as defaults that may be overridden in the
 * various methods by the setting of parameters, such as
 * <code>TransferOptions</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SettableJargonProperties implements JargonProperties {

	private boolean useParallelTransfer = true;
	private int maxParallelThreads = 4;
	private int maxFilesAndDirsQueryMax = 5000;
	private boolean useTransferThreadsPool = false;
	private int transferThreadPoolMaxSimultaneousTransfers = 4;
	private int transferThreadPoolTimeoutMillis = 60000;
	private boolean allowPutGetResourceRedirects = false;
	private boolean computeChecksumAfterTransfer = false;
	private boolean computeAndVerifyChecksumAfterTransfer = false;
	private boolean intraFileStatusCallbacks = false;
	private int irodsSocketTimeout = 0;
	private int irodsParallelSocketTimeout = 0;
	private int internalInputStreamBufferSize = 0;
	private int internalOutputStreamBufferSize = 0;
	private int internalCacheBufferSize = 0;
	private int sendInputStreamBufferSize = 0;
	private int localFileOutputStreamBufferSize = 0;

	/**
	 * Construct a default properties set based on the provided initial set of
	 * <code>JargonProperties</code>. This can be used to wire in properties via
	 * configuration, as in Spring.
	 * 
	 * @param jargonProperties
	 *            {@link JargonProperties} that has the initial set of
	 *            properties.
	 */
	public SettableJargonProperties(final JargonProperties jargonProperties) {
		initialize(jargonProperties);
	}

	/**
	 * Construct a default properties set based on the
	 * <code>jargon.properties</code> in jargon, these can then be overridden.
	 * 
	 * @throws JargonException
	 *             if properties cannot be loaded
	 */
	public SettableJargonProperties() throws JargonException {
		JargonProperties jargonProperties = new DefaultPropertiesJargonConfig();
		initialize(jargonProperties);
	}

	private void initialize(final JargonProperties jargonProperties) {

		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		this.useParallelTransfer = jargonProperties.isUseParallelTransfer();
		this.maxFilesAndDirsQueryMax = jargonProperties
				.getMaxFilesAndDirsQueryMax();
		this.allowPutGetResourceRedirects = jargonProperties
				.isAllowPutGetResourceRedirects();
		this.computeAndVerifyChecksumAfterTransfer = jargonProperties
				.isComputeAndVerifyChecksumAfterTransfer();
		this.computeChecksumAfterTransfer = jargonProperties
				.isComputeChecksumAfterTransfer();
		this.intraFileStatusCallbacks = jargonProperties
				.isIntraFileStatusCallbacks();
		this.irodsParallelSocketTimeout = jargonProperties
				.getIRODSParallelTransferSocketTimeout();
		this.irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		this.maxParallelThreads = jargonProperties.getMaxParallelThreads();
		this.transferThreadPoolTimeoutMillis = jargonProperties
				.getTransferThreadPoolTimeoutMillis();
		this.transferThreadPoolMaxSimultaneousTransfers = jargonProperties.getTransferThreadPoolMaxSimultaneousTransfers();
		this.internalInputStreamBufferSize = jargonProperties.getInternalInputStreamBufferSize();
		this.internalOutputStreamBufferSize = jargonProperties.getInternalOutputStreamBufferSize();
		this.internalCacheBufferSize =jargonProperties.getInternalCacheBufferSize();
		this.sendInputStreamBufferSize = jargonProperties.getSendInputStreamBufferSize();
		this.localFileOutputStreamBufferSize = jargonProperties.getLocalFileOutputStreamBufferSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#isUseParallelTransfer()
	 */
	@Override
	public synchronized boolean isUseParallelTransfer() {
		return useParallelTransfer;
	}

	public synchronized void setUseParallelTransfer(
			final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	public synchronized void setMaxParallelThreads(final int maxParallelThreads) {
		this.maxParallelThreads = maxParallelThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#getMaxParallelThreads()
	 */
	@Override
	public synchronized int getMaxParallelThreads() {
		return maxParallelThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getMaxFilesAndDirsQueryMax
	 * ()
	 */
	@Override
	public synchronized int getMaxFilesAndDirsQueryMax() {
		return maxFilesAndDirsQueryMax;
	}

	@Override
	public synchronized boolean isUseTransferThreadsPool() {
		return useTransferThreadsPool;
	}

	@Override
	public synchronized int getTransferThreadPoolTimeoutMillis() {
		return transferThreadPoolTimeoutMillis;
	}

	/**
	 * @param maxFilesAndDirsQueryMax
	 *            the maxFilesAndDirsQueryMax to set
	 */
	public synchronized void setMaxFilesAndDirsQueryMax(
			final int maxFilesAndDirsQueryMax) {
		this.maxFilesAndDirsQueryMax = maxFilesAndDirsQueryMax;
	}

	/**
	 * @param useTransferThreadsPool
	 *            the useTransferThreadsPool to set
	 */
	public synchronized void setUseTransferThreadsPool(
			final boolean useTransferThreadsPool) {
		this.useTransferThreadsPool = useTransferThreadsPool;
	}

	/**
	 * @param transferThreadPoolTimeoutMillis
	 *            the transferThreadPoolTimeoutMillis to set
	 */
	public synchronized void setTransferThreadPoolTimeoutMillis(
			final int transferThreadPoolTimeoutMillis) {
		this.transferThreadPoolTimeoutMillis = transferThreadPoolTimeoutMillis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isAllowPutGetResourceRedirects()
	 */
	@Override
	public synchronized boolean isAllowPutGetResourceRedirects() {
		return allowPutGetResourceRedirects;
	}

	/**
	 * Allow resource redirects to occur
	 * 
	 * @param allowPutGetResourceRedirects
	 *            <code>boolean</code> which allows resource redirects if
	 *            <code>true</code>
	 * @throws JargonException
	 */
	public synchronized void setAllowPutGetResourceRedirects(
			final boolean allowPutGetResourceRedirects) {
		this.allowPutGetResourceRedirects = allowPutGetResourceRedirects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isComputeChecksumAfterTransfer()
	 */
	@Override
	public synchronized boolean isComputeChecksumAfterTransfer() {
		return computeChecksumAfterTransfer;
	}

	/**
	 * Compute (but do not verify) a checksum after a transfer.
	 * 
	 * @param computeChecksumAfterTransfer
	 *            <code>boolean</code> that will cause a checksum to be computed
	 *            by default if <code>true</code>
	 * @throws JargonException
	 */
	public synchronized void setComputeChecksumAfterTransfer(
			final boolean computeChecksumAfterTransfer) {
		this.computeChecksumAfterTransfer = computeChecksumAfterTransfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isComputeAndVerifyChecksumAfterTransfer()
	 */
	@Override
	public synchronized boolean isComputeAndVerifyChecksumAfterTransfer() {
		return this.computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * Compute and verify the file checksum after a put/get transfer
	 * 
	 * @param computeAndVerifyChecksumAfterTransfer
	 *            <code>boolean</code> that causes a checksum validation if set
	 *            to <code>true</code>
	 */
	public synchronized void setComputeAndVerifyChecksumAfterTransfer(
			final boolean computeAndVerifyChecksumAfterTransfer) {
		this.computeAndVerifyChecksumAfterTransfer = computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * Set whether intra-file status call-backs for file transfers are enabled.
	 * This will give progress of bytes within transfers, with a slight
	 * performance penalty.
	 * 
	 * @param intraFileStatusCallbacks
	 *            the intraFileStatusCallbacks to set
	 */
	public synchronized void setIntraFileStatusCallbacks(
			final boolean intraFileStatusCallbacks) {
		this.intraFileStatusCallbacks = intraFileStatusCallbacks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#isIntraFileStatusCallbacks
	 * ()
	 */
	@Override
	public synchronized boolean isIntraFileStatusCallbacks() {
		return intraFileStatusCallbacks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getIRODSSocketTimeout()
	 */
	@Override
	public synchronized int getIRODSSocketTimeout() {
		return irodsSocketTimeout;
	}
	
	public synchronized void setIRODSSocketTimeout(final int irodsSocketTimeout) {
		this.irodsSocketTimeout = irodsSocketTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getIRODSParallelTransferSocketTimeout()
	 */
	@Override
	public synchronized int getIRODSParallelTransferSocketTimeout() {
		return irodsParallelSocketTimeout;
	}
	
	public synchronized void setIRODSParallelTransferSocketTimeout(int irodsParallelSocketTimeout) {
		this.irodsParallelSocketTimeout = irodsParallelSocketTimeout;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadPoolMaxSimultaneousTransfers()
	 */
	@Override
	public synchronized int getTransferThreadPoolMaxSimultaneousTransfers() {
		return transferThreadPoolMaxSimultaneousTransfers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInternalInputStreamBufferSize()
	 */
	@Override
	public synchronized int getInternalInputStreamBufferSize() {
		return internalInputStreamBufferSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInternalOutputStreamBufferSize()
	 */
	@Override
	public synchronized int getInternalOutputStreamBufferSize() {
		return internalOutputStreamBufferSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getInternalCacheBufferSize
	 * ()
	 */
	@Override
	public synchronized int getInternalCacheBufferSize() {
		return internalCacheBufferSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getSendInputStreamBufferSize()
	 */
	@Override
	public synchronized int getSendInputStreamBufferSize() {
		return sendInputStreamBufferSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInputToOutputCopyBufferByteSize()
	 */
	@Override
	public synchronized int getInputToOutputCopyBufferByteSize() {
		return getInputToOutputCopyBufferByteSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getLocalFileOutputStreamBufferSize()
	 */
	@Override
	public synchronized int getLocalFileOutputStreamBufferSize() {
		return localFileOutputStreamBufferSize;
	}

	/**
	 * @param transferThreadPoolMaxSimultaneousTransfers
	 *            the transferThreadPoolMaxSimultaneousTransfers to set
	 */
	public synchronized void setTransferThreadPoolMaxSimultaneousTransfers(
			final int transferThreadPoolMaxSimultaneousTransfers) {
		this.transferThreadPoolMaxSimultaneousTransfers = transferThreadPoolMaxSimultaneousTransfers;
	}

	/**
	 * @param internalInputStreamBufferSize
	 *            the internalInputStreamBufferSize to set
	 */
	public synchronized void setInternalInputStreamBufferSize(
			final int internalInputStreamBufferSize) {
		this.internalInputStreamBufferSize = internalInputStreamBufferSize;
	}

	/**
	 * @param internalOutputStreamBufferSize
	 *            the internalOutputStreamBufferSize to set
	 */
	public synchronized void setInternalOutputStreamBufferSize(
			final int internalOutputStreamBufferSize) {
		this.internalOutputStreamBufferSize = internalOutputStreamBufferSize;
	}

	/**
	 * @param internalCacheBufferSize
	 *            the internalCacheBufferSize to set
	 */
	public synchronized void setInternalCacheBufferSize(
			final int internalCacheBufferSize) {
		this.internalCacheBufferSize = internalCacheBufferSize;
	}

	/**
	 * @param sendInputStreamBufferSize
	 *            the sendInputStreamBufferSize to set
	 */
	public synchronized void setSendInputStreamBufferSize(
			final int sendInputStreamBufferSize) {
		this.sendInputStreamBufferSize = sendInputStreamBufferSize;
	}

	/**
	 * @param localFileOutputStreamBufferSize
	 *            the localFileOutputStreamBufferSize to set
	 */
	public synchronized void setLocalFileOutputStreamBufferSize(
			final int localFileOutputStreamBufferSize) {
		this.localFileOutputStreamBufferSize = localFileOutputStreamBufferSize;
	}

}
