/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a restart of a get operation
 * 
 * @author Mike Conway - DICE
 *
 */
public class GetTransferRestartProcessor extends
		AbstractTransferRestartProcessor {

	private static Logger log = LoggerFactory
			.getLogger(GetTransferRestartProcessor.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @param restartManager
	 */
	public GetTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager) {
		super(irodsAccessObjectFactory, irodsAccount, restartManager);
	}

	@Override
	public void restartIfNecessary(String irodsAbsolutePath)
			throws RestartFailedException, FileRestartManagementException {

	}

}
