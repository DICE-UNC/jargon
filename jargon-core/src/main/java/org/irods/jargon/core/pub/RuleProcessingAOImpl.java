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
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.IRODSRuleTranslator;
import org.irods.jargon.core.rule.JargonRuleException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.core.utils.TagHandlingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object that an execute iRODS rules, useful in services as a
 * stand-alone object, and a component used in other Access Objects.
 * <p/>
 * NB: for information on changes to the rule engine, please consult:
 * https://www.irods.org/index.php/
 * Changes_and_Improvements_to_the_Rule_Language_and_the_Rule_Engine
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public final class RuleProcessingAOImpl extends IRODSGenericAO implements
		RuleProcessingAO {

	public static final String LOCAL_PATH = "localPath";

	public static final String KEY_WORD = "keyWord";

	public static final String SVALUE = "svalue";
	public static final String COMMA = ",";

	private static final Logger log = LoggerFactory
			.getLogger(RuleProcessingAOImpl.class);

	public static final String CL_PUT_ACTION = "CL_PUT_ACTION";
	public static final String CL_GET_ACTION = "CL_GET_ACTION";

	public static final String BIN_BYTES_BUF_PI = "BinBytesBuf_PI";
	public static final String EXEC_CMD_OUT_PI = "ExecCmdOut_PI";
	public static final String DATA_OBJ_INP_PI = "DataObjInp_PI";
	public static final String KEY_VAL_PAIR_PI = "KeyValPair_PI";

	public static final String BUF_LEN = "buflen";
	public static final String OBJ_PATH = "objPath";
	public static final String RULE_EXEC_OUT = "ruleExecOut";

	private static final Object DEST_RESC_NAME = "rescName";

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected RuleProcessingAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#executeRuleFromResource(java
	 * .lang.String, java.util.List,
	 * org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType)
	 */
	@Override
	public IRODSRuleExecResult executeRuleFromResource(
			final String resourcePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final RuleProcessingType ruleProcessingType)
			throws DataNotFoundException, JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(resourcePath);

		// rule is now a string, run it

		return executeRule(ruleString, irodsRuleInputParameters,
				ruleProcessingType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#executeRuleFromIRODSFile(java
	 * .lang.String, java.util.List)
	 */
	@Override
	public IRODSRuleExecResult executeRuleFromIRODSFile(
			final String ruleFileAbsolutePath,
			final List<IRODSRuleParameter> irodsRuleInputParameters,
			final RuleProcessingType ruleProcessingType) throws JargonException {

		if (ruleFileAbsolutePath == null || ruleFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty ruleFileAbsolutePath");
		}

		IRODSFileReader irodsFileReader = this.getIRODSFileFactory()
				.instanceIRODSFileReader(ruleFileAbsolutePath);

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

		return executeRule(ruleString, irodsRuleInputParameters,
				ruleProcessingType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#executeRule(java.lang.String)
	 */
	@Override
	public IRODSRuleExecResult executeRule(String irodsRuleAsString)
			throws JargonRuleException, JargonException {

		log.info("executing rule: {}", irodsRuleAsString);
		final IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				this.getIRODSServerProperties());

		/*
		 * if iRODS 3.0+, add the @external parameter to the rule body for new
		 * style rules
		 */

		if (this.getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")
				&& IRODSRuleTranslator.isUsingNewRuleSyntax(irodsRuleAsString)) {

			log.debug("adding @external to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@external\n");
			bodyWithExtern.append(irodsRuleAsString);
			irodsRuleAsString = bodyWithExtern.toString();
		}

		final IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(irodsRuleAsString);
		log.debug("translated rule: {}", irodsRule);
		final ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instance(irodsRule);
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		log.debug("response from rule exec: {}", response.parseTag());

		IRODSRuleExecResult irodsRuleExecResult = processRuleResult(response,
				irodsRule);
		log.debug("processing end of rule execution by reading message");

		return irodsRuleExecResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#executeRule(java.lang.String,
	 * java.util.List,
	 * org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType)
	 */
	@Override
	public IRODSRuleExecResult executeRule(String irodsRuleAsString,
			final List<IRODSRuleParameter> inputParameterOverrides,
			final RuleProcessingType ruleProcessingType)
			throws JargonRuleException, JargonException {

		if (irodsRuleAsString == null || irodsRuleAsString.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsRuleAsString");
		}

		if (ruleProcessingType == null) {
			throw new IllegalArgumentException("null ruleProcessingType");
		}

		// tolerate null inputParameterOverrides

		log.info("executing rule: {}", irodsRuleAsString);
		final IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				this.getIRODSServerProperties());

		/*
		 * if iRODS 3.0+, add the @external parameter to the rule body for new
		 * style rules
		 */

		if (this.getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")
				&& IRODSRuleTranslator.isUsingNewRuleSyntax(irodsRuleAsString)) {

			if (ruleProcessingType == RuleProcessingType.CLASSIC) {
				throw new JargonRuleException(
						"cannot run new format rule as CLASSIC");
			}

			// ok
			log.info("verified as new format");
		} else {
			if (ruleProcessingType != RuleProcessingType.CLASSIC) {
				throw new JargonRuleException(
						"must run old format rule as CLASSIC");
			}
		}

		if (ruleProcessingType == RuleProcessingType.CLASSIC) {
			log.debug("classic, do not add external or internal");
		} else if (ruleProcessingType == RuleProcessingType.INTERNAL) {
			log.debug("adding @internal to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@internal\n");
			bodyWithExtern.append(irodsRuleAsString);
			irodsRuleAsString = bodyWithExtern.toString();
		} else {
			log.debug("adding @external to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@external\n");
			bodyWithExtern.append(irodsRuleAsString);
			irodsRuleAsString = bodyWithExtern.toString();
		}

		final IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(irodsRuleAsString,
						inputParameterOverrides);
		log.debug("translated rule: {}", irodsRule);
		final ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instance(irodsRule);
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		log.debug("response from rule exec: {}", response.parseTag());

		IRODSRuleExecResult irodsRuleExecResult = processRuleResult(response,
				irodsRule);
		log.debug("processing end of rule execution by reading message");

		return irodsRuleExecResult;
	}

	private IRODSRuleExecResult processRuleResult(final Tag irodsRuleResult,
			final IRODSRule irodsRule) throws JargonRuleException,
			JargonException {

		if (irodsRule == null) {
			throw new JargonException("irodsRule is nul");
		}

		if (irodsRuleResult == null) {
			log.debug("rule result was null, return empty output parameter map");
			final IRODSRuleExecResult ruleResult = IRODSRuleExecResult
					.instance(
							irodsRule,
							new HashMap<String, IRODSRuleExecResultOutputParameter>());
			return ruleResult;
		}

		// if result was null, it was returned as an empty response, otherwise,
		// I proceed to parse out the result

		int parametersLength = irodsRuleResult.getTag(IRODSConstants.paramLen)
				.getIntValue();

		log.debug("I have {} parameters from the rule", parametersLength);

		Map<String, IRODSRuleExecResultOutputParameter> outputParameters = processIndividualParameters(
				irodsRuleResult, parametersLength);
		IRODSRuleExecResult irodsRuleExecResult = IRODSRuleExecResult.instance(
				irodsRule, outputParameters);

		log.info("execute result: {}", irodsRuleExecResult);

		return irodsRuleExecResult;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#purgeAllDelayedExecQueue()
	 */
	@Override
	public int purgeAllDelayedExecQueue() throws JargonException {
		int numberPurged = 0;

		log.info("purgeAllDelayedExecQueue");

		List<DelayedRuleExecution> delayedRuleExecutions = listAllDelayedRuleExecutions(0);
		RuleExecDelInp ruleExecDelInp = null;

		for (DelayedRuleExecution delayedRuleExecution : delayedRuleExecutions) {
			log.info("deleting rule with id:{}", delayedRuleExecution.getId());
			ruleExecDelInp = RuleExecDelInp.instanceForDeleteRule(String
					.valueOf(delayedRuleExecution.getId()));
			getIRODSProtocol().irodsFunction(ruleExecDelInp);
			numberPurged++;
		}

		return numberPurged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.RuleProcessingAO#listAllDelayedRuleExecutions
	 * (int)
	 */
	@Override
	public List<DelayedRuleExecution> listAllDelayedRuleExecutions(
			final int partialStartIndex) throws JargonException {

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partialStartIndex must be 0 or greater");
		}

		log.info("listAllDelayedRuleExecutions() with partial start of {}",
				partialStartIndex);

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
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query: {}", query, e);
			throw new JargonException(
					"error in query for rule execution status", e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			delayedRuleExecutions
					.add(buildDelayedRuleExecutionFromResultRow(row));
		}

		return delayedRuleExecutions;

	}

	private DelayedRuleExecution buildDelayedRuleExecutionFromResultRow(
			final IRODSQueryResultRow row) throws JargonException {

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
	 * @param parametersLength
	 * @return
	 */
	private Map<String, IRODSRuleExecResultOutputParameter> processIndividualParameters(
			final Tag rulesTag, final int parametersLength)
			throws JargonException {

		String label;
		String type;
		Object value;

		Map<String, IRODSRuleExecResultOutputParameter> irodsRuleOutputParameters = new HashMap<String, IRODSRuleExecResultOutputParameter>();

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

			if (label.equals(CL_PUT_ACTION) || label.equals("CL_GET_ACTION")) {
				// for recording a client side action
				irodsRuleOutputParameters.put(
						label,
						(processRuleResponseWithClientSideActionTag(label,
								type, value, msParam)));
			} else {
				irodsRuleOutputParameters.put(label,
						(processRuleResponseTag(label, type, value, msParam)));
			}
		}

		log.info("rule operation complete");
		return irodsRuleOutputParameters;

	}

	private IRODSRuleExecResultOutputParameter processRuleResponseTag(
			final String label, final String type, final Object value,
			final Tag msParam) throws JargonException {

		log.debug("processing output parameter for label: {}", label);
		log.debug("type: {}", type);
		log.debug("value: {}", value);
		log.debug("tag: {}", msParam.getStringValue());

		// FIXME: mcc, check out the intent here
		if (type.equals(EXEC_CMD_OUT_PI)) {
		}

		return IRODSRuleExecResultOutputParameter.instance(label,
				IRODSRuleExecResultOutputParameter.OutputParamType.STRING,
				value);

	}

	/**
	 * @return the parameter value of the parameter tag. Other values, like
	 *         buffer length, can be derived from it, if the type is known.
	 * @throws JargonException
	 */
	private Object getParameter(final String type, final Tag parameterTag)
			throws JargonException {
		if (type.equals(INT_PI)) {
			return parameterTag.getTag(INT_PI).getTag(MY_INT).getIntValue();
		} else if (type.equals(BUF_LEN_PI)) {
			return parameterTag.getTag(BIN_BYTES_BUF_PI).getTag(BUF).getValue();
		} else if (type.equals(EXEC_CMD_OUT_PI)) {
			return extractStringFromExecCmdOut(parameterTag);
		} else if (type.equals(STR_PI)) {
			return parameterTag.getTag(STR_PI).getTag(MY_STR).getStringValue();
		} else {
			return parameterTag.getTag(type);
		}
	}

	/**
	 * @param parameterTag
	 * @return
	 * @throws JargonException
	 */
	private Object extractStringFromExecCmdOut(final Tag parameterTag)
			throws JargonException {
		Tag exec = parameterTag.getTag(EXEC_CMD_OUT_PI);

		// there should be 3 tags, 0 is buf, 1 is empty, 2 is status
		if (exec.getLength() != 3) {
			throw new JargonException(
					"expected 3 tags in ExecCmdOut_PI tag set");
		}

		// check status
		int status = exec.getTag("status").getIntValue();

		log.debug("status of exec: {}", status);

		if (status != 0) {
			throw new JargonException(
					"invalid status in response ExecCmdOut_PI tag:" + status);
		}

		Tag bufTag = exec.getTag(BIN_BYTES_BUF_PI, 0).getTag(BUF);

		String results;

		if (bufTag != null) {
			String base64Encoded = bufTag.getStringValue();

			byte[] decoded = Base64.fromString(base64Encoded);
			results = new String(decoded);
		} else {
			results = "";
		}

		return results;
	}

	/**
	 * @param fileSystem
	 * @param label
	 * @param type
	 * @param value
	 * @param msParam
	 * @return
	 * @throws IOException
	 * 
	 *             retained note on this method... should check intInfo if ==
	 *             SYS_SVR_TO_CLI_MSI_REQUEST 99999995
	 *             /*lib/core/include/rodsDef.h
	 * 
	 *             // this is the return value for the rcExecMyRule call
	 *             indicating the // server is requesting the client to client
	 *             to perform certain task #define SYS_SVR_TO_CLI_MSI_REQUEST
	 *             99999995 #define SYS_SVR_TO_CLI_COLL_STAT 99999996 #define
	 *             SYS_CLI_TO_SVR_COLL_STAT_REPLY 99999997
	 * 
	 *             // definition for iRods server to client action request from
	 *             a microservice. // these definitions are put in the "label"
	 *             field of MsParam
	 * 
	 *             #define CL_PUT_ACTION "CL_PUT_ACTION" #define CL_GET_ACTION
	 *             "CL_GET_ACTION" #define CL_ZONE_OPR_INX "CL_ZONE_OPR_INX"
	 */

	private IRODSRuleExecResultOutputParameter processRuleResponseWithClientSideActionTag(
			final String label, final String type, final Object value,
			final Tag msParam) throws JargonException {

		if (label.equals(CL_PUT_ACTION) || label.equals(CL_GET_ACTION)) {
			// ok
		} else {
			throw new JargonException("this is not a client side action");
		}

		// server is requesting client action
		Tag fileAction = msParam.getTag(type);

		String irodsFileAbsolutePath = fileAction.getTag(OBJ_PATH)
				.getStringValue();
		log.info("client side action - irods file absolute path: {}",
				irodsFileAbsolutePath);

		int numThreads = fileAction.getTag("numThreads").getIntValue();
		log.info("client side action - num threads: {}", numThreads);

		Tag kvp = fileAction.getTag(KEY_VAL_PAIR_PI);
		Map<String, String> kvpMap = TagHandlingUtils
				.translateKeyValuePairTagIntoMap(kvp);
		log.debug("kvp map is:{}", kvpMap);

		String localPath = kvpMap.get(LOCAL_PATH);

		if (localPath == null) {
			log.error("no local file path found in tags");
			throw new JargonException(
					"client side action indicated, but no localPath in tag");
		}

		String resourceName = kvpMap.get(DEST_RESC_NAME);
		if (resourceName == null) {
			resourceName = "";
		}
		log.info("resourceName:{}", resourceName);

		String forceValue = kvpMap.get("forceFlag");
		boolean force = false;
		if (forceValue != null) {
			log.info("get will use force option");
			force = true;
		}

		log.debug("getting reference to local file");

		File localFile = new File(localPath);

		if (label.equals(CL_GET_ACTION)) {
			clientSideGetAction(irodsFileAbsolutePath, localFile, resourceName,
					force, numThreads);
		} else if (label.equals(CL_PUT_ACTION)) {
			clientSidePutAction(irodsFileAbsolutePath, localFile, resourceName,
					force, numThreads);
		}

		StringBuilder putLabel = new StringBuilder();
		putLabel.append(label);
		putLabel.append('-');
		putLabel.append(irodsFileAbsolutePath);
		String labelValForAction = putLabel.toString();

		return IRODSRuleExecResultOutputParameter
				.instance(
						labelValForAction,
						IRODSRuleExecResultOutputParameter.OutputParamType.CLIENT_ACTION_RESULT,
						localPath);
	}

	private void clientSidePutAction(final String irodsFileAbsolutePath,
			final File localFile, final String resourceName,
			final boolean force, final int nbrThreads) throws JargonException {
		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		irodsFile.setResource(resourceName);
		log.debug("performing put of file");
		TransferControlBlock transferControlBlock = this
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		if (force) {
			transferControlBlock.getTransferOptions().setForceOption(
					ForceOption.USE_FORCE);
		} else {
			transferControlBlock.getTransferOptions().setForceOption(
					ForceOption.NO_FORCE);
		}

		if (nbrThreads == -1) {
			log.debug("override to no parallel threads for this transfer");
			transferControlBlock.getTransferOptions().setUseParallelTransfer(
					false);
		}

		transferControlBlock.getTransferOptions().setMaxThreads(nbrThreads);

		dataObjectAO.putLocalDataObjectToIRODSForClientSideRuleOperation(
				localFile, irodsFile, transferControlBlock);
		log.debug("client side put action was successful");
	}

	private void clientSideGetAction(final String irodsFileAbsolutePath,
			final File localFile, final String resourceName,
			final boolean force, final int nbrThreads) throws JargonException,
			DataNotFoundException {

		log.info("client-side get action");

		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		irodsFile.setResource(resourceName);
		log.info("performing get of file");
		TransferOptions transferOptions = this
				.buildTransferOptionsBasedOnJargonProperties();
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

		int status = dataObjectAO
				.irodsDataObjectGetOperationForClientSideAction(irodsFile,
						localFile, transferOptions);

		this.operationComplete(status);

		log.debug("client side get action was successful");
	}

}
