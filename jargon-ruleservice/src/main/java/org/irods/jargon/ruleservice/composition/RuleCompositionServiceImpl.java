/**
 * 
 */
package org.irods.jargon.ruleservice.composition;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.rule.AbstractRuleTranslator;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.IrodsRuleEngineRuleTranslator;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation to support composition of rules
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RuleCompositionServiceImpl extends AbstractJargonService implements RuleCompositionService {

	public static final Logger log = LoggerFactory.getLogger(RuleCompositionServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * addInputParameterToRule(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Rule addInputParameterToRule(final String ruleAbsolutePath, final String parameterName,
			final String parameterValue) throws FileNotFoundException, DuplicateDataException, JargonException {

		log.info("addInputParameterToRule()");

		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (parameterName == null || parameterName.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterName");
		}

		if (parameterValue == null || parameterValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterValue");
		}

		Rule currentRule = loadRuleFromIrods(ruleAbsolutePath);
		log.info("found current rule, recompose by deleting input parameter and reformatting");

		List<IRODSRuleParameter> newParms = new ArrayList<IRODSRuleParameter>();

		for (int i = 0; i < currentRule.getInputParameters().size(); i++) {
			if (currentRule.getInputParameters().get(i).getUniqueName().equals(parameterName)) {
				log.error("duplicate input parameter");
				throw new DuplicateDataException("duplicate input parameter");
			} else {
				newParms.add(currentRule.getInputParameters().get(i));
			}
		}

		IRODSRuleParameter newParam = new IRODSRuleParameter(parameterName, parameterValue);

		newParms.add(newParam);

		currentRule.setInputParameters(newParms);

		log.info("added parameter...now store the rule");
		storeRule(ruleAbsolutePath, currentRule);
		log.info("parameter add completed");
		return currentRule;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * addOutputParameterToRule(java.lang.String, java.lang.String)
	 */
	@Override
	public Rule addOutputParameterToRule(final String ruleAbsolutePath, final String parameterName)
			throws FileNotFoundException, DuplicateDataException, JargonException {

		log.info("addOutputParameterToRule()");

		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (parameterName == null || parameterName.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterName");
		}

		Rule currentRule = loadRuleFromIrods(ruleAbsolutePath);
		log.info("found current rule, recompose by deleting input parameter and reformatting");

		List<IRODSRuleParameter> newParms = new ArrayList<IRODSRuleParameter>();

		for (int i = 0; i < currentRule.getOutputParameters().size(); i++) {
			if (currentRule.getOutputParameters().get(i).getUniqueName().equals(parameterName)) {
				log.error("duplicate output parameter");
				throw new DuplicateDataException("duplicate output parameter");
			} else {
				newParms.add(currentRule.getOutputParameters().get(i));
			}
		}

		IRODSRuleParameter newParam = new IRODSRuleParameter(parameterName, "");

		newParms.add(newParam);

		currentRule.setOutputParameters(newParms);

		log.info("added parameter...now store the rule");
		storeRule(ruleAbsolutePath, currentRule);
		log.info("parameter add completed");
		return currentRule;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * parseStringIntoRule(java.lang.String)
	 */
	@Override
	public Rule parseStringIntoRule(final String inputRuleAsString) throws JargonException {
		log.info("parseStringIntoRule()");

		if (inputRuleAsString == null || inputRuleAsString.isEmpty()) {
			throw new IllegalArgumentException("inputRuleAsString is null or empty");
		}

		log.info("inputRuleAsString:{}", inputRuleAsString);

		final AbstractRuleTranslator irodsRuleTranslator = new IrodsRuleEngineRuleTranslator(
				getIrodsAccessObjectFactory().getIRODSServerProperties(getIrodsAccount()),
				RuleInvocationConfiguration.instanceWithDefaultAutoSettings(),
				this.getIrodsAccessObjectFactory().getJargonProperties());

		IRODSRule irodsRule = irodsRuleTranslator.translatePlainTextRuleIntoIRODSRule(inputRuleAsString);

		log.info("got irodsRule:{}", irodsRule);

		Rule rule = new Rule();
		rule.setRuleBody(irodsRule.getRuleBody());
		rule.setInputParameters(irodsRule.getIrodsRuleInputParameters());
		rule.setOutputParameters(irodsRule.getIrodsRuleOutputParameters());
		log.info("resulting rule:{}", rule);
		return rule;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * loadRuleFromIrods(java.lang.String)
	 */
	@Override
	public Rule loadRuleFromIrods(final String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException {

		log.info("loadRuleFromIrods()");

		if (absolutePathToRuleFile == null || absolutePathToRuleFile.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutepPathToRuleFile");
		}

		IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount());

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(absolutePathToRuleFile);
		if (!irodsFile.exists()) {
			log.error("did not find rule file");
			throw new FileNotFoundException("rule file not found");
		}

		IRODSFileReader irodsFileReader = irodsFileFactory.instanceIRODSFileReader(absolutePathToRuleFile);

		StringWriter writer = null;
		String ruleString = null;

		try {
			writer = new StringWriter();
			char[] buff = new char[1024];
			int i = 0;
			while ((i = irodsFileReader.read(buff)) > -1) {
				writer.write(buff, 0, i);
			}

			ruleString = writer.toString();

			if (ruleString == null || ruleString.isEmpty()) {
				log.error("null or empty rule string");
				throw new MissingOrInvalidRuleException("no rule found");
			}

			return parseStringIntoRule(ruleString);

		} catch (IOException ioe) {
			log.error("io exception reading rule data from resource", ioe);
			throw new JargonException("error reading rule from resource", ioe);
		} finally {
			try {
				irodsFileReader.close();
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// ignore
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * loadRuleFromIrodsAsString(java.lang.String)
	 */
	@Override
	public String loadRuleFromIrodsAsString(final String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException {
		log.info("loadRuleFromIrodsAsString()");

		log.info("loadRuleFromIrods()");

		if (absolutePathToRuleFile == null || absolutePathToRuleFile.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutepPathToRuleFile");
		}

		IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount());

		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(absolutePathToRuleFile);
		if (!irodsFile.exists()) {
			log.error("did not find rule file");
			throw new FileNotFoundException("rule file not found");
		}

		IRODSFileReader irodsFileReader = irodsFileFactory.instanceIRODSFileReader(absolutePathToRuleFile);

		StringWriter writer = null;
		String ruleString = null;

		try {
			writer = new StringWriter();
			char[] buff = new char[1024];
			int i = 0;
			while ((i = irodsFileReader.read(buff)) > -1) {
				writer.write(buff, 0, i);
			}

			ruleString = writer.toString();

			if (ruleString == null || ruleString.isEmpty()) {
				log.error("null or empty rule string");
				throw new MissingOrInvalidRuleException("no rule found");
			}

		} catch (IOException ioe) {
			log.error("io exception reading rule data from resource", ioe);
			throw new JargonException("error reading rule from resource", ioe);
		} finally {
			try {
				irodsFileReader.close();
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// ignore
			}

		}

		return ruleString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ruleservice.composition.RuleCompositionService#storeRule
	 * (java.lang.String, org.irods.jargon.ruleservice.composition.Rule)
	 */
	@Override
	public Rule storeRule(final String ruleAbsolutePath, final Rule rule) throws JargonException {

		log.info("storeRule()");
		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (rule == null) {
			throw new IllegalArgumentException("null rule");
		}

		log.info("ruleAbsolutePath:{}", ruleAbsolutePath);
		log.info("rule:{}", rule);

		List<String> inputParameters = new ArrayList<String>();
		List<String> outputParameters = new ArrayList<String>();

		StringBuilder sb;

		for (IRODSRuleParameter parm : rule.getInputParameters()) {
			sb = new StringBuilder();
			sb.append(parm.getUniqueName());
			sb.append("=");
			sb.append(parm.retrieveStringValue());
			inputParameters.add(sb.toString());
		}

		for (IRODSRuleParameter parm : rule.getOutputParameters()) {
			outputParameters.add(parm.getUniqueName());
		}

		return storeRuleFromParts(ruleAbsolutePath, rule.getRuleBody(), inputParameters, outputParameters);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * storeRuleFromParts(java.lang.String, java.lang.String, java.util.List,
	 * java.util.List)
	 */
	@Override
	public Rule storeRuleFromParts(final String ruleAbsolutePath, final String ruleBody,
			final List<String> inputParameters, final List<String> outputParameters) throws JargonException {
		log.info("storeRuleFromParts()");

		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleBody");
		}

		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}

		if (outputParameters == null) {
			throw new IllegalArgumentException("null outputParameters");
		}

		log.info("ruleAbsolutePath:{}", ruleAbsolutePath);
		log.info("inputParameters:{}", inputParameters);
		log.info("outputParameters:{}", outputParameters);

		String ruleAsString = buildRuleStringFromParts(ruleBody, inputParameters, outputParameters);
		IRODSFile irodsFile = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(ruleAbsolutePath);

		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory().getStream2StreamAO(getIrodsAccount());
		try {
			stream2StreamAO.streamBytesToIRODSFile(
					ruleAsString.getBytes(getIrodsAccessObjectFactory().getJargonProperties().getEncoding()),
					irodsFile);
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding streaming to file", e);
			throw new JargonException("error writing rule file", e);
		}

		log.info("rule stored:{}", ruleAsString);

		return parseStringIntoRule(ruleAsString);

	}

	private String buildRuleStringFromParts(final String ruleBody, final List<String> inputParameters,
			final List<String> outputParameters) {

		StringBuilder sb = new StringBuilder();
		sb.append(ruleBody);
		sb.append(System.getProperty("line.separator"));
		sb.append("INPUT ");

		if (inputParameters.isEmpty()) {
			sb.append("null");
		} else {
			int i = 0;
			for (String param : inputParameters) {
				if (i++ > 0) {
					sb.append(", ");
				}
				sb.append(param);
			}
		}

		sb.append(System.getProperty("line.separator"));
		sb.append("OUTPUT ");

		if (outputParameters.isEmpty()) {
			sb.append("null");
		} else {
			int i = 0;
			for (String param : outputParameters) {
				if (i++ > 0) {
					sb.append(", ");
				}
				sb.append(param);
			}
		}

		sb.append(System.getProperty("line.separator"));

		String ruleAsString = sb.toString();
		return ruleAsString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * executeRuleFromParts(java.lang.String, java.util.List, java.util.List)
	 */
	@Override
	public IRODSRuleExecResult executeRuleFromParts(final String ruleBody, final List<String> inputParameters,
			final List<String> outputParameters) throws JargonException {

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleBody");
		}

		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}

		if (outputParameters == null) {
			throw new IllegalArgumentException("null outputParameters");
		}

		log.info("ruleBody:{}", ruleBody);
		log.info("inputParameters:{}", inputParameters);
		log.info("outputParameters:{}", outputParameters);

		String ruleAsString = buildRuleStringFromParts(ruleBody, inputParameters, outputParameters);

		RuleProcessingAO ruleProcessingAO = irodsAccessObjectFactory.getRuleProcessingAO(getIrodsAccount());
		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings(this.irodsAccessObjectFactory.getJargonProperties());

		log.info("getting ready to submit rule:{}", ruleAsString);
		return ruleProcessingAO.executeRule(ruleAsString, null, ruleInvocationConfiguration);

	}

	@Override
	public IRODSRuleExecResult executeRuleFromParts(final String ruleBody, final List<String> inputParameters,
			final List<String> outputParameters, final RuleInvocationConfiguration ruleInvocationConfiguration)
			throws JargonException {

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleBody");
		}

		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}

		if (outputParameters == null) {
			throw new IllegalArgumentException("null outputParameters");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		log.info("ruleBody:{}", ruleBody);
		log.info("inputParameters:{}", inputParameters);
		log.info("outputParameters:{}", outputParameters);

		String ruleAsString = buildRuleStringFromParts(ruleBody, inputParameters, outputParameters);

		RuleProcessingAO ruleProcessingAO = irodsAccessObjectFactory.getRuleProcessingAO(getIrodsAccount());

		log.info("getting ready to submit rule:{}", ruleAsString);
		return ruleProcessingAO.executeRule(ruleAsString, null, ruleInvocationConfiguration);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * deleteInputParameterFromRule(java.lang.String, java.lang.String)
	 */
	@Override
	public Rule deleteInputParameterFromRule(final String ruleAbsolutePath, final String parameterToDelete)
			throws FileNotFoundException, JargonException {
		log.info("deleteInputParameterFromRule()");

		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (parameterToDelete == null || parameterToDelete.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterToDelete");
		}

		Rule currentRule = loadRuleFromIrods(ruleAbsolutePath);
		log.info("found current rule, recompose by deleting input parameter and reformatting");

		List<IRODSRuleParameter> newParms = new ArrayList<IRODSRuleParameter>();

		boolean updated = false;
		for (int i = 0; i < currentRule.getInputParameters().size(); i++) {
			if (currentRule.getInputParameters().get(i).getUniqueName().equals(parameterToDelete)) {
				log.info("found parameter, deleting");
				updated = true;
			} else {
				newParms.add(currentRule.getInputParameters().get(i));
			}
		}

		if (updated) {
			log.info("updated rule with deleted input param is:{}", currentRule);
			currentRule.setInputParameters(newParms);
			return storeRule(ruleAbsolutePath, currentRule);
		} else {
			log.info("no update necessary, just return the current rule info:{}", currentRule);
			return currentRule;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * deleteOutputParameterFromRule(java.lang.String, java.lang.String)
	 */
	@Override
	public Rule deleteOutputParameterFromRule(final String ruleAbsolutePath, final String parameterToDelete)
			throws FileNotFoundException, JargonException {
		log.info("deleteOutputParameterFromRule()");

		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (parameterToDelete == null || parameterToDelete.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterToDelete");
		}

		Rule currentRule = loadRuleFromIrods(ruleAbsolutePath);
		log.info("found current rule, recompose by deleting output parameter and reformatting");

		List<IRODSRuleParameter> newParms = new ArrayList<IRODSRuleParameter>();

		boolean updated = false;
		for (int i = 0; i < currentRule.getOutputParameters().size(); i++) {
			if (currentRule.getOutputParameters().get(i).getUniqueName().equals(parameterToDelete)) {
				log.info("found parameter, deleting");
				updated = true;
			} else {
				newParms.add(currentRule.getOutputParameters().get(i));
			}
		}

		if (updated) {
			log.info("updated rule with deleted output param is:{}", currentRule);
			currentRule.setOutputParameters(newParms);
			return storeRule(ruleAbsolutePath, currentRule);
		} else {
			log.info("no update necessary, just return the current rule info:{}", currentRule);
			return currentRule;
		}

	}

	public RuleCompositionServiceImpl() {
		super();
	}

	public RuleCompositionServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ruleservice.composition.RuleCompositionService#storeRule
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public Rule storeRule(final String ruleAbsolutePath, final String rule) throws JargonException {

		log.info("storeRule() from String");
		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}

		if (rule == null || rule.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule");
		}

		log.info("ruleAbsolutePath:{}", ruleAbsolutePath);
		log.info("rule:{}", rule);

		log.info("parsing into a Rule");
		Rule parsedRule = parseStringIntoRule(rule);
		log.info("now store it");
		return storeRule(ruleAbsolutePath, parsedRule);
	}

	@Override
	public IRODSRuleExecResult executeRuleAsRawString(final String rule) throws JargonException {

		if (rule == null || rule.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule");
		}

		log.info("rule:{}", rule);

		RuleProcessingAO ruleProcessingAO = irodsAccessObjectFactory.getRuleProcessingAO(getIrodsAccount());

		return ruleProcessingAO.executeRule(rule);

	}

}