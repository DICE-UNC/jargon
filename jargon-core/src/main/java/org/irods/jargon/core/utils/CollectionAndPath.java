package org.irods.jargon.core.utils;

import java.io.Serializable;

/**
 * Simple value object for a file parent collection and child path or data
 * object
 * 
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
	public CollectionAndPath(final String collectionParent,
			final String childName) {

		if (collectionParent == null) {
			throw new IllegalArgumentException("null  collectionParent");
		}

		if (childName == null) {
			throw new IllegalArgumentException("null  childName");
		}

		if (collectionParent.isEmpty() && childName.isEmpty()) {
			this.childName = "/";
			this.collectionParent = "";
		} else {
			this.collectionParent = collectionParent;
			this.childName = childName;
		}
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
