/**
 *
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.exception.JargonException;

/**
 * Encapsulates an object that represents an output parameter of a rule
 * execution. This is an immutable object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSRuleExecResultOutputParameter {
	public enum OutputParamType {
		RULE_EXEC_OUT, QUERY_OUT, CLIENT_ACTION_RESULT, STRING, INT
	}

	private String parameterName;
	private OutputParamType outputParamType;
	private Object resultObject;

	/**
	 * Return a new immutable value object that encapsulates the output of a
	 * rule for a specific designated output parameter
	 *
	 * @param parameterName
	 *            <code>String</code> with the parameter name as specified in
	 *            the rule
	 * @param outputParamType
	 *            <code>OutputParamType</code> enum value that indicates the
	 *            type of result
	 * @param resultObject
	 *            <code>Object</code> with the value associated with the
	 *            parameter. This result can be cast to the appropriate type by
	 *            the caller based on the parameter name it is associated with.
	 * @return
	 * @throws JargonException
	 */
	public static final IRODSRuleExecResultOutputParameter instance(
			final String parameterName, final OutputParamType outputParamType,
			final Object resultObject) throws JargonException {
		return new IRODSRuleExecResultOutputParameter(parameterName,
				outputParamType, resultObject);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Rule output parm:");
		sb.append("\n   parameterName:");
		sb.append(parameterName);
		sb.append("\n   parmType:");
		sb.append(outputParamType);
		sb.append("\n   resultObject:");
		sb.append(resultObject);
		return sb.toString();
	}

	protected IRODSRuleExecResultOutputParameter(final String parameterName,
			final OutputParamType outputParamType, final Object resultObject)
			throws JargonException {

		if (parameterName == null || parameterName.isEmpty()) {
			throw new JargonException("parameterName is null or empty");
		}

		if (outputParamType == null) {
			throw new JargonException("outputParamType is null");
		}

		if (resultObject == null) {
			throw new JargonException("resultObject is null");
		}

		this.parameterName = parameterName;
		this.outputParamType = outputParamType;
		this.resultObject = resultObject;
	}

	public IRODSRuleExecResultOutputParameter() {
	}

	public String getParameterName() {
		return parameterName;
	}

	public OutputParamType getOutputParamType() {
		return outputParamType;
	}

	public Object getResultObject() {
		return resultObject;
	}

	/**
	 * @param parameterName
	 *            the parameterName to set
	 */
	public void setParameterName(final String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * @param outputParamType
	 *            the outputParamType to set
	 */
	public void setOutputParamType(final OutputParamType outputParamType) {
		this.outputParamType = outputParamType;
	}

	/**
	 * @param resultObject
	 *            the resultObject to set
	 */
	public void setResultObject(final Object resultObject) {
		this.resultObject = resultObject;
	}

}
