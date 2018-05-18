/**
 *
 */
package org.irods.jargon.core.rule;

import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents an iRule in the iRODS system, representing the various parts of a
 * rule, such as parameters and outputs, as an object model.
 *
 * @author Mike Conway - DICE (www.irods.org) TODO: document
 *
 */
public final class IRODSRule {
	private final String ruleAsOriginalText;
	private final List<IRODSRuleParameter> irodsRuleInputParameters;
	private final List<IRODSRuleParameter> irodsRuleOutputParameters;
	private final String ruleBody;
	private final RuleInvocationConfiguration ruleInvocationConfiguration;

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("IRODSRule [");
		if (ruleAsOriginalText != null) {
			builder.append("ruleAsOriginalText=").append(ruleAsOriginalText).append(", ");
		}
		if (irodsRuleInputParameters != null) {
			builder.append("irodsRuleInputParameters=")
					.append(irodsRuleInputParameters.subList(0, Math.min(irodsRuleInputParameters.size(), maxLen)))
					.append(", ");
		}
		if (irodsRuleOutputParameters != null) {
			builder.append("irodsRuleOutputParameters=")
					.append(irodsRuleOutputParameters.subList(0, Math.min(irodsRuleOutputParameters.size(), maxLen)))
					.append(", ");
		}
		if (ruleBody != null) {
			builder.append("ruleBody=").append(ruleBody).append(", ");
		}
		if (ruleInvocationConfiguration != null) {
			builder.append("ruleInvocationConfiguration=").append(ruleInvocationConfiguration);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Return an immutable instance of an {@code IRODSRule}
	 *
	 * @param ruleAsOriginalText
	 *            {@code String} with the plain text version of the rule
	 * @param irodsRuleInputParameters
	 *            {@code List<IRODSRuleParameter>} containing the translated rule
	 *            input parameters
	 * @param irodsRuleOutputParameters
	 *            {@code List<IRODSRuleParameter>} containing the translated rule
	 *            output parameters
	 * @param ruleBody
	 *            {@code String} containing the body of the rule
	 * @param ruleInvocationConfiguration
	 *            {@link RuleInvocationConfiguration} with information about the
	 *            type of rule (rule language) involved
	 * @return {@code IRODSRule} containing an object model of the rule.
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static IRODSRule instance(final String ruleAsOriginalText,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final List<IRODSRuleParameter> irodsRuleOutputParameters, final String ruleBody,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonException {
		return new IRODSRule(ruleAsOriginalText, irodsRuleInputParameters, irodsRuleOutputParameters, ruleBody,
				ruleInvocationConfiguration);
	}

	private IRODSRule(final String ruleAsOriginalText, final List<IRODSRuleParameter> irodsRuleInputParameters,
			final List<IRODSRuleParameter> irodsRuleOutputParameters, final String ruleBody,
			RuleInvocationConfiguration ruleInvocationConfiguration) {
		if (ruleAsOriginalText == null || ruleAsOriginalText.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAsOriginalText");
		}

		if (irodsRuleInputParameters == null) {
			throw new IllegalArgumentException("null irodsRuleInputParameters");
		}

		if (irodsRuleOutputParameters == null) {
			throw new IllegalArgumentException("null irodsRuleOutputParameters");
		}

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleBody");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		this.ruleAsOriginalText = ruleAsOriginalText;
		this.irodsRuleInputParameters = Collections.unmodifiableList(irodsRuleInputParameters);
		this.irodsRuleOutputParameters = Collections.unmodifiableList(irodsRuleOutputParameters);
		this.ruleBody = ruleBody;
		this.ruleInvocationConfiguration = ruleInvocationConfiguration;

	}

	public String getRuleAsOriginalText() {
		return ruleAsOriginalText;
	}

	public List<IRODSRuleParameter> getIrodsRuleInputParameters() {
		return irodsRuleInputParameters;
	}

	public List<IRODSRuleParameter> getIrodsRuleOutputParameters() {
		return irodsRuleOutputParameters;
	}

	public String getRuleBody() {
		return ruleBody;
	}

	/**
	 * @return the ruleInvocationConfiguration
	 */
	public RuleInvocationConfiguration getRuleInvocationConfiguration() {
		return ruleInvocationConfiguration;
	}

}
