/**
 * 
 */
package org.irods.jargon.datautils.pagination;

/**
 * Represents an 'index' entry in a paging representation. This 'index'
 * represents an internal 'jump' location within a set of possible pages.
 * <p/>
 * In a visual representation, such indexes represent intermediate steps through
 * the document directly addressed through a button, or as an increment on a
 * slider control.
 * <p/>
 * Immutable and thread-safe
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PagingIndexEntry {

	public enum IndexType {
		FIRST, PREV, INDEX, NEXT, LAST
	}

	/**
	 * <code>IndexType</code> enum value indicating what type of index entry
	 * this is (next/prev type option versus an index of a page
	 */
	private final IndexType indexType;

	/**
	 * Display representation for an index
	 */
	private final String representation;

	/**
	 * Actual index into the available pages represented by this index
	 */
	private final int index;

	/**
	 * Indicates that this is the current page
	 */
	private final boolean current;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PagingIndexEntry:");
		sb.append("\n\t indexType:");
		sb.append(indexType);
		sb.append("\n\t representation:");
		sb.append(representation);
		sb.append("\n\t index:");
		sb.append(index);
		sb.append("\n\t current:");
		sb.append(current);

		return sb.toString();

	}

	/**
	 * Default constructor for immutable type
	 * 
	 * @param indexType
	 *            <code>IndexType</code> enum value indicates the type of this
	 *            individual index (is it a next, previous, or an actual index
	 *            of a page
	 * @param representation
	 *            <code>String</code> with a display value
	 * @param index
	 *            <code>index</code> that represents the actual offset
	 */
	public PagingIndexEntry(final IndexType indexType,
			final String representation, final int index, final boolean current) {
		super();

		if (indexType == null) {
			throw new IllegalArgumentException("null indexType");
		}

		if (representation == null) {
			throw new IllegalArgumentException("null representation");
		}

		this.indexType = indexType;
		this.representation = representation;
		this.index = index;
		this.current = current;
	}

	public IndexType getIndexType() {
		return indexType;
	}

	public String getRepresentation() {
		return representation;
	}

	public int getIndex() {
		return index;
	}

	public boolean isCurrent() {
		return current;
	}

}
