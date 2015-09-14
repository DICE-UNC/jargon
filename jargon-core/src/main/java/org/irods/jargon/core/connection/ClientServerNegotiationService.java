/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;

import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructInitNegotiation;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructNotifyServerOfResult;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructNotifyServerOfResult.Outcome;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.RandomUtils;
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
	public static final String NEGOTIATION_SHARED_SECRET = "SHARED_SECRET";

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
	 * <p/>
	 * This is triggered after the neg_pi has been sent from the server, see
	 * irods_client_negotiation.cpp ~ line 412
	 * 
	 * @return {@link NegotiatedClientServerConfiguration} with the negotiated
	 *         result
	 * @throws ClientServerNegotiationException
	 *             if the negotiation fails
	 * @throws JargonException
	 */
	StartupResponseData negotiate(
			final ClientServerNegotiationStructInitNegotiation struct)
			throws ClientServerNegotiationException, JargonException {
		log.info("negotiate()");

		if (struct == null) {
			throw new IllegalArgumentException("null struct");
		}
		return negotiateUsingServerProtocol(struct);
	}

	private StartupResponseData negotiateUsingServerProtocol(
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
		return notifyServerOfNegotiationSuccess(negotiatedOutcome);

	}

	/**
	 * After negotiation, notify the server. If the connection uses SSL, this is
	 * the point where the connection is manipulated to wrap the socket in ssl
	 * <p/>
	 * This should be processed in irods_client_negotiation.cpp line 463 -
	 * read_client_server_negotiation_message
	 * 
	 * @param negotiatedOutcome
	 * @return
	 * @throws JargonException
	 */
	private StartupResponseData notifyServerOfNegotiationSuccess(
			Outcome negotiatedOutcome) throws JargonException {
		ClientServerNegotiationStructNotifyServerOfResult struct = ClientServerNegotiationStructNotifyServerOfResult
				.instance(
						ClientServerNegotiationStructNotifyServerOfResult.STATUS_SUCCESS,
						negotiatedOutcome.name());
		Tag versionPiTag = this.irodsMidLevelProtocol
				.irodsFunctionForNegotiation(struct);

		/*
		 * This section maps to rodsAgent.cpp ~ line 235, where the versionPI is
		 * sent after negotation (after call to
		 * client_server_negotiation_for_server)
		 */

		StartupResponseData startupResponse = AuthMechanism
				.buldStartupResponseFromVersionPI(versionPiTag);
		startupResponse
				.setNegotiatedClientServerConfiguration(new NegotiatedClientServerConfiguration(
						negotiatedOutcome == Outcome.CS_NEG_USE_SSL));

		log.info("startupResponse captured:{}", startupResponse);

		wrapConnectionInSslIfConfigured(startupResponse);

		return startupResponse;

	}

	private void wrapConnectionInSslIfConfigured(
			final StartupResponseData startupResponse) throws JargonException,
			AssertionError {
		log.info("wrapConnectionInSsl()");
		// some sanity checking
		if (startupResponse.getNegotiatedClientServerConfiguration() == null) {
			throw new IllegalArgumentException(
					"null negotiatedClientServerConfiguration in startup response");
		}

		if (!startupResponse.getNegotiatedClientServerConfiguration()
				.isSslConnection()) {
			log.info("no ssl");
			return;
		}

		log.info("wrapping in ssl connection");
		SslConnectionUtilities sslConnectionUtilities = new SslConnectionUtilities(
				this.getIrodsMidLevelProtocol().getIrodsSession());
		sslConnectionUtilities
				.createSslSocketForProtocolAndIntegrateIntoProtocol(this
						.getIrodsMidLevelProtocol().getIrodsAccount(), this
						.getIrodsMidLevelProtocol(), false);

		configureParametersForParallelTransfer(startupResponse);

		getIrodsMidLevelProtocol().setStartupResponseData(startupResponse);
		log.info("connection now wrapped in ssl socket!");

	}

	/**
	 * Notify agent of preferences for encryption of parallel transfers based on
	 * jargon properties
	 * 
	 * @throws JargonException
	 */
	private void configureParametersForParallelTransfer(
			final StartupResponseData startupResponse) throws JargonException {
		log.info("configureParametersForParallelTransfer()");
		PipelineConfiguration myProps = PipelineConfiguration.instance(this
				.getIrodsMidLevelProtocol().getIrodsSession()
				.getJargonProperties());
		byte[] key = RandomUtils.generateRandomBytesOfLength(myProps
				.getEncryptionKeySize());
		startupResponse.getNegotiatedClientServerConfiguration()
				.setSslCryptKey(key);

		/*
		 * See irods/plugins/network/ssl/libssl.cpp ~line 757 for analagous code
		 */

		try {
			log.info("sending header with encryption cues");
			this.getIrodsMidLevelProtocol().sendHeader(
					myProps.getEncryptionAlgorithmEnum().getTextValue(),
					myProps.getEncryptionKeySize(),
					myProps.getEncryptionSaltSize(),
					myProps.getEncryptionNumberHashRounds(), 0);
			this.getIrodsMidLevelProtocol().getIrodsConnection().flush();
			log.info("now write the shared secret to iRODS");
			this.getIrodsMidLevelProtocol().irodsFunction(
					NEGOTIATION_SHARED_SECRET, key, null, 0, 0, null, 0, 0, 0);

		} catch (IOException e) {
			log.error("i/o exception sending encryption info", e);
			throw new JargonException("error sending encryption info");
		}

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
