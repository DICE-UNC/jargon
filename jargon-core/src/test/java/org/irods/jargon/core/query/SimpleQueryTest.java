package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

public class SimpleQueryTest {

	@Test
	public void testInstanceOneArgValid() throws Exception {
		String query = "query";
		String args = "args";
		AbstractAliasedQuery sq = SimpleQuery.instanceWithOneArgument(query,
				args, 0);
		Assert.assertEquals("invalid query value", query, sq.getQueryString());
		Assert.assertEquals("invalid args", 1, sq.getArguments().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceWithOneArgumentNullQuery() throws Exception {
		String query = null;
		String args = "args";
		SimpleQuery.instanceWithOneArgument(query, args, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceWithOneArgumentNullArgs() throws Exception {
		String query = "query";
		String args = null;
		SimpleQuery.instanceWithOneArgument(query, args, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceWithOneArgumentBlankQuery() throws Exception {
		String query = "";
		String args = "";
		SimpleQuery.instanceWithOneArgument(query, args, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstaceTooManyParms() throws Exception {
		List<String> parms = new ArrayList<String>();
		parms.add("parm1");
		parms.add("parm2");
		parms.add("parm3");
		parms.add("parm4");
		parms.add("parm5");

		SimpleQuery.instance("query", parms, 0);
	}

	@Test
	public void testInstanceTwoArgsValid() throws Exception {
		String query = "query";
		String arg1 = "arg1";
		String arg2 = "arg2";
		AbstractAliasedQuery sq = SimpleQuery.instanceWithTwoArguments(query,
				arg1, arg2, 0);
		Assert.assertEquals("invalid query value", query, sq.getQueryString());
		Assert.assertEquals("invalid args", 2, sq.getArguments().size());
		Assert.assertEquals("did not set args correctly", arg1, sq
				.getArguments().get(0));
		Assert.assertEquals("did not set args correctly", arg2, sq
				.getArguments().get(1));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceTwoArgsSecondArgBlank() throws Exception {
		String query = "query";
		String arg1 = "arg1";
		String arg2 = "";
		SimpleQuery.instanceWithTwoArguments(query, arg1, arg2, 0);
	}

	@Test
	public void testInstanceNullArray() throws Exception {
		AbstractAliasedQuery sq = SimpleQuery.instance("xxx", null, 0);
		Assert.assertNotNull("should have initialized blank array",
				sq.getArguments());
	}

}
