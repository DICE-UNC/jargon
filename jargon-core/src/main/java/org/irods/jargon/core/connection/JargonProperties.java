package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a source of configuration metadata that will effect the behavior
 * of Jargon through a properties file or other configuration source.
 * <p/>
 * Note that many of these properties may be overridden in a particular operation
 * during invocation.  For example, many properties here control the behavior of transfers,
 * and can be overridden by setting <code>TransferOptions</code>.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface JargonProperties {

	/**
	 * Do I want parallel transfers at all?
	 * 
	 * @return
	 * @throws JargonException
	 */
	boolean isUseParallelTransfer() throws JargonException;

	/**
	 * If doing parallel transfers, what is the maximum number of threads I
	 * should specify?
	 * 
	 * @return
	 * @throws JargonException
	 */
	int getMaxParallelThreads() throws JargonException;

	/**
	 * The file length above which a numThreads will be sent to iRODS in
	 * DataObjInp. This is done for backwards compatibility. Older versions of
	 * iRODS will default to parallel processing if any nonzero number is sent
	 * in numThreads.
	 * 
	 * @return <code>long</code> in megabytes for the size above which a
	 *         non-zero numThreads of value maxParallelThreads will be sent.
	 * @throws JargonException
	 */
	long getParallelThreadsLengthThreshold() throws JargonException;

	/**
	 * Sets a default number of results to ask for when executing GenQuery for
	 * listing files and collections.
	 * 
	 * @return
	 * @throws JargonException
	 */
	int getMaxFilesAndDirsQueryMax() throws JargonException;

	/**
	 * Am I using an executor pool for parallel transfer threads
	 */
	boolean isUseTransferThreadsPool() throws JargonException;

	/**
	 * Minimum number of threads kept in the transfer threads executor pool.
	 * This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool core size
	 * @throws JargonException
	 */
	int getTransferThreadCorePoolSize() throws JargonException;

	/**
	 * Maximum number of threads kept in the transfer threads executor pool.
	 * This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool max size
	 * @throws JargonException
	 */
	int getTransferThreadMaxPoolSize() throws JargonException;

	/**
	 * Timeout for keeping threads in the transfer threads executor pool above
	 * the core size. This is stored in the {@link IRODSSession} object if the
	 * <code>isUseTranfsferThreadsPool()</code> value is true;
	 * 
	 * @return <code>int</code> with the desired transfer thread pool max size
	 * @throws JargonException
	 */
	int getTransferThreadPoolTimeoutMillis() throws JargonException;

	/**
	 * Should puts/gets redirect to the resource server that holds the data?
	 * (equivalent to the -I in iput/iget>
	 * 
	 * @return the allowPutGetResourceRedirects <code>boolean</code> that will
	 *         be <code>true</code> if redirecting is desired
	 * @throws JargonException 
	 */
	boolean isAllowPutGetResourceRedirects() throws JargonException;


	/**
	 * Should checksums be computed after the transfer?  This does not process things as a verification.
	 * @return the computeChecksumAfterTransfer
	 */
	boolean isComputeChecksumAfterTransfer() throws JargonException;

	
	/**
	 * Should checksums be computed, and a verify done, after a transfer, with a <code>FileIntegrityException</code> 
	 * thrown in case of failure?
	 * @return the computeAndVerifyChecksumAfterTransfer
	 */
	boolean isComputeAndVerifyChecksumAfterTransfer() throws JargonException;

}