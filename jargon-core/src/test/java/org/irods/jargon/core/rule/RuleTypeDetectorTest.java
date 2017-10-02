package org.irods.jargon.core.rule;

import org.junit.Test;

import junit.framework.Assert;

public class RuleTypeDetectorTest {

	@Test
	public void testDetectTypeFromExtensionIrods() {
		String testFileName = "/a/path/to/core.r";
		RuleTypeDetector detector = new RuleTypeDetector();
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.IRODS, actual);
	}

	@Test
	public void testDetectTypeFromExtensionPython() {
		String testFileName = "/a/path/to/core.py";
		RuleTypeDetector detector = new RuleTypeDetector();
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.PYTHON, actual);
	}

	@Test
	public void testDetectTypeFromExtensionUnknown() {
		String testFileName = "/a/path/to/core.wtf";
		RuleTypeDetector detector = new RuleTypeDetector();
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.UNKNOWN, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetectTypeFromExtensionNull() {
		RuleTypeDetector detector = new RuleTypeDetector();
		detector.detectTypeFromExtension(null);
	}

	@Test
	public void testDetectTypeFromTextWithIrodsAnnotation() throws Exception {
		RuleTypeDetector detector = new RuleTypeDetector();
		String ruleText = "# @RuleEngine=\"IRODS\"";
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromRuleText(ruleText);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.IRODS, actual);
	}

	@Test
	public void testDetectTypeFromTextWithPythonAnnotation() throws Exception {
		RuleTypeDetector detector = new RuleTypeDetector();
		String ruleText = "# @RuleEngine=\"PYTHON\"";
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromRuleText(ruleText);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.PYTHON, actual);
	}

	@Test
	public void testDetectTypeFromTextWithIrodsSpacey() throws Exception {
		RuleTypeDetector detector = new RuleTypeDetector();
		String ruleText = "# @RuleEngine=\"   IRODS   \"     ";
		IrodsRuleEngineTypeEnum actual = detector.detectTypeFromRuleText(ruleText);
		Assert.assertEquals(IrodsRuleEngineTypeEnum.IRODS, actual);
	}

}
