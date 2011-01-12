package org.irods.jargon.core.query;

import org.junit.Test;

import junit.framework.TestCase;


public class SimpleQueryTest {
	
	@Test
	public void testInstanceValid() throws Exception {
		String query = "query";
		String args = "args";
		SimpleQuery sq = SimpleQuery.instance(query, args);
		TestCase.assertEquals("invalid query value",query, sq.getQueryString());
		TestCase.assertEquals("invalid args", args, sq.getArguments());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInstanceNullQuery() throws Exception {
		String query = null;
		String args = "args";
		SimpleQuery.instance(query, args);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInstanceNullArgs() throws Exception {
		String query = "query";
		String args = null;
		SimpleQuery.instance(query, args);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInstanceBlankQuery() throws Exception {
		String query = "";
		String args = "";
		SimpleQuery.instance(query, args);
	}


}
