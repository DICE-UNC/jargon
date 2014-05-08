package org.irods.jargon.core.pub;

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
	private TransferStatusCallbackListener.CallbackResponse forceOption = TransferStatusCallbackListener.CallbackResponse.NO_FOR_ALL;

	private long bytesReportedIntraFileCallbacks = 0L;
	private int numberIntraFileCallbacks = 0;

	@Override
	public FileStatusCallbackResponse statusCallback(
			final TransferStatus transferStatus) {

		if (transferStatus.isIntraFileStatusReport()) {
			numberIntraFileCallbacks++;
			bytesReportedIntraFileCallbacks = transferStatus
					.getBytesTransfered();
		} else if (transferStatus.getTransferState() == TransferState.FAILURE) {
			errorCallbackCount++;
		} else if (transferStatus.getTransferState() == TransferState.IN_PROGRESS_START_FILE) {
			// ignored
		} else {
			successCallbackCount++;
			lastSourcePath = transferStatus.getSourceFileAbsolutePath();
			lastTargetPath = transferStatus.getTargetFileAbsolutePath();
			lastResource = transferStatus.getTargetResource();
		}

		return FileStatusCallbackResponse.CONTINUE;

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

	@Override
	public void overallStatusCallback(final TransferStatus transferStatus) {

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			errorCallbackCount++;
		} else {
			successCallbackCount++;
		}

		lastSourcePath = transferStatus.getSourceFileAbsolutePath();
		lastTargetPath = transferStatus.getTargetFileAbsolutePath();
		lastResource = transferStatus.getTargetResource();
	}

	/**
	 * @return the bytesReportedIntraFileCallbacks
	 */
	public long getBytesReportedIntraFileCallbacks() {
		return bytesReportedIntraFileCallbacks;
	}

	/**
	 * @return the numberIntraFileCallbacks
	 */
	public int getNumberIntraFileCallbacks() {
		return numberIntraFileCallbacks;
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

}
