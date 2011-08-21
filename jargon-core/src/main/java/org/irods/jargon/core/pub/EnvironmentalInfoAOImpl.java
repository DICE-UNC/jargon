/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.EnvironmentalInfoAccessor;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
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

		return environmentalInfoAccessor.getIRODSServerProperties();
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
				"testrule||msiGetSystemTime(*Time,null)##writeLine(stdout, *Time)|nop\n");
		sb.append("null\n");
		sb.append("*Time%ruleExecOut");
		RuleProcessingAO ruleProcessingAO = this.getIRODSAccessObjectFactory()
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

}
