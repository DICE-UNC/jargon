/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures the client/server negotiation process in the client given a
 * connection and other information.
 * 
 * @author Mike Conway - DICE
 *
 */
class ClientServerNegotiationService {

	private final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol;
	private final IRODSAccount irodsAccount;
	private final StartupResponseData startupResponseData;

	private Logger log = LoggerFactory
			.getLogger(ClientServerNegotiationService.class);

	/**
	 * @param irodsMidLevelProtocol
	 * @param irodsAccount
	 * @param startupResponseData
	 */
	ClientServerNegotiationService(
			AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			IRODSAccount irodsAccount, StartupResponseData startupResponseData) {
		super();
		this.irodsMidLevelProtocol = irodsMidLevelProtocol;
		this.irodsAccount = irodsAccount;
		this.startupResponseData = startupResponseData;
	}

	/**
	 * @return the irodsMidLevelProtocol
	 */
	AbstractIRODSMidLevelProtocol getIrodsMidLevelProtocol() {
		return irodsMidLevelProtocol;
	}

	/**
	 * @return the irodsAccount
	 */
	IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @return the startupResponseData
	 */
	StartupResponseData getStartupResponseData() {
		return startupResponseData;
	}

	/**
	 * Handy method to obtain a reference to the operative negotiation
	 * 
	 * @return
	 */
	private ClientServerNegotiationPolicy referToNegotiationPolicy() {
		return this.getIrodsMidLevelProtocol().getIrodsConnection()
				.getOperativeClientServerNegotiationPolicy();
	}

	/**
	 * Using the configured connection and account information, go through the
	 * iRODS client/server negotiation that occurs after the start-up pack has
	 * been processed.
	 * 
	 * @return
	 * @throws JargonException
	 */
	NegotiatedClientServerConfiguration negotiate() throws JargonException {
		log.info("negotiate()");
		NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

		// see if this is server negotiation

		if (this.referToNegotiationPolicy().getSslNegotiationPolicy() == SslNegotiationPolicy.NO_NEGOTIATION) {
			negotiatedClientServerConfiguration = new NegotiatedClientServerConfiguration(
					false);
		} else {
			negotiatedClientServerConfiguration = negotiateUsingServerProtocol();
		}

		log.info("negotiated configuration:{}",
				negotiatedClientServerConfiguration);
		return negotiatedClientServerConfiguration;

	}

	private NegotiatedClientServerConfiguration negotiateUsingServerProtocol() {
		log.info("negotiateUsingServerProtocol()");

		/*
		 * Analogous to irods_client_negotiation.cpp ~ line 250:
		 * client_server_negotiation_for_client
		 * 
		 * The startup pack was sent requesting negotiation, and I am here
		 * expecting a response to that negotiation
		 */

		return null;
	}
}
