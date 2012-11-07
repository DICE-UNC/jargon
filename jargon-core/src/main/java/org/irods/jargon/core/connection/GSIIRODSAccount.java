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
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param distinguishedName
	 *            <code>String</code> with the user's DN associated with the GSS
	 *            certificate
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 * @return {@link GSIIRODSAccount} for GSS login
	 */
	public static GSIIRODSAccount instance(final String host, final int port,
			final String distinguishedName, final GSSCredential gssCredential) {
		return new GSIIRODSAccount(host, port, distinguishedName, gssCredential);
	}

	/**
	 * Private constructor for GSI login to iRODS, automatically setting the
	 * <code>AuthScheme</code> to GSI.
	 * 
	 * @param host
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param distinguishedName
	 *            <code>String</code> with the user's DN associated with the GSS
	 *            certificate
	 * @param gssCredential
	 *            {@link GSSCredential} for the user login
	 */
	private GSIIRODSAccount(final String host, final int port,
			 final String distinguishedName,
 final GSSCredential gssCredential) {

		super(host, port, "", "", "", "", "");

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
	 * @return the distinguishedName
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}

	/**
	 * @return the serverDistinguishedName provided by iRODS upon GSI
	 *         authentication
	 */
	public String getServerDistinguishedName() {
		return serverDistinguishedName;
	}

	/**
	 * Set the distinguished name of the iRODS server (this is done by the
	 * GSIAuth handler)
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
	 * @see
	 * org.irods.jargon.core.connection.IRODSAccount#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
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

			if (!this.getDistinguishedName()
					.equals(temp.getDistinguishedName())) {
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
		return getHost().hashCode() + getPort()
				+ getDistinguishedName().hashCode();
	}

}
