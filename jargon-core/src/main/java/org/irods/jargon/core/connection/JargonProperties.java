package org.irods.jargon.core.connection;


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
	 * {@link IRODSConnection} write data to iRODS. In these methods, Jargon
	 * uses an internal cache buffer for the sends. This has been done
	 * historically, but the benefits of this cache have not yet been measured.
	 * Setting this as a parameter to turn off will assist in testing the use of
	 * the buffer, and the option of eliminating the buffer altogether. return
	 * <code>int</code> with the size of the internal cache (0 = do not utilize
	 * the cache buffer) jargon.io.internal.cache.buffer.size
	 */
	int getInternalCacheBufferSize();

	/**
	 * Get the buffer size used for the input stream between Jargon and iRODS
	 * passed to the <code>send()</code> method of {@link IRODSConnection}. This
	 * input stream would typically be from a local file that was being sent to
	 * iRODS, or other such source. The {@link IRODSCommands} object, using the
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
	 * from an input stream to output stream in the {@link IRODSConnection}
	 * class <code>send()</code> methods.
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
	 * @return
	 */
	int getGetBufferSize();

	int getIrodsSocketTimeout();

	int getIrodsParallelSocketTimeout();

}