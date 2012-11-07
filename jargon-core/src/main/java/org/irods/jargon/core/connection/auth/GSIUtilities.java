/**
 * 
 */
package org.irods.jargon.core.connection.auth;

import java.io.File;
import java.io.IOException;

import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.GSIIRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.LocalFileUtils;

/**
 * Utility classes for manipulating GSI grid certificates, covering operations
 * such as generating proxy certificates.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GSIUtilities {

	/**
	 * Create the <code>GSIIRODSAccount</code> with the certificate that exists
	 * in the given file
	 * 
	 * @param credentialFile
	 * @param host
	 * @param port
	 * @param userDistinguishedName
	 * @return
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final File credentialFile, final String host, final int port,
			final String userDistinguishedName) throws JargonException {

		if (credentialFile == null) {
			throw new IllegalArgumentException("null credentialFile");
		}

		if (!credentialFile.exists()) {
			throw new IllegalArgumentException("credentialFile not found");
		}

		try {
			byte[] certBytes = LocalFileUtils.getBytesFromFile(credentialFile);
			return createGSIIRODSAccountFromCredential(host, port,
					userDistinguishedName, certBytes);
		} catch (IOException e) {
			throw new JargonException(
					"io exception reading bytes from credential file", e);
		}

	}

	/**
	 * Create the <code>GSIIRODSAccount</code> with the certificate based on the
	 * contents of the given <code>String</code>
	 * 
	 * @param host
	 * @param port
	 * @param userDistinguishedName
	 * @param certificate
	 * @return
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final String host, final int port,
			final String userDistinguishedName, final String certificate)
			throws JargonException {

			if (certificate == null || certificate.isEmpty()) {
				throw new IllegalArgumentException("null or empty certificate");
			}
			
		if (!certificate.startsWith("-----BEGIN CERTIFICATE-----")) {
				throw new IllegalArgumentException("unrecognized certificate format, does not start with -----BEGIN CERTIFICATE-----");
			}
		
		byte[] data = certificate.getBytes();
		
		return createGSIIRODSAccountFromCredential(host, port,
				userDistinguishedName, data);

	}

	/**
	 * Create the <code>GSIIRODSAccount</code> with the certificate based on the
	 * contents of the given <code>byte[]</code>
	 * 
	 * @param host
	 * @param port
	 * @param userDistinguishedName
	 * @param certificate
	 * @return
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final String host, final int port,
			final String userDistinguishedName, final byte[] certificate)
			throws JargonException {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (userDistinguishedName == null || userDistinguishedName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty userDistinguishedName");
		}

		if (certificate == null || certificate.length == 0) {
			throw new IllegalArgumentException("null or empty certificate");
		}

		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
				.getInstance();

		try {
			GSSCredential credential = manager.createCredential(certificate,
					ExtendedGSSCredential.IMPEXP_OPAQUE,
					GSSCredential.DEFAULT_LIFETIME, null,
					GSSCredential.INITIATE_AND_ACCEPT);
			GSIIRODSAccount gsiIRODSAccount = GSIIRODSAccount.instance(host,
					port, userDistinguishedName, credential);
			return gsiIRODSAccount;
		} catch (GSSException e) {
			throw new JargonException("GSSException creating credential", e);
		}

	}

}
