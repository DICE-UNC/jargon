package org.irods.jargon.core.query;

/**
 * Represents possible operator values for general query conditions used in the
 * gen query builder
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public enum QueryConditionOperators {

	NOT_EQUAL("<>"), LESS_THAN_OR_EQUAL_TO("<="), GREATER_THAN_OR_EQUAL_TO(">="), NOT_LIKE("not like"), SOUNDS_LIKE(
			"sounds like"), SOUNDS_NOT_LIKE("sounds not like"), TABLE("table"), NUMERIC_LESS_THAN(
					"n<"), NUMERIC_LESS_THAN_OR_EQUAL_TO("n<="), NUMERIC_GREATER_THAN_OR_EQUAL_TO(
							"n>="), NUMERIC_GREATER_THAN("n>"), NUMERIC_EQUAL("n="), EQUAL(
									"="), LESS_THAN("<"), GREATER_THAN(
											">"), IN("in"), NOT_IN("not in"), BETWEEN("between"), NOT_BETWEEN("not between"),LIKE("like");

	private String operatorAsString;

	QueryConditionOperators(final String operatorAsString) {
		this.operatorAsString = operatorAsString;
	}

	public String getOperatorAsString() {
		return operatorAsString;
	}

	/**
	 * Get the query operator from the genQuery friendly string value, (e.g. ">"
	 * symbol)
	 * 
	 * @param stringValue
	 *            {@code String} with the symbolic value
	 * @return {@link QueryConditionOperators} with the enum corresponding
	 */
	public static QueryConditionOperators getOperatorFromStringValue(final String stringValue) {
		if (stringValue == null || stringValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty stringValue");
		}

		if (stringValue.equalsIgnoreCase(NOT_EQUAL.operatorAsString)) {
			return NOT_EQUAL;
		}

		if (stringValue.equalsIgnoreCase(LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return LESS_THAN_OR_EQUAL_TO;
		}

		if (stringValue.equalsIgnoreCase(GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NOT_LIKE.operatorAsString)) {
			return NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_LIKE.operatorAsString)) {
			return SOUNDS_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_NOT_LIKE.operatorAsString)) {
			return SOUNDS_NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(TABLE.operatorAsString)) {
			return TABLE;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN.operatorAsString)) {
			return NUMERIC_LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_LESS_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN.operatorAsString)) {
			return NUMERIC_GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_EQUAL.operatorAsString)) {
			return NUMERIC_EQUAL;
		}
		if (stringValue.equalsIgnoreCase(EQUAL.operatorAsString)) {
			return EQUAL;
		}
		if (stringValue.equalsIgnoreCase(LESS_THAN.operatorAsString)) {
			return LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(GREATER_THAN.operatorAsString)) {
			return GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(IN.operatorAsString)) {
			return IN;
		}
		if (stringValue.equalsIgnoreCase(NOT_IN.operatorAsString)) {
			return NOT_IN;
		}
		if (stringValue.equalsIgnoreCase(BETWEEN.operatorAsString)) {
			return BETWEEN;
		}
		if (stringValue.equalsIgnoreCase(NOT_BETWEEN.operatorAsString)) {
			return NOT_BETWEEN;
		}
		if (stringValue.equals(LIKE.operatorAsString)) {
			return LIKE;
		}

		return null;

	}

	/**
	 * Turn a string-ified representation of the enum value (e.g. "GREATER THAN") to
	 * the actual enum value
	 * 
	 * @param stringValue
	 *            {@code String} with the string-ified enum name
	 * @return {@link QueryConditionOperators} enum value
	 */
	public static QueryConditionOperators getOperatorFromEnumStringValue(final String stringValue) {
		if (stringValue == null || stringValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty stringValue");
		}

		if (stringValue.equalsIgnoreCase(NOT_EQUAL.toString())) {
			return NOT_EQUAL;
		}

		if (stringValue.equalsIgnoreCase(LESS_THAN_OR_EQUAL_TO.toString())) {
			return LESS_THAN_OR_EQUAL_TO;
		}

		if (stringValue.equalsIgnoreCase(GREATER_THAN_OR_EQUAL_TO.toString())) {
			return GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NOT_LIKE.toString())) {
			return NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_LIKE.toString())) {
			return SOUNDS_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_NOT_LIKE.toString())) {
			return SOUNDS_NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(TABLE.toString())) {
			return TABLE;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN.toString())) {
			return NUMERIC_LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN_OR_EQUAL_TO.toString())) {
			return NUMERIC_LESS_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN_OR_EQUAL_TO.toString())) {
			return NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN.toString())) {
			return NUMERIC_GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_EQUAL.toString())) {
			return NUMERIC_EQUAL;
		}
		if (stringValue.equalsIgnoreCase(EQUAL.toString())) {
			return EQUAL;
		}
		if (stringValue.equalsIgnoreCase(LESS_THAN.toString())) {
			return LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(GREATER_THAN.toString())) {
			return GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NOT_IN.toString())) {
			return NOT_IN;
		}
		if (stringValue.equalsIgnoreCase(IN.toString())) {
			return IN;
		}
		if (stringValue.equalsIgnoreCase(BETWEEN.toString())) {
			return BETWEEN;
		}
		if (stringValue.equalsIgnoreCase(NOT_BETWEEN.toString())) {
			return NOT_BETWEEN;
		}
		if (stringValue.equals(LIKE.toString())) {
			return LIKE;
		}

		return null;

	}

}
