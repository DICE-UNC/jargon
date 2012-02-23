package org.irods.jargon.core.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

/**
 * Helpful methods for parsing and dealing with IRODS URIs, also supports the
 * creation of <code>IRODSAccount</code> based on a given iRODS uri format
 * (irods://).
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSUriUtils {

	private static final char PATH_SEPARATOR = '/';

	/**
	 * Retrieve the iRODS user name from the <code>URI</code>, or
	 * <code>null</code> if this componenet cannot be derived from the
	 * <code>URI</code>
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the discovered iRODS user name, or
	 *         <code>null</code> if the user name is not present.
	 */
	public static String getUserNameFromURI(final URI irodsURI) {

		URIUserParts uriUserParts = getURIUserPartsFromUserInfo(irodsURI);
		return uriUserParts.getUserName();
	}

	/**
	 * Get the parsed user information from the URI
	 * 
	 * @param irodsURI
	 * @return
	 */
	protected static URIUserParts getURIUserPartsFromUserInfo(final URI irodsURI) {
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
	 */
	public static String getPasswordFromURI(final URI irodsURI) {
		URIUserParts uriUserParts = getURIUserPartsFromUserInfo(irodsURI);
		return uriUserParts.getPassword();
	}

	/**
	 * Get the zone (if available) from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the iRODS zone, or <code>null</code> if
	 *         not available.
	 */
	public static String getZoneFromURI(final URI irodsURI) {
		URIUserParts uriUserParts = getURIUserPartsFromUserInfo(irodsURI);
		return uriUserParts.getZone();
	}

	/**
	 * Get the host (if available) from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the iRODS host, or <code>null</code> if
	 *         not available.
	 */
	public static String getHostFromURI(final URI irodsURI) {
		return irodsURI.getHost();
	}

	/**
	 * Get the port from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>int</code> with the iRODS port.
	 */
	public static int getPortFromURI(final URI irodsURI) {
		return irodsURI.getPort();
	}

	/**
	 * Get the path from the <code>URI</code> in iRODS form.
	 * 
	 * @param irodsURI
	 *            {@link URI} in the <code>irods://</code> format
	 * @return <code>String</code> with the iRODS path
	 */
	public static String getAbsolutePathFromURI(final URI irodsURI) {
		return irodsURI.getPath();
	}

	/**
	 * Build an <code>IRODSAccount</code> from the <code>URI</code> in iRODS
	 * format.
	 * 
	 * @param irodsURI
	 *            {@link URI} in irods:// form
	 * @return {@link IRODSAccount} based on the URI information
	 * @throws JargonException
	 *             if the account cannot be built from the information in the
	 *             URI
	 */
	public static IRODSAccount getIRODSAccountFromURI(final URI irodsURI)
			throws JargonException {

		if (!isIRODSURIScheme(irodsURI)) {
			throw new JargonException(
					"cannot derive IRODSAccount, not an iRODS uri");
		}

		URIUserParts uriUserParts = getURIUserPartsFromUserInfo(irodsURI);

		if (uriUserParts.getPassword() == null
				|| uriUserParts.getUserName() == null
				|| uriUserParts.getZone() == null) {
			throw new JargonException(
					"No user information in URI, cannot create iRODS account");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(PATH_SEPARATOR);
		sb.append(uriUserParts.getZone());
		sb.append("/home/");
		sb.append(uriUserParts.getUserName());

		return IRODSAccount.instance(irodsURI.getHost(), irodsURI.getPort(),
				uriUserParts.getUserName(), uriUserParts.getPassword(),
				sb.toString(), uriUserParts.getZone(), "");

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

		boolean isURI = false;
		String uriScheme = irodsURI.getScheme();
		if (uriScheme != null && uriScheme.equals("irods")) {
			isURI = true;
		}
		return isURI;
	}

	/**
	 * Build a URI appropriate for a given iRODS account and absolute path. Note
	 * that if the
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} containing connection information
	 * @param isFile
	 * @param irodsPath
	 * @return
	 * @throws JargonException
	 */
	public static URI buildURIForAnAccountAndPath(
			final IRODSAccount irodsAccount,
			final String irodsPath) throws JargonException {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null iRODSAccount");
		}
		
		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		String absPath = irodsPath;
		// if this is a relative path, use the user home directory to fashion an
		// absolute path
		if (irodsPath.charAt(0) != '/') {
			StringBuilder sb = new StringBuilder();
			sb.append(irodsAccount.getHomeDirectory());
			sb.append("/");
			sb.append(irodsPath);
			absPath = sb.toString();
		}
	
		URI uri = null;

		try {
			uri = new URI("irods", irodsAccount.getUserName(),
						irodsAccount.getHost(), irodsAccount.getPort(),
 absPath,
					null,
						null);

		} catch (URISyntaxException e) {
		
			throw new JargonException(e);
		}

		return uri;
	}
	
}

/**
 * Internal value class for parts of user info in the irods <code>URI</code>
 * scheme
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
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
