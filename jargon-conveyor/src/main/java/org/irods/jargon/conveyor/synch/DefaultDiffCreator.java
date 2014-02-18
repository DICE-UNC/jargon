/**
 * 
 */
package org.irods.jargon.conveyor.synch;

import java.io.File;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityImpl;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default diff creating component based on comparing two trees, and computing
 * file diffs by comparing length and then checksum
 * <p/>
 * This is the simplest type of diff, and does not attempt to track history of
 * local changes
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultDiffCreator extends AbstractSynchronizingDiffCreator {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultDiffCreator.class);

	public DefaultDiffCreator(ConveyorService conveyorService,
			TransferControlBlock transferControlBlock) {
		super(conveyorService, transferControlBlock);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.synch.AbstractSynchronizingDiffCreator#
	 * generateFileTreeDiffModel
	 * (org.irods.jargon.transfer.dao.domain.Synchronization,
	 * org.irods.jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	protected FileTreeModel generateFileTreeDiffModel(
			Synchronization synchronization, Transfer transfer)
			throws ConveyorExecutionException {

		log.info("generateFileTreeDiffModel()");

		assert synchronization != null;
		assert transfer != null;

		log.info("generating diff for: {}", synchronization);

		String localPath = this.normalizeFilePath(synchronization
				.getLocalSynchDirectory());
		String irodsPath = this.normalizeFilePath(synchronization
				.getIrodsSynchDirectory());

		log.info("resolving account and obtaining access object factory...");
		IRODSAccount synchAccount = null;
		synchAccount = getConveyorService().getGridAccountService()
				.irodsAccountForGridAccount(synchronization.getGridAccount());

		IRODSAccessObjectFactory irodsAccessObjectFactory = getConveyorService()
				.getIrodsAccessObjectFactory();

		// FIXME: add tcb and cancel to filetreediffutility

		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(
				synchAccount, irodsAccessObjectFactory,
				this.getTransferControlBlock());

		FileTreeModel diffModel;
		try {
			diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(new File(
					localPath), irodsPath, 0L, 0L);
		} catch (JargonException e) {
			log.error("unable to generate diff model", e);
			throw new ConveyorExecutionException("error generating diff model",
					e);
		}

		log.info("diff model obtained:{}", diffModel);
		return diffModel;

	}
}
