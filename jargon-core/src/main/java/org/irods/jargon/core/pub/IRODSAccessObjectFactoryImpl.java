package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: switch to allow IRODSSession to be injected, add a check with a meaningful warning message to the various instance creating methods if IRODSSession is missing

/**
 * Factory to produce IRODS access objects. This is the key object which can be
 * used to create components that can interact directly with iRODS to query
 * metadata attributes, update the catalog, and move data.
 * <p/>
 * Access objects are styled after traditional DAO's, in that they deal with a
 * particular domain or service, and have methods to query for data about things
 * in iRODS, and methods to update things in iRODS. The access objects use
 * 'POJO' domain objects for input and output parameters, giving some nice,
 * simple abstractions of the iRODS metadata catalog.
 * <p/>
 * Access objects are connected to iRODS at the time they are created. The
 * connection is determined by the
 * {@link org.irods.jargon.core.connection.IRODSAccount} that is specified when
 * the access object is created. The connection is managed using a
 * <code>ThreadLocal</code>, such that any access objects created in the same
 * thread by this factory will automatically create a connection, or will share
 * an already created connection. This also means that, at the end of any set of
 * operations, the connection must be closed. Typically, and
 * {@link IRODSFileSystem} is instantiated, and that
 * <code>IRODSFileSystem</code> is used to get a reference to this access object
 * factory. Once operations are done, the <code>IRODSFileSystem</code> can be
 * used to close connections in that thread. This factory has hooks to also
 * close those connections, and this can be used in cases where this factory is
 * injected itself into another service.
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public final class IRODSAccessObjectFactoryImpl implements
		IRODSAccessObjectFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(IRODSAccessObjectFactoryImpl.class);

	private IRODSSession irodsSession;

	/**
	 * Construct an instance with the given <code>IRODSSession<code>
	 * 
	 * @param irodsSession
	 */
	public IRODSAccessObjectFactoryImpl(final IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * Default constructor which does not initialize the
	 * <code>IRODSSession</code>. It is up to the developer to inject the
	 * <code>IRODSsession</code> or an exception will result.
	 */
	public IRODSAccessObjectFactoryImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#closeSession()
	 */
	@Override
	public void closeSession() throws JargonException {

		if (irodsSession == null) {
			throw new JargonException("null session");
		}

		irodsSession.closeSession();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * closeSessionAndEatExceptions()
	 */
	@Override
	public void closeSessionAndEatExceptions() {

		if (irodsSession == null) {
			return;
		}

		try {
			irodsSession.closeSession();
		} catch (Exception e) {
			LOG.warn("error encountered closing session, ignored", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#closeSession(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void closeSession(final IRODSAccount irodsAccount)
			throws JargonException {
		if (irodsSession == null) {
			throw new JargonException("null session");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		irodsSession.closeSession(irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * closeSessionAndEatExceptions
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
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
			LOG.warn("error encountered closing session, ignored", e);
		}

	}

	/**
	 * Creates an instance of this access object factory.
	 * 
	 * @param irodsSession
	 *            {@link org.irods.jargon.core.connection.IRODSSession} that is
	 *            capable of creating connections to iRODS on demand.
	 * @return
	 * @throws JargonException
	 */
	public static IRODSAccessObjectFactory instance(
			final IRODSSession irodsSession) throws JargonException {
		if (irodsSession == null) {
			LOG.error("null irods session");
			throw new IllegalArgumentException("IRODSSession cannot be null");
		}
		LOG.debug("creating access object factory");
		return new IRODSAccessObjectFactoryImpl(irodsSession);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getUserAO(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public UserAO getUserAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new UserAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getUserGroupAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public UserGroupAO getUserGroupAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new UserGroupAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getEnvironmentalInfoAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public EnvironmentalInfoAO getEnvironmentalInfoAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new EnvironmentalInfoAOImpl(irodsSession, irodsAccount);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getQuotaAO(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public QuotaAO getQuotaAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new QuotaAOImpl(irodsSession, irodsAccount);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIRODSGenQueryExecutor
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSGenQueryExecutor getIRODSGenQueryExecutor(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new IRODSGenQueryExecutorImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getZoneAO(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ZoneAO getZoneAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new ZoneAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getResourceAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ResourceAO getResourceAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new ResourceAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIRODSFileSystemAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSFileSystemAO getIRODSFileSystemAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new IRODSFileSystemAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIRODSFileFactory
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSFileFactory getIRODSFileFactory(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new IRODSFileFactoryImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getCollectionAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public CollectionAO getCollectionAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new CollectionAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getDataObjectAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataObjectAO getDataObjectAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new DataObjectAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getRuleProcessingAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public RuleProcessingAO getRuleProcessingAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new RuleProcessingAOImpl(irodsSession, irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getDataTransferOperations
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataTransferOperations getDataTransferOperations(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new DataTransferOperationsImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getBulkFileOperationsAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public BulkFileOperationsAO getBulkFileOperationsAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new BulkFileOperationsAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getRemoteExecutionOfCommandsAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new RemoteExecutionOfCommandsAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getCollectionAndDataObjectListAndSearchAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new CollectionAndDataObjectListAndSearchAOImpl(irodsSession,
				irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getSimpleQueryExecutorAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public SimpleQueryExecutorAO getSimpleQueryExecutorAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new SimpleQueryExecutorAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getStream2StreamAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public Stream2StreamAO getStream2StreamAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new Stream2StreamAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getSpecificQueryExecutorAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public SpecificQueryExcecutorAO getSpecificQueryExecutorAO(
			final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new SpecificQueryExecutorAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIrodsSession()
	 */
	@Override
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#setIrodsSession(org
	 * .irods.jargon.core.connection.IRODSSession)
	 */
	@Override
	public void setIrodsSession(final IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	private void checkIrodsSessionSet() throws JargonException {
		if (irodsSession == null) {
			throw new JargonException(
					"no irodsSession was set, this is likely due to wiring the IRODSAccessObjectFactory without setting the irodsSession property");
		}
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getJargonProperties()
	 */
	@Override
	public JargonProperties getJargonProperties() {
		// irodsSession synchronizes access
		return irodsSession.getJargonProperties();
	}
}
