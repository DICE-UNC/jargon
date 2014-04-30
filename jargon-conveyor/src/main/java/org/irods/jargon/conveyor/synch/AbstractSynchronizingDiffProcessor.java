package org.irods.jargon.conveyor.synch;

import java.util.Enumeration;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for a mechanism to take a difference tree model and
 * synchronize between iRODS and the local file system. This can be implemented
 * for different synch strategies.
 * <p/>
 * Note that this class keeps instance variables depending on the actual
 * invocation after object creation, and is not meant to be shared or re-used
 * for synch operations. A new class should be obtained from the factory for
 * each synch operation. The caching is done for performance, as there is a
 * minor expense to resolving the iRODS account and obtaining the hooks to do
 * actual transfers to iRODS.
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public abstract class AbstractSynchronizingDiffProcessor implements
		TransferStatusCallbackListener {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractSynchronizingDiffProcessor.class);

	private final ConveyorService conveyorService;
	private final TransferControlBlock transferControlBlock;
	private TransferAttempt transferAttempt;

	public static final String BACKUP_PREFIX = "synch-backup";

	/**
	 * The fields below are initialized on demand with synchronized access
	 */

	private IRODSAccount irodsAccount = null;
	private DataTransferOperations dataTransferOperations = null;
	private TransferStatusCallbackListener transferStatusCallbackListener = null;

	/**
	 * Create an instance with an initialized reference to the conveyor service
	 * 
	 * @param conveyorService
	 *            {@link ConveyorService} reference
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} instance that allows signalling
	 *            of cancellation and communication with the calling process
	 */
	public AbstractSynchronizingDiffProcessor(
			final ConveyorService conveyorService,
			final TransferControlBlock transferControlBlock) {
		super();

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		this.conveyorService = conveyorService;
		this.transferControlBlock = transferControlBlock;

	}

	/**
	 * Given a diff embodied in a <code>FileTreeModel</code>, do necessary
	 * operations to synchronize between local and iRODS.
	 * <p/>
	 * Given a properly calculated diff, do the stuff to bring the source and
	 * target directories in line. The actual way this synch is resolved depends
	 * on the implementation of the various 'schedule' methods stubbed out in
	 * this abstract class. A subclass can implement the stub schedule methods
	 * to respond to the reported 'diff' state.
	 * 
	 * @param TransferAttempt
	 *            {@link TransferAttempt} of type <code>SYNCH</code>, with a
	 *            parent {@link Synchronization} that describes the synch
	 *            relationship
	 * @param diffModel
	 *            {@link FileTreeModel} that embodies the diff between local and
	 *            iRODS
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will receive
	 *            call-backs on the status of the diff processing
	 * @throws ConveyorExecutionException
	 */
	public void execute(final TransferAttempt transferAttempt,
			final FileTreeModel diffModel,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws ConveyorExecutionException {

		log.info("processDiff()");

		assert transferAttempt != null;
		assert diffModel != null;
		assert transferStatusCallbackListener != null;

		try {
			signalStartupCallback(transferAttempt.getTransfer()
					.getSynchronization(), transferStatusCallbackListener);

			synchronized (this) {
				irodsAccount = getConveyorService().getGridAccountService()
						.irodsAccountForGridAccount(
								transferAttempt.getTransfer()
										.getSynchronization().getGridAccount());

				dataTransferOperations = getConveyorService()
						.getIrodsAccessObjectFactory()
						.getDataTransferOperations(irodsAccount);

				this.transferStatusCallbackListener = transferStatusCallbackListener;
				this.transferAttempt = transferAttempt;

			}

			processDiff((FileTreeNode) diffModel.getRoot(), transferAttempt
					.getTransfer().getSynchronization()
					.getLocalSynchDirectory(), transferAttempt.getTransfer()
					.getSynchronization().getIrodsSynchDirectory());

			log.info("diff processed, ");

		} catch (JargonException e) {
			throw new ConveyorExecutionException(e);
		}

	}

/**
	 * Send a message that we are starting the diff resolve step
	 * 
	 * @param synchronization {@link Synchronization
	 * @param transferStatusCallbackListener
	 * @throws JargonException
	 */
	protected void signalStartupCallback(final Synchronization synchronization,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {
		// make an overall status callback that a synch is initiated

		TransferStatus overallSynchStartStatus = TransferStatus.instance(
				TransferType.SYNCH, synchronization.getLocalSynchDirectory(),
				synchronization.getIrodsSynchDirectory(), synchronization
						.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
				TransferState.SYNCH_DIFF_RESOLVE_STEP, synchronization
						.getGridAccount().getHost(), synchronization
						.getGridAccount().getZone());
		transferStatusCallbackListener
				.overallStatusCallback(overallSynchStartStatus);

	}

/**
	 * Send a message that we failed due to errors
	 * 
	 * @param synchronization {@link Synchronization
	 * @param transferStatusCallbackListener
	 * @throws JargonException
	 */
	protected void signalFailureCallback(final Synchronization synchronization,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {
		// make an overall status callback that a synch is initiated

		TransferStatus overallSynchStartStatus = TransferStatus.instance(
				TransferType.SYNCH, synchronization.getLocalSynchDirectory(),
				synchronization.getIrodsSynchDirectory(), synchronization
						.getDefaultStorageResource(), 0L, 0L, 0, 0, 0,
				TransferState.FAILURE, synchronization.getGridAccount()
						.getHost(), synchronization.getGridAccount().getZone());
		transferStatusCallbackListener
				.overallStatusCallback(overallSynchStartStatus);

	}

	/**
	 * Recursive method to process a difference node and its children.
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws TransferEngineException
	 */
	private void processDiff(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {

		if (transferControlBlock.isCancelled()
				|| transferControlBlock.isPaused()) {
			log.info("cancelling...");
			return;
		}

		final FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		log.debug("processing diff node:{}", fileTreeDiffEntry);

		processDiffNode(diffNode, localRootAbsolutePath, irodsRootAbsolutePath,
				fileTreeDiffEntry);

	}

	/**
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param fileTreeDiffEntry
	 * @throws TransferEngineException
	 */
	private void processDiffNode(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final FileTreeDiffEntry fileTreeDiffEntry)
			throws ConveyorExecutionException {

		if (isCancelled()) {
			return;
		}

		if (fileTreeDiffEntry.getDiffType() == DiffType.DIRECTORY_NO_DIFF) {
			evaluateDirectoryNode(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath, fileTreeDiffEntry);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_PLUS) {
			log.debug("local file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.FILE_OUT_OF_SYNCH) {
			log.debug("local file out of synch with irods {}",
					fileTreeDiffEntry.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
			scheduleMatchedFileOutOfSynch(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.RIGHT_HAND_PLUS) {
			log.debug("irods file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleIrodsToLocal(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_NEWER) {
			log.debug("left hand file is newer than irods{}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.RIGHT_HAND_NEWER) {
			log.debug("irods files is newer than left hand side {}",
					fileTreeDiffEntry.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
			scheduleIrodsToLocal(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else {
			log.warn("unknown diff type:{}", fileTreeDiffEntry);
		}
	}

	/**
	 * Recurse through children if this is a directory
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @param fileTreeDiffEntry
	 * @throws TransferEngineException
	 */
	private void evaluateDirectoryNode(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final FileTreeDiffEntry fileTreeDiffEntry)
			throws ConveyorExecutionException {
		log.debug("evaluating directory: {}", fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry()
				.getFormattedAbsolutePath());
		FileTreeNode childNode;
		@SuppressWarnings("rawtypes")
		final Enumeration children = diffNode.children();
		while (children.hasMoreElements()) {

			if (isCancelled()) {
				log.info("cancelling...");
				break;
			}

			childNode = (FileTreeNode) children.nextElement();
			processDiff(childNode, localRootAbsolutePath, irodsRootAbsolutePath);
		}
	}

	/**
	 * Stub method that should be implemented by subclasses that need to move
	 * iRODS files to the local file system when out of synch. By default this
	 * method does nothing.
	 * 
	 * @param diffNode
	 *            {@link FileTreeNode} that represents the diff entry from the
	 *            comparison phase
	 * @param localRootAbsolutePath
	 *            <code>String</code> with the local root directory for the
	 *            configured synch
	 * @param irodsRootAbsolutePath
	 *            <code>String</code> with the irods root directory for the
	 *            configured synch
	 * @throws ConveyorExecutionException
	 */
	protected void scheduleIrodsToLocal(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {

		log.info("scheduleIrodsToLocal() not implemented by default");

	}

	/**
	 * Stub method that should be implemented by subclasses that need to move
	 * local files to iRODS when out of synch. By default this method does
	 * nothing.
	 * 
	 * @param diffNode
	 *            {@link FileTreeNode} that represents the diff entry from the
	 *            comparison phase
	 * @param localRootAbsolutePath
	 *            <code>String</code> with the local root directory for the
	 *            configured synch
	 * @param irodsRootAbsolutePath
	 *            <code>String</code> with the irods root directory for the
	 *            configured synch
	 * @throws ConveyorExecutionException
	 */
	protected void scheduleLocalToIrods(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {

		log.info("scheduleLocalToIrods() not implemented by default");

	}

	/*
	 * private void scheduleIrodsToLocal(final FileTreeNode diffNode, final
	 * String localRootAbsolutePath, final String irodsRootAbsolutePath) throws
	 * ConveyorExecutionException {
	 * 
	 * log.info("\n\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n");
	 * 
	 * log.info("scheduleIrodsToLocal for diffNode:{}", diffNode);
	 * 
	 * 
	 * FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
	 * .getUserObject(); CollectionAndDataObjectListingEntry entry =
	 * fileTreeDiffEntry .getCollectionAndDataObjectListingEntry();
	 * 
	 * String targetRelativePath; if (entry.getObjectType() ==
	 * ObjectType.COLLECTION) { targetRelativePath =
	 * entry.getParentPath().substring( irodsRootAbsolutePath.length()); } else
	 * { targetRelativePath = entry.getFormattedAbsolutePath().substring(
	 * irodsRootAbsolutePath.length()); }
	 * 
	 * StringBuilder sb = new StringBuilder(localRootAbsolutePath);
	 * sb.append(targetRelativePath);
	 * 
	 * log.info("doing a get from irods under target at:{}",
	 * targetRelativePath);
	 * 
	 * log.warn("get operations not yet implemented!");
	 * 
	 * }
	 */

	/**
	 * Stub method when a file exists locally and in iRODS, to be processed by
	 * the subclass in an appropriate manner. By default this method does
	 * nothing
	 * 
	 * @param diffNode
	 *            {@link FileTreeNode} that represents the diff entry from the
	 *            comparison phase
	 * @param localRootAbsolutePath
	 *            <code>String</code> with the local root directory for the
	 *            configured synch
	 * @param irodsRootAbsolutePath
	 *            <code>String</code> with the irods root directory for the
	 *            configured synch
	 * @throws ConveyorExecutionException
	 */
	protected void scheduleMatchedFileOutOfSynch(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)
			throws ConveyorExecutionException {
		log.info("scheduleMatchedFileOutOfSynch() not implemented by default");
	}

	/**
	 * @return the transferControlBlock
	 */
	protected TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @return the irodsAccount
	 */
	protected synchronized IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	protected synchronized void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @return the dataTransferOperations
	 */
	protected synchronized DataTransferOperations getDataTransferOperations() {
		return dataTransferOperations;
	}

	/**
	 * @param dataTransferOperations
	 *            the dataTransferOperations to set
	 */
	protected synchronized void setDataTransferOperations(
			final DataTransferOperations dataTransferOperations) {
		this.dataTransferOperations = dataTransferOperations;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @return the transferStatusCallbackListener
	 */
	protected synchronized TransferStatusCallbackListener getTransferStatusCallbackListener() {
		return transferStatusCallbackListener;
	}

	/**
	 * @param transferStatusCallbackListener
	 *            the transferStatusCallbackListener to set
	 */
	protected synchronized void setTransferStatusCallbackListener(
			final TransferStatusCallbackListener transferStatusCallbackListener) {
		this.transferStatusCallbackListener = transferStatusCallbackListener;
	}

	/**
	 * Convenience method to simplify obtaining a ref to the irods access object
	 * factory
	 * 
	 * @return
	 */
	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return getConveyorService().getIrodsAccessObjectFactory();
	}

	/**
	 * Convenience method to simplify obtaining a ref to an irods file factory,
	 * configured with the <code>IRODSAccount</code> used to initialize this
	 * synch processor
	 * 
	 * @return
	 * @throws ConveyorExecutionException
	 */
	protected IRODSFileFactory getIrodsFileFactory()
			throws ConveyorExecutionException {
		try {
			return getIrodsAccessObjectFactory().getIRODSFileFactory(
					getIrodsAccount());
		} catch (JargonException e) {
			log.error("cannot obtain irodsFileFactory", e);
			throw new ConveyorExecutionException(
					"unable to create an iRODS file factory", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public FileStatusCallbackResponse statusCallback(
			final TransferStatus transferStatus) throws JargonException {

		if (transferStatus.isIntraFileStatusReport()) {
			// quash
		} else {
			getTransferStatusCallbackListener().statusCallback(transferStatus);
		}

		return FileStatusCallbackResponse.CONTINUE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {

		log.info(
				"overall status callback will be quashed, but failures will be sure to have cancel set...{}",
				transferStatus);

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			log.error("failure in underlying transfer:{}", transferStatus);
			log.info("set cancel in tcb, let synch process terminate");
			signalFailureCallback(transferAttempt.getTransfer()
					.getSynchronization(), transferStatusCallbackListener);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * transferAsksWhetherToForceOperation(java.lang.String, boolean)
	 */
	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection) {

		log.info("overwrite situation, cancel as this shouldn't happen");

		return CallbackResponse.CANCEL;
	}

	/**
	 * Checks for a cancellation
	 * 
	 * @return
	 */
	protected boolean isCancelled() {
		return (transferControlBlock.isCancelled() || transferControlBlock
				.isPaused());
	}

}