package org.irods.jargon.core.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.InvalidIRODSUriException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * Helpful methods for parsing and dealing with IRODS URIs, also supports the
 * creation of {@code IRODSAccount} based on a given iRODS uri format
 * (irods://).
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSUriUtils {

	private static final String SCHEME = "irods";
	private static final String SCHEME_TERMINUS = "://";
	private static final String USER_INFO_TERMINUS = "@";
	private static final String PORT_INDICATOR = ":";
	private static final String PATH_SEPARATOR = "/";

	/**
	 * Retrieves the user information from the {@code URI}.
	 *
	 * @param irodsURI
	 *            {@link URI} in the {@code irods://} format
	 * @return The user information if any is present, otherwise {@code null}.
	 * @throws InvalidIRODSUriException
	 *             This is thrown when {@code irodsURI} is not an iRODS URI.
	 */
	public static IRODSUriUserInfo getUserInfo(final URI irodsURI) throws InvalidIRODSUriException {
		if (!isIRODSURIScheme(irodsURI)) {
			throw new InvalidIRODSUriException(irodsURI);
		}
		return IRODSUriUserInfo.fromString(irodsURI.getRawUserInfo());
	}

	/**
	 * Retrieve the iRODS user name from the {@code URI}, or {@code null} if this
	 * componenet cannot be derived from the {@code URI}
	 *
	 * @param irodsURI
	 *            {@link URI} in the {@code irods://} format
	 * @return {@link String} with the discovered iRODS user name, or {@code null}
	 *         if the user name is not present.
	 * @throws InvalidIRODSUriException
	 *             This is thrown when {@code irodsURI} is not an iRODS URI.
	 */
	public static String getUserName(final URI irodsURI) throws InvalidIRODSUriException {
		final IRODSUriUserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getUserName();
	}

	/**
	 * Get the zone (if available) from the {@code URI} in iRODS form.
	 *
	 * @param irodsURI
	 *            {@link URI} in the {@code irods://} format
	 * @return {@link String} with the iRODS zone, or {@code null} if not available.
	 * @throws InvalidIRODSUriException
	 *             This is thrown when {@code irodsURI} is not an iRODS URI.
	 */
	public static String getZone(final URI irodsURI) throws InvalidIRODSUriException {
		final IRODSUriUserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getZone();
	}

	/**
	 * Get the password (if available) from the {@link URI} in iRODS form.
	 *
	 * @param irodsURI
	 *            {@link URI} in the {@code irods://} format
	 * @return {@link String} with the iRODS password, or {@code null} if not
	 *         available.
	 * @throws InvalidIRODSUriException
	 *             This is thrown when {@code irodsURI} is not an iRODS URI.
	 */
	public static String getPassword(final URI irodsURI) throws InvalidIRODSUriException {
		final IRODSUriUserInfo info = getUserInfo(irodsURI);
		return info == null ? null : info.getPassword();
	}

	/**
	 * Get the host (if available) from a URI.
	 *
	 * @param uri
	 *            the URI
	 * @return {@link String} with the host, or {@code null} if not available.
	 */
	public static String getHostFromURI(final URI uri) {
		return uri.getHost();
	}

	/**
	 * Get the port from the URI in iRODS form.
	 *
	 * @param uri
	 *            the URI
	 * @return {@code int} with the port.
	 */
	public static int getPortFromURI(final URI uri) {
		return uri.getPort();
	}

	/**
	 * Get the path from the URI in iRODS form.
	 *
	 * @param uri
	 *            the URI
	 * @return {@link String} with the path
	 */
	public static String getAbsolutePathFromURI(final URI uri) {
		return uri.getPath();
	}

	/**
	 * Build an {@link IRODSAccount} from the {@link URI} in iRODS format.
	 *
	 * @param irodsURI
	 *            {@link URI} in {@code irods://} form
	 * @return {@link IRODSAccount} based on the URI information
	 * @throws JargonException
	 *             if the account cannot be built from the information in the URI
	 */
	public static IRODSAccount getIRODSAccountFromURI(final URI irodsURI) throws JargonException {

		if (!isIRODSURIScheme(irodsURI)) {
			throw new InvalidIRODSUriException(irodsURI);
		}

		final IRODSUriUserInfo info = getUserInfo(irodsURI);

		if (info == null || info.getPassword() == null || info.getZone() == null) {
			throw new JargonException("No user information in URI, cannot create iRODS account");
		}

		final String home = PATH_SEPARATOR + info.getZone() + PATH_SEPARATOR + "home" + PATH_SEPARATOR
				+ info.getUserName();

		return IRODSAccount.instance(irodsURI.getHost(), irodsURI.getPort(), info.getUserName(), info.getPassword(),
				home, info.getZone(), "");
	}

	/**
	 * Test to see if the URI is of the iRODS scheme "irods://".
	 *
	 * @param irodsURI
	 *            {@link URI} to check
	 * @return {@code boolean} which is {@code true} if this is the iRODS URI
	 *         scheme.
	 */
	public static boolean isIRODSURIScheme(final URI irodsURI) {
		return SCHEME.equals(irodsURI.getScheme());
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the root path for indicated host.
	 *
	 * @param host
	 *            the iRODS server hosting the ICAT
	 * @param port
	 *            the TCP port the server listens on
	 * @param userInfo
	 *            the user information used for authentication and authorization
	 * @return the URI.
	 */
	public static URI buildBaseURI(final String host, final int port, final IRODSUriUserInfo userInfo) {
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
	 * @param host
	 *            the iRODS server hosting the ICAT
	 * @param port
	 *            the TCP port the server listens on
	 * @param username
	 *            the iRODS user name used for authorization
	 * @return the URI.
	 */
	public static URI buildBaseURI(final String host, final int port, final String username) {
		final IRODSUriUserInfo info = IRODSUriUserInfo.instance(username, null, null);
		return buildBaseURI(host, port, info);
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided authority,
	 * identifying the resource with the given path on the indicated host.
	 *
	 * @param host
	 *            the iRODS server hosting the ICAT
	 * @param port
	 *            the TCP port the server listens on
	 * @param userInfo
	 *            the user information used for authentication and authorization
	 * @param absPath
	 *            the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildURI(final String host, final int port, final IRODSUriUserInfo userInfo,
			final String absPath) {
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
	 * @param host
	 *            the iRODS server hosting the ICAT
	 * @param port
	 *            the TCP port the server listens on
	 * @param username
	 *            the iRODS user name used for authorization
	 * @param absPath
	 *            the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildURI(final String host, final int port, final String username, final String absPath) {
		try {
			final URI base = buildBaseURI(host, port, username);
			return base.resolve(new URI(null, absPath, null));
		} catch (final URISyntaxException e) {
			throw new JargonRuntimeException(e);
		}
	}

	/**
	 * Constructs a {@link URI} with the iRODS scheme with provided anonymous
	 * authority identifying the resource with the given path on the indicated host.
	 *
	 * @param host
	 *            the iRODS server hosting the ICAT
	 * @param port
	 *            the TCP port the server listens on
	 * @param absPath
	 *            the absolute logical path to the resource
	 * @return the URI.
	 */
	public static URI buildAnonymousURI(final String host, final int port, final String absPath) {
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
	 * Build a URI appropriate for a given iRODS account and path. If the path is
	 * relative, it is assumed to be relative to the account's home collection.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} containing connection information
	 * @param irodsPath
	 *            the path
	 * @return the URI
	 */
	public static URI buildURIForAnAccountAndPath(final IRODSAccount irodsAccount, final String irodsPath) {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null iRODSAccount");
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		final IRODSUriUserInfo info = IRODSUriUserInfo.unauthenticatedLocalInstance(irodsAccount.getUserName());
		final String absPath = mkPathAbs(irodsAccount.getHomeDirectory(), irodsPath);
		return buildBaseURI(irodsAccount.getHost(), irodsAccount.getPort(), absPath, info);
	}

	/**
	 * Build a URI appropriate for a given iRODS account and absolute path.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} containing connection information
	 * @param irodsPath
	 *            the path
	 * @return the URI
	 */
	public static URI buildURIForAnAccountWithNoUserInformationIncluded(final IRODSAccount irodsAccount,
			final String irodsPath) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null iRODSAccount");
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		final String absPath = mkPathAbs(irodsAccount.getHomeDirectory(), irodsPath);
		return buildBaseURI(irodsAccount.getHost(), irodsAccount.getPort(), absPath, null);
	}

	private static URI buildBaseURI(final String host, final int port, final String absPath,
			final IRODSUriUserInfo userInfo) {
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
	private static String mkPathAbs(final String homeDir, final String path) {
		if (path.startsWith(PATH_SEPARATOR)) {
			return path;
		} else {
			return homeDir + PATH_SEPARATOR + path;
		}
	}

}
