package org.irods.jargon.core.rule;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuleParsingUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testParseInputParameterForNameAndValue() {
		String parameter = "*name=value";
		RuleInputParameter rip = RuleParsingUtils
				.parseInputParameterForNameAndValue(parameter);
		Assert.assertEquals("no name given", "*name", rip.getParamName());
		Assert.assertEquals("no value given", "value", rip.getParamValue());
	}

	@Test
	public final void testParseInputParameterForNameAndBlankValue() {
		String parameter = "*name=";
		RuleInputParameter rip = RuleParsingUtils
				.parseInputParameterForNameAndValue(parameter);
		Assert.assertEquals("no name given", "*name", rip.getParamName());
		Assert.assertEquals("no value given", "", rip.getParamValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testParseInputParameterForNameAndNoEquals() {
		String parameter = "*name";
		RuleParsingUtils.parseInputParameterForNameAndValue(parameter);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testParseInputParameterForNameAndValueBlank() {
		String parameter = "";
		RuleParsingUtils.parseInputParameterForNameAndValue(parameter);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testParseInputParameterForNameAndValueNull() {
		String parameter = null;
		RuleParsingUtils.parseInputParameterForNameAndValue(parameter);
	}

}
