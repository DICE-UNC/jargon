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

	public static QueryConditionOperators getOperatorFromStringValue(
			final String stringValue) {
		if (stringValue == null || stringValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty stringValue");
		}

		if (stringValue.equals(NOT_EQUAL.operatorAsString)) {
			return NOT_EQUAL;
		}

		if (stringValue.equals(LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return LESS_THAN_OR_EQUAL_TO;
		}

		if (stringValue.equals(GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equals(NOT_LIKE.operatorAsString)) {
			return NOT_LIKE;
		}
		if (stringValue.equals(SOUNDS_LIKE.operatorAsString)) {
			return SOUNDS_LIKE;
		}
		if (stringValue.equals(SOUNDS_NOT_LIKE.operatorAsString)) {
			return SOUNDS_NOT_LIKE;
		}
		if (stringValue.equals(TABLE.operatorAsString)) {
			return TABLE;
		}
		if (stringValue.equals(NUMERIC_LESS_THAN.operatorAsString)) {
			return NUMERIC_LESS_THAN;
		}
		if (stringValue.equals(NUMERIC_LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_LESS_THAN_OR_EQUAL_TO;
		}
		if (stringValue
				.equals(NUMERIC_GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equals(NUMERIC_GREATER_THAN.operatorAsString)) {
			return NUMERIC_GREATER_THAN;
		}
		if (stringValue.equals(NUMERIC_EQUAL.operatorAsString)) {
			return NUMERIC_EQUAL;
		}
		if (stringValue.equals(EQUAL.operatorAsString)) {
			return EQUAL;
		}
		if (stringValue.equals(LESS_THAN.operatorAsString)) {
			return LESS_THAN;
		}
		if (stringValue.equals(GREATER_THAN.operatorAsString)) {
			return GREATER_THAN;
		}
		if (stringValue.equals(IN.operatorAsString)) {
			return IN;
		}
		if (stringValue.equals(BETWEEN.operatorAsString)) {
			return BETWEEN;
		}
		if (stringValue.equals(LIKE.operatorAsString)) {
			return LIKE;
		}

		return null;

	}
}
