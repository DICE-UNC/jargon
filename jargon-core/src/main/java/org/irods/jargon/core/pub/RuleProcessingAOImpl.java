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
import org.irods.jargon.core.pub.domain.DelayedRuleExecution;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IRODSRuleTranslator;
import org.irods.jargon.core.rule.JargonRuleException;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.TagHandlingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object that an execute iRODS rules, useful in services as a
 * stand-alone object, and a component used in other Access Objects.
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

	private static final Logger LOG = LoggerFactory
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

	private static final Object DEST_RESC_NAME = "destRescName";

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
	 * org.irods.jargon.core.pub.RuleProcessingAO#executeRule(java.lang.String)
	 */
	@Override
	public IRODSRuleExecResult executeRule(String irodsRuleAsString)
			throws JargonRuleException, JargonException {

		LOG.info("executing rule: {}", irodsRuleAsString);
		final IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		
		/*
		 * if iRODS 3.0+, add the @external parameter to the rule body 
		 */
		/*
		if (this.getIRODSServerProperties().isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			LOG.debug("adding @external to the rule body");
			StringBuilder bodyWithExtern = new StringBuilder("@external\n");
			bodyWithExtern.append(irodsRuleAsString);
			irodsRuleAsString = bodyWithExtern.toString();
		} 
		*/
	
		final IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(irodsRuleAsString);
		LOG.debug("translated rule: {}", irodsRule);
		final ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instance(irodsRule);
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		LOG.debug("response from rule exec: {}", response.parseTag());

		IRODSRuleExecResult irodsRuleExecResult = processRuleResult(response,
				irodsRule);
		LOG.debug("processing end of rule execution by reading message");

		return irodsRuleExecResult;
	}

	private IRODSRuleExecResult processRuleResult(final Tag irodsRuleResult,
			final IRODSRule irodsRule) throws JargonRuleException,
			JargonException {

		if (irodsRule == null) {
			throw new JargonException("irodsRule is nul");
		}

		if (irodsRuleResult == null) {
			LOG.debug("rule result was null, return empty output parameter map");
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

		LOG.debug("I have {} parameters from the rule", parametersLength);

		Map<String, IRODSRuleExecResultOutputParameter> outputParameters = processIndividualParameters(
				irodsRuleResult, parametersLength);
		IRODSRuleExecResult irodsRuleExecResult = IRODSRuleExecResult.instance(
				irodsRule, outputParameters);

		LOG.info("execute result: {}", irodsRuleExecResult);

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

		LOG.info("purgeAllDelayedExecQueue");

		List<DelayedRuleExecution> delayedRuleExecutions = listAllDelayedRuleExecutions(0);
		RuleExecDelInp ruleExecDelInp = null;

		for (DelayedRuleExecution delayedRuleExecution : delayedRuleExecutions) {
			LOG.info("deleting rule with id:{}", delayedRuleExecution.getId());
			ruleExecDelInp = RuleExecDelInp.instanceForDeleteRule(String
					.valueOf(delayedRuleExecution.getId()));
			// TODO: eval response
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

		LOG.info("listAllDelayedRuleExecutions() with partial start of {}",
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
		LOG.debug("query for rule exec status:{}", query);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query, 5000); // FIXME:
																				// put
																				// into
																				// props
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			LOG.error("query exception for query: {}", query, e);
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

		if (LOG.isInfoEnabled()) {
			LOG.info("built delayed rule execution built \n");
			LOG.info(dre.toString());
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
		boolean clientSideActionOccurred = false;
		// IRODSRuleExecResultOutputParameter irodsRuleOutputParameter;
		for (int i = 0; i < parametersLength; i++) {
			Tag msParam = rulesTag.getTag(IRODSConstants.MsParam_PI, i);
			label = msParam.getTag(LABEL).getStringValue();
			type = msParam.getTag(TYPE).getStringValue();
			value = getParameter(type, msParam);
			// need to differentiate tag that is client request from
			// tag that is an array

			LOG.debug("rule parameter label: {}", label);
			LOG.debug("parm type: {}", type);
			LOG.debug("value: {}", value);

			if (label.equals(CL_PUT_ACTION) || label.equals("CL_GET_ACTION")) {
				// for recording a client side action
				clientSideActionOccurred = true;
				irodsRuleOutputParameters.put(
						label,
						(processRuleResponseWithClientSideActionTag(label,
								type, value, msParam)));
				// operationComplete(0);
			} else {
				irodsRuleOutputParameters.put(label,
						(processRuleResponseTag(label, type, value, msParam)));
			}
		}

		// now read the rest of the rule response if there were params that were
		// processed
		if (clientSideActionOccurred) {
			LOG.info("a client side action ocurred, I have an irods message to consume to end the rule processing");
			// this.getIRODSProtocol().readMessage();
		}

		LOG.info("rule operation complete");
		return irodsRuleOutputParameters;

	}

	private IRODSRuleExecResultOutputParameter processRuleResponseTag(
			final String label, final String type, final Object value,
			final Tag msParam) throws JargonException {

		LOG.debug("processing output parameter for label: {}", label);
		LOG.debug("type: {}", type);
		LOG.debug("value: {}", value);
		LOG.debug("tag: {}", msParam.getStringValue());

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

		LOG.debug("status of exec: {}", status);

		if (status != 0) {
			throw new JargonException(
					"invalid status in response ExecCmdOut_PI tag:" + status);
		}
		
		Tag bufTag =  exec.getTag(BIN_BYTES_BUF_PI, 0).getTag(BUF);

		String results;
		
		if (bufTag != null) {
		String base64Encoded = bufTag
				.getStringValue();

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
		LOG.info("client side action - irods file absolute path: {}",
				irodsFileAbsolutePath);

		Tag kvp = fileAction.getTag(KEY_VAL_PAIR_PI);
		Map<String, String> kvpMap = TagHandlingUtils
				.translateKeyValuePairTagIntoMap(kvp);
		LOG.debug("kvp map is:{}", kvpMap);

		String localPath = kvpMap.get(LOCAL_PATH);

		if (localPath == null) {
			LOG.error("no local file path found in tags");
			throw new JargonException(
					"client side action indicated, but no localPath in tag");
		}

		String resourceName = kvpMap.get(DEST_RESC_NAME);
		if (resourceName == null) {
			resourceName = "";
		}

		LOG.debug("getting reference to local file");

		File localFile = new File(localPath);

		if (label.equals(CL_GET_ACTION)) {
			clientSideGetAction(irodsFileAbsolutePath, localFile);
		} else if (label.equals(CL_PUT_ACTION)) {
			clientSidePutAction(irodsFileAbsolutePath, localFile, resourceName);
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

	/**
	 * @param irodsFileAbsolutePath
	 * @param localFile
	 * @throws JargonException
	 */
	private void clientSidePutAction(final String irodsFileAbsolutePath,
			final File localFile, final String resourceName)
			throws JargonException {
		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		irodsFile.setResource(resourceName);
		LOG.debug("performing put of file");
		dataObjectAO.putLocalDataObjectToIRODSForClientSideRuleOperation(
				localFile, irodsFile, true, null);
		LOG.debug("client side put action was successful");
	}

	/**
	 * @param irodsFileAbsolutePath
	 * @param localFile
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	private void clientSideGetAction(final String irodsFileAbsolutePath,
			final File localFile) throws JargonException, DataNotFoundException {

		LOG.info("client-side get action");

		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIRODSSession(),
				getIRODSAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		LOG.info("performing get of file");
		dataObjectAO.irodsDataObjectGetOperationForClientSideAction(irodsFile,
				localFile, null);
		LOG.debug("client side get action was successful");
	}

	@SuppressWarnings("unused")
	private void operationComplete(final int status) throws JargonException {
		Tag message = new Tag(INT_PI, new Tag[] { new Tag(MY_INT, status), });
		getIRODSProtocol().irodsFunction(IRODSConstants.RODS_API_REQ, message.parseTag(),
				IRODSConstants.OPR_COMPLETE_AN);
	}

}
