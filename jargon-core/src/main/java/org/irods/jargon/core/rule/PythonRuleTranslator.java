/**
 * 
 */
package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rule translator for Python rules
 * 
 * @author conwaymc
 *
 */
public class PythonRuleTranslator extends AbstractRuleTranslator {

	Logger log = LoggerFactory.getLogger(this.getClass());

	public PythonRuleTranslator(IRODSServerProperties irodsServerProperties,
			RuleInvocationConfiguration ruleInvocationConfiguration, JargonProperties jargonProperties) {
		super(irodsServerProperties, ruleInvocationConfiguration, jargonProperties);
	}

	@Override
	public IRODSRule translatePlainTextRuleIntoIrodsRule(String ruleAsPlainText,
			List<IRODSRuleParameter> overrideInputParameters) throws JargonRuleException, JargonException {

		if (ruleAsPlainText == null || ruleAsPlainText.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule text");
		}

		log.info("translating rule: {}", ruleAsPlainText);

		String trimmedRule = ruleAsPlainText.trim();

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

		RuleCharacteristics ruleCharacteristics = processRuleBodyNewFormat(tokenLines);

		if (ruleCharacteristics == null) {
			throw new JargonRuleException("unable to parse rule");
		}
		// process the rule attributes, as they exist

		if (ruleCharacteristics.getInputLineIndex() != -1) {
			inputParameters = processRuleInputAttributesLine(tokenLines.get(ruleCharacteristics.getInputLineIndex()));
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

		return IRODSRule.instance(ruleAsPlainText, inputParameters, outputParameters, ruleCharacteristics.getRuleBody(),
				this.getRuleInvocationConfiguration());
	}

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

		RuleCharacteristics ruleCharacteristics = new RuleCharacteristics();
		ruleCharacteristics.setInputLineIndex(tokenInput);
		ruleCharacteristics.setOutputLineIndex(tokenOutput);
		ruleCharacteristics.setLastLineOfBody(lastRowOfRule);
		ruleCharacteristics.setRuleBody(total.toString());

		return ruleCharacteristics;
	}

}
