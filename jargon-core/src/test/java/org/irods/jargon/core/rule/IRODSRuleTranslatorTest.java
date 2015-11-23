package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSRuleTranslatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testTranslatePlainTextRuleIntoIRODSRulePre3Point0()
			throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods2.5", "2", "zone");
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertNotNull("null translated rule returned", translatedRule);
	}

	@Test
	public final void testTranslatePlainTextRuleIntoIRODSRulePost3Point0()
			throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertNotNull("null translated rule returned", translatedRule);
	}

	@Test
	public final void testTranslateNewFormatRuleNullInput() throws Exception {

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		String ruleString = "HelloWorld { \n writeLine(\"stdout\", \"Hello, world!\");\n}\nINPUT null\nOUTPUT ruleExecOut\n";

		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertNotNull("null translated rule returned", translatedRule);
		Assert.assertEquals("should be no input params", 0, translatedRule
				.getIrodsRuleInputParameters().size());

	}

	@Test
	public final void testTranslateOutputParamWhenJustRuleExecOut()
			throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertEquals(
				"no output parms found, expected one for ruleExecOut", 1,
				translatedRule.getIrodsRuleOutputParameters().size());
		Assert.assertEquals("no ruleExecOut parm discovered", "ruleExecOut",
				translatedRule.getIrodsRuleOutputParameters().get(0)
				.getUniqueName());
	}

	@Test
	public final void testTranslateMultipleOutputParameters() throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
		.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertEquals(
				"translated parameters are not the same as the output parameter line",
				5, translatedRule.getIrodsRuleOutputParameters().size());
		Assert.assertEquals("*Action", translatedRule
				.getIrodsRuleOutputParameters().get(0).getUniqueName());
		Assert.assertEquals("*Condition", translatedRule
				.getIrodsRuleOutputParameters().get(1).getUniqueName());
		Assert.assertEquals("*Operation", translatedRule
				.getIrodsRuleOutputParameters().get(2).getUniqueName());
		Assert.assertEquals("*C", translatedRule.getIrodsRuleOutputParameters()
				.get(3).getUniqueName());
		Assert.assertEquals("ruleExecOut", translatedRule
				.getIrodsRuleOutputParameters().get(4).getUniqueName());
	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateMultipleOutputParametersMalformedTwoPercents()
			throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods2.5", "2", "zone");

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
		.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);
	}

	@Test
	public final void testTranslateInputParmsStringOfnullAsOnlyParm()
			throws Exception {

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods2.5", "2", "zone");

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
		.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("null\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertEquals(
				"input parm set to string 'null' should result in one dummy input parms",
				1, translatedRule.getIrodsRuleInputParameters().size());
	}

	/*
	 * feature [#500] enhanced 3.0 rule parsing and load rule from resource
	 */
	@Test
	public final void testTranslateMultiLineRuleNewFormat() throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("makeThumbnailFromObj {\n");
		sb.append("msiSplitPath(*objPath,*collName,*objName);\n");
		sb.append(" msiAddSelectFieldToGenQuery(\"DATA_PATH\", \"null\", *GenQInp);\n");
		sb.append("msiAddSelectFieldToGenQuery(\"RESC_LOC\", \"null\", *GenQInp);\n");
		sb.append(" msiAddConditionToGenQuery(\"COLL_NAME\", \"=\", *collName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_NAME\", \"=\", *objName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_RESC_NAME\",\"=\", *resource, *GenQInp);\n");
		sb.append("msiExecGenQuery(*GenQInp, *GenQOut);\n");
		sb.append("foreach (*GenQOut)\n{\n");
		sb.append(" msiGetValByKey(*GenQOut, \"DATA_PATH\", *data_path);\n");
		sb.append("msiGetValByKey(*GenQOut, \"RESC_LOC\", *resc_loc);\n}\n");
		sb.append("msiExecCmd(\"makeThumbnail.py\", '*data_path', *resc_loc, \"null\", \"null\", *CmdOut);\n");
		sb.append("msiGetStdoutInExecCmdOut(*CmdOut, *StdoutStr);\n");
		// sb.append(" writeLine(\"stdout\", *StdoutStr);\n");
		sb.append("INPUT *objPath=\"");
		sb.append("/a/irods/path");
		sb.append("\",*resource=\"");
		sb.append("resource");
		sb.append("\"\n");
		sb.append("OUTPUT *ruleExecOut");
		String ruleString = sb.toString();
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		Assert.assertEquals("did not parse the two input parameters", 2,
				translatedRule.getIrodsRuleInputParameters().size());
		Assert.assertEquals("did not parse the one output parameter", 1,
				translatedRule.getIrodsRuleOutputParameters().size());
	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateInputParmsStringOfnullTwiceAsOnlyParm()
			throws Exception {

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
		.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("null%null\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods2.5", "2", "zone");

		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);

	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateMultipleOutputParametersMalformedTwoSplats()
			throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
		.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%**Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testTranslateNullRule() throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testTranslateBlankRule() throws Exception {
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				irodsServerProperties);
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule("");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testNullIrodsServerProperties() throws Exception {
		new IRODSRuleTranslator(null);
	}

	@Test
	public final void testCollateOverrideInputParmsNoOverrides()
			throws Exception {
		String name1 = "name1";
		String name2 = "name2";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		List<IRODSRuleParameter> params = new ArrayList<IRODSRuleParameter>();
		List<IRODSRuleParameter> overrideParams = new ArrayList<IRODSRuleParameter>();
		params.add(new IRODSRuleParameter(name1, "val"));
		params.add(new IRODSRuleParameter(name2, "val"));
		IRODSRuleTranslator translator = new IRODSRuleTranslator(
				irodsServerProperties);
		List<IRODSRuleParameter> collated = translator
				.collateOverridesIntoInputParameters(overrideParams, params);
		Assert.assertEquals("no collated found", 2, collated.size());
		Assert.assertEquals("name1 not found", name1, collated.get(0)
				.getUniqueName());
		Assert.assertEquals("name2 not found", name2, collated.get(1)
				.getUniqueName());

	}

	@Test
	public final void testCollateOverrideInputParmsOneOverride()
			throws Exception {
		String name1 = "name1";
		String name2 = "name2";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		List<IRODSRuleParameter> params = new ArrayList<IRODSRuleParameter>();
		List<IRODSRuleParameter> overrideParams = new ArrayList<IRODSRuleParameter>();
		params.add(new IRODSRuleParameter(name1, "val"));
		params.add(new IRODSRuleParameter(name2, "val"));
		overrideParams.add(new IRODSRuleParameter(name2, "val2"));
		IRODSRuleTranslator translator = new IRODSRuleTranslator(
				irodsServerProperties);
		List<IRODSRuleParameter> collated = translator
				.collateOverridesIntoInputParameters(overrideParams, params);
		Assert.assertEquals("no collated found", 2, collated.size());
		Assert.assertEquals("name1 not found", name1, collated.get(0)
				.getUniqueName());
		Assert.assertEquals("name2 not found", name2, collated.get(1)
				.getUniqueName());
		Assert.assertEquals("name2 not overridden", "val2", collated.get(1)
				.getValue());

	}

	@Test
	public final void testCollateOverrideInputParmsOneOverrideOneAdd()
			throws Exception {
		String name1 = "name1";
		String name2 = "name2";
		String name3 = "name3";
		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		List<IRODSRuleParameter> params = new ArrayList<IRODSRuleParameter>();
		List<IRODSRuleParameter> overrideParams = new ArrayList<IRODSRuleParameter>();
		params.add(new IRODSRuleParameter(name1, "val"));
		params.add(new IRODSRuleParameter(name2, "val"));
		overrideParams.add(new IRODSRuleParameter(name2, "val2"));
		overrideParams.add(new IRODSRuleParameter(name3, "val2"));
		IRODSRuleTranslator translator = new IRODSRuleTranslator(
				irodsServerProperties);
		List<IRODSRuleParameter> collated = translator
				.collateOverridesIntoInputParameters(overrideParams, params);
		Assert.assertEquals("no collated found", 3, collated.size());
		Assert.assertEquals("name1 not found", name1, collated.get(0)
				.getUniqueName());
		Assert.assertEquals("name2 not found", name2, collated.get(1)
				.getUniqueName());
		Assert.assertEquals("name2 not found", name3, collated.get(2)
				.getUniqueName());
		Assert.assertEquals("name2 not overridden", "val2", collated.get(1)
				.getValue());

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testCollateOverrideInputParmsNuillOverrides()
			throws Exception {

		IRODSServerProperties irodsServerProperties = IRODSServerProperties
				.instance(IcatEnabled.ICAT_ENABLED, 1, "rods3.0", "2", "zone");

		List<IRODSRuleParameter> params = new ArrayList<IRODSRuleParameter>();
		List<IRODSRuleParameter> overrideParams = null;

		IRODSRuleTranslator translator = new IRODSRuleTranslator(
				irodsServerProperties);
		translator.collateOverridesIntoInputParameters(overrideParams, params);

	}

}
