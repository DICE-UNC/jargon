/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General base class for objects that interact with IRODS through a connection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class IRODSGenericAO implements IRODSAccessObject {

	private final IRODSSession irodsSession;
	private final IRODSAccount irodsAccount;

	private static final Logger log = LoggerFactory
			.getLogger(IRODSGenericAO.class);

	/**
	 * Constructor that initializes the access object with a pointer to the
	 * connection information, as well as the session manager that controls
	 * connections.
	 * 
	 * @param irodsSession
	 *            {@link org.irods.jargon.core.connection.IRODSSession} that
	 *            will manage connecting to iRODS.
	 * @param irodsAccount
	 *            (@link org.irods.jargon.core.connection.IRODSAccount} that
	 *            contains the connection information used to get a connection
	 *            from the <code>irodsSession</code>
	 * @throws JargonException
	 */
	public IRODSGenericAO(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		if (irodsSession == null) {
			throw new JargonException("IRODSSession is null");
		}

		if (irodsAccount == null) {
			throw new JargonException("IRODSAccount is null");
		}

		this.irodsSession = irodsSession;
		this.irodsAccount = irodsAccount;

		log.debug("establishing connection");
		irodsSession.currentConnection(irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSSession()
	 */
	@Override
	public final IRODSSession getIRODSSession() {
		return getIrodsSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSAccount()
	 */
	@Override
	public final IRODSAccount getIRODSAccount() {
		return getIrodsAccount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObject#getIRODSServerProperties()
	 */
	@Override
	public final IRODSServerProperties getIRODSServerProperties()
			throws JargonException {
		return getIrodsSession().currentConnection(getIrodsAccount())
				.getIRODSServerProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSProtocol()
	 */
	@Override
	public final IRODSCommands getIRODSProtocol() throws JargonException {
		return getIrodsSession().currentConnection(getIrodsAccount());
	}

	/**
	 * @return the irodsSession
	 */
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}
