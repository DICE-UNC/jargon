package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DelayedRuleExecution;
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

	/**
	 * TODO: work in progress
	 * Purge all rules from the delayed exec queue.  
	 * <p/>
	 * <b>Note:  this method purges ALL rules in the queue</b>
	 * @return <code>int</code> with a count of the rules purged from the delayed execution queue
	 * @throws JargonException
	 */
	int purgeAllDelayedExecQueue() throws JargonException;

	/**
	 * TODO: work in progress
	 * @param partialStartIndex
	 * @return
	 * @throws JargonException
	 */
	List<DelayedRuleExecution> listAllDelayedRuleExecutions(
			int partialStartIndex) throws JargonException;

}