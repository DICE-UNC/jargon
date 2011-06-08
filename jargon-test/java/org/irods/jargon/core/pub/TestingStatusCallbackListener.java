package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

public class TestingStatusCallbackListener implements
		TransferStatusCallbackListener {

	private int successCallbackCount = 0;
	private int errorCallbackCount = 0;
	private String lastSourcePath = "";
	private String lastTargetPath = "";
	private String lastResource = "";

	@Override
	public void statusCallback(final TransferStatus transferStatus)
			throws JargonException {

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			errorCallbackCount++;
		} else {
			successCallbackCount++;
		}

		lastSourcePath = transferStatus.getSourceFileAbsolutePath();
		lastTargetPath = transferStatus.getTargetFileAbsolutePath();
		lastResource = transferStatus.getTargetResource();

	}

	public int getSuccessCallbackCount() {
		return successCallbackCount;
	}

	public void setSuccessCallbackCount(final int successCallbackCount) {
		this.successCallbackCount = successCallbackCount;
	}

	public int getErrorCallbackCount() {
		return errorCallbackCount;
	}

	public void setErrorCallbackCount(final int errorCallbackCount) {
		this.errorCallbackCount = errorCallbackCount;
	}

	public String getLastSourcePath() {
		return lastSourcePath;
	}

	public String getLastTargetPath() {
		return lastTargetPath;
	}

	public String getLastResource() {
		return lastResource;
	}

}
