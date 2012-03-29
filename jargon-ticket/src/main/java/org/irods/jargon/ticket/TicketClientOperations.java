package org.irods.jargon.ticket;

import java.io.File;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

public interface TicketClientOperations {

	/**
	 * Wraps a put operation with ticket semantics.
	 * <p/>
	 * Put a file or collection to iRODS.
	 * 
	 * @param ticketString
	 *            <code>String</code> with the unique ticket string
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that points to
	 *            the file or collection to retrieve.
	 * @param targetLocalFile
	 *            <code>File</code> that will hold the retrieved data.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks indicating the
	 *            real-time status of the transfer. This may be set to null if
	 *            not required
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void putFileToIRODSUsingTicket(
			final String ticketString,
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException;

	/**
	 * Wraps a get operation with ticket semantics.
	 * <p/>
	 * Get a file or collection from iRODS to the local file system. This method
	 * will detect whether this is a get of a single file, or of a collection.
	 * If this is a get of a collection, the method will recursively obtain the
	 * data from iRODS.
	 * 
	 * @param ticketString
	 *            <code>String</code> with the unique ticket string
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that points to
	 *            the file or collection to retrieve.
	 * @param targetLocalFile
	 *            <code>File</code> that will hold the retrieved data.
	 * @param transferStatusCallbackListener
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            implementation that will receive callbacks indicating the
	 *            real-time status of the transfer. This may be set to null if
	 *            not required
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void getOperationFromIRODSUsingTicket(
			final String ticketString,
			final IRODSFile irodsSourceFile,
			final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException;

}