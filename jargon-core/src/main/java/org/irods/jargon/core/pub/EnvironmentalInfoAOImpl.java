/**
 *
 */
package org.irods.jargon.core.pub;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.connection.EnvironmentalInfoAccessor;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.RemoteScriptExecutionException;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object to access information about an IRODS Server
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class EnvironmentalInfoAOImpl extends IRODSGenericAO implements
		EnvironmentalInfoAO {

	public static final Logger log = LoggerFactory
			.getLogger(EnvironmentalInfoAOImpl.class);

	private final EnvironmentalInfoAccessor environmentalInfoAccessor;

	protected EnvironmentalInfoAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		environmentalInfoAccessor = new EnvironmentalInfoAccessor(
				getIRODSSession().currentConnection(getIRODSAccount()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.accessobject.EnvironmentalInfoAO#
	 * getIRODSServerProperties()
	 */
	@Override
	public IRODSServerProperties getIRODSServerPropertiesFromIRODSServer()
			throws JargonException {

		IRODSServerProperties props = environmentalInfoAccessor
				.getIRODSServerProperties();
		return props;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.EnvironmentalInfoAO#getIRODSServerCurrentTime()
	 */
	@Override
	public long getIRODSServerCurrentTime() throws JargonException {
		log.info("getIRODSServerCurrentTime");
		StringBuilder sb = new StringBuilder(
				"getIRODSServerCurrentTime||msiGetSystemTime(*Time,null)##writeLine(stdout, *Time)|nop\n");
		sb.append("null\n");
		sb.append("*Time%ruleExecOut");
		RuleProcessingAO ruleProcessingAO = getIRODSAccessObjectFactory()
				.getRuleProcessingAO(getIRODSAccount());
		IRODSRuleExecResult result = ruleProcessingAO
				.executeRule(sb.toString());
		String execOut = (String) result.getOutputParameterResults()
				.get("*Time").getResultObject();

		if (execOut == null) {
			throw new JargonException(
					"no time returned from time rule execution");
		}

		log.debug("rule result:{}", execOut);
		long timeVal;

		try {
			timeVal = Long.parseLong(execOut) * 1000;
		} catch (NumberFormatException nfe) {
			log.error(
					"error getting time val from *Time in rule results when results were:{}",
					result);
			throw new JargonException("error getting time value", nfe);
		}

		return timeVal;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.EnvironmentalInfoAO#isAbleToRunSpecificQuery()
	 */
	@Override
	public boolean isAbleToRunSpecificQuery() throws JargonException {

		if (getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.EnvironmentalInfoAO#listAvailableRemoteCommands
	 * ()
	 */
	@Override
	public List<RemoteCommandInformation> listAvailableRemoteCommands()
			throws DataNotFoundException, JargonException {
		log.info("listAvailableRemoteCommands()");
		List<RemoteCommandInformation> remoteCommandInformation = new ArrayList<RemoteCommandInformation>();

		RemoteExecutionOfCommandsAO remoteExecutionAO = getIRODSAccessObjectFactory()
				.getRemoteExecutionOfCommandsAO(getIRODSAccount());

		InputStream result = null;
		StringWriter writer = new StringWriter();

		try {

			result = remoteExecutionAO
					.executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(
							"listCommands.sh", "");
			IOUtils.copy(result, writer, getJargonProperties().getEncoding());
		} catch (RemoteScriptExecutionException rse) {
			throw new DataNotFoundException(
					"no data can be found, listCommands.sh is not installed");
		} catch (IOException e) {
			throw new JargonException("IOException processing data", e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

		String rawCommandOutput = writer.toString();
		log.debug("raw command output: {}", rawCommandOutput);

		StringTokenizer st = new StringTokenizer(rawCommandOutput, "\n");
		RemoteCommandInformation remoteCommandInformationEntry = null;
		String token = null;

		while (st.hasMoreTokens()) {
			token = st.nextToken();
			log.debug(token);
			token = token.trim();
			int trimPoint = token.lastIndexOf(' ') + 1;
			String command = token.substring(trimPoint);
			if (command.charAt(0) == '.') {
				continue;
			}

			remoteCommandInformationEntry = new RemoteCommandInformation();
			remoteCommandInformationEntry.setRawData(token);
			remoteCommandInformationEntry.setCommand(command);
			remoteCommandInformationEntry.setHostName(getIRODSAccount()
					.getHost());
			remoteCommandInformationEntry.setZone(getIRODSAccount().getZone());
			remoteCommandInformation.add(remoteCommandInformationEntry);
		}

		log.info("remote commands:{}", remoteCommandInformation);
		return remoteCommandInformation;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.EnvironmentalInfoAO#listAvailableMicroservices
	 * ()
	 */
	@Override
	public List<String> listAvailableMicroservices() throws JargonException {
		log.info("listAvailableMicroservices()");
		List<String> availableMicroservices = new ArrayList<String>();

		if (!getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			throw new JargonException(
					"service not available on servers prior to rods3.0");
		}

		RuleProcessingAO ruleProcessingAO = getIRODSAccessObjectFactory()
				.getRuleProcessingAO(getIRODSAccount());
		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				"/rules/rulemsiListEnabledMS.r", null,
				RuleProcessingType.EXTERNAL);
		String resultBuff = result.getRuleExecOut().trim();
		log.info("raw microservice list:{}", resultBuff);
		StringTokenizer st = new StringTokenizer(resultBuff, "\n");

		while (st.hasMoreTokens()) {
			availableMicroservices.add(st.nextToken());
		}

		return availableMicroservices;
	}

}
