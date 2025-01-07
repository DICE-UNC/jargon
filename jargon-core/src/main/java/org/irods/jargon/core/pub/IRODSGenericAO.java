/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.OperationComplete;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General base class for objects that interact with IRODS through a connection.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class IRODSGenericAO implements IRODSAccessObject {

	private final IRODSSession irodsSession;
	private final IRODSAccount irodsAccount;
	private final boolean instrumented;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private static final Logger log = LogManager.getLogger(IRODSGenericAO.class);

	/**
	 * Constructor that initializes the access object with a pointer to the
	 * connection information, as well as the session manager that controls
	 * connections.
	 *
	 * @param irodsSession {@link org.irods.jargon.core.connection.IRODSSession}
	 *                     that will manage connecting to iRODS.
	 * @param irodsAccount (@link org.irods.jargon.core.connection.IRODSAccount}
	 *                     that contains the connection information used to get a
	 *                     connection from the {@code irodsSession}
	 * @throws JargonException for iRODS error
	 */
	public IRODSGenericAO(final IRODSSession irodsSession, final IRODSAccount irodsAccount) throws JargonException {
		if (irodsSession == null) {
			throw new JargonException("IRODSSession is null");
		}

		if (irodsAccount == null) {
			throw new JargonException("IRODSAccount is null");
		}

		this.irodsSession = irodsSession;
		this.irodsAccount = irodsAccount;
		instrumented = getIRODSSession().getJargonProperties().isInstrument();

		log.debug("establishing connection");
		irodsSession.currentConnection(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#isInstrumented()
	 */
	@Override
	public boolean isInstrumented() {
		return instrumented;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSSession()
	 */
	@Override
	public final IRODSSession getIRODSSession() {
		return irodsSession;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSAccount()
	 */
	@Override
	public final IRODSAccount getIRODSAccount() {
		return irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSServerProperties()
	 */
	@Override
	public final IRODSServerProperties getIRODSServerProperties() throws JargonException {

		/*
		 * I need to force a check if this is eirods here. Note that the actual value
		 * will be cached in the DiscoveredServerPropertiesCache, so this only takes
		 * place
		 */

		return getIRODSSession().currentConnection(getIRODSAccount()).getIRODSServerProperties();
	}

	@Override
	public final IRODSMidLevelProtocol getIRODSProtocol() throws JargonException {
		return getIRODSSession().currentConnection(getIRODSAccount());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getJargonProperties()
	 */
	@Override
	public JargonProperties getJargonProperties() {
		return getIRODSSession().getJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObject#getDefaultTransferControlBlock ()
	 */
	@Override
	public TransferControlBlock buildDefaultTransferControlBlockBasedOnJargonProperties() throws JargonException {
		return getIRODSSession().buildDefaultTransferControlBlockBasedOnJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObject#getIRODSAccessObjectFactory()
	 */
	@Override
	public IRODSAccessObjectFactory getIRODSAccessObjectFactory() throws JargonException {
		if (this.irodsAccessObjectFactory == null) {
			this.irodsAccessObjectFactory = IRODSAccessObjectFactoryImpl.instance(irodsSession);
		}
		return this.irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#getIRODSFileFactory()
	 */
	@Override
	public IRODSFileFactory getIRODSFileFactory() throws JargonException {
		return IRODSAccessObjectFactoryImpl.instance(irodsSession).getIRODSFileFactory(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#
	 * buildTransferOptionsBasedOnJargonProperties()
	 */
	@Override
	public TransferOptions buildTransferOptionsBasedOnJargonProperties() throws JargonException {
		return getIRODSAccessObjectFactory().buildTransferOptionsBasedOnJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObject#operationComplete(int)
	 */
	@Override
	public Tag operationComplete(final int status) throws JargonException {
		OperationComplete operationComplete = OperationComplete.instance(status);
		return getIRODSProtocol().irodsFunction(operationComplete);
	}

	@Override
	public void closeSession() throws JargonException {

		irodsSession.closeSession();
	}

	@Override
	public void closeSessionAndEatExceptions() {
		try {
			irodsSession.closeSession();
		} catch (JargonException e) {
			log.warn("ignored exception on connection close:{}", e.getMessage(), e);
		}

	}

	@Override
	public void closeSession(final IRODSAccount irodsAccount) throws JargonException {
		if (irodsSession == null) {
			throw new JargonException("null session");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		irodsSession.closeSession(irodsAccount);

	}

	@Override
	public void closeSessionAndEatExceptions(final IRODSAccount irodsAccount) {
		if (irodsSession == null) {
			return;
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		try {
			irodsSession.closeSession(irodsAccount);
		} catch (Exception e) {
			log.warn("error encountered closing session, ignored", e);
		}

	}

	/**
	 * This method serves as a shim to inject an {@link IRODSAccessObjectFactory}
	 * when testing. While this is a code smell, it is a small one. The use of this
	 * method is unnecessary in normal circumstances.
	 * 
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory}
	 */
	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
