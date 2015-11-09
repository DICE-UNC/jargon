/**
 *
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.rule.IRODSRuleTest;
import org.irods.jargon.core.rule.IRODSRuleTranslatorTest;
import org.irods.jargon.core.rule.RuleParsingUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSRuleTest.class, IRODSRuleTranslatorTest.class,
	RuleParsingUtilsTest.class })
public class RuleTests {

}
