/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Captures characteristics resulting from a client-server negotiation
 * 
 * @author Mike Conway - DICE
 */
class NegotiatedClientServerConfiguration {
	private final boolean sslConnection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NegotiatedClientServerConfiguration [sslConnection=");
		builder.append(sslConnection);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the sslConnection
	 */
	boolean isSslConnection() {
		return sslConnection;
	}

	/**
	 * Default constructor
	 * 
	 * @param sslConnection
	 *            <code>boolean</code> that will be <code>true</code> if ssl is
	 *            used
	 */
	NegotiatedClientServerConfiguration(boolean sslConnection) {
		super();
		this.sslConnection = sslConnection;
	}

}
