package org.irods.jargon.core.transfer;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;

/***
 * Testing implementation of a status callback listener that can keep telemetry
 * on callbacks
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferStatusCallbackListenerTestingImplementation implements
		TransferStatusCallbackListener {

	/**
	 * Just counts the initial file callbacks no matter what
	 */
	private int initCallbackCtr = 0;
	private int putCallbackCtr = 0;
	private int getCallbackCtr = 0;
	private int copyCallbackCtr = 0;
	private int exceptionCallbackCtr = 0;
	private int replicateCallbackCtr = 0;
	private int overallCallbackCtr = 0;
	private int intraFileCallbackCtr = 0;
	private int skipCtr = 0;

	private int pauseAfter = 0;
	private int cancelAfter = 0;
	/**
	 * flag will cause even files (based on the callback counter) to be sent and
	 * odd files to be skipped
	 */
	private boolean transferThenSkip = false;
	private TransferControlBlock transferControlBlock = null;
	private TransferStatusCallbackListener.CallbackResponse forceOption = TransferStatusCallbackListener.CallbackResponse.NO_FOR_ALL;

	private boolean cancelEncountered = false;
	private boolean pauseEncountered = false;
	private final List<TransferStatus> statusCache = new ArrayList<TransferStatus>();

	public TransferStatusCallbackListenerTestingImplementation() {

	}

	public TransferStatusCallbackListenerTestingImplementation(
			final TransferControlBlock transferControlBlock,
			final int pauseAfter, final int cancelAfter,
			final boolean transferThenSkip) {
		this.transferControlBlock = transferControlBlock;
		this.pauseAfter = pauseAfter;
		this.cancelAfter = cancelAfter;
		this.transferThenSkip = transferThenSkip;
	}

	public TransferStatusCallbackListenerTestingImplementation(
			final TransferControlBlock transferControlBlock,
			final int pauseAfter, final int cancelAfter) {
		this.transferControlBlock = transferControlBlock;
		this.pauseAfter = pauseAfter;
		this.cancelAfter = cancelAfter;
	}

	@Override
	public synchronized FileStatusCallbackResponse statusCallback(
			final TransferStatus transferStatus) throws JargonException {

		if (transferStatus.isIntraFileStatusReport()) {
			intraFileCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.IN_PROGRESS_START_FILE) {
			initCallbackCtr++;

			if (initCallbackCtr % 2 == 0) {
				// even
			} else {
				// odd, am I skipping odd?

				if (transferThenSkip) {
					skipCtr++;
					return FileStatusCallbackResponse.SKIP;
				}
			}
		}

		if (transferStatus.getTransferType() == TransferType.GET
				&& transferStatus.getTransferState() == TransferState.IN_PROGRESS_COMPLETE_FILE) {
			getCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.PUT
				&& transferStatus.getTransferState() == TransferState.IN_PROGRESS_COMPLETE_FILE) {
			putCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.REPLICATE) {
			replicateCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.COPY) {
			copyCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			exceptionCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.CANCELLED) {
			cancelEncountered = true;
		}

		if (transferStatus.getTransferState() == TransferState.PAUSED) {
			pauseEncountered = true;
		}

		int totalCallback = getCallbackCtr + putCallbackCtr
				+ exceptionCallbackCtr + replicateCallbackCtr + copyCallbackCtr;

		if (pauseAfter > 0 && totalCallback == pauseAfter) {
			if (transferControlBlock != null) {
				transferControlBlock.setPaused(true);
			}
		}

		if (cancelAfter > 0 && totalCallback == cancelAfter) {
			if (transferControlBlock != null) {
				transferControlBlock.setCancelled(true);
			}
		}

		statusCache.add(transferStatus);
		return FileStatusCallbackResponse.CONTINUE;

	}

	public synchronized final int getPutCallbackCtr() {
		return putCallbackCtr;
	}

	public synchronized final int getExceptionCallbackCtr() {
		return exceptionCallbackCtr;
	}

	public synchronized final int getGetCallbackCtr() {
		return getCallbackCtr;
	}

	public synchronized int getReplicateCallbackCtr() {
		return replicateCallbackCtr;
	}

	public synchronized void setReplicateCallbackCtr(
			final int replicateCallbackCtr) {
		this.replicateCallbackCtr = replicateCallbackCtr;
	}

	public synchronized int getPauseAfter() {
		return pauseAfter;
	}

	public synchronized int getCancelAfter() {
		return cancelAfter;
	}

	public synchronized TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	public synchronized boolean isCancelEncountered() {
		return cancelEncountered;
	}

	public synchronized boolean isPauseEncountered() {
		return pauseEncountered;
	}

	public synchronized int getCopyCallbackCtr() {
		return copyCallbackCtr;
	}

	public synchronized List<TransferStatus> getStatusCache() {
		return statusCache;
	}

	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {

		overallCallbackCtr++;

	}

	/**
	 * @return the overallCallbackCtr
	 */
	public int getOverallCallbackCtr() {
		return overallCallbackCtr;
	}

	/**
	 * @return the intraFileCallbackCtr
	 */
	public int getIntraFileCallbackCtr() {
		return intraFileCallbackCtr;
	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection) {
		return forceOption;
	}

	public CallbackResponse getForceOption() {
		return forceOption;
	}

	public void setForceOption(final CallbackResponse forceOption) {
		this.forceOption = forceOption;
	}

	/**
	 * @return the skipCtr
	 */
	public int getSkipCtr() {
		return skipCtr;
	}

	/**
	 * @param skipCtr
	 *            the skipCtr to set
	 */
	public void setSkipCtr(final int skipCtr) {
		this.skipCtr = skipCtr;
	}

	/**
	 * @return the initCallbackCtr
	 */
	public int getInitCallbackCtr() {
		return initCallbackCtr;
	}

	/**
	 * @param initCallbackCtr
	 *            the initCallbackCtr to set
	 */
	public void setInitCallbackCtr(final int initCallbackCtr) {
		this.initCallbackCtr = initCallbackCtr;
	}

}
