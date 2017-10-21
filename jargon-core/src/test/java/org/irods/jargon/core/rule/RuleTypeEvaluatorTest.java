package org.irods.jargon.core.rule;

import org.irods.jargon.core.utils.LocalFileUtils;
import org.junit.Test;

import junit.framework.Assert;

public class RuleTypeEvaluatorTest {

	@Test(expected = UnknownRuleTypeException.class)
	public void testNoMatchText() throws Exception {
		String ruleText = "myTestRule \n hello; \n }";
		RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
		ruleTypeEvaluator.guessRuleLanguageType(ruleText);
	}

	@Test
	public void testIrodsText() throws Exception {
		String ruleText = "myTestRule { \n hello; \n }";
		RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
		IrodsRuleInvocationTypeEnum actual = ruleTypeEvaluator.guessRuleLanguageType(ruleText);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS, actual);
	}

	@Test
	public void testPythonText() throws Exception {
		String ruleText = "   def hello :";
		RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
		IrodsRuleInvocationTypeEnum actual = ruleTypeEvaluator.guessRuleLanguageType(ruleText);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON, actual);
	}

	@Test
	public void testDetectTypeFromExtensionIrods() {
		String testFileName = "/a/path/to/core.r";
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS, actual);
	}

	@Test
	public void testDetectTypeFromExtensionPython() {
		String testFileName = "/a/path/to/core.py";
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON, actual);
	}

	@Test
	public void testDetectTypeFromExtensionUnknown() {
		String testFileName = "/a/path/to/core.wtf";
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromExtension(testFileName);
		Assert.assertNull(actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetectTypeFromExtensionNull() {
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		detector.detectTypeFromExtension(null);
	}

	@Test
	public void testDetectTypeFromTextWithIrodsAnnotation() throws Exception {
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		String ruleText = "# @RuleEngine=\"IRODS\"";
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromRuleTextAnnotation(ruleText);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS, actual);
	}

	@Test
	public void testDetectTypeFromTextWithPythonAnnotation() throws Exception {
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		String ruleText = "# @RuleEngine=\"PYTHON\"";
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromRuleTextAnnotation(ruleText);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON, actual);
	}

	@Test
	public void testDetectTypeFromTextWithIrodsSpacey() throws Exception {
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		String ruleText = "# @RuleEngine=\"   IRODS   \"     ";
		IrodsRuleInvocationTypeEnum actual = detector.detectTypeFromRuleTextAnnotation(ruleText);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS, actual);
	}

	@Test
	public void testDetectIrodsNewFormatWithExternal() throws Exception {
		RuleTypeEvaluator detector = new RuleTypeEvaluator();
		String ruleString = LocalFileUtils.getClasspathResourceFileAsString("/rules/rulemsiGetIcatTimeWithExternal.r");
		IrodsRuleInvocationTypeEnum actual = detector.guessRuleLanguageType(ruleString);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS, actual);
	}

}
