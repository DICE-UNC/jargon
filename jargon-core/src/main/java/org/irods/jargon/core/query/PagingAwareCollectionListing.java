/**
 *
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

/**
 * This object helps deal with a complication of listing the contents of an
 * iRODS collection, specifically, that the sub-collections and data objects
 * underneath the given path are derived from two separate queries. Each of
 * these queries might have different paging statuses.
 * <p/>
 * This object returns such a mixed listing from the multiple queries, along
 * with properties that can assist in comprehending the paging status so that
 * subsequent queries can be made.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PagingAwareCollectionListing {

	public enum PagingStyle {
		NONE, CONTINUOUS, SPLIT_COLLECTIONS_AND_FILES
	}

	private PagingAwareCollectionListingDescriptor pagingAwareCollectionListingDescriptor;

	/**
	 * List from the query, will contain a set of collections and data objects
	 * with a paging status reflected in this object.
	 */
<<<<<<< HEAD
	private List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries;
=======
	private List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PagingAwareCollectionListing");
		sb.append("\n\tcollectionOffset:");
		sb.append(collectionsOffset);
		sb.append("\n\tcollectionsCount:");
		sb.append(collectionsCount);
		sb.append("\n\tcollectionsTotalRecords:");
		sb.append(collectionsTotalRecords);
		sb.append("\n\tcollectionsComplete:");
		sb.append(collectionsComplete);
		sb.append("\n\tdataObjectsOffset:");
		sb.append(dataObjectsOffset);
		sb.append("\n\tdataObjectsCount:");
		sb.append(dataObjectsCount);
		sb.append("\n\tdataObjectsTotalRecords:");
		sb.append(dataObjectsTotalRecords);
		sb.append("\n\tdataObjectsComplete:");
		sb.append(dataObjectsComplete);
		sb.append("\n\tpageSizeUtilized:");
		sb.append(pageSizeUtilized);
		return sb.toString();
	}
>>>>>>> master

	/**
	 * Default (no-values) constructor for simple value object
	 */
	public PagingAwareCollectionListing() {
		this.pagingAwareCollectionListingDescriptor = new PagingAwareCollectionListingDescriptor();
		this.collectionAndDataObjectListingEntries = new ArrayList<CollectionAndDataObjectListingEntry>();
	}

	public List<CollectionAndDataObjectListingEntry> getCollectionAndDataObjectListingEntries() {
		return collectionAndDataObjectListingEntries;
	}

	public void setCollectionAndDataObjectListingEntries(
			List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries) {
		this.collectionAndDataObjectListingEntries = collectionAndDataObjectListingEntries;
	}

	public PagingAwareCollectionListingDescriptor getPagingAwareCollectionListingDescriptor() {
		return pagingAwareCollectionListingDescriptor;
	}

	public void setPagingAwareCollectionListingDescriptor(
			PagingAwareCollectionListingDescriptor pagingAwareCollectionListingDescriptor) {
		this.pagingAwareCollectionListingDescriptor = pagingAwareCollectionListingDescriptor;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("PagingAwareCollectionListing [");
		if (pagingAwareCollectionListingDescriptor != null) {
			builder.append("pagingAwareCollectionListingDescriptor=");
			builder.append(pagingAwareCollectionListingDescriptor);
			builder.append(", ");
		}
		if (collectionAndDataObjectListingEntries != null) {
			builder.append("collectionAndDataObjectListingEntries=");
			builder.append(collectionAndDataObjectListingEntries.subList(0,
					Math.min(collectionAndDataObjectListingEntries.size(),
							maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

}
