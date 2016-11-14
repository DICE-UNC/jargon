/**
 *
 */
package org.irods.jargon.core.query;

/**
 * Describes a metadata query operator.
 * <p/>
 * Note that this is used by the older 'string' query technique, which uses
 * iquest like queries, and is not used in the recommended
 * <code>IRODSGenQueryBuilder</code> query technique.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum AVUQueryOperatorEnum {
	EQUAL("=", 0, OperatorClass.VALUE), NOT_EQUAL("!=", 1, OperatorClass.VALUE), LESS_THAN(
			"<", 2, OperatorClass.VALUE), GREATER_THAN(">", 3,
			OperatorClass.VALUE), LESS_OR_EQUAL("<=", 4, OperatorClass.VALUE), GREATER_OR_EQUAL(
			">=", 5, OperatorClass.VALUE), NUM_LESS_THAN("<", 15,
			OperatorClass.VALUE), NUM_LESS_OR_EQUAL("<=", 17,
			OperatorClass.VALUE), NUM_GREATER_OR_EQUAL(">=", 18,
			OperatorClass.VALUE), IN("IN", 6, OperatorClass.VALUE_IN_ARRAY), NOT_IN(
			"NOT_IN", 7, OperatorClass.VALUE_IN_ARRAY), BETWEEN("BETWEEN", 8,
			OperatorClass.VALUE_RANGE), NOT_BETWEEN("NOT_BETWEEN", 9,
			OperatorClass.VALUE_RANGE), LIKE("LIKE", 10, OperatorClass.VALUE), NOT_LIKE(
			"NOT_LIKE", 11, OperatorClass.VALUE), SOUNDS_LIKE("SOUNDS_LIKE",
			11, OperatorClass.VALUE);

	private String operatorValue;
	private int operatorNumericValue;
	private OperatorClass operatorClass;

	private enum OperatorClass {
		VALUE, VALUE_RANGE, VALUE_IN_ARRAY, VALUE_BY_METADATA_TABLE
	}

	AVUQueryOperatorEnum(final String operatorValue,
			final int operatorNumericValue, final OperatorClass operatorClass) {
		this.operatorValue = operatorValue;
		this.operatorNumericValue = operatorNumericValue;
		this.operatorClass = operatorClass;
	}

	public String getOperatorValue() {
		return operatorValue;
	}

	public int getOperatorNumericValue() {
		return operatorNumericValue;
	}

	public OperatorClass getOperatorClass() {
		return operatorClass;
	}
}
