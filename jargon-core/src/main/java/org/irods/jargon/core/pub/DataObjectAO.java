package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedForCollectionTypeException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.transfer.FileRestartInfo;
import org.irods.jargon.core.transfer.FileRestartInfo.RestartType;
import org.irods.jargon.core.transfer.FileRestartManagementException;

/**
 * This is an access object that can be used to manipulate iRODS data objects
 * (files). This object treats the IRODSFile as an object, not as a
 * {@code java.io.File} object. For normal read and other familier
 * {@code java.io.*} operations, see {@link IRODSFile}.
 * 
 * This interface has a default implementation within Jargon. The access object
 * should be obtained using a factory, either by creating from
 * {@link org.irods.jargon.core.pub.IRODSFileSystem}, or from an
 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactory} implementation.
 * This class is handy for retrieving and manipulating system and user metadata
 * associated with data objects (files), as well as performing common query
 * operations. This class also supports various iRODS file operations that are
 * not included in the standard {@code java.io.*} libraries.
 * 
 * For general data movement operations, also see
 * {@link DataTransferOperations}.
 * 
 * <h2>Notes</h2> For soft links, AVU metadata always attaches to the canonical
 * path. There is some inconsistency with the operation of the imeta command,
 * where AVU operations against the soft link target path result in file not
 * found exceptions. This is a slight departure, but perhaps less surprising.
 * The behavior of AVU metadata for data objects, which always operates on the
 * canonical path, is different than the behavior of iRODS for collection AVU
 * metadata. For collections, the AVU's are attached to the soft linked path
 * separately from the AVU metadata attached to the canonical path. Jargon tries
 * to maximize consistency by always operating on the canonical path for
 * metadata operations on data objects.
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
	 *            {@code String} with the absolute path to the collection
	 * @param dataName
	 *            {@code String} with the data Object name
	 * @return {@link DataObject}
	 * @throws DataNotFoundException
	 *             is thrown if the data object does not exist
	 * @throws JargonException
	 *             for iRODS error
	 * @throws FileNotFoundException
	 *             if file missing
	 */
	DataObject findByCollectionNameAndDataName(final String collectionPath, final String dataName)
			throws JargonException, FileNotFoundException;

	/**
	 * For a given absolute path, get an {@code IRODSFileImpl} that is a data
	 * object. If the data exists, and is not a File, this method will throw an
	 * exception. If the given file does not exist, then a File will be returned.
	 * 
	 * The given path may be a soft-linked path, and it will behave as normal.
	 *
	 * @param fileAbsolutePath
	 *            {@code String} with absolute path to the collection
	 * @return {@link IRODSFileImpl}
	 * @throws JargonException
	 *             for iRODS error
	 */
	IRODSFile instanceIRODSFileForPath(final String fileAbsolutePath) throws JargonException;

	/**
	 * Add AVU metadata for this data object
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 * 
	 * For later (4.1+) versions of iRODS consider using the set AVU methods
	 *
	 * @param absolutePath
	 *            {@code String} with the absolute path to the target data object
	 * @param avuData
	 *            {@link AvuData}
	 * @throws JargonException
	 *             for iRODS error
	 * @throws OperationNotSupportedForCollectionTypeException
	 *             when the special collection type does not support this operation
	 * @throws DataNotFoundException
	 *             when data object is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5) is
	 *             inconsistent, where a duplicate will only be detected if units
	 *             are not blank
	 */
	void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws OperationNotSupportedForCollectionTypeException, DataNotFoundException, DuplicateDataException,
			JargonException;

	/**
	 * Add or update AVU metadata for this data object usimg the new set semantics.
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path. *
	 * 
	 * 'set' modifies an AVU if it exists, or creates one if it does not. If the
	 * AttName does not exist, or is used by multiple objects, the AVU for this
	 * object is added. If the AttName is used only by this one object, the AVU
	 * (row) is modified with the new values, reducing the database overhead (unused
	 * rows).
	 * 
	 * @param absolutePath
	 *            {@code String} with the absolute path to the target data object
	 * @param avuData
	 *            {@link AvuData}
	 * @throws DataNotFoundException
	 *             if file missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAVUMetadata(final String absolutePath, final AvuData avuData) throws DataNotFoundException, JargonException;

	/**
	 * * Add or update AVU metadata for this data object usimg the new set
	 * semantics.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path. *
	 * </p>
	 * <p>
	 * 'set' modifies an AVU if it exists, or creates one if it does not. If the
	 * AttName does not exist, or is used by multiple objects, the AVU for this
	 * object is added. If the AttName is used only by this one object, the AVU
	 * (row) is modified with the new values, reducing the database overhead (unused
	 * rows).
	 * </p>
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the target data object
	 *            parent
	 * @param fileName
	 *            {@code String} with the data object name
	 * @param avuData
	 *            {@link AvuData}
	 * @throws DataNotFoundException
	 *             if avu data missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAVUMetadata(final String irodsCollectionAbsolutePath, final String fileName, final AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as identifying
	 * information about the data object itself, based on a metadata query.
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 *
	 * @param avuQuery
	 *            {@code List} of {@link AVUQueryElement} that defines the metadata
	 *            query
	 * @param dataObjectCollectionAbsPath
	 *            {@code String} with the absolute path of the collection for the
	 *            dataObject of interest.
	 * @param dataObjectFileName
	 *            {@code String} with the name of the dataObject of interest.
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for error in query
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(final List<AVUQueryElement> avuQuery,
			final String dataObjectCollectionAbsPath, final String dataObjectFileName)
			throws JargonQueryException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as identifying
	 * information about the data object itself, based on a metadata query.
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 * 
	 * This version of the method will compare AVU values using case-insensitive
	 * queries
	 *
	 * @param avuQuery
	 *            {@code List} of {@link AVUQueryElement} that defines the metadata
	 *            query
	 * @param dataObjectCollectionAbsPath
	 *            {@code String} with the absolute path of the collection for the
	 *            dataObject of interest.
	 * @param dataObjectFileName
	 *            {@code String} with the name of the dataObject of interest.
	 * @param caseInsensitive
	 *            {@code boolean} where {@code true} indicates to treat avu queries
	 *            as case-insensitive
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(List<AVUQueryElement> avuQuery,
			String dataObjectCollectionAbsPath, String dataObjectFileName, boolean caseInsensitive)
			throws JargonQueryException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as identifying
	 * information about the data object itself, based on a metadata query.
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 *
	 * @param avuQuery
	 *            {@code List} of {@link AVUQueryElement} that defines the metadata
	 *            query
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path of the data object
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(List<AVUQueryElement> avuQuery,
			String dataObjectAbsolutePath) throws JargonQueryException, JargonException;

	/**
	 * List the AVU metadata for a particular data object, as well as identifying
	 * information about the data object itself. Other methods are available for
	 * this object to refine to query to include an AVU metadata query. This method
	 * will get all of the metadata for a data object.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 *
	 * @param dataObjectCollectionAbsPath
	 *            {@code String} with the absolute path of the collection for the
	 *            dataObject of interest.
	 * @param dataObjectFileName
	 *            {@code String} with the name of the dataObject of interest.
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */

	List<MetaDataAndDomainData> findMetadataValuesForDataObject(final String dataObjectCollectionAbsPath,
			final String dataObjectFileName) throws JargonQueryException, JargonException;

	/**
	 * List the data objects that answer the given AVU metadata query.
	 *
	 * @param avuQuery
	 *            {@code List} of {@link AVUQueryElement} that defines the metadata
	 *            query
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery)
			throws JargonQueryException, JargonException;

	/**
	 * List the data objects that answer the given AVU metadata query with the
	 * ability to page through a partial start index.
	 *
	 * @param avuQuery
	 *            {@code List} of {@link AVUQueryElement} that defines the metadata
	 *            query
	 * @param partialStartIndex
	 *            {@code int} with a partial start value for paging
	 * @return {@code List} of {@link MetaDataAndDomainData}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(final List<AVUQueryElement> avuQuery,
			final int partialStartIndex) throws JargonQueryException, JargonException;

	/**
	 * List the data objects that answer the given AVU metadata query with the
	 * ability to page through a partial start index.
	 * 
	 * This version supports case-insensitive metadata queries
	 *
	 * @param avuQuery
	 *            {@code List} of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that defines
	 *            the metadata query
	 * @param partialStartIndex
	 *            {@code int} with a partial start value for paging
	 * @param caseInsensitive
	 *            {@code boolean} indicates that the queries should be
	 *            case-insensitive
	 * @return {@code List} of {@link MetaDataAndDomainData}\
	 * @throws JargonQueryException
	 *             for an error in query
	 * @throws JargonException
	 *             for an iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(List<AVUQueryElement> avuQuery, int partialStartIndex,
			boolean caseInsensitive) throws JargonQueryException, JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS Data Objects
	 * that match the metadata query
	 *
	 * @param avuQueryElements
	 *            {@code List} of {@link AVUQueryElement} with the query
	 *            specification
	 * @return List of {@link DataObject}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<DataObject> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS Data Objects
	 * that match the metadata query. This query method allows a partial start as an
	 * offset into the result set to get paging behaviors.
	 *
	 * @param avuQueryElements
	 *            {@code List} of {@link AVUQueryElement} with the query
	 *            specification
	 * @param partialStartIndex
	 *            {@code int} that has the partial start offset into the result set
	 * @return {@link DataObject}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<DataObject> findDomainByMetadataQuery(final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException, JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS Data Objects
	 * that match the metadata query. This query method allows a partial start as an
	 * offset into the result set to get paging behaviors.
	 * 
	 * This method allows the specification of case-insensitive queries on the AVU
	 * values. This is an iRODS3.2+ capability
	 *
	 * @param avuQueryElements
	 *            {@code List} of {@link AVUQueryElement} with the query
	 *            specification
	 * @param partialStartIndex
	 *            {@code int} that has the partial start offset into the result set
	 * @param caseInsensitive
	 *            {@code boolean} that indicates that the AVU query should be
	 *            processed as case-insensitive
	 * @return List of {@link DataObject}
	 * @throws JargonQueryException
	 *             for query error
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<DataObject> findDomainByMetadataQuery(List<AVUQueryElement> avuQueryElements, int partialStartIndex,
			boolean caseInsensitive) throws JargonQueryException, JargonException;

	/**
	 * Replicate the given file to the given target resource. Note that this method
	 * replicates one data object. The {@link DataTransferOperations} access object
	 * has more comprehensive methods for replication, including recursive
	 * replication with the ability to process callbacks.
	 * 
	 * This method will work if a soft linked name is provided as expected.
	 *
	 * @param irodsFileAbsolutePath
	 *            {@code String} containing the absolute path to the target iRODS
	 *            file to replicate.
	 * @param targetResource
	 *            {@code String} containing the resource to which the target file
	 *            should be replicated.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void replicateIrodsDataObject(final String irodsFileAbsolutePath, final String targetResource)
			throws JargonException;

	/**
	 * Get a list of {@code Resource} objects that contain this data object.
	 * 
	 * This method will work if a soft linked name is provided as expected.
	 *
	 * @param dataObjectPath
	 *            {@code String} containing the absolute path to the target iRODS
	 *            collection containing the file
	 * @param dataObjectName
	 *            {@code String} containing the name of the target iRODS file.
	 * @return {@code List} of {@link org.irods.jargon.core.pub.domain.Resource}
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Resource> getResourcesForDataObject(final String dataObjectPath, final String dataObjectName)
			throws JargonException;

	/**
	 * Compute a checksum on a File, iRODS uses MD5 by default.
	 * 
	 * This method will work if a soft linked name is provided as expected.
	 *
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} upon which the
	 *            checksum value will be calculated
	 * @return {@code String} with the MD5 Checksum value
	 * @throws JargonException
	 *             for iRODS error
	 */
	String computeMD5ChecksumOnDataObject(final IRODSFile irodsFile) throws JargonException;

	/**
	 * Replicate the data object given as an absolute path to all of the resources
	 * defined in the {@code irodsResourceGroupName}. This is equivilant to an irepl
	 * -a command.
	 * 
	 * The {@link DataTransferOperations} access object has more comprehensive
	 * methods for replication, including recursive replication with the ability to
	 * process callbacks.
	 * <p>
	 * This method will work if a soft linked name is provided as expected.
	 *
	 * @param irodsFileAbsolutePath
	 *            {@code String} containing the absolute path to the target iRODS
	 *            file to replicate.
	 * @param irodsResourceGroupName
	 *            {@code String} with the name of the resource group to which the
	 *            file will be replicated. The replication will be to all members of
	 *            the resource group.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void replicateIrodsDataObjectToAllResourcesInResourceGroup(final String irodsFileAbsolutePath,
			final String irodsResourceGroupName) throws JargonException;

	/**
	 * Delete the given AVU from the data object identified by absolute path.
	 * 
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 *
	 * @param absolutePath
	 *            {@code String} with he absolute path to the data object from which
	 *            the AVU triple will be deleted.
	 * @param avuData
	 *            {@link AvuData} to be removed.
	 * @throws DataNotFoundException
	 *             if the target data object is not found in iRODS
	 * @throws JargonException
	 *             for iRODS error
	 */
	void deleteAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Find the object representing the data object (file) in iRODS.
	 * 
	 * This method will handle soft-linked paths and return the data object
	 * representing the data at the given soft linked location.
	 *
	 * @param absolutePath
	 *            {@code String} with the full absolute path to the iRODS data
	 *            object.
	 * @return {@link DataObject} with catalog information for the given data object
	 * @throws JargonException
	 *             for iRODS error
	 * @throws FileNotFoundException
	 *             if file is missing
	 */
	DataObject findByAbsolutePath(final String absolutePath) throws JargonException, FileNotFoundException;

	/**
	 * Set the permissions on a data object to read for the given user.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionRead(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to write for the given user.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionWrite(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to own for the given user.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionOwn(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Removes the permissions on a data object to own for the given user.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 * 
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void removeAccessPermissionsForUser(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Get the file permission pertaining to the given data object
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 * 
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be
	 *            checked.
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @return {@link FilePermissionEnum} value with the permissions for the file
	 *         and user.
	 * @throws JargonException
	 *             for iRODS error
	 */
	FilePermissionEnum getPermissionForDataObject(String absolutePath, String userName, String zone)
			throws JargonException;

	/**
	 * List the user permissions for the given iRODS data object.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param irodsDataObjectAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object.
	 * @return {@code List} of {@link UserFilePermission} with the ACL's for the
	 *         given file.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<UserFilePermission> listPermissionsForDataObject(String irodsDataObjectAbsolutePath) throws JargonException;

	/**
	 * List the AVU metadata associated with this irods data object.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 * </p>
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that points to the data object whose metadata
	 *            will be retrieved
	 * @return {@code List} of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObject(IRODSFile irodsFile) throws JargonException;

	/**
	 * List the AVU metadata associated with this irods data object.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 * </p>
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 * @return {@code List} of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws FileNotFoundException
	 *             if the data object is missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<MetaDataAndDomainData> findMetadataValuesForDataObject(String dataObjectAbsolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * This is a special method to modify the Avu value for a given attribute name
	 * and unit. Often, it is the case that applications want to keep unique values
	 * for a data object, and be able to easily change the value while preserving
	 * the attribute name and units. This method allows the specification of an AVU
	 * with the known name and units, and an arbitrary value. The method will find
	 * the unique attribute by name and unit, and overwrite the existing value with
	 * the value given in the {@code AvuData} parameter.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path. *
	 * </p>
	 * <p>
	 * For later (4.1+) versions of iRODS consider using the set AVU methods
	 * </p>
	 * 
	 * @param absolutePath
	 *            {@code String}
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            existing Avu name and unit, with the desired new value
	 * @throws DataNotFoundException
	 *             if the AVU data or collection is not present
	 * @throws JargonException
	 *             for iRODS error
	 */
	void modifyAvuValueBasedOnGivenAttributeAndUnit(String absolutePath, AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Modify the AVU metadata for a data object, giving the absolute path to the
	 * data object, as well as the current and desired AVU data.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path.
	 * </p>
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to the data object
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            existing Avu
	 * @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            desired new AVU
	 * @throws DataNotFoundException
	 *             if the file or AVU was not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void modifyAVUMetadata(String dataObjectAbsolutePath, AvuData currentAvuData, AvuData newAvuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Modify the AVU metadata for a data object, giving the absolute path to the
	 * data object parent collection, and the data object file name, as well as the
	 * current and desired AVU data.
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path. *
	 * </p>
	 * <p>
	 * For later (4.1+) versions of iRODS consider using the set AVU methods
	 * </p>
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the data object
	 * @param dataName
	 *            {@code String} with the data object name
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            existing Avu
	 * @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            desired new AVU
	 * @throws DataNotFoundException
	 *             if the file or AVU was not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void modifyAVUMetadata(String irodsCollectionAbsolutePath, String dataName, AvuData currentAvuData,
			AvuData newAvuData) throws DataNotFoundException, JargonException;

	/**
	 * Add the AVU Metadata for the given irods parent collection/data name
	 * <p>
	 * Note that, in the case of a soft-linked path, the metadata is associated with
	 * the canonical file path, and AVU metadata associated with the canonical file
	 * path will be reflected if querying the soft link target path. *
	 * </p>
	 * <p>
	 * For later (4.1+) versions of iRODS consider using the set AVU methods
	 * </p>
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection
	 * @param dataName
	 *            {@code String} with the file name
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the
	 *            desired new AVU
	 * @param avuData
	 *            {@link AvuData} to add
	 * @throws JargonException
	 *             for iRODS error
	 * @throws DataNotFoundException
	 *             when data object is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5) is
	 *             inconsistent, where a duplicate will only be detected if units
	 *             are not blank
	 */
	void addAVUMetadata(String irodsCollectionAbsolutePath, String dataName, AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * List the user permissions for the given iRODS data object.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 *            parent collection
	 * @param dataName
	 *            {@code String} with the name of the iRODS data Object
	 * @return {@code List} of {@link UserFilePermission} with the ACL's for the
	 *         given file.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<UserFilePermission> listPermissionsForDataObject(String irodsCollectionAbsolutePath, String dataName)
			throws JargonException;

	/**
	 * List the user permissions for the given iRODS data object for a given user.
	 * Note that {@code null} will be returned if no permissions are available.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 *            parent collection
	 * @param dataName
	 *            {@code String} with the name of the iRODS data Object
	 * @param userName
	 *            {@code String} with the name of the iRODS User
	 * @return {@code List} of {@link UserFilePermission} with the ACL's for the
	 *         given file.
	 * @throws JargonException
	 *             for iRODS error
	 */
	UserFilePermission getPermissionForDataObjectForUserName(String irodsCollectionAbsolutePath, String dataName,
			String userName) throws JargonException;

	/**
	 * List the user permissions for the given iRODS data object for a given user.
	 * Note that {@code null} will be returned if no permissions are available.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 * @param userName
	 *            {@code String} with the name of the iRODS User
	 * @return {@code List} of {@link UserFilePermission} with the ACL's for the
	 *         given file.
	 * @throws JargonException
	 *             for iRODS error
	 */
	UserFilePermission getPermissionForDataObjectForUserName(String irodsAbsolutePath, String userName)
			throws JargonException;

	/**
	 * Set the permissions on a data object to read for the given user as an admin.
	 * This admin mode is equivalent to the -M switch of the ichmod icommand.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionReadInAdminMode(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to write for the given user as an admin.
	 * This admin mode is equivalent to the -M switch of the ichmod icommand.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionWriteInAdminMode(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to own for the given user as an admin.
	 * This admin mode is equivalent to the -M switch of the ichmod icommand.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermissionOwnInAdminMode(String zone, String absolutePath, String userName) throws JargonException;

	/**
	 * Remove the permissions on a data object to own for the given user as an
	 * admin. This admin mode is equivalent to the -M switch of the ichmod icommand.
	 * <p>
	 * Note that permissions are kept by the canonical path name. This method will
	 * find the canonical path if this is a soft link and operate on that data
	 * object.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with an optional zone for the file. Leave blank if
	 *            not used, it is not required.
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object.
	 * @param userName
	 *            {@code String} with the user name whose permissions will be set.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void removeAccessPermissionsForUserInAdminMode(String zone, String absolutePath, String userName)
			throws JargonException;

	/**
	 * List the resources that have a copy of the given iRODS file
	 * <p>
	 * Note that this method will follow a soft link and list the resources based on
	 * the canonical path.
	 * </p>
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the iRODS file that
	 *            represents a data object.
	 * @return {@code List} of {@link Resource} that represent the resources in
	 *         iRODS that have a copy of the file/
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Resource> listFileResources(String irodsAbsolutePath) throws JargonException;

	/**
	 * Given a {@code ObjStat}, return a DataObject reflecting the representation of
	 * that data object in the iRODS iCAT. this {@code DataObject} takes special
	 * collection status into account, so that if this is a soft link, it will carry
	 * information about the canoncial path.
	 *
	 * @param objStat
	 *            {@link ObjStat} reflecting the iRODS data object
	 * @return {@link DataObject} representing the iCAT data for the file in iRODS
	 * @throws DataNotFoundException
	 *             if file missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	DataObject findGivenObjStat(ObjStat objStat) throws DataNotFoundException, JargonException;

	/**
	 * Method to set access permission to the desired state, this variant makes it
	 * less necessary to stack 'if' tests in permission setting code.
	 * <p>
	 * Note that {@code FilePermissionEnum} has more permission states defined then
	 * are currently supported by this method. This may require more iRODS core
	 * server updates to make this range of permissions meaningful.
	 * </p>
	 * <p>
	 * For the current variant of this method, only READ, WRITE, and OWN are
	 * supported, Other permission values will cause a {@code JargonException}. This
	 * may be relaxed in the future. Also note that NONE is supported, and actually
	 * causes the access permission to be removed.
	 * </p>
	 *
	 * @param zone
	 *            {@code String} with the zone for the user. This method will work
	 *            cross-zone if appropriate permissions are in place
	 * @param absolutePath
	 *            {@code String} with the absolute path for the data object
	 * @param userName
	 *            {@code userName} (just the name, no name#zone format) for the user
	 * @param filePermission
	 *            {@link FilePermissionEnum}
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setAccessPermission(String zone, String absolutePath, String userName, FilePermissionEnum filePermission)
			throws JargonException;

	/**
	 * Find the data object (file) given it's unique id (the iCAT primary key)
	 *
	 * @param id
	 *            {@code int} with the primary key for the data object in the ICAT
	 * @return {@link DataObject} corresponding to the given id
	 * @throws FileNotFoundException
	 *             if the id does not exist
	 * @throws JargonException
	 *             for iRODS error
	 */
	DataObject findById(int id) throws FileNotFoundException, JargonException;

	/**
	 * List the replicas of a file in a given resource gorup
	 *
	 * @param collectionAbsPath
	 *            {@code String} with the absolute path for the irods parent
	 *            collection
	 * @param fileName
	 *            {@code String} with the data object file name
	 * @param resourceGroupName
	 *            {@code String} with the resource group name
	 * @return {@code List} of {@link DataObject} for replicas in the given resource
	 *         group
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<DataObject> listReplicationsForFileInResGroup(String collectionAbsPath, String fileName,
			String resourceGroupName) throws JargonException;

	/**
	 * Get the total number of replicas for the given data object
	 *
	 * @param collection
	 *            {@code String} with the absolute path for the irods parent
	 *            collection
	 * @param fileName
	 *            {@code String} with the data object file name
	 * @return {@code int} with total number of replicas
	 * @throws JargonException
	 *             for iRODS error
	 */
	int getTotalNumberOfReplsForDataObject(String collection, String fileName) throws JargonException;

	/**
	 * Get the total number of replicas for the given data object in the given
	 * resource group
	 * 
	 * @param irodsAbsolutePath
	 *            {@code String}
	 * @param fileName
	 *            {@code String} with the data object file name
	 * @param resourceGroupName
	 *            {@code String} with the resource group name
	 * @return {@code int} with the total number of replicas
	 * @throws JargonException
	 *             for iRODS error
	 */
	int getTotalNumberOfReplsInResourceGroupForDataObject(String irodsAbsolutePath, String fileName,
			String resourceGroupName) throws JargonException;

	/**
	 * General method to trim replicas for a resource or resource group. Check the
	 * parameter notes carefully.
	 *
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection
	 * @param fileName
	 *            {@code String} with the file name of the data object to be trimmed
	 * @param resourceName
	 *            {@code String} with the optional (blank if not specified) replica
	 *            resource to trim
	 * @param numberOfCopiesToKeep
	 *            {@code int} with the optional (leave -1 if not specified) number
	 *            of copies to retain
	 * @param replicaNumberToDelete
	 *            {@code int} with a specific replica number to trim (leave as -1 if
	 *            not specified)
	 * @param asIRODSAdmin
	 *            {@code boolean} to process the given action as the rodsAdmin
	 * @throws DataNotFoundException
	 *             if the data object is not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void trimDataObjectReplicas(String irodsCollectionAbsolutePath, String fileName, String resourceName,
			int numberOfCopiesToKeep, int replicaNumberToDelete, boolean asIRODSAdmin)
			throws DataNotFoundException, JargonException;

	/**
	 * List all data object replicas
	 *
	 * @param collectionAbsPath
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection
	 * @param fileName
	 *            {@code String} with the file name of the data object to be trimmed
	 * @return {@code List} of {@link DataObject} for each replica
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<DataObject> listReplicationsForFile(String collectionAbsPath, String fileName) throws JargonException;

	/**
	 * Replicate the given data object using a delayed execution
	 *
	 * @param irodsCollectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS parent
	 *            collection
	 * @param fileName
	 *            {@code String} with the file name of the data object to be trimmed
	 * @param resourceName
	 *            {@code String} with the optional (blank if not specified) replica
	 *            resource to trim
	 * @param delayInMinutes
	 *            {@code int} with the number of minutes to delay the replication,
	 *            will put on the deferred rule execution queue
	 * @throws JargonException
	 *             for iRODS error
	 */
	void replicateIrodsDataObjectAsynchronously(String irodsCollectionAbsolutePath, String fileName,
			String resourceName, int delayInMinutes) throws JargonException;

	/**
	 * Given a list of avu metadata, add all to the data object. A response will be
	 * returned giving individual success/failure information. For example, an
	 * attempt to add a duplicate AVU will result in an error entry in the response
	 * versus a thrown exception.
	 *
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object
	 * @param avuData
	 *            {@code List} of {@link AvuData} for each AVU to be added.
	 * @return {@link BulkAVUOperationResponse} with details on the success or
	 *         failure of the add of each AVU.
	 * @throws FileNotFoundException
	 *             if the data object is missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<BulkAVUOperationResponse> addBulkAVUMetadataToDataObject(String absolutePath, List<AvuData> avuData)
			throws FileNotFoundException, JargonException;

	/**
	 * Given a list of avu metadata, delete all from the data object. A response
	 * will be returned giving individual success/failure information. Note that a
	 * delete of a non-existent AVU will be silently ignored
	 *
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object
	 * @param avuData
	 *            {@code List} of {@link AvuData} for each AVU to be deleted.
	 * @return {@link BulkAVUOperationResponse} with details on the success or
	 *         failure of the delete of each AVU.
	 * @throws FileNotFoundException
	 *             if the data object is missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<BulkAVUOperationResponse> deleteBulkAVUMetadataFromDataObject(String absolutePath, List<AvuData> avuData)
			throws JargonException;

	/**
	 * Clear all AVUs from the given data object by doing a bulk delete operation
	 *
	 * @param absolutePath
	 *            {@code String} with the absolute path to the data object
	 * @throws DataNotFoundException
	 *             if the data object is not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void deleteAllAVUForDataObject(final String absolutePath) throws DataNotFoundException, JargonException;

	/**
	 * Utility to stream back an iRODS file and compute a SHA-1 checksum value. Note
	 * that this requires pulling in the file data via stream so it can be expensive
	 * to do for large files. Further, this computed checksum is not the one stored
	 * in iRODS, rather it is computed on the fly. Be aware that data has to be read
	 * to compute this value, and it can be expensive.
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with an iRODS absolute path
	 * @return {@code byte[]} with a SHA-1 checksum value
	 * @throws DataNotFoundException
	 *             if missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	byte[] computeSHA1ChecksumOfIrodsFileByReadingDataFromStream(final String irodsAbsolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * Look up an AVU associated with a data object by providing an ObjStat and id
	 * (the id key of the AVU)
	 *
	 * @param objStat
	 *            {@link ObjStat} for the data object
	 * @param id
	 *            {@code int} with the unique key for the AVU attribute
	 * @return {@link MetaDataAndDomainData} representing that AVU
	 * @throws JargonException
	 *             for iRODS error
	 * @throws DataNotFoundException
	 *             if the AVU is not found
	 */
	MetaDataAndDomainData findMetadataValueForDataObjectById(ObjStat objStat, int id)
			throws DataNotFoundException, JargonException;

	/**
	 * Look up an AVU associated with a data object by providing a path and id (the
	 * id key of the AVU)
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path for the data object
	 * @param id
	 *            {@code int} with the unique key for the AVU attribute
	 * @return {@link MetaDataAndDomainData} representing that AVU
	 * @throws JargonException
	 *             for iRODS error
	 * @throws DataNotFoundException
	 *             if the AVU is not found
	 * @throws FileNotFoundException
	 *             if file not found
	 */
	MetaDataAndDomainData findMetadataValueForDataObjectById(String dataObjectAbsolutePath, int id)
			throws FileNotFoundException, DataNotFoundException, JargonException;

	/**
	 * Only retrieve a restart if it exists, {@code null} if it does not
	 *
	 * @param restartType
	 *            {@link RestartType}
	 * @param irodsAbsolutePath
	 *            {@code String}
	 * @return {@link FileRestartInfo}
	 * @throws FileRestartManagementException
	 *             if error in restart
	 */
	FileRestartInfo retrieveRestartInfoIfAvailable(final RestartType restartType, final String irodsAbsolutePath)
			throws FileRestartManagementException;

	/**
	 * Convenience method to compare the checksum of a given local file and a
	 * complementary iRODS file, returning a {@code boolean} indicating whether the
	 * files match
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} to checksum
	 * @param localFile
	 *            {@link File} to checksum
	 * @return {@code boolean} indicating whether the checksums match
	 * @throws FileNotFoundException
	 *             if either file is missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean verifyChecksumBetweenLocalAndIrods(final IRODSFile irodsFile, final File localFile)
			throws FileNotFoundException, JargonException;

}
