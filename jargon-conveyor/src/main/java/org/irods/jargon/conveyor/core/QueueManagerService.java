/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

/**
 * Manages the persistent queue of transfer information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface QueueManagerService {

	/**
	 * Cause a put operation (transfer to iRODS) to occur. This transfer will be
	 * based on the given iRODS account information.
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the absolutePath to the local source
	 *            file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> with the absolutePath to the iRODS target
	 *            file
	 * @param targetResource
	 *            <code>String</code> with optional (blank if not used) storage
	 *            resource
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the
	 * @throws ConveyorExecutionException
	 */
	void enqueuePutOperation(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException;

	/**
	 * Signal that,if the queue is not busy, that the next pending operation
	 * should be launched
	 * 
	 * @throws ConveyerExecutionException
	 */
	void dequeueNextOperation() throws ConveyorExecutionException, JargonException, Exception;

}
