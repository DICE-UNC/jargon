/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructInitNegotiation;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructNotifyServerOfResult;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructNotifyServerOfResult.Outcome;
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

	private Logger log = LoggerFactory
			.getLogger(ClientServerNegotiationService.class);

	/*
	 * captures negotiation decision table per c code
	 */
	private Outcome[][] negotiationTable = new Outcome[3][3];

	/**
	 * Default constructor takes the mid level protocol object that represents
	 * the iRODS agent connection for which transport negotiation will be done
	 * 
	 * @param irodsMidLevelProtocol
	 *            {@link AbstractIRODSMidLevelProtocol} for negotiation
	 */
	ClientServerNegotiationService(
			AbstractIRODSMidLevelProtocol irodsMidLevelProtocol) {
		super();
		this.irodsMidLevelProtocol = irodsMidLevelProtocol;
		initializeNegotiationTable();
	}

	private void initializeNegotiationTable() {

		negotiationTable[0][0] = Outcome.CS_NEG_USE_SSL; // REQ, REQ
		negotiationTable[0][1] = Outcome.CS_NEG_USE_SSL; // REQ, DC
		negotiationTable[0][2] = Outcome.CS_NEG_USE_TCP; // REQ, REF (failure)
		negotiationTable[1][0] = Outcome.CS_NEG_USE_SSL; // DC, REQ
		negotiationTable[1][1] = Outcome.CS_NEG_USE_SSL; // DC, DC
		negotiationTable[1][2] = Outcome.CS_NEG_USE_TCP; // DC, REF
		negotiationTable[2][0] = Outcome.CS_NEG_FAILURE; // REF, REQ
		negotiationTable[2][1] = Outcome.CS_NEG_USE_TCP; // REF, DC
		negotiationTable[2][2] = Outcome.CS_NEG_USE_TCP; // REF, REF

	}

	/**
	 * @return the irodsMidLevelProtocol
	 */
	AbstractIRODSMidLevelProtocol getIrodsMidLevelProtocol() {
		return irodsMidLevelProtocol;
	}

	/**
	 * Handy method to obtain a reference to the operative negotiation policy.
	 * This represents the expression of the client's desire,either from the
	 * jargon props, or overridden in IRODSAccount.
	 * 
	 * @return {@link ClientServerNegotiationPolicy} with the client position on
	 *         negotiation
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
	 * @return {@link NegotiatedClientServerConfiguration} with the negotiated
	 *         result
	 * @throws ClientServerNegotiationException
	 *             if the negotiation fails
	 * @throws JargonException
	 */
	NegotiatedClientServerConfiguration negotiate(
			final ClientServerNegotiationStructInitNegotiation struct)
			throws ClientServerNegotiationException, JargonException {
		log.info("negotiate()");

		if (struct == null) {
			throw new IllegalArgumentException("null struct");
		}

		NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

		// see if this is server negotiation

		if (this.referToNegotiationPolicy().getSslNegotiationPolicy() == SslNegotiationPolicy.NO_NEGOTIATION) {
			negotiatedClientServerConfiguration = new NegotiatedClientServerConfiguration(
					false);
		} else {
			negotiatedClientServerConfiguration = negotiateUsingServerProtocol(struct);
		}

		log.info("negotiated configuration:{}",
				negotiatedClientServerConfiguration);
		return negotiatedClientServerConfiguration;

	}

	private NegotiatedClientServerConfiguration negotiateUsingServerProtocol(
			ClientServerNegotiationStructInitNegotiation struct)
			throws ClientServerNegotiationException, JargonException {
		log.info("negotiateUsingServerProtocol()");
		log.info("negotiation over response from server:{}", struct);
		log.info("client policy:{}", referToNegotiationPolicy());

		/*
		 * Analogous to irods_client_negotiation.cpp ~ line 250:
		 * client_server_negotiation_for_client
		 * 
		 * The startup pack was sent requesting negotiation, and I am here
		 * expecting a response to that negotiation
		 */

		Outcome negotiatedOutcome = negotiationTable[referToNegotiationPolicy()
				.getSslNegotiationPolicy().ordinal()][struct
				.getSslNegotiationPolicy().ordinal()];
		log.info("negotiatedOutcome:{}", negotiatedOutcome);

		if (negotiatedOutcome == Outcome.CS_NEG_FAILURE) {
			log.error("failure in client server negotiation!...sending error message to the server before throwing the failure exception");
			notifyServerOfNegotiationFailure();
			throw new ClientServerNegotiationException(
					"failure in client server negotiation");
		}

		log.info("was a success, return choice to server");
		notifyServerOfNegotiationSuccess(negotiatedOutcome);

		return new NegotiatedClientServerConfiguration(
				negotiatedOutcome == Outcome.CS_NEG_USE_SSL);

	}

	private void notifyServerOfNegotiationSuccess(Outcome negotiatedOutcome)
			throws JargonException {
		ClientServerNegotiationStructNotifyServerOfResult struct = ClientServerNegotiationStructNotifyServerOfResult
				.instance(
						ClientServerNegotiationStructNotifyServerOfResult.STATUS_SUCCESS,
						negotiatedOutcome.name());
		this.irodsMidLevelProtocol.irodsFunctionForNegotiation(struct);

	}

	/**
	 * After negotiation that results in a failure, send a failure message back
	 * to the server
	 * 
	 * @throws JargonException
	 */
	private void notifyServerOfNegotiationFailure() throws JargonException {
		ClientServerNegotiationStructNotifyServerOfResult struct = ClientServerNegotiationStructNotifyServerOfResult
				.instanceForFailure();
		this.irodsMidLevelProtocol.irodsFunction(struct);
	}
}
