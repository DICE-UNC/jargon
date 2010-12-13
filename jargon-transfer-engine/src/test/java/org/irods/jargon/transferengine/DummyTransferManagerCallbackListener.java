/**
 * 
 */
package org.irods.jargon.transferengine;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transferengine.TransferManager.ErrorStatus;
import org.irods.jargon.transferengine.TransferManager.RunningStatus;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DummyTransferManagerCallbackListener implements
		TransferManagerCallbackListener {

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
	public void transferManagerErrorStatusUpdate(ErrorStatus errorStatus) {
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
	public void transferManagerRunningStatusUpdate(RunningStatus runningStatus) {
		if (runningStatus == RunningStatus.IDLE) {
			isComplete = true;
		}

	}

	@Override
	public void transferStatusCallback(TransferStatus transferStatus) {
		transferStatusHistory.add(transferStatus);
	}

	public List<TransferStatus> getTransferStatusHistory() {
		return transferStatusHistory;
	}



}
