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
	private boolean useNIOForParallelTransfers = false;
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
	private int internalOutputStreamBufferSize = -1;
	private int internalCacheBufferSize = 65535;
	private int sendInputStreamBufferSize = 0;
	private int localFileOutputStreamBufferSize = 0;
	private int localFileInputStreamBufferSize = 0;
	private int putBufferSize = 4194304;
	private int getBufferSize = 4194304;
	private int inputToOutputCopyBufferByteSize = 65536;
	private String encoding = "UTF-8";
	private boolean instrument = false;
	private boolean reconnect = false;
	private boolean defaultToPublicIfNothingUnderRootWhenListing = true;
	private long reconnectTimeInMillis = 600000L;
	private boolean usingDiscoveredServerPropertiesCache = true;
	private boolean usingSpecificQueryForCollectionListingsWithPermissions = true;
	private boolean usingSpecQueryForDataObjPermissionsForUserInGroup = false;
	private int pamTimeToLive = 0;
	private boolean forcePamFlush = false;

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

		useParallelTransfer = jargonProperties.isUseParallelTransfer();
		useNIOForParallelTransfers = jargonProperties
				.isUseNIOForParallelTransfers();
		maxFilesAndDirsQueryMax = jargonProperties.getMaxFilesAndDirsQueryMax();
		allowPutGetResourceRedirects = jargonProperties
				.isAllowPutGetResourceRedirects();
		computeAndVerifyChecksumAfterTransfer = jargonProperties
				.isComputeAndVerifyChecksumAfterTransfer();
		computeChecksumAfterTransfer = jargonProperties
				.isComputeChecksumAfterTransfer();
		intraFileStatusCallbacks = jargonProperties
				.isIntraFileStatusCallbacks();
		irodsParallelSocketTimeout = jargonProperties
				.getIRODSParallelTransferSocketTimeout();
		irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		maxParallelThreads = jargonProperties.getMaxParallelThreads();
		transferThreadPoolTimeoutMillis = jargonProperties
				.getTransferThreadPoolTimeoutMillis();
		transferThreadPoolMaxSimultaneousTransfers = jargonProperties
				.getTransferThreadPoolMaxSimultaneousTransfers();
		internalInputStreamBufferSize = jargonProperties
				.getInternalInputStreamBufferSize();
		internalOutputStreamBufferSize = jargonProperties
				.getInternalOutputStreamBufferSize();
		internalCacheBufferSize = jargonProperties.getInternalCacheBufferSize();
		sendInputStreamBufferSize = jargonProperties
				.getSendInputStreamBufferSize();
		localFileOutputStreamBufferSize = jargonProperties
				.getLocalFileOutputStreamBufferSize();
		localFileInputStreamBufferSize = jargonProperties
				.getLocalFileInputStreamBufferSize();
		putBufferSize = jargonProperties.getPutBufferSize();
		getBufferSize = jargonProperties.getGetBufferSize();
		encoding = jargonProperties.getEncoding();
		inputToOutputCopyBufferByteSize = jargonProperties
				.getInputToOutputCopyBufferByteSize();
		setInstrument(jargonProperties.isInstrument());
		setReconnect(jargonProperties.isReconnect());
		setDefaultToPublicIfNothingUnderRootWhenListing(jargonProperties
				.isDefaultToPublicIfNothingUnderRootWhenListing());
		setUsingSpecQueryForDataObjPermissionsForUserInGroup(jargonProperties
				.isUsingSpecQueryForDataObjPermissionsForUserInGroup());
		setForcePamFlush(jargonProperties.isForcePamFlush());
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

	/**
	 * Utilize parallel transfer algorithm for files above the transfer size
	 * 
	 * @param useParallelTransfer
	 *            <code>boolean</code> of <code>true</code> if parallel
	 *            transfers are allowed
	 */
	public synchronized void setUseParallelTransfer(
			final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	/**
	 * Set the maximum number of threads allowed for parallel transfers. 0 means
	 * use iRODS limit.
	 * 
	 * @param maxParallelThreads
	 *            <code>int</code> with the maximum number of threads to use in
	 *            a parallel transfer, with 0 meaning use the iRODS default set
	 *            in rules.
	 */
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
		return computeAndVerifyChecksumAfterTransfer;
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

	public synchronized void setIRODSParallelTransferSocketTimeout(
			final int irodsParallelSocketTimeout) {
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
		return inputToOutputCopyBufferByteSize;
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

	@Override
	public synchronized int getLocalFileInputStreamBufferSize() {
		return localFileInputStreamBufferSize;
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

	/**
	 * @param localFileInputStremBufferSize
	 *            the localFileInputStreamBufferSize to set
	 */
	public synchronized void setLocalFileInputStreamBufferSize(
			final int localFileInputStreamBufferSize) {
		this.localFileInputStreamBufferSize = localFileInputStreamBufferSize;
	}

	/**
	 * @param irodsSocketTimeout
	 *            the irodsSocketTimeout to set
	 */
	public synchronized void setIrodsSocketTimeout(final int irodsSocketTimeout) {
		this.irodsSocketTimeout = irodsSocketTimeout;
	}

	/**
	 * @param irodsParallelSocketTimeout
	 *            the irodsParallelSocketTimeout to set
	 */
	public synchronized void setIrodsParallelSocketTimeout(
			final int irodsParallelSocketTimeout) {
		this.irodsParallelSocketTimeout = irodsParallelSocketTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getPutBufferSize()
	 */
	@Override
	public synchronized int getPutBufferSize() {
		return putBufferSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getGetBufferSize()
	 */
	@Override
	public synchronized int getGetBufferSize() {
		return getBufferSize;
	}

	/**
	 * @param putBufferSize
	 *            the putBufferSize to set
	 */
	public synchronized void setPutBufferSize(final int putBufferSize) {
		this.putBufferSize = putBufferSize;
	}

	/**
	 * @param getBufferSize
	 *            the getBufferSize to set
	 */
	public synchronized void setGetBufferSize(final int getBufferSize) {
		this.getBufferSize = getBufferSize;
	}

	/**
	 * @param inputToOutputCopyBufferByteSize
	 *            the inputToOutputCopyBufferByteSize to set
	 */
	public synchronized void setInputToOutputCopyBufferByteSize(
			final int inputToOutputCopyBufferByteSize) {
		this.inputToOutputCopyBufferByteSize = inputToOutputCopyBufferByteSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getEncoding()
	 */
	@Override
	public synchronized String getEncoding() {
		return encoding;
	}

	public synchronized void setEncoding(final String encoding) {
		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("encoding is null or empty");
		}

		this.encoding = encoding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUseNIOForParallelTransfers()
	 */
	@Override
	public synchronized boolean isUseNIOForParallelTransfers() {
		return useNIOForParallelTransfers;
	}

	/**
	 * @param useNIOForParallelTransfers
	 *            <code>boolean</code> that is set to <code>true</code> if NIO
	 *            should be used for parallel file transfers
	 */
	public synchronized void setUseNIOForParallelTransfers(
			final boolean useNIOForParallelTransfers) {
		this.useNIOForParallelTransfers = useNIOForParallelTransfers;
	}

	/**
	 * @return <code>boolean</code> that indicates whether a reconnect of long
	 *         running connections is done. This is equvalent to the -T icommand
	 *         option
	 */
	@Override
	public synchronized boolean isReconnect() {
		return reconnect;
	}

	/**
	 * Return <code>boolean</code> that indicates whether detailed performance
	 * information is gathered and reported to the DEBUG log. This may introduce
	 * overhead to operations.
	 * <p/>
	 * Note that the implementation of such instrumentation will be an ongoing
	 * process.
	 * 
	 * @return
	 */
	@Override
	public synchronized boolean isInstrument() {
		return instrument;
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

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public synchronized void setInstrument(final boolean instrument) {
		this.instrument = instrument;
	}

	/**
	 * @param reconnect
	 *            the reconnect to set
	 */
	public synchronized void setReconnect(final boolean reconnect) {
		this.reconnect = reconnect;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isDefaultToPublicIfNothingUnderRootWhenListing()
	 */

	@Override
	public synchronized boolean isDefaultToPublicIfNothingUnderRootWhenListing() {
		return defaultToPublicIfNothingUnderRootWhenListing;
	}

	/**
	 * Set a property that will automatically look for /zone/home/public and
	 * /zone/home/username directories in the process of listing.
	 * 
	 * @param defaultToPublicIfNothingUnderRootWhenListing
	 */
	public synchronized void setDefaultToPublicIfNothingUnderRootWhenListing(
			final boolean defaultToPublicIfNothingUnderRootWhenListing) {
		this.defaultToPublicIfNothingUnderRootWhenListing = defaultToPublicIfNothingUnderRootWhenListing;
	}

	/**
	 * @return the reconnectTimeInMillis <code>long</code> indicating the time
	 *         to wait for reconnect. This is only used if
	 *         <code>isReconnect()</code> is <code>true</code>
	 */
	@Override
	public synchronized long getReconnectTimeInMillis() {
		return reconnectTimeInMillis;
	}

	/**
	 * @param reconnectTimeInMillis
	 *            the reconnectTimeInMillis to set
	 */
	public synchronized void setReconnectTimeInMillis(
			final long reconnectTimeInMillis) {
		this.reconnectTimeInMillis = reconnectTimeInMillis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUsingDiscoveredServerPropertiesCache()
	 */
	@Override
	public synchronized boolean isUsingDiscoveredServerPropertiesCache() {
		return usingDiscoveredServerPropertiesCache;
	}

	@Override
	public synchronized boolean isUsingSpecificQueryForCollectionListingsWithPermissions() {
		return usingSpecificQueryForCollectionListingsWithPermissions;
	}

	public synchronized void setUsingSpecificQueryForCollectionListingWithPermissions(
			final boolean useSpecificQuery) {
		usingSpecificQueryForCollectionListingsWithPermissions = useSpecificQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUsingSpecQueryForDataObjPermissionsForUserInGroup()
	 */
	@Override
	public synchronized boolean isUsingSpecQueryForDataObjPermissionsForUserInGroup() {
		return usingSpecQueryForDataObjPermissionsForUserInGroup;
	}

	/**
	 * @param usingSpecQueryForDataObjPermissionsForUserInGroup
	 *            the usingSpecQueryForDataObjPermissionsForUserInGroup to set
	 */
	public synchronized void setUsingSpecQueryForDataObjPermissionsForUserInGroup(
			final boolean usingSpecQueryForDataObjPermissionsForUserInGroup) {
		this.usingSpecQueryForDataObjPermissionsForUserInGroup = usingSpecQueryForDataObjPermissionsForUserInGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getPAMTimeToLive()
	 */
	@Override
	public synchronized int getPAMTimeToLive() {
		return pamTimeToLive;
	}

	/**
	 * Set the pam time to live (in seconds)
	 * 
	 * @param pamTimeToLive
	 *            <code>int</code> with the time to live for pam passwords
	 */
	public synchronized void setPAMTimeToLive(final int pamTimeToLive) {
		this.pamTimeToLive = pamTimeToLive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#isForcePamFlush()
	 */
	@Override
	public synchronized boolean isForcePamFlush() {
		return forcePamFlush;
	}

	public synchronized void setForcePamFlush(final boolean forcePamFlush) {
		this.forcePamFlush = forcePamFlush;
	}

}
