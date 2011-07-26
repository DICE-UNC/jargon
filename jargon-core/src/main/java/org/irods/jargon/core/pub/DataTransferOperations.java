package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * This is an access object that can be used to move data to, from, and between
 * iRODS resources. Generally, this should be the first place to look for
 * methods that move data. There are other objects that are of potential
 * interest, including objects that represent data objects -
 * {@link org.irods.jargon.core.pub.CollectionAO}, objects that represent iRODS
 * collections - {@link org.irods.jargon.core.pub.DataObjectAO}, and that
 * represent iRODS data objects and collections as <code>java.io.*</code>
 * operations - see {@link org.irods.jargon.core.pub.io.IRODSFile}.
 * <p/>
 * This interface has a default implementation within Jargon. The access object
 * should be obtained using a factory, either by creating from
 * {@link org.irods.jargon.core.pub.IRODSFileSystem}, or from an
 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactory} implementation.
 * <p/>
 * A word should be said about controlling transfers. There are two important
 * components to most of the methods in this class, these are the
 * {@link TransferStatusCallbackListener} and the {@link TransferControlBlock}.
 * The <code>TransferStatusCallbackListener</code> allows a caller of a method
 * in this class to receive call-backs. The call-backs may be an overall
 * initiation of a transfer process, intra-file call-backs that give progress
 * within a file (under development, and will be controllable by setting
 * transfer options}), and per file call-backs. These are handy to display
 * progress bars, monitor error status, and so forth. The
 * <code>TransferControlBlock</code> specifies a shared object between the
 * caller, and a process running a transfer. The
 * <code>TransferControlBlock</code> allows bi-directional communication between
 * a client and a transfer process, and this should be synchronized properly.
 * The <code>TransferControlBlock</code> can signal a cancellation of a process,
 * adjust a transfer as it progresses, and contains aggregate information about
 * the transfer as it runs.
 * <p/>
 * Note that both the <code>TransferControlBlock</code> and
 * <code>TransferStatusCallbackListener</code> are optional, and may be set to
 * <code>null</code> in the various method signatures if not needed.
 * <p/>
 * Note that the status call-backs you receive need to be quickly processed by
 * the implementor of the <code>TransferStatusCallbackListener</code>. Jargon
 * does not play any tricks to queue up these call-backs, they are direct calls
 * from the transfer process and can block progress of a transfer if not handled
 * efficiently.
 * <p/>
 * Transfers can have multiple options that control their behavior. These will
 * be added as necessary, but will likely be too numerous to specify
 * individually. For this reason, a {@TransferOptions} class
 * has been developed. By default, Jargon will consult the
 * {@link JargonProperties} as configured in the {@link IRODSSession} object.
 * Those may be loaded from the default <code>jargon.properties</code> file, or
 * those properties can be set up by an application. If no
 * <code>TransferOptions</code> are specified, the <code>JargonProperties</code>
 * will be consulted to build a default set. In many cases, this is all that is
 * required.
 * <p/>
 * If particular properties are required for an individual transfer, it is
 * possible to specify those options, where they apply. These will be mostly
 * relevant to 'put' and 'get' operations. If custom properties should be set,
 * the proper procedure is to create a <code>TransferControlBlock</code>,
 * typically with the {@link DefaultTransferControlBlock}, and use the method to
 * set a custom <code>TransferOptions</code>. Note that, in
 * <code>DefaultTransferControlBlock</code>, the get() and set() methods are
 * synchronized so that <code>TransferOptions</code> may be changed while
 * transfers occur. This may be done, as each individual file transfer creates a
 * copy of the <code>TransferOptions</code>. To change options while a transfer
 * is running, one may create a new instance of <code>TransferOptions</code>,
 * and then use the synchronized method to set those options in the
 * <code>TransferControlBlock</code>
 * <p/>
 * It is important to note that this arrangement is quite new, and subject to
 * refinement as testing proceeds!
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
	 * <p/>
	 * For this method, if the source is /coll1/coll2/coll3 and the target is
	 * /coll4/coll5/coll6, the coll3 directory will be renamed to coll6 in the
	 * target.
	 * <p/>
	 * For a data object, this method will automatically handle a case where the
	 * source file is a data object, and the target file is a collection. In
	 * this case, the file name is propagated as the name of the file under the
	 * target collection.
	 * <p/>
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
	 * Get a file or collection from iRODS to the local file system. This method
	 * will detect whether this is a get of a single file, or of a collection.
	 * If this is a get of a collection, the method will recursively obtain the
	 * data from iRODS.
	 * 
	 * @param irodsSourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS source
	 *            file to retrieve to the client
	 * @param targetLocalFile
	 *            <code>String</code> that is the absolute path to file in the
	 *            local file system to which the iRODS data will be transferred
	 * @param sourceResourceName
	 *            <code>String</code> with the optional resource from which the
	 *            file will be obtained. This should be left blank if not
	 *            specified (not null)
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
	void getOperation(String irodsSourceFileAbsolutePath,
			String targetLocalFileAbsolutePath, String sourceResourceName,
			TransferStatusCallbackListener transferStatusCallbackListener,
			TransferControlBlock transferControlBlock) throws JargonException;

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
	 * <p/>
	 * If the source is /col1/col2/col3 and the target is /col4/col5, then the
	 * result of the move will be /col4/col5/col3.
	 * <p/>
	 * This method will detect an attempt to reparent a file to its current
	 * collection, and will log and ignore this case.
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

	/**
	 * Copy a file or collection from iRODS to iRODS.
	 * 
	 * @param irodsSourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the source
	 *            collection or data object. This will be copied up to the
	 *            target
	 * @param targetResource
	 *            <code>String</code> with the optional (blank if not specified)
	 *            resource to which the file or collection will be copied
	 * @param irodsTargetFileAbsolutePath
	 *            <code>String<code> with the absolute path to the target iRODS file or collection.   A file may be copied to a collection
	 * @param force
	 *            <code>boolean</code> that indicates that any files that exist
	 *            in the target will be copied over
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
	void copy(String irodsSourceFileAbsolutePath, String targetResource,
			String irodsTargetFileAbsolutePath,
			TransferStatusCallbackListener transferStatusCallbackListener,
			boolean force, TransferControlBlock transferControlBlock)
			throws JargonException;

	/**
	 * Transfer a file from the local file system to iRODS. This will be a
	 * recursive operation if a collection is specified. If a collection is
	 * specified, that collection will become a sub-directory added underneath
	 * the given parent.
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the absolute path of the source file
	 *            on the local file system
	 * @param targetIrodsFileAbsolutePath
	 *            <code>String</code> with the absolute path of the iRODS
	 *            collection that will be the target of the put
	 * @param targetResourceName
	 *            <code>String</code> with the target resource name. This may be
	 *            set to blank if not used, in which case the iRODS default will
	 *            be used. Null is not acceptable
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
	void putOperation(String sourceFileAbsolutePath,
			String targetIrodsFileAbsolutePath, String targetResourceName,
			TransferStatusCallbackListener transferStatusCallbackListener,
			TransferControlBlock transferControlBlock) throws JargonException;

	/**
	 * Move a file or collection between two locations in iRODS. This method
	 * will inspect the paths and create the appropriate command to iRODS
	 * automatically. In this method, the target file is expressed as the actual
	 * target location. There are other methods in this class that will take the
	 * last part of the source path, and use that as the collection name in the
	 * target.
	 * <p/>
	 * For this method, if the source is /coll1/coll2/coll3 and the target is
	 * /coll4/coll5/coll6, the coll3 directory will be renamed to coll6 in the
	 * target.
	 * <p/>
	 * For a data object, this method will automatically handle a case where the
	 * source file is a data object, and the target file is a collection. In
	 * this case, the file name is propagated as the name of the file under the
	 * target collection.
	 * <p/>
	 * If an attempt is made to move a file to itself, the case will be logged
	 * and ignored.
	 * 
	 * @param irodsSourceFile
	 *            <code>IRODSFile<code> with the the source file.
	 * @param irodsTargetFile
	 *            <code>IRODSFile</code> with the target of the move.
	 * @throws JargonException
	 * @throws JargonFileOrCollAlreadyExistsException
	 *             if a move is made to a file or collection that already exists
	 */
	void move(IRODSFile irodsSourceFile, IRODSFile irodsTargetFile)
			throws JargonFileOrCollAlreadyExistsException, JargonException;
}