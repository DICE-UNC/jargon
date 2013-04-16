/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;

/**
 * Basic implementation of a queue manager service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BasicQueueManagerServiceImpl extends
		AbstractConveyorComponentService implements QueueManagerService {

	/**
	 * Injected dependency
	 */
	private TransferDAO transferDAO;

	/**
	 * Injected dependency
	 */
	private TransferAttemptDAO transferAttemptDAO;

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

	/**
	 * @return the transferDAO
	 */
	public synchronized TransferDAO getTransferDAO() {
		return transferDAO;
	}

	/**
	 * @param transferDAO
	 *            the transferDAO to set
	 */
	public synchronized void setTransferDAO(TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	/**
	 * @return the transferAttemptDAO
	 */
	public synchronized TransferAttemptDAO getTransferAttemptDAO() {
		return transferAttemptDAO;
	}

	/**
	 * @param transferAttemptDAO
	 *            the transferAttemptDAO to set
	 */
	public synchronized void setTransferAttemptDAO(
			TransferAttemptDAO transferAttemptDAO) {
		this.transferAttemptDAO = transferAttemptDAO;
	}

}
