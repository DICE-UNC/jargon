/**
 * 
 */
package org.irods.jargon.transfer.synch;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.engine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a diff model between a local and iRODS root directory, process the diff
 * to bring the directories into a synchronized state. This implementation will
 * do direct puts/gets as the diff is processed.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class InPlaceSynchronizingDiffProcessorImpl implements SynchronizingDiffProcessor {
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private TransferManager transferManager;

	private static final Logger log = LoggerFactory
			.getLogger(InPlaceSynchronizingDiffProcessorImpl.class);

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}


	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}


	public TransferManager getTransferManager() {
		return transferManager;
	}


	public void setTransferManager(TransferManager transferManager) {
		this.transferManager = transferManager;
	}


	/**
	 * Default constructor, expects setting of dependencies by injection
	 */
	public InPlaceSynchronizingDiffProcessorImpl() {

	}
	
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.synch.SynchronizingDiffProcessor#processDiff(org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer, org.irods.jargon.core.connection.IRODSAccount, org.irods.jargon.datautils.tree.FileTreeModel)
	 */
	@Override
	public void processDiff( final LocalIRODSTransfer localIRODSTransfer,  final IRODSAccount irodsAccount, final FileTreeModel diffModel ) throws TransferEngineException {
		
		log.info("processDiff()");
		
		if (localIRODSTransfer == null) {
			throw new IllegalArgumentException("null localIRODSTransfer");
		}
		
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		if (diffModel == null) {
			throw new IllegalArgumentException("null diffModel");
		}
		
		
		log.info("localIRODSTransfer:{}", localIRODSTransfer);
		log.info("irodsAccount:{}", irodsAccount);
		log.info("diffModel:{}", diffModel);
		
		if (localIRODSTransfer.getTransferType() != TransferType.SYNCH) {
			throw new TransferEngineException("not a synch transfer");
		}
		
		checkContracts();
		
		processDiffWithValidData(localIRODSTransfer, irodsAccount, diffModel);
		
	}

	/**
	 * Do the actual diff processing, since contracts and dependencies have all been validated
	 * @param localIRODSTransfer
	 * @param irodsAccount
	 * @param diffModel
	 */
	private void processDiffWithValidData(
			LocalIRODSTransfer localIRODSTransfer, IRODSAccount irodsAccount,
			FileTreeModel diffModel) {
		
		log.info("processDiffWithValidData");
		log.info("difModel:{}", diffModel);
		
	}


	private void checkContracts() throws TransferEngineException {

		if (irodsAccessObjectFactory == null) {
			throw new TransferEngineException(
					"irodsAccessObjectFactory dependency has not been set");
		}

		if (transferManager == null) {
			throw new TransferEngineException(
					"transferManager dependency has not been set");
		}
	}

}
