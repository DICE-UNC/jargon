package org.irods.jargon.core.transfer;

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

	private int putCallbackCtr = 0;
	private int getCallbackCtr = 0;
	private int exceptionCallbackCtr = 0;
	private int replicateCallbackCtr = 0;

	public TransferStatusCallbackListenerTestingImplementation() {

	}

	@Override
	public synchronized void statusCallback(TransferStatus transferStatus)
			throws JargonException {

		if (transferStatus.getTransferType() == TransferType.GET) {
			getCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.PUT){
			putCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.REPLICATE) {
			replicateCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			exceptionCallbackCtr++;
		}
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

	public int getReplicateCallbackCtr() {
		return replicateCallbackCtr;
	}

	public void setReplicateCallbackCtr(int replicateCallbackCtr) {
		this.replicateCallbackCtr = replicateCallbackCtr;
	}

}
