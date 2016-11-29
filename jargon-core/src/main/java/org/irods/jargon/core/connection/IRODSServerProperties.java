/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.Date;

/**
 * Immutable information on an IRODS Server that a connection is connected to.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSServerProperties {

	public enum IcatEnabled {
		ICAT_ENABLED, NO_ICAT
	}

	private final Date initializeDate = new Date();
	private final IcatEnabled icatEnabled;
	private final int serverBootTime;
	private IrodsVersion irodsVersion;
	private final String apiVersion;
	private final String rodsZone;

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
		this.apiVersion = apiVersion;
		irodsVersion = new IrodsVersion(relVersion);
		this.rodsZone = rodsZone;
	}

	public Date getInitializeDate() {
		return initializeDate;
	}

	public IcatEnabled getIcatEnabled() {
		return icatEnabled;
	}

	public int getServerBootTime() {
		return serverBootTime;
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

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
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

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support workflow (WSOs)?
	 *
	 * @return <code>boolean</code> of <code>true</code> if this version
	 *         supports WSO workflow
	 */
	public boolean isSupportsWSOWorkflow() {
		boolean supports = false;
		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.2")) {
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

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.2")) {
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

		int compare = getIrodsVersion().compareTo(
				new IrodsVersion(releaseVersion));
		return compare >= 0;

	}

	/**
	 * Is the server 4.0.X and not yet 4.1? Then I need to worry about pam
	 * flushes per https://github.com/DICE-UNC/jargon/issues/70 This overhead
	 * will force the pam flush based on the forceSslFlush flag, which will only
	 * be turned on to bracket the necessary calls to the protocol, preventing a
	 * performance drop from unneeded flushes later
	 *
	 * @return <code>boolean</code> of <code>true</code> if pam flush overhead
	 *         is required
	 */
	public boolean isNeedsPamFlush() {
		return (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.0") && (!isAtLeastIrods410()));
	}

	/**
	 * Is the server at least iRODS 4.1.0
	 *
	 * @return
	 */
	public boolean isAtLeastIrods410() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.1.0");
	}

	/**
	 * Is the server at least iRODS 4.2.0
	 *
	 * @return
	 */
	public boolean isAtLeastIrods420() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2.0");
	}

	public IrodsVersion getIrodsVersion() {
		return irodsVersion;
	}

}
