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
import org.irods.jargon.core.transfer.TransferControlBlock;

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
 * operations, the connection must be closed. Typically, an
 * {@link IRODSFileSystem} is instantiated, and that
 * <code>IRODSFileSystem</code> is used to get a reference to this access object
 * factory. Once operations are done, the <code>IRODSFileSystem</code> can be
 * used to close connections in that thread. This factory has hooks to also
 * close those connections, and this can be used in cases where this factory is
 * injected itself into another service.
 * <p/>
 * Be aware that there should only be one reference to an
 * <code>IRODSFileSystem</code>. This object should not be created for every
 * operation, rather, it should be created and placed in a shared context,
 * passed as a reference, or wrapped in a singleton. Looking at the JUnit code
 * for usage can be somewhat misleading in this respect.
 * <p/>
 * 
 * For example, if using the 'shortcut' <code>IRODSFileSystem</code>. object,
 * you may see a pattern of use like this:
 * 
 * <pre>
 *  IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
 * 
 * UserAO adminUserAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
 * adminUserAO.doSomething();
 * ZoneAO zoneAO = irodsFileSystem.getIRODSAccessObjectFactory().getZoneAO(irodsAccount);
 * zoneAO.doSomething()
 * 
 * irodsFileSystem.close();
 * </pre>
 * 
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSAccessObjectFactory {

	/**
	 * Create an instance of a <code>UserAO</code> access object to interact
	 * with iRODS Users.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.UserAO}
	 * @throws JargonException
	 */
	UserAO getUserAO(final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create an instance of a <code>EnvironmentalInfoAO</code> access object to
	 * retrieve global information from iRODS.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.EnvironmentalInfoAO}
	 * @throws JargonException
	 */
	EnvironmentalInfoAO getEnvironmentalInfoAO(final IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Create an instance of a <code>IRODSGenQueryExecutor</code> access object
	 * to interact with iRODS GenQuery.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.IRODSGenQueryExecutor}
	 * @throws JargonException
	 */
	IRODSGenQueryExecutor getIRODSGenQueryExecutor(
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create an instance of a <code>ZoneAO</code> access object to interact
	 * with iRODS Zones.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.ZoneAO}
	 * @throws JargonException
	 */
	ZoneAO getZoneAO(final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create an instance of a <code>ResourceAO</code> access object to interact
	 * with iRODS Resources.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.ResourceAO}
	 * @throws JargonException
	 */
	ResourceAO getResourceAO(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Create an instance of a <code>IRODSFileSystemAO</code> access object to
	 * interact with iRODS Files. Note that this acess object, while usable as
	 * an API, should rarely need direct use. Rather, the facilities of
	 * {@link org.irods.jargon.core.pub.io.IRODSFile} should be utilized.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.IRODSFileSystemAO}
	 * @throws JargonException
	 */
	IRODSFileSystemAO getIRODSFileSystemAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Returns an <code>IRODSFileFactory</code> that can be used to create
	 * various types of iRODS files and streams.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.io.IRODSFileFactory}
	 * @throws JargonException
	 */
	IRODSFileFactory getIRODSFileFactory(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Create an instance of a <code>UserGroupAO</code> access object to
	 * interact with iRODS UserGroups.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.UserGroupAO}
	 * @throws JargonException
	 */
	UserGroupAO getUserGroupAO(final IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Close all connections associated with the current thread.
	 * 
	 * @throws JargonException
	 */
	void closeSession() throws JargonException;

	/**
	 * Returns a <code>CollectionAO</code> implementation that works on IRODS
	 * Collection objects
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.CollectionAO}
	 * @throws JargonException
	 */
	CollectionAO getCollectionAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Returns a <code>DataObjectAO</code> implementation that works on IRODS
	 * data objects (files)
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.DataObjectAO}
	 * @throws JargonException
	 */
	DataObjectAO getDataObjectAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Returns a <code>CollectionAndDataObjectListAndSearchAO</code> that
	 * contains methods useful for interface search boxes, as well as pagable
	 * tree views of the iRODS hierarchy. This is distinct from the typical find
	 * and list methods in the access objects for DataObject and Collection.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.CollectionAO}
	 * @throws JargonException
	 */
	CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO(
			IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Returns a <code>RuleProcessingAO</code> implementation that interacts
	 * with iRODS rule processing
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.RuleProcessingAO}
	 * @throws JargonException
	 */
	RuleProcessingAO getRuleProcessingAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Returns a <code>DataTransferOperations</code> object that provides a
	 * centralized set of methods for moving data around the iRODS system.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.DataTransferOperations}
	 * @throws JargonException
	 */
	DataTransferOperations getDataTransferOperations(
			final IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Returns a <code>RemoteExecutionOfCommandsAO</code> that can execute
	 * commands (scripts) remotely on an iRODS server.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO}
	 * @throws JargonException
	 */
	RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO(
			IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Close the underlying connection for the given IRODSAccount.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @throws JargonException
	 */
	void closeSession(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * For easy wiring via dependency injection, an access object factory may be
	 * created and injected with the <code>IRODSSession</code>
	 * 
	 * @param irodsSession
	 *            {@link IRODSSession} that will manage the connection to iRODS.
	 */
	void setIrodsSession(final IRODSSession irodsSession);

	/**
	 * Get the <code>IRODSSession</code> that manages connections
	 * 
	 * @return {@link IRODSSession}
	 */
	IRODSSession getIrodsSession();

	/**
	 * Returns a <code>BulkFileOperationsAO</code> that can handle the creation
	 * and extraction of compressed file archives (tars) in iRODS.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link BulkFileOperationsAO}
	 * @throws JargonException
	 */
	BulkFileOperationsAO getBulkFileOperationsAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Get the access object that can manage quota information and settings.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link QuotaAO} instance
	 * @throws JargonException
	 */
	QuotaAO getQuotaAO(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Returns a <code>SimpleQueryExecutorAO</code> that can execute queries on
	 * iRODS using the Simple Query facility. This allows direct SQL queries
	 * using pre-arranged statements. These queries are typically used for
	 * various admin functions, and require admin rights.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link SimpleQueryExecutorAO} to send and process results of a
	 *         simple query.
	 * @throws JargonException
	 */
	SimpleQueryExecutorAO getSimpleQueryExecutorAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Close all connections for this session. Any resulting exceptions are
	 * logged as a warning and ignored.
	 * 
	 * @throws JargonException
	 */
	void closeSessionAndEatExceptions();

	/**
	 * Close the underlying connection for the given IRODSAccount. Any resulting
	 * exceptions are logged as a warning and ignored.
	 * 
	 * @param irodsAccount
	 */
	void closeSessionAndEatExceptions(IRODSAccount irodsAccount);

	/**
	 * Get an access object that can assist in stream to stream or byte array to
	 * stream copies into iRODS.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link Stream2StreamAO}
	 * @throws JargonException
	 */
	Stream2StreamAO getStream2StreamAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Convenience method to obtain a reference to the
	 * <code>JargonProperties</code> that controls behavior of Jargon.
	 * 
	 * @return {@link JargonProperties}
	 */
	JargonProperties getJargonProperties() throws JargonException;

	/**
	 * Get an AO to query audit trail info for a data object
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link DataObjectAuditAO}
	 * @throws JargonException
	 */
	DataObjectAuditAO getDataObjectAuditAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Get an AO to query audit trail info for a collection
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link CollectionAuditAO}
	 * @throws JargonException
	 */
	CollectionAuditAO getCollectionAuditAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Get an AO to administer mounted collections and soft links
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link MountedCollectionAO}
	 * @throws JargonException
	 */
	MountedCollectionAO getMountedCollectionAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Get an AO that manages iRODS file registration and de-registration
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link IRODSRegistrationOfFilesAO}
	 * @throws JargonException
	 */
	IRODSRegistrationOfFilesAO getIRODSRegistrationOfFilesAO(
			IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Build a <code>TransferControlBlock</code> reflecting the default options
	 * as configured in the <code>JargonProperties</code>.
	 * 
	 * @return {@link TransferControlBlock} reflecting default options and
	 *         properties.
	 * @throws JargonException
	 */
	TransferControlBlock buildDefaultTransferControlBlockBasedOnJargonProperties()
			throws JargonException;

	/**
	 * Get the default transfer options based on the properties that have been
	 * set. This can then be tuned for an individual transfer
	 * 
	 * @return {@link TransferOptions} based on defaults set in the jargon
	 *         properties
	 * @throws JargonException
	 */
	public TransferOptions buildTransferOptionsBasedOnJargonProperties()
			throws JargonException;

	/**
	 * Get an AO that allows extension of jargon core libraries by other
	 * packages that need to directly invoke and process packing instructions
	 * and other protocol operations.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link ProtocolExtensionPoint} that may be used to communicate
	 *         with iRODS using packing instructions.
	 * @throws JargonException
	 */
	ProtocolExtensionPoint getProtocolExtensionPoint(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Get the properties of the iRODS server described by the provided
	 * <code>IRODSAccount</code>
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server for which
	 *            properties will be derived
	 * @return {@link IRODSServerProperties} for the server at the given account
	 * @throws JargonException
	 */
	IRODSServerProperties getIRODSServerProperties(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Return a <code>ResourceGroupAO</code> object that can handle the resource
	 * groups in the iRODS icat
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link ResourceGroupAO} that can interact with resource groups on
	 *         the iCAT
	 * @throws JargonException
	 */
	ResourceGroupAO getResourceGroupAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Create an instance of a <code>SpecificQueryAO</code> access object to
	 * interact with iRODS Specific Queries.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the connection to iRODS.
	 * @return {@link org.irods.jargon.core.pub.SpecificQueryAO}
	 * @throws JargonException
	 */
	SpecificQueryAO getSpecificQueryAO(final IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Cause an <code>IRODSAccount</code> to be authenticated, and return and
	 * <code>AuthResponse</code> augmented with information about the principal.
	 * <p/>
	 * Note that the account information is actually cached in a thread local by
	 * the <code>IRODSSession</code>, so this method will return the cached
	 * response if already authenticated. If not cached, this method causes an
	 * authentication process.
	 * 
	 * @param irodsAccount
	 *            {@IRODSAccount} with the authenticating
	 *            principal
	 * @return {@link AuthResponse} containing information about the
	 *         authenticated principal. Note that the authentication process may
	 *         cause the authenticating <code>IRODSAccount</code> to be altered
	 *         or augmented. The resulting account that can be cached and
	 *         re-used by applications will be in the authenticated account.
	 * @throws AuthenticationException
	 *             If the principal cannot be authenticated. This will be thrown
	 *             on initial authentication
	 * @throws JargonException
	 */
	AuthResponse authenticateIRODSAccount(IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException;

	/**
	 * Handy method to see if we're using the dynamic server properties cache.
	 * This is set in the jargon properties.
	 * 
	 * @return <code>boolean</code> true if jargon methods can store and refer
	 *         to cached properties of an iRODS server
	 */
	boolean isUsingDynamicServerPropertiesCache();

	/**
	 * 
	 * Simple cache (tolerating concurrent access) for name/value props. This
	 * cache is meant to hold user-definable properties about a connected server
	 * (by host and zone name). This is meant as an efficient way to record
	 * properties of a connected iRODS server that are discovered by interacting
	 * with the server. This is especially useful for operations that may or may
	 * not be configured, such that repeated failed attempts at an operation are
	 * not made.
	 * <p/>
	 * A good example would be if required specific queries, rules,
	 * micro-services, or remote command scripts are not available to do an
	 * operation.
	 * 
	 * @return
	 */
	DiscoveredServerPropertiesCache getDiscoveredServerPropertiesCache();

	/**
	 * Get service to handle pagaeable collection listings Get a utiltity to
	 * deal with checksums in iRODS
	 * 
	 * @param irodsAccount
	 *            {@IRODSAccount} with the authenticating
	 *            principal
	 * @return {@link CollectionPagerAO}
	 * @throws JargonException
	 */
	CollectionPagerAO getCollectionPagerAO(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * @return {@link DataObjectChecksumUtilitiesAO} that supports varous
	 *         checksum operations
	 * @throws JargonException
	 */
	DataObjectChecksumUtilitiesAO getDataObjectChecksumUtilitiesAO(
			IRODSAccount irodsAccount) throws JargonException;

}