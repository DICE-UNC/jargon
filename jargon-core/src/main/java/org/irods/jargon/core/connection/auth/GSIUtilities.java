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
	 * @param host
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param credentialFile
	 *            {@link File} for the user certificate (a GSI proxy
	 *            certificate)
	 * @param defaultStorageResource
	 *            <code>String</code> with an optional (blank if not specified)
	 *            default storage resource
	 * @return {@link GSIIRODSAccount} configured for use in authentication
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final File credentialFile, final String host, final int port,
			final String defaultStorageResource) throws JargonException {

		if (credentialFile == null) {
			throw new IllegalArgumentException("null credentialFile");
		}

		if (!credentialFile.exists()) {
			throw new IllegalArgumentException("credentialFile not found");
		}

		try {
			byte[] certBytes = LocalFileUtils.getBytesFromFile(credentialFile);
			return createGSIIRODSAccountFromCredential(host, port, certBytes,
					defaultStorageResource);
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
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param certficate
	 *            <code>String</code> with the GSI proxy certificate in
	 *            <code>String</code> form
	 * @param defaultStorageResource
	 *            <code>String</code> with an optional (blank if not specified)
	 *            default storage resource
	 * @return {@link GSIIRODSAccount} configured for use in authentication
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final String host, final int port, final String certificate,
			final String defaultStorageResource) throws JargonException {

		if (certificate == null || certificate.isEmpty()) {
			throw new IllegalArgumentException("null or empty certificate");
		}

		if (!certificate.startsWith("-----BEGIN CERTIFICATE-----")) {
			throw new IllegalArgumentException(
					"unrecognized certificate format, does not start with -----BEGIN CERTIFICATE-----");
		}

		byte[] data = certificate.getBytes();

		return createGSIIRODSAccountFromCredential(host, port, data,
				defaultStorageResource);

	}

	/**
	 * Create the <code>GSIIRODSAccount</code> with the certificate based on the
	 * contents of the given <code>byte[]</code>
	 * 
	 * @param host
	 *            <code>String</code> with the iRODS host name
	 * @param port
	 *            <code>int</code> with the iRODS server port
	 * @param certificate
	 *            GSI proxy certificate in the form of a <code>byte</code> array
	 * @param defaultStorageResource
	 *            <code>String</code> with an optional (blank if not specified)
	 *            default storage resource
	 * @return {@link GSIIRODSAccount} configured for use in authentication
	 * @throws JargonException
	 */
	public static GSIIRODSAccount createGSIIRODSAccountFromCredential(
			final String host, final int port, final byte[] certificate,
			final String defaultStorageResource) throws JargonException {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
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
					port, credential, defaultStorageResource);
			return gsiIRODSAccount;
		} catch (GSSException e) {
			throw new JargonException("GSSException creating credential", e);
		}

	}

	/**
	 * Derive the user distinguished name from the provided cert.
	 * 
	 * @param account
	 * @return
	 * @throws JargonException
	 */
	public static String getDN(final GSIIRODSAccount account)
			throws JargonException {
		StringBuffer dn = null;
		int index = -1, index2 = -1;

		dn = new StringBuffer(account.getDistinguishedName());

		// remove the extra /CN if exists
		index = dn.indexOf("UID");
		if (index >= 0) {
			index2 = dn.lastIndexOf("CN");
			if (index2 > index) {
				dn = dn.delete(index2 - 1, dn.length());
			}
		}

		// The DN gets returned with commas.
		index = dn.indexOf(",");
		while (index >= 0) {
			dn = dn.replace(index, index + 1, "/");
			index = dn.indexOf(",");
		}

		// add / to front if necessary
		if (dn.indexOf("/") != 0) {
			return "/" + dn;
		} else {
			return dn.toString();
		}
	}

}
