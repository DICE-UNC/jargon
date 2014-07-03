package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;

/**
 * Represents a source of configuration metadata that will effect the behavior
 * of Jargon through a properties file or other configuration source.
 * <p/>
 * Note that many of these properties may be overridden in a particular
 * operation during invocation. For example, many properties here control the
 * behavior of transfers, and can be overridden by setting
 * <code>TransferOptions</code>.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface JargonProperties {

	/**
	 * Get the type of checksum that will be used by default when transferring
	 * files to iRODS
	 * 
	 * @return {@link ChecksumEncodingEnum} used in validating file transfers
	 */
	ChecksumEncodingEnum getChecksumEncoding();

	/**
	 * Get the character set encoding used by Jargon
	 * 
	 * @return
	 */
	String getEncoding();

	/**
	 * Do I want parallel transfers at all?
	 * 
	 * @return
	 */
	boolean isUseParallelTransfer();

	/**
	 * If doing parallel transfers, what is the maximum number of threads I
	 * should specify?
	 * 
	 * @return
	 */
	int getMaxParallelThreads();

	/**
	 * <h2>Experimental setting!!</h2>
	 * <p/>
	 * Use NIO to transfer between the local file system and iRODS for parallel
	 * transfer operations
	 * 
	 * @return <code>boolean</code> of <code>true</code> if NIO should be used
	 *         for parallel transfers
	 */
	boolean isUseNIOForParallelTransfers();

	/**
	 * The file length above which a numThreads will be sent to iRODS in
	 * DataObjInp. This is done for backwards compatibility. Older versions of
	 * iRODS will default to parallel processing if any nonzero number is sent
	 * in numThreads.
	 * 
	 * @return <code>long</code> in megabytes for the size above which a
	 *         non-zero numThreads of value maxParallelThreads will be sent.
	 * @throws JargonException
	 *             long getParallelThreadsLengthThreshold();
	 * 
	 *             /** Sets a default number of results to ask for when
	 *             executing GenQuery for listing files and collections.
	 * 
	 * @return
	 */
	int getMaxFilesAndDirsQueryMax();

	/**
	 * Am I using an executor pool for parallel transfer threads
	 */
	boolean isUseTransferThreadsPool();

	/**
	 * Minimum number of transfers supported by the transfer thread executor
	 * pool. This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired number of simultaneous
	 *         transfers. The number of transfer threads will be computed based
	 *         on this number * the maximum parallel transfer threads
	 */
	int getTransferThreadPoolMaxSimultaneousTransfers();

	/**
	 * Timeout for keeping threads in the transfer threads executor pool above
	 * the core size. This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool max size
	 * @throws JargonException
	 */
	int getTransferThreadPoolTimeoutMillis();

	/**
	 * Should puts/gets redirect to the resource server that holds the data?
	 * (equivalent to the -I in iput/iget>
	 * 
	 * @return the allowPutGetResourceRedirects <code>boolean</code> that will
	 *         be <code>true</code> if redirecting is desired
	 */
	boolean isAllowPutGetResourceRedirects();

	/**
	 * Should checksums be computed after the transfer? This does not process
	 * things as a verification.
	 * 
	 * @return the computeChecksumAfterTransfer
	 */
	boolean isComputeChecksumAfterTransfer();

	/**
	 * Should checksums be computed, and a verify done, after a transfer, with a
	 * <code>FileIntegrityException</code> thrown in case of failure?
	 * 
	 * @return the computeAndVerifyChecksumAfterTransfer
	 */
	boolean isComputeAndVerifyChecksumAfterTransfer();

	/**
	 * Gets whether intra-file status call-backs are enabled for transfers. If
	 * <code>true</code>, and a call-back listener is provided, these allow
	 * monitoring of progress of an individual file.
	 * 
	 * @return the intraFileStatusCallbacks
	 */
	boolean isIntraFileStatusCallbacks();

	/**
	 * Get the time-out, in seconds, for the main iRODS socket. Will be zero or
	 * less if not specified
	 * 
	 * @return
	 */
	int getIRODSSocketTimeout();

	/**
	 * Get the internal buffer size used for the input stream between Jargon and
	 * iRODS. See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement return <code>int</code> with the buffer size for the
	 * input stream buffer. (0 = use defaults, -1 = do not wrap with buffered
	 * input stream) jargon.io.internal.input.stream.buffer.size
	 */
	int getInternalInputStreamBufferSize();

	/**
	 * Get the internal buffer size used for the output stream between Jargon
	 * and iRODS. See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement return <code>int</code> with the buffer size for the
	 * output stream buffer. (0 = use defaults, -1 = do not wrap with buffered
	 * input stream) jargon.io.internal.output.stream.buffer.size
	 */
	int getInternalOutputStreamBufferSize();

	/**
	 * Get the size of the internal buffer cache . See
	 * https://code.renci.org/gf/
	 * project/jargon/wiki/?pagename=NormalIOArrangement. Jargon has an internal
	 * buffer where the various <code>send()</code> methods in
	 * {@link IRODSBasicTCPConnection} write data to iRODS. In these methods,
	 * Jargon uses an internal cache buffer for the sends. This has been done
	 * historically, but the benefits of this cache have not yet been measured.
	 * Setting this as a parameter to turn off will assist in testing the use of
	 * the buffer, and the option of eliminating the buffer altogether. return
	 * <code>int</code> with the size of the internal cache (0 = do not utilize
	 * the cache buffer) jargon.io.internal.cache.buffer.size
	 */
	int getInternalCacheBufferSize();

	/**
	 * Get the buffer size used for the input stream between Jargon and iRODS
	 * passed to the <code>send()</code> method of
	 * {@link IRODSBasicTCPConnection}. This input stream would typically be
	 * from a local file that was being sent to iRODS, or other such source. The
	 * {@link IRODSMidLevelProtocol} object, using the
	 * <code>irodsFunction</code> method with the <code>InputStream</code>
	 * parameter, will wrap the given input stream in a
	 * <code>BufferedInputStream</code> based on the setting of this parameter.
	 * 
	 * See https://code.renci.org/gf/project/jargon/wiki/?pagename=
	 * NormalIOArrangement return <code>int</code> with the buffer size for the
	 * buffered stream that will wrap an <code>InputStream</code> to be sent to
	 * iRODS. (0 = use defaults, -1 = do not wrap with buffered input stream)
	 * jargon.io.send.input.stream.buffer.size
	 */
	int getSendInputStreamBufferSize();

	/**
	 * Get the size of the buffer used in read/write operations to copy data
	 * from an input stream to output stream in the
	 * {@link IRODSBasicTCPConnection} class <code>send()</code> methods.
	 * 
	 * @return <code>int</code> with the size of the read/write loop buffer
	 *         jargon.io.input.to.output.copy.byte.buffer.size
	 */
	int getInputToOutputCopyBufferByteSize();

	/**
	 * Get the size of the buffer used in a <code>BufferedOutputStream</code>
	 * that wraps the output stream for the local file. This is used in
	 * processing get operations where the iRODS data is being saved to the
	 * local file system. (0 = use defaults, -1 = do not wrap with buffered
	 * output stream) jargon.io.local.output.stream.buffer.size
	 * 
	 * @return <code>int</code> with the buffer size
	 */
	int getLocalFileOutputStreamBufferSize();

	/**
	 * Get the size of the buffer used in a <code>BufferedInputStream</code>
	 * that wraps the intput stream for the local file. This is used in
	 * processing operations where the data is being read from the local file
	 * system. (0 = use defaults, -1 = do not wrap with buffered output stream)
	 * jargon.io.local.input.stream.buffer.size
	 * 
	 * @return <code>int</code> with the buffer size
	 */
	int getLocalFileInputStreamBufferSize();

	/**
	 * Get the time-out, in seconds, for the parallel transfer sockets. Will be
	 * zero or less if not specified
	 * 
	 * @return
	 */
	int getIRODSParallelTransferSocketTimeout();

	/**
	 * Get the size of the file segment for each successive call in normal put
	 * operations.
	 * 
	 * @return
	 */
	int getPutBufferSize();

	/**
	 * Get the size of the file segment for each successive call in normal get
	 * operations.
	 * 
	 * @return <code>int</code> with buffer size
	 */
	int getGetBufferSize();

	/**
	 * <code>boolean</code> that indicates whether the connection should be
	 * renewed every 10 minutes to get around certain firewall issues. This is
	 * equvalent to the -T option in the iput and iget iCommands.
	 * 
	 * @return
	 */
	boolean isReconnect();

	/**
	 * Get the reconnect time expressed in milliseconds, used by the reconnect
	 * thread that will be launched if the <code>isReconnect()</code> method
	 * returns true. This value has no meaning if the reconnect option is not
	 * selected.
	 * 
	 * @return <code>long</code> with the reconnect time in milliseconds.
	 */
	long getReconnectTimeInMillis();

	/**
	 * <code>boolean</code> that indicates whether certain performance
	 * statistics are gathered and reported to the DEBUG log. This will turn on
	 * useful statistics for optimization and tuning, but will introduce a
	 * certain amount of overhead, so this is typically unsuitable for
	 * production deployment.
	 * <p/>
	 * Note that actual instrumentation will be an ongoing process, and will be
	 * done as certain operations are tuned. Initially, this will represent the
	 * infrastructure for such tuning information.
	 * 
	 * @return
	 */
	boolean isInstrument();

	/**
	 * This parameter tunes the behavior of the
	 * {@link CollectionAndDataObjectListAndSearchAO}, and potentially other
	 * parts of the API involved in listing directories under the root ('/')
	 * directory. In certain situations, such as with strictACL's enabled, a
	 * user may not have permissions to list collections under root. This can
	 * prevent the viewing of directories that a user is actually enabled to see
	 * because the higher level collections do not have the ACL's that enable
	 * this.
	 * <p/>
	 * This property allows a behavior to support the convention that a path
	 * underneath the root, specifically /zonename/home/public might exist, and
	 * the various entry listing methods will attempt to find this path, even
	 * when listing is not possible by calling iRODS.
	 * 
	 * @return <code>boolean</code> that will indicate whether to display the
	 *         home directory, and the public directory underneath the home
	 *         directory.
	 */
	boolean isDefaultToPublicIfNothingUnderRootWhenListing();

	/**
	 * Determines if a cache of discovered properties is used, these are aspects
	 * of iRODS servers (such as, whether specific query support is available),
	 * that are discovered as a result of calling a function in Jargon. Instead
	 * of trying and failing to get a certain service from iRODS over and over
	 * again, a result can be cached here to check.
	 * 
	 * @return <code>boolean</code> that is used to determine whether various
	 *         jargon operations will cache and consult the discovered server
	 *         properties that are available under {@link IRODSSession}
	 */
	boolean isUsingDiscoveredServerPropertiesCache();

	/**
	 * Indicates that specific query should be used for collection listings with
	 * permissions. This prevents expansion of groups. Note that Jargon will
	 * check to see if the server is capable of using specific query and will
	 * fall back if it cannot.
	 * 
	 * @return <code>boolean</code> of <code>true</code> if jargon should
	 *         attempt to use specific query for permissions listings before
	 *         falling back to genquery
	 */
	boolean isUsingSpecificQueryForCollectionListingsWithPermissions();

	/**
	 * Indicates whether a specific query (listUserACLForDataObjViaGroup) is
	 * available and can be used to check the user access rights for a user who
	 * has access via a group. This is so methods like
	 * <code>IRODSFile.canRead()</code> will work even though a user does not
	 * have explicit permissions, and is a member of a group that has access
	 * instead.
	 * 
	 * @return
	 */
	boolean isUsingSpecQueryForDataObjPermissionsForUserInGroup();

	/**
	 * Get the time to live (in seconds) for PAM generated passwords
	 * 
	 * @return <code>int</code> with the PAM time to live, in secords
	 */
	int getPAMTimeToLive();

	/**
	 * Force additional flushes during PAM authorization. This is typically
	 * turned off because it introduces performance overhead. It is useful when
	 * using PAM prior to iRODS 3.3.
	 * 
	 * @return
	 */
	boolean isForcePamFlush();

	/**
	 * Get the type of networking layer that will be used in the low level
	 * connections to iRODS (currently the values are limited to 'tcp'). If no
	 * property is found, then 'tcp' will be defaulted.
	 * 
	 * @return
	 */
	String getConnectionFactory();

}