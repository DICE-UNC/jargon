/**
 *
 */
package org.irods.jargon.core.connection;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ietf.jgss.GSSCredential;
import org.irods.jargon.core.exception.JargonException;

/**
 * Representation of an identity on IRODS. Contains info similar to that
 * contained in the .irodsEnv file. The main account attributes are immutable,
 * but certain elements are mutable as they may be updated during processing.
 * 
 * @author Mike Conway - DICE (www.irods.org) TODO: move from constants to enum
 * 
 */
public final class IRODSAccount implements Serializable {

	private static final long serialVersionUID = 8627989693793656697L;
	public static final String IRODS_VERSION_2_3 = "rods2.3jargon2.3";
	public static final String IRODS_JARGON_RELEASE_NUMBER = "rods2.3";

	/**
	 * Stores the org.ietf.jgss.GSSCredential, used in GSI connections to the
	 * iRODS.
	 */
	private transient final GSSCredential gssCredential;
	/**
	 * iRODS API version "d"
	 */
	public static final String IRODS_API_VERSION = "d";

	/**
	 * User name for anonymous login
	 */
	public static final String PUBLIC_USERNAME = "anonymous";

	/**
	 * User name for anonymous login
	 */
	public static final String STANDARD_PASSWORD = "PASSWORD";
	/**
	 * User name for anonymous login
	 */
	public static final String GSI_PASSWORD = "GSI";

	/**
	 * The certificate authority (CA) list. By default, the CA definition comes
	 * from the user's cog.properties file.
	 */
	private final String certificateAuthority;

	/**
	 * The iRODS version.
	 */

	public static final boolean defaultObfuscate = false;

	public static final int OPTION = 0;

	/**
	 * working with strings got annoying
	 */
	static Map<String, Float> versionNumber = new HashMap<String, Float>(10, 1);
	static {
		versionNumber.put(IRODS_VERSION_2_3, new Float(2.3));
	}

	private final String host;
	private final int port;
	private final String zone;
	private final String userName;
	private final String password;
	private final String defaultStorageResource;
	private final String homeDirectory;
	private static final String authenticationScheme = STANDARD_PASSWORD;
	private final String serverDN;
	private static final int obfuscate = 0;
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

		if (host == null || userName == null || password == null
				|| homeDirectory == null || zone == null
				|| defaultStorageResource == null) {
			throw new JargonException(
					"IRODSAccount values cannot be initialized with null");
		} else if (host.length() == 0 || userName.length() == 0
				|| password.length() == 0 
				|| zone.length() == 0) {
			throw new JargonException(
					"data cannot be blank when initializing with this method");
		}
		return new IRODSAccount(host, port, userName, password, homeDirectory,
				zone, defaultStorageResource);

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
	 * Gets the iRODS options.
	 * 
	 * @return options
	 */
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}

	/**
	 * Gets the iRODS version.
	 * 
	 * @return version
	 */
	public static String getVersion() {
		return IRODS_VERSION_2_3;
	}

	/**
	 * Gets the iRODS version.
	 * 
	 * @return version
	 */
	static float getVersionNumber() {
		return 2.2F; // TODO: what about all of this stuff? move to enum?
	}

	/**
	 * Gets the iRODS option. (Not sure what that is...)
	 * 
	 * @return version
	 */
	static int getOption() {
		return 0;
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
				uri = new URI("irods://" + getUserName() + ":" + getPassword()
						+ "@" + getHost() + ":" + getPort()
						+ getHomeDirectory());
			} else {
				uri = new URI("irods://" + getUserName() + "@" + getHost()
						+ ":" + getPort() + getHomeDirectory());
			}
		} catch (URISyntaxException e) {
			throw new JargonException("cannot convert this account into a URI:"
					+ this, e);
		}
		return uri;
	}

	public static String getIrodsVersion22() {
		return IRODS_VERSION_2_3;
	}

	public static String getIrodsJargonReleaseNumber() {
		return IRODS_JARGON_RELEASE_NUMBER;
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

	public int getObfuscate() {
		return obfuscate;
	}

	public GSSCredential getGssCredential() {
		return gssCredential;
	}

	public static String getIrodsApiVersion() {
		return IRODS_API_VERSION;
	}

	public static String getPublicUsername() {
		return PUBLIC_USERNAME;
	}

	public static String getStandardPassword() {
		return STANDARD_PASSWORD;
	}

	public static String getGsiPassword() {
		return GSI_PASSWORD;
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

}
