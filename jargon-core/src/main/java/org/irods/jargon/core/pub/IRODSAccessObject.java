/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;

/**
 * Generic interface that desribes an object that accesses an underlying IRODS
 * domain object
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface IRODSAccessObject {

	AbstractIRODSMidLevelProtocol getIRODSProtocol() throws JargonException;

	/**
	 * Get the {@code IRODSSession} that provides connections to the iRODS
	 * server.
	 *
	 * @return {@link IRODSSession}
	 */
	IRODSSession getIRODSSession();

	/**
	 * Get the {@code IRODSAccount} that describes the connection.
	 *
	 * @return {@link IRODSAccount}
	 */
	IRODSAccount getIRODSAccount();

	/**
	 * Convenience method that gets the underlying
	 * {@code IRODSServerProperties} associated with the connection.
	 *
	 * @return {@link IRODSServerProperties}
	 */
	IRODSServerProperties getIRODSServerProperties() throws JargonException;

	/**
	 * Convenience method gets configuration information that tunes Jargon
	 * behavior
	 *
	 * @return {@link JargonProperties}
	 */
	JargonProperties getJargonProperties();

	/**
	 * Retrieve a factory that can create other access objects
	 *
	 * @return {@link IRODSAccessObjectFactory}
	 * @throws JargonException
	 */
	IRODSAccessObjectFactory getIRODSAccessObjectFactory()
			throws JargonException;

	/**
	 * Retrieve a factory that can create iRODS file objects for this connected
	 * account
	 *
	 * @return {@link IRODSFileFactory}
	 * @throws JargonException
	 */
	IRODSFileFactory getIRODSFileFactory() throws JargonException;

	/**
	 * Convenience method builds a default {@code TransferControlBlock}
	 * that has default {@code TransferOptions} based on the
	 * {@code JargonProperties} configured for the system.
	 *
	 * @return {@link TransferControlBlock} containing default
	 *         {@link TransferOptions} based on the configured
	 *         {@link JargonProperties}
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
	TransferOptions buildTransferOptionsBasedOnJargonProperties()
			throws JargonException;

	/**
	 * Send an operation complete message
	 *
	 * @param status
	 *            {@code int} with the operation complete status to send
	 * @return {@link Tag} with any response (could be null)
	 * @throws JargonException
	 */
	Tag operationComplete(int status) throws JargonException;

	/**
	 * Upon creation, refer to jargon properties and see if this code should be
	 * instrumented
	 *
	 * @return {@code boolean} of {@code true} if performance
	 *         instrumentation is desired
	 */
	boolean isInstrumented();

	/**
	 * Convenience method closes all sessions associated with the current
	 * Thread.
	 * 
	 * @throws JargonException
	 */
	void closeSession() throws JargonException;

	/**
	 * Convenience method closes all sessions associated with the current
	 * Thread, intercepts and logs/ignores any exceptions that occur in the
	 * close operation.
	 * 
	 * @throws JargonException
	 */
	void closeSessionAndEatExceptions();

	/**
	 * Convenience method closes any session associated with the given
	 * {@code IRODSAccount} in the current thread.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} associated with the connection
	 * @throws JargonException
	 */
	void closeSession(IRODSAccount irodsAccount) throws JargonException;

	/**
	 * Convenience method closes any session associated with the given
	 * {@code IRODSAccount} in the current thread. Logs and ignores any
	 * exception in the close operation.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} associated with the connection
	 * @throws JargonException
	 */
	void closeSessionAndEatExceptions(IRODSAccount irodsAccount);

}
