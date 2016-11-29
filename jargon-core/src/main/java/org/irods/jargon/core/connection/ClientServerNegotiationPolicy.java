/**
 *
 */
package org.irods.jargon.core.connection;

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
	public enum SslNegotiationPolicy {
		CS_NEG_REQ, CS_NEG_DONT_CARE, CS_NEG_REFUSE, NO_NEGOTIATION, CS_NEG_FAILURE
	}

	public static final String REQUEST_NEGOTIATION_STARTUP_OPTION = "request_server_negotiation";

	private SslNegotiationPolicy sslNegotiationPolicy = SslNegotiationPolicy.NO_NEGOTIATION;

	public synchronized SslNegotiationPolicy getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	public synchronized void setSslNegotiationPolicy(
			final SslNegotiationPolicy sslNegotiationPolicy) {
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

}
