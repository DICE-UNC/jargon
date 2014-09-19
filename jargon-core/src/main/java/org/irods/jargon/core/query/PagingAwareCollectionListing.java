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

	/**
	 * Absolute path to the parent of the listed contents as requested
	 */
	private String parentAbsolutePath = "";

	/**
	 * List of <code>String</code> path components for the parent listing
	 */
	private List<String> pathComponents = new ArrayList<String>();

	/**
	 * Represents the paging style supported by the underlying source of the
	 * listing. For example, the iRODS iCAT treats collections and data objects
	 * as separate entities with a paging status for each type, while other
	 * listings might have a single source.
	 */
	private PagingStyle pagingStyle = PagingStyle.CONTINUOUS;

	/**
	 * Offset into collections represented by the results, if the mode is mixed
	 * (collections and data objects together, this is just the offset into the
	 * whole mess
	 */
	private int offset = 0;

	/**
	 * Offset into data objects represented by the results
	 */
	private int dataObjectsOffset = 0;

	/**
	 * In split mode, Count of collections in results, will be 0 if no
	 * collections. In mixed mode, the total count in results.
	 * <p/>
	 * To differentiate from total records, the count is the total number of
	 * results in this page.
	 */
	private int count = 0;

	/**
	 * If not split count, the total records available in the catalog (if
	 * available), if split count, this will contain the total number of
	 * collections
	 */
	private int totalRecords = 0;

	/**
	 * Count of files in results, will be 0 if no files
	 * <p/>
	 * To differentiate from total records, the count is the total number of
	 * results in this page.
	 */
	private int dataObjectsCount = 0;

	/**
	 * Total data object records available in the catalog (may not be available
	 * on all databases)
	 */
	private int dataObjectsTotalRecords = 0;

	/**
	 * Indicates whether the set of collections is complete, or whether more
	 * results exist. Will be <code>true</code> if complete OR if no collections
	 * exist
	 */
	private boolean collectionsComplete = true;

	/**
	 * Indicates whether the set of data objects is complete, or whether more
	 * results exist. Will be <code>true</code> if complete OR if no data
	 * objects exist
	 */
	private boolean dataObjectsComplete = true;

	/**
	 * Reflects the page size
	 */
	private int pageSizeUtilized = 0;

	/**
	 * List from the query, will contain a set of collections and data objects
	 * with a paging status reflected in this object.
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
		sb.append(offset);
		sb.append("\n\tcollectionsCount:");
		sb.append(count);
		sb.append("\n\tcollectionsTotalRecords:");
		sb.append(totalRecords);
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

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public int getDataObjectsOffset() {
		return dataObjectsOffset;
	}

	public void setDataObjectsOffset(final int dataObjectsOffset) {
		this.dataObjectsOffset = dataObjectsOffset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(final int count) {
		this.count = count;
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

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(final int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getDataObjectsTotalRecords() {
		return dataObjectsTotalRecords;
	}

	public void setDataObjectsTotalRecords(final int dataObjectsTotalRecords) {
		this.dataObjectsTotalRecords = dataObjectsTotalRecords;
	}

	public void setPagingStyle(PagingStyle pagingStyle) {
		this.pagingStyle = pagingStyle;
	}

	public PagingStyle getPagingStyle() {
		return pagingStyle;
	}

	public String getParentAbsolutePath() {
		return parentAbsolutePath;
	}

	public List<String> getPathComponents() {
		return pathComponents;
	}

	public void setParentAbsolutePath(String parentAbsolutePath) {
		this.parentAbsolutePath = parentAbsolutePath;
	}

	public void setPathComponents(List<String> pathComponents) {
		this.pathComponents = pathComponents;
	}

}
