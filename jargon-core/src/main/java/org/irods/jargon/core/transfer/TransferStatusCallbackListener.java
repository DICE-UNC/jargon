/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * Listener for callbacks on the status of a transfer operation.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TransferStatusCallbackListener {

	/**
	 * Method that will receive a callback on the status of a transfer
	 * operation. Note that when a status listener is registered for callbacks,
	 * that exceptions that occur in the transfer are not thrown, rather, the
	 * exceptions are transmitted back in the status callback for processing,
	 * and the callee must decide how to handle an exception.
	 * <p>
	 * 
	 * @param transferStatus
	 *            {@link org.irods.jargon.core.transfer.TransferStatus} with
	 *            information on the transfer.
	 * @throws JargonException
	 */
	public void statusCallback(final TransferStatus transferStatus) throws JargonException;
	
	/**
	 * Method will reeive a callback at the initiation and completion of an overall transfer
	*            {@link org.irods.jargon.core.transfer.TransferStatus} with
	 *            information on the transfer.
	 * @throws JargonException
	 */
	public void overallStatusCallback(final TransferStatus transferStatus) throws JargonException;
	
}
