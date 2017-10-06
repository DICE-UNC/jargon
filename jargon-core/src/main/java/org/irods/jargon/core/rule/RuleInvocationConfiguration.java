/**
 * 
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;

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
	private RuleProcessingType ruleProcessingType = RuleProcessingType.DEFAULT;
	private String ruleEngineSpecifier = "";

	/**
	 * Build a default instance that should work as a default with the method
	 * signatures of {@link RuleProcessingAO} before the introduction of this
	 * configuration object. This is a deprecated set of methods, and it is
	 * recommended that users change to signatures that
	 * 
	 * @return {@link RuleInvocationConfiguration}
	 */
	public static RuleInvocationConfiguration instanceWithDefaultAutoSettings() {
		RuleInvocationConfiguration ruleEngineConfiguration = new RuleInvocationConfiguration();
		ruleEngineConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);
		ruleEngineConfiguration.setRuleProcessingType(RuleProcessingType.DEFAULT);
		return ruleEngineConfiguration;
	}

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RuleInvocationConfiguration [");
		if (irodsRuleInvocationTypeEnum != null) {
			builder.append("irodsRuleInvocationTypeEnum=").append(irodsRuleInvocationTypeEnum).append(", ");
		}
		if (ruleProcessingType != null) {
			builder.append("ruleProcessingType=").append(ruleProcessingType).append(", ");
		}
		if (ruleEngineSpecifier != null) {
			builder.append("ruleEngineSpecifier=").append(ruleEngineSpecifier);
		}
		builder.append("]");
		return builder.toString();
	}

	public RuleProcessingType getRuleProcessingType() {
		return ruleProcessingType;
	}

	public void setRuleProcessingType(RuleProcessingType ruleProcessingType) {
		this.ruleProcessingType = ruleProcessingType;
	}

}
