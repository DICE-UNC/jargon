package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.UserFilePermission;

/**
 * This is an access object that can be used to manipulate iRODS Collections.
 * This object treats the IRODSFile that represents a directory as a Collection
 * object, not as a <code>java.io.File</code> object. For familiar
 * <code>java.io.*</code> operations, see
 * {@link org.irods.jargon.core.pub.io.IRODSFile}.
 * <p/>
 * This interface has a default implementation within Jargon. The access object
 * should be obtained using a factory, either by creating from
 * {@link org.irods.jargon.core.pub.IRODSFileSystem}, or from an
 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactory} implementation.
 * This class is handy for retrieving and manipulating system and user metadata
 * associated with collection objects (files), as well as performing common
 * query operations. This class also supports various iRODS file operations that
 * are not included in the standard <code>java.io.*</code> libraries.
 * <p/>
 * For general data movement operations, also see
 * {@link org.irods.jargon.core.pub.DataTransferOperations}.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface CollectionAO extends FileCatalogObjectAO {

	/**
	 * For a given absolute path, get an <code>IRODSFileImpl</code> that is a
	 * collection.
	 * 
	 * @param collectionPath
	 *            <code>String</code> with absolute path to the collection
	 * @return {@link org.irods.jargon.core.pub.io.IRODSFileImpl}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFileForCollectionPath(String collectionPath)
			throws JargonException;

	/**
	 * Given a set of metadata query parameters, return a list of IRODS
	 * Collections that match the metadata query
	 * 
	 * @param avuQueryElements
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @return <code>List</code> of org.irods.jargon.core.pub.domain.Collection}
	 *         with domain objects that satisfy the query.
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<Collection> findDomainByMetadataQuery(
			List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException;

	/**
	 * Get a summary list of collections and data objects and AVU metadata based
	 * on a metadata query
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException;

	/**
	 * Add AVU metadata for this collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws DataNotFoundException when data object is missing
	 * @throws DuplicateDataException when an AVU already exists.  Note that iRODS (at least at 2.5) is inconsistent, where a duplicate will only be detected if units are not blank
	 */
	void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws  DataNotFoundException, DuplicateDataException, JargonException;

	/**
	 * Remove AVU metadata from this collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 */
	void deleteAVUMetadata(final String absolutePath, final AvuData avuData)
			throws JargonException;

	/**
	 * List the AVU metadata for a particular collection, as well as information
	 * identifying the Collection associated with that metadata, based on a
	 * metadata query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param <code>String with the absolute path of the collection of interest.  If this path
	 * is left blank, then the query will not add absolute path to the 'where' clause.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			final List<AVUQueryElement> avuQuery,
			final String collectionAbsolutePath) throws JargonQueryException,
			JargonException;

	/**
	 * Get a list of the metadata values for the given collection absolute path.
	 * This method allows paging of results through a partial start index.
	 * 
	 * @param collectionAbsolutePath
	 *            <code>String</code> with the absolute path of a collection in
	 *            iRODS
	 * @param partialStartIndex
	 *            <code>int</code> with an offset into the results for display
	 *            paging. Set to 0 if no paging offset is desired.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData} with
	 *         AVU values and other values that identify the collection the AVU
	 *         applies to.
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForCollection(
			final String collectionAbsolutePath, final int partialStartIndex)
			throws JargonException, JargonQueryException;
	
	/**
	 * Get a list of the metadata values for the given collection absolute path.
	 * 
	 * @param collectionAbsolutePath
	 *            <code>String</code> with the absolute path of a collection in
	 *            iRODS
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData} with
	 *         AVU values and other values that identify the collection the AVU
	 *         applies to.
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForCollection(
			final String collectionAbsolutePath)
			throws JargonException, JargonQueryException;


	/**
	 * Given a set of metadata query parameters, return a list of IRODS
	 * Collections that match the metadata query. This query method allows a
	 * partial start as an offset into the result set to get paging behaviors.
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
	List<Collection> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException,
			JargonException;

	/**
	 * Find all <code>Collection</code> objects under the given parent.
	 * 
	 * @param absolutePathOfParent
	 *            <code>String</code> with the absolute path of the parent
	 *            directory under which the search should happen. Note a space
	 *            will be interpereted as the root directory by default.
	 * @return a <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.Resource};
	 * @throws JargonException
	 */

	List<Collection> findAll(String absolutePathOfParent)
			throws JargonException;

	/**
	 * Find all <code>Collection</code> objects under the given parent. Note
	 * that this method allows a partial start to be specified such that paging
	 * can be done over large collections.
	 * 
	 * @param absolutePathOfParent
	 *            <code>String</code> with the absolute path of the parent
	 *            directory under which the search should happen. Note a space
	 *            will be interpereted as the root directory by default.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset into the result set from
	 *            which results should be returned, for a paging behavior.
	 * @return a <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.Resource};
	 * @throws JargonException
	 */
	List<Collection> findAll(String absolutePathOfParent, int partialStartIndex)
			throws JargonException;

	/**
	 * List the AVU metadata for a particular collection, as well as information
	 * about the collection itself, based on a metadata query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param <code>String</code> with additional conditions to further limit
	 *        the query, set to blank if unused.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryWithAdditionalWhere(
			final List<AVUQueryElement> avuQuery, final String additionalWhere)
			throws JargonQueryException, JargonException;

	/**
	 * Retrieve a list of <code>Collection</code> domain objects that match the
	 * given query.
	 * 
	 * @param whereClause
	 *            <code>String</code> with where clause in iquest format,
	 *            without the WHERE prefix
	 * @param partialStartIndex
	 *            <code>int</code> with the starting point to return results. 0
	 *            indicates no offset.
	 * @return <code>List<Collection></code> with the query results.
	 * @throws JargonException
	 */
	List<Collection> findWhere(final String whereClause,
			final int partialStartIndex) throws JargonException;

	/**
	 * For the given absolute path, return the given collection.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the collection
	 * @return {@link org.irods.jargon.core.pub.domain.Collection} or null if no
	 *         collection found
	 * @throws DataNotFoundException if collection does not exist
	 * @throws JargonException
	 */
	Collection findByAbsolutePath(final String irodsCollectionAbsolutePath)
			throws JargonException;

	/**
	 * List the AVU metadata for a particular collection, as well as information
	 * about the collection itself, based on a metadata query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param <code>String with the absolute path of the collection of interest.  If this path
	 * is left blank, then the query will not add absolute path to the 'where' clause.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			final List<AVUQueryElement> avuQuery,
			final String collectionAbsolutePath, final int partialStartIndex)
			throws JargonQueryException, JargonException;

	/**
	 * For a given iRODS collection, give a count of the total number of data
	 * objects underneath that collection. This will include files in child
	 * directories.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection to count
	 * @return <code>int</code> with the total count of files, recursively
	 *         counted.
	 * @throws JargonException
	 */
	int countAllFilesUnderneathTheGivenCollection(
			final String irodsCollectionAbsolutePath) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to read.  This can optionally be recursively applied.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionRead(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to write.  This can optionally be recursively applied.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionWrite(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to own.  This can optionally be recursively applied.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionOwn(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, remove access permissions for a given user.  This can optionally be recursively applied.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void removeAccessPermissionForUser(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set he default to inherit access permissions
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionInherit(String zone, String absolutePath,
			boolean recursive) throws JargonException;

	/**
	 * Check the given collection (by absolute path) to see if the inheritance flag is set.  This indicates that access permissions are inherited by children of the collection.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @return
	 * @throws JargonException
	 */
	boolean isCollectionSetForPermissionInheritance(
			String absolutePath) throws JargonException;

	/**
	 * For a given iRODS collection, set he default to not inherit access permissions
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param recursive <code>boolean</code> that indicates whether the permission should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionToNotInherit(String zone, String absolutePath,
			boolean recursive) throws JargonException;

	/**
	 * Get the file permission value for the given absolute path for the given user.
	 * @param absolutePath <code>String</code> with the absolute path to the collection.
	 * @param userName <code>String</code> with the user name whose permissions will be set.
	 * @param zone <code>String</code> with an optional zone for the file.  Leave blank if not used, it is not required.
	 * @return {@link FilePermissionEnum} value with the permission level for the given user.
	 * @throws JargonException
	 */
	FilePermissionEnum getPermissionForCollection(String irodsAbsolutePath,
			String userName, String zone) throws JargonException;

	/**
	 * Get a list of all permissions for all users on the given collection
	 * @param irodsCollectionAbsolutePath <code>String</code> with the absolute path to the iRODS collection for which permissions
	 * will be retrieved.
	 * @return <code>List</code> of  {@link FilePermissionEnum} 
	 * @throws JargonException
	 */
	List<UserFilePermission> listPermissionsForCollection(
			String irodsCollectionAbsolutePath) throws JargonException;

	/**
	 * Overwrite AVU metadata for this collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the current avu.  This will be looked up by attribute + value
	 *  @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the desired state of the avu
	 * @throws JargonException
	 * @throws DataNotFoundExeption if the AVU is not present
	 */
	void modifyAVUMetadata(String absolutePath, AvuData currentAvuData,
			AvuData newAvuData) throws DataNotFoundException, JargonException;

	/**
	 * This is a special method to modify the Avu value for a given attribute name and unit.   Often, it is the case that applications want to keep unique values for a collection, and be able to easily change the value while preserving 
	 * the attribute name and units.  This method allows the specification of an AVU with the known name and units, and an arbitrary value.  The method will find the unique attribute by 
	 * name and unit, and overwrite the existing value with the value given in the <code>AvuData</code> parameter.  
	* @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing the existing Avu name and unit, with the desired new value
	 * @throws DataNotFoundException if the AVU data is not present
	 * @throws JargonException
	 */
	void modifyAvuValueBasedOnGivenAttributeAndUnit(String absolutePath,
			AvuData avuData) throws DataNotFoundException, JargonException;

}