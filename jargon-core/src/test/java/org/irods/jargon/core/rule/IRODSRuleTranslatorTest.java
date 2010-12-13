package org.irods.jargon.core.rule;

import junit.framework.TestCase;

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
	public final void testTranslatePlainTextRuleIntoIRODSRule()
			throws Exception {
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		TestCase.assertNotNull("null translated rule returned", translatedRule);
	}

	@Test
	public final void testTranslateOutputParamWhenJustRuleExecOut()
			throws Exception {
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		TestCase.assertEquals(
				"no output parms found, expected one for ruleExecOut", 1,
				translatedRule.getIrodsRuleOutputParameters().size());
		TestCase.assertEquals("no ruleExecOut parm discovered", "ruleExecOut",
				translatedRule.getIrodsRuleOutputParameters().get(0)
						.getUniqueName());
	}

	@Test
	public final void testTranslateMultipleOutputParameters() throws Exception {
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		TestCase
				.assertEquals(
						"translated parameters are not the same as the output parameter line",
						5, translatedRule.getIrodsRuleOutputParameters().size());
		TestCase.assertEquals("*Action", translatedRule
				.getIrodsRuleOutputParameters().get(0).getUniqueName());
		TestCase.assertEquals("*Condition", translatedRule
				.getIrodsRuleOutputParameters().get(1).getUniqueName());
		TestCase.assertEquals("*Operation", translatedRule
				.getIrodsRuleOutputParameters().get(2).getUniqueName());
		TestCase.assertEquals("*C", translatedRule
				.getIrodsRuleOutputParameters().get(3).getUniqueName());
		TestCase.assertEquals("ruleExecOut", translatedRule
				.getIrodsRuleOutputParameters().get(4).getUniqueName());
	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateMultipleOutputParametersMalformedTwoPercents()
			throws Exception {
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);
	}

	@Test
	public final void testTranslateInputParmsStringOfnullAsOnlyParm()
			throws Exception {
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("null\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule translatedRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		TestCase
				.assertEquals(
						"input parm set to string 'null' should result in one dummy input parms",
						1, translatedRule.getIrodsRuleInputParameters().size());
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
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);

	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateMultipleOutputParametersMalformedTwoSplats()
			throws Exception {
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append("/test/File/name.txt");
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%**Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(ruleString);
	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateNullRule() throws Exception {
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(null);
	}

	@Test(expected = JargonRuleException.class)
	public final void testTranslateBlankRule() throws Exception {
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule("");
	}

}
