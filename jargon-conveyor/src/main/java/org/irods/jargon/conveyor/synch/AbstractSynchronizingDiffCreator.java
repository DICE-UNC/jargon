package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a synchronization, this component can create a proper file diff for
 * later processing. A subclass of this method will compare the source and
 * target directories, and develop a tree model that represents the observed
 * differences. This will then be used in a later resolution phase to
 * synchronize the directories.
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 */
public abstract class AbstractSynchronizingDiffCreator extends
		AbstractSynchronizingComponent {

	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(AbstractSynchronizingDiffCreator.class);

	public AbstractSynchronizingDiffCreator(ConveyorService conveyorService) {
		super(conveyorService);
	}

	/**
	 * Process the given synchronization specification, creating a file diff
	 * model
	 * 
	 * @param synchronization
	 *            {@link Transfer} that describes the source and target for the
	 *            diff operation
	 * @return {@link FileTreeModel} that represents the diff
	 * @throws ConveyorExecutionException
	 */
	public FileTreeModel createDiff(final Transfer transfer)
			throws ConveyorExecutionException {

		log.info("createDiff()");
		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		final Synchronization synchronization = transfer.getSynchronization();

		if (synchronization == null) {
			throw new IllegalArgumentException(
					"transfer is not a synchronization");
		}

		// send the initial status callbacks
		sendInitStatusMessages(transfer, synchronization);

		FileTreeModel fileTreeDiffModel = generateFileTreeDiffModel(
				synchronization, transfer);

		log.info("file tree diff model complete");
		return fileTreeDiffModel;
	}

	protected abstract FileTreeModel generateFileTreeDiffModel(
			Synchronization synchronization, Transfer transfer)
			throws ConveyorExecutionException;

	private void sendInitStatusMessages(final Transfer transfer,
			final Synchronization synchronization)
			throws ConveyorExecutionException {
		// make an overall status callback that a synch is initiated
		TransferStatus overallSynchStartStatus;
		try {
			overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH, synchronization
							.getLocalSynchDirectory(), synchronization
							.getIrodsSynchDirectory(), synchronization
							.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
					TransferState.SYNCH_INITIALIZATION, transfer
							.getGridAccount().getHost(), transfer
							.getGridAccount().getZone());

			getConfiguredCallbackListener().overallStatusCallback(
					overallSynchStartStatus);

			// make an overall status callback that a synch is initiated

			overallSynchStartStatus = TransferStatus.instance(
					TransferType.SYNCH, synchronization
							.getLocalSynchDirectory(), synchronization
							.getIrodsSynchDirectory(), synchronization
							.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
					TransferState.SYNCH_DIFF_GENERATION, transfer
							.getGridAccount().getHost(), transfer
							.getGridAccount().getZone());
			getConfiguredCallbackListener().overallStatusCallback(
					overallSynchStartStatus);

		} catch (JargonException e) {
			log.error("error creating synch", e);
			throw new ConveyorExecutionException("error in synch processing", e);
		}
	}

	/**
	 * Strip trailing slash and any other manipulations to ensure correct
	 * processing of paths
	 * 
	 * @param filePath
	 * @return
	 */
	String normalizeFilePath(final String filePath) {
		assert filePath != null;

		String calculatedLocalRoot = "";
		if (filePath.length() > 1) {
			if (filePath.lastIndexOf(SLASH) == filePath.length() - 1) {
				calculatedLocalRoot = filePath.substring(0,
						filePath.length() - 1);
			} else {
				calculatedLocalRoot = filePath;
			}
		}

		return calculatedLocalRoot;

	}

	/**
	 * Handy method to get the required callback listener (guaranteed to not be
	 * null or an error will be thrown)
	 * 
	 * @return {@link TransferStatusCallbackListener}
	 */
	protected TransferStatusCallbackListener getConfiguredCallbackListener() {
		TransferStatusCallbackListener listener = this.conveyorService
				.getConveyorCallbackListener();
		assert listener != null;
		return listener;
	}

}
