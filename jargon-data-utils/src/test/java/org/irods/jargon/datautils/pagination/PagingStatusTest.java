/**
 * 
 */
package org.irods.jargon.datautils.pagination;

import junit.framework.Assert;

import org.irods.jargon.datautils.pagination.PagingStatus;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mikeconway
 * 
 */
public class PagingStatusTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testPagingStatus() {
		new PagingStatus(1, 1, false, 1, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPagingStatusStartingIndex() {
		new PagingStatus(-1, 1, false, 1, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPagingStatusEndingIndex() {
		new PagingStatus(1, -1, false, 1, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPagingStatusTotalEntries() {
		new PagingStatus(1, 1, false, 1, 0);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.pagination.PagingStatus#isPagesBefore()}
	 * .
	 */
	@Test
	public void testIsPagesBefore() {
		PagingStatus pagingStatus = new PagingStatus(100, 200, false, 300, 1);
		Assert.assertTrue("should be paging before",
				pagingStatus.isPagesBefore());
	}

	@Test
	public void testIsPagesBeforeWhenNot() {
		PagingStatus pagingStatus = new PagingStatus(0, 200, false, 300, 1);
		Assert.assertFalse("should not be paging before",
				pagingStatus.isPagesBefore());
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.pagination.PagingStatus#getNextIndex()}.
	 */
	@Test
	public void testGetNextIndex() {
		PagingStatus pagingStatus = new PagingStatus(1, 1000, false, 5000, 1000);
		int nextIndex = pagingStatus.getNextIndex();
		Assert.assertEquals("next index wrong", 1000, nextIndex);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.pagination.PagingStatus#getPreviousIndex()}
	 * .
	 */
	@Test
	public void testGetPreviousIndex() {
		PagingStatus pagingStatus = new PagingStatus(1000, 2000, false, 5000,
				1000);
		int prevIndex = pagingStatus.getPreviousIndex();
		Assert.assertEquals("previous index wrong", 0, prevIndex);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.pagination.PagingStatus#isThisAPagingSituation()}
	 * .
	 */
	@Test
	public void testIsThisAPagingSituation() {
		PagingStatus pagingStatus = new PagingStatus(1000, 2000, false, 5000,
				1000);
		Assert.assertTrue("should be a paging situation",
				pagingStatus.isThisAPagingSituation());
	}

}
