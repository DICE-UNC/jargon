package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates an iRODS user defined rule in plain text form into an
 * {@link org.irods.jargon.core.rule.IRODSRule} for processing by IRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSRuleTranslator {

	private static final String SPLAT = "*";
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private final IRODSServerProperties irodsServerProperties;

	public IRODSRuleTranslator(final IRODSServerProperties irodsServerProperties) {
		if (irodsServerProperties == null) {
			throw new IllegalArgumentException("null irodsServerProperties");
		}
		this.irodsServerProperties = irodsServerProperties;
	}

	/**
	 * Given a string representing an iRODS rule (including the rule body, as
	 * well as input and output lines, produce a translated rule object to send
	 * to iRODS.
	 *
	 * @param ruleAsPlainText
	 *            <code>String</code> with the rule body and input and output
	 *            parameters
	 * @return {@link IRODSRule}
	 * @throws JargonRuleException
	 * @throws JargonException
	 */
	public IRODSRule translatePlainTextRuleIntoIRODSRule(
			final String ruleAsPlainText) throws JargonRuleException,
			JargonException {

		return translatePlainTextRuleIntoIRODSRule(ruleAsPlainText, null);
	}

	/**
	 * Given a string representing an iRODS rule (including the rule body, as
	 * well as input and output lines, produce a translated rule object to send
	 * to iRODS.
	 *
	 * @param ruleAsPlainText
	 *            <code>String</code> with the rule body and input and output
	 *            parameters
	 * @return {@link IRODSRule}
	 * @throws JargonRuleException
	 * @throws JargonException
	 */
	public IRODSRule translatePlainTextRuleIntoIRODSRule(
			final String ruleAsPlainText,
			final List<IRODSRuleParameter> overrideInputParameters)
			throws JargonRuleException, JargonException {

		if (ruleAsPlainText == null || ruleAsPlainText.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule text");
		}

		log.info("translating rule: {}", ruleAsPlainText);

		String trimmedRule = ruleAsPlainText.trim();

		boolean newFormatRule = IRODSRuleTranslator
				.isUsingNewRuleSyntax(trimmedRule);
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
				inputParameters = processRuleInputAttributesLine(tokenLines
						.get(ruleCharacteristics.getInputLineIndex()));
			} else {
				inputParameters = new ArrayList<IRODSRuleParameter>();
			}

			if (overrideInputParameters != null) {
				log.info("will override parameters");
				inputParameters = collateOverridesIntoInputParameters(
						overrideInputParameters, inputParameters);
			}

			if (ruleCharacteristics.getInputLineIndex() != -1) {

				outputParameters = processRuleOutputAttributesLine(tokenLines
						.get(ruleCharacteristics.getOutputLineIndex()));
			} else {
				outputParameters = new ArrayList<IRODSRuleParameter>();
			}

			irodsRule = IRODSRule.instance(ruleAsPlainText, inputParameters,
					outputParameters, ruleCharacteristics.getRuleBody());
		} else {
			log.info("parsing in old format");
			if (tokenLines.size() < 3) {
				log.error(
						"unable to find the required lines (rule body, input parameters, output parameters) in rule body:{}",
						trimmedRule);
				throw new JargonRuleException(
						"Rule requires at least 3 lines for body, input, and output parameters");
			}

			// process the rule attributes, line above last
			inputParameters = processRuleInputAttributesLine(tokenLines
					.get(tokenLines.size() - 2));

			if (overrideInputParameters != null) {
				log.info("will override parameters");
				inputParameters = collateOverridesIntoInputParameters(
						overrideInputParameters, inputParameters);
			}

			outputParameters = processRuleOutputAttributesLine(tokenLines
					.get(tokenLines.size() - 1));

			irodsRule = IRODSRule.instance(ruleAsPlainText, inputParameters,
					outputParameters, processRuleBodyOldFormat(tokenLines));

		}

		return irodsRule;
	}

	/**
	 * Given a set of derived input parameters (coming from the supplied rule
	 * body), and a set of overrides, arrive at the combined set.
	 *
	 * @param overrideInputParameters
	 *            <code>List</code> of {@link IRODSRuleParameter} which contains
	 *            the supplied set of input parameters that should override
	 *            those derived from the rule body.
	 * @param inputParameters
	 *            <code>List</code> of {@link IRODSRuleParameter} which contains
	 *            the input parameters as derived from the iRODS rule body
	 * @return <code>List</code> of {@link IRODSRuleParameter} with the collated
	 *         rule input parameters, including overrides
	 */
	protected List<IRODSRuleParameter> collateOverridesIntoInputParameters(
			final List<IRODSRuleParameter> overrideInputParameters,
			List<IRODSRuleParameter> inputParameters) {

		if (overrideInputParameters == null) {
			throw new IllegalArgumentException("null overrideInputParameters");
		}

		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}

		List<IRODSRuleParameter> overriddenParms = new ArrayList<IRODSRuleParameter>();

		/*
		 * Look at current params, if they have an override, then save the
		 * override, otherwise, propogate the current value
		 */
		for (IRODSRuleParameter current : inputParameters) {
			boolean found = false;
			for (IRODSRuleParameter override : overrideInputParameters) {
				// try to find an overriding
				if (current.getUniqueName().equals(override.getUniqueName())) {
					overriddenParms.add(override);
					found = true;
				}
			}

			if (!found) {
				// no override found, propogate current over
				overriddenParms.add(current);
			}
		}

		// I may have overrides that are not in current, propagate those over
		// too

		for (IRODSRuleParameter override : overrideInputParameters) {
			boolean found = false;
			for (IRODSRuleParameter current : overriddenParms) {
				// try to find the corresponding current
				if (current.getUniqueName().equals(override.getUniqueName())) {
					found = true;
				}
			}

			if (!found) {
				// no current found, propogate current over
				overriddenParms.add(override);
			}

			log.info("replacing original parms with overridden parms:{}",
					overriddenParms);
			inputParameters = overriddenParms;

		}
		return inputParameters;
	}

	/**
	 * @param tokens
	 * @return
	 */
	static RuleCharacteristics processRuleBodyNewFormat(
			final List<String> tokenLines) {

		// work backword to find input and output lines

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

		// find the rule

		RuleCharacteristics ruleCharacteristics = new RuleCharacteristics();
		ruleCharacteristics.setInputLineIndex(tokenInput);
		ruleCharacteristics.setOutputLineIndex(tokenOutput);
		ruleCharacteristics.setLastLineOfBody(lastRowOfRule);
		ruleCharacteristics.setRuleBody(total.toString());

		return ruleCharacteristics;
	}

	/**
	 * @param tokens
	 * @return
	 */
	static String processRuleBodyOldFormat(final List<String> tokenLines) {
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
	 * Break the line containing rule output parameters into a
	 * <code>List<IRODSRuleParameter></code>
	 *
	 * @param tokens
	 * @return
	 * @throws JargonException
	 */
	List<IRODSRuleParameter> processRuleOutputAttributesLine(
			String outputAttributesLine) throws JargonRuleException,
			JargonException {

		if (outputAttributesLine == null) {
			throw new JargonRuleException(
					"null passed to output attributes parser");
		}

		outputAttributesLine = outputAttributesLine.trim();
		if (outputAttributesLine.isEmpty()) {
			throw new JargonRuleException("outputAttributes line is blank");
		}

		int idxInput = outputAttributesLine.indexOf("OUTPUT");

		if (idxInput > -1) {
			outputAttributesLine = outputAttributesLine.substring(idxInput + 6);
		}

		if (outputAttributesLine.indexOf("**") > -1) {
			throw new JargonRuleException(
					"blank attribute in output attributes line indicated by duplicate '*' delimiters with no data");
		}

		if (outputAttributesLine.indexOf("%%") > -1) {
			throw new JargonRuleException(
					"blank attribute in output attributes line indicated by duplicate '%' delimiters with no data");
		}

		List<IRODSRuleParameter> outputAttributes = new ArrayList<IRODSRuleParameter>();

		StringTokenizer outputParmsTokenizer = null;

		outputParmsTokenizer = new StringTokenizer(
				outputAttributesLine.replaceAll(",", "%"), "%");

		while (outputParmsTokenizer.hasMoreTokens()) {
			outputAttributes.add(processOutputParmsToken(outputParmsTokenizer
					.nextToken().trim()));
		}

		return outputAttributes;

	}

	private IRODSRuleParameter processOutputParmsToken(final String nextToken)
			throws JargonException, JargonRuleException {

		if (nextToken == null) {
			throw new JargonException("null nextToken");
		}

		if (nextToken.isEmpty()) {
			throw new JargonRuleException("output parms token is empty");
		}

		String parmName = nextToken;

		log.debug("returning outputParm: {}", parmName);

		return new IRODSRuleParameter(parmName, "");
	}

	/**
	 * Break the line containing rule input parameters into a
	 * <code>List<IRODSRuleParameter></code>
	 *
	 * @param inputAttributesLine
	 * @return
	 * @throws JargonRuleException
	 * @throws JargonException
	 */
	List<IRODSRuleParameter> processRuleInputAttributesLine(
			String inputAttributesLine) throws JargonRuleException,
			JargonException {

		if (inputAttributesLine == null) {
			throw new JargonRuleException(
					"null tokens passed to input attributes parser");
		}

		inputAttributesLine = inputAttributesLine.trim();

		if (inputAttributesLine.isEmpty()) {
			throw new JargonRuleException("inputAttributesLine is empty");
		}

		int idxInput = inputAttributesLine.indexOf("INPUT");

		if (idxInput > -1) {
			inputAttributesLine = inputAttributesLine.substring(idxInput + 5)
					.trim();
		}

		List<IRODSRuleParameter> inputAttributes = new ArrayList<IRODSRuleParameter>();

		if (inputAttributesLine.equals("null")) {
			if (irodsServerProperties
					.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")
					&& idxInput > -1) {
				log.info("no null prop for new format irods rules");
			} else {
				log.info("adding null param for old style rule");
				inputAttributes.add(new IRODSRuleParameter());
			}
			return inputAttributes;
		}

		// split into tokens on % delim

		if (inputAttributesLine.indexOf("%%") > -1) {
			throw new JargonRuleException(
					"blank attribute in input attributes line indicated by duplicate '%' delimiters with no data");
		}

		// TODO: eventually replace with tokenizer that respects commas and %
		// embedded in quotes
		StringTokenizer inputParmsTokenizer = null;

		if (idxInput > -1) {
			// new rule format, delim by comma
			inputParmsTokenizer = new StringTokenizer(inputAttributesLine, ",");
		} else {
			inputParmsTokenizer = new StringTokenizer(inputAttributesLine, "%");
		}

		while (inputParmsTokenizer.hasMoreTokens()) {

			IRODSRuleParameter param = processInputParmsToken(inputParmsTokenizer
					.nextToken());

			if (param != null) {
				inputAttributes.add(param);

			}

		}
		return inputAttributes;
	}

	private IRODSRuleParameter processInputParmsToken(String nextToken)
			throws JargonRuleException, JargonException {
		if (nextToken == null) {
			throw new JargonException("null nextToken");
		}

		if (nextToken.isEmpty()) {
			throw new JargonRuleException("input parms token is empty");
		}

		nextToken = nextToken.trim();

		if (nextToken.equals("null")) {
			throw new JargonRuleException(
					"embedded 'null' in input attributes, must be the only value if null");
		}

		RuleInputParameter param = RuleParsingUtils
				.parseInputParameterForNameAndValue(nextToken);

		if (param == null) {
			return null;
		}

		if (param.getParamName().indexOf(SPLAT) == -1) {
			throw new JargonRuleException(
					"missing * in first character of input parameter:"
							+ param.getParamName());
		} else if (param.getParamName().indexOf(SPLAT) > 0) {
			throw new JargonRuleException(
					"* must be the first character of input parameter:"
							+ param.getParamName());
		}

		if (param.getParamName().indexOf(SPLAT, 1) > -1) {
			throw new JargonRuleException("duplicate '*' character in parm:"
					+ param.getParamName());
		}

		log.debug("returning inputParm: {}", param);

		return new IRODSRuleParameter(param.getParamName(),
				param.getParamValue());
	}

	/**
	 * Convenience method that uses a simple heuristic to determine whether the
	 * given rule uses the 'new' rule syntax.
	 *
	 * @param ruleText
	 *            <code>String</code> with the rule body
	 * @return <code>boolean</code> that will be <code>true</code> if the rule
	 *         is using the new syntax
	 */
	public static final boolean isUsingNewRuleSyntax(final String ruleText) {
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

}