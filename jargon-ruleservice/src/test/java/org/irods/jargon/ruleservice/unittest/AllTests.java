package org.irods.jargon.ruleservice.unittest;

import org.irods.jargon.ruleservice.composition.RuleCompositionServiceImplTest;
import org.irods.jargon.ruleservice.formatting.HtmlLogTableFormatterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RuleCompositionServiceImplTest.class, HtmlLogTableFormatterTest.class })
public class AllTests {

}
