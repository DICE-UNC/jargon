/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Captures the client-server negotiation policy for iRODS.  This can be tuned through jargon.properties and interactively.  The {@link IRODSAccount} can provide settings
 * for negotiation to override default properties.
 * 
 * See iRODS/lib/core/src/irods_client_negotiation.cpp
 * 
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationPolicy {
	public enum NegotiationPolicy {NO_NEGOTIATION, CS_NEG_REFUSE, CS_NEG_REQ, CS_NEG_DONT_CARE}

	private NegotiationPolicy negotiationPolicy = NegotiationPolicy.NO_NEGOTIATION;

	public synchronized NegotiationPolicy getNegotiationPolicy() {
		return negotiationPolicy;
	}

	public synchronized void setNegotiationPolicy(
			NegotiationPolicy negotiationPolicy) {
		this.negotiationPolicy = negotiationPolicy;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientServerNegotiationPolicy [");
		if (negotiationPolicy != null) {
			builder.append("negotiationPolicy=");
			builder.append(negotiationPolicy);
		}
		builder.append("]");
		return builder.toString();
	}
	
}
