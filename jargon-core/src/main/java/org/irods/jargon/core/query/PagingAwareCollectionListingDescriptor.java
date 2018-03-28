package org.irods.jargon.core.query;

import java.util.ArrayList;
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
	private PagingStyle pagingStyle;
	/**
	 * 
	 * <b>for continuous</b>
	 * <p/>
	 * The offset represents the starting point of the listing in the total
	 * available listing. So the second page of results would be pageSize
	 * 
	 * <b>for mixed</b>
	 * 
	 * Offset into collections represented by the results. The
	 * <code>dataObjectsOffset</code> will reflect the offset into data objects
	 */
	private int offset;
	/**
	 * <b>for continuous</b>
	 * <p/>
	 * This value is unused
	 * 
	 * <b>for mixed</b>
	 * 
	 * Offset into data objects represented by the results. The <code>offset</code>
	 * will reflect the offset into collections
	 */
	private int dataObjectsOffset;
	/**
	 * <b>for continuous</b>
	 * <p/>
	 * 
	 * Count of collections and data objects in current result set. The
	 * <code>dataObjectsCount</code> is unused and would be 0
	 * 
	 * <b>for mixed</b>
	 * <p/>
	 * 
	 * Count of collections in current result set, will be 0 if no collections.
	 * 
	 * <p/>
	 * To differentiate from total records, the count is the total number of results
	 * in this page. It may be the page size, or it may be less, if fewer records
	 * were returned.
	 */
	private int count;
	/**
	 * 
	 * <b>for continuous</b>
	 * <p/>
	 * 
	 * The total records available for all pages (if available).
	 * 
	 * 
	 * <b>for mixed</b>
	 * <p/>
	 * 
	 * 
	 * The total number of collections available
	 */
	private int totalRecords;
	/**
	 * Count of files in result (current page). Only used when split mode. This is
	 * usually the same as page size but may be less if fewer results were
	 * available.
	 */
	private int dataObjectsCount;
	/**
	 * Total data object records available from the source (may not be available on
	 * all databases). This is only used in split mode
	 */
	private int dataObjectsTotalRecords;
	/**
	 * <b>for continuous</b>
	 * <p/>
	 * Indicates with <code>true</code> that all records have been displayed <b>for
	 * mixed</b>
	 * <p/>
	 * Indicates that collection listing is complete, there may be data objects in
	 * this page, and more data objects on the server
	 */
	private boolean complete;
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
	 * Represents the 'pages' available and their characteristics if supported by
	 * the listing source
	 */
	private List<PagingChunk> pagingChunks = new ArrayList<>();

	/**
	 * Are there more colls or data objects to return? This is uniform across
	 * continuous and mixed listings
	 * 
	 * @return
	 */
	public boolean hasMore() {
		return !(this.dataObjectsComplete && this.complete);
	}

	/**
	 * Is there any data to show at all?
	 * 
	 * @return <code>boolean</code> that will be
	 */
	public boolean hasAnyDataToShow() {
		if (!hasMore() && count == 0 && dataObjectsCount == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Return an
	 * <code>int<code> that is the absolute offset across both collections and data objects.  Collections are listed first
	 * so the offset for data objects will be the total offset of the collections added to the offset of any data objects.
	 * 
	 * @return <code>int</code> with the continuous count of data objects and
	 *         collections.
	 *         <p/>
	 *         Remember that 'counts' are 1 based, so the last count value in the
	 *         listing is the offset for the next listing (offset is 0 based)
	 */
	public int computeAbsoluteNextOffset() {
		return this.count + this.dataObjectsCount;
	}

	/**
	 * Return the total records for both collections and data objects together.
	 * 
	 * @return <code>int</code> with the total size of the set
	 */
	public int computeAbsoluteTotalSize() {
		return this.totalRecords + this.dataObjectsTotalRecords;
	}

	public PagingAwareCollectionListingDescriptor() {
	}

	public String getParentAbsolutePath() {
		return parentAbsolutePath;
	}

	public void setParentAbsolutePath(String parentAbsolutePath) {
		this.parentAbsolutePath = parentAbsolutePath;
	}

	public List<String> getPathComponents() {
		return pathComponents;
	}

	public void setPathComponents(List<String> pathComponents) {
		this.pathComponents = pathComponents;
	}

	public PagingStyle getPagingStyle() {
		return pagingStyle;
	}

	public void setPagingStyle(PagingStyle pagingStyle) {
		this.pagingStyle = pagingStyle;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getDataObjectsOffset() {
		return dataObjectsOffset;
	}

	public void setDataObjectsOffset(int dataObjectsOffset) {
		this.dataObjectsOffset = dataObjectsOffset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getDataObjectsCount() {
		return dataObjectsCount;
	}

	public void setDataObjectsCount(int dataObjectsCount) {
		this.dataObjectsCount = dataObjectsCount;
	}

	public int getDataObjectsTotalRecords() {
		return dataObjectsTotalRecords;
	}

	public void setDataObjectsTotalRecords(int dataObjectsTotalRecords) {
		this.dataObjectsTotalRecords = dataObjectsTotalRecords;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean isDataObjectsComplete() {
		return dataObjectsComplete;
	}

	public void setDataObjectsComplete(boolean dataObjectsComplete) {
		this.dataObjectsComplete = dataObjectsComplete;
	}

	public int getPageSizeUtilized() {
		return pageSizeUtilized;
	}

	public void setPageSizeUtilized(int pageSizeUtilized) {
		this.pageSizeUtilized = pageSizeUtilized;
	}

	public ObjStat getObjStat() {
		return objStat;
	}

	public void setObjStat(ObjStat objStat) {
		this.objStat = objStat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("PagingAwareCollectionListingDescriptor [");
		if (objStat != null) {
			builder.append("objStat=").append(objStat).append(", ");
		}
		if (parentAbsolutePath != null) {
			builder.append("parentAbsolutePath=").append(parentAbsolutePath).append(", ");
		}
		if (pathComponents != null) {
			builder.append("pathComponents=").append(pathComponents.subList(0, Math.min(pathComponents.size(), maxLen)))
					.append(", ");
		}
		if (pagingStyle != null) {
			builder.append("pagingStyle=").append(pagingStyle).append(", ");
		}
		builder.append("offset=").append(offset).append(", dataObjectsOffset=").append(dataObjectsOffset)
				.append(", count=").append(count).append(", totalRecords=").append(totalRecords)
				.append(", dataObjectsCount=").append(dataObjectsCount).append(", dataObjectsTotalRecords=")
				.append(dataObjectsTotalRecords).append(", complete=").append(complete).append(", dataObjectsComplete=")
				.append(dataObjectsComplete).append(", pageSizeUtilized=").append(pageSizeUtilized).append(", ");
		if (pagingChunks != null) {
			builder.append("pagingChunks=").append(pagingChunks.subList(0, Math.min(pagingChunks.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the pagingChunks
	 */
	public List<PagingChunk> getPagingChunks() {
		return pagingChunks;
	}

	/**
	 * @param pagingChunks
	 *            the pagingChunks to set
	 */
	public void setPagingChunks(List<PagingChunk> pagingChunks) {
		this.pagingChunks = pagingChunks;
	}

}