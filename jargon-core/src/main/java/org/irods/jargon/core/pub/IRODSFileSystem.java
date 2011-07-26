/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a simplified way of obtaining a connection and getting
 * references to the key factory classes. This class encapsulates a default
 * <code>IRODSProtocolManager</code> responsible for creating connections. This
 * default manager will create a new connection for each requested connection.
 * For other behaviors, such as pooling or cacheing of connections, use other
 * implementations of <code>IRODSProtocolManager</code> directly.
 * <p/>
 * Typical usage would be to create an instance of this object, then use the
 * <code>IRODSFileSystem</code> class to obtain a reference of an
 * <code>IRODSAccessObjectFactory</code> and a <code>IRODSFileFactory</code> .
 * These two factory objects allow creation of various iRODS file
 * implementations that map to the <code>java.io.*</code> packages, and to
 * create various Access Objects.
 * <p/>
 * An Access Object represents the various domains within iRODS (such as User,
 * Resource, Collection) and allows accessing, updating, managing metadata, and
 * other operations specific to each of these objects. Access Objects will
 * operate and return various domain objects that represent data within iRODS.
 * The factories will return instances of file objects and Access Objects upon
 * request using the provided <code>IRODSAccount</code>. The
 * <code>IRODSAccount</code> indicates the iRODS server to which that Access or
 * file object is connected. In other words, one IRODSFileSystem object will
 * manage connections to multiple iRODS servers.
 * <p/>
 * After a connection is no longer needed, it must be closed by using the proper
 * close method contained in the <code>IRODSFileSystem</code> object. Please
 * consult the method documentation of this class for various close options.
 * <p/>
 * It is important to note that the <code>IRODSFileSystem</code> is not created
 * multiple times in typical usage. The proper technique is to create the
 * <code>IRODSFileSystem</code>, and place it in a shared context (such as in
 * the ApplicationContext of a servlet application, use a shared reference, or
 * wrap this object in a singleton for lookup. The JUnit tests can be somewhat
 * misleading in terms of proper usage. The object is safe to share between
 * multiple threads. The underlying connections are tied to a
 * <code>ThreadLocal</code>, such that they are not shared between threads. This
 * <code>IRODSFileSystem</code> object does hold shared instances of objects
 * that manage connections, and these objects are intended to manage the
 * creation of connections across the entire application. The shared objects are
 * designed to manage these connections across multiple threads.
 * <p/>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSFileSystem {

	private final IRODSProtocolManager irodsProtocolManager;
	private final IRODSSession irodsSession;
	private transient IRODSAccessObjectFactory irodsAccessObjectFactory = null;

	private static final Logger log = LoggerFactory
			.getLogger(IRODSFileSystem.class);

	/**
	 * Create a default IRODSFileSystem
	 * 
	 * @return <code>IRODSFileSystem</code> that is initialized and ready to
	 *         connect
	 * @throws JargonException
	 */
	public static IRODSFileSystem instance() throws JargonException {
		return new IRODSFileSystem();
	}

	public IRODSFileSystem() throws JargonException {
		this.irodsProtocolManager = IRODSSimpleProtocolManager.instance();
		this.irodsSession = IRODSSession.instance(irodsProtocolManager);
		log.info("IRODSfileSystem is initialized");
	}

	/**
	 * Lazily initialize and return an <code>IRODSAccessObjectFactoryImpl</code>
	 * 
	 * @return {@link org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl}
	 * @throws JargonException
	 */
	public IRODSAccessObjectFactory getIRODSAccessObjectFactory()
			throws JargonException {
		if (irodsAccessObjectFactory == null) {
			irodsAccessObjectFactory = IRODSAccessObjectFactoryImpl
					.instance(irodsSession);
		}
		return irodsAccessObjectFactory;
	}

	/**
	 * For a given <code>IRODSAccount</code> create an
	 * <code>IRODSFileFactory</code> that can return iRODS file objects for the
	 * particular connection.
	 * 
	 * @param irodsAccount
	 *            {@link org.irods.jargon.core.connection.IRODSAccount}
	 * @return {@link org.irods.jargon.core.pub.io.IRODSFileFactory}
	 * @throws JargonException
	 */
	public IRODSFileFactory getIRODSFileFactory(final IRODSAccount irodsAccount)
			throws JargonException {
		return new IRODSFileFactoryImpl(irodsSession, irodsAccount);
	}

	/**
	 * Close all open iRODS connections that were used in this Thread. Note that
	 * if there are other threads that refer to this
	 * <code>IRODSFileSystem</code>, that Thread must close their own
	 * connection. Connections are stored in a <code>ThreadLocal</code> which
	 * means a Thread's connections to iRODS are only visible from that Thread.
	 * 
	 * @throws JargonException
	 */
	public void close() throws JargonException {
		irodsSession.closeSession();
	}

	/**
	 * Convenience method to close all connections, logging and ignoring any
	 * close errors. This is commonly used in finally blocks and makes code
	 * slightly cleaner. Be sure you do not want an error thrown! If you do, use
	 * the close() method instead.
	 */
	public void closeAndEatExceptions() {
		try {
			irodsSession.closeSession();
		} catch (Exception e) {
			log.error(
					"exception closing connection, this is logged and ignored",
					e);
		}
	}

	/**
	 * Close the session that is connected to the particular iRODS server with
	 * the given account. Note that if there are other threads that refer to
	 * this <code>IRODSFileSystem</code>, that Thread must close their own
	 * connection. Connections are stored in a <code>ThreadLocal</code> which
	 * means a Thread's connections to iRODS are only visible from that Thread.
	 * 
	 * @param irodsAccount
	 */
	public void close(final IRODSAccount irodsAccount) throws JargonException {
		irodsSession.closeSession(irodsAccount);
	}

	/**
	 * Close the session that is connected to the particular iRODS server with
	 * the given account. Note that if there are other threads that refer to
	 * this <code>IRODSFileSystem</code>, that Thread must close their own
	 * connection. Connections are stored in a <code>ThreadLocal</code> which
	 * means a Thread's connections to iRODS are only visible from that Thread.
	 * <p/>
	 * Note that this method wraps the close in a try/catch block, so that any
	 * exception on close is logged and eaten. This is useful in code for neater
	 * finally blocks. If you do want an error thrown, use the
	 * <code>close(IRODSAccount)</code> method.
	 * 
	 * @param irodsAccount
	 */
	public void closeAndEatExceptions(final IRODSAccount irodsAccount) {
		try {
			irodsSession.closeSession(irodsAccount);
		} catch (Exception e) {
			log.error(
					"exception closing connection, this is logged and ignored",
					e);
		}
	}

	/**
	 * Method returns the <code>Map</code> of underlying
	 * <code>IRODSCommands</code> objects. These objects represent the
	 * connections to iRODS by account for the current Thread. This method is
	 * provided to allow display of these connections in diagnostics or for
	 * other uses, but it is highly recommended that these IRODSCommands are not
	 * used for any other purpose.
	 * 
	 * @return {@link org.irods.jagon.core.connection.IRODSCommands} that
	 *         represent low-level connection to iRODS (above the socket level)
	 */
	public Map<String, IRODSCommands> getConnectionMap() {
		return irodsSession.getIRODSCommandsMap();

	}

	/**
	 * Obtain a reference to the <code>IRODSSession</code> object that obtains
	 * connections on behalf of the caller
	 * 
	 * @return
	 */
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	public IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

}
