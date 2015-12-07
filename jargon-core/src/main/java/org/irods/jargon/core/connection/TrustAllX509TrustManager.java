package org.irods.jargon.core.connection;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DO NOT USE IN PRODUCTION!!!!
 *
 * This class will simply trust everything that comes along.
 *
 * @author Mike Conway - DICE
 *
 */
public class TrustAllX509TrustManager implements X509TrustManager {

	private static final Logger log = LoggerFactory
			.getLogger(TrustAllX509TrustManager.class);

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		log.debug("getAcceptedIssuers()");
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(
			final java.security.cert.X509Certificate[] certs,
			final String authType) {
		log.debug("checkClientTrusted()");
	}

	@Override
	public void checkServerTrusted(
			final java.security.cert.X509Certificate[] certs,
			final String authType) {
		log.debug("checkServerTrusted()");
	}

}