package org.irods.jargon.core.query;

import org.irods.jargon.core.query.GenQueryField.SelectFieldSource;

/**
 * Represents an immutable condition part of a gen query as produced by the gen
 * query builder tool.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class GenQueryBuilderCondition {

	private final String selectFieldColumnName;
	private final SelectFieldSource selectFieldSource;
	private final String selectFieldNumericTranslation;
	private final QueryConditionOperators operator;
	private final String value;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GenQueryBuilderCondition");
		sb.append("\n\t selectFieldSource:");
		sb.append(selectFieldSource);
		sb.append("\n\t selectFieldNumericTranslation:");
		sb.append(selectFieldNumericTranslation);
		sb.append("\n\t selectFieldColumnName:");
		sb.append(selectFieldColumnName);
		sb.append("\n\t operator:");
		sb.append(operator);
		sb.append("\n\t value:");
		sb.append(value);
		return sb.toString();
	}

	/**
	 * Create an immutable instance of an individual condition in a general
	 * query
	 * 
	 * @param selectFieldColumnName
	 *            <code>String</code> with the column name
	 * @param selectFieldSource
	 *            {@link SelectFieldSource} that reflects the type of field
	 * @param selectFieldNumericTranslation
	 *            <code>String</code> with the numeric iRODS gen query protocol
	 *            value that maps to this field
	 * @param operator
	 *            {@linkQueryConditionOperators} value with the operation for
	 *            the condition
	 * @param value
	 *            <code>String</code> with the right hand side of the query
	 *            condition
	 * @return
	 */
	static GenQueryBuilderCondition instance(
			final String selectFieldColumnName,
			final SelectFieldSource selectFieldSource,
			final String selectFieldNumericTranslation,
			final QueryConditionOperators operator, final String value) {

		return new GenQueryBuilderCondition(selectFieldColumnName,
				selectFieldSource, selectFieldNumericTranslation, operator,
				value);
	}

	/**
	 * 
	 * @param selectFieldColumnName
	 * @param selectFieldSource
	 * @param selectFieldNumericTranslation
	 * @param operator
	 * @param value
	 */
	private GenQueryBuilderCondition(final String selectFieldColumnName,
			final SelectFieldSource selectFieldSource,
			final String selectFieldNumericTranslation,
			final QueryConditionOperators operator,
			final String value) {
		this.selectFieldColumnName = selectFieldColumnName;
		this.selectFieldSource = selectFieldSource;
		this.selectFieldNumericTranslation = selectFieldNumericTranslation;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * @return the selectFieldColumnName
	 */
	String getSelectFieldColumnName() {
		return selectFieldColumnName;
	}

	/**
	 * @return the selectFieldSource
	 */
	SelectFieldSource getSelectFieldSource() {
		return selectFieldSource;
	}

	/**
	 * @return the selectFieldNumericTranslation
	 */
	String getSelectFieldNumericTranslation() {
		return selectFieldNumericTranslation;
	}

	/**
	 * @return the operator
	 */
	QueryConditionOperators getOperator() {
		return operator;
	}

	/**
	 * @return the value
	 */
	String getValue() {
		return value;
	}

}
