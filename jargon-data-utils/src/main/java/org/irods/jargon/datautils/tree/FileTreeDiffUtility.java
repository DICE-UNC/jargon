package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.Date;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to create a diff between two file trees. These trees may be either
 * local or iRODS.
 * <p/>
 * Note that this method will be passed an <code>IRODSAccessObjectFactory</code>
 * , and this class assumes that the underlying iRODS connection will be closed
 * outside of the scope of this object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileTreeDiffUtility {

	private static Logger log = LoggerFactory
			.getLogger(FileTreeDiffUtility.class);

	public static FileTreeModel generateDiffLocalToIRODS(
			final File localFileRoot, final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final String irodsAbsolutePath) throws JargonException {

		if (localFileRoot == null) {
			throw new IllegalArgumentException("null LocalFileRoot");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (!localFileRoot.exists()) {
			throw new JargonException("localFileRoot does not exist");
		}

		if (!localFileRoot.isDirectory()) {
			throw new JargonException(
					"localFileRoot is not a directory, cannot do a diff");
		}

		log.info("generateDiffLocalToIRODS() for localFileRoot:{}",
				localFileRoot.getAbsolutePath());
		log.info("irodsAbsolutePath for iRODS root:{}", irodsAbsolutePath);

		// get the iRODS file for the right hand side of the diff

		IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile rootIRODSFile = irodsFileFactory
				.instanceIRODSFile(irodsAbsolutePath);

		if (!rootIRODSFile.exists()) {
			throw new JargonException(
					"root iRODS file does not exist, cannot do a diff");
		}

		if (!rootIRODSFile.isDirectory()) {
			throw new JargonException(
					"irodsFile is not a directory, cannot do a diff");
		}

		log.debug("established that local file is a directory");

		// I have a local directory and an iRODS directory. Set the local
		// directory as the root node for a common point of reference
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCreatedAt(new Date(localFileRoot.lastModified()));
		entry.setModifiedAt(entry.getCreatedAt());
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(localFileRoot.getParent());
		entry.setPathOrName(localFileRoot.getAbsolutePath());
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(
				DiffType.DIRECTORY_NO_DIFF, entry);
		FileTreeNode fileTreeNode = new FileTreeNode(diffEntry);
		log.debug("set root node to:{}", fileTreeNode);
		FileTreeModel fileTreeModel = new FileTreeModel(fileTreeNode);

		diffTwoFiles(fileTreeNode, localFileRoot,
				localFileRoot.getAbsolutePath(), (IRODSFileImpl) rootIRODSFile,
				rootIRODSFile.getAbsolutePath());
		return fileTreeModel;
	}

	private static int diffTwoFiles(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final String leftHandSideRootPath,
			final File rightHandSide, final String rightHandSideRootPath)
			throws JargonException {

		// get the relative paths of each side beneath the root so we compare
		// apples to apples
		String leftHandSideAsRelativePath = leftHandSide.getAbsolutePath()
				.substring(leftHandSideRootPath.length());
		String rightHandSideAsRelativePath = rightHandSide.getAbsolutePath()
				.substring(rightHandSideRootPath.length());

		log.debug("lhs as relativePath:{}", leftHandSideAsRelativePath);
		log.debug("rhs as relativePath:{}", rightHandSideAsRelativePath);

		int compValue = leftHandSideAsRelativePath
				.compareTo(rightHandSideAsRelativePath);
		log.debug("comp value is:{}", compValue);
		
		int fileMatchIndex;

		if (compValue < 0) {
			log.debug("lhs < rhs");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.LEFT_HAND_PLUS);
			currentFileTreeNode.add(new FileTreeNode(entry));
			fileMatchIndex = 1;
		} else if (compValue > 0) {
			log.debug("lhs > rhs");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					rightHandSide, DiffType.RIGHT_HAND_PLUS);
			currentFileTreeNode.add(new FileTreeNode(entry));
			fileMatchIndex = -1;
		} else {
			log.debug("file name match");

			processFileNameMatched(currentFileTreeNode, leftHandSide,
					leftHandSideRootPath, rightHandSide, rightHandSideRootPath,
					leftHandSideAsRelativePath);
			
			fileMatchIndex = 0;
		}

		return fileMatchIndex;

	}

	/**
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param leftHandSideAsRelativePath
	 * @throws JargonException
	 */
	private static void processFileNameMatched(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final String leftHandSideRootPath, final File rightHandSide,
			final String rightHandSideRootPath,
			String leftHandSideAsRelativePath) throws JargonException {
		boolean lhsFile = leftHandSide.isFile();
		boolean rhsFile = rightHandSide.isFile();

		if (lhsFile && rhsFile) {
			log.debug("file compare");
			return;
		} else if (lhsFile != rhsFile) {
			log.warn("a file is being compared to a directory of the same name");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.FILE_NAME_DIR_NAME_COLLISION);
			currentFileTreeNode.add(new FileTreeNode(entry));
			return;
		}

		FileTreeNode parentNode;
		// the root node has already been added
		if (leftHandSideAsRelativePath.isEmpty()) {
			parentNode = currentFileTreeNode;
		} else {
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.DIRECTORY_NO_DIFF);
			parentNode = new FileTreeNode(entry);
			currentFileTreeNode.add(parentNode);
		}

		// set up the new root node in the compare tree, these are both
		// directories

		File[] lhsChildren = leftHandSide.listFiles();
		File[] rhsChildren = rightHandSide.listFiles();

		int lhMatchOrPass;
		int j = 0;
		for (int i = 0; i < lhsChildren.length; i++) {
			while(j < rhsChildren.length) {
				lhMatchOrPass = diffTwoFiles(parentNode, lhsChildren[i],
						leftHandSideRootPath, rhsChildren[j],
						rightHandSideRootPath);
				
				if (lhMatchOrPass == -1) {
					// left hand side is greater than rhs, so keep pinging the rhs
					j++;
				} else if (lhMatchOrPass == 0) {
				    // i was matched, so advance both
					j++;
					break;
				} else {
					// rhs was greater, don't advance rhs
					break;
				}
			}
		}
	}

	/**
	 * @param leftHandSide
	 * @param diffType
	 * @return
	 */
	private static FileTreeDiffEntry buildFileTreeDiffEntryForFile(
			final File leftHandSide, final DiffType diffType) {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCreatedAt(new Date(leftHandSide.lastModified()));
		entry.setModifiedAt(entry.getCreatedAt());
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(leftHandSide.getParent());
		entry.setPathOrName(leftHandSide.getAbsolutePath());
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(diffType,
				entry);
		return diffEntry;
	}

}
