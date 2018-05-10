package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;

/**
 * Represents a source of configuration metadata that will effect the behavior
 * of Jargon through a properties file or other configuration source.
 * <p>
 * Note that many of these properties may be overridden in a particular
 * operation during invocation. For example, many properties here control the
 * behavior of transfers, and can be overridden by setting
 * {@code TransferOptions}.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface JargonProperties {

	/**
	 * Get the type of checksum that will be used by default when transferring files
	 * to iRODS
	 *
	 * @return {@link ChecksumEncodingEnum} used in validating file transfers
	 */
	ChecksumEncodingEnum getChecksumEncoding();

	/**
	 * Get the character set encoding used by Jargon
	 *
	 * @return {@code String}
	 */
	String getEncoding();

	/**
	 * Do I want parallel transfers at all?
	 *
	 * @return {@code boolean}
	 */
	boolean isUseParallelTransfer();

	/**
	 * If doing parallel transfers, what is the maximum number of threads I should
	 * specify?
	 *
	 * @return {@code int}
	 */
	int getMaxParallelThreads();

	/**
	 * The file length above which a numThreads will be sent to iRODS in DataObjInp.
	 * This is done for backwards compatibility. Older versions of iRODS will
	 * default to parallel processing if any nonzero number is sent in numThreads.
	 *
	 *
	 * @return <code>int</code> with the max number of files and dirs to return
	 */
	int getMaxFilesAndDirsQueryMax();

	/**
	 * Am I using an executor pool for parallel transfer threads
	 * 
	 * @return {@code boolean} if using a transfer thread pool
	 */
	boolean isUseTransferThreadsPool();

	/**
	 * Minimum number of transfers supported by the transfer thread executor pool.
	 * This is stored in the {@link IRODSSession} object if the
	 * {@code isUseTranfsferThreadsPool()} value is true;
	 *
	 * @return {@code int} with the desired number of simultaneous transfers. The
	 *         number of transfer threads will be computed based on this number *
	 *         the maximum parallel transfer threads
	 */
	int getTransferThreadPoolMaxSimultaneousTransfers();

	/**
	 * Timeout for keeping threads in the transfer threads executor pool above the
	 * core size. This is stored in the {@link IRODSSession} object if the
	 * {@code isUseTranfsferThreadsPool()} value is true;
	 *
	 * @return {@code int} with the desired transfer thread pool max size
	 */
	int getTransferThreadPoolTimeoutMillis();

	/**
	 * Should puts/gets redirect to the resource server that holds the data?
	 * (equivalent to the -I in iput/iget)
	 *
	 * @return the allowPutGetResourceRedirects {@code boolean} that will be
	 *         {@code true} if redirecting is desired
	 */
	boolean isAllowPutGetResourceRedirects();

	/**
	 * Should checksums be computed after the transfer? This does not process things
	 * as a verification.
	 *
	 * @return the computeChecksumAfterTransfer
	 */
	boolean isComputeChecksumAfterTransfer();

	/**
	 * Should checksums be computed, and a verify done, after a transfer, with a
	 * {@code FileIntegrityException} thrown in case of failure?
	 *
	 * @return the computeAndVerifyChecksumAfterTransfer
	 */
	boolean isComputeAndVerifyChecksumAfterTransfer();

	/**
	 * Gets whether intra-file status call-backs are enabled for transfers. If
	 * {@code true}, and a call-back listener is provided, these allow monitoring of
	 * progress of an individual file.
	 *
	 * @return the intraFileStatusCallbacks
	 */
	boolean isIntraFileStatusCallbacks();

	/**
	 * Gets the number of calls to ignore when jargon calls the intra-file status
	 * callback listener. No matter how many bytes have been sent, after the minimum
	 * number of calls, the callback will be made to the listener.
	 *
	 * @return {@code int} with the number of callbacks.
	 */
	int getIntraFileStatusCallbacksNumberCallsInterval();

	/**
	 * Gets the number of bytes to ignore until jargon calls the intra-file status
	 * callback listener. No matter how many times the listener has been called,
	 * after the minimum number of bytes, the callback will be made to the listener.
	 *
	 * @return {@code long} with the number of bytes between a callback
	 */
	long getIntraFileStatusCallbacksTotalBytesInterval();

	/**
	 * Get the time-out, in seconds, for the main iRODS socket. Will be zero or less
	 * if not specified
	 *
	 * @return {@code int} with socket timeout
	 */
	int getIRODSSocketTimeout();

	/**
	 * Get the internal buffer size used for the input stream between Jargon and
	 * iRODS. See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement
	 * 
	 * <p>
	 * jargon.io.internal.input.stream.buffer.size
	 * 
	 * @return {@code int} with the buffer size for the input stream buffer. (0 =
	 *         use defaults, -1 = do not wrap with buffered input stream)
	 * 
	 * 
	 * 
	 * 
	 */
	int getInternalInputStreamBufferSize();

	/**
	 * Get the internal buffer size used for the output stream between Jargon and
	 * iRODS. See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement
	 * 
	 * <p>
	 * 
	 * jargon.io.internal.output.stream.buffer.size
	 * 
	 * @return {@code int} with the buffer size for the output stream buffer. (0 =
	 *         use defaults, -1 = do not wrap with buffered input stream)
	 * 
	 */
	int getInternalOutputStreamBufferSize();

	/**
	 * Get the size of the internal buffer cache . See https://code.renci.org/gf/
	 * project/jargon/wiki/?pagename=NormalIOArrangement. Jargon has an internal
	 * buffer where the various {@code send()} methods in
	 * {@link IRODSBasicTCPConnection} write data to iRODS. In these methods, Jargon
	 * uses an internal cache buffer for the sends. This has been done historically,
	 * but the benefits of this cache have not yet been measured. Setting this as a
	 * parameter to turn off will assist in testing the use of the buffer, and the
	 * option of eliminating the buffer altogether.
	 * 
	 * <p>
	 * jargon.io.internal.cache.buffer.size
	 * 
	 * @return {@code int} with the size of the internal cache (0 = do not utilize
	 *         the cache buffer)
	 * 
	 */
	int getInternalCacheBufferSize();

	/**
	 * Get the buffer size used for the input stream between Jargon and iRODS passed
	 * to the {@code send()} method of {@link IRODSBasicTCPConnection}. This input
	 * stream would typically be from a local file that was being sent to iRODS, or
	 * other such source. The {@link IRODSMidLevelProtocol} object, using the
	 * {@code irodsFunction} method with the {@code InputStream} parameter, will
	 * wrap the given input stream in a {@code BufferedInputStream} based on the
	 * setting of this parameter.
	 * <p>
	 * jargon.io.send.input.stream.buffer.size
	 *
	 * See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement
	 * 
	 * @return {@code int} with the buffer size for the buffered stream that will
	 *         wrap an {@code InputStream} to be sent to iRODS. (0 = use defaults,
	 *         -1 = do not wrap with buffered input stream)
	 * 
	 * 
	 */
	int getSendInputStreamBufferSize();

	/**
	 * Get the size of the buffer used in read/write operations to copy data from an
	 * input stream to output stream in the {@link IRODSBasicTCPConnection} class
	 * {@code send()} methods.
	 * <p>
	 * jargon.io.input.to.output.copy.byte.buffer.size
	 *
	 * @return {@code int} with the size of the read/write loop buffer
	 * 
	 */
	int getInputToOutputCopyBufferByteSize();

	/**
	 * Get the size of the buffer used in a {@code BufferedOutputStream} that wraps
	 * the output stream for the local file. This is used in processing get
	 * operations where the iRODS data is being saved to the local file system. (0 =
	 * use defaults, -1 = do not wrap with buffered output stream)
	 * <p>
	 * jargon.io.local.output.stream.buffer.size
	 *
	 * @return {@code int} with the buffer size
	 */
	int getLocalFileOutputStreamBufferSize();

	/**
	 * Get the size of the buffer used in a {@code BufferedInputStream} that wraps
	 * the intput stream for the local file. This is used in processing operations
	 * where the data is being read from the local file system. (0 = use defaults,
	 * -1 = do not wrap with buffered output stream)
	 * <p>
	 * jargon.io.local.input.stream.buffer.size
	 *
	 * @return {@code int} with the buffer size
	 */
	int getLocalFileInputStreamBufferSize();

	/**
	 * Get the time-out, in seconds, for the parallel transfer sockets. Will be zero
	 * or less if not specified
	 *
	 * @return {@code int}
	 */
	int getIRODSParallelTransferSocketTimeout();

	/**
	 * Get the size of the file segment for each successive call in normal put
	 * operations.
	 *
	 * @return {@code int}
	 */
	int getPutBufferSize();

	/**
	 * Get the size of the file segment for each successive call in normal get
	 * operations.
	 *
	 * @return {@code int} with buffer size
	 */
	int getGetBufferSize();

	/**
	 * {@code boolean} that indicates whether the connection should be renewed every
	 * 10 minutes to get around certain firewall issues. This is equvalent to the -T
	 * option in the iput and iget iCommands.
	 *
	 * @return {@code boolean}
	 */
	boolean isReconnect();

	/**
	 * Get the reconnect time expressed in milliseconds, used by the reconnect
	 * thread that will be launched if the {@code isReconnect()} method returns
	 * true. This value has no meaning if the reconnect option is not selected.
	 *
	 * @return {@code long} with the reconnect time in milliseconds.
	 */
	long getReconnectTimeInMillis();

	/**
	 * {@code boolean} that indicates whether certain performance statistics are
	 * gathered and reported to the DEBUG log. This will turn on useful statistics
	 * for optimization and tuning, but will introduce a certain amount of overhead,
	 * so this is typically unsuitable for production deployment.
	 * <p>
	 * Note that actual instrumentation will be an ongoing process, and will be done
	 * as certain operations are tuned. Initially, this will represent the
	 * infrastructure for such tuning information.
	 *
	 * @return {@code boolean}
	 */
	boolean isInstrument();

	/**
	 * This parameter tunes the behavior of the
	 * {@link CollectionAndDataObjectListAndSearchAO}, and potentially other parts
	 * of the API involved in listing directories under the root ('/') directory. In
	 * certain situations, such as with strictACL's enabled, a user may not have
	 * permissions to list collections under root. This can prevent the viewing of
	 * directories that a user is actually enabled to see because the higher level
	 * collections do not have the ACL's that enable this.
	 * <p>
	 * This property allows a behavior to support the convention that a path
	 * underneath the root, specifically /zonename/home/public might exist, and the
	 * various entry listing methods will attempt to find this path, even when
	 * listing is not possible by calling iRODS.
	 *
	 * @return {@code boolean} that will indicate whether to display the home
	 *         directory, and the public directory underneath the home directory.
	 */
	boolean isDefaultToPublicIfNothingUnderRootWhenListing();

	/**
	 * Determines if a cache of discovered properties is used, these are aspects of
	 * iRODS servers (such as, whether specific query support is available), that
	 * are discovered as a result of calling a function in Jargon. Instead of trying
	 * and failing to get a certain service from iRODS over and over again, a result
	 * can be cached here to check.
	 *
	 * @return {@code boolean} that is used to determine whether various jargon
	 *         operations will cache and consult the discovered server properties
	 *         that are available under {@link IRODSSession}
	 */
	boolean isUsingDiscoveredServerPropertiesCache();

	/**
	 * Indicates that specific query should be used for collection listings with
	 * permissions. This prevents expansion of groups. Note that Jargon will check
	 * to see if the server is capable of using specific query and will fall back if
	 * it cannot.
	 *
	 * @return {@code boolean} of {@code true} if jargon should attempt to use
	 *         specific query for permissions listings before falling back to
	 *         genquery
	 */
	boolean isUsingSpecificQueryForCollectionListingsWithPermissions();

	/**
	 * Indicates whether a specific query (listUserACLForDataObjViaGroup) is
	 * available and can be used to check the user access rights for a user who has
	 * access via a group. This is so methods like {@code IRODSFile.canRead()} will
	 * work even though a user does not have explicit permissions, and is a member
	 * of a group that has access instead.
	 *
	 * @return {@code boolean}
	 */
	boolean isUsingSpecQueryForDataObjPermissionsForUserInGroup();

	/**
	 * Get the time to live (in seconds) for PAM generated passwords
	 *
	 * @return {@code int} with the PAM time to live, in secords
	 */
	int getPAMTimeToLive();

	/**
	 * Force additional flushes during PAM authorization. This is typically turned
	 * off because it introduces performance overhead. It is useful when using PAM
	 * prior to iRODS 3.3.
	 *
	 * @return {@code boolean}
	 */
	boolean isForcePamFlush();

	/**
	 * Is TCP keep alive set for the primary (1247) irods Socket?
	 *
	 * @return {@code boolean}
	 */
	boolean isParallelTcpKeepAlive();

	/**
	 * parallel TCP send window size, set in a number that will be multiplied by
	 * 1024. Set to 0 if no window size set. This is for parallel transfer sockets.
	 *
	 * @return {@code int} that will be multiplied by 1024 and set as the send
	 *         window size
	 */
	int getParallelTcpSendWindowSize();

	/**
	 * parallel TCP receive window size, set in a number that will be multiplied by
	 * 1024. Set to 0 if no window size set. This is for the parallel socket
	 *
	 *
	 * @return {@code int} that will be multiplied by 1024 and set as the receive
	 *         window size
	 */
	int getParallelTcpReceiveWindowSize();

	/**
	 * parallel TCP preference for connection time for parallel TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for conn time
	 */
	int getParallelTcpPerformancePrefsConnectionTime();

	/**
	 * parallel TCP preference for latency for TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for conn time
	 */
	int getParallelTcpPerformancePrefsLatency();

	/**
	 * parallel TCP preference for latency for TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for bandwidth
	 */
	int getParallelTcpPerformancePrefsBandwidth();

	/**
	 * Get the size of the buffer used in reads and writes to iRODS for parallel
	 * transfer threads, in bytes
	 *
	 * @return {@code int} with the buffer size for parallel transfer
	 */
	int getParallelCopyBufferSize();

	/**
	 * Is TCP keep alive set for the primary irods Socket?
	 *
	 * @return {@code boolean}
	 */
	boolean isPrimaryTcpKeepAlive();

	/**
	 * Primary TCP send window size, set in a number that will be multiplied by
	 * 1024. Set to 0 if no window size set. This is for the primary socket (1247)
	 *
	 * @return {@code int} that will be multiplied by 1024 and set as the send
	 *         window size
	 */
	int getPrimaryTcpSendWindowSize();

	/**
	 * Primary TCP receive window size, set in a number that will be multiplied by
	 * 1024. Set to 0 if no window size set. This is for the primary socket (1247)
	 *
	 * @return {@code int} that will be multiplied by 1024 and set as the receive
	 *         window size
	 */
	int getPrimaryTcpReceiveWindowSize();

	/**
	 * Primary TCP preference for connection time for TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for conn time
	 */
	int getPrimaryTcpPerformancePrefsConnectionTime();

	/**
	 * Primary TCP preference for latency for TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for conn time
	 */
	int getPrimaryTcpPerformancePrefsLatency();

	/**
	 * Primary TCP preference for latency for TCP sockets
	 * (socket.setPerformancePreferences())
	 *
	 * @return {@code int} with preference for bandwidth
	 */
	int getPrimaryTcpPerformancePrefsBandwidth();

	/**
	 * Get the type of networking layer that will be used in the low level
	 * connections to iRODS (currently the values are limited to 'tcp'). If no
	 * property is found, then 'tcp' will be defaulted.
	 *
	 * @return {@code String}
	 */
	String getConnectionFactory();

	/**
	 * Get the renewal interval in seconds, after which a connection is discarded
	 * and renewed. Used for preventing timeouts in recursive transfers. Expressed
	 * as a number of seconds. Set to 0 to turn off this behavior
	 * 
	 * @return {@code} int with the socket renewal interval
	 */
	int getSocketRenewalIntervalInSeconds();

	/**
	 * Indicates whether long file transfer restarts should be done.
	 *
	 * @return {@code boolean} of {@code true} if long file restarts should be done
	 */
	boolean isLongTransferRestart();

	/**
	 *
	 * @return {@link SslNegotiationPolicy}
	 */
	SslNegotiationPolicy getNegotiationPolicy();

	/**
	 * Retrieves the default encryption algo for parallel transfers when SSL is
	 * enabled
	 *
	 * @return {@link EncryptionAlgorithmEnum}
	 */
	EncryptionAlgorithmEnum getEncryptionAlgorithmEnum();

	/**
	 * Return the key size for encryption algo for parallel transfers when SSL is
	 * enabled.
	 *
	 * @return {@code int} with an encryption key size
	 */
	int getEncryptionKeySize();

	/**
	 * Return the salt size for encryption algo for parallel transfers when SSL is
	 * enabled.
	 *
	 * @return {@code int} with an encryption salt size
	 */
	int getEncryptionSaltSize();

	/**
	 * Return the number of hash rounds for encryption algo for parallel transfers
	 * when SSL is enabled.
	 *
	 * @return {@code int} with number of hash rounds
	 */
	int getEncryptionNumberHashRounds();

	/**
	 * Indicates whether SSL cert checks need to be bypassed. This is not
	 * recommended for production deployments.
	 * <p>
	 * /** {@code boolean} that indicates whether ssl cert checks should be
	 * bypassed. {@code false} is the default, meaning checks will be done, and is
	 * the recommended production setting. This is used on initial load of the
	 * {@link IRODSSession}. Note that a custom trust manager can also be injected
	 * by a setter method in {@code IRODSSession} after that {@code IRODSSession} is
	 * constructed, replacing any trust manager instantiated by looking at Jargon
	 * properties.
	 *
	 * @return {@code true} if SSL checks should be bypassed
	 */
	boolean isBypassSslCertChecks();

	/**
	 * Default instance name for the iRODS pluggable rule engine used to route
	 * requests.
	 * 
	 * @return <code>String</code> with an instance name suitable for rule routing
	 */
	String getDefaultIrodsRuleEngineIdentifier();

	/**
	 * Default instance name for the iRODS python rule engine used to route
	 * requests.
	 * 
	 * @return <code>String</code> with an instance name suitable for rule routing
	 */
	String getDefaultPythonRuleEngineIdentifier();

	/**
	 * Default instance name for iRODS C++ rule engine used to route requests
	 * 
	 * @return <code>String</code> with an instance name suitable for rule routing
	 */
	String getDefaultCppRuleEngineIdentifier();

	/**
	 * Modifies behavior of Jargon to always set the destination rule engine based
	 * on the detected or set rule type. Without this setting an iRODS is assumed to
	 * treat iRODS native rule language as the default rule engine, so that rules
	 * Jargon guesses are iRODS rules are sent as equivalent to not setting
	 * 
	 * @return <code>boolean</code> if Jargon should set the rule destination when
	 *         sending a rule to run on iRODS
	 */
	boolean isRulesSetDestinationWhenAuto();

}
