package org.irods.jargon.core.utils;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;


/**
 * Helpful methods for parsing and dealing with IRODS URIs, also supports the
 * creation of <code>IRODSAccount</code> based on a given iRODS uri format
 * (irods://).
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSUriUtils {

	/**
	 * This class manages the serializing and deserializing the user info
	 * portion of an irods URI.
	 */
	public static final class UserInfo {

		private static final String ZONE_INDICATOR = ".";
		private static final String PASSWORD_INDICATOR = ":";
		private static final byte ESCAPE_INDICATOR = '%';

		private static final Set<Byte> USER_INFO_ALLOWED_CHARS;

		static {
			final byte[] allowedMarks = new byte[] {
					'-', '_', '!', '~', '*', '\'', '(', ')', ';', '&', '=', '+',
					'$', ','
			};

			final HashSet<Byte> chars = new HashSet<>();
			for (byte c = '0'; c <= '9'; c++) {
				chars.add(c);
			}
			for (byte c = 'a'; c <= 'z'; c++) {
				chars.add(c);
			}
			for (byte c = 'A'; c <= 'Z'; c++) {
				chars.add(c);
			}
			for (byte c : allowedMarks) {
				chars.add(c);
			}
			USER_INFO_ALLOWED_CHARS = Collections.unmodifiableSet(chars);
		}

		/**
		 * Creates an instance that can be used to for user authentication in a
		 * local or remote zone.
		 *
		 * @param user the username
		 * @param zone the authentication zone
		 * @param password the password used to authenticate the user
		 * @return It returns an instance.
		 */
		public static UserInfo instance(final String user, final String zone,
										final String password) {
			return new UserInfo(user, zone, password);
		}

		/**
		 * Creates an instance that can be used to for user authentication in
		 * the local zone.
		 *
		 * @param user the username
		 * @param password the password used to authenticate the user
		 * @return It returns an instance.
		 */
		public static UserInfo localInstance(final String user,
											 final String password)
		{
			return new UserInfo(user, null, password);
		}

		/**
		 * Creates an instance that doesn't contain authentication information
		 * for a local or remote zone.
		 *
		 * @param user the username
		 * @param zone the authentication zone
		 * @return It returns an instance.
		 */
		public static UserInfo unauthenticatedInstance(final String user,
													   final String zone) {
			return new UserInfo(user, zone, null);
		}

		/**
		 * Creates an instance that doesn't contain authentication information
		 * for the local zone.
		 *
		 * @param user the username
		 * @return It returns an instance.
		 */
		public static UserInfo unauthenticatedLocalInstance(final String user) {
			return new UserInfo(user, null, null);
		}

		/**
		 * Creates an instance from the serialized portion of the URI.
		 *
		 * @param encodedStr The serialized user info portion of an irods URI
		 * @return It returns an instance or <code>null</code> if infoStr is
		 * <code>null</code> or empty.
		 */
		static UserInfo fromString(final String encodedStr) {
			if (encodedStr == null || encodedStr.isEmpty()) {
				return null;
			}

			final int zIdx = encodedStr.indexOf(ZONE_INDICATOR);
			final int pIdx = encodedStr.indexOf(PASSWORD_INDICATOR);

			String encodedUser = null;
			String encodedZone = null;
			String encodedPassword = null;

			if (zIdx < 0 && pIdx < 0) {
			    // Only user
				encodedUser = encodedStr;
			} else if (zIdx < 0 && pIdx >= 0) {
			    // User and password
				encodedUser = encodedStr.substring(0, pIdx);
				encodedPassword = encodedStr.substring(pIdx + 1);
			} else if (zIdx >= 0 && pIdx <= zIdx) {
			    // User and zone
				encodedUser = encodedStr.substring(0, zIdx);
				encodedZone = encodedStr.substring(zIdx + 1);
			} else {
				// user, zone, and password
				encodedUser = encodedStr.substring(0, zIdx);
				encodedZone = encodedStr.substring(zIdx + 1, pIdx);
				encodedPassword = encodedStr.substring(pIdx + 1);
			}

			return new UserInfo(decode(encodedUser), decode(encodedZone),
					decode(encodedPassword));
		}

		private static String emptyAsNull(final String value) {
			return (value == null || value.isEmpty()) ? null : value;
		}

		private static String decode(final String encodedValue) {
			if (encodedValue == null) {
				return null;
			}
			final byte[] encoded = encodedValue.getBytes(
					StandardCharsets.US_ASCII);
			final ByteArrayOutputStream decoded = new ByteArrayOutputStream();
			for (int i = 0; i < encoded.length; i++) {
				if (encoded[i] == ESCAPE_INDICATOR) {
					final int ud = Character.digit(encoded[++i], 16);
					final int ld = Character.digit(encoded[++i], 16);
					decoded.write((ud << 4) + ld);
				} else {
					decoded.write(encoded[i]);
				}
			}
			return new String(decoded.toByteArray(), StandardCharsets.UTF_8);
		}

		private static String encode(final String value) {
			final byte[] decoded = value.getBytes(StandardCharsets.UTF_8);
			final ByteArrayOutputStream encoded = new ByteArrayOutputStream();
			for (int i = 0; i < decoded.length; i++) {
				final byte c = decoded[i];
				if (USER_INFO_ALLOWED_CHARS.contains(c)) {
					encoded.write(c);
				} else {
					final String hex = Integer.toHexString(c & 0xff);
					final byte[] digits = hex.toUpperCase().getBytes(
							StandardCharsets.US_ASCII);
					encoded.write(ESCAPE_INDICATOR);
					encoded.write(digits, 0, digits.length);
				}
			}
			return new String(encoded.toByteArray(), StandardCharsets.US_ASCII);
		}

		private final String user;
		private final String zone;
		private final String password;

		private UserInfo(final String user, final String zone,
						 final String password) {
			if (user == null || user.isEmpty()) {
				throw new IllegalArgumentException("must provide a user name");
			}

			this.user = user;
			this.zone = emptyAsNull(zone);
			this.password = emptyAsNull(password);
		}

		/**
		 * Serializes the object for inclusion in an irods URI.
		 * @return the user info portion of an irods URI
		 */
		@Override
		public String toString() {
			final StringBuilder encoded = new StringBuilder();
			encoded.append(encode(user));
			if (zone != null) {
				encoded.append(ZONE_INDICATOR).append(encode(zone));
			}
			if (password != null) {
				encoded.append(PASSWORD_INDICATOR).append(encode(password));
			}
			return encoded.toString();
		}

		/**
		 * @return the username
		 */
		public String getUserName() {
			return user;
		}

		/**
		 * @return the authentication zone
		 */
		public String getZone() {
			return zone;
		}

		/**
		 * @return the password used for authentication of the username
		 */
		public String getPassword() {
			return password;
		}

	}


	/**
	 * Objects of this class represent the case where an iRODS URI was expected,
	 * but another type of URI was provided.
	 */
	public static final class InvalidURIException extends JargonException {

		/**
		 * the constructor
		 *
		 * @param invalidURI the invalid URI
		 */
		InvalidURIException(final URI invalidURI) {
			super("The URI, " + invalidURI + ", is not an iRODS URI.");
		}

	}

	private static final String SCHEME = "irods";
	private static final String SCHEME_TERMINUS = "://";
	private static final String USER_INFO_TERMINUS = "@";
	private static final String PORT_INDICATOR = ":";
	private static final String PATH_SEPARATOR = "/";

	/**
	 * Retrieves the user information from the <code>URI</code>.
	 *
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return The user information if any is present, otherwise
	 * <code>null</code>.
	 * @throws InvalidURIException This is thrown when <code>irodsURI</code> is
	 * not an iRODS URI.
	 */
	public static UserInfo getUserInfo(final URI irodsURI)
			throws InvalidURIException {
		if (!isIRODSURIScheme(irodsURI)) {
			throw new InvalidURIException(irodsURI);
		}
		return UserInfo.fromString(irodsURI.getRawUserInfo());
	}

	/**
	 * Retrieve the iRODS user name from the <code>URI</code>, or
	 * <code>null</code> if this componenet cannot be derived from the
	 * <code>URI</code>
	 *
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return {@link String} with the discovered iRODS user name, or
	 *         <code>null</code> if the user name is not present.
	 * @throws InvalidURIException This is thrown when <code>irodsURI</code> is
	 * not an iRODS URI.
	 */
	public static String getUserName(final URI irodsURI)
			throws InvalidURIException {
		final UserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getUserName();
	}

	/**
	 * Get the zone (if available) from the <code>URI</code> in iRODS form.
	 *
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return {@link String} with the iRODS zone, or <code>null</code> if not
	 * available.
	 * @throws InvalidURIException This is thrown when <code>irodsURI</code> is
	 * not an iRODS URI.
	 */
	public static String getZone(final URI irodsURI) throws InvalidURIException
	{
		final UserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getZone();
	}

	/**
	 * Get the password (if available) from the {@link URI} in iRODS form.
	 *
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return {@link String} with the iRODS password, or <code>null</code> if
	 * not available.
	 * @throws InvalidURIException This is thrown when <code>irodsURI</code> is
	 * not an iRODS URI.
	 */
	public static String getPassword(final URI irodsURI)
			throws InvalidURIException {
		final UserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getPassword();
	}

	/**
	 * Retrieve the iRODS user name from the {@code URI}, or <code>null</code>
	 * if this componenet cannot be derived from the {@code URI}.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return {@link String} with the discovered iRODS user name, or
	 *         <code>null</code> if the user name is not present.
	 * @deprecated user {@link IRODSUriUtils#getUserName(URI)} instead.
	 */
	@Deprecated
	public static String getUserNameFromURI(final URI irodsURI) {
		final UserInfo info = UserInfo.fromString(irodsURI.getRawUserInfo());
		return info == null ? null : info.getUserName();
	}

	/**
	 * Get the parsed user information from the URI
	 * 
	 * @param irodsURI
	 * @return
	 * @deprecated Use {@link IRODSUriUtils#getUserInfo(URI)}
	 */
	@Deprecated
	protected static URIUserParts getURIUserPartsFromUserInfo(
			final URI irodsURI) {
		String userInfo = irodsURI.getUserInfo();

		if (userInfo == null || userInfo.isEmpty()) {
			return null;
		}


		/*
		 * parse out the userInfo part of the URI, which can have userName,
		 * password, and zone info user.zone:password
		 */

		int indexColon = userInfo.indexOf(":");
		int indexDot = userInfo.indexOf(".");

		// user should be everything left of the '.'
		String userName = null;

		if (indexDot != -1) {
			userName = userInfo.substring(0, indexDot);
			// normalize userName to null if empty
			if (userName.isEmpty()) {
				userName = null;
			}
		}

		String password = null;
		String zone = null;

		// password is after the ":", make null if blank
		if (userName != null) {
			if (indexColon != -1) {
				password = userInfo.substring(indexColon + 1);
				if (password.isEmpty()) {
					password = null;
				}
			}
		}

		// zone is either the whole thing, or after the username and ., and
		// before the colon, if it is found
		if (indexColon == -1 && indexDot == -1) {
			if (!userInfo.isEmpty()) {
				// zone is everything, or will be null
				zone = userInfo;
			}
		} else {
			// if user name and ., then substr start is the pos of the dot
			int substrStart = 0;
			int substrEnd = userInfo.length();
			if (indexDot != -1) {
				substrStart = indexDot + 1;
			}
			if (indexColon != -1) {
				substrEnd = indexColon;
			}
			zone = (userInfo.substring(substrStart, substrEnd));

		}

		URIUserParts uriUserParts = new URIUserParts();
		uriUserParts.setPassword(password);
		uriUserParts.setUserName(userName);
		uriUserParts.setZone(zone);
		return uriUserParts;

	}

	/**
	 * Get the password (if available) from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the iRODS password, or <code>null</code>
	 *         if not available.
	 * @deprecated use {@link IRODSUriUtils#getPassword(URI)} instead.
	 */
	@Deprecated
	public static String getPasswordFromURI(final URI irodsURI) {
		final UserInfo info = UserInfo.fromString(irodsURI.getRawUserInfo());
		return info == null ? null : info.getPassword();
	}

	/**
	 * Get the zone (if available) from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the iRODS zone, or <code>null</code> if
	 *         not available.
	 * @deprecated use {@link IRODSUriUtils#getZone(URI)} instead.
	 */
	@Deprecated
	public static String getZoneFromURI(final URI irodsURI) {
		final UserInfo info = UserInfo.fromString(irodsURI.getRawUserInfo());
		return info == null ? null : info.getZone();
	}

	/**
	 * Get the host (if available) from a URI.
	 * 
	 * @param uri the URI
	 * @return {@link String} with the host, or <code>null</code> if not
	 * available.
	 */
	public static String getHostFromURI(final URI uri) {
		return uri.getHost();
	}

	/**
	 * Get the port from the URI in iRODS form.
	 * 
	 * @param uri the URI
	 * @return <code>int</code> with the port.
	 */
	public static int getPortFromURI(final URI uri) {
		return uri.getPort();
	}

	/**
	 * Get the path from the URI in iRODS form.
	 * 
	 * @param uri the URI
	 * @return {@link String} with the path
	 */
	public static String getAbsolutePathFromURI(final URI uri) {
		return uri.getPath();
	}

	/**
	 * Build an {@link IRODSAccount} from the {@link URI} in iRODS format.
	 * 
	 * @param irodsURI
	 *            {@link URI} in <code>irods://</code> form
	 * @return {@link IRODSAccount} based on the URI information
	 * @throws JargonException
	 *             if the account cannot be built from the information in the
	 *             URI
	 */
	public static IRODSAccount getIRODSAccountFromURI(final URI irodsURI)
			throws JargonException {

		if (!isIRODSURIScheme(irodsURI)) {
			throw new InvalidURIException(irodsURI);
		}

		final UserInfo info = getUserInfo(irodsURI);

		if (info == null
				|| info.getPassword() == null
				|| info.getZone() == null) {
			throw new JargonException(
					"No user information in URI, cannot create iRODS account");
		}

		final String home = new StringBuilder()
				.append(PATH_SEPARATOR).append(info.getZone())
				.append(PATH_SEPARATOR).append("home")
				.append(PATH_SEPARATOR).append(info.getUserName())
				.toString();

		return IRODSAccount.instance(irodsURI.getHost(), irodsURI.getPort(),
				info.getUserName(), info.getPassword(), home, info.getZone(),
				"");
	}

	/**
	 * Test to see if the URI is of the iRODS scheme "irods://".
	 * 
	 * @param irodsURI
	 *            {@link URI} to check
	 * @return <code>boolean</code> which is <code>true</code> if this is the
	 *         iRODS URI scheme.
	 */
	public static boolean isIRODSURIScheme(final URI irodsURI) {
		return SCHEME.equals(irodsURI.getScheme());
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the root path for indicated host.
	 *
	 * @param host the iRODS server hosting the ICAT
	 * @param port the TCP port the server listens on
	 * @param userInfo the user information used for authentication and
	 *                    authorization
	 * @return the URI.
	 */
	public static URI buildBaseURI(final String host, final int port,
									final UserInfo userInfo) {
		try {
			final StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(SCHEME).append(SCHEME_TERMINUS);
			if (userInfo != null) {
				uriBuilder.append(userInfo).append(USER_INFO_TERMINUS);
			}
			uriBuilder.append(host).append(PORT_INDICATOR).append(port);
			uriBuilder.append(PATH_SEPARATOR);
			return new URI(uriBuilder.toString());
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the root path for indicated host.
	 *
	 * @param host the iRODS server hosting the ICAT
	 * @param port the TCP port the server listens on
	 * @param username the iRODS user name used for authorization
	 * @return the URI.
	 */
	public static URI buildBaseURI(final String host, final int port,
								   final String username) {
		final UserInfo info = UserInfo.instance(username, null, null);
		return buildBaseURI(host, port, info);
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the resource with the given path on the indicated host.
	 *
	 * @param host the iRODS server hosting the ICAT
	 * @param port the TCP port the server listens on
	 * @param userInfo the user information used for authentication and
	 *                    authorization
	 * @param absPath the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildURI(final String host, final int port,
							   final UserInfo userInfo, final String absPath) {
		try {
			final URI base = buildBaseURI(host, port, userInfo);
			return base.resolve(new URI(null, absPath, null));
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the resource with the given path on the indicated host.
	 *
	 * @param host the iRODS server hosting the ICAT
	 * @param port the TCP port the server listens on
	 * @param username the iRODS user name used for authorization
	 * @param absPath the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildURI(final String host, final int port,
							   final String username, final String absPath) {
		try {
			final URI base = buildBaseURI(host, port, username);
			return base.resolve(new URI(null, absPath, null));
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided anonymous
	 * authority identifying the resource with the given path on the indicated
	 * host.
	 *
	 * @param host the iRODS server hosting the ICAT
	 * @param port the TCP port the server listens on
	 * @param absPath the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildAnonymousURI(final String host, final int port,
										final String absPath) {
		if (absPath == null || absPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}
		try {
			return new URI(SCHEME, null, host, port, absPath, null, null);
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	/**
	 * Build a URI appropriate for a given iRODS account and absolute path.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} containing connection information
	 * @param irodsPath
	 * @return
	 */
	@Deprecated
	public static URI buildURIForAnAccountAndPath(
			final IRODSAccount irodsAccount, final String irodsPath) {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null iRODSAccount");
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		final UserInfo info = UserInfo.unauthenticatedLocalInstance(
						irodsAccount.getUserName());
		final String absPath = mkPathAbs(irodsAccount.getHomeDirectory(),
				irodsPath);
		return buildBaseURI(irodsAccount.getHost(), irodsAccount.getPort(), absPath,
				info);
	}

	/**
	 * Build a URI appropriate for a given iRODS account and absolute path.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} containing connection information
	 * @param irodsPath
	 * @return
	 */
	@Deprecated
	public static URI buildURIForAnAccountWithNoUserInformationIncluded(
			final IRODSAccount irodsAccount, final String irodsPath) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null iRODSAccount");
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		final String absPath = mkPathAbs(irodsAccount.getHomeDirectory(),
				irodsPath);
		return buildBaseURI(irodsAccount.getHost(), irodsAccount.getPort(), absPath,
				null);
	}

	@Deprecated
	private static URI buildBaseURI(final String host, final int port,
									final String absPath,
									final UserInfo userInfo) {
		try {
			final StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(SCHEME).append(SCHEME_TERMINUS);
			if (userInfo != null) {
				uriBuilder.append(userInfo).append(USER_INFO_TERMINUS);
			}
			uriBuilder.append(host).append(PORT_INDICATOR).append(port);
			uriBuilder.append(absPath);
			return new URI(uriBuilder.toString());
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	// if this is a relative path, use the user home directory to fashion an
	// absolute path
	@Deprecated
	private static String mkPathAbs(final String homeDir, final String path) {
		if (path.startsWith(PATH_SEPARATOR)) {
			return path;
		} else {
			return homeDir + PATH_SEPARATOR + path;
		}
	}

}

/**
 * Internal value class for parts of user info in the irods <code>URI</code>
 * scheme
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * @deprecated Use <code>IRODSUriUserInfo</code>
 */
@Deprecated
class URIUserParts {
	private String userName = "";
	private String password = "";
	private String zone = "";

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(final String zone) {
		this.zone = zone;
	}
}
