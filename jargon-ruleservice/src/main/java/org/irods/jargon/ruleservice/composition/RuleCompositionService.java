package org.irods.jargon.ruleservice.composition;

import java.util.List;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;

/**
 * Interface defines an enhanced service for dealing with rules. Specifically,
 * this service provides hooks suitable for developing intefaces used to compose
 * and execute rules.
 * <p>
 * Note that this service class refines and extends the jargon-core
 * {@code RuleProcessingAO} services, which represents basic rule execution.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface RuleCompositionService {

	/**
	 * Return a <code>String</code> which is the raw contents of an iRODS rule file
	 * on iRODS
	 *
	 * @param absolutePathToRuleFile
	 *            <code>String</code> with an iRODS absolute path to a rules file
	 * @return <code>String</code> with the raw rule contents
	 * @throws FileNotFoundException
	 * @throws MissingOrInvalidRuleException
	 * @throws JargonException
	 */
	String loadRuleFromIrodsAsString(final String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException;

	/**
	 * Store a rule presented as a raw String. This method will parse the String out
	 * as a level of validation.
	 *
	 * @param ruleAbsolutePath
	 *            <code>String</code> with the iRODS absolute path where the rule
	 *            will be stored
	 * @param rule
	 *            <code>String</code> with the raw rule text
	 * @return {@Rule} that was the result of the parsing, as stored in iRODS
	 * @throws JargonException
	 */
	Rule storeRule(String ruleAbsolutePath, String rule) throws JargonException;

	/**
	 * Run a rule based on an arbitrary raw string that holds the desired rule
	 *
	 * @param rule
	 *            <code>String</code> with the raw rule text
	 * @return {@link IRODSRuleExecResult} with the output parameters and log from
	 *         the rule
	 * @throws JargonException
	 */
	IRODSRuleExecResult executeRuleAsRawString(String rule) throws JargonException;

	/**
	 * Given a {@code String} that represents an iRODS rule as would be executable
	 * by the iRODS irule command, parse that rule into the rule body, as well as
	 * input and output parameters.
	 *
	 * @param inputRuleAsString
	 *            {@code String} with a valid iRODS rule
	 * @return {@link Rule} that represents that parsed rule string.
	 * @throws JargonException
	 */
	Rule parseStringIntoRule(String inputRuleAsString) throws JargonException;

	/**
	 * Load a rule from iRODS
	 *
	 * @param absolutePathToRuleFile
	 *            {@code String} with an iRODS absolute path to a rules file
	 *            appropriate for 'new format' rules in iRODS.
	 * @return {@link Rule} that represents the parsed iRODS rule
	 * @throws FileNotFoundException
	 *             if the rule file cannot be found
	 * @throws MissingOrInvalidRuleException
	 *             if the rule is empty
	 * @throws JargonException
	 */
	Rule loadRuleFromIrods(String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException;

	/**
	 * Given a rule in primative string values, store as a rule in the given iRODS
	 * file. This will handle overwrites.
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file
	 *            appropriate for 'new format' rules in iRODS.
	 * @param ruleBody
	 *            {@code String} with a valid iRODS rule body (without the input or
	 *            output sections)
	 * @param inputParameters
	 *            {@code List<String>} with the input parameters of the rule in
	 *            simple string name=value format, without wrapping quotes
	 * @param outputParameters
	 *            {@code List<String>} with the output parameters of the rule in
	 *            simple string format
	 * @return {@link Rule} which is the parsed version of the given rule
	 * @throws JargonException
	 */
	Rule storeRuleFromParts(String ruleAbsolutePath, String ruleBody, List<String> inputParameters,
			List<String> outputParameters) throws JargonException;

	/**
	 * Given a rule in {@link Rule} format, serialize and store in iRODS
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file
	 *            appropriate for 'new format' rules in iRODS.
	 * @param rule
	 *            {@link Rule} to serialize and store
	 * @return {@link Rule} that has been stored
	 * @throws JargonException
	 */
	Rule storeRule(String ruleAbsolutePath, Rule rule) throws JargonException;

	/**
	 * Given the name of an input parameter (with the leading * included), delete it
	 * from the rule input parameter list and store back in iRODS.
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file
	 *            appropriate for 'new format' rules in iRODS.
	 * @param parameterToDelete
	 *            {@code String} with the name of the parameter, with the leading *
	 *            character (e.g. *Flags), this input parameter will be deleted from
	 *            the rule. If the parameter is missing, then no action will be
	 *            taken.
	 * @return {@link Rule} reflecting the state of the rule after any updates.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	Rule deleteInputParameterFromRule(String ruleAbsolutePath, String parameterToDelete)
			throws FileNotFoundException, JargonException;

	/**
	 * Given the name of an output parameter (with the leading * included), delete
	 * it from the rule output parameter list and store back in iRODS.
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file
	 *            appropriate for 'new format' rules in iRODS.
	 * @param parameterToDelete
	 *            {@code String} with the name of the output parameter, with the
	 *            leading * character (e.g. *Flags), this input parameter will be
	 *            deleted from the rule. If the parameter is missing, then no action
	 *            will be taken.
	 * @return {@link Rule} reflecting the state of the rule after any updates.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	Rule deleteOutputParameterFromRule(String ruleAbsolutePath, String parameterToDelete)
			throws FileNotFoundException, JargonException;

	/**
	 * Given a rule in primative string values, execute the rule in iRODS.
	 *
	 * @param ruleBody
	 *            {@code String} with a valid iRODS rule body (without the input or
	 *            output sections)
	 * @param inputParameters
	 *            {@code List<String>} with the input parameters of the rule in
	 *            simple string name=value format, without wrapping quotes
	 * @param outputParameters
	 *            {@code List<String>} with the output parameters of the rule in
	 *            simple string format
	 * @return {@link IRODSRuleExecResult} with the output parameters from the rule
	 *         execution
	 * @throws JargonException
	 */
	IRODSRuleExecResult executeRuleFromParts(String ruleBody, List<String> inputParameters,
			List<String> outputParameters) throws JargonException;

	/**
	 * Given a rule in primative string values, execute the rule in iRODS,
	 * specifying rule type and language information.
	 *
	 * @param ruleBody
	 *            {@code String} with a valid iRODS rule body (without the input or
	 *            output sections)
	 * @param inputParameters
	 *            {@code List<String>} with the input parameters of the rule in
	 *            simple string name=value format, without wrapping quotes
	 * @param outputParameters
	 *            {@code List<String>} with the output parameters of the rule in
	 *            simple string format
	 * @param ruleInvocationConfiguration
	 *            {@link RuleInvocationConfiguration} specifying rule language and
	 *            instance information
	 * @return {@link IRODSRuleExecResult} with the output parameters from the rule
	 *         execution
	 * @throws JargonException
	 */
	IRODSRuleExecResult executeRuleFromParts(String ruleBody, List<String> inputParameters,
			List<String> outputParameters, RuleInvocationConfiguration ruleInvocationConfiguration)
			throws JargonException;

	/**
	 * Add the given input parameter to the iRODS rule.
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file to
	 *            which the parameter will be added
	 * @param parameterName
	 *            {@code String} with the name of the new parameter
	 * @param parameterValue
	 *            {@code String} with the value for the new parameter
	 * @return {@link Rule} as updated
	 * @throws FileNotFoundException
	 *             if the iRODS rule file is missing
	 * @throws DuplicateDataException
	 *             if the parameter already exists
	 * @throws JargonException
	 */
	Rule addInputParameterToRule(String ruleAbsolutePath, String parameterName, String parameterValue)
			throws FileNotFoundException, DuplicateDataException, JargonException;

	/**
	 * Add the given output parameter to the iRODS rule.
	 *
	 * @param ruleAbsolutePath
	 *            {@code String} with an iRODS absolute path to a rules file to
	 *            which the parameter will be added
	 * @param parameterName
	 *            {@code String} with the name of the new parameter
	 * @return {@link Rule} as updated
	 * @throws FileNotFoundException
	 *             if the iRODS rule file is missing
	 * @throws DuplicateDataException
	 *             if the parameter already exists
	 * @throws JargonException
	 */
	Rule addOutputParameterToRule(String ruleAbsolutePath, String parameterName)
			throws FileNotFoundException, DuplicateDataException, JargonException;

}