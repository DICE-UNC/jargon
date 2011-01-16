/**
 * 
 */
package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE (www.irods.org) A field in a GenQuery select in
 *         translated form, including the type of select (e.g. a field, versus a
 *         sum() or count() of a field. This is an immutable, thread-safe type
 */
public class GenQuerySelectField {
	public enum SelectFieldTypes {
		FIELD, SUM, MIN, MAX, AVG, COUNT, FILE_ACCESS
	}

	public enum SelectFieldSource {
		UNKNOWN, DEFINED_QUERY_FIELD, AVU, EXTENSIBLE_METADATA
	}

	private final String selectFieldColumnName;
	private final SelectFieldTypes selectFieldType;
	private final SelectFieldSource selectFieldSource;
	private final String selectFieldNumericTranslation;

	/**
	 * Create an instance of a select field using a value from the enumeration
	 * of iRODS GenQueryFields.
	 * 
	 * @param selectField
	 *            {@link org.irods.jargon.core.query.RodsGenQueryEnum} value
	 *            that represents the desired select field.
	 * @param selectFieldType
	 *            <code>SelectFieldTypes</code> enum value that classifies the
	 *            select field as a value, or an aggregation.
	 * @param selectFieldSource
	 *            <code>SelectFieldSource</code> enum value that indicates the
	 *            type of field (iRODS iCAT value, user AVU, extensible
	 *            metadata, etc).
	 * @return <code>SelectField</code> describing details about this field.
	 * @throws JargonException
	 */
	public static GenQuerySelectField instance(final RodsGenQueryEnum selectField,
			final SelectFieldTypes selectFieldType,
			final SelectFieldSource selectFieldSource) throws JargonException {
		if (selectField == null) {
			throw new JargonException("select field was null");
		}
		return new GenQuerySelectField(selectField.getName(),
				String.valueOf(selectField.getNumericValue()), selectFieldType,
				selectFieldSource);
	}

	/**
	 * Create an instance of a select field providing the field name and
	 * appropriate numeric translation value.
	 * 
	 * @param selectFieldName
	 *            <code>String</code> containing the original string value of
	 *            the select field in the query.
	 * @param selectFieldNumericTranslation
	 *            <code>String</code> containing the numeric value that iRODS
	 *            uses in query processing, sent in GenQueryInp packing
	 *            instruction.
	 * @param selectFieldType
	 *            <code>SelectFieldTypes</code> enum value that classifies the
	 *            select field as a value, or an aggregation.
	 * @param selectFieldSource
	 *            <code>SelectFieldSource</code> enum value that indicates the
	 *            type of field (iRODS iCAT value, user AVU, extensible
	 *            metadata, etc).
	 * @return <code>SelectField</code> describing details about this field.
	 * @throws JargonException
	 */
	public static GenQuerySelectField instance(final String selectFieldName,
			final String selectFieldNumericTranslation,
			final SelectFieldTypes selectFieldType,
			final SelectFieldSource selectFieldSource) throws JargonException {
		return new GenQuerySelectField(selectFieldName, selectFieldNumericTranslation,
				selectFieldType, selectFieldSource);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Select field");
		sb.append("\n   selectFieldColumnName:");
		sb.append(selectFieldColumnName);
		sb.append("\n   selectFieldNumericTranslation:");
		sb.append(selectFieldNumericTranslation);
		sb.append("\n   selectFieldType:");
		sb.append(selectFieldType);
		sb.append("\n   selectFieldSource:");
		sb.append(selectFieldSource);
		return sb.toString();
	}

	private GenQuerySelectField(final String selectFieldColumnName,
			final String selectFieldNumericTranslation,
			final SelectFieldTypes selectFieldType,
			final SelectFieldSource selectFieldSource) throws JargonException {

		if (selectFieldColumnName == null
				|| selectFieldColumnName.length() == 0) {
			throw new JargonException("select field was or missing");
		}

		if (selectFieldType == null) {
			throw new JargonException("field type was null");
		}

		if (selectFieldSource == null) {
			throw new JargonException("field source was null");
		}

		if (selectFieldNumericTranslation == null
				|| selectFieldNumericTranslation.length() == 0) {
			throw new JargonException("field translation is null or blank");
		}

		this.selectFieldColumnName = selectFieldColumnName;
		this.selectFieldType = selectFieldType;
		this.selectFieldSource = selectFieldSource;
		this.selectFieldNumericTranslation = selectFieldNumericTranslation;

	}

	public String getSelectFieldColumnName() {
		return selectFieldColumnName;
	}

	public SelectFieldTypes getSelectFieldType() {
		return selectFieldType;
	}

	public SelectFieldSource getSelectFieldSource() {
		return selectFieldSource;
	}

	public String getSelectFieldNumericTranslation() {
		return selectFieldNumericTranslation;
	}

}
