/**
 *
 */
package org.irods.jargon.core.query;

import org.irods.jargon.core.query.GenQueryField.SelectFieldSource;

/**
 * Represents the field name, operator, and value for a condition in an IRODS
 * query after translating the field names to the internal IRODS values. This is
 * an internal representation for the query parser. This is an immutable object,
 * and is thread-safe.
 *
 * @author mikeconway
 *
 */
public class TranslatedGenQueryCondition {
	private final String columnName;
	private final org.irods.jargon.core.query.GenQueryField.SelectFieldSource fieldSource;
	private final String columnNumericTranslation;
	private final String operator;
	private final String value;

	/**
	 * Static initializer when the field is given as a value from the
	 * {@code RodsGenQueryEnum} enumeration.
	 *
	 * @param fieldName
	 *            {@link org.irods.jargon.core.query.RodsGenQueryEnum} value for the
	 *            condition field.
	 * @param operator
	 *            {@code String} with the operator.
	 * @param value
	 *            {@code String} with the value component of the condition.
	 * @return {@code TranslatedQueryCondition} object.
	 * @throws JargonQueryException
	 *             for query error
	 */
	public static TranslatedGenQueryCondition instance(final RodsGenQueryEnum fieldName, final String operator,
			final String value) throws JargonQueryException {
		return new TranslatedGenQueryCondition(fieldName, operator, value);
	}

	/**
	 * Static initializer when the field is given as a string that is the translated
	 * name of the field in a format that GenQuery will understand. This can be used
	 * when constructing query fields from extensible metadata values.
	 *
	 * @param fieldName
	 *            {@code String} with the translated value for the condition.
	 * @param operator
	 *            {@code String} with the operator.
	 * @param value
	 *            {@code String} with the value component of the condition.
	 * @param columnNumericTranslation
	 *            {@code String} with the field number for a packing instruction
	 * @return {@code TranslatedQueryCondition} object.
	 * @throws JargonQueryException
	 *             for query error
	 */
	public static TranslatedGenQueryCondition instanceForExtensibleMetaData(final String fieldName,
			final String operator, final String value, final String columnNumericTranslation)
			throws JargonQueryException {
		return new TranslatedGenQueryCondition(fieldName, operator, value, columnNumericTranslation);
	}

	/**
	 * Static initializer when the field is given as a string that is the translated
	 * name of the field in a format that GenQuery will understand.
	 *
	 * @param fieldName
	 *            {@code String} with the translated value for the condition.
	 * @param operator
	 *            {@code String} with the operator.
	 * @param value
	 *            {@code String} with the value component of the condition.
	 * @param columnNumericTranslation
	 *            {@code String} with the translation to use in a packing
	 *            instruction
	 * @return {@code TranslatedQueryCondition} object.
	 * @throws JargonQueryException
	 *             for query error
	 */
	public static TranslatedGenQueryCondition instanceWithFieldNameAndNumericTranslation(final String fieldName,
			final String operator, final String value, final String columnNumericTranslation)
			throws JargonQueryException {
		return new TranslatedGenQueryCondition(fieldName, operator, value, columnNumericTranslation);
	}

	private TranslatedGenQueryCondition(final String fieldName, final String operator, final String value,
			final String columnNumericTranslation) throws JargonQueryException {

		if (fieldName == null || fieldName.isEmpty()) {
			throw new JargonQueryException("field name in condition is null or blank");
		}

		if (operator == null) {
			throw new JargonQueryException("operator is null");
		}

		if (value == null) {
			throw new JargonQueryException("value in condition is null");
		}

		if (columnNumericTranslation == null || columnNumericTranslation.isEmpty()) {
			throw new JargonQueryException("columnNumericTranslation is null or blank");
		}

		columnName = fieldName;
		fieldSource = GenQuerySelectField.SelectFieldSource.EXTENSIBLE_METADATA;
		this.operator = operator;
		this.value = value;
		this.columnNumericTranslation = columnNumericTranslation;

	}

	private TranslatedGenQueryCondition(final RodsGenQueryEnum fieldName, final String operator, final String value)
			throws JargonQueryException {
		if (fieldName == null) {
			throw new JargonQueryException("field name in condition is null");
		}

		if (operator == null) {
			throw new JargonQueryException("operator is null");
		}

		if (value == null) {
			throw new JargonQueryException("value in condition is null");
		}

		columnName = fieldName.getName();
		fieldSource = GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD;
		this.operator = operator;
		this.value = value;
		columnNumericTranslation = String.valueOf(fieldName.getNumericValue());

	}

	private TranslatedGenQueryCondition(final String fieldName, final SelectFieldSource fieldSource,
			final String columnNumericTranslation, final String operator, final String value)
			throws JargonQueryException {
		if (fieldName == null || fieldName.length() == 0) {
			throw new JargonQueryException("field name in condition is blank or null");
		}

		if (fieldSource == null) {
			throw new JargonQueryException("field source in condition is null");
		}

		if (columnNumericTranslation == null || columnNumericTranslation.length() == 0) {
			throw new JargonQueryException("field source in condition is blank or null");
		}

		if (operator == null) {
			throw new JargonQueryException("operator is null");
		}

		if (value == null) {
			throw new JargonQueryException("value in condition is null");
		}

		columnName = fieldName;
		this.fieldSource = GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD;
		this.operator = operator;
		this.value = value;
		this.columnNumericTranslation = columnNumericTranslation;

	}

	public String getColumnName() {
		return columnName;
	}

	public SelectFieldSource getFieldSource() {
		return fieldSource;
	}

	public String getColumnNumericTranslation() {
		return columnNumericTranslation;
	}

	public String getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TranslatedGenQueryCondition [");
		if (columnName != null) {
			builder.append("columnName=").append(columnName).append(", ");
		}
		if (fieldSource != null) {
			builder.append("fieldSource=").append(fieldSource).append(", ");
		}
		if (columnNumericTranslation != null) {
			builder.append("columnNumericTranslation=").append(columnNumericTranslation).append(", ");
		}
		if (operator != null) {
			builder.append("operator=").append(operator).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value);
		}
		builder.append("]");
		return builder.toString();
	}

}
