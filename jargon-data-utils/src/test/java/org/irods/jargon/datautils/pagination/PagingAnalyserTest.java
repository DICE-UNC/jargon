package org.irods.jargon.datautils.pagination;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.datautils.pagination.PagingActions;
import org.irods.jargon.datautils.pagination.PagingAnalyser;
import org.irods.jargon.datautils.pagination.PagingIndexEntry;
import org.junit.BeforeClass;
import org.junit.Test;

public class PagingAnalyserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	/**
	 * 
	 * 10000 files, 5000 page size, on first page
	 * 
	 */
	
	@Test
	public void testBuild10000RowsFirstPage5000PageSize() {
		List<IRODSDomainObject> irodsDomainObjects = new ArrayList<IRODSDomainObject>();
		IRODSDomainObject first = new IRODSDomainObject();
		first.setCount(1);
		first.setLastResult(false);
		first.setTotalRecords(10000);
		irodsDomainObjects.add(first);

		IRODSDomainObject last = new IRODSDomainObject();
		last.setCount(5000);
		last.setLastResult(false);
		last.setTotalRecords(10000);
		irodsDomainObjects.add(last);
		PagingActions pagingActions = PagingAnalyser
				.buildPagingActionsFromListOfIRODSDomainObjects(
						irodsDomainObjects, 5000);
		Assert.assertNotNull("null pagingActions", pagingActions);

		/*
		 * PagingActions
	 minValue:0
	 maxValue:10000
	 pageSize:5000[PagingIndexEntry:
	 indexType:INDEX
	 representation:1
	 index:0
	 current:true, PagingIndexEntry:
	 indexType:INDEX
	 representation:2
	 index:5000
	 current:false, PagingIndexEntry:
	 indexType:NEXT
	 representation:>
	 index:5000
	 current:false, PagingIndexEntry:
	 indexType:LAST
	 representation:>>
	 index:5000
	 current:false]
		 */
		
		Assert.assertNotNull("null pagingActions", pagingActions);

		Assert.assertEquals("did not get the expected 4 pages", 4,
				pagingActions.getPagingIndexEntries().size());


		// index 1
		Assert.assertEquals("expected index of 1 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(0).getIndexType());
		Assert.assertEquals("expected index of 1 in representation", "1",
				pagingActions.getPagingIndexEntries().get(0)
						.getRepresentation());
		Assert.assertEquals("expected index of 1 should start at 0", 0,
				pagingActions.getPagingIndexEntries().get(0).getIndex());
		Assert.assertTrue("1 should be current", pagingActions
				.getPagingIndexEntries().get(0).isCurrent());
		// index 2
		Assert.assertEquals("expected index of 2 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(1).getIndexType());
		Assert.assertEquals("expected index of 2 in representation", "2",
				pagingActions.getPagingIndexEntries().get(1)
						.getRepresentation());
		Assert.assertEquals("expected index of 2 should start at 5000", 5000,
				pagingActions.getPagingIndexEntries().get(1).getIndex());
		Assert.assertFalse("should not be current", pagingActions
				.getPagingIndexEntries().get(1).isCurrent());
		
		// next should point to index 2 values
		Assert.assertEquals("prev type expected",
				PagingIndexEntry.IndexType.NEXT, pagingActions
						.getPagingIndexEntries().get(2).getIndexType());
		Assert.assertEquals("expected > in representation", ">", pagingActions
				.getPagingIndexEntries().get(2).getRepresentation());
		Assert.assertEquals("expected index of next should start at 5000",
				5000, pagingActions.getPagingIndexEntries().get(2).getIndex());
		// last should have index 2 values
		Assert.assertEquals("expected index of last should be last type",
				PagingIndexEntry.IndexType.LAST, pagingActions
						.getPagingIndexEntries().get(3).getIndexType());
		Assert.assertEquals("expected index of >> in representation", ">>",
				pagingActions.getPagingIndexEntries().get(3)
						.getRepresentation());
		Assert.assertEquals("expected index of last should start at 5000",
				5000, pagingActions.getPagingIndexEntries().get(3).getIndex());
	
	}
	
	
	

	/**
	 * be on 4th page of a 3000 record set with page size of 500 page1 = 0-499
	 * page2 = 500-999 page3 = 1000-1499 page4 = 1500-1999 page5=2000-2499 page6=2500-3000
	 */
	@Test
	public void testBuildPagingActionsFromListOfIRODSDomainObjects() {
		List<IRODSDomainObject> irodsDomainObjects = new ArrayList<IRODSDomainObject>();
		IRODSDomainObject first = new IRODSDomainObject();
		first.setCount(1500);
		first.setLastResult(false);
		first.setTotalRecords(3000);
		irodsDomainObjects.add(first);

		IRODSDomainObject last = new IRODSDomainObject();
		last.setCount(2000);
		last.setLastResult(false);
		last.setTotalRecords(3000);
		irodsDomainObjects.add(last);
		PagingActions pagingActions = PagingAnalyser
				.buildPagingActionsFromListOfIRODSDomainObjects(
						irodsDomainObjects, 500);
		Assert.assertNotNull("null pagingActions", pagingActions);

		Assert.assertEquals("did not get the expected 6 pages", 10,
				pagingActions.getPagingIndexEntries().size());
		Assert.assertEquals("did not get a first",
				PagingIndexEntry.IndexType.FIRST, pagingActions
						.getPagingIndexEntries().get(0).getIndexType());
		Assert.assertEquals("did not get a prev",
				PagingIndexEntry.IndexType.PREV, pagingActions
						.getPagingIndexEntries().get(1).getIndexType());
		Assert.assertEquals("prev should be page 3, starting at count of 1000",
				1000, pagingActions.getPagingIndexEntries().get(1).getIndex());
		// prev should have an index of current -1!

		// index 1
		Assert.assertEquals("expected index of 1 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(2).getIndexType());
		Assert.assertEquals("expected index of 1 in representation", "1",
				pagingActions.getPagingIndexEntries().get(2)
						.getRepresentation());
		Assert.assertEquals("expected index of 1 should start at 0", 0,
				pagingActions.getPagingIndexEntries().get(2).getIndex());
		// index 2
		Assert.assertEquals("expected index of 2 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(3).getIndexType());
		Assert.assertEquals("expected index of 2 in representation", "2",
				pagingActions.getPagingIndexEntries().get(3)
						.getRepresentation());
		Assert.assertEquals("expected index of 2 should start at 500", 500,
				pagingActions.getPagingIndexEntries().get(3).getIndex());
		// index 3
		Assert.assertEquals("expected index of 3 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(4).getIndexType());
		Assert.assertEquals("expected index of 3 in representation", "3",
				pagingActions.getPagingIndexEntries().get(4)
						.getRepresentation());
		Assert.assertEquals("expected index of 3 should start at 1000", 1000,
				pagingActions.getPagingIndexEntries().get(4).getIndex());
		Assert.assertFalse("should not be current", pagingActions
				.getPagingIndexEntries().get(4).isCurrent());

		// index 4 (current)
		Assert.assertEquals("expected index of 4 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(5).getIndexType());
		Assert.assertEquals("expected index of 4 in representation", "4",
				pagingActions.getPagingIndexEntries().get(5)
						.getRepresentation());
		Assert.assertEquals("expected index of 4 should start at 1500", 1500,
				pagingActions.getPagingIndexEntries().get(5).getIndex());
		Assert.assertTrue("should be current", pagingActions
				.getPagingIndexEntries().get(5).isCurrent());
		// index 5
		Assert.assertEquals("expected index of 5 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(6).getIndexType());
		Assert.assertEquals("expected index of 5 in representation", "5",
				pagingActions.getPagingIndexEntries().get(6)
						.getRepresentation());
		Assert.assertEquals("expected index of 5 should start at 2000", 2000,
				pagingActions.getPagingIndexEntries().get(6).getIndex());
		Assert.assertFalse("should not be current", pagingActions
				.getPagingIndexEntries().get(6).isCurrent());
		// index 6
		Assert.assertEquals("expected index of 6 should be an index type",
				PagingIndexEntry.IndexType.INDEX, pagingActions
						.getPagingIndexEntries().get(7).getIndexType());
		Assert.assertEquals("expected index of 6 in representation", "6",
				pagingActions.getPagingIndexEntries().get(7)
						.getRepresentation());
		Assert.assertEquals("expected index of 6 should start at 2500", 2500,
				pagingActions.getPagingIndexEntries().get(7).getIndex());
		Assert.assertFalse("should not be current", pagingActions
				.getPagingIndexEntries().get(7).isCurrent());
		// next should point to index 5 values
		Assert.assertEquals("prev type expected",
				PagingIndexEntry.IndexType.NEXT, pagingActions
						.getPagingIndexEntries().get(8).getIndexType());
		Assert.assertEquals("expected > in representation", ">", pagingActions
				.getPagingIndexEntries().get(8).getRepresentation());
		Assert.assertEquals("expected index of next should start at 2000",
				2000, pagingActions.getPagingIndexEntries().get(8).getIndex());
		// last should have index 6 values
		Assert.assertEquals("expected index of last should be last type",
				PagingIndexEntry.IndexType.LAST, pagingActions
						.getPagingIndexEntries().get(9).getIndexType());
		Assert.assertEquals("expected index of >> in representation", ">>",
				pagingActions.getPagingIndexEntries().get(9)
						.getRepresentation());
		Assert.assertEquals("expected index of last should start at 2500",
				2500, pagingActions.getPagingIndexEntries().get(9).getIndex());
	}

	/**
	 * be on 1st page of a 5500 record set with page size of 1000 page1 = 0-999
	 */
	@Test
	public void testBuildPagingActionsFromListOfIRODSDomainObjects2() {
		List<IRODSDomainObject> irodsDomainObjects = new ArrayList<IRODSDomainObject>();
		IRODSDomainObject first = new IRODSDomainObject();
		first.setCount(1);
		first.setLastResult(false);
		first.setTotalRecords(5500);
		irodsDomainObjects.add(first);

		IRODSDomainObject last = new IRODSDomainObject();
		last.setCount(5500);
		last.setLastResult(false);
		last.setTotalRecords(5500);
		irodsDomainObjects.add(last);
		PagingActions pagingActions = PagingAnalyser
				.buildPagingActionsFromListOfIRODSDomainObjects(
						irodsDomainObjects, 5500);
		Assert.assertNotNull("null pagingActions", pagingActions);
	}
	
	
	/**
	 * be on a 1 page set, no pagination, no indexes should be there
	 */
	@Test
	public void testBuildPagingWhenNoPaging() {
		List<IRODSDomainObject> irodsDomainObjects = new ArrayList<IRODSDomainObject>();
		IRODSDomainObject first = new IRODSDomainObject();
		first.setCount(1);
		first.setLastResult(true);
		first.setTotalRecords(500);
		irodsDomainObjects.add(first);

		PagingActions pagingActions = PagingAnalyser
				.buildPagingActionsFromListOfIRODSDomainObjects(
						irodsDomainObjects, 5000);
		Assert.assertNotNull("null pagingActions", pagingActions);
		Assert.assertEquals("should be no indexes here", 0, pagingActions.getPagingIndexEntries().size());
	}
	
	@Test
	public void testGetCurrentWhenOnPage4() {
		List<IRODSDomainObject> irodsDomainObjects = new ArrayList<IRODSDomainObject>();
		IRODSDomainObject first = new IRODSDomainObject();
		first.setCount(1500);
		first.setLastResult(false);
		first.setTotalRecords(3000);
		irodsDomainObjects.add(first);

		IRODSDomainObject last = new IRODSDomainObject();
		last.setCount(2000);
		last.setLastResult(false);
		last.setTotalRecords(3000);
		irodsDomainObjects.add(last);
		PagingActions pagingActions = PagingAnalyser
				.buildPagingActionsFromListOfIRODSDomainObjects(
						irodsDomainObjects, 500);
		PagingIndexEntry current = pagingActions.getCurrentIndexEntry();
		Assert.assertNotNull("no current entry found", current);
	}
}
