/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * Callback listener that will force the current connection to do unnatural
 * things to simulate some exception in the transfer
 * 
 * @author Mike Conway
 * 
 * 
 */
public class TransferKillingCallbackListener implements
		TransferStatusCallbackListener {

	/**
	 * 
	 */
	public TransferKillingCallbackListener(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final int killAfter, final IRODSAccount irodsAccount) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public FileStatusCallbackResponse statusCallback(
			TransferStatus transferStatus) throws JargonException {

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

	}

}
