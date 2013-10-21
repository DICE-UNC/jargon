package org.irods.jargon.ruleservice.composition;

import java.util.List;

import org.irods.jargon.core.exception.FileNotFoundException;
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
	 * @throws FileNotFoundException if the rule file cannot be found
	 * @throws MissingOrInvalidRuleException
	 *             if the rule is empty
	 * @throws JargonException
	 */
	Rule loadRuleFromIrods(String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException;

	/**
	 * Given a rule in primate string values, store as a rule in the given iRODS file.  This will handle overwrites.
	 * @param ruleAbsolutePath    <code>String</code> with an iRODS absolute path to a rules
	 *            file appropriate for 'new format' rules in iRODS.
	 * @param ruleBody   <code>String</code> with a valid iRODS rule body (without the input or output sections)
	 * @param inputParameters <code>List<String></code> with the input parameters of the rule in simple string name=value format, without wrapping quotes
	 * @param outputParameters <code>List<String></code> with the output parameters of the rule in simple string format
	 * @return {@link Rule} which is the parsed version of the given rule
	 * @throws JargonException
	 */
	Rule storeRuleFromParts(String ruleAbsolutePath, String ruleBody,
			List<String> inputParameters, List<String> outputParameters)
			throws JargonException;

}