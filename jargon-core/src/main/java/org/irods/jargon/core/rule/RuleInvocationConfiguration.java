/**
 * 
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.connection.JargonProperties;
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
	/**
	 * {@link RuleProcessingAO.RuleProcessingType} enum value. Note that it should
	 * be set to {@code CLASSIC} for classic rules, and {@code EXTERNAL} or
	 * {@code INTERNAL} for new format rules. This applies to iRODS rules only
	 */
	private RuleProcessingType ruleProcessingType = RuleProcessingType.DEFAULT;

	/**
	 * This provides a direct hard-coded identifier for the target rule engine,
	 * matching the rule engine id in the iRODS server side configuration. If left
	 * blank, it uses the default names for the rule engine types as specified in
	 * jargon properties
	 */
	private String ruleEngineSpecifier = "";

	/**
	 * Flag to signal whether Jargon should alter the ruleEngineSpecifier instance
	 * name if it is blank. This will consult the jargon properties or other system
	 * information to determine the appropriate instance according to the rule
	 * language detected. Set to <code>true</code> to have Jargon guess the rule
	 * engine instance name.
	 * <p/>
	 * Note that you can always just set the <code>ruleEngineSpecifier</code>
	 * directly, and that is equivalent to setting the instance name via the irule
	 * command.
	 */
	private boolean encodeRuleEngineInstance = false;

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

	/**
	 * Create a complete clone of the configuration
	 * 
	 * @param ruleInvocationConfiguration
	 *            {@link RuleInvocationConfiguration} to copy
	 * @return copied {@link RuleInvocationConfiguration}
	 */
	public RuleInvocationConfiguration copyRuleInvocationConfiguration(
			final RuleInvocationConfiguration ruleInvocationConfiguration) {

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		RuleInvocationConfiguration copy = new RuleInvocationConfiguration();
		copy.setIrodsRuleInvocationTypeEnum(ruleInvocationConfiguration.getIrodsRuleInvocationTypeEnum());
		copy.setRuleEngineSpecifier(ruleInvocationConfiguration.getRuleEngineSpecifier());
		copy.setRuleProcessingType(ruleInvocationConfiguration.getRuleProcessingType());
		return copy;

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
			builder.append("ruleEngineSpecifier=").append(ruleEngineSpecifier).append(", ");
		}
		builder.append("encodeRuleEngineInstance=").append(encodeRuleEngineInstance).append("]");
		return builder.toString();
	}

	public RuleProcessingType getRuleProcessingType() {
		return ruleProcessingType;
	}

	public void setRuleProcessingType(RuleProcessingType ruleProcessingType) {
		this.ruleProcessingType = ruleProcessingType;
	}

	public boolean isEncodeRuleEngineInstance() {
		return encodeRuleEngineInstance;
	}

	public void setEncodeRuleEngineInstance(boolean encodeRuleEngineInstance) {
		this.encodeRuleEngineInstance = encodeRuleEngineInstance;
	}

	/**
	 * Create an instance based on any settings in jargon properties
	 * 
	 * @param jargonProperties
	 *            {@link JargonProperties} that are binding
	 * @return
	 */
	public static RuleInvocationConfiguration instanceWithDefaultAutoSettings(JargonProperties jargonProperties) {
		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}
		RuleInvocationConfiguration ruleEngineConfiguration = new RuleInvocationConfiguration();
		ruleEngineConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);
		ruleEngineConfiguration.setRuleProcessingType(RuleProcessingType.DEFAULT);
		ruleEngineConfiguration.setEncodeRuleEngineInstance(jargonProperties.isRulesSetDestinationWhenAuto());
		return ruleEngineConfiguration;
	}

}
