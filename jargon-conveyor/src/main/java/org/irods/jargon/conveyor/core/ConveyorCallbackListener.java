package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * Interface to be implemented by a listener that will receive callbacks on the
 * overall status of the <code>ConveyorService</code>, as well as callbacks
 * issued by the underlying Jargon transfer process. An example use-case would
 * be a GUI interface that wants to present icons that depict the real-time
 * status of the transfer engine.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConveyorCallbackListener extends
		TransferStatusCallbackListener {

	/**
	 * Callback when the running status of the <code>ConveyorService</code> has
	 * updated.
	 * 
	 * @param runningStatus
	 *            <code>QueueStatus</code> with the new status to set
	 */
	void setQueueStatus(final QueueStatus queueStatus);

	/**
	 * Signals an exception in the actual functioning of the conveyor framework
	 * itself
	 * 
	 * @param conveyorException
	 *            <code>Exception</code> occurring within the conveyor framework
	 *            that cannot be handled
	 */
	void signalUnhandledConveyorException(final Exception conveyorException);

}
