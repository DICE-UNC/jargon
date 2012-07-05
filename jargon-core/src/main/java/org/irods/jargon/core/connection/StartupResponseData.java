/**
 * 
 */
package org.irods.jargon.core.connection;

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

}
