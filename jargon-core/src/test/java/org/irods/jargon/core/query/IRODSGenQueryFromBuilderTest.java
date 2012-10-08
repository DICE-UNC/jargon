package org.irods.jargon.core.query;

import junit.framework.Assert;

import org.junit.Test;

public class IRODSGenQueryFromBuilderTest {

	@Test
	public void testCaseInsensitiveQueryMakesConditionsUpperCase()
			throws Exception {
		String testValue = "testvalue";
		IRODSGenQueryBuilder irodsGenQueryBuilder = new IRODSGenQueryBuilder(
				true, true, null);
		irodsGenQueryBuilder.addSelectAsGenQueryValue(
				RodsGenQueryEnum.COL_D_DATA_PATH).addConditionAsGenQueryField(
				RodsGenQueryEnum.COL_COLL_ACCESS_NAME,
				QueryConditionOperators.EQUAL, testValue);
		IRODSGenQueryFromBuilder query = irodsGenQueryBuilder
				.exportIRODSQueryFromBuilder(10);
		TranslatedIRODSGenQuery translatedIRODSGenQuery = query
				.convertToTranslatedIRODSGenQuery();
		TranslatedGenQueryCondition condition = translatedIRODSGenQuery
				.getTranslatedQueryConditions().get(0);
		Assert.assertEquals("did not upper case the condition",
				'\'' + testValue.toUpperCase() + '\'', condition.getValue());

	}

	@Test
	public void testCaseInsensitiveQueryNotUpperCase() throws Exception {
		String testValue = "testvalue";
		IRODSGenQueryBuilder irodsGenQueryBuilder = new IRODSGenQueryBuilder(
				true, false, null);
		irodsGenQueryBuilder.addSelectAsGenQueryValue(
				RodsGenQueryEnum.COL_D_DATA_PATH).addConditionAsGenQueryField(
				RodsGenQueryEnum.COL_COLL_ACCESS_NAME,
				QueryConditionOperators.EQUAL, testValue);
		IRODSGenQueryFromBuilder query = irodsGenQueryBuilder
				.exportIRODSQueryFromBuilder(10);
		TranslatedIRODSGenQuery translatedIRODSGenQuery = query
				.convertToTranslatedIRODSGenQuery();
		TranslatedGenQueryCondition condition = translatedIRODSGenQuery
				.getTranslatedQueryConditions().get(0);
		Assert.assertEquals("did not upper case the condition",
				'\'' + testValue + '\'', condition.getValue());

	}

}
