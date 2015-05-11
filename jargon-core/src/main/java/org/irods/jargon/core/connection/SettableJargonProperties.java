package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

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
	private final boolean useNIOForParallelTransfers = false;
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
	private String connectionFactory = "tcp";
	private ChecksumEncodingEnum checksumEncoding = ChecksumEncodingEnum.DEFAULT;
	private boolean parallelTcpKeepAlive;
	private int parallelTcpSendWindowSize;
	private int parallelTcpReceiveWindowSize;
	private int parallelTcpPerformancePrefsConnectionTime;
	private int parallelTcpPerformancePrefsLatency;
	private int parallelTcpPerformancePrefsBandwidth;
	private boolean primaryTcpKeepAlive;
	private int primaryTcpSendWindowSize;
	private int primaryTcpReceiveWindowSize;
	private int primaryTcpPerformancePrefsConnectionTime;
	private int primaryTcpPerformancePrefsLatency;
	private int primaryTcpPerformancePrefsBandwidth;
	private int socketRenewalIntervalInSeconds;
	private boolean longTransferRestart = true;
	/**
	 * Size (in bytes) of the buffer used to copy between input and output for
	 * parallel transfers
	 */
	private int parallelCopyBufferSize;
	/**
	 * Number of callbacks before an intra file callback listener will be
	 * notified, no matter how many bytes passed
	 */
	private int intraFileStatusCallbacksNumberCallsInterval = 5;
	/**
	 * Number of bytes in a callback before in intra file callback listener will
	 * be notified, no matter how many calls have been made
	 */
	private long intraFileStatusCallbacksTotalBytesInterval = 4194304;

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
		connectionFactory = jargonProperties.getConnectionFactory();
		checksumEncoding = jargonProperties.getChecksumEncoding();

		parallelTcpKeepAlive = jargonProperties.isParallelTcpKeepAlive();
		parallelTcpPerformancePrefsBandwidth = jargonProperties
				.getParallelTcpPerformancePrefsBandwidth();
		parallelTcpPerformancePrefsConnectionTime = jargonProperties
				.getParallelTcpPerformancePrefsConnectionTime();
		parallelTcpPerformancePrefsLatency = jargonProperties
				.getParallelTcpPerformancePrefsLatency();
		parallelTcpReceiveWindowSize = jargonProperties
				.getParallelTcpReceiveWindowSize();
		parallelTcpSendWindowSize = jargonProperties
				.getParallelTcpSendWindowSize();
		primaryTcpKeepAlive = jargonProperties.isPrimaryTcpKeepAlive();
		primaryTcpPerformancePrefsBandwidth = jargonProperties
				.getPrimaryTcpPerformancePrefsBandwidth();
		primaryTcpPerformancePrefsConnectionTime = jargonProperties
				.getPrimaryTcpPerformancePrefsConnectionTime();
		primaryTcpPerformancePrefsLatency = jargonProperties
				.getPrimaryTcpPerformancePrefsLatency();
		primaryTcpReceiveWindowSize = jargonProperties
				.getPrimaryTcpReceiveWindowSize();
		primaryTcpSendWindowSize = jargonProperties
				.getPrimaryTcpSendWindowSize();
		socketRenewalIntervalInSeconds = jargonProperties
				.getSocketRenewalIntervalInSeconds();
		longTransferRestart = jargonProperties.isLongTransferRestart();
		parallelCopyBufferSize = jargonProperties.getParallelCopyBufferSize();
		this.intraFileStatusCallbacksNumberCallsInterval = jargonProperties
				.getIntraFileStatusCallbacksNumberCallsInterval();
		this.intraFileStatusCallbacksTotalBytesInterval = jargonProperties
				.getIntraFileStatusCallbacksTotalBytesInterval();

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

	@Override
	public synchronized String getConnectionFactory() {
		return connectionFactory;
	}

	public synchronized int getPamTimeToLive() {
		return pamTimeToLive;
	}

	public synchronized void setPamTimeToLive(final int pamTimeToLive) {
		this.pamTimeToLive = pamTimeToLive;
	}

	public synchronized void setUsingDiscoveredServerPropertiesCache(
			final boolean usingDiscoveredServerPropertiesCache) {
		this.usingDiscoveredServerPropertiesCache = usingDiscoveredServerPropertiesCache;
	}

	public synchronized void setUsingSpecificQueryForCollectionListingsWithPermissions(
			final boolean usingSpecificQueryForCollectionListingsWithPermissions) {
		this.usingSpecificQueryForCollectionListingsWithPermissions = usingSpecificQueryForCollectionListingsWithPermissions;
	}

	public synchronized void setConnectionFactory(final String connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getChecksumEncoding()
	 */
	@Override
	public synchronized ChecksumEncodingEnum getChecksumEncoding() {
		return checksumEncoding;
	}

	/**
	 * Set the encoding used for computing checksums
	 * 
	 * @param checksumEncoding
	 */
	public synchronized void setChecksumEncoding(
			final ChecksumEncodingEnum checksumEncoding) {
		if (checksumEncoding == null) {
			throw new IllegalArgumentException("null checksumEncoding");
		}

		this.checksumEncoding = checksumEncoding;

	}

	@Override
	public synchronized boolean isParallelTcpKeepAlive() {
		return parallelTcpKeepAlive;
	}

	public synchronized void setParallelTcpKeepAlive(
			final boolean parallelTcpKeepAlive) {
		this.parallelTcpKeepAlive = parallelTcpKeepAlive;
	}

	@Override
	public synchronized int getParallelTcpSendWindowSize() {
		return parallelTcpSendWindowSize;
	}

	public synchronized void setParallelTcpSendWindowSize(
			final int parallelTcpSendWindowSize) {
		this.parallelTcpSendWindowSize = parallelTcpSendWindowSize;
	}

	@Override
	public synchronized int getParallelTcpReceiveWindowSize() {
		return parallelTcpReceiveWindowSize;
	}

	public synchronized void setParallelTcpReceiveWindowSize(
			final int parallelTcpReceiveWindowSize) {
		this.parallelTcpReceiveWindowSize = parallelTcpReceiveWindowSize;
	}

	@Override
	public synchronized int getParallelTcpPerformancePrefsConnectionTime() {
		return parallelTcpPerformancePrefsConnectionTime;
	}

	public synchronized void setParallelTcpPerformancePrefsConnectionTime(
			final int parallelTcpPerformancePrefsConnectionTime) {
		this.parallelTcpPerformancePrefsConnectionTime = parallelTcpPerformancePrefsConnectionTime;
	}

	@Override
	public synchronized int getParallelTcpPerformancePrefsLatency() {
		return parallelTcpPerformancePrefsLatency;
	}

	public synchronized void setParallelTcpPerformancePrefsLatency(
			final int parallelTcpPerformancePrefsLatency) {
		this.parallelTcpPerformancePrefsLatency = parallelTcpPerformancePrefsLatency;
	}

	@Override
	public synchronized int getParallelTcpPerformancePrefsBandwidth() {
		return parallelTcpPerformancePrefsBandwidth;
	}

	public synchronized void setParallelTcpPerformancePrefsBandwidth(
			final int parallelTcpPerformancePrefsBandwidth) {
		this.parallelTcpPerformancePrefsBandwidth = parallelTcpPerformancePrefsBandwidth;
	}

	@Override
	public synchronized boolean isPrimaryTcpKeepAlive() {
		return primaryTcpKeepAlive;
	}

	public synchronized void setPrimaryTcpKeepAlive(
			final boolean primaryTcpKeepAlive) {
		this.primaryTcpKeepAlive = primaryTcpKeepAlive;
	}

	@Override
	public synchronized int getPrimaryTcpSendWindowSize() {
		return primaryTcpSendWindowSize;
	}

	public synchronized void setPrimaryTcpSendWindowSize(
			final int primaryTcpSendWindowSize) {
		this.primaryTcpSendWindowSize = primaryTcpSendWindowSize;
	}

	@Override
	public synchronized int getPrimaryTcpReceiveWindowSize() {
		return primaryTcpReceiveWindowSize;
	}

	public synchronized void setPrimaryTcpReceiveWindowSize(
			final int primaryTcpReceiveWindowSize) {
		this.primaryTcpReceiveWindowSize = primaryTcpReceiveWindowSize;
	}

	@Override
	public synchronized int getPrimaryTcpPerformancePrefsConnectionTime() {
		return primaryTcpPerformancePrefsConnectionTime;
	}

	public synchronized void setPrimaryTcpPerformancePrefsConnectionTime(
			final int primaryTcpPerformancePrefsConnectionTime) {
		this.primaryTcpPerformancePrefsConnectionTime = primaryTcpPerformancePrefsConnectionTime;
	}

	@Override
	public synchronized int getPrimaryTcpPerformancePrefsLatency() {
		return primaryTcpPerformancePrefsLatency;
	}

	public synchronized void setPrimaryTcpPerformancePrefsLatency(
			final int primaryTcpPerformancePrefsLatency) {
		this.primaryTcpPerformancePrefsLatency = primaryTcpPerformancePrefsLatency;
	}

	@Override
	public synchronized int getPrimaryTcpPerformancePrefsBandwidth() {
		return primaryTcpPerformancePrefsBandwidth;
	}

	public synchronized void setPrimaryTcpPerformancePrefsBandwidth(
			final int primaryTcpPerformancePrefsBandwidth) {
		this.primaryTcpPerformancePrefsBandwidth = primaryTcpPerformancePrefsBandwidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SettableJargonProperties [useParallelTransfer=");
		builder.append(useParallelTransfer);
		builder.append(", useNIOForParallelTransfers=");
		builder.append(useNIOForParallelTransfers);
		builder.append(", maxParallelThreads=");
		builder.append(maxParallelThreads);
		builder.append(", maxFilesAndDirsQueryMax=");
		builder.append(maxFilesAndDirsQueryMax);
		builder.append(", useTransferThreadsPool=");
		builder.append(useTransferThreadsPool);
		builder.append(", transferThreadPoolMaxSimultaneousTransfers=");
		builder.append(transferThreadPoolMaxSimultaneousTransfers);
		builder.append(", transferThreadPoolTimeoutMillis=");
		builder.append(transferThreadPoolTimeoutMillis);
		builder.append(", allowPutGetResourceRedirects=");
		builder.append(allowPutGetResourceRedirects);
		builder.append(", computeChecksumAfterTransfer=");
		builder.append(computeChecksumAfterTransfer);
		builder.append(", computeAndVerifyChecksumAfterTransfer=");
		builder.append(computeAndVerifyChecksumAfterTransfer);
		builder.append(", intraFileStatusCallbacks=");
		builder.append(intraFileStatusCallbacks);
		builder.append(", irodsSocketTimeout=");
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
		builder.append(", localFileOutputStreamBufferSize=");
		builder.append(localFileOutputStreamBufferSize);
		builder.append(", localFileInputStreamBufferSize=");
		builder.append(localFileInputStreamBufferSize);
		builder.append(", putBufferSize=");
		builder.append(putBufferSize);
		builder.append(", getBufferSize=");
		builder.append(getBufferSize);
		builder.append(", inputToOutputCopyBufferByteSize=");
		builder.append(inputToOutputCopyBufferByteSize);
		builder.append(", ");
		if (encoding != null) {
			builder.append("encoding=");
			builder.append(encoding);
			builder.append(", ");
		}
		builder.append("instrument=");
		builder.append(instrument);
		builder.append(", reconnect=");
		builder.append(reconnect);
		builder.append(", defaultToPublicIfNothingUnderRootWhenListing=");
		builder.append(defaultToPublicIfNothingUnderRootWhenListing);
		builder.append(", reconnectTimeInMillis=");
		builder.append(reconnectTimeInMillis);
		builder.append(", usingDiscoveredServerPropertiesCache=");
		builder.append(usingDiscoveredServerPropertiesCache);
		builder.append(", usingSpecificQueryForCollectionListingsWithPermissions=");
		builder.append(usingSpecificQueryForCollectionListingsWithPermissions);
		builder.append(", usingSpecQueryForDataObjPermissionsForUserInGroup=");
		builder.append(usingSpecQueryForDataObjPermissionsForUserInGroup);
		builder.append(", pamTimeToLive=");
		builder.append(pamTimeToLive);
		builder.append(", forcePamFlush=");
		builder.append(forcePamFlush);
		builder.append(", ");
		if (connectionFactory != null) {
			builder.append("connectionFactory=");
			builder.append(connectionFactory);
			builder.append(", ");
		}
		if (checksumEncoding != null) {
			builder.append("checksumEncoding=");
			builder.append(checksumEncoding);
			builder.append(", ");
		}
		builder.append("parallelTcpKeepAlive=");
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
		builder.append(", socketRenewalIntervalInSeconds=");
		builder.append(socketRenewalIntervalInSeconds);
		builder.append(", longTransferRestart=");
		builder.append(longTransferRestart);
		builder.append(", parallelCopyBufferSize=");
		builder.append(parallelCopyBufferSize);
		builder.append(", intraFileStatusCallbacksNumberCallsInterval=");
		builder.append(intraFileStatusCallbacksNumberCallsInterval);
		builder.append(", intraFileStatusCallbacksTotalBytesInterval=");
		builder.append(intraFileStatusCallbacksTotalBytesInterval);
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getSocketRenewalIntervalInSeconds()
	 */
	@Override
	public synchronized int getSocketRenewalIntervalInSeconds() {
		return socketRenewalIntervalInSeconds;
	}

	/**
	 * Set the interval in seconds to renew a socket during long transfers. Set
	 * to 0 to turn this behavior off.
	 * 
	 * @param socketRenewalIntervalInSeconds
	 */
	public synchronized void setSocketRenewalIntervalInSeconds(
			final int socketRenewalIntervalInSeconds) {
		this.socketRenewalIntervalInSeconds = socketRenewalIntervalInSeconds;
	}

	@Override
	public synchronized boolean isLongTransferRestart() {
		return longTransferRestart;
	}

	/**
	 * Sets the ability to restart long file transfers if needed
	 * 
	 * @param longFileTransferRestart
	 */
	public synchronized void setLongTransferRestart(
			final boolean longFileTransferRestart) {
		longTransferRestart = longFileTransferRestart;
	}

	@Override
	public synchronized int getParallelCopyBufferSize() {
		return parallelCopyBufferSize;
	}

	/**
	 * Set the size (in bytes) of the copy buffer used between streams in
	 * parallel transfer
	 * 
	 * @param parallelCopyBufferSize
	 */
	public synchronized void setParallelCopyBufferSize(
			final int parallelCopyBufferSize) {
		this.parallelCopyBufferSize = parallelCopyBufferSize;
	}

	@Override
	public synchronized int getIntraFileStatusCallbacksNumberCallsInterval() {
		return intraFileStatusCallbacksNumberCallsInterval;
	}

	public synchronized void setIntraFileStatusCallbacksNumberCallsInterval(
			final int intraFileStatusCallbacksNumberCallsInterval) {
		this.intraFileStatusCallbacksNumberCallsInterval = intraFileStatusCallbacksNumberCallsInterval;
	}

	@Override
	public synchronized long getIntraFileStatusCallbacksTotalBytesInterval() {
		return intraFileStatusCallbacksTotalBytesInterval;
	}

	public synchronized void setIntraFileStatusCallbacksTotalBytesInterval(
			final long intraFileStatusCallbacksTotalBytesInterval) {
		this.intraFileStatusCallbacksTotalBytesInterval = intraFileStatusCallbacksTotalBytesInterval;
	}

}
