package org.irods.jargon.datautils.pagination;

/**
 * Represents the status of a paged set of data. Convenience methods for
 * understanding the paging status are provided. This is an immutable,
 * thread-safe data object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class PagingStatus {

	/**
	 * The offset into the total results that this page represents, the index of
	 * the first row in the page
	 */
	private final int startingIndex;

	/**
	 * The index of the last row in the page
	 */
	private final int endingIndex;

	/**
	 * The total number of results in all pages. This may not be available, in
	 * which case, it should be zero.
	 */
	private final int totalEntries;

	/**
	 * Indicates that the last row in the page is the last row for all pages
	 */
	private final boolean lastEntryIsLast;

	/**
	 * The size of pages requested from the source
	 */
	private final int pageSize;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PagingStatus");
		sb.append("\n\t startingIndex:");
		sb.append(startingIndex);
		sb.append("\n\t endingIndex:");
		sb.append(endingIndex);
		sb.append("\n\t totalEntries:");
		sb.append(totalEntries);
		sb.append("\n\t lastEntryIsLast:");
		sb.append(lastEntryIsLast);
		sb.append("\n\t pageSize:");
		sb.append(pageSize);
		return sb.toString();
	}

	/**
	 * Public constructor creates immutable representation of the paging status
	 * of the current page
	 * 
	 * @param startingIndex
	 *            <code>int</code> offset into the total results that this page
	 *            represents, the index of the first row in the page
	 * @param endingIndex
	 *            <code>int</code> index of the last row in the page
	 * @param lastEntry
	 *            <code>boolean</code> indicates that the last row in the page
	 *            is the last row for all pages
	 * @param totalEntries
	 *            <code>int</code> total number of results in all pages. This
	 *            may not be available, in which case, it should be zero.
	 * @param pageSize
	 *            <code>int</code> size of pages requested from the source
	 */
	public PagingStatus(final int startingIndex, final int endingIndex,
			final boolean lastEntry, final int totalEntries, final int pageSize) {

		if (startingIndex < 0) {
			throw new IllegalArgumentException(
					"starting index must be 0 or greater");
		}

		if (endingIndex < 0) {
			throw new IllegalArgumentException(
					"ending index must be 0 or greater");
		}

		if (totalEntries < 0) {
			throw new IllegalArgumentException(
					"totalEntries must be 0 or greater");
		}

		if (pageSize < 1) {
			throw new IllegalArgumentException("pageSize must be 1 or greater");
		}

		this.startingIndex = startingIndex;
		this.endingIndex = endingIndex;
		lastEntryIsLast = lastEntry;
		this.totalEntries = totalEntries;
		this.pageSize = pageSize;
	}

	/**
	 * Get the total entries, this may not be available from the iCAT, in which
	 * case a zero will be returned
	 * 
	 * @return
	 */
	public int getTotalEntries() {
		return totalEntries;
	}

	/**
	 * If the paging display breaks the paging into segments, then this computes
	 * the number of segments to display based on a chunking factor
	 * 
	 * @param chunkingFactor
	 *            <code>int</code> with desired number of results in each paging
	 *            segment
	 * @return <code>int</code> with desired number of segments
	 */
	public int getNumberOfSegments(final int chunkingFactor) {

		if (chunkingFactor <= 0) {
			return 500;
		} else {
			return totalEntries / chunkingFactor;
		}
	}

	/**
	 * Should I expect pages before this
	 */
	public boolean isPagesBefore() {
		return startingIndex > 1;
	}

	/**
	 * Should I expect pages after this one
	 * 
	 * @return
	 */
	public boolean isPagesAfter() {
		return lastEntryIsLast == false;
	}

	/**
	 * Get a count of the total number of pages that can be displayed
	 * 
	 * @return
	 */
	public int computeExpectedNumberOfPages() {
		// cannot calculate if no totals
		if (totalEntries <= 0) {
			return -1;
		} else {
			return totalEntries / pageSize;
		}
	}

	/**
	 * Is this batch of records indicating that they are the last ones?
	 * 
	 * @return
	 */
	public boolean isThisSetLast() {
		return lastEntryIsLast == true;
	}

	/**
	 * Get the next index to query
	 * 
	 * @return
	 */
	public int getNextIndex() {
		return endingIndex;
	}

	/**
	 * Get the previous index
	 */

	public int getPreviousIndex() {
		if (startingIndex == 1) {
			return 0;
		}

		int computed = startingIndex - pageSize;

		if (computed < 1) {
			computed = 0;
		}

		return computed;

	}

	/**
	 * Is this an occasion where I need to worry about paging
	 * 
	 * @return
	 */
	public boolean isThisAPagingSituation() {
		boolean paging = false;
		if (startingIndex == 1 && lastEntryIsLast) {
			paging = false;
		} else {
			paging = true;
		}
		return paging;
	}

	/**
	 * Get the number of results in each page
	 * 
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Return the first index represented by the current page of data
	 * 
	 * @return
	 */
	public int getStartingIndex() {
		return startingIndex;
	}

}
