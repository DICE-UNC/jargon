/**
 * 
 */
package org.irods.jargon.datautils.pagination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.datautils.pagination.PagingIndexEntry.IndexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that looks at a 'pagable' object in Jargon, such as a set of {@link IRODSDomainObject} containing paging
 * information, or a result set from Jargon.
 * <p/>
 * This service will create  
 * @author Mike Conway
 *
 */
public class PagingAnalyser {
	
	/**
	 * max number of index entries to show
	 */
	public static final int TOTAL_INDEXES = 10;
	
	private static final Logger log = LoggerFactory
			.getLogger(PagingAnalyser.class);

	/**
	 * Given a list of <code>IRODSDomainObjects</code> which contain count and index information, create a model
	 * that represents paging options for this set.  This allows the representation of paging through visual controls.
	 * @param irodsDomainObjects <code>List</code> of {@link IRODSDomainObject} that typically comes from Jargon methods,
	 * containing paging information
	 * @param pageSize <code>int</code> with the requested size of the result set page (the query limit used to create the set)
	 * @return {@link PagingActions}, which represents a model for a paging control component
	 */
	public static PagingActions buildPagingActionsFromListOfIRODSDomainObjects(final List<IRODSDomainObject> irodsDomainObjects, final int pageSize) {
		if (irodsDomainObjects == null) {
			throw new IllegalArgumentException("null irodsDomainObjects");
		}
		
		// return a paging actions that indicates no paging
		if (irodsDomainObjects.isEmpty()) {
			return new PagingActions();
		}
		
		int totalRecordCount = irodsDomainObjects.get(0).getTotalRecords();
		int firstIndex = irodsDomainObjects.get(0).getCount();
		int lastIndex = irodsDomainObjects.get(irodsDomainObjects.size() - 1).getCount();
		boolean lastEntry = irodsDomainObjects.get(irodsDomainObjects.size() -1).isLastResult();
		
		PagingStatus pagingStatus = new PagingStatus(firstIndex, lastIndex, lastEntry, totalRecordCount, pageSize);
		log.info("pagingStatus:{}", pagingStatus);
		return getPagingActionsFromPagingStatus(pagingStatus);
	
	}

	/**
	 * Given a <code>PagingStatus</code> describing the paging status of a given collection, return a model
	 * that represents a paging control component.
	 * @param pagingStatus
	 * @return
	 */
	private static PagingActions getPagingActionsFromPagingStatus(
			PagingStatus pagingStatus) {
		
		
		/**
		 * If paging is not indicated, then return an empty paging action
		 */
		if (!pagingStatus.isThisAPagingSituation()) {
			return new PagingActions();
		}
		
		List<PagingIndexEntry> pagingIndexEntries = new ArrayList<PagingIndexEntry>();
		
		/*
		 * Some kind of paging, if this is already paged within the results, we include 'first and previous'
		 */
		
		if (pagingStatus.isPagesBefore()) {
			pagingIndexEntries.add(new PagingIndexEntry(IndexType.FIRST, "<<", 0, false));
			int previousIndex = pagingStatus.getPreviousIndex();
			pagingIndexEntries.add(new PagingIndexEntry(IndexType.PREV, "<", previousIndex, false));
		}
		
		/*
		 * See if I have a total records, in which case, I will compute some indexes 
		 */
		
		if (pagingStatus.getTotalEntries() > 0) {
			pagingIndexEntries.addAll(buildNumberedIndexes(pagingStatus));
		}
		
		if (pagingStatus.isPagesAfter()) {
			buildAfterIndexes(pagingStatus, pagingIndexEntries);
		}
		
		
		
		PagingActions pagingActions = new PagingActions(pagingIndexEntries, 0, pagingStatus.getTotalEntries(), pagingStatus.getPageSize());
		log.info("pagingActions:{}", pagingActions);
		return pagingActions;
		
	}

	/**
	 * Build the final indexes for a paging model, which would be the Next and optionally, a Last option.  The last option
	 * is only available if a total record count is in the <code>PagingStatus</code>, otherwise, I have no way of knowing
	 * how to advance to the last page
	 * @param pagingStatus
	 * @param pagingIndexEntries
	 */
	private static void buildAfterIndexes(PagingStatus pagingStatus,
			List<PagingIndexEntry> pagingIndexEntries) {
		pagingIndexEntries.add(new PagingIndexEntry(IndexType.NEXT, ">", pagingStatus.getNextIndex(), false));
		if (pagingStatus.getTotalEntries() > 0) {
			pagingIndexEntries.add(buildLastIndexIndexes(pagingStatus));
		}
	}

	/**
	 * Returns the proper index to get to the last page of results.
	 * @param pagingStatus
	 * @return
	 */
	private static PagingIndexEntry buildLastIndexIndexes(
			PagingStatus pagingStatus) {
		int finalStartingIndex = pagingStatus.getTotalEntries() - pagingStatus.getPageSize();
		return new PagingIndexEntry(IndexType.LAST, ">>", finalStartingIndex, false);
	}

	private static Collection<PagingIndexEntry> buildNumberedIndexes(
			PagingStatus pagingStatus) {
		
		// go for 10 total indexes (make a parameter?).  Note page indexes are 1 based
		
		int currentPage = pagingStatus.getStartingIndex() / pagingStatus.getPageSize() + 1;
		// -1 so that center page is available
		int pagesInDirection = TOTAL_INDEXES / 2 - 1;
		
		int excessNext = 0;
		int nextIndexes = pagesInDirection + currentPage;
		
		/*
		 * Looking at potential pages to the right of the current index, which will be to the max number
		 * of pages that can be displayed.  Any excess can be added in the other direction so we get the
		 * most buttons in the index section
		 */
		
		if (currentPage + nextIndexes > pagingStatus.computeExpectedNumberOfPages()) {
			nextIndexes = pagingStatus.computeExpectedNumberOfPages() - currentPage;
			excessNext = pagesInDirection - nextIndexes;
		}
		
		/*
		 * Same thing going in the previous direction...
		 */
		
		int excessPrevious = 0;
		int previousIndexes = currentPage - pagesInDirection;
		if (previousIndexes <= 0) {
			previousIndexes = currentPage - 1;
			excessPrevious = pagesInDirection - previousIndexes;
		}
		
		// add the excess to the other side up to the limits
		nextIndexes += excessPrevious;
		if (nextIndexes + currentPage > pagingStatus.computeExpectedNumberOfPages()) {
			nextIndexes = pagingStatus.computeExpectedNumberOfPages() - currentPage;
		}
		
		previousIndexes += excessNext;
		if (currentPage - previousIndexes < 0) {
			previousIndexes = currentPage - 1;
		}
		
		
		/*
		 * OK, I have a centroid, and an extent forwards and backwards, so now create some index buttons
		 */
		
		List<PagingIndexEntry> indexEntries = new ArrayList<PagingIndexEntry>();
		
		int totalIndexes = previousIndexes + nextIndexes + 1; // plus one for current page
		for (int i = 0; i < totalIndexes; i ++ ) {
			
			boolean isCurrent = false;
			if (i + 1 == currentPage){
				isCurrent = true;
			}
			
			int thisIndex = (i) * pagingStatus.getPageSize();
			
			//indexes run from the nextIndex forward, and are zero based, add 1 to make it 1 based
			indexEntries.add(new PagingIndexEntry(IndexType.INDEX, String.valueOf(i + 1), thisIndex, isCurrent));	
		}
		
		return indexEntries;
	}

}
