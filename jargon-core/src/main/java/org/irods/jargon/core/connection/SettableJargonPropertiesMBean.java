/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;

/**
 * MBeans interface for JargonProperties
 * 
 * @author conwaymc
 *
 */
public interface SettableJargonPropertiesMBean extends JargonProperties {

	void setRulesSetDestinationWhenAuto(final boolean rulesSetDestinationWhenAuto);

	void setDefaultCppRuleEngineIdentifier(final String defaultCppRuleEngineIdentifier);

	void setDefaultPythonRuleEngineIdentifier(final String defaultPythonRuleEngineIdentifier);

	void setDefaultIrodsRuleEngineIdentifier(final String defaultIrodsRuleEngineIdentifier);

	void setBypassSslCertChecks(final boolean bypassSslCertChecks);

	void setEncryptionNumberHashRounds(final int encryptionNumberHashRounds);

	void setEncryptionSaltSize(final int encryptionSaltSize);

	void setEncryptionKeySize(final int encryptionKeySize);

	void setEncryptionAlgorithmEnum(final EncryptionAlgorithmEnum encryptionAlgorithmEnum);

	void setNegotiationPolicy(final SslNegotiationPolicy negotiationPolicy);

	void setIntraFileStatusCallbacksTotalBytesInterval(final long intraFileStatusCallbacksTotalBytesInterval);

	void setIntraFileStatusCallbacksNumberCallsInterval(final int intraFileStatusCallbacksNumberCallsInterval);

	void setParallelCopyBufferSize(final int parallelCopyBufferSize);

	void setLongTransferRestart(final boolean longFileTransferRestart);

	void setSocketRenewalIntervalInSeconds(final int socketRenewalIntervalInSeconds);

	void setPrimaryTcpPerformancePrefsBandwidth(final int primaryTcpPerformancePrefsBandwidth);

	void setPrimaryTcpPerformancePrefsLatency(final int primaryTcpPerformancePrefsLatency);

	void setPrimaryTcpPerformancePrefsConnectionTime(final int primaryTcpPerformancePrefsConnectionTime);

	void setPrimaryTcpReceiveWindowSize(final int primaryTcpReceiveWindowSize);

	void setPrimaryTcpSendWindowSize(final int primaryTcpSendWindowSize);

	void setPrimaryTcpKeepAlive(final boolean primaryTcpKeepAlive);

	void setParallelTcpPerformancePrefsBandwidth(final int parallelTcpPerformancePrefsBandwidth);

	void setParallelTcpPerformancePrefsLatency(final int parallelTcpPerformancePrefsLatency);

	void setParallelTcpPerformancePrefsConnectionTime(final int parallelTcpPerformancePrefsConnectionTime);

	void setParallelTcpReceiveWindowSize(final int parallelTcpReceiveWindowSize);

	void setParallelTcpSendWindowSize(final int parallelTcpSendWindowSize);

	void setParallelTcpKeepAlive(final boolean parallelTcpKeepAlive);

	void setChecksumEncoding(final ChecksumEncodingEnum checksumEncoding);

	void setConnectionFactory(final String connectionFactory);

	void setUsingSpecificQueryForCollectionListingsWithPermissions(
			final boolean usingSpecificQueryForCollectionListingsWithPermissions);

	void setUsingDiscoveredServerPropertiesCache(final boolean usingDiscoveredServerPropertiesCache);

	void setPamTimeToLive(final int pamTimeToLive);

	void setForcePamFlush(final boolean forcePamFlush);

	void setPAMTimeToLive(final int pamTimeToLive);

	void setUsingSpecQueryForDataObjPermissionsForUserInGroup(
			final boolean usingSpecQueryForDataObjPermissionsForUserInGroup);

	void setUsingSpecificQueryForCollectionListingWithPermissions(final boolean useSpecificQuery);

	void setReconnectTimeInMillis(final long reconnectTimeInMillis);

	void setDefaultToPublicIfNothingUnderRootWhenListing(final boolean defaultToPublicIfNothingUnderRootWhenListing);

	void setReconnect(final boolean reconnect);

	void setInstrument(final boolean instrument);

	void setEncoding(final String encoding);

	void setInputToOutputCopyBufferByteSize(final int inputToOutputCopyBufferByteSize);

	void setGetBufferSize(final int getBufferSize);

	void setPutBufferSize(final int putBufferSize);

	void setIrodsParallelSocketTimeout(final int irodsParallelSocketTimeout);

	void setIrodsSocketTimeout(final int irodsSocketTimeout);

	void setLocalFileInputStreamBufferSize(final int localFileInputStreamBufferSize);

	void setLocalFileOutputStreamBufferSize(final int localFileOutputStreamBufferSize);

	void setSendInputStreamBufferSize(final int sendInputStreamBufferSize);

	void setInternalCacheBufferSize(final int internalCacheBufferSize);

	void setInternalOutputStreamBufferSize(final int internalOutputStreamBufferSize);

	void setInternalInputStreamBufferSize(final int internalInputStreamBufferSize);

	void setTransferThreadPoolMaxSimultaneousTransfers(final int transferThreadPoolMaxSimultaneousTransfers);

	void setIRODSParallelTransferSocketTimeout(final int irodsParallelSocketTimeout);

	void setIRODSSocketTimeout(final int irodsSocketTimeout);

	void setIntraFileStatusCallbacks(final boolean intraFileStatusCallbacks);

	void setComputeAndVerifyChecksumAfterTransfer(final boolean computeAndVerifyChecksumAfterTransfer);

	void setComputeChecksumAfterTransfer(final boolean computeChecksumAfterTransfer);

	void setAllowPutGetResourceRedirects(final boolean allowPutGetResourceRedirects);

	void setUseTransferThreadsPool(final boolean useTransferThreadsPool);

	void setMaxFilesAndDirsQueryMax(final int maxFilesAndDirsQueryMax);

	void setReplicaTokenLockTimeoutSeconds(int replicaTokenLockTimeoutSeconds);

}
