/**
 * 
 */
package org.irods.jargon.transfer.engine;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.transfer.engine.TransferManager.ErrorStatus;
import org.irods.jargon.transfer.engine.TransferManager.RunningStatus;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DummyTransferManagerCallbackListener implements
		TransferManagerCallbackListener {

	public boolean isHasError() {
		return hasError;
	}

	public boolean isComplete() {
		return isComplete;
	}

	private boolean hasError = false;

	private boolean isComplete = false;

	private List<TransferStatus> transferStatusHistory = new ArrayList<TransferStatus>();
	private List<TransferStatus> overallStatusHistory = new ArrayList<TransferStatus>();

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.transferengine.TransferManagerCallbackListener#
	 * transferManagerErrorStatusUpdate
	 * (org.irods.jargon.transferengine.TransferManager.ErrorStatus)
	 */
	@Override
	public void transferManagerErrorStatusUpdate(final ErrorStatus errorStatus) {
		if (errorStatus == ErrorStatus.ERROR) {
			hasError = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.transferengine.TransferManagerCallbackListener#
	 * transferManagerRunningStatusUpdate
	 * (org.irods.jargon.transferengine.TransferManager.RunningStatus)
	 */
	@Override
	public void transferManagerRunningStatusUpdate(
			final RunningStatus runningStatus) {
		if (runningStatus == RunningStatus.IDLE) {
			isComplete = true;
		}

	}

	@Override
	public void statusCallback(final TransferStatus transferStatus) {
		transferStatusHistory.add(transferStatus);
	}

	public List<TransferStatus> getTransferStatusHistory() {
		return transferStatusHistory;
	}

	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {

		if (transferStatus.getTransferState() == TransferState.OVERALL_COMPLETION
				|| transferStatus.getTransferState() == TransferState.OVERALL_INITIATION) {
			// OK
		} else {
			throw new JargonException("illegal status reported as overall");
		}

		overallStatusHistory.add(transferStatus);
	}

	/**
	 * @return the overallStatusHistory
	 */
	public List<TransferStatus> getOverallStatusHistory() {
		return overallStatusHistory;
	}

}
