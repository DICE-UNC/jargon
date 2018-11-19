/**
 *
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

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
	public static final String RULE_INSTANCE_NAME_KW = "instance_name";

	public static final String MY_INT = "myInt";
	public static final String MY_STR = "myStr";

	private final IRODSRule irodsRule;
	private final String host;
	private final int port;
	private final String zone;
	private boolean listAvailableRuleEnginesMode = false;

	private static final Logger log = LoggerFactory.getLogger(ExecMyRuleInp.class);

	/**
	 * Create an instance of this packing instruction.
	 *
	 * @param irodsRule
	 *            {@link org.irods.jargon.core.rule.IRODSRule}
	 * @param host
	 *            {@code String} with the host name. Leave as blank if this is not a
	 *            remote rule
	 * @param port
	 *            {@code int} giving the port of the remote host. Set to 0 if unused
	 * @param zone
	 *            {@code String} giving the zone the rule should execute on.
	 * @return {@link ExecMyRuleInp}
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static final ExecMyRuleInp instanceWithRemoteAttributes(final IRODSRule irodsRule, final String host,
			final int port, final String zone) throws JargonException {
		return new ExecMyRuleInp(irodsRule, host, port, zone);
	}

	/**
	 * Create an instance of this packing instruction.
	 *
	 * @param irodsRule
	 *            {@link org.irods.jargon.core.rule.IRODSRule}
	 * @return {@link ExecMyRuleInp}
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static final ExecMyRuleInp instance(final IRODSRule irodsRule) throws JargonException {
		return new ExecMyRuleInp(irodsRule, "", 0, "");
	}

	public static final ExecMyRuleInp instanceForListAvailableRuleEngines() throws JargonException {
		return new ExecMyRuleInp();
	}

	/**
	 * No values constructor when executing for list available rule engines, allows
	 * no {@code irodsRule} to be provided, and sets mode to list available rule
	 * engines when generating the tag.
	 */
	private ExecMyRuleInp() {
		this.irodsRule = null;
		this.host = "";
		this.port = 0;
		this.zone = "";
		this.listAvailableRuleEnginesMode = true;
	}

	/**
	 * Default constructor with all final fields initialized
	 *
	 * @param irodsRule
	 *            {@link IRODSRule} describing the user-submitted rule
	 * @param host
	 *            <code>String</code> with an optional irods host for remote
	 *            execution
	 * @param port
	 *            <code>int</code> with an optional irods port for remote execution
	 * @param zone
	 *            <code>String</code> with an optional zone for remote execution
	 */
	private ExecMyRuleInp(final IRODSRule irodsRule, final String host, final int port, final String zone) {
		super();

		if (irodsRule == null) {
			throw new IllegalArgumentException("null IRODS rule");
		}

		this.irodsRule = irodsRule;

		// see if this is a remote execution. If a host is supplied, then port
		// and zone must be

		if (host == null || host.isEmpty()) {
			this.host = "";
			this.port = 0;
			this.zone = "";
		} else {

			if (zone == null || zone.isEmpty()) {
				throw new IllegalArgumentException("null or missing zone when remotely executing the rule");
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

		if (this.listAvailableRuleEnginesMode) {
			log.info("generating tag for list all available");
			return getTagValueForListAllAvailable();
		}

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		if (!irodsRule.getRuleInvocationConfiguration().getRuleEngineSpecifier().isEmpty()) {
			log.debug("adding rule engine instance:{}",
					irodsRule.getRuleInvocationConfiguration().getRuleEngineSpecifier());
			kvps.add(KeyValuePair.instance(RULE_INSTANCE_NAME_KW,
					irodsRule.getRuleInvocationConfiguration().getRuleEngineSpecifier()));
		}

		final Tag message = new Tag(PI_TAG,
				new Tag[] {
						new Tag(MY_RULE, irodsRule
								.getRuleBody()),
						new Tag(RHOSTADDR_PI, new Tag[] { new Tag(HOST_ADDR, host), new Tag(RODS_ZONE, zone),
								new Tag(PORT, port), new Tag(DUMMY_INT, 0), }),
						createKeyValueTag(kvps) });

		// process output parameters
		final StringBuilder sb = new StringBuilder();

		log.debug("process output parameters");
		final String outputParms = createTagValsForRuleOutputParams(sb);
		log.debug("rule output parms: {}", outputParms);
		message.addTag(new Tag(OUT_PARAM_DESC, outputParms));

		// process input parameters

		log.debug("processing rule input params");
		final Tag paramArray = new Tag(MS_PARAM_ARRAY_PI,
				new Tag[] { new Tag(PARAM_LEN, irodsRule.getIrodsRuleInputParameters().size()), new Tag(OPR_TYPE, 0) });

		for (IRODSRuleParameter irodsRuleInputParameter : irodsRule.getIrodsRuleInputParameters()) {
			paramArray.addTag(getMsParamArrayTagForInputParameter(irodsRuleInputParameter));
		}

		message.addTag(paramArray);

		return message;
	}

	/*
	 * Abbreviated tag when asking for available rule engines
	 */
	private Tag getTagValueForListAllAvailable() throws JargonException {
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		kvps.add(KeyValuePair.instance("available", "true"));
		final Tag message = new Tag(PI_TAG,
				new Tag[] {
						new Tag(MY_RULE, ""), new Tag(RHOSTADDR_PI, new Tag[] { new Tag(HOST_ADDR, host),
								new Tag(RODS_ZONE, zone), new Tag(PORT, port), new Tag(DUMMY_INT, 0), }),
						createKeyValueTag(kvps) });
		return message;

	}

	private Tag getMsParamArrayTagForInputParameter(final IRODSRuleParameter irodsRuleInputParameter) {

		log.debug("process input parameter: {}", irodsRuleInputParameter);

		final String type = irodsRuleInputParameter.getType();

		final Tag param = new Tag(IRODSConstants.MsParam_PI,
				new Tag[] { new Tag(LABEL, irodsRuleInputParameter.getUniqueName()),
						new Tag(TYPE, irodsRuleInputParameter.getType()), });

		if (type.equals(INT_PI)) {
			param.addTag(new Tag(INT_PI, new Tag[] {
					// only one parameter, the int
					new Tag(MY_INT, irodsRuleInputParameter.retrieveIntValue()), }));
		} else if (type.equals(BUF_LEN_PI)) {
			param.addTag(new Tag(BUF_LEN_PI, new Tag[] {
					// send a byte buffer
					new Tag(BUF_LEN, irodsRuleInputParameter.retrieveByteValue().length),
					// maybe convert to Base64?
					new Tag(BUF, new String(irodsRuleInputParameter.retrieveByteValue())), }));
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
		for (IRODSRuleParameter irodsRuleOutputParameter : irodsRule.getIrodsRuleOutputParameters()) {
			if (firstParm) {
				firstParm = false;
			} else {
				sb.append('%');
			}
			sb.append(irodsRuleOutputParameter.getUniqueName());
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExecMyRuleInp [");
		if (irodsRule != null) {
			builder.append("irodsRule=").append(irodsRule).append(", ");
		}
		if (host != null) {
			builder.append("host=").append(host).append(", ");
		}
		builder.append("port=").append(port).append(", ");
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}

		builder.append("]");
		return builder.toString();
	}
}