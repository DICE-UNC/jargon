package org.irods.jargon.core.query;

/**
 * Represents possible operator values for general query conditions used in the
 * gen query builder
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public enum QueryConditionOperators {

	NOT_EQUAL("<>"), LESS_THAN_OR_EQUAL_TO("<="), GREATER_THAN_OR_EQUAL_TO(">="), NOT_LIKE(
			"not like"), SOUNDS_LIKE("sounds like"), SOUNDS_NOT_LIKE(
			"sounds not like"), TABLE("table"), NUMERIC_LESS_THAN("n<"), NUMERIC_LESS_THAN_OR_EQUAL_TO(
			"n<="), NUMERIC_GREATER_THAN_OR_EQUAL_TO("n>="), NUMERIC_GREATER_THAN(
			"n>"), NUMERIC_EQUAL("n="), EQUAL("="), LESS_THAN("<"), GREATER_THAN(
			">"), IN("in"), BETWEEN("between"), LIKE("like");

	private String operatorAsString;

	QueryConditionOperators(final String operatorAsString) {
		this.operatorAsString = operatorAsString;
	}

	public String getOperatorAsString() {
		return operatorAsString;
	}
}
