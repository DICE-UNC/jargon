package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Entry that describes a collection or data object (local or in iRODS) that is
 * a difference between two trees. The object contains a description of the data
 * object or collection, and whether it is
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileTreeDiffEntry {

	public enum DiffType {
		LEFT_HAND_PLUS, RIGHT_HAND_PLUS, DIRECTORY_NO_DIFF, FILE_NAME_DIR_NAME_COLLISION, LEFT_HAND_NEWER, RIGHT_HAND_NEWER, FILE_OUT_OF_SYNCH
	}

	private final DiffType diffType;
	private final long lengthOppositeFile;
	private long timestampOppositeFile;
	/**
	 * Optional field that can be filled in post tree generation that rolls up diffs in children, usually through a tree post-processing phase
	 */
	private int countOfDiffsInChildren = 0; 

	private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;
	
	/**
	 * Classify this as a diff (an actual difference was noted), or as a non-diff entry that simply describes the tree
	 * @return <code>true</code> if this is an actual diff
	 */
	public boolean isCountAsDiff() {
		boolean isDiff = true;
		if (diffType == DiffType.DIRECTORY_NO_DIFF) {
			isDiff = false;
		}
		
		return isDiff;
	}

	/**
	 * Static instance method creates a new immutable entry
	 * 
	 * @param diffType
	 *            {@link DiffType} enum value that describes the difference type
	 * @param collectionAndDataObjectListingEntry
	 *            {@link CollectionAndDataObjectListingEntry} that describes the
	 *            file or collection in the diff
	 * @param diffType
	 * @param collectionAndDataObjectListingEntry
	 * @return
	 */
	public static FileTreeDiffEntry instance(
			final DiffType diffType,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		return new FileTreeDiffEntry(diffType,
				collectionAndDataObjectListingEntry, 0, 0);
	}

	/**
	 * Static instance method creates a new immutable entry
	 * 
	 * @param diffType
	 *            {@link DiffType} enum value that describes the difference type
	 * @param collectionAndDataObjectListingEntry
	 *            {@link CollectionAndDataObjectListingEntry} that describes the
	 *            file or collection in the diff
	 * @param lengthRhsFile
	 *            <code>long</code> with the length of the right hand side file,
	 *            or 0
	 * @param timestampRhsFile
	 *            <code>long</code> with the timestamp of the right hand side
	 *            file, or 0
	 * @return
	 */
	public static FileTreeDiffEntry instance(
			final DiffType diffType,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry,
			final long lengthRhsFile, final long timestampRhsFile) {
		return new FileTreeDiffEntry(diffType,
				collectionAndDataObjectListingEntry, lengthRhsFile,
				timestampRhsFile);
	}

	/**
	 * Private constructor
	 * 
	 * @param diffType
	 * @param collectionAndDataObjectListingEntry
	 */
	private FileTreeDiffEntry(
			final DiffType diffType,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry,
			final long lengthRhsFile, final long timestampRhsFile) {

		if (diffType == null) {
			throw new IllegalArgumentException("null diffType");
		}

		if (collectionAndDataObjectListingEntry == null) {
			throw new IllegalArgumentException(
					"null collectionAndDataObjectListingEntry");
		}

		if (lengthRhsFile < 0) {
			throw new IllegalArgumentException("negative lengthRhsFile");
		}

		if (timestampRhsFile < 0) {
			throw new IllegalArgumentException("negative timestampRhsFile");
		}

		this.diffType = diffType;
		this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
		this.lengthOppositeFile = lengthRhsFile;
		this.timestampOppositeFile = timestampRhsFile;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nfileTreeDiffEntry");
		sb.append("\n   diffType:");
		sb.append(diffType);
		sb.append("\n   collectionAndDataObjectListingEntry:");
		sb.append(collectionAndDataObjectListingEntry);
		sb.append("\n   lengthOppositeFile:");
		sb.append(lengthOppositeFile);
		sb.append("\n   timestampOppositeFile");
		sb.append(timestampOppositeFile);
		sb.append("\n   countDiffsInChildren:");
		sb.append(countOfDiffsInChildren);
		return sb.toString();
	}

	public DiffType getDiffType() {
		return diffType;
	}

	public CollectionAndDataObjectListingEntry getCollectionAndDataObjectListingEntry() {
		return collectionAndDataObjectListingEntry;
	}

	public long getTimestampOppositeFile() {
		return timestampOppositeFile;
	}

	public void setTimestampOppositeFile(final long timestampOppositeFile) {
		this.timestampOppositeFile = timestampOppositeFile;
	}

	/**
	 * @return the countOfDiffsInChildren
	 */
	public int getCountOfDiffsInChildren() {
		return countOfDiffsInChildren;
	}

	/**
	 * @param countOfDiffsInChildren the countOfDiffsInChildren to set
	 */
	public void setCountOfDiffsInChildren(int countOfDiffsInChildren) {
		this.countOfDiffsInChildren = countOfDiffsInChildren;
	}

}
