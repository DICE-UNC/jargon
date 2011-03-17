package org.irods.jargon.transfer.synch;

import java.io.File;
import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.datautils.tree.FileTreeDiffUtility;
import org.irods.jargon.datautils.tree.FileTreeModel;
import org.irods.jargon.datautils.tree.FileTreeNode;
import org.irods.jargon.transfer.engine.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare a local watched folder to a remote iRODS folder and enqueue necessary
 * transfers to synchronize between the two.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchronizeProcessorImpl implements SynchronizeProcessor {

	private IRODSAccount irodsAccount;

	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private TransferManager transferManager;

	private FileTreeDiffUtility fileTreeDiffUtility;

	private static final char SLASH = '/';

	private static final Logger log = LoggerFactory
			.getLogger(SynchronizeProcessorImpl.class);

	/**
	 * Private constructor
	 * 
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 * @param transferManager
	 */
	public SynchronizeProcessorImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.synch.SynchronizeProcessor#synchronizeLocalToIRODS
	 * (java.lang.String, java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public void synchronizeLocalToIRODS(final String synchDeviceName,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		if (synchDeviceName == null || synchDeviceName.isEmpty()) {
			throw new IllegalArgumentException("null synchDeviceName");
		}

		if (localRootAbsolutePath == null || localRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null localRootAbsolutePath");
		}

		if (irodsRootAbsolutePath == null || irodsRootAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsRootAbsolutePath");
		}

		if (timestampforLastSynchLeftHandSide < 0) {
			throw new IllegalArgumentException(
					"negative timestampforLastSynchLeftHandSide, set to 0 if not specified");
		}

		if (timestampForLastSynchRightHandSide < 0) {
			throw new IllegalArgumentException(
					"negative timestampForLastSynchRightHandSide, set to 0 if not specified");
		}

		checkInitialization();

		log.info("synchronizeLocalToIRODS for device:{}", synchDeviceName);
		log.info("   localRootAbsolutePath:{}", localRootAbsolutePath);
		log.info("    irodsRootAbsolutePath:{}", irodsRootAbsolutePath);
		log.info("   timestampForLastSynchLeftHandSide:{}",
				timestampforLastSynchLeftHandSide);
		log.info("   timestampForLastSynchRightHandSide:{}",
				timestampForLastSynchRightHandSide);

		StringBuilder calculatedLocalRoot = new StringBuilder(
				localRootAbsolutePath);
		if (localRootAbsolutePath.length() > 1) {
			if (localRootAbsolutePath.lastIndexOf(SLASH) != localRootAbsolutePath
					.length() - 1) {
				log.debug("appending a trailing slash to local absolute path");
				calculatedLocalRoot.append(SLASH);
			}
		}

		StringBuilder calculatedIrodsRoot = new StringBuilder(
				irodsRootAbsolutePath);
		if (irodsRootAbsolutePath.length() > 1) {
			if (irodsRootAbsolutePath.lastIndexOf(SLASH) != irodsRootAbsolutePath
					.length() - 1) {
				log.debug("appending a trailing slash to irods absolute path");
				calculatedIrodsRoot.append(SLASH);
			}
		}

		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				new File(calculatedLocalRoot.toString()),
				calculatedIrodsRoot.toString(),
				timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
		log.debug("diff model obtained");
		if (diffModel == null) {
			throw new JargonException(
					"null diff model returned, cannot process");
		}

		processDiff((FileTreeNode) diffModel.getRoot(),
				calculatedLocalRoot.toString(), calculatedIrodsRoot.toString(),
				timestampforLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
		log.debug("processing complete");
	}

	// given a tree model, do any necessary operations to synchronize
	private void processDiff(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();

		if (fileTreeDiffEntry.getDiffType() == DiffType.DIRECTORY_NO_DIFF) {
			log.debug("evaluating directory: {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			FileTreeNode childNode;
			@SuppressWarnings("rawtypes")
			Enumeration children = diffNode.children();
			while (children.hasMoreElements()) {
				childNode = (FileTreeNode) children.nextElement();
				processDiff(childNode, localRootAbsolutePath,
						irodsRootAbsolutePath,
						timestampforLastSynchLeftHandSide,
						timestampForLastSynchRightHandSide);
			}
		} else if (fileTreeDiffEntry.getDiffType() == DiffType.LEFT_HAND_PLUS) {
			log.debug("local file is new directory {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleLocalToIrods(diffNode, localRootAbsolutePath,
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
			log.debug("irods files is newer than left hand side {}", fileTreeDiffEntry
					.getCollectionAndDataObjectListingEntry()
					.getFormattedAbsolutePath());
			scheduleIrodsToLocal(diffNode, localRootAbsolutePath,
					irodsRootAbsolutePath);
		} else {
			log.warn("unknown diff type:{}", fileTreeDiffEntry);
		}

	}

	private void scheduleIrodsToLocal(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath)  throws JargonException {
		
		/*
		 * the diff node will have the absolute local path of the file, this is
		 * the source of the put. the iRODS path will be the local path minus
		 * the local root, appended to the iRODS root
		 */

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();
		CollectionAndDataObjectListingEntry entry = fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry();
		String targetRelativePath = entry.getFormattedAbsolutePath().substring(
				irodsRootAbsolutePath.length());
		StringBuilder sb = new StringBuilder(localRootAbsolutePath);
		sb.append(targetRelativePath);

		log.info("enqueueing a put to irods under target at:{}",
				targetRelativePath);
		transferManager.enqueueAGet(entry.getFormattedAbsolutePath(),
				sb.toString(), irodsAccount.getDefaultStorageResource(),
				irodsAccount);
	}

	/**
	 * the node is a local file/collection that needs to be scheduled to move to
	 * irods
	 * 
	 * @param diffNode
	 * @param localRootAbsolutePath
	 * @param irodsRootAbsolutePath
	 */
	private void scheduleLocalToIrods(final FileTreeNode diffNode,
			final String localRootAbsolutePath,
			final String irodsRootAbsolutePath) throws JargonException {
		/*
		 * the diff node will have the absolute path of the irods file file, this is
		 * the source of the put. the local path will be the irods path minus
		 * the irods root, appended to the local root
		 */

		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) diffNode
				.getUserObject();
		CollectionAndDataObjectListingEntry entry = fileTreeDiffEntry
				.getCollectionAndDataObjectListingEntry();
		String targetRelativePath = entry.getFormattedAbsolutePath().substring(
				localRootAbsolutePath.length());
		StringBuilder sb = new StringBuilder(irodsRootAbsolutePath);
		sb.append(targetRelativePath);

		log.info("enqueueing a put to irods under target at:{}",
				targetRelativePath);
		transferManager.enqueueAPut(entry.getFormattedAbsolutePath(),
				sb.toString(), irodsAccount.getDefaultStorageResource(),
				irodsAccount);

	}

	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	@Override
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	@Override
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@Override
	public TransferManager getTransferManager() {
		return transferManager;
	}

	@Override
	public void setTransferManager(final TransferManager transferManager) {
		this.transferManager = transferManager;
	}

	@Override
	public FileTreeDiffUtility getFileTreeDiffUtility() {
		return fileTreeDiffUtility;
	}

	@Override
	public void setFileTreeDiffUtility(
			final FileTreeDiffUtility fileTreeDiffUtility) {
		this.fileTreeDiffUtility = fileTreeDiffUtility;
	}

	/**
	 * checks that all appropriate values have been set.
	 */
	private void checkInitialization() {
		if (irodsAccount == null) {
			throw new IllegalStateException("no irodsAccount was set");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalStateException(
					"no irodsAccessObjectFactory was set");
		}

		if (transferManager == null) {
			throw new IllegalStateException("no transferManager was set");
		}

		if (fileTreeDiffUtility == null) {
			throw new IllegalStateException("no fileTreeDiffUtility was set");
		}
	}

}
