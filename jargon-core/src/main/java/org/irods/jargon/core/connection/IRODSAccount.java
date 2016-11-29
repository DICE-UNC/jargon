package org.irods.jargon.core.connection;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Representation of an identity on IRODS. Contains info similar to that
 * contained in the .irodsEnv file.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSAccount implements Serializable {

	private static final long serialVersionUID = 8627989693793656697L;
	public static final String IRODS_JARGON_RELEASE_NUMBER = "rods3.2";
	public static final String IRODS_API_VERSION = "d";

	public static final boolean defaultObfuscate = false;
	public static final String PUBLIC_USERNAME = "anonymous";
	private AuthScheme authenticationScheme = AuthScheme.STANDARD;

	private final String host;
	private final int port;
	private String userZone;
	private String userName;
	private final String proxyZone;
	private final String proxyName;
	private String password;
	private String defaultStorageResource;
	private String homeDirectory;

	/**
	 * Client-server negotiation policy. This is an override of the default
	 * negotiation policy settings derived from jargon properties. This allows a
	 * per-connection specification of an appropriate policy. If this is left
	 * <code>null</code> (the default), then the policy in jargon.properties is
	 * respected.
	 */
	private ClientServerNegotiationPolicy clientServerNegotiationPolicy;

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
	 * @param clientServerNegotiationPolicy
	 *            {@link ClientServerNegotiationPolicy} object describing
	 *            overrides from the default policy, may be set to
	 *            <code>null</code> to accept defaults from Jargon properties.
	 */
	public static IRODSAccount instance(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String zone,
			final String defaultStorageResource,
			final ClientServerNegotiationPolicy clientServerNegotiationPolicy)
			throws JargonException {
		return new IRODSAccount(host, port, userName, password, homeDirectory,
				zone, defaultStorageResource, userName, zone, null,
				clientServerNegotiationPolicy);
	}

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
		return new IRODSAccount(host, port, userName, password, homeDirectory,
				zone, defaultStorageResource, userName, zone, null, null);
	}

	/**
	 * Creates an object to hold iRODS account information. All parameters need
	 * to be initialized to use this initializer. Note that this instance method
	 * will set the auth scheme
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
	 * @param authenticationScheme
	 *            authenticationScheme to use
	 */
	public static IRODSAccount instance(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String zone,
			final String defaultStorageResource,
			final AuthScheme authenticationScheme) throws JargonException {

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

		IRODSAccount irodsAccount = new IRODSAccount(host, port, userName,
				password, homeDirectory, zone, defaultStorageResource);

		if (authenticationScheme == null) {
			throw new IllegalArgumentException("null authenticationScheme");
		}

		irodsAccount.setAuthenticationScheme(authenticationScheme);
		return irodsAccount;

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
				initialAccount.getDefaultStorageResource(),
				initialAccount.getProxyName(), initialAccount.getProxyZone(),
				null, initialAccount.getClientServerNegotiationPolicy());

	}

	/**
	 * Creates an object to hold iRODS account information for a proxied user.
	 * All parameters need to be initialized to use this initializer.
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
	 * @param userZone
	 *            the IRODS zone of the user
	 * @param defaultStorageResource
	 *            default storage resource
	 * @param proxyName
	 *            the name of the user's proxy
	 * @param proxyZone
	 *            the zone where the proxy is authenticated
	 */
	public static IRODSAccount instanceWithProxy(final String host,
			final int port, final String userName, final String password,
			final String homeDirectory, final String userZone,
			final String defaultStorageResource, final String proxyName,
			final String proxyZone, final AuthScheme authScheme,
			final ClientServerNegotiationPolicy clientServerNegotiationPolicy) {
		return new IRODSAccount(host, port, userName, password, homeDirectory,
				userZone, defaultStorageResource, proxyName, proxyZone,
				authScheme, clientServerNegotiationPolicy);
	}

	/**
	 * Creates an object to hold iRODS account information for a proxied user.
	 * All parameters need to be initialized to use this initializer.
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
	 * @param userZone
	 *            the IRODS zone of the user
	 * @param defaultStorageResource
	 *            default storage resource
	 * @param proxyName
	 *            the name of the user's proxy
	 * @param proxyZone
	 *            the zone where the proxy is authenticated
	 */
	public static IRODSAccount instanceWithProxy(final String host,
			final int port, final String userName, final String password,
			final String homeDirectory, final String userZone,
			final String defaultStorageResource, final String proxyName,
			final String proxyZone) {
		return new IRODSAccount(host, port, userName, password, homeDirectory,
				userZone, defaultStorageResource, proxyName, proxyZone, null,
				null);
	}

	/**
	 * Creates an iRODS account using a constructor
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
	 * @param userZone
	 *            the IRODS zone of the user
	 * @param defaultStorageResource
	 *            default storage resource
	 * @param clientServerNegotiationPolicy
	 *            {@link ClientServerNegotiationPolicy} object describing
	 *            overrides from the default policy, may be set to
	 *            <code>null</code> to
	 */
	public IRODSAccount(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String userZone,
			final String defaultStorageResource) {
		this.host = host;
		this.port = port;
		this.userName = userName;

		proxyName = userName;
		this.password = password;
		this.homeDirectory = homeDirectory;
		this.userZone = userZone;
		proxyZone = userZone;
		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * Comprehensive constructor that includes auth scheme, proxy information,
	 * and the ability to overide client-server negotiation settings on a
	 * per-transaction basis
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
	 * @param userZone
	 *            the IRODS zone of the user
	 * @param defaultStorageResource
	 *            default storage resource
	 * @param clientServerNegotiationPolicy
	 *            {@link ClientServerNegotiationPolicy} object describing
	 *            overrides from the default policy, may be set to <code>nu
	 * @param proxyName
	 *            the name of the user's proxy
	 * @param proxyZone
	 *            the zone where the proxy is authenticated
	 * @param authScheme
	 *            {@link AuthScheme}
	 * @param clientServerNegotiationPolicy
	 *            {@link ClientServerNegotiationPolicy} object describing
	 *            overrides from the default policy, may be set to <code>null
	 *            </code> to accept defaults from Jargon properties.
	 */
	private IRODSAccount(final String host, final int port,
			final String userName, final String password,
			final String homeDirectory, final String userZone,
			final String defaultStorageResource, final String proxyName,
			final String proxyZone, final AuthScheme authScheme,
			final ClientServerNegotiationPolicy clientServerNegotiationPolicy) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("host is null or empty");
		}
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		if (proxyName == null || proxyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty proxy name");
		}
		if (password == null) {
			throw new IllegalArgumentException("password is null");
		}
		if (homeDirectory == null) {
			throw new IllegalArgumentException("homeDirectory is null");
		}
		if (userZone == null || userZone.isEmpty()) {
			throw new IllegalArgumentException("user zone is null or empty");
		}
		if (proxyZone == null || proxyZone.isEmpty()) {
			throw new IllegalArgumentException("proxy zone is null or empty");
		}
		if (defaultStorageResource == null) {
			throw new IllegalArgumentException("defaultStorageResource is null");
		}
		if (authScheme == null) {
			authenticationScheme = AuthScheme.STANDARD;
		} else {
			authenticationScheme = authScheme;
		}

		this.host = host;
		this.port = port;
		this.userName = userName;
		this.proxyName = proxyName;
		this.password = password;
		this.homeDirectory = homeDirectory;
		this.userZone = userZone;
		this.proxyZone = proxyZone;
		this.defaultStorageResource = defaultStorageResource;
		this.clientServerNegotiationPolicy = clientServerNegotiationPolicy;
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
	 * @return the iRODS zone.
	 */
	public String getZone() {
		return userZone;
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

			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return host.hashCode() + port + userName.hashCode();
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
	 * Return the URI representation of this Account object. If the account uses
	 * a proxy user a password cannot be included in the URI.
	 *
	 * @param includePassword
	 *            If true, the account's password will be included in the URI,
	 *            if possible.
	 *
	 * @throws UnsupportedOperationException
	 *             This exception is thrown if an attempt is made to create a
	 *             irods URI that authenticates as a proxy user.
	 */
	public URI toURI(final boolean includePassword) throws JargonException {
		try {
			if (includePassword) {
				if (proxied()) {
					throw new UnsupportedOperationException(
							"irods URI scheme doesn't support authentication through a proxy.");
				}

				return new URI("irods://" + getUserName() + "." + getZone()
						+ ":" + getPassword() + "@" + getHost() + ":"
						+ getPort() + getHomeDirectory());
			} else {
				return new URI("irods://" + getUserName() + "." + getZone()
						+ "@" + getHost() + ":" + getPort()
						+ getHomeDirectory());
			}
		} catch (URISyntaxException e) {
			throw new JargonException("cannot convert this account into a URI:"
					+ this, e);
		}
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

	/**
	 * Returns the name of the user's proxy
	 *
	 * @return the proxy name
	 */
	public final String getProxyName() {
		return proxyName;
	}

	/**
	 * Returns the name of the zone where the proxy user is authenticated
	 *
	 * @return the zone name
	 */
	public final String getProxyZone() {
		return proxyZone;
	}

	public String getPassword() {
		return password;
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

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(final String zone) {
		userZone = zone;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	private boolean proxied() {
		if (!(getUserName().equals(getProxyName()))) {
			return true;
		} else if (!(getZone().equals(getProxyZone()))) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized ClientServerNegotiationPolicy getClientServerNegotiationPolicy() {
		return clientServerNegotiationPolicy;
	}

	public synchronized void setClientServerNegotiationPolicy(
			final ClientServerNegotiationPolicy clientServerNegotiationPolicy) {
		this.clientServerNegotiationPolicy = clientServerNegotiationPolicy;
	}

}
