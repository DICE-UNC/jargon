/**
 * 
 */
package org.irods.jargon.core.rule;

/**
 * Specifies the information needed to run a user submitted rule on a particular
 * rule engine. This is useful when a user-submitted rule needs to be bound to a
 * particular rule engine, e.g. phython versus iRODS native rule language.
 * <p/>
 * The configuration may just ask Jargon to figure out where to run, setting an
 * automatic mode, it may select the default name for the iRODS or Python rule
 * engine based on the <code>JargonProperties</code>, or it can even directly
 * enter the name of a targeted rule engine.
 * 
 * @author Mike Conway
 *
 */
public class RuleInvocationConfiguration {

	private IrodsRuleInvocationTypeEnum irodsRuleInvocationTypeEnum = IrodsRuleInvocationTypeEnum.AUTO_DETECT;
	private String ruleEngineSpecifier = "";

	/**
	 * @return the irodsRuleInvocationTypeEnum
	 */
	public IrodsRuleInvocationTypeEnum getIrodsRuleInvocationTypeEnum() {
		return irodsRuleInvocationTypeEnum;
	}

	/**
	 * @param irodsRuleInvocationTypeEnum
	 *            the irodsRuleInvocationTypeEnum to set
	 */
	public void setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum irodsRuleInvocationTypeEnum) {
		this.irodsRuleInvocationTypeEnum = irodsRuleInvocationTypeEnum;
	}

	/**
	 * @return the ruleEngineSpecifier
	 */
	public String getRuleEngineSpecifier() {
		return ruleEngineSpecifier;
	}

	/**
	 * @param ruleEngineSpecifier
	 *            the ruleEngineSpecifier to set
	 */
	public void setRuleEngineSpecifier(String ruleEngineSpecifier) {
		this.ruleEngineSpecifier = ruleEngineSpecifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RuleInvocationConfiguration [irodsRuleInvocationTypeEnum=" + irodsRuleInvocationTypeEnum
				+ ", ruleEngineSpecifier=" + ruleEngineSpecifier + "]";
	}

}
