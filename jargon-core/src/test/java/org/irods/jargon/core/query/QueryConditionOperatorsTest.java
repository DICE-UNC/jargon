package org.irods.jargon.core.query;

import org.junit.Assert;
import org.junit.Test;

public class QueryConditionOperatorsTest {

	@Test
	public void testOperatorsFromString() {

		Assert.assertEquals(
				QueryConditionOperators.NOT_EQUAL,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NOT_EQUAL
						.getOperatorAsString()));

		Assert.assertEquals(
				QueryConditionOperators.LESS_THAN_OR_EQUAL_TO,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.LESS_THAN_OR_EQUAL_TO
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NOT_LIKE,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NOT_LIKE
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.SOUNDS_LIKE,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.SOUNDS_LIKE
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.SOUNDS_NOT_LIKE,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.SOUNDS_NOT_LIKE
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.TABLE,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.TABLE
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NUMERIC_LESS_THAN,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NUMERIC_LESS_THAN
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NUMERIC_GREATER_THAN,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NUMERIC_GREATER_THAN
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.NUMERIC_EQUAL,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.NUMERIC_EQUAL
						.getOperatorAsString()));

		Assert.assertEquals(
				QueryConditionOperators.EQUAL,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.EQUAL
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.LESS_THAN,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.LESS_THAN
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.GREATER_THAN,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.GREATER_THAN
						.getOperatorAsString()));
		Assert.assertEquals(QueryConditionOperators.IN, QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.IN
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.BETWEEN,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.BETWEEN
						.getOperatorAsString()));
		Assert.assertEquals(
				QueryConditionOperators.LIKE,
				QueryConditionOperators
				.getOperatorFromStringValue(QueryConditionOperators.LIKE
						.getOperatorAsString()));

		Assert.assertNull(QueryConditionOperators
				.getOperatorFromStringValue("goo"));

	}
}
