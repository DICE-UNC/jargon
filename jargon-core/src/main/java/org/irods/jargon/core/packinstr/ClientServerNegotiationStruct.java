/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.ClientServerNegotiationException;

/**
 * Structure that is the result of a client-server negotiation, analgous to the
 * cs_neg_t struct in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationStruct {

	private int status = 0;
	private SslNegotiationPolicy sslNegotiationPolicy = SslNegotiationPolicy.NO_NEGOTIATION;
	public static final String NEG_PI = "CS_NEG_PI";

	public static ClientServerNegotiationStruct instanceFromTag(final Tag tag)
			throws ClientServerNegotiationException {

		if (tag == null) {
			throw new IllegalArgumentException("Null tag");
		}

		if (!tag.getName().equals(NEG_PI)) {
			throw new IllegalArgumentException("tag is not a NEG_PI tag");
		}

		ClientServerNegotiationStruct struct = new ClientServerNegotiationStruct();
		int status = tag.getTag("status").getIntValue();
		struct.setStatus(status);
		String sslNegResult = tag.getTag("result").getStringValue();

		if (sslNegResult == null || sslNegResult.isEmpty()) {
			throw new ClientServerNegotiationException(
					"no ssl negotiation result found");
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_DONT_CARE
				.name())) {
			struct.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_REFUSE
				.name())) {
			struct.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REFUSE);
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_REQ.name())) {
			struct.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQ);
		} else {
			throw new ClientServerNegotiationException(
					"Unrecognized ssl negotiation response:" + sslNegResult);
		}

		return struct;

	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the sslNegotiationPolicy
	 */
	public SslNegotiationPolicy getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	/**
	 * @param sslNegotiationPolicy
	 *            the sslNegotiationPolicy to set
	 */
	public void setSslNegotiationPolicy(
			SslNegotiationPolicy sslNegotiationPolicy) {
		this.sslNegotiationPolicy = sslNegotiationPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientServerNegotiationStruct [status=").append(status)
				.append(", ");
		if (sslNegotiationPolicy != null) {
			builder.append("sslNegotiationPolicy=")
					.append(sslNegotiationPolicy);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Was this negotiation a success?
	 * 
	 * @return
	 */
	public boolean wasThisASuccess() {
		return this.getStatus() == 1;
	}

}
