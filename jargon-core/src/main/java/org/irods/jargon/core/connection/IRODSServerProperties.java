/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.Date;

import org.irods.jargon.core.utils.MiscIRODSUtils;

/**
 * Immutable information on an IRODS Server that a connection is connected to.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSServerProperties {

	public static final String JARGON_VERSION = "3.3";

	public enum IcatEnabled {
		ICAT_ENABLED, NO_ICAT
	}

	private final Date initializeDate = new Date();
	private final IcatEnabled icatEnabled;
	private final int serverBootTime;
	private final String relVersion;
	private final String apiVersion;
	private final String rodsZone;
	private boolean eirods = false;

	/**
	 * This is a supplemental flag that indicates whether a server is eIRODS.
	 * 
	 * @return <code>true</code> if the given server is an eIRODS servers
	 */
	public synchronized boolean isEirods() {
		return eirods;
	}

	public synchronized void setEirods(boolean eirods) {
		this.eirods = eirods;
	}

	public static IRODSServerProperties instance(final IcatEnabled icatEnabled,
			final int serverBootTime, final String relVersion,
			final String apiVersion, final String rodsZone) {
		return new IRODSServerProperties(icatEnabled, serverBootTime,
				relVersion, apiVersion, rodsZone);
	}

	private IRODSServerProperties(final IcatEnabled icatEnabled,
			final int serverBootTime, final String relVersion,
			final String apiVersion, final String rodsZone) {
		super();
		this.icatEnabled = icatEnabled;
		this.serverBootTime = serverBootTime;
		this.relVersion = relVersion;
		this.apiVersion = apiVersion;
		this.rodsZone = rodsZone;
	}

	public Date getInitializeDate() {
		return initializeDate;
	}

	public static String getJargonVersion() {
		return JARGON_VERSION;
	}

	public IcatEnabled getIcatEnabled() {
		return icatEnabled;
	}

	public int getServerBootTime() {
		return serverBootTime;
	}

	public String getRelVersion() {
		return relVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public String getRodsZone() {
		return rodsZone;
	}

	/**
	 * Does the server (based on version) support connection re-routing?
	 * 
	 * @return <code>boolean</code> of <code>true</code> if re-routing is
	 *         supported.
	 */
	public boolean isSupportsConnectionRerouting() {
		boolean supports = false;
		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.5")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support specific (SQL) query
	 * 
	 * @return <code>boolean</code> of <code>true</code> if specific query is
	 *         supported
	 */
	public boolean isSupportsSpecificQuery() {
		boolean supports = false;

		if (this.isEirods()) {
			supports = true;
		} else if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support tickets?
	 * 
	 * @return <code>boolean</code> of <code>true</code> if this version
	 *         supports tickets
	 */
	public boolean isSupportsTickets() {
		boolean supports = false;

		if (this.isEirods()) {
			supports = false;
		} else if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support case-insensitive gen query
	 * conditions
	 * 
	 * @return <code>boolean</code> of <code>true</code> if this version
	 *         supports case-insensitive gen query conditions
	 */
	public boolean isSupportsCaseInsensitiveQueries() {
		boolean supports = false;

		if (this.isEirods()) {
			supports = true;
		} else if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.2")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Handy method compares the iRODS release version of the target server, and
	 * will indicate that the iRODS version being connected to is at or above
	 * the given version.
	 * 
	 * @param releaseVersion
	 *            <code>String</code> in standard iRODS version format that will
	 *            be checked against the currently-connected server.
	 * @return <code>boolean</code> that will be <code>true</code> if the iRODS
	 *         server is at or above the <code>releaseVersion</code>
	 */
	public boolean isTheIrodsServerAtLeastAtTheGivenReleaseVersion(
			final String releaseVersion) {
		if (releaseVersion == null || releaseVersion.length() == 0) {
			throw new IllegalArgumentException("null or empty releaseVersion");
		}

		return MiscIRODSUtils.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(
				getRelVersion(), releaseVersion);

		/*
		 * 
		 * 
		 * // The result is a negative integer if this String object //
		 * lexicographically precedes the argument string. int compValue =
		 * getRelVersion().compareToIgnoreCase(releaseVersion); return compValue
		 * >= 0;
		 */

	}

	@Override
	public String toString() {
		char ret = '\n';
		String tabOver = "    ";
		StringBuilder builder = new StringBuilder();
		builder.append("IRODS server properties");
		builder.append(ret);
		builder.append(tabOver);
		builder.append("icat enabled?:");
		builder.append(icatEnabled.toString());
		builder.append(ret);

		builder.append(tabOver);
		builder.append("Server boot time:");
		builder.append(serverBootTime);
		builder.append(ret);

		builder.append(tabOver);
		builder.append("Rel version:");
		builder.append(relVersion);
		builder.append(ret);

		builder.append(tabOver);
		builder.append("API version:");
		builder.append(apiVersion);
		builder.append(ret);

		builder.append(tabOver);
		builder.append("zone:");
		builder.append(rodsZone);
		builder.append(ret);
		builder.append(tabOver);
		builder.append("eirods:");
		builder.append(eirods);
		builder.append(ret);

		return builder.toString();

	}

}
