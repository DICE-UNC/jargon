package org.irods.jargon.core.connection;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.ietf.jgss.GSSCredential;
import org.irods.jargon.core.exception.JargonException;

/**
 * Representation of an identity on IRODS. Contains info similar to that
 * contained in the .irodsEnv file. The main account attributes are immutable,
 * but certain elements are mutable as they may be updated during processing.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSAccount implements Serializable {

	private static final long serialVersionUID = 8627989693793656697L;
	public static final String IRODS_JARGON_RELEASE_NUMBER = "rods3.0";
	public static final String IRODS_API_VERSION = "d";

	public static final boolean defaultObfuscate = false;
	public static final String PUBLIC_USERNAME = "anonymous";
	private AuthScheme authenticationScheme = AuthScheme.STANDARD;
	private String serviceName = "";

	/**
	 * The certificate authority (CA) list. By default, the CA definition comes
	 * from the user's cog.properties file.
	 */
	private final String certificateAuthority;
	/**
	 * Stores the org.ietf.jgss.GSSCredential, used in GSI connections to the
	 * iRODS.
	 */
	private transient final GSSCredential gssCredential;

	private final String host;
	private final int port;
	private final String zone;
	private final String userName;
	private String password;
	private String defaultStorageResource;
	private String homeDirectory;

	private final String serverDN;
	private List<String> authenticatedRoles = new ArrayList<String>();

	/**
	 * Creates an object to hold iRODS account information. All parameters need
	 * to be initialized to use this initializer.
	 * <P>
	 * 
	 * @param host
	 *            the iRODS server domain name
	 * @param port
	 *            the port on the iRODS server
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @param homeDirectory
	 *            home directory on the iRODS
	 * @param zone
	 *            the IRODS zone
	 * @param defaultStorageResource
	 *            default storage resource
	 */
	public static IRODSAccount instance(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String zone,
			final String defaultStorageResource) throws JargonException {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("host is null or empty");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (password == null) {
			throw new IllegalArgumentException("password is null");
		}

		if (homeDirectory == null) {
			throw new IllegalArgumentException("homeDirectory is null");
		}

		if (zone == null || zone.isEmpty()) {
			throw new IllegalArgumentException("zone is null or empty");
		}

		if (defaultStorageResource == null) {
			throw new IllegalArgumentException("defaultStorageResource is null");
		}

		return new IRODSAccount(host, port, userName, password, homeDirectory,
				zone, defaultStorageResource);
	}

	/**
	 * Create an <code>IRODSAccount</code> suitable for anonymous access.
	 * 
	 * @param host
	 *            <code>String</code> with the DNS name of the iRODS host
	 * @param port
	 *            <code>int</code> with the iRODS port number (typically 1247)
	 * @param homeDirectory
	 *            <code>String</code> with optional value for the starting home
	 *            directory, this can be used to set initial views, etc by other
	 *            code
	 * @param zone
	 *            <code>String</code> with the iRODS zone
	 * @param defaultStorageResource
	 *            <code>String</code> with optional value for the default
	 *            storage resource. Note that iRODS may have defaults set by
	 *            policy. In cases where no default policy exists, and none is
	 *            specified here, an error can occur.
	 * @return <code>IRODSAccount</code> suitable for anonymous access
	 * @throws JargonException
	 */
	public static IRODSAccount instanceForAnonymous(final String host,
			final int port, final String homeDirectory, final String zone,
			final String defaultStorageResource) throws JargonException {
		return instance(host, port, PUBLIC_USERNAME, "", "", zone,
				defaultStorageResource);
	}

	/**
	 * Create a re-routed iRODS account using an initial account, and a host
	 * name to which the connection should be re-routed
	 * 
	 * @param initialAccount
	 *            {@link IRODSAccount} for the initial connection
	 * @param reroutedHostName
	 *            <code>String</code> with the host name to which the connection
	 *            should be routed.
	 * @return <code>IRODSAccount</code> connected to the new host.
	 * @throws JargonException
	 */
	public static IRODSAccount instanceForReroutedHost(
			final IRODSAccount initialAccount, final String reroutedHostName)
			throws JargonException {

		if (initialAccount == null) {
			throw new IllegalArgumentException("null initialAccount");
		}

		if (reroutedHostName == null || reroutedHostName.isEmpty()) {
			throw new IllegalArgumentException("null or empty reroutedHostName");
		}

		return new IRODSAccount(reroutedHostName, initialAccount.getPort(),
				initialAccount.getUserName(), initialAccount.getPassword(),
				initialAccount.getHomeDirectory(), initialAccount.getZone(),
				initialAccount.getDefaultStorageResource());

	}

	/**
	 * Creates an object to hold iRODS account information. Uses the
	 * GSSCredential to discover the connection information. Sets the
	 * authentication option to GSI_AUTH.
	 * 
	 * @param host
	 *            the iRODS server domain name
	 * @param port
	 *            the port on the iRODS server
	 * @param gssCredential
	 *            the org.ietf.jgss.GSSCredential object
	 */
	public static IRODSAccount instance(final String host, final int port,
			final GSSCredential gssCredential) {
		return new IRODSAccount(host, port, gssCredential, "", "");
	}

	/**
	 * Creates an object to hold iRODS account information. Uses the
	 * GSSCredential to discover the connection information.
	 * 
	 * @param host
	 *            the iRODS server domain name
	 * @param port
	 *            the port on the iRODS server
	 * @param userName
	 *            the user name
	 * @param gssCredential
	 *            the org.ietf.jgss.GSSCredential object
	 * @param homeDirectory
	 *            home directory on the iRODS
	 * @param zone
	 *            the IRODS zone
	 * @param defaultStorageResource
	 *            default storage resource
	 */
	public static IRODSAccount instance(final String host, final int port,
			final GSSCredential gssCredential, final String homeDirectory,
			final String defaultStorageResource) throws JargonException {
		if (host == null || gssCredential == null || homeDirectory == null
				|| defaultStorageResource == null) {
			throw new JargonException(
					"IRODSAccount initialized with null values");
		}

		return new IRODSAccount(host, port, gssCredential, homeDirectory,
				defaultStorageResource);
	}

	public IRODSAccount(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String zone,
			final String defaultStorageResource) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.homeDirectory = homeDirectory;
		this.zone = zone;
		this.defaultStorageResource = defaultStorageResource;
		this.serverDN = "";
		this.gssCredential = null;
		this.certificateAuthority = "";
	}

	@SuppressWarnings("unused")
	private IRODSAccount(final String host, final int port,
			final GSSCredential gssCredential) {
		this(host, port, gssCredential, "", "");
	}

	/**
	 * Creates an object to hold iRODS account information. Uses the
	 * GSSCredential to discover the connection information.
	 * 
	 * @param host
	 *            the iRODS server domain name
	 * @param port
	 *            the port on the iRODS server
	 * @param userName
	 *            the user name
	 * @param gssCredential
	 *            the org.ietf.jgss.GSSCredential object
	 * @param homeDirectory
	 *            home directory on the iRODS
	 * @param zone
	 *            the IRODS zone
	 * @param defaultStorageResource
	 *            default storage resource
	 */
	private IRODSAccount(final String host, final int port,
			final GSSCredential gssCredential, final String homeDirectory,
			final String defaultStorageResource) {

		this.host = host;
		this.port = port;
		this.userName = "";
		this.password = "";
		this.homeDirectory = homeDirectory;
		this.zone = "";
		this.defaultStorageResource = defaultStorageResource;
		this.serverDN = "";
		this.certificateAuthority = "";
		this.gssCredential = gssCredential;
	}

	/**
	 * Gets the default storage resource.
	 * 
	 * @return defaultStorageResource
	 */
	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}

	/**
	 * Get the authentication scheme used for login to iRODS
	 * 
	 * @return {@link AuthScheme} enum value
	 */
	public AuthScheme getAuthenticationScheme() {
		return authenticationScheme;
	}

	/**
	 * @return the Server DN string used by the client.
	 */
	public String getServerDN() {
		return serverDN;
	}

	/**
	 * @return the iRODS zone.
	 */
	public String getZone() {
		return zone;
	}

	// for GSI
	/**
	 * Gets the locations of the GSI Certificate Authority (CA). By default, the
	 * CA definition comes from the user's cog.properties file.
	 */
	public String getCertificateAuthority() {
		return certificateAuthority;
	}

	/**
	 * If one exists, gets the GSSCredential used to make a GSI authentication.
	 */
	public GSSCredential getGSSCredential() {
		return gssCredential;
	}

	/**
	 * Tests this local file system account object for equality with the given
	 * object. Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are account objects for the same filesystem.
	 * 
	 * @param obj
	 *            The object to be compared with this abstract pathname
	 * 
	 * @return <code>true</code> if and only if the objects are the same;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		try {
			if (obj == null) {
				return false;
			}

			IRODSAccount temp = (IRODSAccount) obj;

			if (!getHost().equals(temp.getHost())) {
				return false;
			}
			if (getPort() != temp.getPort()) {
				return false;
			}
			if (!getUserName().equals(temp.getUserName())) {
				return false;
			}
			if (!getPassword().equals(temp.getPassword())) {
				return false;
			}

			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return host.hashCode() + port + userName.hashCode()
				+ password.hashCode();
	}

	/**
	 * Returns a string representation of this file system object. The string is
	 * formated according to the iRODS URI model. Note: the user password will
	 * not be included in the URI.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("irods://");
		sb.append(getUserName());
		sb.append("@");
		sb.append(getHost());
		sb.append(":");
		sb.append(getPort());
		return sb.toString();
	}

	/**
	 * Return the URI representation of this Account object.
	 * 
	 * @param includePassword
	 *            If true, the account's password will be included in the URI,
	 *            if possible.
	 */
	public URI toURI(final boolean includePassword) throws JargonException {
		URI uri = null;
		try {
			if (includePassword) {
				uri = new URI("irods://" + getUserName() + "." + getZone()
						+ ":" + getPassword() + "@" + getHost() + ":"
						+ getPort() + getHomeDirectory());
			} else {
				uri = new URI("irods://" + getUserName() + "." + getZone()
						+ "@" + getHost() + ":" + getPort()
						+ getHomeDirectory());
			}
		} catch (URISyntaxException e) {
			throw new JargonException("cannot convert this account into a URI:"
					+ this, e);
		}
		return uri;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public GSSCredential getGssCredential() {
		return gssCredential;
	}

	public static boolean isDefaultObfuscate() {
		return defaultObfuscate;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}

	protected List<String> getAuthenticatedRoles() {
		return authenticatedRoles;
	}

	protected void setAuthenticatedRoles(final List<String> authenticatedRoles) {
		this.authenticatedRoles = authenticatedRoles;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(final String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @param authenticationScheme
	 *            the authenticationScheme to set
	 */
	public void setAuthenticationScheme(final AuthScheme authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}

	/*
	 * @param defaultStorageResource the defaultStorageResource to set
	 */
	public void setDefaultStorageResource(final String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * @param homeDirectory
	 *            the homeDirectory to set
	 */
	public void setHomeDirectory(final String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	/**
	 * Check if this is 'anonymous'
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if this is an
	 *         anonymous iRODS account
	 */
	public boolean isAnonymousAccount() {
		if (userName.equals(PUBLIC_USERNAME)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

}
