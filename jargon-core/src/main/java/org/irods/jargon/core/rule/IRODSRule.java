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

	/**
	 * Return an immutable instance of an {@code IRODSRule}
	 *
	 * @param ruleAsOriginalText
	 *            {@code String} with the plain text version of the rule
	 * @param irodsRuleInputParameters
	 *            {@code List<IRODSRuleParameter>} containing the
	 *            translated rule input parameters
	 * @param irodsRuleOutputParameters
	 *            {@code List<IRODSRuleParameter>} containing the
	 *            translated rule output parameters
	 * @param ruleBody
	 *            {@code String} containing the body of the rule
	 * @return {@code IRODSRule} containing an object model of the rule.
	 * @throws JargonException
	 */
	public static IRODSRule instance(final String ruleAsOriginalText,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final List<IRODSRuleParameter> irodsRuleOutputParameters,
			final String ruleBody) throws JargonException {
		return new IRODSRule(ruleAsOriginalText, irodsRuleInputParameters,
				irodsRuleOutputParameters, ruleBody);
	}

	private IRODSRule(final String ruleAsOriginalText,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final List<IRODSRuleParameter> irodsRuleOutputParameters,
			final String ruleBody) throws JargonException {
		if (ruleAsOriginalText == null || ruleAsOriginalText.isEmpty()) {
			throw new JargonException("null or empty ruleAsOriginalText");
		}

		if (irodsRuleInputParameters == null) {
			throw new JargonException("null irodsRuleInputParameters");
		}

		if (irodsRuleOutputParameters == null) {
			throw new JargonException("null irodsRuleOutputParameters");
		}

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new JargonException("null or empty ruleBody");
		}

		this.ruleAsOriginalText = ruleAsOriginalText;
		this.irodsRuleInputParameters = Collections
				.unmodifiableList(irodsRuleInputParameters);
		this.irodsRuleOutputParameters = Collections
				.unmodifiableList(irodsRuleOutputParameters);
		this.ruleBody = ruleBody;

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

}
