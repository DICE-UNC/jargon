package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;

/**
 * Implementation of the {@code JargonProperties} interface that is suitable for
 * user-definition and injection into the {@code IRODSession}. Typically,
 * properties that control Jargon are pulled from a default jargon.properties
 * file. This class would allow, for example, the wiring of property options via
 * Spring through various setters.
 * <p>
 * Some of these properties serve as defaults that may be overridden in the
 * various methods by the setting of parameters, such as {@code TransferOptions}
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
	private boolean rulesSetDestinationWhenAuto = true;
	private String defaultIrodsRuleEngineIdentifier = "irods_rule_engine_plugin-irods_rule_language-instance";
	private String defaultPythonRuleEngineIdentifier = "irods_rule_engine_plugin-cpp_default_policy-instance";
	private String defaultCppRuleEngineIdentifier = "irods_rule_engine_plugin-cpp_default_policy-instance";

	/**
	 * Size (in bytes) of the buffer used to copy between input and output for
	 * parallel transfers
	 */
	private int parallelCopyBufferSize;
	/**
	 * Number of callbacks before an intra file callback listener will be notified,
	 * no matter how many bytes passed
	 */
	private int intraFileStatusCallbacksNumberCallsInterval = 5;
	/**
	 * Number of bytes in a callback before in intra file callback listener will be
	 * notified, no matter how many calls have been made
	 */
	private long intraFileStatusCallbacksTotalBytesInterval = 4194304;
	/**
	 * Default SSL negotiation policy, may be overrideen per request in the
	 * IRODSAccount
	 */
	private SslNegotiationPolicy negotiationPolicy = SslNegotiationPolicy.NO_NEGOTIATION;
	/**
	 * Encryption algo for parallel transfers
	 */
	private EncryptionAlgorithmEnum encryptionAlgorithmEnum = EncryptionAlgorithmEnum.AES_256_CBC;
	/**
	 * Key size for encryption of parallel transfers when SSL negotiated
	 */
	private int encryptionKeySize = 32;
	/**
	 * Salt size for encryption of parallel transfers when SSL negotiated
	 */
	private int encryptionSaltSize = 8;

	/**
	 * Number of hash rounds for encryption of parallel transfers when SSL
	 * negotiated
	 */
	private int encryptionNumberHashRounds = 16;

	/**
	 * {@code boolean} that indicates whether ssl cert checks should be bypassed.
	 * {@code false} is the default, meaning checks will be done, and is the
	 * recommended production setting.
	 */
	private boolean bypassSslCertChecks;

	/**
	 * Construct a default properties set based on the provided initial set of
	 * {@code JargonProperties}. This can be used to wire in properties via
	 * configuration, as in Spring.
	 *
	 * @param jargonProperties
	 *            {@link JargonProperties} that has the initial set of properties.
	 */
	public SettableJargonProperties(final JargonProperties jargonProperties) {
		initialize(jargonProperties);
	}

	/**
	 * Construct a default properties set based on the {@code jargon.properties} in
	 * jargon, these can then be overridden.
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
		allowPutGetResourceRedirects = jargonProperties.isAllowPutGetResourceRedirects();
		computeAndVerifyChecksumAfterTransfer = jargonProperties.isComputeAndVerifyChecksumAfterTransfer();
		computeChecksumAfterTransfer = jargonProperties.isComputeChecksumAfterTransfer();
		intraFileStatusCallbacks = jargonProperties.isIntraFileStatusCallbacks();
		irodsParallelSocketTimeout = jargonProperties.getIRODSParallelTransferSocketTimeout();
		irodsSocketTimeout = jargonProperties.getIRODSSocketTimeout();
		maxParallelThreads = jargonProperties.getMaxParallelThreads();
		transferThreadPoolTimeoutMillis = jargonProperties.getTransferThreadPoolTimeoutMillis();
		transferThreadPoolMaxSimultaneousTransfers = jargonProperties.getTransferThreadPoolMaxSimultaneousTransfers();
		internalInputStreamBufferSize = jargonProperties.getInternalInputStreamBufferSize();
		internalOutputStreamBufferSize = jargonProperties.getInternalOutputStreamBufferSize();
		internalCacheBufferSize = jargonProperties.getInternalCacheBufferSize();
		sendInputStreamBufferSize = jargonProperties.getSendInputStreamBufferSize();
		localFileOutputStreamBufferSize = jargonProperties.getLocalFileOutputStreamBufferSize();
		localFileInputStreamBufferSize = jargonProperties.getLocalFileInputStreamBufferSize();
		putBufferSize = jargonProperties.getPutBufferSize();
		getBufferSize = jargonProperties.getGetBufferSize();
		encoding = jargonProperties.getEncoding();
		inputToOutputCopyBufferByteSize = jargonProperties.getInputToOutputCopyBufferByteSize();
		setInstrument(jargonProperties.isInstrument());
		setReconnect(jargonProperties.isReconnect());
		setDefaultToPublicIfNothingUnderRootWhenListing(
				jargonProperties.isDefaultToPublicIfNothingUnderRootWhenListing());
		setUsingSpecQueryForDataObjPermissionsForUserInGroup(
				jargonProperties.isUsingSpecQueryForDataObjPermissionsForUserInGroup());
		setForcePamFlush(jargonProperties.isForcePamFlush());
		connectionFactory = jargonProperties.getConnectionFactory();
		checksumEncoding = jargonProperties.getChecksumEncoding();

		parallelTcpKeepAlive = jargonProperties.isParallelTcpKeepAlive();
		parallelTcpPerformancePrefsBandwidth = jargonProperties.getParallelTcpPerformancePrefsBandwidth();
		parallelTcpPerformancePrefsConnectionTime = jargonProperties.getParallelTcpPerformancePrefsConnectionTime();
		parallelTcpPerformancePrefsLatency = jargonProperties.getParallelTcpPerformancePrefsLatency();
		parallelTcpReceiveWindowSize = jargonProperties.getParallelTcpReceiveWindowSize();
		parallelTcpSendWindowSize = jargonProperties.getParallelTcpSendWindowSize();
		primaryTcpKeepAlive = jargonProperties.isPrimaryTcpKeepAlive();
		primaryTcpPerformancePrefsBandwidth = jargonProperties.getPrimaryTcpPerformancePrefsBandwidth();
		primaryTcpPerformancePrefsConnectionTime = jargonProperties.getPrimaryTcpPerformancePrefsConnectionTime();
		primaryTcpPerformancePrefsLatency = jargonProperties.getPrimaryTcpPerformancePrefsLatency();
		primaryTcpReceiveWindowSize = jargonProperties.getPrimaryTcpReceiveWindowSize();
		primaryTcpSendWindowSize = jargonProperties.getPrimaryTcpSendWindowSize();
		socketRenewalIntervalInSeconds = jargonProperties.getSocketRenewalIntervalInSeconds();
		longTransferRestart = jargonProperties.isLongTransferRestart();
		parallelCopyBufferSize = jargonProperties.getParallelCopyBufferSize();
		intraFileStatusCallbacksNumberCallsInterval = jargonProperties.getIntraFileStatusCallbacksNumberCallsInterval();
		intraFileStatusCallbacksTotalBytesInterval = jargonProperties.getIntraFileStatusCallbacksTotalBytesInterval();
		negotiationPolicy = jargonProperties.getNegotiationPolicy();
		encryptionAlgorithmEnum = jargonProperties.getEncryptionAlgorithmEnum();
		encryptionKeySize = jargonProperties.getEncryptionKeySize();
		encryptionNumberHashRounds = jargonProperties.getEncryptionNumberHashRounds();
		encryptionSaltSize = jargonProperties.getEncryptionSaltSize();
		bypassSslCertChecks = jargonProperties.isBypassSslCertChecks();
		defaultIrodsRuleEngineIdentifier = jargonProperties.getDefaultIrodsRuleEngineIdentifier();
		defaultPythonRuleEngineIdentifier = jargonProperties.getDefaultPythonRuleEngineIdentifier();
		defaultCppRuleEngineIdentifier = jargonProperties.getDefaultCppRuleEngineIdentifier();
		this.rulesSetDestinationWhenAuto = jargonProperties.isRulesSetDestinationWhenAuto();
	}

	@Override
	public synchronized boolean isUseParallelTransfer() {
		return useParallelTransfer;
	}

	public synchronized void setUseParallelTransfer(final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	public synchronized void setMaxParallelThreads(final int maxParallelThreads) {
		this.maxParallelThreads = maxParallelThreads;
	}

	@Override
	public synchronized int getMaxParallelThreads() {
		return maxParallelThreads;
	}

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

	public synchronized void setMaxFilesAndDirsQueryMax(final int maxFilesAndDirsQueryMax) {
		this.maxFilesAndDirsQueryMax = maxFilesAndDirsQueryMax;
	}

	public synchronized void setUseTransferThreadsPool(final boolean useTransferThreadsPool) {
		this.useTransferThreadsPool = useTransferThreadsPool;
	}

	public synchronized void setTransferThreadPoolTimeoutMillis(final int transferThreadPoolTimeoutMillis) {
		this.transferThreadPoolTimeoutMillis = transferThreadPoolTimeoutMillis;
	}

	@Override
	public synchronized boolean isAllowPutGetResourceRedirects() {
		return allowPutGetResourceRedirects;
	}

	public synchronized void setAllowPutGetResourceRedirects(final boolean allowPutGetResourceRedirects) {
		this.allowPutGetResourceRedirects = allowPutGetResourceRedirects;
	}

	@Override
	public synchronized boolean isComputeChecksumAfterTransfer() {
		return computeChecksumAfterTransfer;
	}

	public synchronized void setComputeChecksumAfterTransfer(final boolean computeChecksumAfterTransfer) {
		this.computeChecksumAfterTransfer = computeChecksumAfterTransfer;
	}

	@Override
	public synchronized boolean isComputeAndVerifyChecksumAfterTransfer() {
		return computeAndVerifyChecksumAfterTransfer;
	}

	public synchronized void setComputeAndVerifyChecksumAfterTransfer(
			final boolean computeAndVerifyChecksumAfterTransfer) {
		this.computeAndVerifyChecksumAfterTransfer = computeAndVerifyChecksumAfterTransfer;
	}

	public synchronized void setIntraFileStatusCallbacks(final boolean intraFileStatusCallbacks) {
		this.intraFileStatusCallbacks = intraFileStatusCallbacks;
	}

	@Override
	public synchronized boolean isIntraFileStatusCallbacks() {
		return intraFileStatusCallbacks;
	}

	@Override
	public synchronized int getIRODSSocketTimeout() {
		return irodsSocketTimeout;
	}

	public synchronized void setIRODSSocketTimeout(final int irodsSocketTimeout) {
		this.irodsSocketTimeout = irodsSocketTimeout;
	}

	@Override
	public synchronized int getIRODSParallelTransferSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

	public synchronized void setIRODSParallelTransferSocketTimeout(final int irodsParallelSocketTimeout) {
		this.irodsParallelSocketTimeout = irodsParallelSocketTimeout;
	}

	@Override
	public synchronized int getTransferThreadPoolMaxSimultaneousTransfers() {
		return transferThreadPoolMaxSimultaneousTransfers;
	}

	@Override
	public synchronized int getInternalInputStreamBufferSize() {
		return internalInputStreamBufferSize;
	}

	@Override
	public synchronized int getInternalOutputStreamBufferSize() {
		return internalOutputStreamBufferSize;
	}

	@Override
	public synchronized int getInternalCacheBufferSize() {
		return internalCacheBufferSize;
	}

	@Override
	public synchronized int getSendInputStreamBufferSize() {
		return sendInputStreamBufferSize;
	}

	@Override
	public synchronized int getInputToOutputCopyBufferByteSize() {
		return inputToOutputCopyBufferByteSize;
	}

	@Override
	public synchronized int getLocalFileOutputStreamBufferSize() {
		return localFileOutputStreamBufferSize;
	}

	@Override
	public synchronized int getLocalFileInputStreamBufferSize() {
		return localFileInputStreamBufferSize;
	}

	public synchronized void setTransferThreadPoolMaxSimultaneousTransfers(
			final int transferThreadPoolMaxSimultaneousTransfers) {
		this.transferThreadPoolMaxSimultaneousTransfers = transferThreadPoolMaxSimultaneousTransfers;
	}

	public synchronized void setInternalInputStreamBufferSize(final int internalInputStreamBufferSize) {
		this.internalInputStreamBufferSize = internalInputStreamBufferSize;
	}

	public synchronized void setInternalOutputStreamBufferSize(final int internalOutputStreamBufferSize) {
		this.internalOutputStreamBufferSize = internalOutputStreamBufferSize;
	}

	public synchronized void setInternalCacheBufferSize(final int internalCacheBufferSize) {
		this.internalCacheBufferSize = internalCacheBufferSize;
	}

	public synchronized void setSendInputStreamBufferSize(final int sendInputStreamBufferSize) {
		this.sendInputStreamBufferSize = sendInputStreamBufferSize;
	}

	public synchronized void setLocalFileOutputStreamBufferSize(final int localFileOutputStreamBufferSize) {
		this.localFileOutputStreamBufferSize = localFileOutputStreamBufferSize;
	}

	public synchronized void setLocalFileInputStreamBufferSize(final int localFileInputStreamBufferSize) {
		this.localFileInputStreamBufferSize = localFileInputStreamBufferSize;
	}

	public synchronized void setIrodsSocketTimeout(final int irodsSocketTimeout) {
		this.irodsSocketTimeout = irodsSocketTimeout;
	}

	public synchronized void setIrodsParallelSocketTimeout(final int irodsParallelSocketTimeout) {
		this.irodsParallelSocketTimeout = irodsParallelSocketTimeout;
	}

	@Override
	public synchronized int getPutBufferSize() {
		return putBufferSize;
	}

	@Override
	public synchronized int getGetBufferSize() {
		return getBufferSize;
	}

	public synchronized void setPutBufferSize(final int putBufferSize) {
		this.putBufferSize = putBufferSize;
	}

	public synchronized void setGetBufferSize(final int getBufferSize) {
		this.getBufferSize = getBufferSize;
	}

	public synchronized void setInputToOutputCopyBufferByteSize(final int inputToOutputCopyBufferByteSize) {
		this.inputToOutputCopyBufferByteSize = inputToOutputCopyBufferByteSize;
	}

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

	@Override
	public synchronized boolean isReconnect() {
		return reconnect;
	}

	@Override
	public synchronized boolean isInstrument() {
		return instrument;
	}

	public synchronized int getIrodsSocketTimeout() {
		return irodsSocketTimeout;
	}

	public synchronized int getIrodsParallelSocketTimeout() {
		return irodsParallelSocketTimeout;
	}

	public synchronized void setInstrument(final boolean instrument) {
		this.instrument = instrument;
	}

	public synchronized void setReconnect(final boolean reconnect) {
		this.reconnect = reconnect;
	}

	@Override
	public synchronized boolean isDefaultToPublicIfNothingUnderRootWhenListing() {
		return defaultToPublicIfNothingUnderRootWhenListing;
	}

	public synchronized void setDefaultToPublicIfNothingUnderRootWhenListing(
			final boolean defaultToPublicIfNothingUnderRootWhenListing) {
		this.defaultToPublicIfNothingUnderRootWhenListing = defaultToPublicIfNothingUnderRootWhenListing;
	}

	@Override
	public synchronized long getReconnectTimeInMillis() {
		return reconnectTimeInMillis;
	}

	public synchronized void setReconnectTimeInMillis(final long reconnectTimeInMillis) {
		this.reconnectTimeInMillis = reconnectTimeInMillis;
	}

	@Override
	public synchronized boolean isUsingDiscoveredServerPropertiesCache() {
		return usingDiscoveredServerPropertiesCache;
	}

	@Override
	public synchronized boolean isUsingSpecificQueryForCollectionListingsWithPermissions() {
		return usingSpecificQueryForCollectionListingsWithPermissions;
	}

	public synchronized void setUsingSpecificQueryForCollectionListingWithPermissions(final boolean useSpecificQuery) {
		usingSpecificQueryForCollectionListingsWithPermissions = useSpecificQuery;
	}

	@Override
	public synchronized boolean isUsingSpecQueryForDataObjPermissionsForUserInGroup() {
		return usingSpecQueryForDataObjPermissionsForUserInGroup;
	}

	public synchronized void setUsingSpecQueryForDataObjPermissionsForUserInGroup(
			final boolean usingSpecQueryForDataObjPermissionsForUserInGroup) {
		this.usingSpecQueryForDataObjPermissionsForUserInGroup = usingSpecQueryForDataObjPermissionsForUserInGroup;
	}

	@Override
	public synchronized int getPAMTimeToLive() {
		return pamTimeToLive;
	}

	public synchronized void setPAMTimeToLive(final int pamTimeToLive) {
		this.pamTimeToLive = pamTimeToLive;
	}

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

	@Override
	public synchronized ChecksumEncodingEnum getChecksumEncoding() {
		return checksumEncoding;
	}

	public synchronized void setChecksumEncoding(final ChecksumEncodingEnum checksumEncoding) {
		if (checksumEncoding == null) {
			throw new IllegalArgumentException("null checksumEncoding");
		}

		this.checksumEncoding = checksumEncoding;

	}

	@Override
	public synchronized boolean isParallelTcpKeepAlive() {
		return parallelTcpKeepAlive;
	}

	public synchronized void setParallelTcpKeepAlive(final boolean parallelTcpKeepAlive) {
		this.parallelTcpKeepAlive = parallelTcpKeepAlive;
	}

	@Override
	public synchronized int getParallelTcpSendWindowSize() {
		return parallelTcpSendWindowSize;
	}

	public synchronized void setParallelTcpSendWindowSize(final int parallelTcpSendWindowSize) {
		this.parallelTcpSendWindowSize = parallelTcpSendWindowSize;
	}

	@Override
	public synchronized int getParallelTcpReceiveWindowSize() {
		return parallelTcpReceiveWindowSize;
	}

	public synchronized void setParallelTcpReceiveWindowSize(final int parallelTcpReceiveWindowSize) {
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

	public synchronized void setParallelTcpPerformancePrefsLatency(final int parallelTcpPerformancePrefsLatency) {
		this.parallelTcpPerformancePrefsLatency = parallelTcpPerformancePrefsLatency;
	}

	@Override
	public synchronized int getParallelTcpPerformancePrefsBandwidth() {
		return parallelTcpPerformancePrefsBandwidth;
	}

	public synchronized void setParallelTcpPerformancePrefsBandwidth(final int parallelTcpPerformancePrefsBandwidth) {
		this.parallelTcpPerformancePrefsBandwidth = parallelTcpPerformancePrefsBandwidth;
	}

	@Override
	public synchronized boolean isPrimaryTcpKeepAlive() {
		return primaryTcpKeepAlive;
	}

	public synchronized void setPrimaryTcpKeepAlive(final boolean primaryTcpKeepAlive) {
		this.primaryTcpKeepAlive = primaryTcpKeepAlive;
	}

	@Override
	public synchronized int getPrimaryTcpSendWindowSize() {
		return primaryTcpSendWindowSize;
	}

	public synchronized void setPrimaryTcpSendWindowSize(final int primaryTcpSendWindowSize) {
		this.primaryTcpSendWindowSize = primaryTcpSendWindowSize;
	}

	@Override
	public synchronized int getPrimaryTcpReceiveWindowSize() {
		return primaryTcpReceiveWindowSize;
	}

	public synchronized void setPrimaryTcpReceiveWindowSize(final int primaryTcpReceiveWindowSize) {
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

	public synchronized void setPrimaryTcpPerformancePrefsLatency(final int primaryTcpPerformancePrefsLatency) {
		this.primaryTcpPerformancePrefsLatency = primaryTcpPerformancePrefsLatency;
	}

	@Override
	public synchronized int getPrimaryTcpPerformancePrefsBandwidth() {
		return primaryTcpPerformancePrefsBandwidth;
	}

	public synchronized void setPrimaryTcpPerformancePrefsBandwidth(final int primaryTcpPerformancePrefsBandwidth) {
		this.primaryTcpPerformancePrefsBandwidth = primaryTcpPerformancePrefsBandwidth;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SettableJargonProperties [useParallelTransfer=").append(useParallelTransfer)
				.append(", useNIOForParallelTransfers=").append(useNIOForParallelTransfers)
				.append(", maxParallelThreads=").append(maxParallelThreads).append(", maxFilesAndDirsQueryMax=")
				.append(maxFilesAndDirsQueryMax).append(", useTransferThreadsPool=").append(useTransferThreadsPool)
				.append(", transferThreadPoolMaxSimultaneousTransfers=")
				.append(transferThreadPoolMaxSimultaneousTransfers).append(", transferThreadPoolTimeoutMillis=")
				.append(transferThreadPoolTimeoutMillis).append(", allowPutGetResourceRedirects=")
				.append(allowPutGetResourceRedirects).append(", computeChecksumAfterTransfer=")
				.append(computeChecksumAfterTransfer).append(", computeAndVerifyChecksumAfterTransfer=")
				.append(computeAndVerifyChecksumAfterTransfer).append(", intraFileStatusCallbacks=")
				.append(intraFileStatusCallbacks).append(", irodsSocketTimeout=").append(irodsSocketTimeout)
				.append(", irodsParallelSocketTimeout=").append(irodsParallelSocketTimeout)
				.append(", internalInputStreamBufferSize=").append(internalInputStreamBufferSize)
				.append(", internalOutputStreamBufferSize=").append(internalOutputStreamBufferSize)
				.append(", internalCacheBufferSize=").append(internalCacheBufferSize)
				.append(", sendInputStreamBufferSize=").append(sendInputStreamBufferSize)
				.append(", localFileOutputStreamBufferSize=").append(localFileOutputStreamBufferSize)
				.append(", localFileInputStreamBufferSize=").append(localFileInputStreamBufferSize)
				.append(", putBufferSize=").append(putBufferSize).append(", getBufferSize=").append(getBufferSize)
				.append(", inputToOutputCopyBufferByteSize=").append(inputToOutputCopyBufferByteSize).append(", ");
		if (encoding != null) {
			builder.append("encoding=").append(encoding).append(", ");
		}
		builder.append("instrument=").append(instrument).append(", reconnect=").append(reconnect)
				.append(", defaultToPublicIfNothingUnderRootWhenListing=")
				.append(defaultToPublicIfNothingUnderRootWhenListing).append(", reconnectTimeInMillis=")
				.append(reconnectTimeInMillis).append(", usingDiscoveredServerPropertiesCache=")
				.append(usingDiscoveredServerPropertiesCache)
				.append(", usingSpecificQueryForCollectionListingsWithPermissions=")
				.append(usingSpecificQueryForCollectionListingsWithPermissions)
				.append(", usingSpecQueryForDataObjPermissionsForUserInGroup=")
				.append(usingSpecQueryForDataObjPermissionsForUserInGroup).append(", pamTimeToLive=")
				.append(pamTimeToLive).append(", forcePamFlush=").append(forcePamFlush).append(", ");
		if (connectionFactory != null) {
			builder.append("connectionFactory=").append(connectionFactory).append(", ");
		}
		if (checksumEncoding != null) {
			builder.append("checksumEncoding=").append(checksumEncoding).append(", ");
		}
		builder.append("parallelTcpKeepAlive=").append(parallelTcpKeepAlive).append(", parallelTcpSendWindowSize=")
				.append(parallelTcpSendWindowSize).append(", parallelTcpReceiveWindowSize=")
				.append(parallelTcpReceiveWindowSize).append(", parallelTcpPerformancePrefsConnectionTime=")
				.append(parallelTcpPerformancePrefsConnectionTime).append(", parallelTcpPerformancePrefsLatency=")
				.append(parallelTcpPerformancePrefsLatency).append(", parallelTcpPerformancePrefsBandwidth=")
				.append(parallelTcpPerformancePrefsBandwidth).append(", primaryTcpKeepAlive=")
				.append(primaryTcpKeepAlive).append(", primaryTcpSendWindowSize=").append(primaryTcpSendWindowSize)
				.append(", primaryTcpReceiveWindowSize=").append(primaryTcpReceiveWindowSize)
				.append(", primaryTcpPerformancePrefsConnectionTime=").append(primaryTcpPerformancePrefsConnectionTime)
				.append(", primaryTcpPerformancePrefsLatency=").append(primaryTcpPerformancePrefsLatency)
				.append(", primaryTcpPerformancePrefsBandwidth=").append(primaryTcpPerformancePrefsBandwidth)
				.append(", socketRenewalIntervalInSeconds=").append(socketRenewalIntervalInSeconds)
				.append(", longTransferRestart=").append(longTransferRestart).append(", rulesSetDestinationWhenAuto=")
				.append(rulesSetDestinationWhenAuto).append(", ");
		if (defaultIrodsRuleEngineIdentifier != null) {
			builder.append("defaultIrodsRuleEngineIdentifier=").append(defaultIrodsRuleEngineIdentifier).append(", ");
		}
		if (defaultPythonRuleEngineIdentifier != null) {
			builder.append("defaultPythonRuleEngineIdentifier=").append(defaultPythonRuleEngineIdentifier).append(", ");
		}
		if (defaultCppRuleEngineIdentifier != null) {
			builder.append("defaultCppRuleEngineIdentifier=").append(defaultCppRuleEngineIdentifier).append(", ");
		}
		builder.append("parallelCopyBufferSize=").append(parallelCopyBufferSize)
				.append(", intraFileStatusCallbacksNumberCallsInterval=")
				.append(intraFileStatusCallbacksNumberCallsInterval)
				.append(", intraFileStatusCallbacksTotalBytesInterval=")
				.append(intraFileStatusCallbacksTotalBytesInterval).append(", ");
		if (negotiationPolicy != null) {
			builder.append("negotiationPolicy=").append(negotiationPolicy).append(", ");
		}
		if (encryptionAlgorithmEnum != null) {
			builder.append("encryptionAlgorithmEnum=").append(encryptionAlgorithmEnum).append(", ");
		}
		builder.append("encryptionKeySize=").append(encryptionKeySize).append(", encryptionSaltSize=")
				.append(encryptionSaltSize).append(", encryptionNumberHashRounds=").append(encryptionNumberHashRounds)
				.append(", bypassSslCertChecks=").append(bypassSslCertChecks).append("]");
		return builder.toString();
	}

	@Override
	public synchronized int getSocketRenewalIntervalInSeconds() {
		return socketRenewalIntervalInSeconds;
	}

	public synchronized void setSocketRenewalIntervalInSeconds(final int socketRenewalIntervalInSeconds) {
		this.socketRenewalIntervalInSeconds = socketRenewalIntervalInSeconds;
	}

	@Override
	public synchronized boolean isLongTransferRestart() {
		return longTransferRestart;
	}

	public synchronized void setLongTransferRestart(final boolean longFileTransferRestart) {
		longTransferRestart = longFileTransferRestart;
	}

	@Override
	public synchronized int getParallelCopyBufferSize() {
		return parallelCopyBufferSize;
	}

	public synchronized void setParallelCopyBufferSize(final int parallelCopyBufferSize) {
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

	@Override
	public synchronized SslNegotiationPolicy getNegotiationPolicy() {
		return negotiationPolicy;
	}

	public synchronized void setNegotiationPolicy(final SslNegotiationPolicy negotiationPolicy) {
		if (negotiationPolicy == null) {
			throw new IllegalArgumentException("null negotiationPolicy");
		}
		this.negotiationPolicy = negotiationPolicy;
	}

	@Override
	public synchronized EncryptionAlgorithmEnum getEncryptionAlgorithmEnum() {
		return encryptionAlgorithmEnum;
	}

	public synchronized void setEncryptionAlgorithmEnum(final EncryptionAlgorithmEnum encryptionAlgorithmEnum) {
		this.encryptionAlgorithmEnum = encryptionAlgorithmEnum;
	}

	@Override
	public synchronized int getEncryptionKeySize() {
		return encryptionKeySize;
	}

	public synchronized void setEncryptionKeySize(final int encryptionKeySize) {
		this.encryptionKeySize = encryptionKeySize;
	}

	@Override
	public synchronized int getEncryptionSaltSize() {
		return encryptionSaltSize;
	}

	public synchronized void setEncryptionSaltSize(final int encryptionSaltSize) {
		this.encryptionSaltSize = encryptionSaltSize;
	}

	@Override
	public synchronized int getEncryptionNumberHashRounds() {
		return encryptionNumberHashRounds;
	}

	public synchronized void setEncryptionNumberHashRounds(final int encryptionNumberHashRounds) {
		this.encryptionNumberHashRounds = encryptionNumberHashRounds;
	}

	@Override
	public synchronized boolean isBypassSslCertChecks() {
		return bypassSslCertChecks;
	}

	public synchronized void setBypassSslCertChecks(final boolean bypassSslCertChecks) {
		this.bypassSslCertChecks = bypassSslCertChecks;
	}

	@Override
	public String getDefaultIrodsRuleEngineIdentifier() {
		return defaultIrodsRuleEngineIdentifier;
	}

	public void setDefaultIrodsRuleEngineIdentifier(final String defaultIrodsRuleEngineIdentifier) {
		this.defaultIrodsRuleEngineIdentifier = defaultIrodsRuleEngineIdentifier;
	}

	@Override
	public String getDefaultPythonRuleEngineIdentifier() {
		return defaultPythonRuleEngineIdentifier;
	}

	public void setDefaultPythonRuleEngineIdentifier(final String defaultPythonRuleEngineIdentifier) {
		this.defaultPythonRuleEngineIdentifier = defaultPythonRuleEngineIdentifier;
	}

	public boolean isUseNIOForParallelTransfers() {
		return useNIOForParallelTransfers;
	}

	@Override
	public String getDefaultCppRuleEngineIdentifier() {
		return defaultCppRuleEngineIdentifier;
	}

	public void setDefaultCppRuleEngineIdentifier(final String defaultCppRuleEngineIdentifier) {
		this.defaultCppRuleEngineIdentifier = defaultCppRuleEngineIdentifier;
	}

	@Override
	public boolean isRulesSetDestinationWhenAuto() {
		return this.rulesSetDestinationWhenAuto;
	}

	public void setRulesSetDestinationWhenAuto(final boolean rulesSetDestinationWhenAuto) {
		this.rulesSetDestinationWhenAuto = rulesSetDestinationWhenAuto;
	}

}
