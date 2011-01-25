package org.irods.jargon.core.pub;

import java.io.File;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;

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
public interface DataObjectAO extends IRODSAccessObject {

	/**
	 * Put a local file to IRODS. This method will operate using any defaults in
	 * the <code>JargonProperties</code> to control things like behavior of
	 * parallel transfers.
	 * 
	 * @param localFile
	 *            <code>java.io.File</code> containing the data to go to IRODS
	 * @param irodsFileDestination
	 *            {@link org.IRODSFileImpl.jargon.core.pub.io.IRODSFile} that
	 *            gives the destination for the put. If the given target
	 *            destination is given as an iRODS collection, the name of the
	 *            local file will be the name of the iRODS file, and it will
	 *            proceed as normal.
	 * @param overwrite
	 *            <code>boolean</code> that determines whether this is an
	 *            overwrite
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODS(File localFile,
			IRODSFile irodsFileDestination, boolean overwrite)
			throws JargonException;

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
	 * FIXME: refactor along with data obj avu code
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 */
	void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Retrieve a file from iRODS and store it locally. A specific resource is
	 * not indicated to iRODS for the get operation.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @throws JargonException
	 */
	void getDataObjectFromIrods(final IRODSFile irodsFileToGet,
			final File localFileToHoldData) throws DataNotFoundException,
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
	 * @throws JargonException
	 */
	void irodsDataObjectGetOperationForClientSideAction(
			final IRODSFile irodsFileToGet, final File localFileToHoldData)
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
	 * Put a local file to IRODS with special consideration for the requirements
	 * of client-side put operations in rules. There is a subtle issue with
	 * issuing certain commands to an iRODS agent that is processing a rule, and
	 * doing anything that causes a GenQuery to run will cause unexpected query
	 * results to be returned from the agent when not requested. This method is
	 * not intended for use other than when processing client side put actions
	 * as commanded by an iRODS rule. This special method sets a flag to quash
	 * certain checks that the normal put operation does on the target iRODS
	 * file for the put.
	 * 
	 * @param localFile
	 *            <code>java.io.File</code> containing the data to go to IRODS
	 * @param irodsFileDestination
	 *            {@link org.IRODSFileImpl.jargon.core.pub.io.IRODSFile} that
	 *            gives the destination for the put. If the given target
	 *            destination is given as an iRODS collection, the name of the
	 *            local file will be the name of the iRODS file, and it will
	 *            proceed as normal.
	 * @param overwrite
	 *            <code>boolean</code> that determines whether this is an
	 *            overwrite
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODSForClientSideRuleOperation(
			final File localFile, final IRODSFile irodsFileDestination,
			final boolean overwrite) throws JargonException;

	/**
	 * Retrieve a file from iRODS and store it locally. The resource provided in
	 * the <code>IRODSFile<code> object is sent in the
	 * request to iRODS as the specific resource from which the file is retrieved.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @throws JargonException
	 */
	void getDataObjectFromIrodsUsingTheSpecificResourceSetInIrodsFile(
			final IRODSFile irodsFileToGet, final File localFileToHoldData)
			throws DataNotFoundException, JargonException;

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
	 * @throws JargonException
	 */
	DataObject findByAbsolutePath(final String absolutePath)
			throws JargonException;

	/**
	 * FIXME: not yet implemented Update the comment for the given data object.
	 * 
	 * @param comment
	 *            <code>String</code> with desired comment value. Set to blank
	 *            if comment is to be cleared.
	 * @param dataObjectAbsolutePath
	 *            <code>String</code>
	 */
	void updateComment(final String comment, final String dataObjectAbsolutePath)
			throws JargonException;

	/**
	 * Set the permissions on a data object to read for the given user.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the data object.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @throws JargonException
	 */
	void setAccessPermissionRead(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to write for the given user.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the data object.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @throws JargonException
	 */
	void setAccessPermissionWrite(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Set the permissions on a data object to own for the given user.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the data object.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @throws JargonException
	 */
	void setAccessPermissionOwn(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Removes the permissions on a data object to own for the given user.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the data object.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @throws JargonException
	 */
	void removeAccessPermissionsForUser(String zone, String absolutePath,
			String userName) throws JargonException;

	/**
	 * Get the file permission pertaining to the given data object
	 * @param absolutePath <code>String</code> with the absolute path to the data object.
	 * @param userName <code>String</code> with the user name whose permissions will be checked.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @return {@link FilePermissionEnum} value with the permissions for the file and user.
	 * @throws JargonException
	 */
	FilePermissionEnum getPermissionForDataObject(String absolutePath,
			String userName, String zone) throws JargonException;

}