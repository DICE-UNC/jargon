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
	private int transferThreadCorePoolSize = 0;
	private int transferThreadMaxPoolSize = 16;
	private int transferThreadPoolTimeoutMillis = 60000;
	private boolean allowPutGetResourceRedirects = false;
	private boolean computeChecksumAfterTransfer = false;
	private boolean computeAndVerifyChecksumAfterTransfer = false;
	private boolean intraFileStatusCallbacks = false;
	private int irodsSocketTimeout = 0;
	private int irodsParallelSocketTimeout = 0;
	
	/**
	 * Construct a default properties set based on the provided initial set of <code>JargonProperties</code>.
	 * This can be used to wire in properties via configuration, as in Spring.
	 * @param jargonProperties {@link JargonProperties} that has the initial set of properties.
	 */
	public SettableJargonProperties(final JargonProperties jargonProperties) {
		initialize(jargonProperties);
	}
	
	/**
	 * Construct a default properties set based on the <code>jargon.properties</code> in jargon, these can
	 * then be overridden.
	 * @throws JargonException if properties cannot be loaded
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
		this.maxFilesAndDirsQueryMax = jargonProperties.getMaxFilesAndDirsQueryMax();
		this.allowPutGetResourceRedirects = jargonProperties.isAllowPutGetResourceRedirects();
		this.computeAndVerifyChecksumAfterTransfer = jargonProperties.isComputeAndVerifyChecksumAfterTransfer();
		this.computeChecksumAfterTransfer = jargonProperties.isComputeChecksumAfterTransfer();
		this.intraFileStatusCallbacks = jargonProperties.isIntraFileStatusCallbacks();
		this.irodsParallelSocketTimeout = jargonProperties.getIRODSParallelTransferSocketTimeout();
		this.irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		this.maxParallelThreads = jargonProperties.getMaxParallelThreads();
		this.transferThreadCorePoolSize = jargonProperties.getTransferThreadCorePoolSize();
		this.transferThreadMaxPoolSize = jargonProperties.getTransferThreadMaxPoolSize();
		this.transferThreadPoolTimeoutMillis = jargonProperties.getTransferThreadPoolTimeoutMillis();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#isUseParallelTransfer()
	 */
	@Override
	public boolean isUseParallelTransfer() {
		return useParallelTransfer;
	}

	public void setUseParallelTransfer(final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	public void setMaxParallelThreads(final int maxParallelThreads) {
		this.maxParallelThreads = maxParallelThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#getMaxParallelThreads()
	 */
	@Override
	public int getMaxParallelThreads() {
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
	public int getMaxFilesAndDirsQueryMax(){
		return maxFilesAndDirsQueryMax;
	}

	@Override
	public boolean isUseTransferThreadsPool()  {
		return useTransferThreadsPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadCorePoolSize()
	 */
	@Override
	public int getTransferThreadCorePoolSize() {
		return transferThreadCorePoolSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadMaxPoolSize()
	 */
	@Override
	public int getTransferThreadMaxPoolSize() {
		return transferThreadMaxPoolSize;
	}

	@Override
	public int getTransferThreadPoolTimeoutMillis()  {
		return transferThreadPoolTimeoutMillis;
	}

	/**
	 * @param maxFilesAndDirsQueryMax
	 *            the maxFilesAndDirsQueryMax to set
	 */
	public void setMaxFilesAndDirsQueryMax(final int maxFilesAndDirsQueryMax) {
		this.maxFilesAndDirsQueryMax = maxFilesAndDirsQueryMax;
	}

	/**
	 * @param useTransferThreadsPool
	 *            the useTransferThreadsPool to set
	 */
	public void setUseTransferThreadsPool(final boolean useTransferThreadsPool) {
		this.useTransferThreadsPool = useTransferThreadsPool;
	}

	/**
	 * @param transferThreadCorePoolSize
	 *            the transferThreadCorePoolSize to set
	 */
	public void setTransferThreadCorePoolSize(
			final int transferThreadCorePoolSize) {
		this.transferThreadCorePoolSize = transferThreadCorePoolSize;
	}

	/**
	 * @param transferThreadMaxPoolSize
	 *            the transferThreadMaxPoolSize to set
	 */
	public void setTransferThreadMaxPoolSize(final int transferThreadMaxPoolSize) {
		this.transferThreadMaxPoolSize = transferThreadMaxPoolSize;
	}

	/**
	 * @param transferThreadPoolTimeoutMillis
	 *            the transferThreadPoolTimeoutMillis to set
	 */
	public void setTransferThreadPoolTimeoutMillis(
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
	public boolean isAllowPutGetResourceRedirects() {
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
	public void setAllowPutGetResourceRedirects(
			final boolean allowPutGetResourceRedirects)  {
		this.allowPutGetResourceRedirects = allowPutGetResourceRedirects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isComputeChecksumAfterTransfer()
	 */
	@Override
	public boolean isComputeChecksumAfterTransfer()  {
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
	public void setComputeChecksumAfterTransfer(
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
	public boolean isComputeAndVerifyChecksumAfterTransfer()
			 {
		return this.computeAndVerifyChecksumAfterTransfer;
	}

	/**
	 * Compute and verify the file checksum after a put/get transfer
	 * 
	 * @param computeAndVerifyChecksumAfterTransfer
	 *            <code>boolean</code> that causes a checksum validation if set
	 *            to <code>true</code>
	 */
	public void setComputeAndVerifyChecksumAfterTransfer(
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
	public void setIntraFileStatusCallbacks(
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
	public boolean isIntraFileStatusCallbacks() {
		return intraFileStatusCallbacks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getIRODSSocketTimeout()
	 */
	@Override
	public int getIRODSSocketTimeout() {
		return irodsSocketTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getIRODSParallelTransferSocketTimeout()
	 */
	@Override
	public int getIRODSParallelTransferSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

	/**
	 * @return the irodsSocketTimeout in seconds, or 0 or less if not used
	 */
	public int getIrodsSocketTimeout() {
		return irodsSocketTimeout;
	}

	/**
	 * @param irodsSocketTimeout
	 *            the irodsSocketTimeout in seconds or 0 or less if not used
	 */
	public void setIrodsSocketTimeout(final int irodsSocketTimeout) {
		this.irodsSocketTimeout = irodsSocketTimeout;
	}

	/**
	 * @return the irodsParallelSocketTimeout in seconds or 0 or less if not
	 *         used
	 */
	public int getIrodsParallelSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

	/**
	 * @param irodsParallelSocketTimeout
	 *            the irodsParallelSocketTimeout to set in seconds or 0 or less
	 *            if not used
	 */
	public void setIrodsParallelSocketTimeout(
			final int irodsParallelSocketTimeout) {
		this.irodsParallelSocketTimeout = irodsParallelSocketTimeout;
	}

}
