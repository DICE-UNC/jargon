/**
 * 
 */
package org.irods.jargon.core.connection;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Protocol manager that will hand out connections based on per-role proxy
 * accounts TODO: work in progress
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSProxyingProtocolManager implements IRODSProtocolManager {

	@SuppressWarnings("unused")
	private final List<ConnectionProxyDefinition> connectionProxyDefinitions;

	public static IRODSProxyingProtocolManager instance(
			final List<ConnectionProxyDefinition> connectionProxyDefinitions)
			throws JargonException {
		return new IRODSProxyingProtocolManager(connectionProxyDefinitions);
	}

	private IRODSProxyingProtocolManager(
			final List<ConnectionProxyDefinition> connectionProxyDefinitions)
			throws JargonException {
		if (connectionProxyDefinitions == null
				|| connectionProxyDefinitions.isEmpty()) {
			throw new JargonException(
					"null or empty connection proxy definitions");
		}

		this.connectionProxyDefinitions = connectionProxyDefinitions;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#destroy()
	 */
	@Override
	public void destroy() throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSProtocolManager#getIRODSProtocol
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSCommands getIRODSProtocol(final IRODSAccount irodsAccount, final PipelineConfiguration pipelineConfiguration)
			throws JargonException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#initialize()
	 */
	@Override
	public void initialize() throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.connection.IRODSProtocolManager#
	 * returnConnectionWithIoException
	 * (org.irods.jargon.core.connection.IRODSManagedConnection)
	 */
	@Override
	public void returnConnectionWithIoException(
			final IRODSManagedConnection irodsConnection) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.IRODSProtocolManager#returnIRODSConnection
	 * (org.irods.jargon.core.connection.IRODSManagedConnection)
	 */
	@Override
	public void returnIRODSConnection(
			final IRODSManagedConnection irodsConnection)
			throws JargonException {

	}

}
