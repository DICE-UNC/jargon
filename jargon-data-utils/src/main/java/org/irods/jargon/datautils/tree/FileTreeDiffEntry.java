package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Entry that describes a collection or data object (local or in iRODS) that is a difference between two trees.  The object contains a description of 
 * the data object or collection, and whether it is
 *  
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileTreeDiffEntry {

	public enum DiffType{ LEFT_HAND_PLUS, RIGHT_HAND_PLUS, LEFT_HAND_NEWER, RIGHT_HAND_NEWER, DIRECTORY_NO_DIFF, FILE_NAME_DIR_NAME_COLLISION}
	private final DiffType diffType;
	
	private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;
	
	/**
	 * Static instance method creates a new immutable entry
	 * @param diffType {@link DiffType} enum value that describes the difference type.
	 * @param collectionAndDataObjectListingEntry {@link CollectionAndDataObjectListingEntry} that describes the file or collection in the diff.
	 * @return
	 */
	public static FileTreeDiffEntry instance(final DiffType diffType, final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		return new FileTreeDiffEntry(diffType, collectionAndDataObjectListingEntry);
	}

	/**
	 * Private constructor
	 * @param diffType
	 * @param collectionAndDataObjectListingEntry
	 */
	private FileTreeDiffEntry(final DiffType diffType, final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		
		if (diffType == null) {
			throw new IllegalArgumentException("null diffType");
		}
		
		if (collectionAndDataObjectListingEntry == null) {
			throw new IllegalArgumentException("null collectionAndDataObjectListingEntry");
		}
		
		this.diffType = diffType;
		this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("fileTreeDiffEntry");
		sb.append("\n   diffType:");
		sb.append(diffType);
		sb.append("\n   collectionAndDataObjectListingEntry:");
		sb.append(collectionAndDataObjectListingEntry);
		return sb.toString();
	}
		
	public DiffType getDiffType() {
		return diffType;
	}
	
	public CollectionAndDataObjectListingEntry getCollectionAndDataObjectListingEntry() {
		return collectionAndDataObjectListingEntry;
	}

}
