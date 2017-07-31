package org.irods.jargon.core.connection;

/**
 * Factory to create {@code IRODSProtocol} implementations. This allows
 * plug-ability of lower level communication strategies at the network level.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSProtocolFactory {

	public enum ProtocolType {
		CLASSIC, BUFFERED_SOCKET
	}

}
