package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.query.SimpleQuery;
import org.junit.Ignore;
import org.junit.Test;

public class SimpleQueryInpTest {

	@Ignore //TODO: implement when pi is fixed
	public void testGetTagValue() throws Exception {
		SimpleQuery sq = SimpleQuery.instance("query", "myargs");
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(sq);
		String tagVal = simpleQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		Assert.assertEquals("did not get expected tag value", sb.toString(),
				tagVal);
	}

	@Test
	public void testInstance() {
		SimpleQuery sq = SimpleQuery.instance("query", "");
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(sq);
		Assert.assertNotNull("null simpleQueryInp returned", simpleQueryInp);
		Assert.assertEquals("did not correctly set api number",
				SimpleQueryInp.SIMPLE_QUERY_API_NBR);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullQuery() {
		SimpleQuery sq = null;
		SimpleQueryInp.instance(sq);
	}

}
