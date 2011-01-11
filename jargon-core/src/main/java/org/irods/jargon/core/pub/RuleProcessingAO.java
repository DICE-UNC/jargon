package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.JargonRuleException;

public interface RuleProcessingAO extends IRODSAccessObject {

	/**
	 * Submit a user-defined rule for processing (analagous to running irule)
	 * 
	 * @param irodsRuleAsString
	 *            <code>String</code> containing an iRODS rule as would be
	 *            submitted via irule
	 * @throws JargonRuleException
	 *             error in translation of the rule, typically syntax
	 * @throws JargonException
	 *             other error in irods or jargon
	 */
	IRODSRuleExecResult executeRule(final String irodsRuleAsString)
			throws JargonRuleException, JargonException;

}