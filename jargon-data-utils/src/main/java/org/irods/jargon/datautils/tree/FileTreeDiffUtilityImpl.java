package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.datautils.AbstractDataUtilsService;
import org.irods.jargon.datautils.tree.FileOrDirFilter.FilterFor;
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
public class FileTreeDiffUtilityImpl extends AbstractDataUtilsService implements
		FileTreeDiffUtility {

	private static Logger log = LoggerFactory
			.getLogger(FileTreeDiffUtilityImpl.class);

	private DataObjectAO dataObjectAO = null;

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

	@Override
	public boolean verifyLocalAndIRODSTreesMatch(final File localFileRoot,
			final String irodsAbsolutePath,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		log.info("verifyLocalAndIRODSTreesMatch");

		FileTreeModel diffModel = generateDiffLocalToIRODS(localFileRoot,
				irodsAbsolutePath, timestampForLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);

		return assertNoDiffsInTree((FileTreeNode) diffModel.getRoot());

	}

	private boolean assertNoDiffsInTree(final FileTreeNode fileTreeNode) {

		FileTreeDiffEntry entry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		if (entry.getDiffType() != FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF) {
			log.warn("diff found when not expected:{}", entry);
			return false;
		}

		FileTreeNode childNode = null;
		boolean noDiffs = true;
		@SuppressWarnings("unchecked")
		Enumeration<FileTreeNode> children = fileTreeNode.children();
		while (children.hasMoreElements()) {
			childNode = children.nextElement();
			noDiffs = assertNoDiffsInTree(childNode);
			if (noDiffs) {
				break;
			}
		}

		return noDiffs;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.tree.FileTreeDiffUtility#generateDiffLocalToIRODS
	 * (java.io.File, java.lang.String, long, long)
	 */
	@Override
	public FileTreeModel generateDiffLocalToIRODS(final File localFileRoot,
			final String irodsAbsolutePath,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
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

		if (timestampForLastSynchLeftHandSide < 0) {
			throw new IllegalArgumentException(
					"timestampForLastSynchLeftHandSide is less than zero");
		}

		if (timestampForLastSynchRightHandSide < 0) {
			throw new IllegalArgumentException(
					"timestampForLastSynchRightHandSide is less than zero");
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
				timestampForLastSynchLeftHandSide,
				timestampForLastSynchRightHandSide);
		return fileTreeModel;
	}

	/**
	 * Given two relative paths, do the diff. This is the recursive call that
	 * will descend into child directories and update a shared tree model.
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param timestampforLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @return
	 * @throws JargonException
	 */
	private int diffTwoFiles(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final String leftHandSideRootPath,
			final File rightHandSide, final String rightHandSideRootPath,
			final long timestampforLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		// get the relative paths of each side beneath the root so we compare
		// apples to apples
		String leftHandSideAsRelativePath = leftHandSide.getAbsolutePath()
				.substring(leftHandSideRootPath.length());
		String rightHandSideAsRelativePath = rightHandSide.getAbsolutePath()
				.substring(rightHandSideRootPath.length());

		log.debug("diffTwoFiles in currentTreeNode:{}", currentFileTreeNode);

		/*
		 * On Win, filenames come across with a // as the delim, replace with a
		 * single / to normalize for comparison
		 */
		leftHandSideAsRelativePath = leftHandSideAsRelativePath.replace('\\',
				'/');

		log.debug("lhs as relativePath:{}", leftHandSideAsRelativePath);
		log.debug("rhs as relativePath:{}", rightHandSideAsRelativePath);

		int compValue = leftHandSideAsRelativePath
				.compareTo(rightHandSideAsRelativePath);
		log.debug("comp value is:{}", compValue);

		int fileMatchIndex;

		if (compValue < 0) {
			log.debug("lhs < rhs");
			log.debug("lhs timestamp:{}", leftHandSide.lastModified());
			log.debug("lhs cutoff:{}", timestampforLastSynchLeftHandSide);
			if (timestampforLastSynchLeftHandSide == NO_TIMESTAMP_CHECKS || true) { // FIXME:
				// mode
				// leftHandSide.lastModified()
				// >
				// timestampforLastSynchLeftHandSide)
				// {
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						leftHandSide, DiffType.LEFT_HAND_PLUS, 0, 0);
				currentFileTreeNode.add(new FileTreeNode(entry));
				log.info("left hand plus generated:{}", entry);
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
			if (timestampForLastSynchRightHandSide == NO_TIMESTAMP_CHECKS || true) { // rightHandSide.lastModified()
																						// >
																						// timestampForLastSynchRightHandSide)
																						// {
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						rightHandSide, DiffType.RIGHT_HAND_PLUS, 0, 0);
				log.info("right hand plus generated:{}", entry);
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
					timestampforLastSynchLeftHandSide,
					timestampForLastSynchRightHandSide);
			fileMatchIndex = 0;
		}

		return fileMatchIndex;

	}

	/**
	 * Two relative paths are matched. Decide if they are files or directories,
	 * and diff appropriately.
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param leftHandSideAsRelativePath
	 * @param timestampForLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws JargonException
	 */
	private void processFileNameMatched(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final String leftHandSideRootPath,
			final File rightHandSide, final String rightHandSideRootPath,
			final String leftHandSideAsRelativePath,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		boolean lhsFile = leftHandSide.isFile();
		boolean rhsFile = rightHandSide.isFile();

		if (lhsFile && rhsFile) {
			compareTwoMatchedFiles(currentFileTreeNode, leftHandSide,
					rightHandSide, timestampForLastSynchLeftHandSide,
					timestampForLastSynchRightHandSide);
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
					timestampForLastSynchLeftHandSide,
					timestampForLastSynchRightHandSide);
		}
	}

	/**
	 * I've matched two directories by relative path, proceed to diff them
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param leftHandSideRootPath
	 * @param rightHandSide
	 * @param rightHandSideRootPath
	 * @param leftHandSideAsRelativePath
	 * @param timestampForLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws JargonException
	 */
	private void compareTwoEqualDirectories(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final String leftHandSideRootPath, final File rightHandSide,
			final String rightHandSideRootPath,
			final String leftHandSideAsRelativePath,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {

		log.info("\n\n************************************\n************************************\n\n");
		log.info("comparing two equal directories");
		log.info("   lhs dir:{}", leftHandSide.getAbsolutePath());
		log.info("   rhs dir:{}", rightHandSide.getAbsolutePath());

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

		Comparator<File> fileNameComparator = new FileNameComparator();

		log.debug("inspecting for files in this pass using a filter for files");
		FileOrDirFilter filter = new FileOrDirFilter(FilterFor.FILE);
		File[] lhsChildren = leftHandSide.listFiles(filter);
		Arrays.sort(lhsChildren, fileNameComparator);

		log.debug("lhs files in dir:{}", lhsChildren);
		File[] rhsChildren = rightHandSide.listFiles(filter);
		Arrays.sort(rhsChildren, fileNameComparator);
		log.debug("rhs files in dir:{}", rhsChildren);

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
			log.debug("inspecting lhs file:{}", lhsFile.getAbsolutePath());
			if (!lhsFile.isFile()) {
				log.debug("filtering for files only, this is a dir");
				continue;
			}

			if (j >= rhsChildren.length) {
				lhsChildIsUnmatched(currentFileTreeNode, lhsFile,
						timestampForLastSynchLeftHandSide,
						timestampForLastSynchRightHandSide);

			} else {
				while (j < rhsChildren.length) {

					lhMatchOrPass = diffTwoFiles(parentNode, lhsFile,
							leftHandSideRootPath, rhsChildren[j],
							rightHandSideRootPath,
							timestampForLastSynchLeftHandSide,
							timestampForLastSynchRightHandSide);

					if (lhMatchOrPass == -1) {
						// left hand side is greater than rhs, so keep pinging
						// the
						// rhs
						j++;
						log.info("advance rhs pointer");
					} else if (lhMatchOrPass == 0) {
						// i was matched, so advance both
						j++;
						log.info("advance rhs pointer");
						break;
					} else {
						// rhs was greater, don't advance rhs
						break;
					}
				}
			}
		}

		log.info("looking for unmatched rhs files");
		/*
		 * the match is driven by the lhs file. Once I've exhausted those, I
		 * need to see if any unmatched rhs files exist
		 */

		File rhsFile;

		for (; j < rhsChildren.length; j++) {
			rhsFile = rhsChildren[j];
			if (!rhsFile.isFile()) {
				continue;
			}

			log.debug("unaccounted for rhs file: {}", rhsFile.getAbsolutePath());
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(rhsFile,
					DiffType.RIGHT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
		}

		log.info("processing matches on dirs now");

		filter = new FileOrDirFilter(FilterFor.DIR);
		lhsChildren = leftHandSide.listFiles(filter);
		Arrays.sort(lhsChildren, fileNameComparator);
		log.debug("lhs files in dir:{}", lhsChildren);
		rhsChildren = rightHandSide.listFiles(filter);
		Arrays.sort(rhsChildren, fileNameComparator);
		log.debug("rhs files in dir:{}", rhsChildren);

		j = 0;
		// files done, now match collections (platform by platform the ordering
		// may be different iRODS vs. local)
		// use the lhs as the point of ref, ping each child and do a match/merge
		// w/rhs
		for (File element : lhsChildren) {
			log.debug("inspecting lhs dir, looking for dirs only:{}",
					element.getAbsolutePath());

			log.debug("j is:{}", j);
			log.debug("rhsChildren.length() is: {}", rhsChildren.length);

			if (j >= rhsChildren.length) {
				log.debug("left hand side file is unmatched for : {}",
						element.getAbsolutePath());
				lhsChildIsUnmatched(currentFileTreeNode, element,
						timestampForLastSynchLeftHandSide,
						timestampForLastSynchRightHandSide);
			} else {
				while (j < rhsChildren.length) {

					if (rhsChildren[j].isFile()) {
						log.debug("rhs is a file, ignore");
						j++;
						continue;
					}

					lhMatchOrPass = diffTwoFiles(parentNode, element,
							leftHandSideRootPath, rhsChildren[j],
							rightHandSideRootPath,
							timestampForLastSynchLeftHandSide,
							timestampForLastSynchRightHandSide);

					log.info("checking match or pass for a lhs dir got:{}",
							lhMatchOrPass);

					if (lhMatchOrPass == -1) {
						// left hand side is greater than rhs, so keep pinging
						// the
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

		/*
		 * the match is driven by the lhs file. Once I've exhausted those, I
		 * need to see if any unmatched rhs collections exist
		 */

		for (; j < rhsChildren.length; j++) {
			rhsFile = rhsChildren[j];
			if (rhsFile.isFile()) {
				continue;
			}

			log.debug("unaccounted for rhs collection: {}",
					rhsFile.getAbsolutePath());
			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(rhsFile,
					DiffType.RIGHT_HAND_PLUS, 0, 0);
			currentFileTreeNode.add(new FileTreeNode(entry));
		}
	}

	/**
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param timestampForLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @param lhsFile
	 */
	private void lhsChildIsUnmatched(final FileTreeNode currentFileTreeNode,
			final File leftHandSide,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide) {

		log.info("lhsChildIsUnmatched:{}", leftHandSide.getAbsolutePath());
		log.info("lhs last synch:{}", timestampForLastSynchLeftHandSide);
		log.info("leftHandSide lastModified:{}", leftHandSide.lastModified());

		// lhs file has no match, right now this synchs up to irods
		log.debug("unaccounted for lhs file: {}",
				leftHandSide.getAbsolutePath());
		FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(leftHandSide,
				DiffType.LEFT_HAND_PLUS, 0, 0);
		currentFileTreeNode.add(new FileTreeNode(entry));
	}

	/**
	 * I've matched two files by relative paths. Now inspect for changes and
	 * generate any appropriate diff. If either the right or left timestamp is
	 * set to no checks, then timestamps are not checked at all.
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param rightHandSide
	 * @param timestampForLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 * @throws JargonException
	 */
	private void compareTwoMatchedFiles(final FileTreeNode currentFileTreeNode,
			final File leftHandSide, final File rightHandSide,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide)
			throws JargonException {
		log.debug("file compare");

		if (leftHandSide.length() != rightHandSide.length()) {

			FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
					leftHandSide, DiffType.FILE_OUT_OF_SYNCH,
					rightHandSide.length(), rightHandSide.lastModified());
			log.debug("files differ on length:{}", entry);
			currentFileTreeNode.add(new FileTreeNode(entry));
		} else {
			String lhsChecksum = LocalFileUtils
					.md5ByteArrayToString(LocalFileUtils
							.computeMD5FileCheckSumViaAbsolutePath(leftHandSide
									.getAbsolutePath()));
			log.debug("left hand side checksum:{}", lhsChecksum);
			String rhsChecksum = getIRODSChecksumOnDataObject(rightHandSide);
			if (lhsChecksum.equals(rhsChecksum)) {
				log.debug("checksum match, files are same");
			} else {
				log.debug("files differ on checksum");
				FileTreeDiffEntry entry = buildFileTreeDiffEntryForFile(
						leftHandSide, DiffType.FILE_OUT_OF_SYNCH,
						rightHandSide.length(), rightHandSide.lastModified());
				log.debug("files differ on checksum:{}", entry);

				currentFileTreeNode.add(new FileTreeNode(entry));
			}
		}
	}

	/**
	 * Two files are different. Each side has been updated since the last
	 * synchronization.
	 * 
	 * @param currentFileTreeNode
	 * @param leftHandSide
	 * @param rightHandSide
	 * @param timestampForLastSynchLeftHandSide
	 * @param timestampForLastSynchRightHandSide
	 */
	private void twoFilesDifferAndBothArePostLastSynch(
			final FileTreeNode currentFileTreeNode, final File leftHandSide,
			final File rightHandSide,
			final long timestampForLastSynchLeftHandSide,
			final long timestampForLastSynchRightHandSide) {

		log.debug("twoFilesDifferAndBothArePostLastSynch");

		if (leftHandSide.length() == rightHandSide.length()) {
			log.debug("lengths are same, don't treat as diff");
			return;
		}

		// lengths are different, pick a file.

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
			log.warn(
					"unable to determine any differences between two files when lhs is:{}",
					leftHandSide.getAbsolutePath());
			log.warn("   and rhs is:{}", rightHandSide.getAbsolutePath());
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
		entry.setDataSize(diffFile.length());

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

	private String getIRODSChecksumOnDataObject(final File irodsFile)
			throws JargonException {
		if (dataObjectAO == null) {
			dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount);
		}
		return dataObjectAO
				.computeMD5ChecksumOnDataObject((IRODSFile) irodsFile);
	}

}
