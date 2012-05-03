
package org.irods.jargon.core.connection.auth;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.irods.jargon.core.connection.IRODSAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ServiceTicketGenerator implements
		PrivilegedExceptionAction<AuthResponse> {

	private final IRODSAccount irodsAccount;

	public static final Logger log = LoggerFactory
			.getLogger(ServiceTicketGenerator.class);

	/**
	 * @param irodsAccount
	 */
	ServiceTicketGenerator(IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccount.getAuthenticationScheme() != IRODSAccount.AuthScheme.KERBEROS) {
			throw new IllegalArgumentException(
					"irodsAccount does not indicate Kerberos");
		}

		if (irodsAccount.getServiceName() == null
				|| irodsAccount.getServiceName().isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAccount serviceName is blank");
		}

		this.irodsAccount = irodsAccount;
	}

	public AuthResponse run() throws Exception {
		AuthResponse response = new AuthResponse();
		response.setAuthenticatedIRODSAccount(irodsAccount);
		response.setAuthType("kerberos");
		try {
			// GSSAPI is generic, but if you give it the following Object ID,
			// it will create Kerberos 5 service tickets
			Oid kerberos5Oid = new Oid("1.2.840.113554.1.2.2");

			log.info("getting gssManager");
			// create a GSSManager, which will do the work
			GSSManager gssManager = GSSManager.getInstance();

			// tell the GSSManager the Kerberos name of the client and service
			// (substitute your appropriate names here)
			GSSName clientName = gssManager.createName(
					irodsAccount.getUserName(),
					GSSName.NT_USER_NAME);
			log.info("clientName:{}", clientName);
			GSSName serviceName = gssManager.createName(
					irodsAccount.getServiceName(), null);
			log.info("serviceName:{}", serviceName);

			// get the client's credentials. note that this run() method was
			// called by Subject.doAs(),
			// so the client's credentials (Kerberos TGT or Ticket-Granting
			// Ticket) are already available in the Subject
			GSSCredential clientCredentials = gssManager.createCredential(
					clientName, 8 * 60 * 60, kerberos5Oid,
					GSSCredential.INITIATE_ONLY);

			log.info("got client credential");

			// create a security context between the client and the service
			GSSContext gssContext = gssManager.createContext(serviceName,
					kerberos5Oid, clientCredentials,
					GSSContext.DEFAULT_LIFETIME);
			log.info("got gssContext:{}", gssContext);

			// initialize the security context
			// this operation will cause a Kerberos request of Active Directory,
			// to create a service ticket for the client to use the service
			byte[] serviceTicket = gssContext.initSecContext(new byte[0], 0, 0);
			response.getResponseProperties().put("subject", serviceTicket);
			gssContext.dispose();

			// return the Kerberos service ticket as an array of encrypted bytes
			log.info("formulated authResponse:{}", response);
			return response;
		} catch (Exception ex) {
			throw new PrivilegedActionException(ex);
		}
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}
