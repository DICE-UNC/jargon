package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class SpecificQueryInpTest {

	@Test
	public void testGetTagValue() throws Exception {
		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(null,
				"query", 10, 0);
		String tagVal = specificQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<specificQueryInp_PI><sql>query</sql>\n");
		sb.append("<maxRows>10</maxRows>\n");
		sb.append("<continueInx>0</continueInx>\n");
		sb.append("<rowOffset>0</rowOffset>\n");
		sb.append("<options>0</options>\n");

		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");

		sb.append("</specificQueryInp_PI>\n");
		
		Assert.assertEquals("did not get expected tag value", sb.toString(),
				tagVal);
	}

	@Test
	public void testGetTagValueAutoClose() throws Exception {
		SpecificQueryInp specificQueryInp = SpecificQueryInp.instanceForClose();
		String tagVal = specificQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<specificQueryInp_PI><sql>close</sql>\n");
		sb.append("<maxRows>0</maxRows>\n");
		sb.append("<continueInx>0</continueInx>\n");
		sb.append("<rowOffset>0</rowOffset>\n");
		sb.append("<options>0</options>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</specificQueryInp_PI>\n");

		Assert.assertEquals("did not get expected tag value", sb.toString(),
				tagVal);
	}

	@Test
	public void testGetTagValue10Args() throws Exception {

		List<String> args = new ArrayList<String>();
		args.add("1");
		args.add("2");
		args.add("3");
		args.add("4");
		args.add("5");
		args.add("6");
		args.add("7");
		args.add("8");
		args.add("9");
		args.add("10");

		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(args,
				"query", 10, 0);
		String tagVal = specificQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<specificQueryInp_PI><sql>query</sql>\n");
		sb.append("<arg1>1</arg1>\n");
		sb.append("<arg2>2</arg2>\n");
		sb.append("<arg3>3</arg3>\n");
		sb.append("<arg4>4</arg4>\n");
		sb.append("<arg5>5</arg5>\n");
		sb.append("<arg6>6</arg6>\n");
		sb.append("<arg7>7</arg7>\n");
		sb.append("<arg8>8</arg8>\n");
		sb.append("<arg9>9</arg9>\n");
		sb.append("<arg10>10</arg10>\n");
		sb.append("<maxRows>10</maxRows>\n");
		sb.append("<continueInx>0</continueInx>\n");
		sb.append("<rowOffset>0</rowOffset>\n");
		sb.append("<options>0</options>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</specificQueryInp_PI>\n");

		Assert.assertEquals("did not get expected tag value", sb.toString(),
				tagVal);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTagValueNullQuery() throws Exception {
		SpecificQueryInp.instance(null, null, 10, 0);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTagValueBlankQuery() throws Exception {
		SpecificQueryInp.instance(null, "", 10, 0);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTagValue11Args() throws Exception {

		List<String> args = new ArrayList<String>();
		args.add("1");
		args.add("2");
		args.add("3");
		args.add("4");
		args.add("5");
		args.add("6");
		args.add("7");
		args.add("8");
		args.add("9");
		args.add("10");
		args.add("11");

		SpecificQueryInp.instance(args, "query", 10, 0);

	}

}




