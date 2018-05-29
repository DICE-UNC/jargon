package org.irods.jargon.core.query;

import org.junit.Assert;
import org.junit.Test;

public class QueryConditionOperatorsTest {

	@Test
	public void testOperatorsFromString() {

		Assert.assertEquals(QueryConditionOperators.NOT_EQUAL, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.NOT_EQUAL.getOperatorAsString()));

		Assert.assertEquals(QueryConditionOperators.LESS_THAN_OR_EQUAL_TO, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.LESS_THAN_OR_EQUAL_TO.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NOT_LIKE, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.NOT_LIKE.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.SOUNDS_LIKE, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.SOUNDS_LIKE.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.SOUNDS_NOT_LIKE, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.SOUNDS_NOT_LIKE.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.TABLE, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.TABLE.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NUMERIC_LESS_THAN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.NUMERIC_LESS_THAN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO,
				QueryConditionOperators.getOperatorFromEnumStringValue(
						QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO,
				QueryConditionOperators.getOperatorFromEnumStringValue(
						QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NUMERIC_GREATER_THAN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.NUMERIC_GREATER_THAN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.NUMERIC_EQUAL, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.NUMERIC_EQUAL.getOperatorAsString()));

		Assert.assertEquals(QueryConditionOperators.EQUAL, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.EQUAL.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.LESS_THAN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.LESS_THAN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.GREATER_THAN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.GREATER_THAN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.IN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.IN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.BETWEEN, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.BETWEEN.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.LIKE, QueryConditionOperators
				.getOperatorFromEnumStringValue(QueryConditionOperators.LIKE.getOperatorAsString()));

		Assert.assertNull(QueryConditionOperators.getOperatorFromEnumStringValue("goo"));

	}
}
