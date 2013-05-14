/**
 * 
 */
package org.irods.jargon.conveyor.unittest.utils;

import org.irods.jargon.conveyor.core.ConveyorCallbackListener;
import org.irods.jargon.conveyor.core.QueueStatus;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;

/**
 * Dummy callback listener
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class DevNullCallbackListener implements ConveyorCallbackListener {

	/**
	 * 
	 */
	public DevNullCallbackListener() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void overallStatusCallback(TransferStatus transferStatus)
			throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToForceOperation(java.lang.String, boolean)
	 */
	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			String irodsAbsolutePath, boolean isCollection) {
		return CallbackResponse.CANCEL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.ConveyorCallbackListener#setQueueStatus
	 * (org.irods.jargon.conveyor.core.QueueStatus)
	 */
	@Override
	public void setQueueStatus(QueueStatus queueStatus) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.ConveyorCallbackListener#
	 * signalUnhandledConveyorException(java.lang.Exception)
	 */
	@Override
	public void signalUnhandledConveyorException(Exception conveyorException) {

	}

}
