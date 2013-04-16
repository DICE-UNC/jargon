/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.irods.jargon.conveyor.core.AbstractConveyorService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Basic implementation of a queue manager service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BasicQueueManagerServiceImpl extends AbstractConveyorService
		implements QueueManagerService {

	public BasicQueueManagerServiceImpl() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#enqueuePutOperation
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void enqueuePutOperation(String sourceFileAbsolutePath,
			String targetFileAbsolutePath, String targetResource,
			IRODSAccount irodsAccount) throws ConveyorExecutionException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#dequeueNextOperation()
	 */
	@Override
	public void dequeueNextOperation() throws ConveyorExecutionException {
		// FIXME: implement!
	}

}
