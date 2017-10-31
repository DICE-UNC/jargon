package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecificQueryAOTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private String query = "select * from table";
	private String alias = "neato_query";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testGetSpecficQueryAO() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		Assert.assertNotNull("queryAO is null", queryAO);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddSpecificQueryNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.addSpecificQuery(null);

	}

	@Test
	public void testAddSpecificQuery() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				alias, query);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);

		SpecificQueryDefinition actual = queryAO
				.findSpecificQueryByAlias(alias);
		Assert.assertNotNull("did not find query I just added", actual);

	}

	@Test(expected = DuplicateDataException.class)
	public void testAddSpecificQueryDuplicate() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				alias, query);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveSpecificQueryNullQuery() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQuery(null);

	}

	@Test
	public void testRemoveSpecificQuery() throws Exception {
		String alias = "testRemoveSpecificQuery";
		String query = "testRemoveSpecificQuery sql statement";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}
		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				alias, query);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.addSpecificQuery(specificQuery);
		queryAO.removeSpecificQuery(specificQuery);

		boolean nowRemoved = false;

		try {
			queryAO.findSpecificQueryByAlias(alias);
		} catch (DataNotFoundException dnf) {
			nowRemoved = true;
		}

		Assert.assertTrue("did not remove it", nowRemoved);

	}

	@Test
	public void testRemoveSpecificQueryBySQL() throws Exception {
		String alias = "testRemoveSpecificQueryBySQL";
		String query = "testRemoveSpecificQueryBySQL sql statement";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}
		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				alias, query);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.addSpecificQuery(specificQuery);
		queryAO.removeAllSpecificQueryBySQL(query);

		boolean nowRemoved = false;

		try {
			queryAO.findSpecificQueryByAlias(alias);
		} catch (DataNotFoundException dnf) {
			nowRemoved = true;
		}

		Assert.assertTrue("did not remove it", nowRemoved);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveSpecificQueryBySQLNull() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		queryAO.removeAllSpecificQueryBySQL(null);

	}

	@Test
	public void testExecuteSpecificQueryLS() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		SpecificQuery specificQuery = SpecificQuery.instanceWithNoArguments(
				"ls", 0, "");

		SpecificQueryResultSet specificQueryResultSet = queryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						accessObjectFactory.getJargonProperties()
								.getMaxFilesAndDirsQueryMax());
		Assert.assertNotNull("null result set", specificQueryResultSet);
		Assert.assertFalse("no results returned, expected at least ls and lsl",
				specificQueryResultSet.getResults().isEmpty());

	}

	@Test
	public void testListLikeLS() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		List<SpecificQueryDefinition> actual = queryAO
				.listSpecificQueryByAliasLike("ls");
		Assert.assertFalse("did not get results from query", actual.isEmpty());
		for (SpecificQueryDefinition definition : actual) {
			Assert.assertFalse("no alias", definition.getAlias().isEmpty());
			Assert.assertFalse("no query", definition.getSql().isEmpty());
		}
	}

	@Test
	public void testCountArgumentsInQuery() throws Exception {
		String query = "select R_USER_MAIN.user_name ,R_USER_MAIN.zone_name,"
				+ "R_TOKN_MAIN.token_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where "
				+ "R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND "
				+ "R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = 'R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id";

		int count = SpecificQueryAOImpl.countArgumentsInQuery(query);
		Assert.assertEquals("incorrect count from query", 1, count);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCountArgumentsInNullQuery() throws Exception {
		SpecificQueryAOImpl.countArgumentsInQuery(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCountArgumentsInBlankQuery() throws Exception {
		SpecificQueryAOImpl.countArgumentsInQuery("");
	}

	@Test
	public void parseColumnNamesFromQuery() throws Exception {
		String query = "select R_USER_MAIN.user_name ,R_USER_MAIN.zone_name,"
				+ "R_TOKN_MAIN.token_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where "
				+ "R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND "
				+ "R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = 'R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id";

		List<String> colNames = SpecificQueryAOImpl
				.parseColumnNamesFromQuery(query);
		Assert.assertFalse("no column names found", colNames.isEmpty());
		Assert.assertEquals("R_USER_MAIN.user_name", colNames.get(0));
		Assert.assertEquals("R_USER_MAIN.zone_name", colNames.get(1));
		Assert.assertEquals("R_TOKN_MAIN.token_name", colNames.get(2));

	}

	@Test
	public void parseColumnNamesFromQueryWithDistinct() throws Exception {
		String query = "select distinct R_USER_MAIN.user_name ,R_USER_MAIN.zone_name,"
				+ "R_TOKN_MAIN.token_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where "
				+ "R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND "
				+ "R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = 'R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id";

		List<String> colNames = SpecificQueryAOImpl
				.parseColumnNamesFromQuery(query);
		Assert.assertFalse("no column names found", colNames.isEmpty());
		Assert.assertEquals("R_USER_MAIN.user_name", colNames.get(0));
		Assert.assertEquals("R_USER_MAIN.zone_name", colNames.get(1));
		Assert.assertEquals("R_TOKN_MAIN.token_name", colNames.get(2));

	}

	@Test
	public void parseColumnNamesFromQueryWithCount() throws Exception {
		String query = "select count (R_USER_MAIN.user_name), R_USER_MAIN.zone_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where "
				+ "R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND "
				+ "R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = 'R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id";

		List<String> colNames = SpecificQueryAOImpl
				.parseColumnNamesFromQuery(query);
		Assert.assertFalse("no column names found", colNames.isEmpty());
		Assert.assertEquals("R_USER_MAIN.user_name", colNames.get(0));
		Assert.assertEquals("R_USER_MAIN.zone_name", colNames.get(1));

	}

	@Test
	public void parseColumnNamesFromQueryWithCountAndDistinct()
			throws Exception {
		String query = "select count (distinct R_USER_MAIN.zone_name), R_USER_MAIN.user_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where "
				+ "R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND "
				+ "R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = 'R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id";

		List<String> colNames = SpecificQueryAOImpl
				.parseColumnNamesFromQuery(query);
		Assert.assertFalse("no column names found", colNames.isEmpty());
		Assert.assertEquals("R_USER_MAIN.zone_name", colNames.get(0));
		Assert.assertEquals("R_USER_MAIN.user_name", colNames.get(1));

	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnNamesFromQueryNullQuery() throws Exception {
		String query = null;
		SpecificQueryAOImpl.parseColumnNamesFromQuery(query);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseColumnNamesFromQueryBlankQuery() throws Exception {
		String query = "";
		SpecificQueryAOImpl.parseColumnNamesFromQuery(query);
	}

	@Test
	public void lookUpShowCollAcls() throws Exception {
		String collAclQueryAlias = "ShowCollAcls";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);

		String userHome = MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(irodsAccount);
		List<String> arguments = new ArrayList<String>();
		arguments.add(userHome);

		List<SpecificQueryDefinition> actual = queryAO
				.listSpecificQueryByAliasLike(collAclQueryAlias);
		Assert.assertFalse(
				"did not get results from query, the showCollAcl specific query may not be registered, please run jargon-specquery.sh to provision standard jargon specific queries",
				actual.isEmpty());

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				collAclQueryAlias, arguments, 0, irodsAccount.getZone());
		SpecificQueryResultSet specificQueryResultSet = queryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						accessObjectFactory.getJargonProperties()
								.getMaxFilesAndDirsQueryMax());
		Assert.assertNotNull("null result set", specificQueryResultSet);
		Assert.assertFalse("no results returned, expected at least ls and lsl",
				specificQueryResultSet.getResults().isEmpty());

	}

	/**
	 * Bug [#1109] specific query no data found results in exception
	 *
	 * @throws Exception
	 */
	@Test
	public void lookUpShowCollAclsNoResultsExpected() throws Exception {
		String collAclQueryAlias = "ShowCollAcls";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);

		String userHome = "/a/non/existent/collection";
		List<String> arguments = new ArrayList<String>();
		arguments.add(userHome);
		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				collAclQueryAlias, arguments, 0, "");
		SpecificQueryResultSet specificQueryResultSet = queryAO
				.executeSpecificQueryUsingAlias(specificQuery,
						accessObjectFactory.getJargonProperties()
								.getMaxFilesAndDirsQueryMax());
		Assert.assertNotNull("null result set", specificQueryResultSet);
		Assert.assertTrue("expected no results", specificQueryResultSet
				.getResults().isEmpty());

	}

	@Test
	public void testFindSpecificQueryByAliasLike() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		List<SpecificQueryDefinition> actual = queryAO
				.listSpecificQueryByAliasLike("ShowCollAcls");
		Assert.assertFalse("did not get results from query", actual.isEmpty());
		for (SpecificQueryDefinition definition : actual) {
			Assert.assertFalse("no alias", definition.getAlias().isEmpty());
			Assert.assertFalse("no query", definition.getSql().isEmpty());
		}
	}

	@Test
	public void testFindSpecificQueryByAliasLikeWhenNoResults()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		queryAO.listSpecificQueryByAliasLike("thisaliasshouldntbeinirodsatallblahblahhennngh");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindSpecificQueryByAliasNullAlias() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		queryAO.listSpecificQueryByAliasLike(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindSpecificQueryByAliasBlankAlias() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		queryAO.listSpecificQueryByAliasLike("");

	}

	@Test
	public void testFindSpecificQueryByAliasWithZoneHint() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		SpecificQueryDefinition actual = queryAO.findSpecificQueryByAlias(
				"ShowCollAcls", irodsAccount.getZone());
		Assert.assertEquals("did not find correct query", "ShowCollAcls",
				actual.getAlias());

	}

	@Test
	public void testFindSpecificQueryByAlias() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		SpecificQueryDefinition actual = queryAO
				.findSpecificQueryByAlias("ShowCollAcls");
		Assert.assertEquals("did not find correct query", "ShowCollAcls",
				actual.getAlias());

	}

	@Test
	public void testListSpecificQueryByAliasLike() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		String queryAlias = "testListSpecificQueryByAliasLike";
		String queryAlias2 = "testListSpecificQueryByAliasLike2";
		String query = "select * from blah";

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				queryAlias, query);

		queryAO.removeSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);

		specificQuery = new SpecificQueryDefinition(queryAlias2, query);

		queryAO.removeSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);

		List<SpecificQueryDefinition> actual = queryAO
				.listSpecificQueryByAliasLike(queryAlias + "%",
						irodsAccount.getZone());
		Assert.assertFalse("empty results", actual.isEmpty());
		Assert.assertEquals("should have found the two entries", 2,
				actual.size());

	}

	@Test(expected = DataNotFoundException.class)
	public void testFindSpecificQueryByAliasNotFound() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		SpecificQueryAO queryAO = accessObjectFactory
				.getSpecificQueryAO(irodsAccount);
		queryAO.findSpecificQueryByAlias("ShowCollAclsButThisNameIsNotFoundItsNot");
	}

	/**
	 * Bug [#1373] alias lookup failing on specific query. This works because no
	 * special chars in query
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindSpecificQueryByAliasBug1373SmallQueryWithACrVersionWithout()
			throws Exception {

		String queryAlias = "VersionWithout";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		// stringify the test query

		String testQuery = "select * from btable";

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				queryAlias, testQuery);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQueryByAlias(queryAlias);
		queryAO.addSpecificQuery(specificQuery);

		SpecificQueryDefinition actual = queryAO
				.findSpecificQueryByAlias(queryAlias);
		Assert.assertEquals("did not find correct query", queryAlias,
				actual.getAlias());

	}

	/**
	 * Bug [#1373] alias lookup failing on specific query
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindSpecificQueryByAliasBug1373SmallQueryWithACr()
			throws Exception {

		String queryAlias = "testFindSpecificQueryByAliasBug1373SmallQuery";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		// stringify the test query

		String testQuery = "select * \n from atable";

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				queryAlias, testQuery);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQueryByAlias(queryAlias);
		queryAO.addSpecificQuery(specificQuery);

		SpecificQueryDefinition actual = queryAO
				.findSpecificQueryByAlias(queryAlias);
		Assert.assertEquals("did not find correct query", queryAlias,
				actual.getAlias());

	}

	/**
	 * Bug [#1373] alias lookup failing on specific query
	 *
	 * @throws Exception
	 */
	@Test
	public void testFindSpecificQueryByAliasBug1373() throws Exception {

		String queryAlias = "testFindSpecificQueryByAliasBug1373";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			return;
		}

		// stringify the test query

		String testQuery = LocalFileUtils
				.getClasspathResourceFileAsString("/specific-query/define-query-bug-1373.txt");

		SpecificQueryDefinition specificQuery = new SpecificQueryDefinition(
				queryAlias, testQuery);

		SpecificQueryAO queryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);
		queryAO.removeSpecificQuery(specificQuery);
		queryAO.addSpecificQuery(specificQuery);

		SpecificQueryDefinition actual = queryAO
				.findSpecificQueryByAlias(queryAlias);
		Assert.assertEquals("did not find correct query", queryAlias,
				actual.getAlias());

	}

}
