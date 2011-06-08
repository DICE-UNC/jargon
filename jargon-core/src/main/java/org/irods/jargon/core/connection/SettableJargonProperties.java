/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Implementation of the <code>JargonProperties</code> interface that is sutable
 * for user-definition and injection into the <code>IRODSession</code>.
 * Typcially, properties that control Jargon are pulled from a default
 * jargon.properties file. This class would allow, for example, the wiring of
 * property opttions via Spring through various setters.
 * <p/>
 * Note that this is, at first, a minimal implementatoin with certain defaults.
 * In the future, a mechanism that consults the jargon properties for defaults
 * and operates to override specific properties can be implemented.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SettableJargonProperties implements JargonProperties {

	// FIXME: get defaults from prop and allow overrides

	private boolean useParallelTransfer = true;
	private int maxParallelThreads = 4;
	private long parallelThreadsLengthThreshold = 33554432;
	private int maxFilesAndDirsQueryMax = 5000;
	private boolean useTransferThreadsPool = false;
	private int transferThreadCorePoolSize = 0;
	private int transferThreadMaxPoolSize = 16;
	private int transferThreadPoolTimeoutMillis = 60000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getParallelThreadsLengthThreshold()
	 */
	@Override
	public long getParallelThreadsLengthThreshold() throws JargonException {
		return parallelThreadsLengthThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#isUseParallelTransfer()
	 */
	@Override
	public boolean isUseParallelTransfer() throws JargonException {
		return useParallelTransfer;
	}

	public void setUseParallelTransfer(final boolean useParallelTransfer) {
		this.useParallelTransfer = useParallelTransfer;
	}

	public void setMaxParallelThreads(final int maxParallelThreads) {
		this.maxParallelThreads = maxParallelThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#getMaxParallelThreads()
	 */
	@Override
	public int getMaxParallelThreads() throws JargonException {
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
	public int getMaxFilesAndDirsQueryMax() throws JargonException {
		return maxFilesAndDirsQueryMax;
	}

	@Override
	public boolean isUseTransferThreadsPool() throws JargonException {
		return useTransferThreadsPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadCorePoolSize()
	 */
	@Override
	public int getTransferThreadCorePoolSize() throws JargonException {
		return transferThreadCorePoolSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadMaxPoolSize()
	 */
	@Override
	public int getTransferThreadMaxPoolSize() throws JargonException {
		return transferThreadMaxPoolSize;
	}

	@Override
	public int getTransferThreadPoolTimeoutMillis() throws JargonException {
		return transferThreadPoolTimeoutMillis;
	}

	/**
	 * @param parallelThreadsLengthThreshold the parallelThreadsLengthThreshold to set
	 */
	public void setParallelThreadsLengthThreshold(
			long parallelThreadsLengthThreshold) {
		this.parallelThreadsLengthThreshold = parallelThreadsLengthThreshold;
	}

	/**
	 * @param maxFilesAndDirsQueryMax the maxFilesAndDirsQueryMax to set
	 */
	public void setMaxFilesAndDirsQueryMax(int maxFilesAndDirsQueryMax) {
		this.maxFilesAndDirsQueryMax = maxFilesAndDirsQueryMax;
	}

	/**
	 * @param useTransferThreadsPool the useTransferThreadsPool to set
	 */
	public void setUseTransferThreadsPool(boolean useTransferThreadsPool) {
		this.useTransferThreadsPool = useTransferThreadsPool;
	}

	/**
	 * @param transferThreadCorePoolSize the transferThreadCorePoolSize to set
	 */
	public void setTransferThreadCorePoolSize(int transferThreadCorePoolSize) {
		this.transferThreadCorePoolSize = transferThreadCorePoolSize;
	}

	/**
	 * @param transferThreadMaxPoolSize the transferThreadMaxPoolSize to set
	 */
	public void setTransferThreadMaxPoolSize(int transferThreadMaxPoolSize) {
		this.transferThreadMaxPoolSize = transferThreadMaxPoolSize;
	}

	/**
	 * @param transferThreadPoolTimeoutMillis the transferThreadPoolTimeoutMillis to set
	 */
	public void setTransferThreadPoolTimeoutMillis(
			int transferThreadPoolTimeoutMillis) {
		this.transferThreadPoolTimeoutMillis = transferThreadPoolTimeoutMillis;
	}

}
