package org.irods.jargon.core.rule;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

public class IRODSRuleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstance() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();
		IRODSRule irodsRule = IRODSRule.instance("x", new ArrayList<IRODSRuleParameter>(),
				new ArrayList<IRODSRuleParameter>(), "yyy", irodsRuleInvocationConfiguration);
		Assert.assertNotNull("no return from initializer", irodsRule);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullRuleOrigText() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance(null, new ArrayList<IRODSRuleParameter>(), new ArrayList<IRODSRuleParameter>(), "yyy",
				irodsRuleInvocationConfiguration);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankRuleOrigText() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance("", new ArrayList<IRODSRuleParameter>(), new ArrayList<IRODSRuleParameter>(), "yyy",
				irodsRuleInvocationConfiguration);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullInputParams() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance("xxxx", null, new ArrayList<IRODSRuleParameter>(), "yyy", irodsRuleInvocationConfiguration);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullOutputParams() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance("xxxx", new ArrayList<IRODSRuleParameter>(), null, "yyy", irodsRuleInvocationConfiguration);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullRuleBody() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance("xxxx", new ArrayList<IRODSRuleParameter>(), new ArrayList<IRODSRuleParameter>(), null,
				irodsRuleInvocationConfiguration);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankRuleBody() throws Exception {
		RuleInvocationConfiguration irodsRuleInvocationConfiguration = new RuleInvocationConfiguration();

		IRODSRule.instance("xxxx", new ArrayList<IRODSRuleParameter>(), new ArrayList<IRODSRuleParameter>(), "",
				irodsRuleInvocationConfiguration);
	}

}
