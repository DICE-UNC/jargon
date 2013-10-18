package org.irods.jargon.ruleservice.composition;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface defines an enhanced service for dealing with rules. Specifically,
 * this service provides hooks suitable for developing intefaces used to compose
 * and execute rules.
 * <p/>
 * Note that this service class refines and extends the jargon-core
 * <code>RuleProcessingAO</code> services, which represents basic rule
 * execution.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface RuleCompositionService {

	/**
	 * Given a <code>String</code> that represents an iRODS rule as would be
	 * executable by the iRODS irule command, parse that rule into the rule
	 * body, as well as input and output parameters.
	 * 
	 * @param inputRuleAsString
	 *            <code>String</code> with a valid iRODS rule
	 * @return {@link Rule} that represents that parsed rule string.
	 * @throws JargonException
	 */
	Rule parseStringIntoRule(String inputRuleAsString) throws JargonException;

	/**
	 * Load a rule from iRODS
	 * 
	 * @param absolutePathToRuleFile
	 *            <code>String</code> with an iRODS absolute path to a rules
	 *            file appropriate for 'new format' rules in iRODS.
	 * @return {@link Rule} that represents the parsed iRODS rule
	 * @throws MissingOrInvalidRuleException
	 *             if the rule is empty
	 * @throws JargonException
	 */
	Rule loadRuleFromIrods(String absolutePathToRuleFile)
			throws MissingOrInvalidRuleException, JargonException;

}