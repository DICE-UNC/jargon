package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleTranslator;
import org.junit.Test;

public class ExecMyRuleInp_PITest {

	@Test
	public void testInstanceWithRemoteAttributes() {
		// TODO: test in Jargon 2.3.1 and then here
	}

	@Test
	public void testInstance() throws Exception {
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\nnull\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		ExecMyRuleInp rulePI = ExecMyRuleInp.instance(irodsRule);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", rulePI);
		Assert.assertEquals("api number not set", ExecMyRuleInp.RULE_API_NBR,
				rulePI.getApiNumber());

	}

	@Test
	public void testGetParsedTags() throws Exception {
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\nnull\n ruleExecOut";
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		ExecMyRuleInp rulePI = ExecMyRuleInp.instance(irodsRule);
		String outputXML = rulePI.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecMyRuleInp_PI><myRule>List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, &quot;: &quot;)|nop</myRule>\n");
		sb.append("<RHostAddr_PI><hostAddr></hostAddr>\n");
		sb.append("<rodsZone></rodsZone>\n");
		sb.append("<port>0</port>\n");
		sb.append("<dummyInt>0</dummyInt>\n");
		sb.append("</RHostAddr_PI>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("<outParamDesc>ruleExecOut</outParamDesc>\n");
		sb.append("<MsParamArray_PI><paramLen>1</paramLen>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<MsParam_PI><label></label>\n");
		sb.append("<type>NULL_PI</type>\n");
		sb.append("<STR_PI><myStr></myStr>\n");
		sb.append("</STR_PI>\n");
		sb.append("</MsParam_PI>\n");
		sb.append("</MsParamArray_PI>\n");
		sb.append("</ExecMyRuleInp_PI>\n");
		String expectedXML = sb.toString();

		// get rid of unique variable name which messes up the equals
		int i = outputXML.indexOf("<label>") + 7;
		int j = outputXML.indexOf("</label>");

		StringBuilder newActual = new StringBuilder();
		newActual.append(outputXML.substring(0, i));
		newActual.append(outputXML.substring(j));
		String newActualString = newActual.toString();
		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				newActualString);
	}

	@Test
	public void testGetParsedTagsWithInput() throws Exception {

		String testFileName = "/this/is/only/a/test";
		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append(testFileName);
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		ExecMyRuleInp rulePI = ExecMyRuleInp.instance(irodsRule);
		String outputXML = rulePI.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecMyRuleInp_PI><myRule>myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop</myRule>\n");
		sb.append("<RHostAddr_PI><hostAddr></hostAddr>\n");
		sb.append("<rodsZone></rodsZone>\n");
		sb.append("<port>0</port>\n");
		sb.append("<dummyInt>0</dummyInt>\n");
		sb.append("</RHostAddr_PI>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("<outParamDesc>*Action%*Condition%*Operation%*C%ruleExecOut</outParamDesc>\n");
		sb.append("<MsParamArray_PI><paramLen>3</paramLen>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<MsParam_PI><label>*Action</label>\n");
		sb.append("<type>STR_PI</type>\n");
		sb.append("<STR_PI><myStr>chksumRescLoc</myStr>\n");
		sb.append("</STR_PI>\n");
		sb.append("</MsParam_PI>\n");
		sb.append("<MsParam_PI><label>*Condition</label>\n");
		sb.append("<type>STR_PI</type>\n");
		sb.append("<STR_PI><myStr>COLL_NAME = '/this/is/only/a/test'</myStr>\n");
		sb.append("</STR_PI>\n");
		sb.append("</MsParam_PI>\n");
		sb.append("<MsParam_PI><label>*Operation</label>\n");
		sb.append("<type>STR_PI</type>\n");
		sb.append("<STR_PI><myStr>ChksumAll</myStr>\n");
		sb.append("</STR_PI>\n");
		sb.append("</MsParam_PI>\n");
		sb.append("</MsParamArray_PI>\n");
		sb.append("</ExecMyRuleInp_PI>\n");

		String expectedXML = sb.toString();
		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				outputXML);
	}

	@Test
	public void testGetParsedTagsWithConditionalInInput() throws Exception {

		StringBuilder builder = new StringBuilder();
		builder.append("testExecReturnArray||msiMakeGenQuery(\"RESC_NAME, RESC_LOC\",*Condition,*GenQInp)##msiExecGenQuery(*GenQInp, *GenQOut)|nop\n");
		builder.append("*Condition=RESC_NAME > 'a'\n");
		builder.append("*GenQOut");
		String ruleString = builder.toString();
		IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(ruleString);
		ExecMyRuleInp rulePI = ExecMyRuleInp.instance(irodsRule);
		String outputXML = rulePI.getParsedTags();

		StringBuilder sb = new StringBuilder();

		sb.append("<ExecMyRuleInp_PI><myRule>testExecReturnArray||msiMakeGenQuery(&quot;RESC_NAME, RESC_LOC&quot;,*Condition,*GenQInp)##msiExecGenQuery(*GenQInp, *GenQOut)|nop</myRule>\n");
		sb.append("<RHostAddr_PI><hostAddr></hostAddr>\n");
		sb.append("<rodsZone></rodsZone>\n");
		sb.append("<port>0</port>\n");
		sb.append("<dummyInt>0</dummyInt>\n");
		sb.append("</RHostAddr_PI>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("<outParamDesc>*GenQOut</outParamDesc>\n");
		sb.append("<MsParamArray_PI><paramLen>1</paramLen>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<MsParam_PI><label>*Condition</label>\n");
		sb.append("<type>STR_PI</type>\n");
		sb.append("<STR_PI><myStr>RESC_NAME &gt; 'a'</myStr>\n");
		sb.append("</STR_PI>\n");
		sb.append("</MsParam_PI>\n");
		sb.append("</MsParamArray_PI>\n");
		sb.append("</ExecMyRuleInp_PI>\n");

		String expectedXML = sb.toString();
		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				outputXML);
	}

	// 331 [main] INFO edu.sdsc.grid.io.irods.IRODSConnection createHeader 574-
	// functionID: 625

	// 284 [main] INFO edu.sdsc.grid.io.irods.IRODSConnection createHeader 574-
	// functionID: 625

}
