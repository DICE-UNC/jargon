/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;

/**
 * @author Mike Conway - DICE
 * 
 */
public class InvocationContext {

	/**
	 * Injected reference to the current, resolved iRODS account for this
	 * transfer
	 */
	private IRODSAccount irodsAccount;

	/**
	 * Injected reference to the current transfer attempt
	 */
	private TransferAttempt transferAttempt;

	/**
	 * Injected reference to the control block for the current transfer
	 */
	private TransferControlBlock transferControlBlock;

	/**
	 * Shared map of objects
	 */
	private final Map<String, Object> sharedProperties = new ConcurrentHashMap<String, Object>();

	/**
	 * @return the {@link IRODSAccount}
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the {@link IRODSAccount} to set
	 */
	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @return the {@link TransferAttempt}
	 */
	public TransferAttempt getTransferAttempt() {
		return transferAttempt;
	}

	/**
	 * @param transferAttempt
	 *            the {@link TransferAttempt} to set
	 */
	public void setTransferAttempt(TransferAttempt transferAttempt) {
		this.transferAttempt = transferAttempt;
	}

	/**
	 * @return the {@link TransferControlBlock}
	 */
	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @param transferControlBlock
	 *            the {@link TransferControlBlock} to set
	 */
	public void setTransferControlBlock(
			TransferControlBlock transferControlBlock) {
		this.transferControlBlock = transferControlBlock;
	}

	/**
	 * @return the sharedProperties
	 */
	public Map<String, Object> getSharedProperties() {
		return sharedProperties;
	}

}
