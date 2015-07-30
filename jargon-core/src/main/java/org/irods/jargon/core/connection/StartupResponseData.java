/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.utils.MiscIRODSUtils;

/**
 * Represents the iRODS server response to the sending of the StartupPack_PI at
 * the initiation of a connection. This information is useful for connection
 * have specified reconnect, and hold reconnect port and host information.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         <Version_PI> <status>0</status> <relVersion>rods3.1</relVersion>
 *         <apiVersion>d</apiVersion> <reconnPort>63610</reconnPort>
 *         <reconnAddr>192.168.43.202</reconnAddr> <cookie>1575067662</cookie>
 *         </Version_PI>
 */
public class StartupResponseData {

	private final int status;
	private final String relVersion;
	private final String apiVersion;
	private final int reconnPort;
	private final String reconnAddr;
	private final String cookie;
	private final boolean eirods;
	/**
	 * Holds the result of any client/server negotiation, will always be
	 * present, even if no negotiation is done
	 */
	private NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

	public StartupResponseData(
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		if (negotiatedClientServerConfiguration == null) {
			throw new IllegalArgumentException(
					"null negotiatedClientServerConfiguration");
		}
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
		this.status = 0;
		this.relVersion = "";
		this.apiVersion = "";
		this.reconnPort = 0;
		this.reconnAddr = "";
		this.cookie = "";
		this.eirods = true;
	}

	/**
	 * Default constructor initializes all of the required fields in response to
	 * a startup packet operation.
	 * 
	 * @param status
	 * @param relVersion
	 * @param apiVersion
	 * @param reconnPort
	 * @param reconnAddr
	 * @param cookie
	 */
	public StartupResponseData(final int status, final String relVersion,
			final String apiVersion, final int reconnPort,
			final String reconnAddr, final String cookie) {

		if (relVersion == null || relVersion.isEmpty()) {
			throw new IllegalArgumentException("null or empty relVersion");
		}

		if (apiVersion == null || apiVersion.isEmpty()) {
			throw new IllegalArgumentException("null or empty apiVersion");
		}

		if (reconnAddr == null) {
			throw new IllegalArgumentException("null reconnAddr");
		}

		if (cookie == null) {
			throw new IllegalArgumentException("null cookie");
		}

		this.status = status;
		this.relVersion = relVersion;
		this.apiVersion = apiVersion;
		this.reconnPort = reconnPort;
		this.reconnAddr = reconnAddr;
		this.cookie = cookie;

		int intCookie = Integer.parseInt(cookie);

		if (intCookie >= AbstractIRODSMidLevelProtocol.EIRODS_MIN
				&& intCookie <= AbstractIRODSMidLevelProtocol.EIRODS_MAX) {
			eirods = true;
		} else if (MiscIRODSUtils
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(relVersion,
						"rods4")) {
			eirods = true;
		} else {
			eirods = false;
		}

		/*
		 * Indicate no ssl in negotiated configuration
		 */
		this.negotiatedClientServerConfiguration = new NegotiatedClientServerConfiguration(
				false);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("StartupResponseData:");
		sb.append("\n   status:");
		sb.append(status);
		sb.append("\n   relVersion:");
		sb.append(relVersion);
		sb.append("\n   apiVersion:");
		sb.append(apiVersion);
		sb.append("\n   reconnPort:");
		sb.append(reconnPort);
		sb.append("\n   reconnAddr:");
		sb.append(reconnAddr);
		sb.append("\n   cookie:");
		sb.append(cookie);
		return sb.toString();
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the relVersion
	 */
	public String getRelVersion() {
		return relVersion;
	}

	/**
	 * @return the apiVersion
	 */
	public String getApiVersion() {
		return apiVersion;
	}

	/**
	 * @return the reconnPort
	 */
	public int getReconnPort() {
		return reconnPort;
	}

	/**
	 * @return the reconnAddr
	 */
	public String getReconnAddr() {
		return reconnAddr;
	}

	/**
	 * @return the cookie
	 */
	public String getCookie() {
		return cookie;
	}

	public boolean isEirods() {
		return eirods;
	}

	/**
	 * @return the negotiatedClientServerConfiguration
	 */
	public NegotiatedClientServerConfiguration getNegotiatedClientServerConfiguration() {
		return negotiatedClientServerConfiguration;
	}

	/**
	 * This setter is exposed because the startup response data may be augmented
	 * after a client/server negotiation
	 * 
	 * @param negotiatedClientServerConfiguration
	 *            the negotiatedClientServerConfiguration to set
	 */
	public void setNegotiatedClientServerConfiguration(
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
	}

}
