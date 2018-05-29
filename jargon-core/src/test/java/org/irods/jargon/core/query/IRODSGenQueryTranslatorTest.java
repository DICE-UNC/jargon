package org.irods.jargon.core.query;

import java.util.List;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class IRODSGenQueryTranslatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIRODSQueryTranslator() throws Exception {
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.3", "d", "zone");
		new IRODSGenQueryTranslator(props);

	}

	@Test(expected = JargonException.class)
	public final void testIRODSQueryTranslatorNullServerProps() throws Exception {
		// test passes if no exceptions were thrown
		new IRODSGenQueryTranslator(null);
	}

	@Test
	public final void testParseSelectsIntoListOfNames() throws Exception {
		String query = "select blah, yelp";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<String> selects = translator.parseSelectsIntoListOfNames(query);
		Assert.assertEquals(2, selects.size());
		Assert.assertEquals("BLAH", selects.get(0));
		Assert.assertEquals("YELP", selects.get(1));
	}

	@Test(expected = JargonQueryException.class)
	public final void testParseSelectsIntoListOfNamesNoSelect() throws Exception {
		String query = "blah, yelp";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		translator.parseSelectsIntoListOfNames(query);

	}

	@Test
	public final void testParseConditionsIntoList() throws Exception {
		String query = "select blah, yelp where a = 1";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<GenQueryCondition> conditions = translator.parseConditionsIntoList(query);
		Assert.assertEquals(1, conditions.size());
		GenQueryCondition cond = conditions.get(0);
		Assert.assertEquals("a", cond.getFieldName());
		Assert.assertEquals("=", cond.getOperator());
		Assert.assertEquals("1", cond.getValue());

	}

	@Test
	public final void testParseConditionsIntoListWithGroupBy() throws Exception {
		String query = "select blah, yelp where a = 1 group by hello";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<GenQueryCondition> conditions = translator.parseConditionsIntoList(query);
		Assert.assertEquals(1, conditions.size());
		GenQueryCondition cond = conditions.get(0);
		Assert.assertEquals("a", cond.getFieldName());
		Assert.assertEquals("=", cond.getOperator());
		Assert.assertEquals("1", cond.getValue());

	}

	@Test(expected = JargonQueryException.class)
	public final void testParseConditionsIntoListNoSpaceAfterOperatorFinalPosition() throws Exception {
		String query = "select blah, yelp where a =1";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		translator.parseConditionsIntoList(query);

	}

	@Test(expected = JargonQueryException.class)
	public final void testParseConditionsIntoListNoSpaceAfterOperator() throws Exception {
		String query = "select blah, yelp where a =1 and x = 4";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		translator.parseConditionsIntoList(query);

	}

	@Test
	public final void testParseTwoConditionsIntoList() throws Exception {
		String query = "select blah, yelp where a = 1 and z > 1234";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<GenQueryCondition> conditions = translator.parseConditionsIntoList(query);
		Assert.assertEquals(2, conditions.size());
		GenQueryCondition cond = conditions.get(0);
		Assert.assertEquals("a", cond.getFieldName());
		Assert.assertEquals("=", cond.getOperator());
		Assert.assertEquals("1", cond.getValue());

		cond = conditions.get(1);
		Assert.assertEquals("z", cond.getFieldName());
		Assert.assertEquals(">", cond.getOperator());
		Assert.assertEquals("1234", cond.getValue());

	}

	@Test
	public final void testParseConditionWithEmbeddedSingleQuote() throws Exception {
		String query = "SELECT DATA_NAME WHERE COLL_NAME = '/test1/home/test1/test-scratch/IRODSFileTest' AND DATA_NAME = 'testExistsQuote\\'infilename.txt'";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.4", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<GenQueryCondition> conditions = translator.parseConditionsIntoList(query);
		Assert.assertEquals(2, conditions.size());
		GenQueryCondition cond = conditions.get(0);
		Assert.assertEquals("COLL_NAME", cond.getFieldName());
		Assert.assertEquals("=", cond.getOperator());
		Assert.assertEquals("'/test1/home/test1/test-scratch/IRODSFileTest'", cond.getValue());

		cond = conditions.get(1);
		Assert.assertEquals("DATA_NAME", cond.getFieldName());
		Assert.assertEquals("=", cond.getOperator());
		Assert.assertEquals("'testExistsQuote'infilename.txt'", cond.getValue());

	}

	@Test
	public final void testParseNoConditionsIntoList() throws Exception {
		String query = "select blah, yelp";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		List<GenQueryCondition> conditions = translator.parseConditionsIntoList(query);
		Assert.assertEquals(0, conditions.size());
	}

	@Test(expected = JargonQueryException.class)
	public final void testConditions2WheresIntoList() throws Exception {
		String query = "select blah, yelp where where a = 2";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		translator.parseConditionsIntoList(query);
	}

	@Test(expected = JargonQueryException.class)
	public final void testConditionsIncompleteList() throws Exception {
		String query = "select blah, yelp where where a = ";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		translator.parseConditionsIntoList(query);
	}

	@Test
	public final void testTranslateOnlySelectsIntoTranslatedQuery() throws Exception {

		String query = ("select " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(2, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals(RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(), sel1.getSelectFieldColumnName());
		GenQuerySelectField sel2 = translatedQuery.getSelectFields().get(1);
		Assert.assertEquals(RodsGenQueryEnum.COL_AUDIT_USER_ID.getName(), sel2.getSelectFieldColumnName());
	}

	@Test
	public final void testDistinctQuery() throws Exception {

		String query = ("select " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertTrue("this should be classified as a distinct query", translatedQuery.isDistinct());

	}

	@Test
	public final void testNonDistinctQuery() throws Exception {

		String query = ("select non-distinct " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertFalse("this should not be classified as a distinct query", translatedQuery.isDistinct());

	}

	@Test(expected = JargonQueryException.class)
	public final void testNonDistinctQuerySelectMissing() throws Exception {

		String query = ("non-distinct" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		translator.getTranslatedQuery(irodsQuery);

	}

	@Test
	public final void testNonDistinctQueryUpperCase() throws Exception {

		String query = ("select NON-DISTINCT " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertFalse("this should not be classified as a distinct query", translatedQuery.isDistinct());

	}

	@Test
	public final void testTranslateCountAggregation() throws Exception {

		String query = ("select count(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.COUNT,
				sel1.getSelectFieldType());

	}

	@Test
	public final void testTranslateSumAggregation() throws Exception {

		String query = ("select SUM(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.SUM,
				sel1.getSelectFieldType());

	}

	@Test
	public final void testTranslateAvgAggregation() throws Exception {

		String query = ("select Avg(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.AVG,
				sel1.getSelectFieldType());

	}

	@Test
	public final void testTranslateMinAggregation() throws Exception {

		String query = ("select mIn(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.MIN,
				sel1.getSelectFieldType());

	}

	@Test
	public final void testTranslateMaxAggregation() throws Exception {

		String query = ("select maX(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.MAX,
				sel1.getSelectFieldType());

	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateAggregationWithEmbeddedSpaces() throws Exception {

		String query = ("select mIn(  " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + "     )");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		translator.getTranslatedQuery(irodsQuery);

	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateCountAggregationOpenNoClose() throws Exception {

		String query = ("select count(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName());
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		translator.getTranslatedQuery(irodsQuery);

	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateAggregationInvalidAggregationType() throws Exception {

		String query = ("select bob(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		translator.getTranslatedQuery(irodsQuery);

	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateAggregationTwoOpenParens() throws Exception {

		String query = ("select SUM((" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ")");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertNull("should have null indicating irods lookup failed", sel1);

	}

	@Test
	public final void testTranslateAggregationTwoCloseParens() throws Exception {

		String query = ("select SUM(" + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + "))");
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(1, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals("field not translated", RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(),
				sel1.getSelectFieldColumnName());
		Assert.assertEquals("did not classify as a count()", GenQuerySelectField.SelectFieldTypes.SUM,
				sel1.getSelectFieldType());

	}

	@Test
	public final void testTranslateSelectsAndConditionsIntoTranslatedQuery() throws Exception {
		String query = ("select " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName() + " where " + RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getName()
				+ " = '123'");

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);
		Assert.assertEquals(2, translatedQuery.getSelectFields().size());
		GenQuerySelectField sel1 = translatedQuery.getSelectFields().get(0);
		Assert.assertEquals(RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName(), sel1.getSelectFieldColumnName());
		GenQuerySelectField sel2 = translatedQuery.getSelectFields().get(1);
		Assert.assertEquals(RodsGenQueryEnum.COL_AUDIT_USER_ID.getName(), sel2.getSelectFieldColumnName());
		Assert.assertEquals(1, translatedQuery.getTranslatedQueryConditions().size());
		TranslatedGenQueryCondition testCondition = translatedQuery.getTranslatedQueryConditions().get(0);
		Assert.assertEquals(RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getName(), testCondition.getColumnName());
		Assert.assertEquals("=", testCondition.getOperator());
		Assert.assertEquals("'123'", testCondition.getValue());
	}

	@Test
	public final void testTranslateQueryCheckingIRODSQueryFieldsType() throws Exception {
		String query = ("select " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName() + " where " + RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getName()
				+ " = '123'");

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);

		Assert.assertEquals(1, translatedQuery.getTranslatedQueryConditions().size());
		TranslatedGenQueryCondition testCondition = translatedQuery.getTranslatedQueryConditions().get(0);
		Assert.assertEquals("this should be an irods gen query field",
				GenQuerySelectField.SelectFieldSource.DEFINED_QUERY_FIELD, testCondition.getFieldSource());
	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateQueryMissingWhere() throws Exception {
		StringBuilder query = new StringBuilder();

		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		query.append(',');
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(',');
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(" = '");
		query.append("joebob");
		query.append("'");
		String queryString = query.toString();
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 10);

		translator.getTranslatedQuery(irodsQuery);
	}

	@Test(expected = JargonQueryException.class)
	public final void testTranslateQueryMissingComma() throws Exception {
		StringBuilder query = new StringBuilder();

		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		query.append(',');
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(" = '");
		query.append("joebob");
		query.append("'");
		String queryString = query.toString();
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 10);

		translator.getTranslatedQuery(irodsQuery);
	}

	@Test
	public final void testTranslateQueryCheckingIRODSQueryFieldTranslation() throws Exception {
		String query = ("select " + RodsGenQueryEnum.COL_DATA_ACCESS_DATA_ID.getName() + ","
				+ RodsGenQueryEnum.COL_AUDIT_USER_ID.getName() + " where " + RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getName()
				+ " = '123'");

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		TranslatedIRODSGenQuery translatedQuery = translator.getTranslatedQuery(irodsQuery);

		Assert.assertEquals(1, translatedQuery.getTranslatedQueryConditions().size());
		TranslatedGenQueryCondition testCondition = translatedQuery.getTranslatedQueryConditions().get(0);
		Assert.assertEquals("this should be looked up and translated to irods code",
				String.valueOf(RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getNumericValue()),
				testCondition.getColumnNumericTranslation());
	}

	@Test
	public final void queryExample1Test() throws Exception {
		String query = "SELECT COLL_ID,COLL_NAME,META_COLL_ATTR_NAME,META_COLL_ATTR_VALUE,META_COLL_ATTR_UNITS WHERE META_COLL_ATTR_NAME = 'PolicyDrivenService:PolicyRepository' AND META_COLL_ATTR_VALUE = 'My first policy'";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);

		translator.getTranslatedQuery(irodsQuery);

		// no errors means good translation

	}

	@Ignore
	// FIXME: between does not properly work in iquest?
	public final void queryWithBetweenAndTwoValues() throws Exception {
		String query = "SELECT COLL_ID,COLL_NAME,META_COLL_ATTR_NAME,META_COLL_ATTR_VALUE,META_COLL_ATTR_UNITS WHERE META_COLL_ATTR_NAME BETWEEN 'inval1' AND 'inval2'";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);
		translator.getTranslatedQuery(irodsQuery);

		// no errors means good translation

	}

	@Ignore
	// FIXME: work in progress
	public final void queryWithInAndTwoValues() throws Exception {
		String query = "SELECT COLL_ID,COLL_NAME,META_COLL_ATTR_NAME,META_COLL_ATTR_VALUE,META_COLL_ATTR_UNITS WHERE META_COLL_ATTR_NAME IN ('inval1','inval2')";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.5", "d", "zone");

		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);
		translator.getTranslatedQuery(irodsQuery);

		// no errors means good translation

	}

	@Test
	public final void queryWithNotLike() throws Exception {
		String query = "SELECT USER_NAME WHERE USER_NAME NOT LIKE 'thisname'";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);
		TranslatedIRODSGenQuery gq = translator.getTranslatedQuery(irodsQuery);
		TranslatedGenQueryCondition qc = gq.getTranslatedQueryConditions().get(0);
		Assert.assertNotNull("null condition set", qc);
		Assert.assertEquals("did not set not like in condition", "NOT LIKE", qc.getOperator());

	}

	@Test(expected = JargonQueryException.class)
	public final void queryWithNotNotLike() throws Exception {
		String query = "SELECT USER_NAME WHERE USER_NAME NOT NOT LIKE 'thisname'";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);
		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 10);
		translator.getTranslatedQuery(irodsQuery);
	}

	@Test
	public final void tokenizeOrderBy() throws Exception {
		String query = "SELECT COLL_ID,COLL_NAME,META_COLL_ATTR_NAME,META_COLL_ATTR_VALUE,META_COLL_ATTR_UNITS ORDER BY META_COLL_ATTR_NAME";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.4", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		List<String> orderByFields = translator.parseOrderByFieldsIntoList(query);

		Assert.assertEquals("did not set the group by", 1, orderByFields.size());
		Assert.assertEquals("did not find order by field", "META_COLL_ATTR_NAME", orderByFields.get(0));

	}

	@Test
	public final void tokenizeTwoOrderBy() throws Exception {
		String query = "SELECT COLL_ID,COLL_NAME,META_COLL_ATTR_NAME,META_COLL_ATTR_VALUE,META_COLL_ATTR_UNITS ORDER BY META_COLL_ATTR_NAME, META_COLL_ATTR_VALUE";
		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.4", "d", "zone");
		IRODSGenQueryTranslator translator = new IRODSGenQueryTranslator(props);

		List<String> orderByFields = translator.parseOrderByFieldsIntoList(query);

		Assert.assertEquals("did not set the group by", 2, orderByFields.size());
		Assert.assertEquals("did not find order by field", "META_COLL_ATTR_NAME", orderByFields.get(0));
		Assert.assertEquals("did not find order by field", "META_COLL_ATTR_VALUE", orderByFields.get(1));

	}

}
