package org.irods.jargon.core.pub;

import static edu.sdsc.grid.io.irods.IRODSConstants.OPR_COMPLETE_AN;
import static edu.sdsc.grid.io.irods.IRODSConstants.RODS_API_REQ;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.BUF;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.BUF_LEN_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.INT_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.LABEL;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.MY_INT;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.MY_STR;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.STR_PI;
import static org.irods.jargon.core.packinstr.ExecMyRuleInp.TYPE;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ExecMyRuleInp;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IRODSRuleTranslator;
import org.irods.jargon.core.rule.JargonRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.Base64;
import edu.sdsc.grid.io.irods.IRODSConstants;
import edu.sdsc.grid.io.irods.Tag;

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
	public IRODSRuleExecResult executeRule(final String irodsRuleAsString)
			throws JargonRuleException, JargonException {
		LOG.info("executing rule: {}", irodsRuleAsString);
		final IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator();
		final IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(irodsRuleAsString);
		LOG.debug("translated rule: {}", irodsRule);
		final ExecMyRuleInp execMyRuleInp = ExecMyRuleInp.instance(irodsRule);
		final Tag response = getIRODSProtocol().irodsFunction(execMyRuleInp);
		LOG.debug("response from rule exec: {}", response.parseTag());

		return processRuleResult(response, irodsRule);
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

				irodsRuleOutputParameters.put(
						label,
						(processRuleResponseWithClientSideActionTag(label,
								type, value, msParam)));
			} else {
				irodsRuleOutputParameters.put(label,
						(processRuleResponseTag(label, type, value, msParam)));
			}
		}

		LOG.info("rule operation complete");
		operationComplete(0);
		return irodsRuleOutputParameters;

	}

	private IRODSRuleExecResultOutputParameter processRuleResponseTag(
			final String label, final String type, final Object value,
			final Tag msParam) throws JargonException {

		LOG.debug("processing output parameter for label: {}", label);
		LOG.debug("type: {}", type);
		LOG.debug("value: {}", value);
		LOG.debug("tag: {}", msParam.getStringValue());

		final IRODSRuleExecResultOutputParameter.OutputParamType paramType;

		// FIXME: mcc, check out the intent here
		if (type.equals(EXEC_CMD_OUT_PI)) {
			paramType = IRODSRuleExecResultOutputParameter.OutputParamType.RULE_EXEC_OUT;
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

		String base64Encoded = exec.getTag(BIN_BYTES_BUF_PI, 0).getTag(BUF)
				.getStringValue();

		byte[] decoded = Base64.fromString(base64Encoded);

		String results = new String(decoded);

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

		String otherFilePath = fileAction.getTag(KEY_VAL_PAIR_PI)
				.getTag(SVALUE).getStringValue();
		String otherFileType = fileAction.getTag(KEY_VAL_PAIR_PI)
				.getTag(KEY_WORD).getStringValue();

		if (otherFileType.equals(LOCAL_PATH)) {
			LOG.info("received request for local file: {}", otherFilePath);
		} else {
			LOG.error("received invaid  request for local file: {}",
					otherFilePath);
			LOG.error("other file type was invalid: {}", otherFileType);
			throw new JargonException(
					"Rule requests tranfer from unknown protocol");
		}

		LOG.debug("getting reference to local file");

		File localFile = new File(otherFilePath);

		if (label.equals(CL_GET_ACTION)) {
			clientSideGetAction(irodsFileAbsolutePath, localFile);
		} else if (label.equals(CL_PUT_ACTION)) {
			clientSidePutAction(irodsFileAbsolutePath, localFile);
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
						otherFilePath);
	}

	/**
	 * @param irodsFileAbsolutePath
	 * @param localFile
	 * @throws JargonException
	 */
	private void clientSidePutAction(final String irodsFileAbsolutePath,
			final File localFile) throws JargonException {
		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIrodsSession(),
				getIrodsAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		LOG.debug("performing put of file");
		dataObjectAO.putLocalDataObjectToIRODSForClientSideRuleOperation(
				localFile, irodsFile, true);
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

		DataObjectAO dataObjectAO = new DataObjectAOImpl(getIrodsSession(),
				getIrodsAccount());
		IRODSFile irodsFile = dataObjectAO
				.instanceIRODSFileForPath(irodsFileAbsolutePath);
		LOG.info("performing get of file");
		dataObjectAO.irodsDataObjectGetOperationForClientSideAction(irodsFile,
				localFile);
		LOG.debug("client side get action was successful");
	}

	private void operationComplete(final int status) throws JargonException {
		Tag message = new Tag(INT_PI, new Tag[] { new Tag(MY_INT, status), });
		getIRODSProtocol().irodsFunction(RODS_API_REQ, message.parseTag(),
				OPR_COMPLETE_AN);
	}

}
