/**
 * 
 */
package org.irods.jargon.core.connection;

import org.ietf.jgss.GSSCredential;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GSIIRODSAccount extends IRODSAccount {

	private static final long serialVersionUID = -2165721910428546185L;

	/**
	 * Stores the org.ietf.jgss.GSSCredential, used in GSI connections to the
	 * iRODS.
	 */
	private transient final GSSCredential gssCredential;

	/**
	 * Client DN
	 */
	private final String distinguishedName;

	/**
	 * Static initializer method
	 * 
	 * @param host
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param zone
	 *            <code>String</code> with the iRODS zone name
	 * @param distinguishedName
	 *            <code>String</code> with the user's DN associated with the GSS
	 *            certificate
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 * @param homeDirectory
	 *            <code>String</code> with optional home directory to use, use
	 *            blank if not needed
	 * @param defaultStorageResource
	 *            <code>String</code> with an optional default storage resource,
	 *            use blank if not needed
	 * @return {@link GSIIRODSAccount} for GSS login
	 */
	public static GSIIRODSAccount instance(final String host, final int port,
			final String zone, final String distinguishedName,
			final GSSCredential gssCredential,

			final String homeDirectory, final String defaultStorageResource) {
		return new GSIIRODSAccount(host, port, zone, distinguishedName,
				gssCredential, homeDirectory, defaultStorageResource);
	}

	/**
	 * Private constructor for GSI login to iRODS, automatically setting the
	 * <code>AuthScheme</code> to GSI.
	 * 
	 * @param host
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param zone
	 *            <code>String</code> with the iRODS zone name
	 * @param distinguishedName
	 *            <code>String</code> with the user's DN associated with the GSS
	 *            certificate
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 * @param homeDirectory
	 *            <code>String</code> with optional home directory to use, use
	 *            blank if not needed
	 * @param defaultStorageResource
	 *            <code>String</code> with an optional default storage resource,
	 *            use blank if not needed
	 */
	private GSIIRODSAccount(final String host, final int port,
			final String zone, final String distinguishedName,
			final GSSCredential gssCredential, final String homeDirectory,
			final String defaultStorageResource) {

		super(host, port, "", "", homeDirectory, zone, defaultStorageResource);

		if (gssCredential == null) {
			throw new IllegalArgumentException("null gssCredential");
		}

		if (distinguishedName == null || distinguishedName.isEmpty()) {
			throw new IllegalArgumentException("null distinguishedName");
		}

		this.gssCredential = gssCredential;
		this.distinguishedName = distinguishedName;
		this.setAuthenticationScheme(AuthScheme.GSI);

	}

	/**
	 * If one exists, gets the GSSCredential used to make a GSI authentication.
	 */
	public GSSCredential getGSSCredential() {
		return gssCredential;
	}

	/**
	 * @return the gssCredential
	 */
	public GSSCredential getGssCredential() {
		return gssCredential;
	}

	/**
	 * @return the distinguishedName
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}

}
