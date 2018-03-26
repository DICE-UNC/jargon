package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 *         A field in a GenQuery select in translated form, including the type
 *         of select (e.g. a field, versus a sum() or count() of a field. This
 *         is an immutable, thread-safe type
 */
public class GenQuerySelectField extends GenQueryField {

	private final SelectFieldTypes selectFieldType;

	/**
	 * Create an instance of a select field using a value from the enumeration
	 * of iRODS GenQueryFields.
	 *
	 * @param selectField
	 *            {@link org.irods.jargon.core.query.RodsGenQueryEnum} value
	 *            that represents the desired select field.
	 * @param selectFieldType
	 *            {@code SelectFieldTypes} enum value that classifies the
	 *            select field as a value, or an aggregation.
	 * @param selectFieldSource
	 *            {@code SelectFieldSource} enum value that indicates the
	 *            type of field (iRODS iCAT value, user AVU, extensible
	 *            metadata, etc).
	 * @return {@code SelectField} describing details about this field.
	 * @throws JargonException
	 */
	public static GenQuerySelectField instance(
			final RodsGenQueryEnum selectField,
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
	 *            {@code String} containing the original string value of
	 *            the select field in the query.
	 * @param selectFieldNumericTranslation
	 *            {@code String} containing the numeric value that iRODS
	 *            uses in query processing, sent in GenQueryInp packing
	 *            instruction.
	 * @param selectFieldType
	 *            {@code SelectFieldTypes} enum value that classifies the
	 *            select field as a value, or an aggregation.
	 * @param selectFieldSource
	 *            {@code SelectFieldSource} enum value that indicates the
	 *            type of field (iRODS iCAT value, user AVU, extensible
	 *            metadata, etc).
	 * @return {@code SelectField} describing details about this field.
	 * @throws JargonException
	 */
	public static GenQuerySelectField instance(final String selectFieldName,
			final String selectFieldNumericTranslation,
			final SelectFieldTypes selectFieldType,
			final SelectFieldSource selectFieldSource) throws JargonException {
		return new GenQuerySelectField(selectFieldName,
				selectFieldNumericTranslation, selectFieldType,
				selectFieldSource);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Select field");
		sb.append("\n   selectFieldColumnName:");
		sb.append(getSelectFieldColumnName());
		sb.append("\n   selectFieldNumericTranslation:");
		sb.append(getSelectFieldNumericTranslation());
		sb.append("\n   selectFieldType:");
		sb.append(getSelectFieldType());
		sb.append("\n   selectFieldSource:");
		sb.append(getSelectFieldSource());
		return sb.toString();
	}

	private GenQuerySelectField(final String selectFieldColumnName,
			final String selectFieldNumericTranslation,
			final SelectFieldTypes selectFieldType,
			final SelectFieldSource selectFieldSource) throws JargonException {

		super(selectFieldColumnName, selectFieldSource,
				selectFieldNumericTranslation);

		if (selectFieldType == null) {
			throw new JargonException("field type was null");
		}

		this.selectFieldType = selectFieldType;

	}

	public SelectFieldTypes getSelectFieldType() {
		return selectFieldType;
	}

}
