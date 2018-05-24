/**
 *
 */
package org.irods.jargon.core.connection;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class GSIIRODSAccount extends IRODSAccount {

	private static final long serialVersionUID = -2165721910428546185L;

	/**
	 * Stores the org.ietf.jgss.GSSCredential, used in GSI connections to the iRODS.
	 */
	private transient final GSSCredential gssCredential;

	/**
	 * Client DN, this is derived from the cert
	 */
	private final String distinguishedName;

	/**
	 * Server DN
	 */
	private String serverDistinguishedName = "";

	/**
	 * Certificate authority
	 */
	private String certificateAuthority = "";

	/**
	 * Static initializer method
	 *
	 * @param host
	 *            {@code String} with the iRODS host name
	 * @param port
	 *            {@code int} with the iRODS server port
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 * @param defaultStorageResource
	 *            {@code String} with an optional (blank if not specified) default
	 *            storage resource
	 * @return {@link GSIIRODSAccount} for GSS login
	 * @throws JargonException
	 *             if there was an error creating the account
	 */
	public static GSIIRODSAccount instance(final String host, final int port, final GSSCredential gssCredential,
			final String defaultStorageResource) throws JargonException {
		return new GSIIRODSAccount(host, port, gssCredential, defaultStorageResource);
	}

	/**
	 * Private constructor for GSI login to iRODS, automatically setting the
	 * {@code AuthScheme} to GSI.
	 *
	 * @param host
	 *            {@code String} with the iRODS host name
	 * @param port
	 *            {@code int} with the iRODS server port
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 * @throws JargonException
	 *             if there was an error creating the account
	 */
	private GSIIRODSAccount(final String host, final int port, final GSSCredential gssCredential,
			final String defaultStorageResource) throws JargonException {

		super(host, port, "", "", "", "", defaultStorageResource);

		if (gssCredential == null) {
			throw new IllegalArgumentException("null gssCredential");
		}

		this.gssCredential = gssCredential;
		try {
			distinguishedName = gssCredential.getName().toString();
		} catch (GSSException e) {
			throw new JargonException("GSSException getting distinguished name", e);
		}
		setAuthenticationScheme(AuthScheme.GSI);

	}

	/**
	 * If one exists, gets the GSSCredential used to make a GSI authentication.
	 *
	 * @return {@link GSSCredential} used to sign on
	 */
	public GSSCredential getGSSCredential() {
		return gssCredential;
	}

	/**
	 * @return the distinguishedName
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}

	/**
	 * @return the serverDistinguishedName provided by iRODS upon GSI authentication
	 */
	public String getServerDistinguishedName() {
		return serverDistinguishedName;
	}

	/**
	 * Set the distinguished name of the iRODS server (this is done by the GSIAuth
	 * handler)
	 *
	 * @param serverDistinguishedName
	 */
	void setServerDistinguishedName(final String serverDistinguishedName) {
		this.serverDistinguishedName = serverDistinguishedName;
	}

	/**
	 * @return the certificateAuthority
	 */
	public String getCertificateAuthority() {
		return certificateAuthority;
	}

	/**
	 * @param certificateAuthority
	 *            the certificateAuthority to set
	 */
	public void setCertificateAuthority(final String certificateAuthority) {
		this.certificateAuthority = certificateAuthority;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.IRODSAccount#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		try {
			if (obj == null) {
				return false;
			}

			GSIIRODSAccount temp = (GSIIRODSAccount) obj;

			if (!getHost().equals(temp.getHost())) {
				return false;
			}
			if (getPort() != temp.getPort()) {
				return false;
			}

			if (!getDistinguishedName().equals(temp.getDistinguishedName())) {
				return false;
			}

			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.connection.IRODSAccount#hashCode()
	 */
	@Override
	public int hashCode() {
		return getHost().hashCode() + getPort() + getDistinguishedName().hashCode();
	}

	/**
	 * Returns a string representation of this file system object. The string is
	 * formated according to the iRODS URI model. Note: the user password will not
	 * be included in the URI.
	 * <p>
	 * This version using the user DN as the user name. This may change in the
	 * future.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("irods://");
		sb.append(getDistinguishedName());
		sb.append("@");
		sb.append(getHost());
		sb.append(":");
		sb.append(getPort());
		return sb.toString();
	}

}
