/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Structure that is the result of a client-server negotiation, analogous to the
 * cs_neg_t struct in iRODS, responding to the server as a result of a
 * negotiation (failure or success)
 *
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationStructInitNegotiation extends
		AbstractIRODSPackingInstruction {

	private int status = 0;
	private SslNegotiationPolicy sslNegotiationPolicy = SslNegotiationPolicy.NO_NEGOTIATION;
	public static final String NEG_PI = "CS_NEG_PI";
	public static final int STATUS_FAILURE = 0;
	public static final int STATUS_SUCCESS = 1;
	public static final int API_NBR = 0;
	public static final String CS_NEG_RESULT_KW = "cs_neg_result_kw";

	private ClientServerNegotiationStructInitNegotiation(final Tag tag)
			throws ClientServerNegotiationException {

		if (tag == null) {
			throw new IllegalArgumentException("Null tag");
		}

		if (!tag.getName().equals(NEG_PI)) {
			throw new IllegalArgumentException("tag is not a NEG_PI tag");
		}

		int status = tag.getTag("status").getIntValue();
		setStatus(status);
		String sslNegResult = tag.getTag("result").getStringValue();

		if (sslNegResult == null || sslNegResult.isEmpty()) {
			throw new ClientServerNegotiationException(
					"no ssl negotiation result found");
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_DONT_CARE
				.name())) {
			setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_DONT_CARE);
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_REFUSE
				.name())) {
			setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REFUSE);
		} else if (sslNegResult.equals(SslNegotiationPolicy.CS_NEG_REQUIRE.name())) {
			setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
		} else {
			throw new ClientServerNegotiationException(
					"Unrecognized ssl negotiation response:" + sslNegResult);
		}

	}

	/**
	 * Create an instance of the struct based on deserializing the Tag structure
	 *
	 * @param tag
	 * @return
	 * @throws ClientServerNegotiationException
	 */
	public static ClientServerNegotiationStructInitNegotiation instanceFromTag(
			final Tag tag) throws ClientServerNegotiationException {

		return new ClientServerNegotiationStructInitNegotiation(tag);

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
	public void setStatus(final int status) {
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
			final SslNegotiationPolicy sslNegotiationPolicy) {
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
		return getStatus() == 1;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		StringBuilder sb = new StringBuilder();
		sb.append(CS_NEG_RESULT_KW);
		sb.append('=');
		sb.append(sslNegotiationPolicy.name());
		sb.append(';');
		Tag message = new Tag(NEG_PI, new Tag[] { new Tag("status", status),
				new Tag("result", sb.toString()) });
		return message;
	}

}
