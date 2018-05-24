package org.irods.jargon.ruleservice.composition;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.rule.IRODSRuleParameter;

/**
 * Representation of a rule in iRODS. This is a representation of a 'new' iRODS
 * rule, and does not support the 'classic' rule
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class Rule {

	public enum ProcessingType {
		INTERNAL, EXTERNAL
	}

	private ProcessingType processingType = ProcessingType.EXTERNAL;
	private String ruleBody = "";
	private List<IRODSRuleParameter> inputParameters = new ArrayList<IRODSRuleParameter>();
	private List<IRODSRuleParameter> outputParameters = new ArrayList<IRODSRuleParameter>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Rule:");
		sb.append("\n\tprocessingType:");
		sb.append(processingType);
		sb.append("\n\truleBody:");
		sb.append(ruleBody);
		sb.append("\n\tinputParameters:");
		for (IRODSRuleParameter parm : inputParameters) {
			sb.append("\n\t\tparm:");
			sb.append(parm);
		}
		sb.append("\n\toutputParameters:");
		for (IRODSRuleParameter parm : outputParameters) {
			sb.append("\n\t\tparm:");
			sb.append(parm);
		}
		return sb.toString();
	}

	public Rule() {
	}

	public ProcessingType getProcessingType() {
		return processingType;
	}

	public void setProcessingType(final ProcessingType processingType) {
		this.processingType = processingType;
	}

	public String getRuleBody() {
		return ruleBody;
	}

	public void setRuleBody(final String ruleBody) {
		this.ruleBody = ruleBody;
	}

	public List<IRODSRuleParameter> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(final List<IRODSRuleParameter> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public List<IRODSRuleParameter> getOutputParameters() {
		return outputParameters;
	}

	public void setOutputParameters(final List<IRODSRuleParameter> outputParameters) {
		this.outputParameters = outputParameters;
	}

}
