/**
 * 
 */
package org.irods.jargon.datautils.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an abstract portrayal of actions available as determined from an underlying 
 * {@link PagingStatus}.  In actuality, this translates to a template by which a visual paging control
 * can be configured.
 * <p/>
 * This involves representations for whether next and previous actions, as well as intermediate index actions, are
 * available.  It also handles insertion of 'elipses' or place holders where too many indexes would be required 
 * to traverse the pages represented by the <code>PagingStatus</code>.
 * <p/>
 * The goal is to support 'pagination' button sets, as well as 'slider' type continuous interface components, but this
 * could be equally applied to a command line utility.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PagingActions {
	
	/**
	 * List of the indexes from 'right to left' that represent this paging action set
	 */
	private final List<PagingIndexEntry> pagingIndexEntries;
	
	/**
	 * Minimum value for a continuous page (such as represented in a slider)
	 */
	private final int minValue;
	
	/**
	 * Max value for a continuous page (such as represented in a slider)
	 */
	private final int maxValue;
	
	/**
	 * Size of an indiviudual page, representing a 'step' in a slider, and the amout of data represented in
	 * each index.
	 */
	private final int pageSize;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PagingActions");
		sb.append("\n\t minValue:");
		sb.append(minValue);
		sb.append("\n\t maxValue:");
		sb.append(maxValue);
		sb.append("\n\t pageSize:");
		sb.append(pageSize);
		sb.append(pagingIndexEntries);
		return sb.toString();
	}
	
	/**
	 * Default constructor returns a <code>PagingActions</code> that indicates that no paging is needed
	 */
	public PagingActions() {
		this.pagingIndexEntries = Collections.unmodifiableList(new ArrayList<PagingIndexEntry>());
		this.maxValue=0;
		this.minValue=0;
		this.pageSize=0;
	}
	
	
	/**
	 * Default constructor with immutable values
	 * @param pagingIndexEntries <code>List</code> of {@link PagingIndexEntry} representing paging options
	 * @param minValue <code>int</code> Minimum value for a continuous page (such as represented in a slider)
	 * @param maxValue <code>int</code> Max value for a continuous page (such as represented in a slider)
	 * @param pageSize <code>int</code> Size of an indiviudual page, representing a 'step' in a slider, and the amout of data represented in
	 * each index.
	 */
	public PagingActions(List<PagingIndexEntry> pagingIndexEntries,
			int minValue, int maxValue, int pageSize) {
		super();
		
		if (pagingIndexEntries == null) {
			throw new IllegalArgumentException("null pagingIndexEntries");
		}
		
		this.pagingIndexEntries = Collections.unmodifiableList(pagingIndexEntries);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.pageSize = pageSize;
	}


	public List<PagingIndexEntry> getPagingIndexEntries() {
		return pagingIndexEntries;
	}


	public int getMinValue() {
		return minValue;
	}


	public int getMaxValue() {
		return maxValue;
	}


	public int getPageSize() {
		return pageSize;
	}
	
	/**
	 * Is there any paging to do?
	 * @return <code>boolean</code> if there is paging to do
	 */
	public boolean isPaging() {
		return (pagingIndexEntries.size() > 0);
	}
	

}
