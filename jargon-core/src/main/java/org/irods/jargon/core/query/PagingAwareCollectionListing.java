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
	private List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries;

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
