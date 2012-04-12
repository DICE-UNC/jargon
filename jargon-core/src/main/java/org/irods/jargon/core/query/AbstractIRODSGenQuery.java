package org.irods.jargon.core.query;

public class AbstractIRODSGenQuery {

	protected final int numberOfResultsDesired;

	public enum RowCountOptions {
		NO_ROW_COUNT, ROW_COUNT_FOR_THIS_RESULT, ROW_COUNT_INCLUDING_SKIPPED_ROWS
	}

	public AbstractIRODSGenQuery(final int numberOfResultsDesired) {
		if (numberOfResultsDesired <= 0) {
			throw new IllegalArgumentException(
					"numberOfResultsDesiredIsNullOrEmpty");
		}

		this.numberOfResultsDesired = numberOfResultsDesired;
	}

	public int getNumberOfResultsDesired() {
		return numberOfResultsDesired;
	}

}