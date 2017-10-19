/**
 *
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.PythonRuleProcessingAOImplTest;
import org.irods.jargon.core.rule.IRODSRuleTest;
import org.irods.jargon.core.rule.IrodsRuleEngineRuleTranslatorTest;
import org.irods.jargon.core.rule.IrodsRuleFactoryTest;
import org.irods.jargon.core.rule.RuleEngineInstanceChooserTest;
import org.irods.jargon.core.rule.RuleParsingUtilsTest;
import org.irods.jargon.core.rule.RuleTypeEvaluatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSRuleTest.class, IrodsRuleEngineRuleTranslatorTest.class, RuleParsingUtilsTest.class,
		PythonRuleProcessingAOImplTest.class, IrodsRuleFactoryTest.class, RuleTypeEvaluatorTest.class,
		RuleEngineInstanceChooserTest.class })
public class RuleTests {

}
