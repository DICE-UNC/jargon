/**
 * Utility classes for manipulating GSI grid certificates, covering operations such as generating proxy certificates.
 */
package org.irods.jargon.core.connection.auth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.proxy.ProxyPathValidator;
import org.globus.gsi.proxy.ProxyPathValidatorException;
import org.globus.gsi.proxy.ProxyPolicyHandler;
import org.globus.gsi.proxy.ext.GlobusProxyCertInfoExtension;
import org.globus.gsi.proxy.ext.ProxyCertInfo;
import org.globus.gsi.proxy.ext.ProxyCertInfoExtension;
import org.globus.util.Util;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GSIUtilities {

	protected X509Certificate[] certificates;
	protected int bits = 512;
	protected int lifetime = 3600 * 12;

	protected ProxyCertInfo proxyCertInfo = null;
	protected int proxyType;

	protected boolean quiet = false;
	protected boolean debug = false;
	protected boolean stdin = false;
	private PrivateKey userKey = null;

	public static final String GENERIC_POLICY_OID = "1.3.6.1.4.1.3536.1.1.1.8";

	protected GlobusCredential proxy = null;

	public void createProxy(final String cert, final String key,
			final boolean verify, final boolean globusStyle,
			final String proxyFile) {

		loadCertificates(cert);

		if (!quiet) {
			String dn = null;
			if (globusStyle) {
				dn = CertUtil.toGlobusID(getCertificate().getSubjectDN());
			} else {
				dn = getCertificate().getSubjectDN().getName();
			}
			System.out.println("Your identity: " + dn);
		}

		loadKey(key);

		if (debug) {
			System.out.println("Using " + bits + " bits for private key");
		}

		if (!quiet) {
			System.out.println("Creating proxy, please wait...");
		}

		sign();

		if (verify) {
			try {
				verify();
				System.out.println("Proxy verify OK");
			} catch (Exception e) {
				System.out.println("Proxy verify failed: " + e.getMessage());
				if (debug) {
					e.printStackTrace();
				}
				System.exit(-1);
			}
		}

		if (debug) {
			System.out.println("Saving proxy to: " + proxyFile);
		}

		if (!quiet) {
			System.out.println("Your proxy is valid until "
					+ proxy.getCertificateChain()[0].getNotAfter());
		}

		OutputStream out = null;
		try {
			File file = Util.createFile(proxyFile);
			// set read only permissions
			if (!Util.setOwnerAccessOnly(proxyFile)) {
				System.err
						.println("Warning: Please check file permissions for your proxy file.");
			}
			out = new FileOutputStream(file);
			// write the contents
			proxy.save(out);
		} catch (SecurityException e) {
			System.err.println("Failed to save proxy to a file: "
					+ e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Failed to save proxy to a file: "
					+ e.getMessage());
			System.exit(-1);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void loadCertificates(final String arg) {
		try {
			certificates = CertUtil.loadCertificates(arg);
		} catch (IOException e) {
			System.err.println("Error: Failed to load cert: " + arg);
			System.exit(-1);
		} catch (GeneralSecurityException e) {
			System.err.println("Error: Unable to load user certificate: "
					+ e.getMessage());
			System.exit(-1);
		}
	}

	public void loadKey(final String arg) {
		try {
			OpenSSLKey key = new BouncyCastleOpenSSLKey(arg);

			if (key.isEncrypted()) {
				String prompt = (quiet) ? "Enter GRID pass phrase: "
						: "Enter GRID pass phrase for this identity: ";

				String pwd = (stdin) ? Util.getInput(prompt) : Util
						.getPrivateInput(prompt);

				if (pwd == null) {
					System.exit(1);
				}

				key.decrypt(pwd);
			}

			userKey = key.getPrivateKey();

		} catch (IOException e) {
			System.err.println("Error: Failed to load key: " + arg);
			System.exit(-1);
		} catch (GeneralSecurityException e) {
			System.err.println("Error: Wrong pass phrase");
			if (debug) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
	}

	public X509Certificate getCertificate() {
		return this.certificates[0];
	}

	public void sign() {
		try {
			BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory
					.getDefault();

			X509ExtensionSet extSet = null;
			if (proxyCertInfo != null) {
				extSet = new X509ExtensionSet();
				if (CertUtil.isGsi4Proxy(proxyType)) {
					// RFC compliant OID
					extSet.add(new ProxyCertInfoExtension(proxyCertInfo));
				} else {
					// old OID
					extSet.add(new GlobusProxyCertInfoExtension(proxyCertInfo));
				}
			}

			proxy = factory.createCredential(certificates, userKey, bits,
					lifetime, proxyType, extSet);
		} catch (GeneralSecurityException e) {
			System.err.println("Failed to create a proxy: " + e.getMessage());
			System.exit(-1);
		}
	}

	// verifies the proxy credential
	public void verify() throws Exception {

		TrustedCertificates trustedCerts = TrustedCertificates
				.getDefaultTrustedCertificates();

		if (trustedCerts == null || trustedCerts.getCertificates() == null
				|| trustedCerts.getCertificates().length == 0) {
			throw new Exception("Unable to load CA ceritificates");
		}

		ProxyPathValidator validator = new ProxyPathValidator();

		if (proxyCertInfo != null) {
			String oid = proxyCertInfo.getProxyPolicy().getPolicyLanguage()
					.getId();
			validator.setProxyPolicyHandler(oid, new ProxyPolicyHandler() {
				public void validate(final ProxyCertInfo proxyCertInfo,
						final X509Certificate[] certPath, final int index)
						throws ProxyPathValidatorException {
					// ignore policy - this is just for proxy init case
					System.out.println("Proxy verify: Ignoring proxy policy");
					if (debug) {
						String policy = new String(proxyCertInfo
								.getProxyPolicy().getPolicy());
						System.out.println("Policy:");
						System.out.println(policy);
					}
				}

				@Override
				public void validate(final ProxyCertInfo arg0,
						final CertPath arg1, final int arg2)
						throws CertPathValidatorException {

				}
			});
		}

		validator.validate(proxy.getCertificateChain(),
				trustedCerts.getCertificates(), null,
				trustedCerts.getSigningPolicies());

	}

}
