package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.pub.domain.SpecificQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralAdminInpForSQTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testAddSpecificQuery() throws Exception {
		String query = "select count(data_id) from r_data_main";
		String alias = "get_dataobject_ids";

		SpecificQuery specificQuery = new SpecificQuery(query, alias);
		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForAddSpecificQuery(specificQuery);

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testAddSpecificQueryCheckXML() throws Exception {
		String query = "select count(data_id) from r_data_main";
		String alias = "get_dataobject_ids";

		SpecificQuery specificQuery = new SpecificQuery(query, alias);
		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForAddSpecificQuery(specificQuery);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>add</arg0>\n");
		sb.append("<arg1>specificQuery</arg1>\n");
		sb.append("<arg2>");
		sb.append(query);
		sb.append("</arg2>\n");
		sb.append("<arg3>");
		sb.append(alias);
		sb.append("</arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddSpecificQueryNullSQ() throws Exception {
		GeneralAdminInpForSQ.instanceForAddSpecificQuery(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddSpecificQueryNullQuery() throws Exception {
		SpecificQuery specificQuery = new SpecificQuery(null, "neato_query");
		GeneralAdminInpForSQ.instanceForAddSpecificQuery(specificQuery);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddSpecificQueryNullAlias() throws Exception {
		String query = "select count(data_id) from r_data_main";
		SpecificQuery specificQuery = new SpecificQuery(query, null);
		GeneralAdminInpForSQ.instanceForAddSpecificQuery(specificQuery);
	}
	
	@Test
	public void testRemoveSpecificQuery() throws Exception {
		String query = "select count(data_id) from r_data_main";
		String alias = "get_dataobject_ids";

		SpecificQuery specificQuery = new SpecificQuery(query, alias);
		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(specificQuery);

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testRemoveSpecificQueryCheckXML() throws Exception {
		String query = "select count(data_id) from r_data_main";
		String alias = "get_dataobject_ids";

		SpecificQuery specificQuery = new SpecificQuery(query, alias);
		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(specificQuery);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>rm</arg0>\n");
		sb.append("<arg1>specificQuery</arg1>\n");
		sb.append("<arg2>");
		sb.append(alias);
		sb.append("</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveSpecificQueryNullSQ() throws Exception {
		GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveSpecificQueryNullAlias() throws Exception {
		String query = "select count(data_id) from r_data_main";

		SpecificQuery specificQuery = new SpecificQuery(query, null);
		GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(specificQuery);
	}
	
	@Test
	public void testRemoveSpecificQueryNullQuery() throws Exception {
		String alias = "get_dataobject_ids";

		SpecificQuery specificQuery = new SpecificQuery(null, alias);
		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(specificQuery);

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testRemoveSpecificQueryByAlias() throws Exception {
		String alias = "get_dataobject_ids";

		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQueryByAlias(alias);

		Assert.assertNotNull(pi);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveSpecificQueryByAliasNullAlias() throws Exception {
		GeneralAdminInpForSQ.instanceForRemoveSpecificQueryByAlias(null);
	}
	
	@Test
	public void testRemoveSpecificQueryByAliasCheckXML() throws Exception {
		String alias = "get_dataobject_ids";

		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQueryByAlias(alias);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>rm</arg0>\n");
		sb.append("<arg1>specificQuery</arg1>\n");
		sb.append("<arg2>");
		sb.append(alias);
		sb.append("</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testRemoveAllSpecificQueryBySQL() throws Exception {
		String query = "select count(data_id) from r_data_main";

		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveSpecificQueryByAlias(query);

		Assert.assertNotNull(pi);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveAllSpecificQueryBySQLNullSQL() throws Exception {
	GeneralAdminInpForSQ.instanceForRemoveAllSpecificQueryBySQL(null);
	}
	
	@Test
	public void testRemoveAllSpecificQueryBySQLCheckXML() throws Exception {
		String query = "select count(data_id) from r_data_main";

		GeneralAdminInpForSQ pi = GeneralAdminInpForSQ.instanceForRemoveAllSpecificQueryBySQL(query);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<generalAdminInp_PI><arg0>rm</arg0>\n");
		sb.append("<arg1>specificQuery</arg1>\n");
		sb.append("<arg2>");
		sb.append(query);
		sb.append("</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("<arg7></arg7>\n");
		sb.append("<arg8></arg8>\n");
		sb.append("<arg9></arg9>\n");
		sb.append("</generalAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
}
