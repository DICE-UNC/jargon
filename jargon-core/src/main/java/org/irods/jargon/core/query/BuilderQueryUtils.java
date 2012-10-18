/**
 * General utilities for builder gen queries as defined in <code>IRODSGenQueryBuilder</code>
 */
package org.irods.jargon.core.query;

/**
 * @author mikeconway
 * 
 */
public class BuilderQueryUtils {

	/**
	 * right now all static and no instances
	 */
	private BuilderQueryUtils() {
	}

	/**
	 * Translate between operator values as defined in {@link AVUQueryElement}
	 * and those understood in {@link GenQueryBuilderCondition}.
	 * 
	 * @param avuQueryElement
	 *            {@link AVUQueryElement}
	 */
	public static QueryConditionOperators translateAVUQueryElementOperatorToBuilderQueryCondition(
			final AVUQueryElement avuQueryElement) throws JargonQueryException {

		if (avuQueryElement == null) {
			throw new IllegalArgumentException("avuQueryElement is null");
		}

		switch (avuQueryElement.getOperator()) {
		case EQUAL:
			return QueryConditionOperators.EQUAL;
		case NOT_EQUAL:
			return QueryConditionOperators.NOT_EQUAL;
		case LESS_THAN:
			return QueryConditionOperators.LESS_THAN;
		case GREATER_THAN:
			return QueryConditionOperators.GREATER_THAN;
		case LESS_OR_EQUAL:
			return QueryConditionOperators.LESS_THAN_OR_EQUAL_TO;
		case GREATER_OR_EQUAL:
			return QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO;
		case NUM_LESS_THAN:
			return QueryConditionOperators.NUMERIC_LESS_THAN;
		case NUM_LESS_OR_EQUAL:
			return QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO;
		case NUM_GREATER_OR_EQUAL:
			return QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		case IN:
			return QueryConditionOperators.IN;
		case NOT_IN:
			return QueryConditionOperators.NOT_IN;
		case BETWEEN:
			return QueryConditionOperators.BETWEEN;
		case NOT_BETWEEN:
			return QueryConditionOperators.NOT_BETWEEN;
		case LIKE:
			return QueryConditionOperators.LIKE;
		case NOT_LIKE:
			return QueryConditionOperators.NOT_LIKE;
		case SOUNDS_LIKE:
			return QueryConditionOperators.SOUNDS_LIKE;
		default:
			throw new JargonQueryException("unknown query operator");

		}
	}
}
