/**
 * 
 */
package org.irods.jargon.transferengine;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transferengine.TransferManager.ErrorStatus;
import org.irods.jargon.transferengine.TransferManager.RunningStatus;

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
	public void transferStatusCallback(final TransferStatus transferStatus) {
		transferStatusHistory.add(transferStatus);
	}

	public List<TransferStatus> getTransferStatusHistory() {
		return transferStatusHistory;
	}

}
