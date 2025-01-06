package org.irods.jargon.core.pub;

import static org.irods.jargon.core.packinstr.ExecMyRuleInp.BUF;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.BUF_LEN_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.INT_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.LABEL;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.MY_INT;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.MY_STR;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.STR_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.TYPE;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.ExecMyRuleInp;
import org.irods.jargon.core.packinstr.RuleExecDelInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.domain.DelayedRuleExecution;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter.OutputParamType;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.IrodsRuleFactory;
import org.irods.jargon.core.rule.IrodsRuleInvocationTypeEnum;
import org.irods.jargon.core.rule.JargonRuleException;
import org.irods.jargon.core.rule.RuleEngineInstanceChooser;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.irods.jargon.core.rule.RuleTypeEvaluator;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.core.utils.TagHandlingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Access object that an execute iRODS rules, useful in services as a
 * stand-alone object, and a component used in other Access Objects.
 * <p>
 * NB: for information on changes to the rule engine, please consult:
 * https://www.irods.org/index.php/
 * Changes_and_Improvements_to_the_Rule_Language_and_the_Rule_Engine
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public final class RuleProcessingAOImpl extends IRODSGenericAO implements RuleProcessingAO {

	public static final String LOCAL_PATH = "localPath";

	public static final String KEY_WORD = "keyWord";

	public static final String SVALUE = "svalue";
	public static final String COMMA = ",";

	private static final Logger log = LogManager.getLogger(RuleProcessingAOImpl.class);

	public static final String CL_PUT_ACTION = "CL_PUT_ACTION";
	public static final String CL_GET_ACTION = "CL_GET_ACTION";

	public static final String BIN_BYTES_BUF_PI = "BinBytesBuf_PI";
	public static final String EXEC_CMD_OUT_PI = "ExecCmdOut_PI";
	public static final String DATA_OBJ_INP_PI = "DataObjInp_PI";
	public static final String KEY_VAL_PAIR_PI = "KeyValPair_PI";

	public static final String BUF_LEN = "buflen";
	public static final String OBJ_PATH = "objPath";
	public static final String RULE_EXEC_OUT = "ruleExecOut";
	public static final String RULE_EXEC_ERROR_OUT = "ruleExecErrorOut";

	private static final Object DEST_RESC_NAME = "rescName";

	ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param irodsSession {@link IRODSSession}
	 * @param irodsAccount {@link IRODSAccount}
	 * @throws JargonException for iRODS error
	 */
	protected RuleProcessingAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#executeRuleFromResource(java
	 * .lang.String, java.util.List,
	 * org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType)
	 *
	 * @deprecated use method variant that allows specification of the {@link
	 * RuleInvocationConfiguration}
	 */
	@Deprecated
	@Override
	public IRODSRuleExecResult executeRuleFromResource(final String resourcePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters, final RuleProcessingType ruleProcessingType)
			throws DataNotFoundException, JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}

		log.warn("using default 'AUTO' ruleInvocationConfiguration - consider setting this explicitly");
		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings(getJargonProperties());
		ruleInvocationConfiguration.setRuleProcessingType(ruleProcessingType);

		return executeRuleFromResource(resourcePath, irodsRuleInputParameters, ruleInvocationConfiguration);

	}

	@Override
	public IRODSRuleExecResult executeJsonStyleRule(final String ruleString,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final List<IRODSRuleParameter> irodsRuleContextParameters,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonRuleException, JargonException {

		if (ruleString == null || ruleString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleString");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		if (irodsRuleInputParameters == null) {
			throw new IllegalArgumentException("null irodsRuleInputParameters");
		}

		if (irodsRuleContextParameters == null) {
			throw new IllegalArgumentException("null irodsRuleContextParameters");
		}

		log.info("ruleName:{}", ruleString);
		log.info("irodsRuleInputParameters:{}", irodsRuleInputParameters);
		log.info("irodsRuleContextParameters:{}", irodsRuleContextParameters);
		log.info("ruleInvocationConfiguration:{}", ruleInvocationConfiguration);

		// jsonize the input parameters

		ObjectNode rootNode = mapper.createObjectNode();
		for (IRODSRuleParameter irodsRuleParameter : irodsRuleInputParameters) {
			rootNode.put(irodsRuleParameter.getUniqueName(), irodsRuleParameter.getValueAsStringWithQuotesStripped());
		}

		String inputParamsString = null;

		try {
			inputParamsString = mapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			log.error("unable to parse parameters into json string", e);
			throw new JargonException("json processing exception in rule", e);
		}

		// jsonize context params

		ObjectNode contextNode = mapper.createObjectNode();
		for (IRODSRuleParameter irodsRuleParameter : irodsRuleContextParameters) {
			contextNode.put(irodsRuleParameter.getUniqueName(),
					irodsRuleParameter.getValueAsStringWithQuotesStripped());
		}

		String inputContextString = null;

		try {
			inputContextString = mapper.writeValueAsString(contextNode);
		} catch (JsonProcessingException e) {
			log.error("unable to parse context parameters into json string", e);
			throw new JargonException("json processing exception in rule", e);
		}

		// form the call to the underlying rule

		List<IRODSRuleParameter> finalInputParameters = new ArrayList<IRODSRuleParameter>();
		finalInputParameters.add(new IRODSRuleParameter("*params", replaceAllQuotesInJson(inputParamsString)));
		finalInputParameters.add(new IRODSRuleParameter("*config", replaceAllQuotesInJson(inputContextString)));
		IRODSRuleExecResult ruleExecResult = executeRule(ruleString, finalInputParameters, ruleInvocationConfiguration);
		log.info("ruleExecResult:{}", ruleExecResult);
		return ruleExecResult;
	}

	private String replaceAllQuotesInJson(final String inputString) {
		return inputString.replaceAll("\"", "\\\\\"");
	}

	@Override
	public IRODSRuleExecResult executeRuleFromResource(final String resourcePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final RuleInvocationConfiguration ruleInvocationConfiguration)
			throws DataNotFoundException, JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		// local copy as I may alter the configuration

		RuleInvocationConfiguration copiedRuleInvocationConfiguration = ruleInvocationConfiguration
				.copyRuleInvocationConfiguration(ruleInvocationConfiguration);
		if (copiedRuleInvocationConfiguration
				.getIrodsRuleInvocationTypeEnum() == IrodsRuleInvocationTypeEnum.AUTO_DETECT) {
			log.info("auto-detecting the rule type via the file path");
			RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
			IrodsRuleInvocationTypeEnum typeEnum = ruleTypeEvaluator.detectTypeFromExtension(resourcePath);
			log.info("evaluated type to be:{}", typeEnum);
			if (typeEnum != null) {
				copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(typeEnum);
			}
			/*
			 * if it didn't decide the type it can still try and evaluate by looking at the
			 * rule text or annotation when it delegates to execute with the ruleString
			 */
		}

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(resourcePath);

		// rule is now a string, run it

		return executeRule(ruleString, irodsRuleInputParameters, copiedRuleInvocationConfiguration);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#executeRuleFromIRODSFile(java
	 * .lang.String, java.util.List)
	 *
	 * @deprecated use method variant that allows specification of the {@link
	 * RuleInvocationConfiguration}
	 */
	@Override
	@Deprecated
	public IRODSRuleExecResult executeRuleFromIRODSFile(final String ruleFileAbsolutePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters, final RuleProcessingType ruleProcessingType)
			throws JargonException {

		if (ruleFileAbsolutePath == null || ruleFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleFileAbsolutePath");
		}

		log.warn("using default 'AUTO' ruleInvocationConfiguration - consider setting this explicitly");
		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings(getJargonProperties());
		ruleInvocationConfiguration.setRuleProcessingType(ruleProcessingType);
		return executeRuleFromIRODSFile(ruleFileAbsolutePath, irodsRuleInputParameters, ruleInvocationConfiguration);

	}

	@Override
	public IRODSRuleExecResult executeRuleFromIRODSFile(final String ruleFileAbsolutePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonException {

		if (ruleFileAbsolutePath == null || ruleFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleFileAbsolutePath");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		// local copy as I may alter the configuration

		RuleInvocationConfiguration copiedRuleInvocationConfiguration = ruleInvocationConfiguration
				.copyRuleInvocationConfiguration(ruleInvocationConfiguration);
		if (copiedRuleInvocationConfiguration
				.getIrodsRuleInvocationTypeEnum() == IrodsRuleInvocationTypeEnum.AUTO_DETECT) {
			log.info("auto-detecting the rule type via the file path");
			RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
			IrodsRuleInvocationTypeEnum typeEnum = ruleTypeEvaluator.detectTypeFromExtension(ruleFileAbsolutePath);
			log.info("evaluated type to be:{}", typeEnum);
			if (typeEnum != null) {
				copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(typeEnum);
			}
			/*
			 * if it didn't decide the type it can still try and evaluate by looking at the
			 * rule text or annotation when it delegates to execute with the ruleString
			 */
		}

		IRODSFileReader irodsFileReader = getIRODSFileFactory().instanceIRODSFileReader(ruleFileAbsolutePath);

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

		// rule is now a string, run it

		return executeRule(ruleString, irodsRuleInputParameters, copiedRuleInvocationConfiguration);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#executeRule(java.lang.String)
	 *
	 * TODO: deprecate and add context method
	 *
	 */
	@Override
	public IRODSRuleExecResult executeRule(final String irodsRuleAsString) throws JargonRuleException, JargonException {

		log.info("executing rule: {}", irodsRuleAsString);
		log.warn("using default 'AUTO' ruleInvocationConfiguration - consider setting this explicitly");
		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings(getJargonProperties());

		return executeRule(irodsRuleAsString, ruleInvocationConfiguration);
	}

	public IRODSRuleExecResult executeRule(final String irodsRuleAsString,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonRuleException, JargonException {

		log.info("executeRule()");
		if (irodsRuleAsString == null || irodsRuleAsString.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsRuleAsString");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration()");
		}

		return executeRule(irodsRuleAsString, null, ruleInvocationConfiguration);
	}

	@Override
	public List<String> listAvailableRuleEngines() throws JargonException, UnsupportedOperationException {
		log.info("listAvailableRuleEngines()");
		EnvironmentalInfoAO environmentalInfoAO = this.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(this.getIRODSAccount());

		if (!environmentalInfoAO.getIRODSServerProperties().isAtLeastIrods420()) {
			log.info("cannot support this operation based on current version");
			throw new UnsupportedOperationException("cannot support multiple rule engines");
		}

		ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instanceForListAvailableRuleEngines();
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		log.debug("response from rule exec: {}", response.parseTag());
		/*
		 * For some reason this operation returns the list of rule engines as an error
		 * response, so that tag is shoved back into the response and I will need to
		 * parse it here
		 */
		if (!response.getName().equals("RErrMsg_PI")) {
			log.error("did not get RErrMsg_PI from list rule engines");
			throw new JargonRuntimeException("unexpected protocol response encountered when listing rule engines");
		}

		String message = response.getTag("msg").getStringValue();
		log.info("rule listing tag response:{}", message);
		List<String> ruleEngines = new ArrayList<String>();

		String[] split = message.split("\n");

		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				ruleEngines.add(split[i].trim());
			}
		}

		log.info("rule engines:{}", ruleEngines);
		return ruleEngines;
	}

	@Override
	public IRODSRuleExecResult executeRule(final String irodsRuleAsString,
			final List<IRODSRuleParameter> inputParameterOverrides,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonRuleException, JargonException {
		log.info("executeRule()");
		if (irodsRuleAsString == null || irodsRuleAsString.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsRuleAsString");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		// tolerate null inputParameterOverrides

		log.info("executing rule: {}", irodsRuleAsString);
		log.info("with configuration:{}", ruleInvocationConfiguration);
		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(getIRODSAccessObjectFactory(), getIRODSAccount());
		final IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(irodsRuleAsString, inputParameterOverrides,
				ruleInvocationConfiguration);
		log.debug("translated rule: {}", irodsRule);

		log.debug("decorating the rule with the appropriate rule engine instance");
		RuleEngineInstanceChooser ruleEngineInstanceChooser = new RuleEngineInstanceChooser(getJargonProperties(),
				getIRODSServerProperties());
		ruleEngineInstanceChooser.decorateRuleInvocationConfugurationWithRuleEngineInstance(irodsRule);

		final ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instance(irodsRule);
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		log.debug("response from rule exec: {}", response.parseTag());

		IRODSRuleExecResult irodsRuleExecResult = processRuleResult(response, irodsRule);

		log.debug("processing end of rule execution by reading message");

		return irodsRuleExecResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#executeRule(java.lang.String,
	 * java.util.List,
	 * org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType)
	 *
	 * TODO: deprecate and add rule context invocation
	 */
	@Override
	public IRODSRuleExecResult executeRule(final String irodsRuleAsString,
			final List<IRODSRuleParameter> inputParameterOverrides, final RuleProcessingType ruleProcessingType)
			throws JargonRuleException, JargonException {

		log.info("executeRule() with a default of AUTO_DETECT");
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);
		ruleInvocationConfiguration.setRuleProcessingType(ruleProcessingType);
		return executeRule(irodsRuleAsString, inputParameterOverrides, ruleInvocationConfiguration);

	}

	private IRODSRuleExecResult processRuleResult(final Tag irodsRuleResult, final IRODSRule irodsRule)
			throws JargonRuleException, JargonException {

		if (irodsRule == null) {
			throw new JargonException("irodsRule is null");
		}

		if (irodsRuleResult == null) {
			log.debug("rule result was null, return empty output parameter map");
			final IRODSRuleExecResult ruleResult = IRODSRuleExecResult.instance(irodsRule,
					new HashMap<String, IRODSRuleExecResultOutputParameter>());
			return ruleResult;
		}

		Map<String, IRODSRuleExecResultOutputParameter> outputParameters = new HashMap<String, IRODSRuleExecResultOutputParameter>();

		// if result was null, it was returned as an empty response, otherwise,
		// I proceed to parse out the result

		/*
		 * I will loop through and accumulate output parameters while I encounter client
		 * side actions, and then I will return the accumulated set to the caller after
		 * sending an op complete
		 */

		int parametersLength = irodsRuleResult.getTag(IRODSConstants.paramLen).getIntValue();

		log.debug("I have {} parameters from the rule", parametersLength);

		boolean wasClientAction = processIndividualParameters(irodsRuleResult, parametersLength, outputParameters);

		/*
		 * Rule processing may produce multiple output messges. These messages may
		 * include commands to do client-side operations, such as gets and puts. The
		 * gets and puts go through the same code paths as those in
		 * DataTransferOperations, with special flags that allow smooth operation in
		 * this special use case.
		 *
		 * Note that 'operation complete' messages are sent when certain steps are
		 * encountered, such as client side actions. Below, Jargon accounts for the
		 * potential multiple read/write sequences that occur to process each step in
		 * the rule execution, looking for more output parameters in the form of client
		 * side actions, logged messages, or other output parameters.
		 *
		 *
		 * The loop will keep processing until all client side actions are complete.
		 */

		while (wasClientAction) {
			log.info("get additional information for subsequent responses");

			Tag subsequentResultTag = operationComplete(0);

			/*
			 * Per comment above, read and discard intermediate status protocol messages,
			 * looking for more parms
			 */

			if (subsequentResultTag == null) {
				subsequentResultTag = getIRODSProtocol().readMessage();
			}

			if (subsequentResultTag == null) {
				break;
			}

			/*
			 * this read may be another client side op, if there are parameters in this tag,
			 * then don't read again, iRODS is a bit funny about this, and doesn't seem
			 * consistent, so I'm taking a flexible approach to processing tags in this
			 * sequence
			 */

			parametersLength = subsequentResultTag.getTag(IRODSConstants.paramLen).getIntValue();
			log.debug("I have {} parameters from subsequent rule messages", parametersLength);
			wasClientAction = processIndividualParameters(subsequentResultTag, parametersLength, outputParameters);
		}

		IRODSRuleExecResult irodsRuleExecResult = IRODSRuleExecResult.instance(irodsRule, outputParameters);

		log.info("execute result: {}", irodsRuleExecResult);
		return irodsRuleExecResult;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#purgeRuleFromDelayedExecQueue
	 * (int)
	 */
	@Override
	public void purgeRuleFromDelayedExecQueue(final int queueId) throws JargonException {
		log.info("purgeRuleFromDelayedExecQueue()");
		RuleExecDelInp ruleExecDelInp = null;
		log.info("deleting rule with id:{}", queueId);
		ruleExecDelInp = RuleExecDelInp.instanceForDeleteRule(String.valueOf(queueId));
		getIRODSProtocol().irodsFunction(ruleExecDelInp);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#purgeAllDelayedExecQueue()
	 */
	@Override
	public int purgeAllDelayedExecQueue() throws JargonException {
		int numberPurged = 0;

		log.info("purgeAllDelayedExecQueue");

		List<DelayedRuleExecution> delayedRuleExecutions = listAllDelayedRuleExecutions(0);
		RuleExecDelInp ruleExecDelInp = null;

		for (DelayedRuleExecution delayedRuleExecution : delayedRuleExecutions) {
			log.info("deleting rule with id:{}", delayedRuleExecution.getId());
			ruleExecDelInp = RuleExecDelInp.instanceForDeleteRule(String.valueOf(delayedRuleExecution.getId()));
			getIRODSProtocol().irodsFunction(ruleExecDelInp);
			numberPurged++;
		}

		return numberPurged;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RuleProcessingAO#listAllDelayedRuleExecutions
	 * (int)
	 */
	@Override
	public List<DelayedRuleExecution> listAllDelayedRuleExecutions(final int partialStartIndex) throws JargonException {

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("partialStartIndex must be 0 or greater");
		}

		log.info("listAllDelayedRuleExecutions() with partial start of {}", partialStartIndex);

		List<DelayedRuleExecution> delayedRuleExecutions = new ArrayList<DelayedRuleExecution>();

		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_ID.getName());
		sb.append(COMMA);

		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_USER_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_ADDRESS.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_FREQUENCY.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_PRIORITY.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_ESTIMATED_EXE_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_NOTIFICATION_ADDR.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_LAST_EXE_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_RULE_EXEC_STATUS.getName());

		final String query = sb.toString();
		log.debug("query for rule exec status:{}", query);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 5000);
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query: {}", query, e);
			throw new JargonException("error in query for rule execution status", e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			delayedRuleExecutions.add(buildDelayedRuleExecutionFromResultRow(row));
		}

		return delayedRuleExecutions;

	}

	private DelayedRuleExecution buildDelayedRuleExecutionFromResultRow(final IRODSQueryResultRow row)
			throws JargonException {

		DelayedRuleExecution dre = new DelayedRuleExecution();
		dre.setId(Integer.parseInt(row.getColumn(0)));
		dre.setName(row.getColumn(1));
		dre.setUserName(row.getColumn(2));
		dre.setAddress(row.getColumn(3));
		dre.setExecTime(row.getColumn(4));
		dre.setFrequency(row.getColumn(5));
		dre.setPriority(row.getColumn(6));
		dre.setEstimatedExecTime(row.getColumn(7));
		dre.setNotificationAddress(row.getColumn(8));
		dre.setLastExecTime(row.getColumn(9));
		dre.setExecStatus(row.getColumn(10));

		// add info to track position in records for possible requery
		dre.setLastResult(row.isLastResult());
		dre.setCount(row.getRecordCount());

		if (log.isInfoEnabled()) {
			log.info("built delayed rule execution built \n");
			log.info(dre.toString());
		}

		return dre;

	}

	/**
	 * Process a response from a rule invocation, accumulate any output parameters
	 * encountered, then
	 *
	 * @param rulesTag                  {@link Tag} with rule
	 * @param parametersLength          {@code int}
	 * @param irodsRuleOutputParameters {@code Map} of
	 *                                  {@link IRODSRuleExecResultOutputParameter}
	 *                                  with overrides
	 * @return {@code boolean} if the param is a client action (like a client-side
	 *         get or put)
	 * @throws JargonException for iRODS error
	 */
	private boolean processIndividualParameters(final Tag rulesTag, final int parametersLength,
			final Map<String, IRODSRuleExecResultOutputParameter> irodsRuleOutputParameters) throws JargonException {

		String label;
		String type;
		Object value;
		boolean wasClientAction = false;

		for (int i = 0; i < parametersLength; i++) {
			Tag msParam = rulesTag.getTag(IRODSConstants.MsParam_PI, i);
			label = msParam.getTag(LABEL).getStringValue();
			type = msParam.getTag(TYPE).getStringValue();
			value = getParameter(type, msParam);
			// need to differentiate tag that is client request from
			// tag that is an array

			log.debug("rule parameter label: {}", label);
			log.debug("parm type: {}", type);
			log.debug("value: {}", value);

			if (label.equals(CL_PUT_ACTION) || label.equals(CL_GET_ACTION)) {
				// for recording a client side action
				wasClientAction = true;
				irodsRuleOutputParameters.put(label,
						(processRuleResponseWithClientSideActionTag(label, type, value, msParam)));

			} else if (label.equals(RULE_EXEC_OUT)) {
				irodsRuleOutputParameters.putAll(extractStringFromExecCmdOut(msParam));
			} else {
				irodsRuleOutputParameters.put(label, (processRuleResponseTag(label, type, value, msParam)));
			}
		}

		log.info("rule operation complete");
		return wasClientAction;

	}

	private IRODSRuleExecResultOutputParameter processRuleResponseTag(final String label, final String type,
			final Object value, final Tag msParam) throws JargonException {

		log.debug("processing output parameter for label: {}", label);
		log.debug("type: {}", type);
		log.debug("value: {}", value);
		log.debug("tag: {}", msParam.getStringValue());

		return IRODSRuleExecResultOutputParameter.instance(label,
				IRODSRuleExecResultOutputParameter.OutputParamType.STRING, value);
	}

	/**
	 * @return the parameter value of the parameter tag. Other values, like buffer
	 *         length, can be derived from it, if the type is known.
	 * @throws JargonExceptionIRODSRuleExecResultOutputParameter
	 */
	private Object getParameter(final String type, final Tag parameterTag) throws JargonException {
		if (type.equals(INT_PI)) {
			return parameterTag.getTag(INT_PI).getTag(MY_INT).getIntValue();
		} else if (type.equals(BUF_LEN_PI)) {
			return parameterTag.getTag(BIN_BYTES_BUF_PI).getTag(BUF).getValue();
		} else if (type.equals(STR_PI)) {
			return parameterTag.getTag(STR_PI).getTag(MY_STR).getStringValue();
		} else {
			return parameterTag.getTag(type);
		}
	}

	private Map<String, IRODSRuleExecResultOutputParameter> extractStringFromExecCmdOut(final Tag parameterTag)
			throws JargonException {

		Map<String, IRODSRuleExecResultOutputParameter> resultMap = new HashMap<String, IRODSRuleExecResultOutputParameter>();

		Tag exec = parameterTag.getTag(EXEC_CMD_OUT_PI);

		// there should be 3 tags, 0 is buf, 1 is empty, 2 is status
		if (exec.getLength() != 3) {
			throw new JargonException("expected 3 tags in ExecCmdOut_PI tag set");
		}

		// check status
		int status = exec.getTag("status").getIntValue();

		log.debug("status of exec: {}", status);

		if (status != 0) {
			throw new JargonException("invalid status in response ExecCmdOut_PI tag:" + status);
		}

		String result;
		Tag bufTag = exec.getTag(BIN_BYTES_BUF_PI, 0).getTag(BUF);
		if (bufTag != null) {
			String base64Encoded = bufTag.getStringValue();
			byte[] decoded = Base64.fromString(base64Encoded);
			String buf = new String(decoded);
			int zeroIdx = buf.indexOf('\0');
			if (zeroIdx == -1) {
				result = buf;
			} else {
				result = buf.substring(0, zeroIdx);
			}
		} else {
			result = "";
		}

		IRODSRuleExecResultOutputParameter param = IRODSRuleExecResultOutputParameter.instance(RULE_EXEC_OUT,
				OutputParamType.STRING, result);
		resultMap.put(RULE_EXEC_OUT, param);

		Tag errTag = exec.getTag(BIN_BYTES_BUF_PI, 1).getTag(BUF);
		if (errTag != null) {
			String base64Encoded = errTag.getStringValue();
			byte[] decoded = Base64.fromString(base64Encoded);
			String buf = new String(decoded);
			int zeroIdx = buf.indexOf('\0');
			if (zeroIdx == -1) {
				result = buf;
			} else {
				result = buf.substring(0, zeroIdx);
			}
		} else {
			result = "";
		}

		param = IRODSRuleExecResultOutputParameter.instance(RULE_EXEC_ERROR_OUT, OutputParamType.STRING, result);
		resultMap.put(RULE_EXEC_ERROR_OUT, param);

		return resultMap;
	}

	/*
	 * retained note on this method... should check intInfo if ==
	 * SYS_SVR_TO_CLI_MSI_REQUEST 99999995 /*lib/core/include/rodsDef.h <p> this is
	 * the return value for the rcExecMyRule call indicating theserver is requesting
	 * the client to client to perform certain task <p> #define
	 * SYS_SVR_TO_CLI_MSI_REQUEST 99999995 <p> #define SYS_SVR_TO_CLI_COLL_STAT
	 * 99999996 <p> #define SYS_CLI_TO_SVR_COLL_STAT_REPLY 99999997 <p> definition
	 * for iRods server to client action request from a microservice. These
	 * definitions are put in the "label" field of MsParam <p> #define CL_PUT_ACTION
	 * "CL_PUT_ACTION" #define CL_GET_ACTION "CL_GET_ACTION" #define CL_ZONE_OPR_INX
	 * "CL_ZONE_OPR_INX"
	 */

	private IRODSRuleExecResultOutputParameter processRuleResponseWithClientSideActionTag(final String label,
			final String type, final Object value, final Tag msParam) throws JargonException {

		if (label.equals(CL_PUT_ACTION) || label.equals(CL_GET_ACTION)) {
			// ok
		} else {
			throw new JargonException("this is not a client side action");
		}

		// server is requesting client action
		Tag fileAction = msParam.getTag(type);

		String irodsFileAbsolutePath = fileAction.getTag(OBJ_PATH).getStringValue();
		log.info("client side action - irods file absolute path: {}", irodsFileAbsolutePath);

		int numThreads = fileAction.getTag("numThreads").getIntValue();
		log.info("client side action - num threads: {}", numThreads);

		Tag kvp = fileAction.getTag(KEY_VAL_PAIR_PI);
		Map<String, String> kvpMap = TagHandlingUtils.translateKeyValuePairTagIntoMap(kvp);
		log.debug("kvp map is:{}", kvpMap);

		String localPath = kvpMap.get(LOCAL_PATH);

		if (localPath == null) {
			log.error("no local file path found in tags");
			throw new JargonException("client side action indicated, but no localPath in tag");
		}

		String resourceName = kvpMap.get(DEST_RESC_NAME);
		if (resourceName == null) {
			resourceName = kvpMap.get("destRescName");
			if (resourceName == null) {
				resourceName = "";
			}
		}

		log.info("resourceName:{}", resourceName);

		String forceValue = kvpMap.get("forceFlag");
		boolean force = false;
		if (forceValue != null) {
			log.info("get will use force option");
			force = true;
		}

		String replNum = kvpMap.get("replNum");
		if (replNum != null) {
			log.info("get will fetch replica : {}", replNum);
		} else {
			replNum = "";
		}

		log.debug("getting reference to local file");

		File localFile = new File(localPath);

		if (label.equals(CL_GET_ACTION)) {
			clientSideGetAction(irodsFileAbsolutePath, localFile, resourceName, replNum, force, numThreads);
		} else if (label.equals(CL_PUT_ACTION)) {
			clientSidePutAction(irodsFileAbsolutePath, localFile, resourceName, force, numThreads);
		}

		StringBuilder putLabel = new StringBuilder();
		putLabel.append(label);
		putLabel.append('-');
		putLabel.append(irodsFileAbsolutePath);
		String labelValForAction = putLabel.toString();

		return IRODSRuleExecResultOutputParameter.instance(labelValForAction,
				IRODSRuleExecResultOutputParameter.OutputParamType.CLIENT_ACTION_RESULT, localPath);
	}

	private void clientSidePutAction(final String irodsFileAbsolutePath, final File localFile,
			final String resourceName, final boolean force, final int nbrThreads) throws JargonException {
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) getIRODSAccessObjectFactory()
				.getDataObjectAO(getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(irodsFileAbsolutePath);
		irodsFile.setResource(resourceName);
		log.debug("performing put of file");
		TransferControlBlock transferControlBlock = buildDefaultTransferControlBlockBasedOnJargonProperties();
		if (force) {
			transferControlBlock.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		} else {
			transferControlBlock.getTransferOptions().setForceOption(ForceOption.NO_FORCE);
		}

		if (nbrThreads == -1) {
			log.debug("override to no parallel threads for this transfer");
			transferControlBlock.getTransferOptions().setUseParallelTransfer(false);
		}

		transferControlBlock.getTransferOptions().setMaxThreads(nbrThreads);

		dataObjectAO.putLocalDataObjectToIRODSForClientSideRuleOperation(localFile, irodsFile, transferControlBlock);
		log.debug("client side put action was successful");

	}

	private void clientSideGetAction(final String irodsFileAbsolutePath, final File localFile,
			final String resourceName, final String replNum, final boolean force, final int nbrThreads)
			throws JargonException, DataNotFoundException {

		log.info("client-side get action");

		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) getIRODSAccessObjectFactory()
				.getDataObjectAO(getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO.instanceIRODSFileForPath(irodsFileAbsolutePath);
		irodsFile.setResource(resourceName);
		log.info("performing get of file");
		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();
		transferOptions.setClientSideRuleAction(true);
		if (force) {
			transferOptions.setForceOption(ForceOption.USE_FORCE);
		} else {
			transferOptions.setForceOption(ForceOption.NO_FORCE);
		}

		if (nbrThreads == -1) {
			log.debug("override to no parallel threads for this transfer");
			transferOptions.setUseParallelTransfer(false);
		}

		transferOptions.setMaxThreads(nbrThreads);

		dataObjectAO.irodsDataObjectGetOperationForClientSideAction(irodsFile, localFile, replNum, transferOptions);

		log.debug("client side get action was successful");

	}

}