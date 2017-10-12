package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRuleTranslator {

	private static final String SPLAT = "*";
	private final IRODSServerProperties irodsServerProperties;
	private final RuleInvocationConfiguration ruleInvocationConfiguration;
	Logger log = LoggerFactory.getLogger(this.getClass());
	private final JargonProperties jargonProperties;

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
	public IRODSRule translatePlainTextRuleIntoIRODSRule(final String ruleAsPlainText)
			throws JargonRuleException, JargonException {
		return translatePlainTextRuleIntoRule(ruleAsPlainText, null);
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
	public abstract IRODSRule translatePlainTextRuleIntoRule(final String ruleAsPlainText,
			final List<IRODSRuleParameter> overrideInputParameters) throws JargonRuleException, JargonException;

	/**
	 * Given a set of derived input parameters (coming from the supplied rule body),
	 * and a set of overrides, arrive at the combined set.
	 *
	 * @param overrideInputParameters
	 *            {@code List} of {@link IRODSRuleParameter} which contains the
	 *            supplied set of input parameters that should override those
	 *            derived from the rule body.
	 * @param inputParameters
	 *            {@code List} of {@link IRODSRuleParameter} which contains the
	 *            input parameters as derived from the iRODS rule body
	 * @return {@code List} of {@link IRODSRuleParameter} with the collated rule
	 *         input parameters, including overrides
	 */
	protected List<IRODSRuleParameter> collateOverridesIntoInputParameters(
			final List<IRODSRuleParameter> overrideInputParameters, List<IRODSRuleParameter> inputParameters) {

		if (overrideInputParameters == null) {
			throw new IllegalArgumentException("null overrideInputParameters");
		}

		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}

		List<IRODSRuleParameter> overriddenParms = new ArrayList<IRODSRuleParameter>();

		/*
		 * Look at current params, if they have an override, then save the override,
		 * otherwise, propagate the current value
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

			log.info("replacing original parms with overridden parms:{}", overriddenParms);
			inputParameters = overriddenParms;

		}
		return inputParameters;
	}

	/**
	 * Break the line containing rule output parameters into a
	 * {@code List<IRODSRuleParameter>}
	 *
	 * @param tokens
	 * @return
	 * @throws JargonException
	 */
	List<IRODSRuleParameter> processRuleOutputAttributesLine(String outputAttributesLine)
			throws JargonRuleException, JargonException {

		if (outputAttributesLine == null) {
			throw new JargonRuleException("null passed to output attributes parser");
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

		outputParmsTokenizer = new StringTokenizer(outputAttributesLine.replaceAll(",", "%"), "%");

		while (outputParmsTokenizer.hasMoreTokens()) {
			outputAttributes.add(processOutputParmsToken(outputParmsTokenizer.nextToken().trim()));
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
	 * {@code List<IRODSRuleParameter>}
	 *
	 * @param inputAttributesLine
	 * @return
	 * @throws JargonRuleException
	 * @throws JargonException
	 */
	List<IRODSRuleParameter> processRuleInputAttributesLine(String inputAttributesLine)
			throws JargonRuleException, JargonException {

		if (inputAttributesLine == null) {
			throw new JargonRuleException("null tokens passed to input attributes parser");
		}

		inputAttributesLine = inputAttributesLine.trim();

		if (inputAttributesLine.isEmpty()) {
			throw new JargonRuleException("inputAttributesLine is empty");
		}

		int idxInput = inputAttributesLine.indexOf("INPUT");

		if (idxInput > -1) {
			inputAttributesLine = inputAttributesLine.substring(idxInput + 5).trim();
		}

		List<IRODSRuleParameter> inputAttributes = new ArrayList<IRODSRuleParameter>();

		if (inputAttributesLine.equals("null")) {
			if (irodsServerProperties.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0") && idxInput > -1) {
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

			IRODSRuleParameter param = processInputParmsToken(inputParmsTokenizer.nextToken());

			if (param != null) {
				inputAttributes.add(param);

			}

		}
		return inputAttributes;
	}

	private IRODSRuleParameter processInputParmsToken(String nextToken) throws JargonRuleException, JargonException {
		if (nextToken == null) {
			throw new JargonException("null nextToken");
		}

		if (nextToken.isEmpty()) {
			throw new JargonRuleException("input parms token is empty");
		}

		nextToken = nextToken.trim();

		if (nextToken.equals("null")) {
			throw new JargonRuleException("embedded 'null' in input attributes, must be the only value if null");
		}

		RuleInputParameter param = RuleParsingUtils.parseInputParameterForNameAndValue(nextToken);

		if (param == null) {
			return null;
		}

		if (param.getParamName().indexOf(SPLAT) == -1) {
			throw new JargonRuleException("missing * in first character of input parameter:" + param.getParamName());
		} else if (param.getParamName().indexOf(SPLAT) > 0) {
			throw new JargonRuleException("* must be the first character of input parameter:" + param.getParamName());
		}

		if (param.getParamName().indexOf(SPLAT, 1) > -1) {
			throw new JargonRuleException("duplicate '*' character in parm:" + param.getParamName());
		}

		log.debug("returning inputParm: {}", param);

		return new IRODSRuleParameter(param.getParamName(), param.getParamValue());
	}

	public IRODSServerProperties getIrodsServerProperties() {
		return irodsServerProperties;
	}

	/**
	 * @return the ruleInvocationConfiguration
	 */
	public RuleInvocationConfiguration getRuleInvocationConfiguration() {
		return ruleInvocationConfiguration;
	}

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsServerProperties
	 *            {@link IRODSServerProperties} that characterizes the current iRODS
	 *            server
	 * @param ruleInvocationConfiguration
	 *            {@link RuleInvocationConfiguration} with information about which
	 *            rule engine the rule should be invoked on
	 * @param jargonProperties
	 *            {@link JargonProperties} with settings that indicate desired
	 *            client behavior
	 */
	public AbstractRuleTranslator(final IRODSServerProperties irodsServerProperties,
			final RuleInvocationConfiguration ruleInvocationConfiguration, final JargonProperties jargonProperties) {

		if (irodsServerProperties == null) {
			throw new IllegalArgumentException("null irodsServerProperties");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		this.irodsServerProperties = irodsServerProperties;
		this.ruleInvocationConfiguration = ruleInvocationConfiguration;
		this.jargonProperties = jargonProperties;
	}

	/**
	 * @return the jargonProperties
	 */
	public JargonProperties getJargonProperties() {
		return jargonProperties;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractRuleTranslator [");
		if (irodsServerProperties != null) {
			builder.append("irodsServerProperties=").append(irodsServerProperties).append(", ");
		}
		if (ruleInvocationConfiguration != null) {
			builder.append("ruleInvocationConfiguration=").append(ruleInvocationConfiguration).append(", ");
		}
		if (log != null) {
			builder.append("log=").append(log).append(", ");
		}
		if (jargonProperties != null) {
			builder.append("jargonProperties=").append(jargonProperties);
		}
		builder.append("]");
		return builder.toString();
	}

}