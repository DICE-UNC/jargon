package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

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
	long getParallelThreadsLengthThreshold();

	/**
	 * Sets a default number of results to ask for when executing GenQuery for
	 * listing files and collections.
	 * 
	 * @return
	 */
	int getMaxFilesAndDirsQueryMax();

	/**
	 * Am I using an executor pool for parallel transfer threads
	 */
	boolean isUseTransferThreadsPool();

	/**
	 * Minimum number of threads kept in the transfer threads executor pool.
	 * This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool core size
	 */
	int getTransferThreadCorePoolSize();

	/**
	 * Maximum number of threads kept in the transfer threads executor pool.
	 * This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool max size
	 */
	int getTransferThreadMaxPoolSize();

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
	 * Gets whether intra-file status call-backs are enabled for transfers.  If <code>true</code>, and a 
	 * call-back listener is provided, these allow monitoring of progress of an individual file.
	 * @return the intraFileStatusCallbacks
	 */
	boolean isIntraFileStatusCallbacks();
	
	/**
	 * Get the time-out, in seconds, for the main iRODS socket.  Will be zero or less if not specified
	 * @return
	 */
	int getIRODSSocketTimeout();
	
	/**
	 * Get the time-out, in seconds, for the parallel transfer sockets.  Will be zero or less if not specified
	 * @return
	 */
	int getIRODSParallelTransferSocketTimeout();

}