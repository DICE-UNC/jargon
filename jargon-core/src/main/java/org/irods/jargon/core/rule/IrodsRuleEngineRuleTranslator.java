package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates an iRODS user defined rule in plain text form into an
 * {@link org.irods.jargon.core.rule.IRODSRule} for processing by IRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IrodsRuleEngineRuleTranslator extends AbstractRuleTranslator {

	Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Default constructor with required dependencies
	 * 
	 * @param irodsServerProperties
	 *            {@link IRODSServerProperties} of the connected grid
	 * @param ruleInvocationConfiguration
	 *            {@link RuleInvocationConfiguration} with configuration regarding
	 *            the type of rule and type of rule processing to be done
	 */
	public IrodsRuleEngineRuleTranslator(final IRODSServerProperties irodsServerProperties,
			final RuleInvocationConfiguration ruleInvocationConfiguration, final JargonProperties jargonProperties) {
		super(irodsServerProperties, ruleInvocationConfiguration, jargonProperties);
	}

	/**
	 * Given a string representing an iRODS rule (including the rule body, as well
	 * as input and output lines, produce a translated rule object to send to iRODS.
	 *
	 * @param ruleAsPlainText
	 *            {@code String} with the rule body and input and output parameters
	 * @return {@link IRODSRule}
	 * @throws JargonRuleException
	 * @throws JargonException
	 */
	@Override
	public IRODSRule translatePlainTextRuleIntoIrodsRule(final String ruleAsPlainText,
			final List<IRODSRuleParameter> overrideInputParameters) throws JargonRuleException, JargonException {

		if (ruleAsPlainText == null || ruleAsPlainText.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule text");
		}

		super.getRuleInvocationConfiguration().setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.IRODS);

		log.info("translating rule: {}", ruleAsPlainText);

		String trimmedRule = ruleAsPlainText.trim();

		boolean newFormatRule = isUsingNewRuleSyntax(trimmedRule);
		StringTokenizer tokens = new StringTokenizer(trimmedRule, "\n");
		List<String> tokenLines = new ArrayList<String>();

		while (tokens.hasMoreElements()) {
			String token = (String) tokens.nextElement();
			if (!token.trim().isEmpty()) {
				tokenLines.add(token);
			}
		}

		List<IRODSRuleParameter> inputParameters;
		List<IRODSRuleParameter> outputParameters;
		IRODSRule irodsRule;

		if (newFormatRule) {
			log.info("parsing in new format");
			RuleCharacteristics ruleCharacteristics = processRuleBodyNewFormat(tokenLines);

			if (ruleCharacteristics == null) {
				throw new JargonRuleException("unable to parse rule");
			}
			// process the rule attributes, as they exist

			if (ruleCharacteristics.getInputLineIndex() != -1) {
				inputParameters = processRuleInputAttributesLine(
						tokenLines.get(ruleCharacteristics.getInputLineIndex()));
			} else {
				inputParameters = new ArrayList<IRODSRuleParameter>();
			}

			if (overrideInputParameters != null) {
				log.info("will override parameters");
				inputParameters = collateOverridesIntoInputParameters(overrideInputParameters, inputParameters);
			}

			if (ruleCharacteristics.getInputLineIndex() != -1) {

				outputParameters = processRuleOutputAttributesLine(
						tokenLines.get(ruleCharacteristics.getOutputLineIndex()));
			} else {
				outputParameters = new ArrayList<IRODSRuleParameter>();
			}

			irodsRule = IRODSRule.instance(ruleAsPlainText, inputParameters, outputParameters,
					ruleCharacteristics.getRuleBody(), this.getRuleInvocationConfiguration());
		} else {
			log.info("parsing in old format");
			if (tokenLines.size() < 3) {
				log.error(
						"unable to find the required lines (rule body, input parameters, output parameters) in rule body:{}",
						trimmedRule);
				throw new JargonRuleException("Rule requires at least 3 lines for body, input, and output parameters");
			}

			// process the rule attributes, line above last
			inputParameters = processRuleInputAttributesLine(tokenLines.get(tokenLines.size() - 2));

			if (overrideInputParameters != null) {
				log.info("will override parameters");
				inputParameters = collateOverridesIntoInputParameters(overrideInputParameters, inputParameters);
			}

			outputParameters = processRuleOutputAttributesLine(tokenLines.get(tokenLines.size() - 1));

			irodsRule = IRODSRule.instance(ruleAsPlainText, inputParameters, outputParameters,
					processRuleBodyOldFormat(tokenLines), this.getRuleInvocationConfiguration());

		}

		return irodsRule;
	}

	/**
	 * @param tokens
	 * @return
	 * @throws OperationNotSupportedByThisServerException
	 */
	private RuleCharacteristics processRuleBodyNewFormat(final List<String> tokenLines)
			throws OperationNotSupportedByThisServerException {

		// work backward to find input and output lines

		if (tokenLines.size() == 0) {
			return null;
		}

		// input and output lines may be at the end

		int tokenInput = -1;
		int tokenOutput = -1;
		int lastRowOfRule = tokenLines.size();

		for (int i = tokenLines.size() - 1; i >= 0; i--) {
			if (tokenLines.get(i).toUpperCase().startsWith("OUTPUT")) {
				tokenOutput = i;
			} else if (tokenLines.get(i).toUpperCase().startsWith("INPUT")) {
				tokenInput = i;
			} else {
				lastRowOfRule = i;
				break;
			}
		}

		StringBuilder total = new StringBuilder();
		// if formatting error, such as only one line, below breaks
		for (int i = 0; i <= lastRowOfRule; i++) {

			total.append(tokenLines.get(i));
			total.append("\n");
		}

		/*
		 * if iRODS 3.0+, add the @external parameter to the rule body for new style
		 * rules
		 */

		if (getIrodsServerProperties().isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")
				&& isUsingNewRuleSyntax(total.toString())) {

			// ok
			log.debug("verified as new format");
		} else {
			log.error("new format rules not supported by iRODS version:{}", this.getIrodsServerProperties());
			throw new OperationNotSupportedByThisServerException("new rule format not supported in this irods version");
		}

		String parsedRule;
		if (this.getRuleInvocationConfiguration().getRuleProcessingType() == RuleProcessingType.INTERNAL) {
			log.debug("adding @internal to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@internal\n");
			bodyWithExtern.append(total);
			parsedRule = bodyWithExtern.toString();
		} else {
			log.debug("adding @external to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@external\n");
			bodyWithExtern.append(total);
			parsedRule = bodyWithExtern.toString();
		}

		// find the rule

		RuleCharacteristics ruleCharacteristics = new RuleCharacteristics();
		ruleCharacteristics.setInputLineIndex(tokenInput);
		ruleCharacteristics.setOutputLineIndex(tokenOutput);
		ruleCharacteristics.setLastLineOfBody(lastRowOfRule);
		ruleCharacteristics.setRuleBody(parsedRule);

		return ruleCharacteristics;
	}

	/**
	 * @param tokens
	 * @return
	 */
	private String processRuleBodyOldFormat(final List<String> tokenLines) {
		StringBuilder total = new StringBuilder();
		// if formatting error, such as only one line, below breaks
		int ctr = 0;
		for (String line : tokenLines) {

			if (ctr == tokenLines.size() - 2) {
				break;
			}

			total.append(line);
			total.append("\n");
			ctr++;
		}

		// find the rule
		return total.toString();
	}

	/**
	 * Convenience method that uses a simple heuristic to determine whether the
	 * given rule uses the 'new' rule syntax.
	 *
	 * @param ruleText
	 *            {@code String} with the rule body
	 * @return {@code boolean} that will be {@code true} if the rule is using the
	 *         new syntax
	 */
	private boolean isUsingNewRuleSyntax(final String ruleText) {
		if (ruleText == null || ruleText.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleText");
		}
		boolean isNew = false;

		if (ruleText.indexOf('|') == -1 && ruleText.indexOf('{') > -1) {
			isNew = true;
		}

		return isNew;

	}
}

class RuleCharacteristics {
	private String ruleBody = "";
	private int lastLineOfBody = -1;
	private int inputLineIndex = -1;
	private int outputLineIndex = -1;

	public String getRuleBody() {
		return ruleBody;
	}

	public void setRuleBody(final String ruleBody) {
		this.ruleBody = ruleBody;
	}

	public int getLastLineOfBody() {
		return lastLineOfBody;
	}

	public void setLastLineOfBody(final int lastLineOfBody) {
		this.lastLineOfBody = lastLineOfBody;
	}

	public int getInputLineIndex() {
		return inputLineIndex;
	}

	public void setInputLineIndex(final int inputLineIndex) {
		this.inputLineIndex = inputLineIndex;
	}

	public int getOutputLineIndex() {
		return outputLineIndex;
	}

	public void setOutputLineIndex(final int outputLineIndex) {
		this.outputLineIndex = outputLineIndex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RuleCharacteristics [");
		if (ruleBody != null) {
			builder.append("ruleBody=").append(ruleBody).append(", ");
		}
		builder.append("lastLineOfBody=").append(lastLineOfBody).append(", inputLineIndex=").append(inputLineIndex)
				.append(", outputLineIndex=").append(outputLineIndex).append("]");
		return builder.toString();
	}

}