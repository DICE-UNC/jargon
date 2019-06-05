/**
 * 
 */
package org.irods.jargon.core.query;

/**
 * Represents a view of the paging status and navigation options for a pageable
 * collection. Thiis is used for collection listings and virtual collections
 * with an eye towards generation of paging links for API and interfaces.
 * 
 * @author conwaymc
 *
 */
public class PageableCollection {

	public enum PageingRelValue {
		FIRST, PREV, NEXT, LAST
	}

	/**
	 * 
	 */
	public PageableCollection() {
		// TODO Auto-generated constructor stub
	}

	public class PageableRelLink {

		/**
		 * {@link PageingRelValue} Indicates the type of paging link
		 */
		private PageingRelValue pagingRelValue;

		/**
		 * {@code int} with the relative page number, based on the total records and
		 * page size
		 */
		private int pageNumber = 0;

		/**
		 * {@code int} with the absolute offset into the collection, the calculated page
		 * number corresponds to this value
		 */
		private int offset = 0;

	}

}
