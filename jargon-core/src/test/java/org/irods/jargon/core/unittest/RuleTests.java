/**
 *
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.rule.IRODSRuleTest;
import org.irods.jargon.core.rule.IRODSRuleTranslatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSRuleTest.class, IRODSRuleTranslatorTest.class })
public class RuleTests {

}
