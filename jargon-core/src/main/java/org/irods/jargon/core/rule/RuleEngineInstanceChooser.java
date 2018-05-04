/**
 * 
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to set the iRODS rule engine instance based on a provided rule as
 * well as server configuration information
 * 
 * @author conwaymc
 *
 */
public class RuleEngineInstanceChooser {

	private static final Logger log = LoggerFactory.getLogger(RuleEngineInstanceChooser.class);
	private final JargonProperties jargonProperties;
	private final IRODSServerProperties irodsServerProperties;

	/**
	 * Default constructor with required dependencies
	 * 
	 * @param jargonProperties
	 *            {@link JargonProperties} containing rule engine mappings
	 * @param irodsServerProperties
	 *            {@link IRODSServerProperties} with additional server attributes
	 */
	public RuleEngineInstanceChooser(final JargonProperties jargonProperties,
			final IRODSServerProperties irodsServerProperties) {
		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		if (irodsServerProperties == null) {
			throw new IllegalArgumentException("null irodsServerProperties");
		}

		this.jargonProperties = jargonProperties;
		this.irodsServerProperties = irodsServerProperties;

	}

	/**
	 * Inspect the <code>IRODSRule</code> and correctly indicate the iRODS rule
	 * engine instance that should be used to invoke the rule. Note that this
	 * function will respect the <code>encodeRuleEngineInstance</code> flag in the
	 * included {@link RuleInvocationConfiguration} and if <code>true</code> it will
	 * try and set it based on Jargon and irods configuration. Note that you can
	 * also just directly set the value in the <code>ruleEngineSpecifier</code> also
	 * in the <code>RuleInvocationSpecification</code>
	 * 
	 * 
	 * @param irodsRule
	 *            {@link IRODSRule} to set with the instance. Note that invoking
	 *            this method will alter the {@link RuleInvocationConfiguration} in
	 *            the <code>IRODSRule</code>.
	 */
	public void decorateRuleInvocationConfugurationWithRuleEngineInstance(final IRODSRule irodsRule)
			throws UnknownRuleTypeException {

		log.info("decorateRuleInvocationConfugurationWithRuleEngineInstance()");
		if (irodsRule == null) {
			throw new IllegalArgumentException("null irodsRule");
		}

		RuleInvocationConfiguration config = irodsRule.getRuleInvocationConfiguration();

		if (!config.getRuleEngineSpecifier().isEmpty()) {
			log.debug("using the already-entered rule engine instance:{}", config.getRuleEngineSpecifier());
			return;
		}

		if (!config.isEncodeRuleEngineInstance()) {
			log.debug("was set to not configure a blank instance..just leave it alone");
			return;
		}

		// I need to guess at the rule engine instance, use the jargon props (other data
		// may be available later, like introspection)

		switch (config.getIrodsRuleInvocationTypeEnum()) {
		case IRODS:
			config.setRuleEngineSpecifier(jargonProperties.getDefaultIrodsRuleEngineIdentifier());
			break;
		case PYTHON:
			config.setRuleEngineSpecifier(jargonProperties.getDefaultPythonRuleEngineIdentifier());
			break;
		default:
			log.error("cannot process the rule type:{}", config);
			throw new UnknownRuleTypeException("cannot process given rule type, cannot determine default");

		}

		log.info("config has been decorated with the rule engine instance:{}", config);

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RuleEngineInstanceChooser [");
		if (jargonProperties != null) {
			builder.append("jargonProperties=").append(jargonProperties).append(", ");
		}
		if (irodsServerProperties != null) {
			builder.append("irodsServerProperties=").append(irodsServerProperties);
		}
		builder.append("]");
		return builder.toString();
	}

}
