package org.irods.jargon.core.query;

import java.util.List;

import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;

/**
 * General information about the paging situation, including start and end
 * indexes, total records, and other info that can be used to derive paging
 * actions (like page forward or backward)
 *
 * @author Mike Conway - DICE
 *
 */
public class PagingAwareCollectionListingDescriptor {

	private ObjStat objStat;

	/**
	 * Absolute path to the parent of the listed contents as requested
	 */
	private String parentAbsolutePath;
	/**
	 * List of <code>String</code> path components for the parent listing
	 */
	private List<String> pathComponents;
	/**
	 * Represents the paging style supported by the underlying source of the
	 * listing. For example, the iRODS iCAT treats collections and data objects as
	 * separate entities with a paging status for each type, while other listings
	 * might have a single source.
	 */
	private PagingStyle pagingStyle; // FIXME: remove paging style for pacl
	/**
	 * Offset into collections represented by the results, if the mode is mixed
	 * (collections and data objects together, this is just the offset into the
	 * whole mess
	 */
	private int offset;
	/**
	 * Offset into data objects represented by the results
	 */
	private int dataObjectsOffset;
	/**
	 * In split mode, Count of collections in results, will be 0 if no collections.
	 * In mixed mode, the total count in results.
	 * <p/>
	 * To differentiate from total records, the count is the total number of results
	 * in this page.
	 */
	private int count;
	/**
	 * If not split count, the total records available in the catalog (if
	 * available), if split count, this will contain the total number of collections
	 */
	private int totalRecords;
	/**
	 * Count of files available on the server itself
	 * <p/>
	 * To differentiate from count, the count is the total number of results in this
	 * page.
	 */
	private int dataObjectsCount;
	/**
	 * Total data object records available in the catalog (may not be available on
	 * all databases)
	 */
	private int dataObjectsTotalRecords;
	/**
	 * Indicates whether the set of collections is complete, or whether more results
	 * exist. Will be <code>true</code> if complete OR if no collections exist
	 */
	private boolean collectionsComplete;
	/**
	 * Indicates whether the set of data objects is complete, or whether more
	 * results exist. Will be <code>true</code> if complete OR if no data objects
	 * exist
	 */
	private boolean dataObjectsComplete;
	/**
	 * Reflects the page size
	 */
	private int pageSizeUtilized;

	/**
	 * Are there more colls or data objects to return?
	 *
	 * @return {@code boolean} if more results to return
	 */
	public boolean hasMore() {
		return !(dataObjectsComplete && collectionsComplete);
	}

	/**
	 * Return an {@code int} that is the absolute offset across both collections and
	 * data objects. Collections are listed first so the offset for data objects
	 * will be the total offset of the collections added to the offset of any data
	 * objects.
	 *
	 * @return {@code int} with the continuous count of data objects and
	 *         collections.
	 *         <p>
	 *         Remember that 'counts' are 1 based, so the last count value in the
	 *         listing is the offset for the next listing (offset is 0 based)
	 */
	public int computeAbsoluteNextOffset() {
		return count + dataObjectsCount;
	}

	/**
	 * Return the total records for both collections and data objects together.
	 *
	 * @return {@code int} with the total size of the set
	 */
	public int computeAbsoluteTotalSize() {
		return totalRecords + dataObjectsTotalRecords;
	}

	public PagingAwareCollectionListingDescriptor() {
	}

	public String getParentAbsolutePath() {
		return parentAbsolutePath;
	}

	public void setParentAbsolutePath(final String parentAbsolutePath) {
		this.parentAbsolutePath = parentAbsolutePath;
	}

	public List<String> getPathComponents() {
		return pathComponents;
	}

	public void setPathComponents(final List<String> pathComponents) {
		this.pathComponents = pathComponents;
	}

	public PagingStyle getPagingStyle() {
		return pagingStyle;
	}

	public void setPagingStyle(final PagingStyle pagingStyle) {
		this.pagingStyle = pagingStyle;
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

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(final int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getDataObjectsCount() {
		return dataObjectsCount;
	}

	public void setDataObjectsCount(final int dataObjectsCount) {
		this.dataObjectsCount = dataObjectsCount;
	}

	public int getDataObjectsTotalRecords() {
		return dataObjectsTotalRecords;
	}

	public void setDataObjectsTotalRecords(final int dataObjectsTotalRecords) {
		this.dataObjectsTotalRecords = dataObjectsTotalRecords;
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

	public ObjStat getObjStat() {
		return objStat;
	}

	public void setObjStat(final ObjStat objStat) {
		this.objStat = objStat;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "PagingAwareCollectionListingDescriptor [" + (objStat != null ? "objStat=" + objStat + ", " : "")
				+ (parentAbsolutePath != null ? "parentAbsolutePath=" + parentAbsolutePath + ", " : "")
				+ (pathComponents != null
						? "pathComponents=" + pathComponents.subList(0, Math.min(pathComponents.size(), maxLen)) + ", "
						: "")
				+ (pagingStyle != null ? "pagingStyle=" + pagingStyle + ", " : "") + "offset=" + offset
				+ ", dataObjectsOffset=" + dataObjectsOffset + ", count=" + count + ", totalRecords=" + totalRecords
				+ ", dataObjectsCount=" + dataObjectsCount + ", dataObjectsTotalRecords=" + dataObjectsTotalRecords
				+ ", collectionsComplete=" + collectionsComplete + ", dataObjectsComplete=" + dataObjectsComplete
				+ ", pageSizeUtilized=" + pageSizeUtilized + "]";
	}

}