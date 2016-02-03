/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Utility class used to characterize listings
 * 
 * @author Mike Conway - DICE
 *
 */
public class ListAndCount {
	private int countTotal = 0;
	private int countThisPage = 0;
	private boolean endOfRecords = false;
	private int offsetStart = 0;
	private List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries;

	/**
	 * @return the countTotal
	 */
	public int getCountTotal() {
		return countTotal;
	}

	/**
	 * @param countTotal
	 *            the countTotal to set
	 */
	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}

	/**
	 * @return the collectionAndDataObjectListingEntries
	 */
	public List<CollectionAndDataObjectListingEntry> getCollectionAndDataObjectListingEntries() {
		return collectionAndDataObjectListingEntries;
	}

	/**
	 * @param collectionAndDataObjectListingEntries
	 *            the collectionAndDataObjectListingEntries to set
	 */
	public void setCollectionAndDataObjectListingEntries(
			List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries) {
		this.collectionAndDataObjectListingEntries = collectionAndDataObjectListingEntries;
	}

	/**
	 * @return the countThisPage
	 */
	public int getCountThisPage() {
		return countThisPage;
	}

	/**
	 * @param countThisPage
	 *            the countThisPage to set
	 */
	public void setCountThisPage(int countThisPage) {
		this.countThisPage = countThisPage;
	}

	/**
	 * @return the endOfRecords
	 */
	public boolean isEndOfRecords() {
		return endOfRecords;
	}

	/**
	 * @param endOfRecords
	 *            the endOfRecords to set
	 */
	public void setEndOfRecords(boolean endOfRecords) {
		this.endOfRecords = endOfRecords;
	}

	/**
	 * @return the offsetStart
	 */
	public int getOffsetStart() {
		return offsetStart;
	}

	/**
	 * @param offsetStart
	 *            the offsetStart to set
	 */
	public void setOffsetStart(int offsetStart) {
		this.offsetStart = offsetStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 5;
		StringBuilder builder = new StringBuilder();
		builder.append("ListAndCount [countTotal=");
		builder.append(countTotal);
		builder.append(", countThisPage=");
		builder.append(countThisPage);
		builder.append(", endOfRecords=");
		builder.append(endOfRecords);
		builder.append(", offsetStart=");
		builder.append(offsetStart);
		builder.append(", ");
		if (collectionAndDataObjectListingEntries != null) {
			builder.append("collectionAndDataObjectListingEntries=");
			builder.append(collectionAndDataObjectListingEntries.subList(0,
					Math.min(collectionAndDataObjectListingEntries.size(),
							maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}
}
