package org.irods.jargon.core.query;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class GenQueryField {

	public enum SelectFieldTypes {
		FIELD, SUM, MIN, MAX, AVG, COUNT, FILE_ACCESS
	}

	public enum SelectFieldSource {
		UNKNOWN, DEFINED_QUERY_FIELD, AVU, EXTENSIBLE_METADATA
	}

	private final SelectFieldSource selectFieldSource;
	private final String selectFieldNumericTranslation;
	private final String selectFieldColumnName;

	/**
	 * @param selectFieldColumnName
	 * @param selectFieldSource
	 * @param selectFieldNumericTranslation
	 */
	public GenQueryField(final String selectFieldColumnName,
			final SelectFieldSource selectFieldSource,
			final String selectFieldNumericTranslation) {

		if (selectFieldColumnName == null
				|| selectFieldColumnName.length() == 0) {
			throw new IllegalArgumentException("select field was or missing");
		}

		if (selectFieldSource == null) {
			throw new IllegalArgumentException("field source was null");
		}

		if (selectFieldNumericTranslation == null
				|| selectFieldNumericTranslation.length() == 0) {
			throw new IllegalArgumentException(
					"field translation is null or blank");
		}

		this.selectFieldColumnName = selectFieldColumnName;
		this.selectFieldSource = selectFieldSource;
		this.selectFieldNumericTranslation = selectFieldNumericTranslation;
	}

	public String getSelectFieldColumnName() {
		return selectFieldColumnName;
	}

	public SelectFieldSource getSelectFieldSource() {
		return selectFieldSource;
	}

	public String getSelectFieldNumericTranslation() {
		return selectFieldNumericTranslation;
	}

}
