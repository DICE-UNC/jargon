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
public class FileTreeDiffUtilityImpl implements FileTreeDiffUtility {

	private static Logger log = LoggerFactory
			.getLogger(FileTreeDiffUtilityImpl.class);

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	private static final int NO_TIMESTAMP_CHECKS = 0;

	/**
	 * Default constructor
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that is used to connect to the
	 *            compared iRODS file system
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that is used to obtain
	 *            objects needed to work with iRODS data
	 */
	public FileTreeDiffUtilityImpl(final IRODSAccount irodsAccount,
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.tree.FileTreeDiffUtility#generateDiffLocalToIRODS
	 * (java.io.File, java.lang.String, long)
	 */
	@Override
	public FileTreeModel generateDiffLocalToIRODS(final File localFileRoot,
			final String irodsAbsolutePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {

		if (localFileRoot == null) {
			throw new IllegalArgumentException("null LocalFileRoot");
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

	private int diffTwoFiles(final FileTreeNode currentFileTreeNode,
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
			if (timestampForIrodsFileThatIndicatesThatTheFileHasChanged == NO_TIMESTAMP_CHECKS
					|| leftHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						leftHandSide, DiffType.LEFT_HAND_PLUS, 0, 0);
				currentFileTreeNode.add(new FileTreeNode(entry));
			} else {
				// the lhs file is plus, but the mod date is before the last
				// synch. This is an indeterminate state. This could be treated
				// as an iRODS delete?
				log.debug(
						"lhs file is seen as new, but modified time is before last synch, iRODS delete?, currently no deletes done:{}",
						leftHandSide.getAbsolutePath());
			}
			fileMatchIndex = 1;
		} else if (compValue > 0) {
			log.debug("lhs > rhs");
			if (timestampForIrodsFileThatIndicatesThatTheFileHasChanged == NO_TIMESTAMP_CHECKS
					|| rightHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						rightHandSide, DiffType.RIGHT_HAND_PLUS, 0, 0);
				currentFileTreeNode.add(new FileTreeNode(entry));
			} else {
				// the rhs file is plus, but the mod date is before the last
				// synch. This is an indeterminate state. This coul dbe treated
				// as a local delete?
				log.debug("rhs file last mod:{}", rightHandSide.lastModified());
				log.debug(
						"rhs file is seen as new, but modified time is before last synch, local delete? currently no deletes done:{}",
						rightHandSide.getAbsolutePath());
			}
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
	 * Two file names have been matched. Decide on the course of action in the
	 * diff process based on whether they are data objects, or directories
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param leftHandSideAsRelativePath
	 * @throws JargonException
	 */
	private void processFileNameMatched(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final String leftHandSideRootPath,
			final File rightHandSide, final String rightHandSideRootPath,
			final String leftHandSideAsRelativePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {

		boolean lhsFile = leftHandSide.isFile();
		boolean rhsFile = rightHandSide.isFile();

		if (lhsFile && rhsFile) {
			compareTwoEqualFilesOnTimestamp(currentFileTreeNode, leftHandSide,
					rightHandSide,
					timestampForIrodsFileThatIndicatesThatTheFileHasChanged);
		} else if (lhsFile != rhsFile) {
			log.warn("a file is being compared to a directory of the same name");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.FILE_NAME_DIR_NAME_COLLISION,
					rightHandSide.length(), rightHandSide.lastModified());
			currentFileTreeNode.add(new FileTreeNode(entry));
		} else {
			compareTwoEqualDirectories(currentFileTreeNode, leftHandSide,
					leftHandSideRootPath, rightHandSide, rightHandSideRootPath,
					leftHandSideAsRelativePath,
					timestampForIrodsFileThatIndicatesThatTheFileHasChanged);
		}
	}

	/**
	 * I have matched two files by name, each is a directory. Proceed to diff
	 * them.
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param leftHandSideAsRelativePath
	 * @param timestampForIrodsFileThatIndicatesThatTheFileHasChanged
	 * @throws JargonException
	 */
	private void compareTwoEqualDirectories(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final String leftHandSideRootPath, final File rightHandSide,
			final String rightHandSideRootPath,
			final String leftHandSideAsRelativePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {
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
		int i = 0;
		// first compare files (platform by platform the ordering may be
		// different iRODS vs. local)
		// use the lhs as the point of ref, ping each child and do a match/merge
		// w/rhs
		File lhsFile = null;
		for (i = 0; i < lhsChildren.length; i++) {
			lhsFile = lhsChildren[i];
			if (!lhsFile.isFile()) {
				continue;
			}
			
			while (j < rhsChildren.length) {

				if (!rhsChildren[j].isFile()) {
					j++;
					continue;
				}

				lhMatchOrPass = diffTwoFiles(parentNode, lhsFile,
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
		
		/*
		 * the match is driven by the lhs file.  Once I've exhausted those, I need to see if any unmatched rhs files exist
		 */
		
		File rhsFile;
		
		for(; j < rhsChildren.length; j++) {
			rhsFile = rhsChildren[j];
			if (!rhsFile.isFile()) {
				continue;
			}
			
			log.debug("unaccounted for rhs file: {}", rhsFile.getAbsolutePath());
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					rhsFile, DiffType.RIGHT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
		}

		j = 0;
		// files done, now match collections (platform by platform the ordering
		// may be different iRODS vs. local)
		// use the lhs as the point of ref, ping each child and do a match/merge
		// w/rhs
		for (File element : lhsChildren) {
			if (element.isFile()) {
				continue;
			}
			while (j < rhsChildren.length) {

				if (rhsChildren[j].isFile()) {
					j++;
					continue;
				}

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
		
		/*
		 * the match is driven by the lhs file.  Once I've exhausted those, I need to see if any unmatched rhs collections exist
		 */
		
		
		for(; j < rhsChildren.length; j++) {
			rhsFile = rhsChildren[j];
			if (rhsFile.isFile()) {
				continue;
			}
			
			log.debug("unaccounted for rhs collection: {}", rhsFile.getAbsolutePath());
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					rhsFile, DiffType.RIGHT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
		}
	}

	/**
	 * I have matched two files, each is a data object. Diff this one file
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param rightHandSide
	 * @param timestampForIrodsFileThatIndicatesThatTheFileHasChanged
	 */
	private void compareTwoEqualFilesOnTimestamp(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final File rightHandSide,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException {
		log.debug("file compare");

		if (timestampForIrodsFileThatIndicatesThatTheFileHasChanged == NO_TIMESTAMP_CHECKS) {
			log.debug("comparing files without a last synch, use existing last mod data");
		} else {

			log.debug("checking file timestamp against cutoff:{}", timestampForIrodsFileThatIndicatesThatTheFileHasChanged);
			/*
			 * I have a timestamp that is a cut-off for the irods file. If the
			 * local file or iRODS file has changed since that timestamp, then a
			 * difference is noted.
			 */
			if (leftHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged
					&& rightHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
				twoFilesDifferAndBothArePostLastSynch(currentFileTreeNode,
						leftHandSide, rightHandSide);
			} else if (leftHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
				log.debug("left hand side file has been modified");
				log.debug("   rhs ts:{}", rightHandSide.lastModified());
				log.debug("   lhs ts:{}", leftHandSide.lastModified());
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						leftHandSide, DiffType.LEFT_HAND_NEWER,
						rightHandSide.length(), rightHandSide.lastModified());
				currentFileTreeNode.add(new FileTreeNode(entry));
			} else if (rightHandSide.lastModified() > timestampForIrodsFileThatIndicatesThatTheFileHasChanged) {
				log.debug("right hand side file has been modified");
				log.debug("   rhs ts:{}", rightHandSide.lastModified());
				log.debug("   lhs ts:{}", leftHandSide.lastModified());
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						rightHandSide, DiffType.RIGHT_HAND_NEWER,
						leftHandSide.length(), leftHandSide.lastModified());
				currentFileTreeNode.add(new FileTreeNode(entry));
			} else {
				log.debug("timestamps match, treat as no diff");
			}
		}
	}

	/**
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param rightHandSide
	 */
	private void twoFilesDifferAndBothArePostLastSynch(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final File rightHandSide) {
		log.debug("both files after cutoff, will pick most recent file");

		if (leftHandSide.lastModified() > rightHandSide.lastModified()) {
			log.debug("left hand side is newer");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.LEFT_HAND_NEWER,
					rightHandSide.length(), rightHandSide.lastModified());
			currentFileTreeNode.add(new FileTreeNode(entry));
		} else if (rightHandSide.lastModified() > leftHandSide.lastModified()) {
			log.debug("left hand side is newer");
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					rightHandSide, DiffType.RIGHT_HAND_NEWER,
					rightHandSide.length(), rightHandSide.lastModified());
			currentFileTreeNode.add(new FileTreeNode(entry));
		} else {
			log.debug("files are equal, skip");
		}

	}

	/**
	 * @param diffFile
	 * @param diffType
	 * @return
	 */
	private FileTreeDiffEntry buildFileTreeDiffEntryForFile(
			final File diffFile, final DiffType diffType,
			final long lengthOppositeSide, final long timestampOppositeSide) {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCreatedAt(new Date(diffFile.lastModified()));
		entry.setModifiedAt(entry.getCreatedAt());

		if (diffFile.isFile()) {
			entry.setObjectType(ObjectType.DATA_OBJECT);
			entry.setParentPath(diffFile.getParent());
			entry.setPathOrName(diffFile.getName());
		} else {
			entry.setObjectType(ObjectType.COLLECTION);
			entry.setParentPath(diffFile.getParent());
			StringBuilder sb = new StringBuilder();
			sb.append(diffFile.getParent());
			sb.append("/");
			sb.append(diffFile.getName());
			entry.setPathOrName(sb.toString());
		}
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(diffType,
				entry, lengthOppositeSide, timestampOppositeSide);
		return diffEntry;
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
}
