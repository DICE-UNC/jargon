package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

	public IRODSRule translatePlainTextRuleIntoIRODSRule(
			final String ruleAsPlainText) throws JargonRuleException,
			JargonException {
		if (ruleAsPlainText == null || ruleAsPlainText.isEmpty()) {
			throw new JargonRuleException("null or empty rule text");
		}

		log.info("translating rule: {}", ruleAsPlainText);
		StringTokenizer tokens = new StringTokenizer(ruleAsPlainText, "\n");

		String ruleBody = processRuleBody(tokens);

		// if formatting error, such as only one line, below breaks
		if (!tokens.hasMoreTokens()) {
			throw new JargonRuleException(
					"Rule stream is malformed, no input attributes found");
		}

		// process the rule attributes
		List<IRODSRuleParameter> inputParameters = processRuleInputAttributesLine(tokens
				.nextToken());

		// if formatting error, such as only one line, below breaks
		if (!tokens.hasMoreTokens()) {
			throw new JargonRuleException(
					"Rule stream is malformed, no output attributes found");
		}

		List<IRODSRuleParameter> outputParameters = processRuleOutputAttributesLine(tokens
				.nextToken());

		IRODSRule irodsRule = IRODSRule.instance(ruleAsPlainText,
				inputParameters, outputParameters, ruleBody);
		return irodsRule;
	}

	/**
	 * @param tokens
	 * @return
	 */
	static String processRuleBody(final StringTokenizer tokens) {
		String total;
		// if formatting error, such as only one line, below breaks
		if (!tokens.hasMoreTokens()) {
			throw new IllegalArgumentException("Rule stream is malformed");
		}

		// Remove comments
		total = tokens.nextToken();
		while (total.startsWith("#")) {
			total = tokens.nextToken();
		}
		// find the rule
		return total;
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

		if (outputAttributesLine.indexOf("%%") > -1) {
			throw new JargonRuleException(
					"blank attribute in output attributes line indicated by duplicate '%' delimiters with no data");
		}

		List<IRODSRuleParameter> outputAttributes = new ArrayList<IRODSRuleParameter>();

		StringTokenizer outputParmsTokenizer = new StringTokenizer(
				outputAttributesLine, "%");

		while (outputParmsTokenizer.hasMoreTokens()) {
			outputAttributes.add(processOutputParmsToken(outputParmsTokenizer
					.nextToken()));
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

		if (parmName.indexOf(SPLAT, 1) > -1) {
			throw new JargonRuleException("duplicate '*' character in parm:"
					+ parmName);
		}

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

		List<IRODSRuleParameter> inputAttributes = new ArrayList<IRODSRuleParameter>();

		if (inputAttributesLine.equals("null")) {
			inputAttributes.add(new IRODSRuleParameter());
			return inputAttributes;
		}

		// split into tokens on % delim

		if (inputAttributesLine.indexOf("%%") > -1) {
			throw new JargonRuleException(
					"blank attribute in input attributes line indicated by duplicate '%' delimiters with no data");
		}

		StringTokenizer inputParmsTokenizer = new StringTokenizer(
				inputAttributesLine, "%");

		while (inputParmsTokenizer.hasMoreTokens()) {
			inputAttributes.add(processInputParmsToken(inputParmsTokenizer
					.nextToken()));
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

		// split on required '=' sign. I can have a simple equality or a
		// condition where the value is a condition with embedded = sign
		StringTokenizer nameValTokenizer = new StringTokenizer(nextToken, "=");

		if (nameValTokenizer.countTokens() == 2
				|| nameValTokenizer.countTokens() == 3) {
			// this is OK
		} else {
			throw new JargonRuleException(
					"could not find name and val separated by an '=' sign in input attribute: "
							+ nextToken);
		}

		String parmName = nameValTokenizer.nextToken();
		String val = nameValTokenizer.nextToken();

		if (parmName.indexOf(SPLAT) == -1) {
			throw new JargonRuleException(
					"missing * in first character of input parameter:"
							+ parmName);
		} else if (parmName.indexOf(SPLAT) > 0) {
			throw new JargonRuleException(
					"* must be the first character of input parameter:"
							+ parmName);
		}

		if (parmName.indexOf(SPLAT, 1) > -1) {
			throw new JargonRuleException("duplicate '*' character in parm:"
					+ parmName);
		}

		if (val.isEmpty()) {
			throw new JargonRuleException(
					"value is null for input rule attribute:" + parmName);
		}

		if (nameValTokenizer.hasMoreTokens()) {
			StringBuilder newVal = new StringBuilder();
			// remember the tokenizer is split on =, so this is the right hand
			// side of a condition that is the value of the attribute
			newVal.append(val);
			newVal.append('=');
			newVal.append(nameValTokenizer.nextToken());
			val = newVal.toString();
		}

		log.debug("returning inputParm: {}", parmName);
		log.debug("parm value: {}", val);

		return new IRODSRuleParameter(parmName, val);
	}
}
