/**
 *
 */
package org.irods.jargon.datautils.avuautocomplete;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of an AVU search
 *
 * @author Mike Conway - NIEHS
 *
 */
public class AvuSearchResult {

	/**
	 * offset used to get this result set
	 */
	private int offset = 0;
	/**
	 * are there more elements to return from the original query?
	 */
	private boolean more = false;
	/**
	 * The actual avu attribute or value, as appropriate to the query
	 */
	private List<String> elements = new ArrayList<String>();

	private int nextOffset = 0;

	/**
	 *
	 */
	public AvuSearchResult() {
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public boolean isMore() {
		return more;
	}

	public void setMore(final boolean more) {
		this.more = more;
	}

	public List<String> getElements() {
		return elements;
	}

	public void setElements(final List<String> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("AvuSearchResult [offset=").append(offset).append(", more=").append(more).append(", ");
		if (elements != null) {
			builder.append("elements=").append(elements.subList(0, Math.min(elements.size(), maxLen))).append(", ");
		}
		builder.append("nextOffset=").append(nextOffset).append("]");
		return builder.toString();
	}

	/**
	 * @return the nextOffset
	 */
	public int getNextOffset() {
		return nextOffset;
	}

	/**
	 * @param nextOffset
	 *            the nextOffset to set
	 */
	public void setNextOffset(final int nextOffset) {
		this.nextOffset = nextOffset;
	}

}
