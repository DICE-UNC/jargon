package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a source of configuration metadata that will effect the behavior
 * of Jargon through a properties file or other configuration source.
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
	 * The file length above which a numThreads will be sent to iRODS in DataObjInp.  This is done for backwards compatibility.  Older
	 * versions of iRODS will default to parallel processing if any nonzero number is sent in numThreads.
	 * 
	 * @return <code>long</code> in megabytes for the size above which a non-zero numThreads of value maxParallelThreads will be sent.
	 * @throws JargonException
	 */
	long getParallelThreadsLengthThreshold() throws JargonException;
	
	/**
	 * Sets a default number of results to ask for when executing GenQuery for listing files and collections.
	 * @return
	 * @throws JargonException
	 */
	int getMaxFilesAndDirsQueryMax() throws JargonException;
	
	/**
	 * IRODSFile system metadata, such as <code>length()</code> and <code>isFile()</code> issue a query when called,
	 * this can cause performance issues.  This optimization will cache all data when one system metadata value is queried. 
	 * Subsequent calls will read from the cache until the <code>refresh()</code> method is called in <code>IRODSFile</code>.
	 * @return
	 * @throws JargonException
	 */ 
	boolean isCacheFIleMetadata() throws JargonException;

}