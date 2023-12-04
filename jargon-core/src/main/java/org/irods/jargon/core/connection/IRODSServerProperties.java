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

	public static IRODSServerProperties instance(final IcatEnabled icatEnabled, final int serverBootTime,
			final String relVersion, final String apiVersion, final String rodsZone) {
		return new IRODSServerProperties(icatEnabled, serverBootTime, relVersion, apiVersion, rodsZone);
	}

	private IRODSServerProperties(final IcatEnabled icatEnabled, final int serverBootTime, final String relVersion,
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
	 * @return {@code boolean} of {@code true} if re-routing is supported.
	 */
	public boolean isSupportsConnectionRerouting() {
		boolean supports = false;
		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.5")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support metadata 'set' operations as
	 * opposed to 'add'
	 *
	 * @return <code>boolean</code> indicating whether metadata 'set' semantics are
	 *         supported
	 */
	public boolean isSupportsMetadataSet() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.1")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support specific (SQL) query
	 *
	 * @return {@code boolean} of {@code true} if specific query is supported
	 */
	public boolean isSupportsSpecificQuery() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Is this server after 4.2.11, when cond input was added to the ticket admin
	 * packing instruction?
	 * 
	 * @return {@code true} if server requires cond input in the ticket admin
	 *         packing instruction
	 */
	public boolean isTicketAdminCondInput() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2.11")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support composable resources
	 *
	 * @return <code>boolean</code> indicating whether composable resources are
	 *         supported
	 */
	public boolean isSupportsComposableResoures() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.0")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Indicates whether the iRODS server supports replica tokens and locking
	 * 
	 * @return {@code boolean} if replica tokens and locking are supported
	 */
	public boolean isSupportsReplicaTokens() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2.8")) {
			supports = true;
		}
		return supports;
	}

	/**
	 * Does the server (based on version) support tickets?
	 *
	 * @return {@code boolean} of {@code true} if this version supports tickets
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
	 * @return {@code boolean} of {@code true} if this version supports WSO workflow
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
	 * @return {@code boolean} of {@code true} if this version supports
	 *         case-insensitive gen query conditions
	 */
	public boolean isSupportsCaseInsensitiveQueries() {
		boolean supports = false;

		if (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.2")) {
			supports = true;
		}
		return supports;
	}
	
	/**
	 * Handy method used to detect the exact version of the connected iRODS server.
	 * 
	 * @param releaseVersion {@code String} in standard iRODS version format that
	 *                       will be checked against the currently-connected server.
	 * @return {@code boolean} that will be {@code true} if the iRODS server is
	 *         {@code releaseVersion}
	 */
	public boolean isVersion(final String releaseVersion) {
		if (releaseVersion == null || releaseVersion.isEmpty()) {
			throw new IllegalArgumentException("null or empty releaseVersion");
		}

		return getIrodsVersion().compareTo(new IrodsVersion(releaseVersion)) == 0;
	}

	/**
	 * Handy method compares the iRODS release version of the target server, and
	 * will indicate that the iRODS version being connected to is at or above the
	 * given version.
	 *
	 * @param releaseVersion {@code String} in standard iRODS version format that
	 *                       will be checked against the currently-connected server.
	 * @return {@code boolean} that will be {@code true} if the iRODS server is at
	 *         or above the {@code releaseVersion}
	 */
	public boolean isTheIrodsServerAtLeastAtTheGivenReleaseVersion(final String releaseVersion) {
		if (releaseVersion == null || releaseVersion.length() == 0) {
			throw new IllegalArgumentException("null or empty releaseVersion");
		}

		int compare = getIrodsVersion().compareTo(new IrodsVersion(releaseVersion));
		return compare >= 0;

	}

	/**
	 * Is the server 4.0.X and not yet 4.1? Then I need to worry about pam flushes
	 * per https://github.com/DICE-UNC/jargon/issues/70 This overhead will force the
	 * pam flush based on the forceSslFlush flag, which will only be turned on to
	 * bracket the necessary calls to the protocol, preventing a performance drop
	 * from unneeded flushes later
	 *
	 * @return {@code boolean} of {@code true} if pam flush overhead is required
	 */
	public boolean isNeedsPamFlush() {
		return (isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.0") && (!isAtLeastIrods410()));
	}

	/**
	 * Is the server at least iRODS 4.1.0
	 *
	 * @return {@code boolean}
	 */
	public boolean isAtLeastIrods410() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.1.0");
	}

	/**
	 * Is the server at least iRODS 4.2.0
	 *
	 * @return {@code boolean}
	 */
	public boolean isAtLeastIrods420() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2.0");
	}

	/**
	 * Is the server at least iRODS 4.3.0
	 *
	 * @return {@code boolean}
	 */
	public boolean isAtLeastIrods430() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.3.0");
	}

	/**
	 * Is the server at least iRODS 4.3.1
	 *
	 * @return {@code boolean}
	 */
	public boolean isAtLeastIrods431() {
		return isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.3.1");
	}

	public IrodsVersion getIrodsVersion() {
		return irodsVersion;
	}

}
