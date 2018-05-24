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
 * <p>
 * This object returns such a mixed listing from the multiple queries, along
 * with properties that can assist in comprehending the paging status so that
 * subsequent queries can be made.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PagingAwareCollectionListing {

	/**
	 * Offset into collections represented by the results
	 */
	private int collectionsOffset = 0;

	/**
	 * Offset into data objects represented by the results
	 */
	private int dataObjectsOffset = 0;

	/**
	 * Count of collections in results, will be 0 if no collections
	 */
	private int collectionsCount = 0;

	/**
	 * Total records available in the catalog (may not be available on all
	 * databases)
	 */
	private int collectionsTotalRecords = 0;

	/**
	 * Count of files in results, will be 0 if no files
	 */
	private int dataObjectsCount = 0;

	/**
	 * Total records available in the catalog (may not be available on all
	 * databases)
	 */
	private int dataObjectsTotalRecords = 0;

	/**
	 * Indicates whether the set of collections is complete, or whether more results
	 * exist. Will be {@code true} if complete OR if no collections exist
	 */
	private boolean collectionsComplete = true;

	/**
	 * Indicates whether the set of data objects is complete, or whether more
	 * results exist. Will be {@code true} if complete OR if no data objects exist
	 */
	private boolean dataObjectsComplete = true;

	/**
	 * Reflects the page size
	 */
	private int pageSizeUtilized = 0;

	/**
	 * List from the query, will contain a set of collections and data objects with
	 * a paging status reflected in this object.
	 */
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

	/**
	 * Default (no-values) constructor for simple value object
	 */
	public PagingAwareCollectionListing() {
	}

	public int getCollectionsOffset() {
		return collectionsOffset;
	}

	public void setCollectionsOffset(final int collectionsOffset) {
		this.collectionsOffset = collectionsOffset;
	}

	public int getDataObjectsOffset() {
		return dataObjectsOffset;
	}

	public void setDataObjectsOffset(final int dataObjectsOffset) {
		this.dataObjectsOffset = dataObjectsOffset;
	}

	public int getCollectionsCount() {
		return collectionsCount;
	}

	public void setCollectionsCount(final int collectionsCount) {
		this.collectionsCount = collectionsCount;
	}

	public int getDataObjectsCount() {
		return dataObjectsCount;
	}

	public void setDataObjectsCount(final int dataObjectsCount) {
		this.dataObjectsCount = dataObjectsCount;
	}

	public boolean isCollectionsComplete() {
		return collectionsComplete;
	}

	public void setCollectionsComplete(final boolean collectionsComplete) {
		this.collectionsComplete = collectionsComplete;
	}

	public boolean isDataObjectsComplete() {
		return dataObjectsComplete;
	}

	public void setDataObjectsComplete(final boolean dataObjectsComplete) {
		this.dataObjectsComplete = dataObjectsComplete;
	}

	public int getPageSizeUtilized() {
		return pageSizeUtilized;
	}

	public void setPageSizeUtilized(final int pageSizeUtilized) {
		this.pageSizeUtilized = pageSizeUtilized;
	}

	public List<CollectionAndDataObjectListingEntry> getCollectionAndDataObjectListingEntries() {
		return collectionAndDataObjectListingEntries;
	}

	public void setCollectionAndDataObjectListingEntries(
			final List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries) {
		this.collectionAndDataObjectListingEntries = collectionAndDataObjectListingEntries;
	}

	public int getCollectionsTotalRecords() {
		return collectionsTotalRecords;
	}

	public void setCollectionsTotalRecords(final int collectionsTotalRecords) {
		this.collectionsTotalRecords = collectionsTotalRecords;
	}

	public int getDataObjectsTotalRecords() {
		return dataObjectsTotalRecords;
	}

	public void setDataObjectsTotalRecords(final int dataObjectsTotalRecords) {
		this.dataObjectsTotalRecords = dataObjectsTotalRecords;
	}

}
