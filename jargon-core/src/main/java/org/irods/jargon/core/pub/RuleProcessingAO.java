package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DelayedRuleExecution;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.JargonRuleException;

public interface RuleProcessingAO extends IRODSAccessObject {

	/**
	 * Submit a user-defined rule for processing (analagous to running irule).
	 * Note that this method will, if running on iRODS 3.0 or higher, add the @external
	 * flag, which is explained below.  The @external flag is used when processing rules in the new
	 * rule format, and has not effect (or causes any harm) if supplied with a 'classic' style rule.
	 * <p/>
	 * The difference between having @external not having it is how the rule
	 * engine interprets string input parameters. For external, the input
	 * parameters are "code", for internal the input parameters are "values".
	 * For example, if you have,
	 * <p/>
	 * INPUT *A=10
	 * <p/>
	 * The packed binary/XML will have a string 1.
	 * <p/>
	 * If the rule is external, 10 is interpreted as "code", so the value is
	 * integer 10.
	 * <p/>
	 * If the rule is internal, 10 is interpreted as a "value", so the value is
	 * string "10".
	 * 
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
	 * TODO: work in progress Purge all rules from the delayed exec queue.
	 * <p/>
	 * <b>Note: this method purges ALL rules in the queue</b>
	 * 
	 * @return <code>int</code> with a count of the rules purged from the
	 *         delayed execution queue
	 * @throws JargonException
	 */
	int purgeAllDelayedExecQueue() throws JargonException;

	/**
	 * TODO: work in progress
	 * 
	 * @param partialStartIndex
	 * @return
	 * @throws JargonException
	 */
	List<DelayedRuleExecution> listAllDelayedRuleExecutions(
			int partialStartIndex) throws JargonException;

}