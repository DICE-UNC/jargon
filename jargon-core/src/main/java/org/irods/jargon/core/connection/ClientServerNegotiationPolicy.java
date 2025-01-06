/**
 *
 */
package org.irods.jargon.core.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Captures the client-server negotiation policy for iRODS. This can be tuned
 * through jargon.properties and interactively. The {@link IRODSAccount} can
 * provide settings for negotiation to override default properties.
 *
 * See iRODS/lib/core/src/irods_client_negotiation.cpp
 *
 * @author Mike Conway - DICE
 *
 */

public class ClientServerNegotiationPolicy {
	/*
	 * NB the order of this enumeration needs to be carefully maintained, as the
	 * enum ordinals are used in the {@link ClientServerNegotiationService} to
	 * interpolate with the negotiation table.
	 */

	private static Logger log = LogManager.getLogger(ClientServerNegotiationPolicy.class);

	public enum SslNegotiationPolicy {
		CS_NEG_REQUIRE, CS_NEG_DONT_CARE, CS_NEG_REFUSE, NO_NEGOTIATION, CS_NEG_FAILURE
	}

	public static final String REQUEST_NEGOTIATION_STARTUP_OPTION = "request_server_negotiation";

	private SslNegotiationPolicy sslNegotiationPolicy = SslNegotiationPolicy.NO_NEGOTIATION;

	public synchronized SslNegotiationPolicy getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	public synchronized void setSslNegotiationPolicy(final SslNegotiationPolicy sslNegotiationPolicy) {
		this.sslNegotiationPolicy = sslNegotiationPolicy;
	}

	public synchronized String buildStartupOptionsForNegotiation() {

		if (sslNegotiationPolicy == SslNegotiationPolicy.NO_NEGOTIATION) {
			return "";
		} else {
			return REQUEST_NEGOTIATION_STARTUP_OPTION;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientServerNegotiationPolicy [");
		if (sslNegotiationPolicy != null) {
			builder.append("sslNegotiationPolicy=");
			builder.append(sslNegotiationPolicy);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Handy method (this is a bit awkward and needs to be refactored) to find the
	 * ssl negotiation policy as an enum value from a given string
	 *
	 * @param policyString
	 *            {@link String} with the string name of the SSL negotiation policy
	 * @return {@link SslNegotiationPolicy}
	 */
	public static SslNegotiationPolicy findSslNegotiationPolicyFromString(final String policyString) {

		if (policyString == null || policyString.isEmpty()) {
			throw new IllegalArgumentException("null or empty policyString");
		}

		log.info("policyString:{}", policyString);

		if (policyString.equals(SslNegotiationPolicy.CS_NEG_REQUIRE.toString())) {
			log.info("setting to neg require");
			return SslNegotiationPolicy.CS_NEG_REQUIRE;
		} else if (policyString.equals(SslNegotiationPolicy.CS_NEG_DONT_CARE.toString())) {
			log.info("setting to neg dont care");
			return SslNegotiationPolicy.CS_NEG_DONT_CARE;
		} else if (policyString.equals(SslNegotiationPolicy.CS_NEG_REFUSE.toString())) {
			log.info("setting to neg refuse");

			return SslNegotiationPolicy.CS_NEG_REFUSE;
		} else if (policyString.equals(SslNegotiationPolicy.NO_NEGOTIATION.toString())) {
			log.info("setting to no negotiation");
			return SslNegotiationPolicy.NO_NEGOTIATION;
		} else {
			throw new IllegalArgumentException("unknown negotiation policy");
		}

	}

}
