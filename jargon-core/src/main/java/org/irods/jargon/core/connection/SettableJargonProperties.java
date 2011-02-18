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

	//FIXME: get defaults from prop and allow overrides
	

	private boolean useParallelTransfer = true;
	private int maxParallelThreads = 4;
	private long parallelThreadsLengthThreshold = 734003200;
	private int maxFilesAndDirsQueryMax = 5000;
	private boolean cacheFileMetadata = true;

	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.connection.JargonProperties#getParallelThreadsLengthThreshold()
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.connection.JargonProperties#getMaxFilesAndDirsQueryMax()
	 */
	@Override
	public int getMaxFilesAndDirsQueryMax() throws JargonException {
		return maxFilesAndDirsQueryMax;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.connection.JargonProperties#isCacheFIleMetadata()
	 */
	@Override
	public boolean isCacheFIleMetadata() {
		return cacheFileMetadata;
	}
 
	public void setCacheFileMetadata(boolean cacheFileMetadata) {
		this.cacheFileMetadata = cacheFileMetadata;
	}

}
