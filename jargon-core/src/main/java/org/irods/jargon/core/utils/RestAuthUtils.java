/**
 * 
 */
package org.irods.jargon.core.utils;

import org.apache.commons.codec.binary.Base64;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpful utils for working with iRODS authentication in BasicAuthentication
 * scenarios
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RestAuthUtils {

	private static Logger log = LoggerFactory.getLogger(RestAuthUtils.class);

	/**
	 * Given an {@link IRODSAccount} get the basic auth token value
	 * 
	 * @param irodsAccount {@link IRODSAccount}
	 * @return {@code String} with an encoded basic auth value
	 */
	public static String basicAuthTokenFromIRODSAccount(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");

		StringBuilder toEncode = new StringBuilder();
		toEncode.append(irodsAccount.getUserName());
		toEncode.append(":");
		toEncode.append(irodsAccount.getPassword());

		sb.append(Base64.encodeBase64String(toEncode.toString().getBytes()));
		return sb.toString();
	}

	/**
	 * Given a basic auth header and defautl values, build an iRODS Account
	 * 
	 * @param basicAuthData          {@code String} with basic auth data
	 * @param host                   {@code String} with the configured iRODS host
	 * @param zone                   {@code String} with the configured iRODS zone
	 * @param port                   {@code int} with the configured iRODS port
	 * @param defaultStorageResource {@code String} with an optional default storage
	 *                               resource
	 * @return {@link IRODSAccount}
	 * @throws JargonException {@link JargonException}
	 */
	public static IRODSAccount getIRODSAccountFromBasicAuthValues(final String basicAuthData, final String host,
			final String zone, final int port, final String defaultStorageResource) throws JargonException {

		log.info("getIRODSAccountFromBasicAuthValues");

		if (basicAuthData == null || basicAuthData.isEmpty()) {
			throw new IllegalArgumentException("null or empty basicAuthData");
		}

		final int index = basicAuthData.indexOf(' ');
		log.info("index of end of basic prefix:{}", index);
		String auth = basicAuthData.substring(index);

		String decoded = new String(Base64.decodeBase64(auth));

		log.info("index of end of basic prefix:{}", index);
		if (decoded.isEmpty()) {
			throw new JargonException("user and password not in credentials");

		}
		final String[] credentials = decoded.split(":");

		if (credentials.length != 2) {
			throw new JargonException("user and password not in credentials");
		}

		log.info("see if auth scheme is overrideen by the provided credentials");
		/*
		 * Ids can be prepended with STANDARD: or PAM:
		 */

		String userId = credentials[0];
		AuthScheme authScheme = AuthScheme.STANDARD;
		if (userId.startsWith(AuthScheme.STANDARD.toString())) {
			log.info("authScheme override to Standard");
			authScheme = AuthScheme.STANDARD;
			userId = userId.substring(AuthScheme.STANDARD.toString().length() + 1);
		} else if (userId.startsWith(AuthScheme.PAM.toString())) {
			log.info("authScheme override to PAM");
			authScheme = AuthScheme.PAM;
			userId = userId.substring(AuthScheme.PAM.toString().length() + 1);
		}

		log.debug("userId:{}", userId);

		return IRODSAccount.instance(host, port, userId, credentials[1], "", zone, defaultStorageResource, authScheme);
	}

}
