package org.irods.jargon.ticket;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the context for a mid-tier ticket distribution service.
 * Essentially, this describes a web accessible handler that can redeem tickets.
 * This context would describe the typical host/port information such that a
 * redeemed ticket can go to the right place.
 * <p>
 * This object is utilized by the {@link TicketDistributionService} to ask for
 * usable Ticket URL links and other information.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketDistributionContext {
	private String host = "";
	private int port = 80;
	private boolean ssl = false;
	private String context = "idrop-web/ticket/redeemTicket";
	private Map<String, String> additionalParameters = new HashMap<String, String>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ticketDistributionContext:");
		sb.append("\n   host:");
		sb.append(host);
		sb.append("\n   port:");
		sb.append(port);
		sb.append("\n   isSSL?:");
		sb.append(ssl);
		sb.append("\n   context:");
		sb.append(context);
		return sb.toString();
	}

	/**
	 * @return the host that will receive requests
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
	}

	/**
	 * @return the port that the mid tier service will listen on
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @return whether the service uses SSL
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * @param ssl
	 *            the ssl to set
	 */
	public void setSsl(final boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * Additional path information, typically an application context/method
	 * format. In idrop-web this is the controller and controller method that
	 * will receive ticket requests
	 * 
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(final String context) {
		this.context = context;
	}

	/**
	 * Additional parameters that should be added to the generated URL
	 * 
	 * @return the additionalParameters
	 */
	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	/**
	 * @param additionalParameters
	 *            the additionalParameters to set
	 */
	public void setAdditionalParameters(
			final Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

}
