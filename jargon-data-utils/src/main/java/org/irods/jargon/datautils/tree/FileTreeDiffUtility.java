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

	/**
	 * Generate a tree model that is the diff between the left hand side and the
	 * iRODS file system on the right hand side. This diff tree will depict the
	 * common directory structure and indicate places where files or collections
	 * differ from the perspective of the 'left hand side' file.
	 * <p/>
	 * The <code>FileTreeDiffEntry</code> will indicate the type of difference
	 * found. This is the user data object that is kept in a
	 * <code>FileTreeNode</code> in the resulting <code>FileTreeModel</code>.
	 * <p/>
	 * This utility will compare file-to-file for data objects. Since local and
	 * iRODS time stamps are not a good indicator for direct comparison, the
	 * utility compares two file (data objects) based on length. Optionally, the
	 * iRODS timestamp can be compared to a provided timestamp value to treat
	 * files modified after the given cut-off as 'changed'.
	 * 
	 * @param localFileRoot
	 *            <code>File</code> that is the left hand side of the comparison
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that is used to connect to the
	 *            compared iRODS file system
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that is used to obtain
	 *            objects needed to work with iRODS data
	 * @param irodsAbsolutePath
	 *            <code>String</code> that is the root directory that will be
	 *            compared to the <code>localFileRoot</code> The file and
	 *            collection names will be compared relative to the respective
	 *            left hand and right hand root absolute paths
	 * @param timestampForIrodsFileThatIndicatesThatTheFileHasChanged
	 *            <code>long</code> with the timestamp that, if before the last
	 *            modified date of the given left hand and right hand files,
	 *            indicates that the file has changed. Leave as zero to turn off
	 *            this check.
	 * @return {@link FileTreeModel} with the common directory structure and any
	 *         detected diffs. The model is made of {@link FileTreeNode} objects
	 *         that contain the {@link FileTreeDiffEntry} as the user data
	 *         object.
	 * @throws JargonException
	 */
	public static FileTreeModel generateDiffLocalToIRODS(
			final File localFileRoot, final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final String irodsAbsolutePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {

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

		if (timestampForIrodsFileThatIndicatesThatTheFileHasChanged < 0) {
			throw new IllegalArgumentException(
					"timestampForIrodsFileThatIndicatesThatTheFileHasChanged is less than zero");
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
		// directory as the root node in the resulting diff tree for a common
		// point of reference
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
				rootIRODSFile.getAbsolutePath(),
				timestampForIrodsFileThatIndicatesThatTheFileHasChanged);
		return fileTreeModel;
	}

	private static int diffTwoFiles(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final String leftHandSideRootPath,
			final File rightHandSide, final String rightHandSideRootPath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
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
					leftHandSide, DiffType.LEFT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
			fileMatchIndex = 1;
		} else if (compValue > 0) {
			log.debug("lhs > rhs");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					rightHandSide, DiffType.RIGHT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
			fileMatchIndex = -1;
		} else {
			log.debug("file name match");

			processFileNameMatched(currentFileTreeNode, leftHandSide,
					leftHandSideRootPath, rightHandSide, rightHandSideRootPath,
					leftHandSideAsRelativePath,
					timestampForIrodsFileThatIndicatesThatTheFileHasChanged);

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
			final String leftHandSideAsRelativePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {

		boolean lhsFile = leftHandSide.isFile();
		boolean rhsFile = rightHandSide.isFile();

		if (lhsFile && rhsFile) {
			log.debug("file compare");

			// compare by length
			if (leftHandSide.length() != rightHandSide.length()) {
				log.debug("files differ based on length");
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						leftHandSide, DiffType.FILE_OUT_OF_SYNCH,
						rightHandSide.length(), rightHandSide.lastModified());
				currentFileTreeNode.add(new FileTreeNode(entry));
			} else if (timestampForIrodsFileThatIndicatesThatTheFileHasChanged > 0) {
				log.debug("checking file timestamp against cutoff");
				/*
				 * I have a timestamp that is a cut-off for the irods file. If
				 * the local file or iRODS file has changed since that
				 * timestamp, then a difference is noted.
				 */
				if (leftHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged
						|| rightHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
					log.debug("file mod date is after the provided timestamp cutoff");
					FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
							leftHandSide, DiffType.FILE_OUT_OF_SYNCH,
							rightHandSide.length(),
							rightHandSide.lastModified());
					currentFileTreeNode.add(new FileTreeNode(entry));
				}
			}

			return;
		} else if (lhsFile != rhsFile) {
			log.warn("a file is being compared to a directory of the same name");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.FILE_NAME_DIR_NAME_COLLISION, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
			return;
		}

		FileTreeNode parentNode;
		// the root node in the resulting diff tree has already been added when
		// starting the diff, so don't double-add
		if (leftHandSideAsRelativePath.isEmpty()) {
			parentNode = currentFileTreeNode;
		} else {
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.DIRECTORY_NO_DIFF, 0, 0);
			parentNode = new FileTreeNode(entry);
			currentFileTreeNode.add(parentNode);
		}

		// set up the new root node in the compare tree, these are both
		// directories and I'll recursively descend to the children with this
		// parent as the root.

		File[] lhsChildren = leftHandSide.listFiles();
		File[] rhsChildren = rightHandSide.listFiles();

		int lhMatchOrPass;
		int j = 0;
		// use the lhs as the point of ref, ping each child and do a match/merge
		// w/rhs
		for (File element : lhsChildren) {
			while (j < rhsChildren.length) {
				lhMatchOrPass = diffTwoFiles(parentNode, element,
						leftHandSideRootPath, rhsChildren[j],
						rightHandSideRootPath,
						timestampForIrodsFileThatIndicatesThatTheFileHasChanged);

				if (lhMatchOrPass == -1) {
					// left hand side is greater than rhs, so keep pinging the
					// rhs
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
			final File leftHandSide, final DiffType diffType,
			final long lengthRightHandSide, final long timestampRightHandSide) {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCreatedAt(new Date(leftHandSide.lastModified()));
		entry.setModifiedAt(entry.getCreatedAt());

		if (leftHandSide.isFile()) {
			entry.setObjectType(ObjectType.DATA_OBJECT);
			entry.setParentPath(leftHandSide.getParent());
			entry.setPathOrName(leftHandSide.getName());
		} else {
			entry.setObjectType(ObjectType.COLLECTION);
			entry.setParentPath(leftHandSide.getParent());
			StringBuilder sb = new StringBuilder();
			sb.append(leftHandSide.getParent());
			sb.append("/");
			sb.append(leftHandSide.getName());
			entry.setPathOrName(sb.toString());
		}
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(diffType,
				entry, lengthRightHandSide, timestampRightHandSide);
		return diffEntry;
	}
}
