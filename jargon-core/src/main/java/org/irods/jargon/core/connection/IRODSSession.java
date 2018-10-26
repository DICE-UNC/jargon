/**
 *
 */
package org.irods.jargon.core.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import org.irods.jargon.core.checksum.LocalChecksumComputerFactory;
import org.irods.jargon.core.checksum.LocalChecksumComputerFactoryImpl;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl.QueryCloseBehavior;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryProcessor;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.TranslatedIRODSGenQuery;
import org.irods.jargon.core.transfer.AbstractRestartManager;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.MemoryBasedTransferRestartManager;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to open and maintain connections to iRODS across services. This is
 * used internally to keep connections to iRODS on a per-thread basis. A
 * {@code Map} is kept in a ThreadLocal cache with the {@code IRODSAccount} as
 * the key.
 * <p>
 * Connections are returned to the particular {@code IRODSProtocolManager} for
 * disposal or return to cache or pool. See the comments for
 * {@link IRODSMidLevelProtocol} for details on connection creation and
 * disposal.
 * <p>
 * {@code IRODSSession} is also the place where shared, expensive objects are
 * kept. Note that IRODSSession is not coded as a singleton. It is up to the
 * developer to place the {@code IRODSSession} in a context where it can be
 * shared across the application (such as in a Servlet application context). The
 * comments for {@link IRODSFileSystem} have more information. Essentially,
 * {@code IRODSSession} is meant to be created once, either directly, or wrapped
 * in the shared {@code IRODSFileSystem}. If desired, the developer can wrap
 * these objects as singletons, but that is not imposed by Jargon.
 * <p>
 * The {@code IRODSAccount} presented by the user is the key to the session
 * cache. The actual operative account is stored within the iRODS protocol. For
 * example, a PAM login may create a temp irods user under the covers, so a user
 * presents his pam iRODS account, but the system uses the derived account.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSSession {

	/**
	 * {@code ThreadLocal} to cache connections to iRODS. This is a {@code Map} that
	 * is keyed by the {@link IRODSAccount}, so that each thread automatically
	 * shares a common connection to an iRODS server.
	 */
	public static final ThreadLocal<Map<String, IRODSMidLevelProtocol>> sessionMap = new ThreadLocal<Map<String, IRODSMidLevelProtocol>>();

	/**
	 * The parallel transfer thread pool is lazily initialized on the first parallel
	 * transfer operation. This will use the {@code JargonProperties} configured in
	 * this {@code Session} if the properties indicate that the pool will be used.
	 * Once initialized, changing the {@code JargonProperties} controlling the pool
	 * have no effect.
	 */
	private ExecutorService parallelTransferThreadPool = null;
	private IRODSProtocolManager irodsProtocolManager;
	private static final Logger log = LoggerFactory.getLogger(IRODSSession.class);

	/**
	 * Trust manager (which can have a custom manager injected) for SSL
	 * certificates. {@code JargonProperties} can set a 'trust all' trust manager by
	 * setting the {@code bypass.ssl.cert.checks} to {@code true}, otherwise, the
	 * default will be used. In addition, a custom trust manager may be injected
	 * here.
	 */
	private X509TrustManager x509TrustManager = null;

	/**
	 * @return the x509TrustManager that is currently set for SSL connections, it
	 *         may be {@code null}, which will take a default for any SSL sockets
	 *         created
	 */
	public synchronized X509TrustManager getX509TrustManager() {
		return x509TrustManager;
	}

	/**
	 * @param x509TrustManager
	 *            the x509TrustManager to set, this may be left {@code null}, in
	 *            which case a default trust manager is used. this allows users to
	 *            inject their own certificate trust manager. Note as well that
	 *            {@link JargonProperties} can also specifiy that a 'trust all'
	 *            trust manager should be used, in which case the
	 *            {@link TrustAllX509TrustManager} will be created and put here.
	 *            This creation is only done when constructing iRODS session using
	 *            the constructor that takes a {@code JargonProperties} parameter,
	 *            with the bypassSslCet
	 */
	public synchronized void setX509TrustManager(final X509TrustManager x509TrustManager) {
		this.x509TrustManager = x509TrustManager;
	}

	/**
	 * Manager for long file restarts. Defaults to a simple in-memory manager, but
	 * can have an alternative manager injected. There is no harm in leaving this as
	 * {@code null} if not needed, as Jargon will guard against null access and
	 * assume restarts are not supported
	 */
	private AbstractRestartManager restartManager = null;

	/**
	 * General configuration properties for operation of jargon, buffer sizes,
	 * thread counts, etc.
	 */
	private JargonProperties jargonProperties;

	/**
	 * Factory to return a checksum computation strategy
	 */
	private final LocalChecksumComputerFactory localChecksumComputerFactory = new LocalChecksumComputerFactoryImpl();

	/**
	 * Simple cache (tolerating concurrent access) for name/value props. This cache
	 * is meant to hold user-definable properties about a connected server (by host
	 * and zone name). This is meant as an efficient way to record properties of a
	 * connected iRODS server that are discovered by interacting with the server.
	 * This is especially useful for operations that may or may not be configured,
	 * such that repeated failed attempts at an operation are not made.
	 * <p>
	 * A good example would be if required specific queries, rules, micro-services,
	 * or remote command scripts are not available to do an operation.
	 */
	private final DiscoveredServerPropertiesCache discoveredServerPropertiesCache = new DiscoveredServerPropertiesCache();

	/**
	 * Get the {@code JargonProperties} that contains metadata to tune the behavior
	 * of Jargon. This will either be the default, loaded from the
	 * {@code jargon.properties} file, or a custom source that can be injected into
	 * the {@code IRODSSession} object.
	 *
	 * @return {@link JargonProperties} with configuration metadata.
	 */
	public JargonProperties getJargonProperties() {
		synchronized (this) {
			return jargonProperties;
		}
	}

	/**
	 * Convenience method builds a default {@code TransferControlBlock} that has
	 * default {@code TransferOptions} based on the {@code JargonProperties}
	 * configured for the system.
	 *
	 * @return {@link TransferControlBlock} containing default
	 *         {@link TransferOptions} based on the configured
	 *         {@link JargonProperties}
	 * @throws JargonException
	 *             for iRODS error
	 */
	public TransferControlBlock buildDefaultTransferControlBlockBasedOnJargonProperties() throws JargonException {
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock.instance();
		synchronized (this) {

			transferControlBlock.setTransferOptions(buildTransferOptionsBasedOnJargonProperties());
		}
		return transferControlBlock;
	}

	/**
	 * Build an immutable {@code PipelineConfiguration} object that controls i/o
	 * behavior with iRODS
	 *
	 * @return {@link PipelineConfiguration} which is an immutable set of properties
	 *         to control i/o behavior of Jargon
	 */
	public PipelineConfiguration buildPipelineConfigurationBasedOnJargonProperties() {

		synchronized (this) {
			return PipelineConfiguration.instance(jargonProperties);
		}

	}

	/**
	 * Get the default transfer options based on the properties that have been set.
	 * This can then be tuned for an individual transfer
	 *
	 * @return {@link TransferOptions} based on defaults set in the jargon
	 *         properties
	 */
	public TransferOptions buildTransferOptionsBasedOnJargonProperties() {

		TransferOptions transferOptions = new TransferOptions();
		synchronized (this) {
			transferOptions.setMaxThreads(jargonProperties.getMaxParallelThreads());
			transferOptions.setUseParallelTransfer(jargonProperties.isUseParallelTransfer());
			transferOptions.setAllowPutGetResourceRedirects(jargonProperties.isAllowPutGetResourceRedirects());
			transferOptions.setComputeAndVerifyChecksumAfterTransfer(
					jargonProperties.isComputeAndVerifyChecksumAfterTransfer());
			transferOptions.setComputeChecksumAfterTransfer(jargonProperties.isComputeChecksumAfterTransfer());
			transferOptions.setIntraFileStatusCallbacks(jargonProperties.isIntraFileStatusCallbacks());
			transferOptions.setIntraFileStatusCallbacksNumberCallsInterval(
					jargonProperties.getIntraFileStatusCallbacksNumberCallsInterval());
			transferOptions.setIntraFileStatusCallbacksTotalBytesInterval(
					jargonProperties.getIntraFileStatusCallbacksTotalBytesInterval());
			transferOptions.setChecksumEncoding(jargonProperties.getChecksumEncoding());

		}

		log.debug("transfer options based on properties:{}", transferOptions);

		return transferOptions;
	}

	/**
	 * Close all sessions to iRODS that exist for this Thread. This method can be
	 * safely called by multiple threads, as the connections are in a
	 * {@code ThreadLocal}
	 *
	 * @throws JargonException
	 *             for iRODS error
	 */
	public void closeSession() throws JargonException {
		log.debug("closing all irods sessions");
		final Map<String, IRODSMidLevelProtocol> irodsProtocols = sessionMap.get();

		if (irodsProtocols == null) {
			log.warn("closing session that is already closed, silently ignore");
			return;
		}

		for (IRODSMidLevelProtocol irodsMidLevelProtocol : irodsProtocols.values()) {
			log.debug("found and am closing connection to : {}", irodsMidLevelProtocol.getIrodsAccount().toString());
			// irodsMidLevelProtocol.disconnect();
			getIrodsProtocolManager().returnIRODSProtocol(irodsMidLevelProtocol);
			// I don't remove from the map because the map is just going to be
			// set to null in the ThreadLocal below
		}

		log.debug("all sessions closed for this Thread");
		sessionMap.set(null);
	}

	public IRODSSession(final JargonProperties jargonProperties) {
		log.info("IRODSSession(jargonProperties) with properties of: {}", jargonProperties);
		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		this.jargonProperties = jargonProperties;
		checkInitTrustManager();
	}

	public IRODSSession() {
		log.debug("IRODS Session creation, loading default properties, these may be overridden...");
		try {
			jargonProperties = new SettableJargonProperties(new DefaultPropertiesJargonConfig());
			if (jargonProperties.isLongTransferRestart()) {
				// by default, at startup, if the long transfer restart is
				// selected, then start out with the default
				// in-memory implementation. If the dev futzes with this, they
				// have to make sure
				// a restart manager is available.
				restartManager = new MemoryBasedTransferRestartManager();
			}
			log.info("setting system prop for TLS...");
			// java.lang.System.setProperty("jdk.tls.client.protocols",
			// "TLSv1,TLSv1.1,TLSv1.2");
		} catch (Exception e) {
			log.warn("unable to load default jargon properties", e);
			throw new JargonRuntimeException("unable to load jargon props", e);
		}
		checkInitTrustManager();
	}

	private void checkInitTrustManager() {
		log.info("checkInitTrustManager()");
		if (getJargonProperties().isBypassSslCertChecks()) {
			log.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			log.warn("setting trustAllX509TrustManager, not recommended for production!!!");
			log.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			setX509TrustManager(new TrustAllX509TrustManager());
		}
	}

	/**
	 * Create a session with an object that will hand out connections.
	 *
	 * @param irodsConnectionManager
	 *            {@link IRODSProtocolManager} that is in charge of handing out
	 *            connections
	 * @throws JargonException
	 *             for iRODS error
	 */
	public IRODSSession(final IRODSProtocolManager irodsConnectionManager) throws JargonException {

		this();

		if (irodsConnectionManager == null) {
			throw new JargonException("irods connection manager cannot be null");
		}

		irodsProtocolManager = irodsConnectionManager;
	}

	/**
	 * Instance method, still supported (for now) but switching to straight setter
	 * methods and a default constructor to make it easer to wire with dependency
	 * injection. Look to see this deprecated.
	 *
	 * @param irodsProtocolHandler
	 *            {@link IRODSProtocolManager} to create connections in the session
	 * @return {@link IRODSSession}
	 * @throws JargonException
	 *             for iRODS error
	 */

	public static IRODSSession instance(final IRODSProtocolManager irodsProtocolHandler) throws JargonException {
		return new IRODSSession(irodsProtocolHandler);
	}

	/**
	 * For a given {@code IRODSAccount}, create and return, or return a connection
	 * from the cache. This connection is per-Thread, so if another thread has a
	 * cached connection, it is not visible from here, and must be properly closed
	 * on that Thread.
	 *
	 * @param irodsAccount
	 *            {@code IRODSAccount} that describes this connection to iRODS.
	 * @return {@link org.irods.jargon.core.connection.IRODSMidLevelProtocol} that
	 *         represents protocol level (above the socket level) communications to
	 *         iRODS.
	 * @throws JargonException
	 *             for iRODS error
	 */
	public AbstractIRODSMidLevelProtocol currentConnection(final IRODSAccount irodsAccount) throws JargonException {

		if (irodsProtocolManager == null) {
			log.error("no irods connection manager provided");
			throw new JargonRuntimeException(
					"IRODSSession improperly initialized, requires the IRODSConnectionManager to be initialized");
		}

		if (irodsAccount == null) {
			log.error("irodsAccount is null in connection");
			throw new IllegalArgumentException("irodsAccount is null");
		}

		AbstractIRODSMidLevelProtocol irodsProtocol = null;

		Map<String, IRODSMidLevelProtocol> irodsProtocols = sessionMap.get();

		if (irodsProtocols == null) {
			log.debug("no connections are cached, so create a new cache map");
			irodsProtocols = new HashMap<String, IRODSMidLevelProtocol>();
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount, irodsProtocols);
			log.debug("put a reference to a new connection for account: {}", irodsAccount.toString());
			sessionMap.set(irodsProtocols);
			return irodsProtocol;
		}

		// there is a protocol map, look up the connection for this account

		irodsProtocol = irodsProtocols.get(irodsAccount.toString());

		if (irodsProtocol == null) {
			log.debug("null connection in thread local, using IRODSConnectionManager to create a new connection");
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount, irodsProtocols);
		} else if (irodsProtocol.isConnected()) {

			log.debug("session using previously established connection:{}", irodsProtocol);
		} else {
			log.warn(
					"***************** session has a connection marked closed, create a new one and put back into the cache:{}",
					irodsProtocol);
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount, irodsProtocols);
		}

		return irodsProtocol;
	}

	/**
	 * Given an already established connection, renew the underlying connection
	 * using the existing credentials. This is used to seamlessly renew a socket
	 * during operations 'under the covers', for operations like long running
	 * transfers that may time out.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return {@link org.irods.jargon.core.connection.IRODSMidLevelProtocol} with a
	 *         renewed connection
	 * @throws AuthenticationException
	 *             for auth error
	 * @throws JargonException
	 *             for iRODS error
	 */
	public AbstractIRODSMidLevelProtocol currentConnectionCheckRenewalOfSocket(final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException {

		log.info("renewConnection()");
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		AbstractIRODSMidLevelProtocol irodsMidLevelProtocol = currentConnection(irodsAccount);

		log.info("evaluate conn for renewal:{}", irodsAccount);

		boolean shutdown = evaluateConnectionForRenewal(irodsMidLevelProtocol);
		if (!shutdown) {
			return irodsMidLevelProtocol;
		} else {
			log.info("return a refreshed connection");
			return currentConnection(irodsAccount);
		}

	}

	/**
	 * Based on the configured properties, evaluate the age of the current
	 * connection and potentially renew the connection if necessary.
	 *
	 * @param irodsMidLevelProtocol
	 * @return {@code boolean} that will be {@code true} if the conn was shut down
	 * @throws AuthenticationException
	 * @throws JargonException
	 */
	private boolean evaluateConnectionForRenewal(final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol)
			throws AuthenticationException, JargonException {

		int renewalInterval = irodsMidLevelProtocol.getPipelineConfiguration().getSocketRenewalIntervalInSeconds();
		// 0 means ignore
		if (renewalInterval == 0) {
			return false;
		}
		// compute a window based on time of connection...restart?
		long renewalWindow = irodsMidLevelProtocol.getConnectTimeInMillis() + renewalInterval * 1000;
		long currTime = System.currentTimeMillis();
		if (currTime > renewalWindow) {
			log.debug("renewing:{}", irodsMidLevelProtocol);
			this.closeSession(irodsMidLevelProtocol.getIrodsAccount());
			return true;
		} else {
			return false;
		}
	}

	private IRODSMidLevelProtocol connectAndAddToProtocolsMap(final IRODSAccount irodsAccount,
			final Map<String, IRODSMidLevelProtocol> irodsProtocols) throws JargonException {
		IRODSMidLevelProtocol irodsProtocol;
		irodsProtocol = irodsProtocolManager.getIRODSProtocol(irodsAccount,
				buildPipelineConfigurationBasedOnJargonProperties(), this);
		if (irodsProtocol == null) {
			log.error("no connection returned from connection manager");
			throw new JargonRuntimeException("null connection returned from connection manager");
		}

		// irodsProtocol.setIrodsSession(this);
		irodsProtocols.put(irodsAccount.toString(), irodsProtocol);

		/*
		 * check for GSI and add user info, consider factoring out to a 'post processor'
		 * MC
		 */
		if (irodsAccount.getAuthenticationScheme() == AuthScheme.GSI) {
			log.debug("adding user information to iRODS account for GSI");
			addUserInfoForGSIAccount(irodsAccount, irodsProtocol);
		}

		log.debug("put a reference to a new connection for account: {}", irodsAccount.toString());
		sessionMap.set(irodsProtocols);
		log.debug("returned new connection:{}", irodsProtocol);
		return irodsProtocol;
	}

	private void addUserInfoForGSIAccount(final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands) throws JargonException {
		log.debug("addUserInfoForGSIAccount()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (!(irodsAccount instanceof GSIIRODSAccount)) {
			throw new IllegalArgumentException("irodsAccount parameter is not a GSIIRODSAccount");
		}

		GSIIRODSAccount gsiIRODSAccount = (GSIIRODSAccount) irodsAccount;

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		try {
			final String dn = gsiIRODSAccount.getDistinguishedName().trim();
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_DN, QueryConditionOperators.EQUAL, dn);
			GenQueryProcessor genQueryProcessor = new GenQueryProcessor(irodsCommands);

			IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(1);

			TranslatedIRODSGenQuery translatedIRODSQuery = genQueryProcessor.translateProvidedQuery(query);

			IRODSQueryResultSetInterface resultSet = genQueryProcessor.executeTranslatedIRODSQuery(translatedIRODSQuery,
					0, 0, QueryCloseBehavior.AUTO_CLOSE, "");

			IRODSQueryResultRow row = resultSet.getFirstResult();
			gsiIRODSAccount.setUserName(row.getColumn(0));
			gsiIRODSAccount.setZone(row.getColumn(1));
			gsiIRODSAccount.setHomeDirectory(MiscIRODSUtils.computeHomeDirectoryForIRODSAccount(gsiIRODSAccount));

		} catch (GenQueryBuilderException e) {
			log.error("error building query for user DN", e);
			throw new JargonException("query builder exception building user DN query", e);
		} catch (JargonQueryException e) {
			log.error("error building query for user DN", e);
			throw new JargonException("jargon query  exception building user DN query", e);
		}
	}

	/**
	 * @return the irodsConnectionManager
	 */
	public synchronized IRODSProtocolManager getIrodsConnectionManager() {
		return irodsProtocolManager;
	}

	/**
	 * @param irodsConnectionManager
	 *            the irodsConnectionManager to set
	 */
	public void setIrodsConnectionManager(final IRODSProtocolManager irodsConnectionManager) {
		irodsProtocolManager = irodsConnectionManager;
	}

	/**
	 * Close an iRODS session for the given account
	 *
	 * @param irodsAccount
	 *            {@code IRODSAccount} that describes the connection that should be
	 *            closed.
	 * @throws JargonException
	 *             if an error occurs on the close. If the connection does not exist
	 *             it is logged and ignored.
	 */
	public void closeSession(final IRODSAccount irodsAccount) throws JargonException {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.debug("closing irods session for: {}", irodsAccount.toString());
		final Map<String, IRODSMidLevelProtocol> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			log.warn("closing session that is already closed, silently ignore");
			return;
		}

		final IRODSMidLevelProtocol irodsMidLevelProtocol = irodsProtocols.get(irodsAccount.toString());

		if (irodsMidLevelProtocol == null) {
			log.warn("closing a connection that is not held, silently ignore");
			return;

		}
		log.debug("found and am closing connection to : {}", irodsAccount.toString());

		getIrodsProtocolManager().returnIRODSProtocol(irodsMidLevelProtocol);

		irodsProtocols.remove(irodsAccount.toString());
		if (irodsProtocols.isEmpty()) {
			log.debug("no more connections, so clear cache from ThreadLocal");
			sessionMap.set(null);
		}

	}

	/**
	 * Signal to the {@code IRODSSession} that a connection has been forcefully
	 * terminated due to errors, and should be removed from the cache.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} that maps the connection
	 *
	 */
	public void discardSessionForErrors(final IRODSAccount irodsAccount) {

		log.warn("discarding irods session for: {}", irodsAccount.toString());
		final Map<String, IRODSMidLevelProtocol> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			log.warn("discarding session that is already closed, silently ignore");
			return;
		}

		IRODSMidLevelProtocol badConnection;
		badConnection = irodsProtocols.get(irodsAccount.toString());
		if (badConnection != null) {
			getIrodsProtocolManager().returnWithForce(badConnection);
			irodsProtocols.remove(irodsAccount.toString());
		}

		if (irodsProtocols.isEmpty()) {
			log.debug("no more connections, so clear cache from ThreadLocal");
			sessionMap.set(null);
		}

	}

	/**
	 * This method is not particularly useful, but does provide a route to get a
	 * direct handle on the connections for this Thread in cases where such status
	 * information needs to be kept. Returns null if no map is available.
	 *
	 * @return {@code Map<String, IRODSMidLevelProtocol>}
	 */
	public Map<String, IRODSMidLevelProtocol> getIRODSCommandsMap() {
		return sessionMap.get();
	}

	public IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	public void setIrodsProtocolManager(final IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	/**
	 * Get (lazily) the pool of parallel transfer threads. This will return
	 * {@code null} if the use of the pool is not set in the
	 * {@code JargonProperties}. The method will create the pool on the first
	 * request based on the {@code JargonProperties}, and once created, changing the
	 * properties does not reconfigure the pool, it just returns the lazily created
	 * instance.
	 *
	 * @return {@link ExecutorService} that is the pool of threads for the paralllel
	 *         transfers, or {@code null} if the pool is not configured in the
	 *         jargon properties.
	 * @throws JargonException
	 *             for iRODS error
	 */
	public ExecutorService getParallelTransferThreadPool() throws JargonException {
		log.debug("getting the ParallelTransferThreadPool");
		synchronized (this) {

			if (!jargonProperties.isUseTransferThreadsPool()) {
				log.debug("I am not using the parallel transfer threads pool, return null");
				return null;
			}

			if (parallelTransferThreadPool != null) {
				log.debug("returning already created ParallelTransferThreadPool");
				return parallelTransferThreadPool;
			}

			int poolSize = jargonProperties.getTransferThreadPoolMaxSimultaneousTransfers()
					* jargonProperties.getMaxParallelThreads();
			int maxParallelThreads = jargonProperties.getMaxParallelThreads();

			log.debug("creating the parallel transfer threads pool");
			log.debug("   max # threads: {}", maxParallelThreads);

			log.debug("   pool timeout millis:{}", jargonProperties.getTransferThreadPoolTimeoutMillis());

			parallelTransferThreadPool = new ThreadPoolExecutor(maxParallelThreads, poolSize,
					jargonProperties.getTransferThreadPoolTimeoutMillis(), TimeUnit.MILLISECONDS,
					new ArrayBlockingQueue<Runnable>(poolSize), new RejectedParallelThreadExecutionHandler());

			log.debug("parallelTransferThreadPool created");
			return parallelTransferThreadPool;
		}
	}

	/**
	 * Set the Jargon properties
	 *
	 * @param jargonProperties
	 *            the jargonProperties to set
	 */
	public void setJargonProperties(final JargonProperties jargonProperties) {
		synchronized (this) {
			this.jargonProperties = jargonProperties;
		}
	}

	/**
	 *
	 * Simple cache (tolerating concurrent access) for name/value props. This cache
	 * is meant to hold user-definable properties about a connected server (by host
	 * and zone name). This is meant as an efficient way to record properties of a
	 * connected iRODS server that are discovered by interacting with the server.
	 * This is especially useful for operations that may or may not be configured,
	 * such that repeated failed attempts at an operation are not made.
	 * <p>
	 * A good example would be if required specific queries, rules, micro-services,
	 * or remote command scripts are not available to do an operation.
	 *
	 * @return {@link DiscoveredServerPropertiesCache}
	 */
	public DiscoveredServerPropertiesCache getDiscoveredServerPropertiesCache() {
		return discoveredServerPropertiesCache;
	}

	/**
	 * Handy method to see if we're using the dynamic server properties cache. This
	 * is set in the jargon properties.
	 *
	 * @return {@code boolean}
	 */
	public boolean isUsingDynamicServerPropertiesCache() {
		// getjargonProperties is already sync'd
		return getJargonProperties().isUsingDiscoveredServerPropertiesCache();
	}

	/**
	 * Get a reference to a factory that can return checksum computation strategies
	 * on local file systems
	 *
	 * @return {@link LocalChecksumComputerFactory}
	 */
	public LocalChecksumComputerFactory getLocalChecksumComputerFactory() {
		return localChecksumComputerFactory;
	}

	public synchronized AbstractRestartManager getRestartManager() {

		if (restartManager == null) {
			if (jargonProperties.isLongTransferRestart()) {
				log.warn("no restart manager provided, long file restart is on, create default memory based manager");
				restartManager = new MemoryBasedTransferRestartManager();
			}
		}

		return restartManager;
	}

	public synchronized void setRestartManager(final AbstractRestartManager restartManager) {
		this.restartManager = restartManager;
	}

	/**
	 * Retrieve an instance of SSL connection utilities that can manage SSL
	 * connections to iRODS
	 *
	 * @return
	 */
	SslConnectionUtilities instanceSslConnectionUtilities() {
		return new SslConnectionUtilities(this);

	}

}
