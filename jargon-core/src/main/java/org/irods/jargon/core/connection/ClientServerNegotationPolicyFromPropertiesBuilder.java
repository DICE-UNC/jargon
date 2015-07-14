/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Utility to build a {@link ClientServerNegotationPolicy} from the jargon default properties
 * @author Mike Conway - DICE
 */
public class ClientServerNegotationPolicyFromPropertiesBuilder {
	
	private final IRODSSession irodsSession;

	/**
	 * Default constructor that accepts an <code>IRODSSession</code>
	 * @param irodsSession {@link IRODSSession} with a link to properties and settings
	 */
	public ClientServerNegotationPolicyFromPropertiesBuilder(final IRODSSession irodsSession) {
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}

		this.irodsSession = irodsSession;
		
	}
	
	/**
	 * Build a negotiation policy from defaults
	 * @return {@link ClientServerNegotiationPolicy}
	 */
	public ClientServerNegotiationPolicy buildClientServerNegotiationPolicyFromJargonProperties() {
		
		JargonProperties jargonProperties = irodsSession.getJargonProperties();
		
		ClientServerNegotiationPolicy clientServerNegotiationPolicy = new ClientServerNegotiationPolicy();
		clientServerNegotiationPolicy.setNegotiationPolicy(jargonProperties.getNegotiationPolicy());
		return clientServerNegotiationPolicy;
	
	}
	
}
