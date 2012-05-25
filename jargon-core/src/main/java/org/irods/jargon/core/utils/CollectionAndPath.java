package org.irods.jargon.core.utils;

import java.io.Serializable;

/**
 * Simple value object for a file parent collection and child path or data object
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CollectionAndPath implements Serializable {

	private static final long serialVersionUID = 4393777934246880439L;
	private final String collectionParent;
	private final String childName;

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CollectionAndPath");
		sb.append("\n   collectionParent:");
		sb.append(collectionParent);
		sb.append("\n   childName:");
		sb.append(childName);
		return sb.toString();
	}

	/**
	 * @param collectionParent
	 * @param childName
	 */
	public CollectionAndPath(String collectionParent, String childName) {

		if (collectionParent == null || collectionParent.isEmpty()) {
			throw new IllegalArgumentException("null or empty collectionParent");
		}

		if (childName == null || childName.isEmpty()) {
			throw new IllegalArgumentException("null or empty childName");
		}

		this.collectionParent = collectionParent;
		this.childName = childName;
	}

	/**
	 * @return the childName
	 */
	public String getChildName() {
		return childName;
	}

	/**
	 * @return the collectionParent
	 */
	public String getCollectionParent() {
		return collectionParent;
	}
	
	
}
