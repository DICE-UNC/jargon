package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.AbstractAliasedQuery;
import org.irods.jargon.core.query.SimpleQuery;
import org.junit.Assert;
import org.junit.Test;

public class SimpleQueryInpTest {

	@Test
	public void testGetTagValue() throws Exception {
		AbstractAliasedQuery sq = SimpleQuery.instanceWithOneArgument("query", "myargs", 0);
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(sq);
		String tagVal = simpleQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<simpleQueryInp_PI><sql>query</sql>\n");
		sb.append("<arg1>myargs</arg1>\n");
		sb.append("<arg2></arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<control>0</control>\n");
		sb.append("<form>2</form>\n");
		sb.append("<maxBufSize>1024</maxBufSize>\n");
		sb.append("</simpleQueryInp_PI>\n");

		Assert.assertEquals("did not get expected tag value", sb.toString(), tagVal);
	}

	@Test
	public void testGetTagValueFourParams() throws Exception {

		List<String> parms = new ArrayList<String>();
		parms.add("parm1");
		parms.add("parm2");
		parms.add("parm3");
		parms.add("parm4");
		AbstractAliasedQuery sq = SimpleQuery.instance("query", parms, 0);
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(sq);
		String tagVal = simpleQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<simpleQueryInp_PI><sql>query</sql>\n");
		sb.append("<arg1>parm1</arg1>\n");
		sb.append("<arg2>parm2</arg2>\n");
		sb.append("<arg3>parm3</arg3>\n");
		sb.append("<arg4>parm4</arg4>\n");
		sb.append("<control>0</control>\n");
		sb.append("<form>2</form>\n");
		sb.append("<maxBufSize>1024</maxBufSize>\n");
		sb.append("</simpleQueryInp_PI>\n");

		Assert.assertEquals("did not get expected tag value", sb.toString(), tagVal);
	}

	@Test
	public void testInstance() {
		AbstractAliasedQuery sq = SimpleQuery.instanceWithOneArgument("query", "", 0);
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(sq);
		Assert.assertNotNull("null simpleQueryInp returned", simpleQueryInp);
		Assert.assertEquals("did not correctly set api number", SimpleQueryInp.SIMPLE_QUERY_API_NBR,
				simpleQueryInp.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullQuery() {
		AbstractAliasedQuery sq = null;
		SimpleQueryInp.instance(sq);
	}

}
