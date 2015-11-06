package org.irods.jargon.core.connection;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * DO NOT USE IN PRODUCTION!!!!
 * 
 * This class will simply trust everything that comes along.
 * 
 * @author Mike Conway - DICE
 *
 */
public class TrustAllX509TrustManager implements X509TrustManager {
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
	}

}