/**
 * 
 */
package org.irods.jargon.transferengine;

import org.irods.jargon.core.transfer.TransferStatus;

/**
 * Interface to be implemented by a listener that will receive callbacks on the
 * overall status of the <code>TransferManager</code>. An example use-case would
 * be a GUI interface that wants to present icons that depict the real-time
 * status of the transfer engine.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TransferManagerCallbackListener {

	/**
	 * Callback when the running status of the <code>TransferManager</code> has
	 * updated.
	 * 
	 * @param runningStatus
	 *            <code>TransferManager.RunningStatus</code> enum value with the
	 *            new status
	 */
	public void transferManagerRunningStatusUpdate(
			final TransferManager.RunningStatus runningStatus);

	/**
	 * Callback when the error status of the <code>TransferManager</code> has
	 * been updated.
	 * 
	 * @param errorStatus
	 *            <code>TransferManager.ErrorStatus</code> enum value with the
	 *            new error status.
	 */
	public void transferManagerErrorStatusUpdate(
			final TransferManager.ErrorStatus errorStatus);

	/**
	 * Allows transfer managers to tap into the status callbacks coming from the
	 * iRODS transfer operation. This may be per file updates, and in the future
	 * might be intra-file byte count updates as well.
	 * 
	 * @param transferStatus
	 *            <code>TransferStatus</code> block with details of the current
	 *            transfer.
	 */
	public void transferStatusCallback(final TransferStatus transferStatus);

}
