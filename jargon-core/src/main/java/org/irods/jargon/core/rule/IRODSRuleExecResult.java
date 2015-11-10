/**
 *
 */
package org.irods.jargon.core.rule;

import java.util.Map;

import org.irods.jargon.core.exception.JargonException;

/**
 * Encapsulates the result of the execution of a rule. This is an immutable
 * object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSRuleExecResult {

	private final IRODSRule irodsRule;
	private final Map<String, IRODSRuleExecResultOutputParameter> outputParameterResults;

	/**
	 * Return a new instance encapsulating the result of a rule execution
	 *
	 * @param irodsRule
	 *            {@link org.irods.jargon.core.rule.IRODSRule}
	 * @param resultParameters
	 *            <code>Tag</code> with the response from iRODS
	 * @return
	 * @throws JargonException
	 */
	public static IRODSRuleExecResult instance(
			final IRODSRule irodsRule,
			final Map<String, IRODSRuleExecResultOutputParameter> resultParameters)
					throws JargonException {
		return new IRODSRuleExecResult(irodsRule, resultParameters);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("IRODSRuleExecResult");
		sb.append("\n   irodsRule:");
		sb.append(irodsRule);
		sb.append("\n   resultParameters:");
		sb.append(outputParameterResults);
		return sb.toString();
	}

	private IRODSRuleExecResult(
			final IRODSRule irodsRule,
			final Map<String, IRODSRuleExecResultOutputParameter> resultParameters)
					throws JargonException {

		if (irodsRule == null) {
			throw new JargonException("null irodsRule");
		}

		if (resultParameters == null) {
			throw new JargonException("null resultParameters");
		}

		this.irodsRule = irodsRule;
		outputParameterResults = resultParameters;
	}

	public IRODSRule getIrodsRule() {
		return irodsRule;
	}

	public Map<String, IRODSRuleExecResultOutputParameter> getOutputParameterResults() {
		return outputParameterResults;
	}

	/**
	 * Return the standard output from the rule invocation, this is a short-cut
	 * to getting the ruleExecOut from the output parameters.
	 *
	 * @return <code>String</code> with the rule exec out. This will be a blank
	 *         value (not null) if no output was found.
	 */
	public String getRuleExecOut() {
		IRODSRuleExecResultOutputParameter execOut = outputParameterResults
				.get("ruleExecOut");
		if (execOut == null) {
			return "";
		}

		Object outputObject = execOut.getResultObject();
		if (outputObject == null) {
			return "";
		}
		String out = (String) outputObject;
		return out;
	}

	/**
	 * Return the standard error from the rule invocation, this is a short-cut
	 * to getting the ruleExecOut from the output parameters.
	 *
	 * @return <code>String</code> with the rule exec err. This will be a blank
	 *         value (not null) if no output was found.
	 */
	public String getRuleExecErr() {
		IRODSRuleExecResultOutputParameter execOut = outputParameterResults
				.get("ruleExecErrorOut");
		if (execOut == null) {
			return "";
		}

		Object outputObject = execOut.getResultObject();
		if (outputObject == null) {
			return "";
		}
		String out = (String) outputObject;
		return out;
	}
}
