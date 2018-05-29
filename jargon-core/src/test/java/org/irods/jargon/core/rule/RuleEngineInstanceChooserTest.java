package org.irods.jargon.core.rule;

import java.util.ArrayList;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.junit.Test;

import org.junit.Assert;

public class RuleEngineInstanceChooserTest {

	@Test
	public void testPythonRuleSetFromProps() throws Exception {
		String testInstance = "hithere";
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setDefaultPythonRuleEngineIdentifier(testInstance);
		IRODSServerProperties irodsServerProperties = IRODSServerProperties.instance(IcatEnabled.ICAT_ENABLED, 111111,
				"rods4.2.1", "d", "testZone");
		RuleEngineInstanceChooser ruleEngineInstanceChooser = new RuleEngineInstanceChooser(jargonProperties,
				irodsServerProperties);
		RuleInvocationConfiguration configuration = new RuleInvocationConfiguration();
		configuration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);
		configuration.setEncodeRuleEngineInstance(true);
		IRODSRule irodsRule = IRODSRule.instance("blah", new ArrayList<IRODSRuleParameter>(),
				new ArrayList<IRODSRuleParameter>(), "blah", configuration);
		ruleEngineInstanceChooser.decorateRuleInvocationConfugurationWithRuleEngineInstance(irodsRule);
		Assert.assertEquals(testInstance, configuration.getRuleEngineSpecifier());

	}

	@Test
	public void testIrodsRuleSetFromProps() throws Exception {
		String testInstance = "hithere";
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setDefaultIrodsRuleEngineIdentifier(testInstance);
		IRODSServerProperties irodsServerProperties = IRODSServerProperties.instance(IcatEnabled.ICAT_ENABLED, 111111,
				"rods4.2.1", "d", "testZone");
		RuleEngineInstanceChooser ruleEngineInstanceChooser = new RuleEngineInstanceChooser(jargonProperties,
				irodsServerProperties);
		RuleInvocationConfiguration configuration = new RuleInvocationConfiguration();
		configuration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.IRODS);
		configuration.setEncodeRuleEngineInstance(true);
		IRODSRule irodsRule = IRODSRule.instance("blah", new ArrayList<IRODSRuleParameter>(),
				new ArrayList<IRODSRuleParameter>(), "blah", configuration);
		ruleEngineInstanceChooser.decorateRuleInvocationConfugurationWithRuleEngineInstance(irodsRule);
		Assert.assertEquals(testInstance, configuration.getRuleEngineSpecifier());

	}

	@Test
	public void testPythonRuleDontSetFromProps() throws Exception {
		String testInstance = "hithere";
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setDefaultPythonRuleEngineIdentifier(testInstance);
		IRODSServerProperties irodsServerProperties = IRODSServerProperties.instance(IcatEnabled.ICAT_ENABLED, 111111,
				"rods4.2.1", "d", "testZone");
		RuleEngineInstanceChooser ruleEngineInstanceChooser = new RuleEngineInstanceChooser(jargonProperties,
				irodsServerProperties);
		RuleInvocationConfiguration configuration = new RuleInvocationConfiguration();
		configuration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);
		configuration.setEncodeRuleEngineInstance(false);
		IRODSRule irodsRule = IRODSRule.instance("blah", new ArrayList<IRODSRuleParameter>(),
				new ArrayList<IRODSRuleParameter>(), "blah", configuration);
		ruleEngineInstanceChooser.decorateRuleInvocationConfugurationWithRuleEngineInstance(irodsRule);
		Assert.assertEquals("", configuration.getRuleEngineSpecifier());

	}

}
