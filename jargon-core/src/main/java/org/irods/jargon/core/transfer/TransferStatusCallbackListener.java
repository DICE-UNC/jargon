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
	 * Valid callback responses for overwrite messages
	 */
	public enum CallbackResponse {
		YES_THIS_FILE, NO_THIS_FILE, YES_FOR_ALL, NO_FOR_ALL, CANCEL
	}

	/**
	 * Callback response that is honored for pre-file transfer callbacks and can
	 * skip the transfer of the specific file
	 */
	public enum FileStatusCallbackResponse {
		CONTINUE, SKIP
	}

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
	 * @return {@link FileStatusCallbackResponse} that indicates whether a file
	 *         should be skipped in a pre file operation. The nominal response
	 *         is CONTINUE, a SKIP has no effect except in the pre file transfer
	 *         response
	 * @throws JargonException
	 */
	FileStatusCallbackResponse statusCallback(
			final TransferStatus transferStatus) throws JargonException;

	/**
	 * Method will reeive a callback at the initiation and completion of an
	 * overall transfer {@link org.irods.jargon.core.transfer.TransferStatus}
	 * with information on the transfer.
	 *
	 * @throws JargonException
	 */
	void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException;

	/**
	 * A callback from a running transfer will occur if a file exists during an
	 * operation, and this method provides an opportunity for the client to
	 * determine this behavior in real time by answering the call back.
	 *
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the file or
	 *            collection to be over-written.
	 * @param isCollection
	 *            <code>boolean</code> that hints that the path is a collection,
	 *            versus a data object. This is mostly useful for creating a
	 *            more specific dialog in the case of a user interface.
	 * @return {@link CallbackResponse} enum value determining the behavior of
	 *         overwrites for the given transfer.
	 */
	CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection);
}
