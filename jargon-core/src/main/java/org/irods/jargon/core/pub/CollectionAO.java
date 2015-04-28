package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;

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
 * <p/>
 * <h2>Notes</h2>
 * For soft links, AVU metadata always attaches to the given path, which can be
 * a soft link. This is somewhat different than metadata handling for data
 * objects, where the AVU metadata always associates with the canonical path.
 * For collections, AVU metadata can be different for the canonical and soft
 * linked path, and this class will use these paths as explicitly provided by
 * the caller of the metadata methods.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface CollectionAO extends FileCatalogObjectAO {

	/**
	 * For a given absolute path, get an <code>IRODSFileImpl</code> that is a
	 * collection.
	 * <p/>
	 * Note that a soft-linked path will behave normally.
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
	 * Given a set of metadata query parameters, return a list of IRODS
	 * Collections that match the metadata query.
	 * <p/>
	 * This version of the method allows specification of a case-insensitive AVU
	 * query
	 * 
	 * @param avuQueryElements
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @param caseInsensitive
	 *            <code>boolean</code> that will cause the AVU query to be
	 *            case-insensitive
	 * @return <code>List</code> of org.irods.jargon.core.pub.domain.Collection}
	 *         with domain objects that satisfy the query.
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<Collection> findDomainByMetadataQuery(
			List<AVUQueryElement> avuQueryElements, int partialStartIndex,
			boolean caseInsensitive) throws JargonQueryException,
			JargonException;

	/**
	 * Get a set of AVU metadata, and the iRODS objects to which the metadata is
	 * attached, based on a metadata query
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @param offset
	 *            <code>int</code> with a paging offset
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			List<AVUQueryElement> avuQuery, int offset)
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
	 * Get a summary list of collections and data objects and AVU metadata based
	 * on a meta-data query
	 * <p/>
	 * 
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @param caseInsensitive
	 *            <code>boolean</code> that, when <code>true</code> will do case
	 *            insensitive avu queries
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			List<AVUQueryElement> avuQuery, boolean caseInsensitive)
			throws JargonQueryException, JargonException;

	/**
	 * Get a list of collections and associated metadata that match a given AVU
	 * query. This version allows both an offset and specification of case
	 * insensitivity
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElements} with the
	 *            query specification
	 * @param offset
	 *            <code>int</code> with a paging offset
	 * @param caseInsensitive
	 *            <code>boolean</code> of <code>true</code> which allows
	 *            case-insensitive AVU queries
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			List<AVUQueryElement> avuQuery, int offset, boolean caseInsensitive)
			throws JargonQueryException, JargonException;

	/**
	 * Add AVU metadata for this collection. *
	 * <p/>
	 * Note that, in the case of a soft-linked path, the metadata is associated
	 * with that path, and is separate from metadata associated with the
	 * canonical file path
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws FileNotFoundException
	 *             when data object is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5)
	 *             is inconsistent, where a duplicate will only be detected if
	 *             units are not blank
	 */
	void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws FileNotFoundException, DuplicateDataException,
			JargonException;

	/**
	 * Remove AVU metadata from this collection.
	 * <p/>
	 * Note that, in the case of a soft-linked path, the metadata is associated
	 * with that path, and is separate from metadata associated with the
	 * canonical file path
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws FileNotFoundException
	 *             if the target iRODS collection is missing
	 * @throws JargonException
	 */
	void deleteAVUMetadata(final String absolutePath, final AvuData avuData)
			throws FileNotFoundException, JargonException;

	/**
	 * List the AVU metadata for a particular collection, as well as information
	 * identifying the Collection associated with that metadata, based on a
	 * metadata query.
	 * <p/>
	 * Note that this method will work across zones, so that if the given
	 * collection path is in a federated zone, the query will be made against
	 * that zone.
	 * <p/>
	 * Note that for soft links, metadata is associated with the given path, so
	 * a soft link and a canonical path may each have different AVU metadata.
	 * This method takes the path as given and finds that metadata.
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
	 * List the AVU metadata for a particular collection, as well as information
	 * identifying the Collection associated with that metadata, based on a
	 * metadata query.
	 * <p/>
	 * Note that this method will work across zones, so that if the given
	 * collection path is in a federated zone, the query will be made against
	 * that zone.
	 * <p/>
	 * Note that for soft links, metadata is associated with the given path, so
	 * a soft link and a canonical path may each have different AVU metadata.
	 * This method takes the path as given and finds that metadata.
	 * <p/>
	 * This method allows request for case-insensitive AVU queries
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @param <code>String with the absolute path of the collection of interest.  If this path
	 * is left blank, then the query will not add absolute path to the 'where' clause.
	 * @param <code>boolean</code> indicates that this is a case-insensitive
	 *        query.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws FileNotFoundExcepton
	 *             if the collection cannot be found
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQueryForCollection(
			List<AVUQueryElement> avuQuery, String collectionAbsolutePath,
			int partialStartIndex, boolean caseInsensitive)
			throws FileNotFoundException, JargonQueryException, JargonException;

	/**
	 * Get a list of the metadata values for the given collection absolute path.
	 * This method allows paging of results through a partial start index.
	 * <p/>
	 * Note that this method will work across zones, so that if the given
	 * collection path is in a federated zone, the query will be made against
	 * that zone.
	 * <p/>
	 * Note that for soft links, metadata is associated with the given path, so
	 * a soft link and a canonical path may each have different AVU metadata.
	 * This method takes the path as given and finds that metadata.
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
	 * @throws FileNotFoundException
	 *             if the collection could not be found
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	List<MetaDataAndDomainData> findMetadataValuesForCollection(
			final String collectionAbsolutePath, final int partialStartIndex)
			throws FileNotFoundException, JargonException, JargonQueryException;

	/**
	 * Get a list of the metadata values for the given collection absolute path.
	 * <p/>
	 * Note that for soft links, metadata is associated with the given path, so
	 * a soft link and a canonical path may each have different AVU metadata.
	 * This method takes the path as given and finds that metadata.
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
			final String collectionAbsolutePath) throws JargonException,
			JargonQueryException;

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
	 * For the given absolute path, return the given collection.
	 * <p/>
	 * Note that this method will work correctly with soft linked collection
	 * names
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the collection
	 * @return {@link org.irods.jargon.core.pub.domain.Collection} or null if no
	 *         collection found
	 * @throws DataNotFoundException
	 *             if collection does not exist
	 * @throws JargonException
	 */
	Collection findByAbsolutePath(final String irodsCollectionAbsolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * List the AVU metadata for a particular collection, as well as information
	 * about the collection itself, based on a metadata query. *
	 * <p/>
	 * Note that, in the case of a soft-linked path, the metadata is associated
	 * with that path, and is separate from metadata associated with the
	 * canonical file path
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
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection to count
	 * @return <code>int</code> with the total count of files, recursively
	 *         counted.
	 * @throws FileNotFoundException
	 *             if the collection absolute path is not found in iRODS
	 * @throws JargonException
	 */
	int countAllFilesUnderneathTheGivenCollection(
			final String irodsCollectionAbsolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * For a given iRODS collection, set the access permission to read. This can
	 * optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionRead(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to write. This
	 * can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionWrite(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to own. This can
	 * optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionOwn(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, remove access permissions for a given user.
	 * This can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void removeAccessPermissionForUser(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set he default to inherit access
	 * permissions
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionInherit(String zone, String absolutePath,
			boolean recursive) throws JargonException;

	/**
	 * Check the given collection (by absolute path) to see if the inheritance
	 * flag is set. This indicates that access permissions are inherited by
	 * children of the collection.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @return
	 * @throws FileNotFoundException
	 *             if the collection does not exist
	 * @throws JargonException
	 */
	boolean isCollectionSetForPermissionInheritance(String absolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * For a given iRODS collection, set he default to not inherit access
	 * permissions
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionToNotInherit(String zone, String absolutePath,
			boolean recursive) throws JargonException;

	/**
	 * Get the file permission value for the given absolute path for the given
	 * user.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @return {@link FilePermissionEnum} value with the permission level for
	 *         the given user.
	 * @throws FileNotFoundException
	 *             if the collection path is not found
	 * @throws JargonException
	 */
	FilePermissionEnum getPermissionForCollection(String irodsAbsolutePath,
			String userName, String zone) throws FileNotFoundException,
			JargonException;

	/**
	 * Get a list of all permissions for all users on the given collection
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection for which permissions will be retrieved.
	 * @return <code>List</code> of {@link FilePermissionEnum}
	 * @throws FileNotFoundException
	 *             if file is not located
	 * @throws JargonException
	 */
	List<UserFilePermission> listPermissionsForCollection(
			String irodsCollectionAbsolutePath) throws FileNotFoundException,
			JargonException;

	/**
	 * Overwrite AVU metadata for this collection *
	 * <p/>
	 * Note that, in the case of a soft-linked path, the metadata is associated
	 * with that path, and is separate from metadata associated with the
	 * canonical file path
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the current avu. This will be looked up by attribute + value
	 * @param newAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the desired state of the avu
	 * @throws JargonException
	 * @throws DataNotFoundExeption
	 *             if the AVU is not present
	 */
	void modifyAVUMetadata(String absolutePath, AvuData currentAvuData,
			AvuData newAvuData) throws DataNotFoundException, JargonException;

	/**
	 * This is a special method to modify the Avu value for a given attribute
	 * name and unit. Often, it is the case that applications want to keep
	 * unique values for a collection, and be able to easily change the value
	 * while preserving the attribute name and units. This method allows the
	 * specification of an AVU with the known name and units, and an arbitrary
	 * value. The method will find the unique attribute by name and unit, and
	 * overwrite the existing value with the value given in the
	 * <code>AvuData</code> parameter. *
	 * <p/>
	 * Note that, in the case of a soft-linked path, the metadata is associated
	 * with that path, and is separate from metadata associated with the
	 * canonical file path
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param currentAvuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} describing
	 *            the existing Avu name and unit, with the desired new value
	 * @throws DataNotFoundException
	 *             if the AVU data is not present
	 * @throws JargonException
	 */
	void modifyAvuValueBasedOnGivenAttributeAndUnit(String absolutePath,
			AvuData avuData) throws DataNotFoundException, JargonException;

	/**
	 * Retrieve the permission for the given user for the given collection. Note
	 * that the method will return null if no ACL currently exists.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the target
	 *            collection
	 * @param userName
	 *            - <code>String</code> with the name of the user
	 * @return {@link UserFilePermission} or <code>null</code> if no permission
	 *         is found
	 * @throws JargonException
	 */
	UserFilePermission getPermissionForUserName(
			String irodsCollectionAbsolutePath, String userName)
			throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to read as an
	 * administrator. This can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 *            <p/>
	 *            This method is equivalent to runnign the ichmod icommand with
	 *            the -M flag.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionReadAsAdmin(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to write as an
	 * administrator. This can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 *            <p/>
	 *            This method is equivalent to runnign the ichmod icommand with
	 *            the -M flag.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionWriteAsAdmin(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set the access permission to own as an
	 * administrator. This can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 *            <p/>
	 *            This method is equivalent to runnign the ichmod icommand with
	 *            the -M flag.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionOwnAsAdmin(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection,remove the access permission for the user as
	 * an administrator. This can optionally be recursively applied.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 *            <p/>
	 *            This method is equivalent to runnign the ichmod icommand with
	 *            the -M flag.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param userName
	 *            <code>String</code> with the user name whose permissions will
	 *            be set.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void removeAccessPermissionForUserAsAdmin(String zone, String absolutePath,
			String userName, boolean recursive) throws JargonException;

	/**
	 * Given an <code>ObjStat</code> object, return a <code>Collection</code>
	 * object representing the collection data in the iCAT
	 * 
	 * @param objStat
	 *            {@link ObjStat} for the given collection
	 * @return {@link Collection} representing the collection in iRODS
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	Collection findGivenObjStat(ObjStat objStat) throws DataNotFoundException,
			JargonException;

	/**
	 * Method to set access permission to the desired state, this variant makes
	 * it less necessary to stack 'if' tests in permission setting code.
	 * <p/>
	 * Note that <code>FilePermissionEnum</code> has more permission states
	 * defined then are currently supported by this method. This may require
	 * more iRODS core server updates to make this range of permissions
	 * meaningful.
	 * <p/>
	 * For the current variant of this method, only READ, WRITE, and OWN are
	 * supported, Other permission values will cause a
	 * <code>JargonException</code>. This may be relaxed in the future. Also
	 * note that NONE is supported, and actually causes the access permission to
	 * be removed.
	 * 
	 * @param zone
	 *            <code>String</code> with the zone for the user. This method
	 *            will work cross-zone if appropriate permissions are in place
	 * @param absolutePath
	 *            <code>String</code> with the absolute path for the collection
	 * @param userName
	 *            <code>userName</code> (just the name, no name#zone format) for
	 *            the user
	 * @param recursive
	 *            <code>boolean</code> to indicate that the permission must be
	 *            recursively applied to subdirectories
	 * @param filePermission
	 *            {@link FilePermissionEnum}
	 * @throws JargonException
	 */
	void setAccessPermission(String zone, String absolutePath, String userName,
			boolean recursive, FilePermissionEnum filePermission)
			throws JargonException;

	/**
	 * Find the iRODS <code>Collection</code> with the given primary key in the
	 * ICAT
	 * 
	 * @param id
	 *            <code>int</code> with the iRODS primary key
	 * @return {@link Collection} with the given primary key
	 * @throws DataNotFoundException
	 *             if the collection is not found
	 * @throws JargonException
	 */
	Collection findById(int id) throws DataNotFoundException, JargonException;

	/**
	 * Replicate the given collection asynchronously (via delayed exec rule).
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path for the collection
	 * @param resourceName
	 *            <code>String</code> with the resource to which the data will
	 *            be replicated
	 * @param delayInMinutes
	 *            <code>int</code> with the number of minutes to delay the
	 *            execution
	 * @throws JargonException
	 */
	void replicateCollectionAsynchronously(String irodsCollectionAbsolutePath,
			String resourceName, int delayInMinutes) throws JargonException;

	/**
	 * Convenience method to add a set of AVU metadata. This operation is
	 * tolerant of individual duplicate AVUs, and will trap those exceptions and
	 * not throw them. <br/>
	 * This method will return a collection of individual success or failure for
	 * each AVU.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path for the collection
	 * @param avuData
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} with the AVU
	 *            values to be added to the collection
	 * @return <code>List</code> of {@link BulkAVUOperationResponse}
	 * 
	 * @throws JargonException
	 *             if an unexpected exception not anticipated by the bulk AVU
	 *             process occurs
	 */
	List<BulkAVUOperationResponse> addBulkAVUMetadataToCollection(
			String absolutePath, List<AvuData> avuData)
			throws FileNotFoundException, JargonException;

	/**
	 * Convenience method to delete a set of AVU metadata. This operation is
	 * tolerant of individual non-existent AVUs, and will trap those exceptions
	 * and not throw them. <br/>
	 * This method will return a collection of individual success or failure for
	 * each AVU.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path for the collection
	 * @param avuData
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.domain.AvuData} with the AVU
	 *            values to be deleted from the collection
	 * @return <code>List</code> of {@link BulkAVUOperationResponse}
	 * @throws JargonException
	 *             if an unexpected exception not anticipated by the bulk AVU
	 *             process occurs
	 */
	List<BulkAVUOperationResponse> deleteBulkAVUMetadataFromCollection(
			String absolutePath, List<AvuData> avuData) throws JargonException;

	/**
	 * Do a buld delete of all AVUs associated with the collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path for the collection
	 * @throws DataNotFoundException
	 *             if the collection is missing
	 * @throws JargonException
	 */
	void deleteAllAVUMetadata(String absolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * Query for a specific AVU associated with the collection based on the
	 * metadata id
	 * 
	 * @param collectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection
	 * @param id
	 *            <code>int</code> with the id of the avu (unique database key)
	 * @return {@link MetaDataAndDomainData} at the given id
	 * @throws FileNotFoundException
	 *             if the collection is not found
	 * @throws DataNotFoundException
	 *             if the avu is not found
	 * @throws JargonException
	 */
	MetaDataAndDomainData findMetadataValueForCollectionByMetadataId(
			String collectionAbsolutePath, int id)
			throws FileNotFoundException, DataNotFoundException,
			JargonException;

	/**
	 * Query for a specific AVU associated with the collection based on the
	 * metadata id and a provided objStat
	 * 
	 * @param objStat
	 *            {@link ObjStat} previously obtained objStat of a collection
	 * @param id
	 *            <code>int</code> with the id of the avu (unique database key)
	 * @return {@link MetaDataAndDomainData} at the given id
	 * @throws DataNotFoundException
	 *             if the avu is not found
	 * @throws JargonException
	 */
	MetaDataAndDomainData findMetadataValueForCollectionById(ObjStat objStat,
			int id) throws DataNotFoundException, JargonException;

	/**
	 * For a given iRODS collection, set he default to not inherit access
	 * permissions. This version will do the operation in admin mode.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionToNotInheritInAdminMode(String zone,
			String absolutePath, boolean recursive) throws JargonException;

	/**
	 * For a given iRODS collection, set he default to inherit access
	 * permissions, using admin mode.
	 * <p/>
	 * Note that this method will work if a soft-linked collection name is
	 * supplied. Permissions are always associated with the canonical path name,
	 * and a soft linked collection will have the same permissions as the
	 * canonical collection
	 * 
	 * @param zone
	 *            <code>String</code> with an optional zone for the file. Leave
	 *            blank if not used, it is not required.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection.
	 * @param recursive
	 *            <code>boolean</code> that indicates whether the permission
	 *            should be applied recursively
	 * @throws JargonException
	 */
	void setAccessPermissionInheritAsAdmin(String zone, String absolutePath,
			boolean recursive) throws JargonException;

}