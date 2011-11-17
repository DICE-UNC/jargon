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

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.TransferType;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to open and maintain connections to iRODS across services. This is
 * used internally to keep connections to iRODS on a per-thread basis. A
 * <code>Map</code> is kept in a ThreadLocal cache with the
 * <code>IRODSAccount</code> as the key.
 * <p/>
 * Connections are returned to the particular <code>IRODSProtocolManager</code>
 * for disposal or return to cache or pool.  See the comments for {@link IRODSCommands} for
 * details on connection creation and disposal.
 * <p/>
 * <code>IRODSSession</code> is also the place where shared, expensive objects
 * are kept. Note that IRODSSession is not coded as a singleton. It is up to the
 * developer to place the <code>IRODSSession</code> in a context where it can be
 * shared across the application (such as in a Servlet application context). The
 * comments for {@link IRODSFileSystem} have more information. Essentially,
 * <code>IRODSSession</code> is meant to be created once, either directly, or
 * wrapped in the shared <code>IRODSFileSystem</code>. If desired, the developer
 * can wrap these objects as singletons, but that is not imposed by Jargon.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSSession {

	public static final Logger log = LoggerFactory
			.getLogger(IRODSSession.class);

	/**
	 * <code>ThreadLocal</code> to cache connections to iRODS. This is a
	 * <code>Map</code> that is keyed by the {@link IRODSAccount}, so that each
	 * thread automatically shares a common connection to an iRODS server.
	 */
	public static final ThreadLocal<Map<String, IRODSCommands>> sessionMap = new ThreadLocal<Map<String, IRODSCommands>>();
	
	/**
	 * The parallel transfer thread pool is lazily initialized on the first
	 * parallel transfer operation. This will use the
	 * <code>JargonProperties</code> configured in this <code>Session</code> if
	 * the properties indicate that the pool will be used. Once initialized,
	 * changing the <code>JargonProperties</code> controlling the pool have no
	 * effect.
	 */
	private ExecutorService parallelTransferThreadPool = null;
	private IRODSProtocolManager irodsProtocolManager;
	private static final Logger LOG = LoggerFactory
			.getLogger(IRODSSession.class);
	private JargonProperties jargonProperties;

	/**
	 * Get the <code>JargonProperties</code> that contains metadata to tune the
	 * behavior of Jargon. This will either be the default, loaded from the
	 * <code>jargon.properties</code> file, or a custom source that can be
	 * injected into the <code>IRODSSession</code> object.
	 * 
	 * @return {@link JargonProperties} with configuration metadata.
	 */
	public JargonProperties getJargonProperties() {
		synchronized (this) {
			return jargonProperties;
		}
	}
	
	/**
	 * Build an immutable <code>PipelineConfiguration</code> object that controls i/o behavior with iRODS
	 * @return {@link PipelineConfiguration} which is an immutable set of properties to control i/o behavior of Jargon
	 */
	public PipelineConfiguration buildPipelineConfigurationBasedOnJargonProperties() {
		
		synchronized (this) {
			return PipelineConfiguration.instance(jargonProperties);
		}
		
	}

	/**
	 * Get the default transfer options based on the properties that have been
	 * set. This can then be tuned for an individual transfer
	 * 
	 * @return {@link TransferOptions} based on defaults set in the jargon
	 *         properties
	 * @throws JargonException
	 */
	public TransferOptions buildTransferOptionsBasedOnJargonProperties()
			 {

		TransferOptions transferOptions = new TransferOptions();
		synchronized (this) {
			transferOptions.setMaxThreads(jargonProperties
					.getMaxParallelThreads());

			if (jargonProperties.isUseParallelTransfer()) {
				transferOptions.setTransferType(TransferType.STANDARD);
			} else {
				transferOptions.setTransferType(TransferType.NO_PARALLEL);
			}

			transferOptions.setAllowPutGetResourceRedirects(jargonProperties
					.isAllowPutGetResourceRedirects());
			transferOptions
					.setComputeAndVerifyChecksumAfterTransfer(jargonProperties
							.isComputeAndVerifyChecksumAfterTransfer());
			transferOptions.setComputeChecksumAfterTransfer(jargonProperties
					.isComputeChecksumAfterTransfer());
			transferOptions.setIntraFileStatusCallbacks(jargonProperties
					.isIntraFileStatusCallbacks());
		}
		
		log.info("transfer options based on properties:{}", transferOptions);
		
		return transferOptions;
	}

	/**
	 * Close all sessions to iRODS that exist for this Thread. This method can
	 * be safely called by multiple threads, as the connections are in a
	 * <code>ThreadLocal</code>
	 * 
	 * @throws JargonException
	 */
	public void closeSession() throws JargonException {
		LOG.info("closing all irods sessions");
		final Map<String, IRODSCommands> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			LOG.warn("closing session that is already closed, silently ignore");
			return;
		}

		for (IRODSCommands irodsCommands : irodsProtocols.values()) {
			LOG.debug("found and am closing connection to : {}", irodsCommands
					.getIRODSAccount().toString());
			irodsCommands.disconnect();

			// I don't remove from the map because the map is just going to be
			// set to null in the ThreadLocal below
		}

		LOG.debug("all sessions closed for this Thread");
		sessionMap.set(null);
	}

	public IRODSSession() {
		LOG.info("IRODS Session creation, loading default properties, these may be overridden...");
		try {
			jargonProperties = new DefaultPropertiesJargonConfig();
		} catch (Exception e) {
			LOG.warn("unable to load default jargon properties");
		}
	}

	/**
	 * Create a session with an object that will hand out connections.
	 * 
	 * @param irodsConnectionManager
	 *            {@link IRODSProtocolManager} that is in charge of handing out
	 *            connections
	 * @throws JargonException
	 */
	public IRODSSession(final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {

		this();

		if (irodsConnectionManager == null) {
			throw new JargonException("irods connection manager cannot be null");
		}

		this.irodsProtocolManager = irodsConnectionManager;
	}

	/**
	 * Instance method, still supported (for now) but switching to straight
	 * setter methods and a default constructor to make it easer to wire with
	 * dependency injection. Look to see this deprecated.
	 * 
	 * @param irodsConnectionManager
	 * @return
	 * @throws JargonException
	 */

	public static IRODSSession instance(
			final IRODSProtocolManager irodsConnectionManager)
			throws JargonException {
		return new IRODSSession(irodsConnectionManager);
	}

	/**
	 * For a given <code>IRODSAccount</code>, create and return, or return a
	 * connection from the cache. This connection is per-Thread, so if another
	 * thread has a cached connection, it is not visible from here, and must be
	 * properly closed on that Thread.
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes this connection to
	 *            iRODS.
	 * @return {@link org.irods.jargon.core.connection.IRODSCommands} that
	 *         represents low level (but above the socket level) communications
	 *         to iRODS.
	 * @throws JargonException
	 */
	public IRODSCommands currentConnection(final IRODSAccount irodsAccount)
			throws JargonException {

		if (irodsProtocolManager == null) {
			LOG.error("no irods connection manager provided");
			throw new JargonException(
					"IRODSSession improperly initialized, requires the IRODSConnectionManager to be initialized");
		}

		if (irodsAccount == null) {
			LOG.error("irodsAccount is null in connection");
			throw new JargonException("irodsAccount is null");
		}

		IRODSCommands irodsProtocol = null;

		Map<String, IRODSCommands> irodsProtocols = sessionMap.get();

		if (irodsProtocols == null) {
			LOG.debug("no connections are cached, so create a new cache map");
			irodsProtocols = new HashMap<String, IRODSCommands>();
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount,
					irodsProtocols);
			LOG.debug("put a reference to a new connection for account: {}",
					irodsAccount.toString());
			sessionMap.set(irodsProtocols);
			return irodsProtocol;
		}

		// there is a protocol map, look up the connection for this account

		irodsProtocol = irodsProtocols.get(irodsAccount.toString());

		if (irodsProtocol == null) {
			LOG.debug("null connection in thread local, using IRODSConnectionManager to create a new connection");
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount,
					irodsProtocols);
		} else if (irodsProtocol.isConnected()) {
			LOG.debug("session using previously established connection:{}",
					irodsProtocol);
		} else {
			LOG.warn(
					"***************** session has a connection marked closed, create a new one and put back into the cache:{}",
					irodsProtocol);
			irodsProtocol = connectAndAddToProtocolsMap(irodsAccount,
					irodsProtocols);
		}

		return irodsProtocol;
	}

	/**
	 * @param irodsAccount
	 * @param irodsProtocols
	 * @return
	 * @throws JargonException
	 */
	private IRODSCommands connectAndAddToProtocolsMap(
			final IRODSAccount irodsAccount,
			Map<String, IRODSCommands> irodsProtocols) throws JargonException {
		IRODSCommands irodsProtocol;
		irodsProtocol = irodsProtocolManager.getIRODSProtocol(irodsAccount, this.buildPipelineConfigurationBasedOnJargonProperties());
		if (irodsProtocol == null) {
			LOG.error("no connection returned from connection manager");
			throw new JargonException(
					"null connection returned from connection manager");
		}

		irodsProtocol.setIrodsSession(this);
		irodsProtocols.put(irodsAccount.toString(), irodsProtocol);
		LOG.debug("put a reference to a new connection for account: {}",
				irodsAccount.toString());
		sessionMap.set(irodsProtocols);
		return irodsProtocol;
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
	public void setIrodsConnectionManager(
			final IRODSProtocolManager irodsConnectionManager) {
		this.irodsProtocolManager = irodsConnectionManager;
	}

	/**
	 * Close an iRODS session for the given account
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the connection that
	 *            should be closed.
	 * @throws JargonException
	 *             if an error occurs on the close. If the connection does not
	 *             exist it is logged and ignored.
	 */
	public void closeSession(final IRODSAccount irodsAccount)
			throws JargonException {

		LOG.debug("closing irods session for: {}", irodsAccount.toString());
		final Map<String, IRODSCommands> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			LOG.warn("closing session that is already closed, silently ignore");
			return;
		}

		final IRODSCommands irodsProtocol = irodsProtocols.get(irodsAccount
				.toString());

		if (irodsProtocol == null) {
			LOG.warn("closing a connection that is not held, silently ignore");
			return;

		}
		LOG.debug("found and am closing connection to : {}",
				irodsAccount.toString());

		irodsProtocol.disconnect();

		irodsProtocols.remove(irodsAccount.toString());
		if (irodsProtocols.isEmpty()) {
			LOG.debug("no more connections, so clear cache from ThreadLocal");
			sessionMap.set(null);
		}

	}

	/**
	 * Signal to the <code>IRODSSession</code> that a connection has been forcefully terminated due to errors, and
	 * should be removed from the cache.
	 * @param irodsAccount {@link IRODSAccount} that maps the connection
	 * @throws JargonException
	 */
	public void discardSessionForErrors(final IRODSAccount irodsAccount)
			throws JargonException {

		LOG.warn("discarding irods session for: {}", irodsAccount.toString());
		final Map<String, IRODSCommands> irodsProtocols = sessionMap.get();
		if (irodsProtocols == null) {
			LOG.warn("discarding session that is already closed, silently ignore");
			return;
		}
		
		irodsProtocols.remove(irodsAccount.toString());

		if (irodsProtocols.isEmpty()) {
			LOG.debug("no more connections, so clear cache from ThreadLocal");
			sessionMap.set(null);
		}

	}

	/**
	 * This method is not particularly useful, but does provide a route to get a
	 * direct handle on the connections for this Thread in cases where such
	 * status information needs to be kept. Returns null if no map is available.
	 * 
	 * @return
	 */
	public Map<String, IRODSCommands> getIRODSCommandsMap() {
		return sessionMap.get();
	}

	protected IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	protected void setIrodsProtocolManager(
			final IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	/**
	 * Get (lazily) the pool of parallel transfer threads. This will return
	 * <code>null</code> if the use of the pool is not set in the
	 * <code>JargonProperties</code>. The method will create the pool on the
	 * first request based on the <code>JargonProperties</code>, and once
	 * created, changing the properties does not reconfigure the pool, it just
	 * returns the lazily created instance.
	 * 
	 * @return {@link ExecutorService} that is the pool of threads for the
	 *         paralllel transfers, or <code>null</code> if the pool is not
	 *         configured in the jargon properties.
	 * @throws JargonException
	 */
	public ExecutorService getParallelTransferThreadPool()
			throws JargonException {
		log.info("getting the ParallelTransferThreadPool");
		synchronized (this) {

			if (!jargonProperties.isUseTransferThreadsPool()) {
				log.info("I am not using the parallel transfer threads pool, return null");
				return null;
			}

			if (parallelTransferThreadPool != null) {
				log.info("returning already created ParallelTransferThreadPool");
				return parallelTransferThreadPool;
			}

			int poolSize = jargonProperties.getTransferThreadPoolMaxSimultaneousTransfers() * jargonProperties.getMaxParallelThreads();
			int maxParallelThreads =  jargonProperties.getMaxParallelThreads();
			
			log.info("creating the parallel transfer threads pool");
			log.info("   max # threads: {}",
					maxParallelThreads);
			
			log.info("   pool timeout millis:{}",
					jargonProperties.getTransferThreadPoolTimeoutMillis());

			parallelTransferThreadPool = new ThreadPoolExecutor(
					maxParallelThreads,
					poolSize,
					jargonProperties.getTransferThreadPoolTimeoutMillis(),
					TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
							poolSize),
					new RejectedParallelThreadExecutionHandler());
			
			log.info("parallelTransferThreadPool created");
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

}
