package org.irods.jargon.ticket;

import java.io.File;
import java.io.InputStream;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.ticket.io.FileStreamAndInfo;

public interface TicketClientOperations {

	/**
	 * Wraps a put operation with ticket semantics.
	 * <p/>
	 * Put a file or collection to iRODS. Note that 'force' is not supported
	 * with tickets at this time, so overwrites will return an
	 * <code>OverwriteException</code>
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

	/**
	 * Given an iRODS ticket for a data object, return an object that has an
	 * <code>InputStream</code> for that file, as well as the length of data to
	 * be streamed. This method is oriented towards applications that need to
	 * represent the data from iRODS as a stream.
	 * <p/>
	 * Note that currently only 'get' and 'put' are supported via tickets, so
	 * mid-tier applications that wish to stream data back to the client need to
	 * do an intermediate get to the mid-tier platform and then stream from this
	 * location.
	 * <p/>
	 * Tickets are limited in what they can access, so various operations that
	 * refer to the iCAT, such as obtaining the length, or differentiating
	 * between a file and a collection, cannot be done in the typical way. As a
	 * work-around, this object holds the lenght of the cached file so that it
	 * may be sent in browser responses.
	 * 
	 * @param ticketString
	 *            <code>String</code> with the unique string that represents the
	 *            ticket
	 * @param irodsSourceFile
	 *            {@link IRODSFile} that represents the data to be streamed back
	 *            to the caller
	 * @param intermediateCacheRootDirectory
	 *            {@link File} on the local file system that is the directory
	 *            root where temporary files may be cached. Note that, upon
	 *            close of the returned stream, this file will be cleaned up
	 *            from that cache.
	 * @return {@link FileStreamAndInfo} with a buffered stream that will delete
	 *         the cached file upon close. This object also contains a length
	 *         for the file.
	 * @throws DataNotFoundException
	 *             if the ticket data is not available
	 * @throws JargonException
	 */
	FileStreamAndInfo redeemTicketGetDataObjectAndStreamBack(
			String ticketString, IRODSFile irodsSourceFile,
			File intermediateCacheRootDirectory) throws DataNotFoundException,
			JargonException;

	/**
	 * This method specifically addresses 'upload' scenarios, where data is
	 * supplied via an <code>InputStream</code>, representing the contents that
	 * should be placed in a target file with a given <code>fileName</code>
	 * underneath a given target iRODS collection path in
	 * <code>irodsCollectionAbsolutePath</code>. This method will take the
	 * contents of the input stream, store in a temporary cache location as
	 * described by the <code>intermediateCacheRootDirectory</code>, then put
	 * that file to iRODS. Once the operation is complete, the temporary file
	 * will be removed. This removal is done in a finally block, so that if the
	 * put operation fails, it should minimize leakage of old files.
	 * <p/>
	 * The primary use case for this method is in mid-tier applications where a
	 * file is being uploaded from a browser. Since the iRODS ticket system does
	 * not support input or output streams, the upload needs to be wrapped to
	 * emulate a direct streaming via a ticket.
	 * 
	 * @param ticketString
	 *            <code>String</code> with the unique ticket id, which must have
	 *            write privilages
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the target iRODS parent collection
	 *            absolute path. The file will be placed under this collection
	 *            using the given <code>fileName</code>
	 * @param fileName
	 *            <code>String</code> with the name of the file being uploaded
	 *            to iRODS
	 * @param inputStreamForFileData
	 *            <code>InputStream</code> which should be properly buffered by
	 *            the caller. This could be the input stream resulting from an
	 *            http upload operation
	 * @param temporaryCacheDirectoryLocation
	 *            {@link File} representing a temporary local file system
	 *            directory where temporary files may be cached
	 * @throws DataNotFoundException
	 *             if the ticket information is not available
	 * @throws OverwriteException
	 *             if an overwrite would occur
	 * @throws JargonException
	 */
	void redeemTicketAndStreamToIRODSCollection(String ticketString,
			String irodsCollectionAbsolutePath, String fileName,
			InputStream inputStreamForFileData,
			File temporaryCacheDirectoryLocation) throws DataNotFoundException,
			OverwriteException, JargonException;

}