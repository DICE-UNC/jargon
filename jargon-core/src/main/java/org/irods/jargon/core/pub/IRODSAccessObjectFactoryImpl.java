package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.DiscoveredServerPropertiesCache;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to produce IRODS access objects. This is the key object which can be
 * used to create components that can interact directly with iRODS to query
 * metadata attributes, update the catalog, and move data.
 * <p>
 * Access objects are styled after traditional DAO's, in that they deal with a
 * particular domain or service, and have methods to query for data about things
 * in iRODS, and methods to update things in iRODS. The access objects use
 * 'POJO' domain objects for input and output parameters, giving some nice,
 * simple abstractions of the iRODS metadata catalog.
 * <p>
 * Access objects are connected to iRODS at the time they are created. The
 * connection is determined by the
 * {@link org.irods.jargon.core.connection.IRODSAccount} that is specified when
 * the access object is created. The connection is managed using a
 * {@code ThreadLocal}, such that any access objects created in the same thread
 * by this factory will automatically create a connection, or will share an
 * already created connection. This also means that, at the end of any set of
 * operations, the connection must be closed. Typically, and
 * {@link IRODSFileSystem} is instantiated, and that {@code IRODSFileSystem} is
 * used to get a reference to this access object factory. Once operations are
 * done, the {@code IRODSFileSystem} can be used to close connections in that
 * thread. This factory has hooks to also close those connections, and this can
 * be used in cases where this factory is injected itself into another service.
 *
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public final class IRODSAccessObjectFactoryImpl implements IRODSAccessObjectFactory {

	private static final Logger log = LoggerFactory.getLogger(IRODSAccessObjectFactoryImpl.class);

	private IRODSSession irodsSession;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * authenticateIRODSAccount (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public AuthResponse authenticateIRODSAccount(final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException {
		log.debug("authenticateIRODSAccount()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.debug("any existing session will be closed, or at least handed back to a pool/cache");
		irodsSession.closeSession(irodsAccount);

		/*
		 * Note that this works if the account is already authenticated by simply
		 * returning the cached response. If the account is not authenticated, it will
		 * cause the authentication process and cache the response.
		 */
		AuthResponse authResponse = irodsSession.currentConnection(irodsAccount).getAuthResponse();

		log.debug("authResponse:{}", authResponse);
		return authResponse;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * authenticateIRODSAccountUtilizingCachedConnectionIfPresent
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public AuthResponse authenticateIRODSAccountUtilizingCachedConnectionIfPresent(final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException {
		log.debug("authenticateIRODSAccountUtilizingCachedConnectionIfPresent()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		/*
		 * Note that this works if the account is already authenticated by simply
		 * returning the cached response. If the account is not authenticated, it will
		 * cause the authentication process and cache the response.
		 */
		AuthResponse authResponse = irodsSession.currentConnection(irodsAccount).getAuthResponse();
		log.debug("authResponse:{}", authResponse);
		return authResponse;
	}

	/**
	 * Construct an instance with the given {@code IRODSSession}
	 *
	 * @param irodsSession {@link IRODSSession}
	 */
	public IRODSAccessObjectFactoryImpl(final IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * Default constructor which does not initialize the {@code IRODSSession}. It is
	 * up to the developer to inject the {@code IRODSsession} or an exception will
	 * result.
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
			log.warn("error encountered closing session, ignored", e);
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
	public void closeSession(final IRODSAccount irodsAccount) throws JargonException {
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
	 * closeSessionAndEatExceptions (org.irods.jargon.core.connection.IRODSAccount)
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
			log.warn("error encountered closing session, ignored", e);
		}

	}

	/**
	 * Creates an instance of this access object factory.
	 *
	 * @param irodsSession {@link org.irods.jargon.core.connection.IRODSSession}
	 *                     that is capable of creating connections to iRODS on
	 *                     demand.
	 * @return {@link IRODSAccessObjectFactory}
	 * @throws JargonException for iRODS error
	 */
	public static IRODSAccessObjectFactory instance(final IRODSSession irodsSession) throws JargonException {
		if (irodsSession == null) {
			log.error("null irods session");
			throw new IllegalArgumentException("IRODSSession cannot be null");
		}
		log.debug("creating access object factory");
		return new IRODSAccessObjectFactoryImpl(irodsSession);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getUserAO(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public UserAO getUserAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new UserAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getUserGroupAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public UserGroupAO getUserGroupAO(final IRODSAccount irodsAccount) throws JargonException {
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
	public EnvironmentalInfoAO getEnvironmentalInfoAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new EnvironmentalInfoAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getQuotaAO(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public QuotaAO getQuotaAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new QuotaAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getIRODSGenQueryExecutor (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSGenQueryExecutor getIRODSGenQueryExecutor(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new IRODSGenQueryExecutorImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getZoneAO(org.irods
	 * .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ZoneAO getZoneAO(final IRODSAccount irodsAccount) throws JargonException {
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
	public ResourceAO getResourceAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new ResourceAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getResourceGroupAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ResourceGroupAO getResourceGroupAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new ResourceGroupAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIRODSFileSystemAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSFileSystemAO getIRODSFileSystemAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new IRODSFileSystemAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getIRODSFileFactory
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSFileFactory getIRODSFileFactory(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new IRODSFileFactoryImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getCollectionAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public CollectionAO getCollectionAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new CollectionAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getDataObjectAO(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataObjectAO getDataObjectAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new DataObjectAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getDataObjectAuditAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataObjectAuditAO getDataObjectAuditAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new DataObjectAuditAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getCollectionAuditAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public CollectionAuditAO getCollectionAuditAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new CollectionAuditAOImpl(irodsSession, irodsAccount);
	}

	@Override
	public CollectionPagerAO getCollectionPagerAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new CollectionPagerAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getRuleProcessingAO(org.
	 * irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public RuleProcessingAO getRuleProcessingAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		// don't initialize the server properties here for eIRODS, as it's used
		// to load the rule base. This is not awesome but will go away when
		// eirods numbering get's lined up with community
		return new RuleProcessingAOImpl(irodsSession, irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getDataTransferOperations (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataTransferOperations getDataTransferOperations(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new DataTransferOperationsImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getBulkFileOperationsAO (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public BulkFileOperationsAO getBulkFileOperationsAO(final IRODSAccount irodsAccount) throws JargonException {
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
	public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO(final IRODSAccount irodsAccount)
			throws JargonException {
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
		return new CollectionAndDataObjectListAndSearchAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getSimpleQueryExecutorAO (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public SimpleQueryExecutorAO getSimpleQueryExecutorAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new SimpleQueryExecutorAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getStream2StreamAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public Stream2StreamAO getStream2StreamAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new Stream2StreamAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.IRODSAccessObjectFactory#getMountedCollectionAO
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public MountedCollectionAO getMountedCollectionAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new MountedCollectionAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getProtocolExtensionPoint (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ProtocolExtensionPoint getProtocolExtensionPoint(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new ProtocolExtensionPointImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getIRODSRegistrationOfFilesAO (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSRegistrationOfFilesAO getIRODSRegistrationOfFilesAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new IRODSRegistrationOfFilesAOImpl(irodsSession, irodsAccount);
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
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#setIrodsSession(org
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getJargonProperties()
	 */
	@Override
	public JargonProperties getJargonProperties() throws JargonException {
		checkIrodsSessionSet();
		// irodsSession synchronizes access
		return irodsSession.getJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getIRODSServerProperties (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public final IRODSServerProperties getIRODSServerProperties(final IRODSAccount irodsAccount)
			throws JargonException {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (isUsingDynamicServerPropertiesCache()) {

			if (getIrodsSession().getDiscoveredServerPropertiesCache().retrieveValue(irodsAccount.getHost(),
					irodsAccount.getZone(), DiscoveredServerPropertiesCache.EIRODS) == null) {

				log.debug("need to cache and update isEirods");
				getEnvironmentalInfoAO(irodsAccount);
			}
		}

		return irodsSession.currentConnection(irodsAccount).getIRODSServerProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * buildDefaultTransferControlBlockBasedOnJargonProperties()
	 */
	@Override
	public TransferControlBlock buildDefaultTransferControlBlockBasedOnJargonProperties() throws JargonException {
		checkIrodsSessionSet();
		// irodsSession synchronizes access
		return irodsSession.buildDefaultTransferControlBlockBasedOnJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * buildTransferOptionsBasedOnJargonProperties()
	 */
	@Override
	public TransferOptions buildTransferOptionsBasedOnJargonProperties() throws JargonException {
		checkIrodsSessionSet();
		return irodsSession.buildTransferOptionsBasedOnJargonProperties();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getSpecificQueryAO
	 * (org.irods .jargon.core.connection.IRODSAccount)
	 */
	@Override
	public SpecificQueryAO getSpecificQueryAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new SpecificQueryAOImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#getTrashOperationsAO(
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public TrashOperationsAO getTrashOperationsAO(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new TrashOperationsAOImpl(irodsSession, irodsAccount);
	}

	@Override
	public DataObjectChecksumUtilitiesAO getDataObjectChecksumUtilitiesAO(final IRODSAccount irodsAccount)
			throws JargonException {
		checkIrodsSessionSet();
		return new DataObjectChecksumUtilitiesAOImpl(irodsSession, irodsAccount);
	}

	@Override
	public ApiPluginExecutor getApiPluginExecutor(final IRODSAccount irodsAccount) throws JargonException {
		checkIrodsSessionSet();
		return new ApiPluginExecutorImpl(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * getDiscoveredServerPropertiesCache()
	 */
	@Override
	public DiscoveredServerPropertiesCache getDiscoveredServerPropertiesCache() {
		return getIrodsSession().getDiscoveredServerPropertiesCache();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSAccessObjectFactory#
	 * isUsingDynamicServerPropertiesCache()
	 */
	@Override
	public boolean isUsingDynamicServerPropertiesCache() {
		return getIrodsSession().isUsingDynamicServerPropertiesCache();
	}
	
	public IRODSGenquery2Executor getIRODSGenquery2Executor(final IRODSAccount irodsAccount) throws JargonException {
		return new IRODSGenquery2ExecutorImpl(irodsSession, irodsAccount);
	}

}
