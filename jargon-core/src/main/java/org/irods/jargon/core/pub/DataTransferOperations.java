package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * This is an access object that can be used to move data to, from, and between
 * iRODS resources. Generally, this should be the first place to look for
 * methods that move data. There are other objects that are of potential
 * interest, including objects that represent data objects -
 * {@link org.irods.jargon.core.pub.DataObjectAO}, objects that represent iRODS
 * collections - {@link org.irods.jargon.core.pub.DataObjectAO}, and that
 * represent iRODS data objects and collections as <code>java.io.*</code>
 * operations - see {@link org.irods.jargon.core.pub.io.IRODSFile}.
 * 
 * This interface has a default implementation within Jargon. The access object
 * should be obtained using a factory, either by creating from
 * {@link org.irods.jargon.core.pub.IRODSFileSystem}, or from an
 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactory} implementation.
 * This class is handy for retrieving and manipulating system and user metadata
 * associated with collection objects (files), as well as performing common
 * query operations. This class also supports various iRODS file operations that
 * are not included in the standard <code>java.io.*</code> libraries.
 * 
 * For general data movement operations, also see
 * {@link org.irods.jargon.core.pub.DataTransferOperations}.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface DataTransferOperations extends IRODSAccessObject {

	/**
	 * Transfer a file between iRODS resources
	 * 
	 * @param absolutePathToSourceFile
	 *            <code>String</code> with the absolute path to the source file
	 *            in iRODS.
	 * @param absolutePathToTargetFile
	 *            <code>String</code> with the absolute path to the target file
	 *            in iRODS.
	 * @param targetResource
	 *            <code>String</code> with the target resource name iRODS.
	 * @throws JargonException
	 * @throws JargonFileOrCollAlreadyExistsException
	 *             if a move is made to a file or collection that already exists
	 */
	void physicalMove(final String absolutePathToSourceFile,
			final String targetResource)
			throws JargonFileOrCollAlreadyExistsException, JargonException;

	/**
	 * Move a file or collection between two locations in iRODS. This method
	 * will inspect the paths and create the appropriate command to iRODS
	 * automatically. In this method, the target file is expressed as the actual
	 * target location. There are other methods in this class that will take the
	 * last part of the source path, and use that as the collection name in the
	 * target.
	 * 
	 * For this method, if the source is /coll1/coll2/coll3 and the target is
	 * /coll4/coll5/coll6, the coll3 directory will be renamed to coll6 in the
	 * target.
	 * 
	 * For a data object, this method will automatically handle a case where the
	 * source file is a data object, and the target file is a collection. In
	 * this case, the file name is propagated as the name of the file under the
	 * target collection.
	 * 
	 * If an attempt is made to move a file to itself, the case will be logged
	 * and ignored.
	 * 
	 * @param absolutePathToSourceFile
	 *            <code>String<code> with the absolute path to the source file.
	 * @param absolutePathToTargetFile
	 *            <code>String</code> with the absolute path to the target of
	 *            the move.
	 * @throws JargonException
	 * @throws JargonFileOrCollAlreadyExistsException
	 *             if a move is made to a file or collection that already exists
	 */
	void move(final String absolutePathToSourceFile,
			final String absolutePathToTargetFile)
			throws JargonFileOrCollAlreadyExistsException, JargonException;

	/**
	 * Put a file or a collection (recursively) to iRODS. This method allows
	 * registration of a <code>TransferStatusCallbackListener</code> that will
	 * provide callbacks about the status of the transfer suitable for progress
	 * monitoring
	 * 
	 * @param sourceFile
	 *            <code>File</code> with the source directory or file.
	 * @param targetIrodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} with the target
	 *            iRODS file or collection.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws JargonException
	 */
	void putOperation(
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException;

	/**
	 * Get a file or collection from iRODS to the local file system. This method
	 * will detect whether this is a get of a single file, or of a collection.
	 * If this is a get of a collection, the method will recursively obtain the
	 * data from iRODS.
	 * 
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
	 * @throws JargonException
	 */
	void getOperation(
			final IRODSFile irodsSourceFile,
			final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException;

	/**
	 * Perform a replication operation. This will copy the given file to a
	 * target iRODS resource.
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file
	 *            that should be replicated.
	 * @param targetResource
	 *            <code>String</code> with the resource to which the file should
	 *            be replicated.
	 * @param transferStatusCallbackListener
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferStatusCallbackListener}
	 *            that can receive status callbacks. This may be set to null if
	 *            this functionality is not required.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws JargonException
	 */
	void replicate(
			final String irodsFileAbsolutePath,
			final String targetResource,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException;

	/**
	 * Move the given source collection (must be a collection) underneath the
	 * given target collection. This method only applies to moving a collection
	 * to a collection, and will throw an exception if a data object is used for
	 * the source or target. This convenience method takes the last path
	 * component of the source collection, and moves that collection underneath
	 * the target.
	 * 
	 * If the source is /col1/col2/col3 and the target is /col4/col5, then the
	 * result of the move will be /col4/col5/col3.
	 * 
	 * This method will detect an attempt to reparent a file to its current
	 * collection, and will LOG and ignore this case.
	 * 
	 * @param absolutePathToSourceFile
	 *            <code>String</code> with the absolute path to the source
	 *            collection. The last path component will b moved underneath
	 *            the target as described above.
	 * @param absolutePathToTheTargetCollection
	 *            <code>String</code> with the absoulute path to the target
	 *            collection, which will be the parent of the source collection
	 *            as described above.
	 * @throws JargonException
	 */
	void moveTheSourceCollectionUnderneathTheTargetCollectionUsingSourceParentCollectionName(
			final String absolutePathToSourceFile,
			final String absolutePathToTheTargetCollection)
			throws JargonException;

}