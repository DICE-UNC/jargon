package org.irods.jargon.core.query;

import org.irods.jargon.core.query.GenQuerySelectField.SelectFieldSource;

/**
 * Represents an immutable condition part of a gen query as produced by the gen
 * query builder tool.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GenQueryBuilderCondition {

	private final String selectFieldColumnName;
	private final SelectFieldSource selectFieldSource;
	private final String selectFieldNumericTranslation;
	private final String operator;
	private final String value;

	/**
	 * @param selectFieldColumnName
	 * @param selectFieldSource
	 * @param selectFieldNumericTranslation
	 * @param operator
	 * @param value
	 */
	private GenQueryBuilderCondition(String selectFieldColumnName,
			SelectFieldSource selectFieldSource,
			String selectFieldNumericTranslation, String operator, String value) {
		this.selectFieldColumnName = selectFieldColumnName;
		this.selectFieldSource = selectFieldSource;
		this.selectFieldNumericTranslation = selectFieldNumericTranslation;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * @return the selectFieldColumnName
	 */
	public String getSelectFieldColumnName() {
		return selectFieldColumnName;
	}

	/**
	 * @return the selectFieldSource
	 */
	public SelectFieldSource getSelectFieldSource() {
		return selectFieldSource;
	}

	/**
	 * @return the selectFieldNumericTranslation
	 */
	public String getSelectFieldNumericTranslation() {
		return selectFieldNumericTranslation;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
