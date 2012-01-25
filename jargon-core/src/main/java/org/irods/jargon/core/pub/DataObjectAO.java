package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * This is an access object that can be used to manipulate iRODS data objects
 * (files). This object treats the IRODSFile as an object, not as a
 * <code>java.io.File</code> object. For normal read and other familier
 * <code>java.io.*</code> operations, see
 * {@link org.irods.jargon.core.pub.io.IRODSFile}.
 * 
 * This interface has a default implementation within Jargon. The access object
 * should be obtained using a factory, either by creating from
 * {@link org.irods.jargon.core.pub.IRODSFileSystem}, or from an
 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactory} implementation.
 * This class is handy for retrieving and manipulating system and user metadata
 * associated with data objects (files), as well as performing common query
 * operations. This class also supports various iRODS file operations that are
 * not included in the standard <code>java.io.*</code> libraries.
 * 
 * For general data movement operations, also see
 * {@link org.irods.jargon.core.pub.DataTransferOperations}.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface DataObjectAO extends FileCatalogObjectAO {

	/**
	 * Query method will return the first data object found with the given
	 * collectionPath and dataName.
	 * 
	 * Note that this method will return 'null' if the object is not found.
	 * 
	 * @param collectionPath
	 *            <code>String</code> with the absolute path to the collection
	 * @param dataName
	 *            <code>String</code> with the data Object name
	 * @return {@link org.irods.jargon.core.pub.DataObject}
	 * @throws DataNotFoundException
	 *             is thrown if the data object does not exist
	 * @throws JargonException
	 */
	DataObject findByCollectionNameAndDataName(final String collectionPath,
			final String dataName) throws JargonException;

	/**
	 * Handy query method will return DataObjects that match the given 'WHERE'
	 * clause. This appends the default selects such that they can be converted
	 * into domain objects.
	 * 
	 * @param where
	 *            <code>String</code> with the iquest form query condition,
	 *            omitting the WHERE clause
	 * @return a <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.DataObject}
	 * @throws JargonException
	 */
	List<DataObject> findWhere(final String where) throws JargonException;

	/**
	 * For a given absolute path, get an <code>IRODSFileImpl</code> that is a
	 * data object. If the data exists, and is not a File, this method will
	 * throw an exception. If the given file does not exist, then a File will be
	 * returned.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with absolute path to the collection
	 * @return {@link org.irods.jargon.core.pub.io.IRODSFileImpl}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFileForPath(final String fileAbsolutePath)
			throws JargonException;

	/**
	 * Add AVU metadata for this data object
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             when data object is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5)
	 *             is inconsistent, where a duplicate will only be detected if
	 *             units are not blank
	 */
	void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, DuplicateDataException,
			JargonException;

	/**
	 * Retrieve a file from iRODS and store it locally.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers. This get operation will use the default
	 * settings for <code>TransferOptions</code>.
	 * <p/>
	 * A note about overwrites: This method call does not allow for
	 * specification of transfer options or registration for a callback
	 * listener, instead, it will look at the configured
	 * <code>JargonProperties</code> for any global settings on overwrites.
	 * Other method signatures for get operations allow specification of force
	 * options, and also allow interaction between the caller and the
	 * transferring process when an overwrite is detected.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void getDataObjectFromIrods(final IRODSFile irodsFileToGet,
			final File localFileToHoldData) throws OverwriteException,
			DataNotFoundException,
			JargonException;

	/**
	 * Get operation for a single data object. This method allows specification
	 * of a <code>TransferOptions</code>, which will be cloned and used in this
	 * individual transfer (the method may override the transferOptions based on
	 * evaluation of the transfer).
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * Note that this is a shorthand method call that will create a default
	 * <code>TransferControlBlock</code> and use the default
	 * <code>TransferOptions</code> set on properties.
	 * <p/>
	 * Note that the <code>TransferOptions</code>, if provided, will indicate
	 * whether to do a force operation. If force is turned off, then an
	 * attempted overwrite will result in an <code>OverwriteException</code>. In
	 * order to interactively set this option, use the method signature for get
	 * that includes the <code>TransferStatusCallbackListener</code>.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * 
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @param transferOptions
	 *            {@link TransferOptions} that will be cloned internally and
	 *            used to control aspects of the transfer. This can be
	 *            <code>null</code> if not needed, in which case the
	 *            <code>JargonProperties</code> will be consulted to build a
	 *            default set of options. Note that the
	 *            <code>TransferOptions</code> object will be cloned, and as
	 *            such the passed-in parameter will not be altered.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 * @deprecated In order to align the related transfer methods in this API
	 *             and accomodate future growth in transfer options, it is
	 *             advised to switch to the method that allows provision of the
	 *             <code>TransferControlBlock</code> and
	 *             <code>TransferStatus</code> methods.
	 */
	@Deprecated
	void getDataObjectFromIrodsGivingTransferOptions(IRODSFile irodsFileToGet,
			File localFileToHoldData, TransferOptions transferOptions)
			throws OverwriteException, DataNotFoundException, JargonException;

	/**
	 * Get operation for a single data object. This method allows the the
	 * definition of a <code>TransferControlBlock</code> object as well as a
	 * <code>TransferStatusCallbackListener</code>.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer. Setting the resource name in the
	 *            <code>irodsFileToGet</code> will specify that the file is
	 *            retrieved from that particular resource.
	 * 
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener}, or <code>null</code>
	 *            if not specified, that can receive call-backs on the status of
	 *            the transfer operation
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void getDataObjectFromIrods(IRODSFile irodsFileToGet,
			File localFileToHoldData,
			TransferControlBlock transferControlBlock,
			TransferStatusCallbackListener transferStatusCallbackListener)
			throws OverwriteException, DataNotFoundException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as
	 * identifying information about the data object itself, based on a metadata
	 * query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param dataObjectCollectionAbsPath
	 *            <code>String with the absolute path of the collection for the dataObject of interest.
	 * @param dataObjectFileName
	 *            <code>String with the name of the dataObject of interest.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(
			final List<AVUQueryElement> avuQuery,
			final String dataObjectCollectionAbsPath,
			final String dataObjectFileName) throws JargonQueryException,
			JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as
	 * identifying information about the data object itself, based on a metadata
	 * query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param dataObjectAbsolutePath
	 *            <code>String</code> with the absolute path of the data object
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(
			List<AVUQueryElement> avuQuery, String dataObjectAbsolutePath)
			throws JargonQueryException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as
	 * identifying information about the data object itself. Other methods are
	 * available for this object to refine to query to include an AVU metadata
	 * query. This method will get all of the metadata for a data object.
	 * 
	 * @param dataObjectCollectionAbsPath
	 *            <code>String with the absolute path of the collection for the dataObject of interest.
	 * @param dataObjectFileName
	 *            <code>String with the name of the dataObject of interest.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */

	List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			final String dataObjectCollectionAbsPath,
			final String dataObjectFileName) throws JargonQueryException,
			JargonException;

	/**
	 * List the data objects that answer the given AVU metadata query
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException;

	/**
	 * Retrieve a file from iRODS and store it locally. This method will assume
	 * that the resource is not specified, which is useful for processing
	 * client-side rule actions, or other occasions where the get operation
	 * needs to be directly processed and there can be no other intervening XML
	 * protocol operations.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer. The resource of the
	 *            <code>IRODSFile</code> is controlling.
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer
	 * @param {@link TransferOptions} to control the transfer, or null if not
	 *        specified. Note that the <code>TransferOptions</code> object will
	 *        be cloned, and as such the passed-in parameter will not be
	 *        altered.
	 * @throws JargonException
	 */
	void irodsDataObjectGetOperationForClientSideAction(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final TransferOptions transferOptions)
			throws DataNotFoundException, JargonException;

	/**
	 * Handy query method will return DataObjects that match the given 'WHERE'
	 * clause. This appends the default selects such that they can be converted
	 * into domain objects.
	 * 
	 * @param where
	 *            <code>String</code> with the iquest form query condition,
	 *            omitting the WHERE clause
	 * @param partialStart
	 *            <code>int</code> with the partial start index when paging
	 *            through large queries
	 * @return a <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.DataObject}
	 * @throws JargonException
	 */
	List<DataObject> findWhere(final String where, final int partialStart)
			throws JargonException;

	/**
	 * List the data objects that answer the given AVU metadata query with the
	 * ability to page through a partial start index.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param partialStartIndex
	 *            <code>int</code> with a partial start value for paging
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery, final int partialStartIndex)
			throws JargonQueryException, JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS Data
	 * Objects that match the metadata query
	 * 
	 * @param avuQueryElements
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @return
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<DataObject> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS Data
	 * Objects that match the metadata query. This query method allows a partial
	 * start as an offset into the result set to get paging behaviors.
	 * 
	 * @param avuQueryElements
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @param partialStartIndex
	 *            <code>int</code> that has the partial start offset into the
	 *            result set
	 * @return
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<DataObject> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException,
			JargonException;

	/**
	 * Replicate the given file to the given target resource. Note that this
	 * method replicates one data object. The
	 * {@link org.irods.jargon.core.pub.DataTransferOperations} access object
	 * has more comprehensive methods for replication, including recursive
	 * replication with the ability to process callbacks.
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> containing the absolute path to the target
	 *            iRODS file to replicate.
	 * @param dataObjectName
	 *            <code>String<code> with the name of the data object.
	 * @param targetResource
	 *            <code>String</code> containing the resource to which the
	 *            target file should be replicated.
	 * @throws JargonException
	 */
	void replicateIrodsDataObject(final String irodsFileAbsolutePath,
			final String targetResource) throws JargonException;

	/**
	 * Get a list of <code>Resource</code> objects that contain this data
	 * object.
	 * 
	 * @param dataObjectPath
	 *            <code>String</code> containing the absolute path to the target
	 *            iRODS collection containing the file
	 * @param dataObjectName
	 *            <code>String</code> containing the name of the target iRODS
	 *            file.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.Resource}
	 * @throws JargonException
	 */
	List<Resource> getResourcesForDataObject(final String dataObjectPath,
			final String dataObjectName) throws JargonException;

	/**
	 * Compute a checksum on a File, iRODS uses MD5 by default
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} upon which the
	 *            checksum value will be calculated
	 * @return <code>String</code> with the MD5 Checksum value
	 * @throws JargonException
	 */
	String computeMD5ChecksumOnDataObject(final IRODSFile irodsFile)
			throws JargonException;

	/**
	 * Replicate the data object given as an absolute path to all of the
	 * resources defined in the <code>irodsResourceGroupName</code>. This is
	 * equivilant to an irepl -a command.
	 * 
	 * The {@link org.irods.jargon.core.pub.DataTransferOperations} access
	 * object has more comprehensive methods for replication, including
	 * recursive replication with the ability to process callbacks.
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> containing the absolute path to the target
	 *            iRODS file to replicate.
	 * @param irodsResourceGroupName
	 *            <code>String<code> with the name of the resource group to which the file will be replicated.  The replication will be to 
	 * all members of the resource group.
	 * @throws JargonException
	 */
	void replicateIrodsDataObjectToAllResourcesInResourceGroup(
			final String irodsFileAbsolutePath,
			final String irodsResourceGroupName) throws JargonException;

	/**
	 * Method to put local data to iRODS taking default options, and not
	 * specifying a call-back listener. Note that re-routing of connections to
	 * resources is not done from methods in this class, but can be handled by
	 * using the methods in {@link DataTransferOperations}. Note that this
	 * operation is for a single data object, not for recursive transfers of
	 * collections. See {@link DataTransferOperations} for recursive data
	 * transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether data should be
	 *            overwritten at the target
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source local file does not exist or the target iRODS
	 *             collection does not exist
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODS(File localFile,
			IRODSFile irodsFileDestination, boolean overwrite)
			throws DataNotFoundException, OverwriteException, JargonException;

	/**
	 * Transfer a file or directory from the local file system to iRODS.
	 * <p/>
	 * Note that re-routing of connections to resources is not done from methods
	 * in this class, but can be handled by using the methods in
	 * {@link DataTransferOperations}.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether data should be
	 *            overwritten at the target. This is used to over-ride the
	 *            setting in the <code>TransferControlBlock</code>.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener}, or <code>null</code>
	 *            if not specified, that can receive callbacks on the status of
	 *            the transfer operation
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source local file does not exist or the target iRODS
	 *             collection does not exist
	 * @throws JargonException
	 * @deprecated for consistency, the signature with both the transfer control
	 *             block and the over-write flag will be removed, please use the
	 *             method without <code>overwrite</code>, and specify the
	 *             over-write behavior in the <code>transferControlBlock</code>
	 *             along with other <code>TransferOptions</code> parameters
	 */
	@Deprecated
	void putLocalDataObjectToIRODS(File localFile,
			IRODSFile irodsFileDestination, boolean overwrite,
			TransferControlBlock transferControlBlock,
			TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, OverwriteException, JargonException;

	/**
	 * Transfer a file or directory from the local file system to iRODS.
	 * <p/>
	 * Note that re-routing of connections to resources is not done from methods
	 * in this class, but can be handled by using the methods in
	 * {@link DataTransferOperations}.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener}, or <code>null</code>
	 *            if not specified, that can receive callbacks on the status of
	 *            the transfer operation
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source local file does not exist or the target iRODS
	 *             collection does not exist
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODS(File localFile,
			IRODSFile irodsFileDestination,
			TransferControlBlock transferControlBlock,
			TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, OverwriteException, JargonException;

	/**
	 * Transfer a file or directory from the local file system to iRODS as
	 * invoked by a client-side rule operation. This is used only for special
	 * cases during rule invocation.
	 * <p/>
	 * Note that re-routing of connections to resources is not done from methods
	 * in this class, but can be handled by using the methods in
	 * {@link DataTransferOperations}.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether data should be
	 *            overwritten at the target
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODSForClientSideRuleOperation(File localFile,
			IRODSFile irodsFileDestination, boolean overwrite,
			TransferControlBlock transferControlBlock) throws JargonException;

	/**
	 * Delete the given AVU from the data object identified by absolute path.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with he absolute path to the data object
	 *            from which the AVU triple will be deleted.
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} to be
	 *            removed.
	 * @throws DataNotFoundException
	 *             if the target data object is not found in iRODS
	 * @throws JargonException
	 */
	void deleteAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Find the object representing the data object (file) in iRODS.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the full absolute path to the iRODS
	 *            data object.
	 * @return {@link org.irods.jargon.core.pub.domain.DataObject} with catalog
	 *         information for the given data object
	 * @throws DataNotFoundException
	 *             if data object is not found
	 * @throws JargonException
	 */
	DataObject findByAbsolutePath(final String absolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * Set the permissions on a data object to read for the given user.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionRead(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to write for the given user.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionWrite(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to own for the given user.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionOwn(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Removes the permissions on a data object to own for the given user.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void removeAccessPermissionsForUser(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Get the file permission pertaining to the given data object
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be checked.
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @return {@link FilePermissionEnum} value with the permissions for the
	 *         file and user.
	 * @throws JargonException
	 */
	FilePermissionEnum getPermissionForDataObject(String absolutePath,
			String userName, String zone) throws JargonException;

	/**
	 * Copy a file from one iRODS resource to another with a 'no force' option.
	 * 
	 * @param irodsSourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the source file
	 * @param irodsTargetFileAbsolutePath
	 *            <code>String</code> with the absolute path to the target file.
	 * @param targetResourceName
	 *            <code>String</code> with the optional (blank if not specified)
	 *            resource that will hold the target file * @throws
	 *            OverwriteException if an overwrite is attempted and the force
	 *            option has not been set
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 * @deprecated it is advised to switch to the copyIRODSDataObject() method
	 *             that takes the optional <code>TransferControlBlock</code> and
	 *             <code>TransferStatusCallbackListener</code> objects.
	 */
	@Deprecated
	void copyIrodsDataObject(String irodsSourceFileAbsolutePath,
			String irodsTargetFileAbsolutePath, String targetResourceName)
			throws OverwriteException, DataNotFoundException, JargonException;

	/**
	 * Copy a file from one iRODS resource to another with a 'force' option that
	 * will overwrite another file.
	 * 
	 * @param irodsSourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the source file
	 * @param irodsTargetFileAbsolutePath
	 *            <code>String</code> with the absolute path to the target file.
	 * @param targetResourceName
	 *            <code>String</code> with the optional (blank if not specified)
	 *            resource that will hold the target file
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 * @deprecated it is advised to switch to the copyIRODSDataObject() method
	 *             that takes the optional <code>TransferControlBlock</code> and
	 *             <code>TransferStatusCallbackListener</code> objects.
	 * 
	 */
	@Deprecated
	void copyIrodsDataObjectWithForce(String irodsSourceFileAbsolutePath,
			String irodsTargetFileAbsolutePath, String targetResourceName)
			throws OverwriteException, DataNotFoundException, JargonException;

	/**
	 * List the user permissions for the given iRODS data object.
	 * 
	 * @param irodsDataObjectAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS data
	 *            object.
	 * @return <code>List</code> of {@link UserFilePermission} with the ACL's
	 *         for the given file.
	 * @throws JargonException
	 */
	List<UserFilePermission> listPermissionsForDataObject(
			String irodsDataObjectAbsolutePath) throws JargonException;

	/**
	 * List the AVU metadata associated with this irods data object.
	 * 
	 * @param irodsFile
	 *            {@link IRODSfile} that points to the data object whose
	 *            metadata will be retrieved
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			IRODSFile irodsFile) throws JargonException;

	/**
	 * List the AVU metadata associated with this irods data object.
	 * 
	 * @param dataObjectAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS data
	 *            object
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			String dataObjectAbsolutePath) throws JargonException;

	/**
	 * This is a special method to modify the Avu value for a given attribute
	 * name and unit. Often, it is the case that applications want to keep
	 * unique values for a data object, and be able to easily change the value
	 * while preserving the attribute name and units. This method allows the
	 * specification of an AVU with the known name and units, and an arbitrary
	 * value. The method will find the unique attribute by name and unit, and
	 * overwrite the existing value with the value given in the
	 * <code>AvuData</code> parameter.
	 * 
	 * @param absolutePath
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the existing Avu name and unit, with the desired new value
	 * @throws DataNotFoundException
	 *             if the AVU data or collection is not present
	 * @throws JargonException
	 */
	void modifyAvuValueBasedOnGivenAttributeAndUnit(String absolutePath,
			AvuData avuData) throws DataNotFoundException, JargonException;

	/**
	 * Modify the AVU metadata for a data object, giving the absolute path to
	 * the data object, as well as the current and desired AVU data.
	 * 
	 * @param dataObjectAbsolutePath
	 *            <code>String</code> with the absolute path to the data object
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the existing Avu
	 * @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the desired new AVU
	 * @throws DataNotFoundException
	 *             if the file or AVU was not found
	 * @throws JargonException
	 */
	void modifyAVUMetadata(String dataObjectAbsolutePath,
			AvuData currentAvuData, AvuData newAvuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Modify the AVU metadata for a data object, giving the absolute path to
	 * the data object parent collection, and the data object file name, as well
	 * as the current and desired AVU data.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the data object
	 * @param dataName
	 *            <code>String</code> with the data object name
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the existing Avu
	 * @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the desired new AVU
	 * @throws DataNotFoundException
	 *             if the file or AVU was not found
	 * @throws JargonException
	 */
	void modifyAVUMetadata(String irodsCollectionAbsolutePath, String dataName,
			AvuData currentAvuData, AvuData newAvuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Add the AVU Metadata for the given irods parent collection/data name
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS parent
	 *            collection
	 * @param dataName
	 *            <code>String</code> with the file name
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the desired new AVU
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             when data object is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5)
	 *             is inconsistent, where a duplicate will only be detected if
	 *             units are not blank
	 */
	void addAVUMetadata(String irodsCollectionAbsolutePath, String dataName,
			AvuData avuData) throws DataNotFoundException, JargonException;

	/**
	 * List the user permissions for the given iRODS data object.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS data
	 *            object parent collection
	 * @param dataname
	 *            <code>String</code> with the name of the iRODS data Object
	 * @return <code>List</code> of {@link UserFilePermission} with the ACL's
	 *         for the given file.
	 * @throws JargonException
	 */
	List<UserFilePermission> listPermissionsForDataObject(
			String irodsCollectionAbsolutePath, String dataName)
			throws JargonException;

	/**
	 * List the user permissions for the given iRODS data object for a given
	 * user. Note that <code>null</code> will be returned if no permissions are
	 * available.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS data
	 *            object parent collection
	 * @param dataName
	 *            <code>String</code> with the name of the iRODS data Object
	 * @param userName
	 *            <code>String</code> with the name of the iRODS User
	 * @return <code>List</code> of {@link UserFilePermission} with the ACL's
	 *         for the given file.
	 * @throws JargonException
	 */
	UserFilePermission getPermissionForDataObjectForUserName(
			String irodsCollectionAbsolutePath, String dataName, String userName)
			throws JargonException;

	/**
	 * List the user permissions for the given iRODS data object for a given
	 * user. Note that <code>null</code> will be returned if no permissions are
	 * available.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS data
	 *            object
	 * @param userName
	 *            <code>String</code> with the name of the iRODS User
	 * @return <code>List</code> of {@link UserFilePermission} with the ACL's
	 *         for the given file.
	 * @throws JargonException
	 */
	UserFilePermission getPermissionForDataObjectForUserName(
			String irodsAbsolutePath, String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to read for the given user as an
	 * admin. This admin mode is equivalent to the -M switch of the ichmod
	 * icommand.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionReadInAdminMode(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to write for the given user as an
	 * admin. This admin mode is equivalent to the -M switch of the ichmod
	 * icommand.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionWriteInAdminMode(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to own for the given user as an
	 * admin. This admin mode is equivalent to the -M switch of the ichmod
	 * icommand.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void setAccessPermissionOwnInAdminMode(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Remove the permissions on a data object to own for the given user as an
	 * admin. This admin mode is equivalent to the -M switch of the ichmod
	 * icommand.
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the data object.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @throws JargonException
	 */
	void removeAccessPermissionsForUserInAdminMode(String zone,
			String absolutePath, String userName) throws JargonException;

	/**
	 * List the resources that have a copy of the given iRODS file
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            that represents a data object.
	 * @return <code>List</code> of {@Resource} that represent the
	 *         resources in iRODS that have a copy of the file/
	 * @throws JargonException
	 */
	List<Resource> listFileResources(String irodsAbsolutePath)
			throws JargonException;

	/**
	 * Copy a file from one iRODS location to another. This is the preferred
	 * method signature for copy operations, with other forms now deprecated.
	 * Note that the <code>transferControlBlock</code> and
	 * <code>TransferStatusCallbackListener</code> objects are optional and may
	 * be set to <code>null</code> if not required.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * 
	 * 
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param irodsTargetFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            collection or explicitly named target for the transfer
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void copyIRODSDataObject(IRODSFile irodsSourceFile,
			IRODSFile irodsTargetFile,
			TransferControlBlock transferControlBlock,
			TransferStatusCallbackListener transferStatusCallbackListener)
			throws OverwriteException, DataNotFoundException, JargonException;

}