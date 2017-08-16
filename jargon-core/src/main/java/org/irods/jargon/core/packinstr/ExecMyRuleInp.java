/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable object gives translation of an ExecMyRuleInp operation into XML
 * protocol format.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ExecMyRuleInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "ExecMyRuleInp_PI";

	public static final String MS_PARAM_ARRAY_PI = "MsParamArray_PI";
	public static final int RULE_API_NBR = 625;

	public static final String MY_RULE = "myRule";
	public static final String RHOSTADDR_PI = "RHostAddr_PI";
	public static final String HOST_ADDR = "hostAddr";
	public static final String PORT = "port";
	public static final String DUMMY_INT = "dummyInt";
	public static final String RODS_ZONE = "rodsZone";
	public static final String OUT_PARAM_DESC = "outParamDesc";
	public static final String PARAM_LEN = "paramLen";
	public static final String OPR_TYPE = "oprType";
	public static final String LABEL = "label";
	public static final String TYPE = "type";
	public static final String INT_PI = "INT_PI";
	public static final String BUF_LEN_PI = "BUF_LEN_PI";
	public static final String BUF_LEN = "buflen";
	public static final String BUF = "buf";
	public static final String STR_PI = "STR_PI";

	public static final String MY_INT = "myInt";
	public static final String MY_STR = "myStr";

	private transient final IRODSRule irodsRule;
	private transient final String host;
	private transient final int port;
	private transient final String zone;

	private static final Logger log = LoggerFactory
			.getLogger(ExecMyRuleInp.class);

	/**
	 * Create an instance of this packing instruction.
	 *
	 * @param irodsRule
	 *            {@link org.irods.jargon.core.rule.IRODSRule}
	 * @param host
	 *            {@code String} with the host name. Leave as blank if this
	 *            is not a remote rule
	 * @param port
	 *            {@code int} giving the port of the remote host. Set to 0
	 *            if unused
	 * @param zone
	 *            {@code String} giving the zone the rule should execute
	 *            on.
	 * @return {@link ExecMyRuleInp}
	 * @throws JargonException
	 */
	public static final ExecMyRuleInp instanceWithRemoteAttributes(
			final IRODSRule irodsRule, final String host, final int port,
			final String zone) throws JargonException {
		return new ExecMyRuleInp(irodsRule, host, port, zone);
	}

	/**
	 * Create an instance of this packing instruction.
	 *
	 * @param irodsRule
	 *            {@link org.irods.jargon.core.rule.IRODSRule}
	 * @return {@link ExecMyRuleInp}
	 * @throws JargonException
	 */
	public static final ExecMyRuleInp instance(final IRODSRule irodsRule)
			throws JargonException {
		return new ExecMyRuleInp(irodsRule, "", 0, "");
	}

	private ExecMyRuleInp(final IRODSRule irodsRule, final String host,
			final int port, final String zone) throws JargonException {
		super();

		if (irodsRule == null) {
			throw new JargonException("null IRODS rule");
		}

		this.irodsRule = irodsRule;

		// see if this is a remote execution. If a host is supplied, then port
		// and zone must be

		if (host == null || host.isEmpty()) {
			this.host = "";
			this.port = 0;
			this.zone = "";
		} else {
			if (port <= 1024) {
				throw new JargonException("invalid port number of " + port);
			}

			if (zone == null || zone.isEmpty()) {
				throw new JargonException(
						"null or missing zone when remotely executing the rule");
			}
			this.host = host;
			this.port = port;
			this.zone = zone;
		}

		setApiNumber(RULE_API_NBR);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {

		final Tag message = new Tag(PI_TAG,
				new Tag[] {
						new Tag(MY_RULE, irodsRule.getRuleBody()),
						new Tag(RHOSTADDR_PI, new Tag[] {
								new Tag(HOST_ADDR, host),
								new Tag(RODS_ZONE, zone), new Tag(PORT, port),
								new Tag(DUMMY_INT, 0), }),
						Tag.createKeyValueTag(null), });

		// process output parameters
		final StringBuilder sb = new StringBuilder();

		log.debug("process output parameters");
		final String outputParms = createTagValsForRuleOutputParams(sb);
		log.debug("rule output parms: {}", outputParms);
		message.addTag(new Tag(OUT_PARAM_DESC, outputParms));

		// process input parameters

		log.debug("processing rule input params");
		final Tag paramArray = new Tag(MS_PARAM_ARRAY_PI, new Tag[] {
				new Tag(PARAM_LEN, irodsRule.getIrodsRuleInputParameters()
						.size()), new Tag(OPR_TYPE, 0) });

		for (IRODSRuleParameter irodsRuleInputParameter : irodsRule
				.getIrodsRuleInputParameters()) {
			paramArray
					.addTag(getMsParamArrayTagForInputParameter(irodsRuleInputParameter));
		}

		message.addTag(paramArray);

		return message;
	}

	private Tag getMsParamArrayTagForInputParameter(
			final IRODSRuleParameter irodsRuleInputParameter) {

		log.debug("process input parameter: {}", irodsRuleInputParameter);

		final String type = irodsRuleInputParameter.getType();

		final Tag param = new Tag(IRODSConstants.MsParam_PI, new Tag[] {
				new Tag(LABEL, irodsRuleInputParameter.getUniqueName()),
				new Tag(TYPE, irodsRuleInputParameter.getType()), });

		if (type.equals(INT_PI)) {
			param.addTag(new Tag(INT_PI, new Tag[] {

					// only one parameter, the int
					new Tag(MY_INT, irodsRuleInputParameter.retrieveIntValue()), }));

		} else if (type.equals(BUF_LEN_PI)) {
			param.addTag(new Tag(BUF_LEN_PI, new Tag[] {
					// send a byte buffer
					new Tag(BUF_LEN,
							irodsRuleInputParameter.retrieveByteValue().length),
							// maybe convert to Base64?
							new Tag(BUF, new String(irodsRuleInputParameter
									.retrieveByteValue())), }));
		} else {// STR_PI or NULL_PI
			param.addTag(new Tag(STR_PI, new Tag[] {
					// only one parameter, the string
					// if default, try sending the string value, might
					// work...
					new Tag(MY_STR, irodsRuleInputParameter.retrieveStringValue()), }));
		}
		return param;
	}

	/**
	 * @param sb
	 * @param firstParm
	 * @return
	 */
	private String createTagValsForRuleOutputParams(final StringBuilder sb) {
		boolean firstParm = true;
		for (IRODSRuleParameter irodsRuleOutputParameter : irodsRule
				.getIrodsRuleOutputParameters()) {
			if (firstParm) {
				firstParm = false;
			} else {
				sb.append('%');
			}
			sb.append(irodsRuleOutputParameter.getUniqueName());
		}

		return sb.toString();
	}
}
