package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;

/**
 * This interface describes an access object that assists in the searching and
 * listing of collections and data objects, suitable for developing tree
 * depictions of an iRODS file system, and searching that file system on file
 * and collection names.
 * <p/>
 * This access object is being designed to support basic interface functionality
 * for both Swing and web GUI tree models, and basic search boxes for file or
 * collection names. More advanced searching based on metadata or other criteria
 * are available elsewhere in the API.
 * <p/>
 * <b>NOTE:</b> Within iRODS, Collections (directories) and Data Objects (files)
 * are different parts of the iCAT. For this reason, the listings are generated
 * separately, with the convention of collections first, and data objects
 * second. In the various listing methods, you will see methods for combining
 * queries on collections and data objects (such as listing the contents of a
 * parent directory). This separation of iCAT types makes life a little
 * complicated, in that these combined listing operations may end up with
 * collections and data objects with different paging requirements (e.g. There
 * are more collections to page, but no more data objects. This must be
 * accounted for by client programs. The
 * {@link CollectionAndDataObjectListingEntry} extends the
 * {@link IRODSDomainObject} superclass, this superclass provides methods to
 * access whether there are more entries, and at what sequence in a result
 * collection the 'has more' entry occurs, so that offset is available for
 * re-query.
 * <p/>
 * Note that this access object supports iRODS special collections ,note the
 * comments on individual methods for details. The Information returned is meant
 * to reflect the object or collection at the requested path, so if it is
 * soft-linked, the object is returned from the perspective of the soft link,
 * and the descriptive object will contain information on the canonical parent
 * collection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface CollectionAndDataObjectListAndSearchAO extends
		IRODSAccessObject {

	/**
	 * Specific query for coll listing with ACLs
	 */
	public static final String SHOW_COLL_ACLS = "ilsLACollections";

	public static final String SHOW_DATA_OBJ_ACLS = "ilsLADataObjects";

	/**
	 * This is a method that can support listing and paging of collections,
	 * suitable for creating interfaces that need to handle paging of large
	 * collections. Note that this method returns a simple value object that
	 * contains information about paging for each object. Clients of this method
	 * can inspect the returned results to determine the position of each result
	 * and whether there are more records to display.
	 * <p/>
	 * This method is not recursive, it only lists the collections under the
	 * given parent. The parent is an absolute path, this particular method does
	 * not 'search', rather it just lists.
	 * <p/>
	 * Soft links are supported with this method. The listing entry returned
	 * will indicate the request parent collection of a given file or data
	 * object, and internally will hold the canonical directory that is the
	 * parent, and reflect that it is a special collection.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset from which to start returning
	 *            results.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException
	 *             if the absolutePathToParent does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listCollectionsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException;

	/**
	 * This is a method that can support listing and paging of files in a
	 * collection, suitable for creating interfaces that need to handle paging
	 * of large collections. Note that this method returns a simple value object
	 * that contains information about paging for each object. Clients of this
	 * method can inspect the returned results to determine the position of each
	 * result and whether there are more records to display.
	 * <p/>
	 * This method is not a search method, it simply lists the directories that
	 * are direct children of the given path.
	 * <p/>
	 * Soft links are supported with this method. The listing entry returned
	 * will indicate the request parent collection of a given file or data
	 * object, and internally will hold the canonical directory that is the
	 * parent, and reflect that it is a special collection.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset from which to start returning
	 *            results.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws FileNotFoundException
	 *             if the absolutePathToParent does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPath(
			final String absolutePathToParent, final int partialStartIndex)
			throws FileNotFoundException, JargonException;

	/**
	 * This method is in support of applications and interfaces that need to
	 * support listing and paging of collections. This method returns a simple
	 * value object that contains information about paging for each object, such
	 * as record count, and whether this is the last record. This method will
	 * list objects that are direct children underneath the given parent.
	 * <p/>
	 * Note that this collection is composed of a collection of objects for
	 * child collections, and a collection of objects for child data objects
	 * (subdirectories versus files). There are separate counts and
	 * 'isLastEntry' values for each type, discriminated by the
	 * <code>CollectionAndDataObjectListingEntry.objectType</code>. In usage,
	 * this method would be called for the parent directory under which the
	 * subdirectories and files should be listed. The response will include the
	 * sum of both files and subdirectories, and each type may have more
	 * results. Once this result is returned, the
	 * <code>listDataObjectsUnderPath</code> and
	 * <code>listCollectionsUnderPath</code> methods may be called separately
	 * with a partial start index value as appropriate. It is up to the caller
	 * to determine which types need paging.
	 * <p/>
	 * Soft links are supported with this method. The listing entry returned
	 * will indicate the request parent collection of a given file or data
	 * object, and internally will hold the canonical directory that is the
	 * parent, and reflect that it is a special collection.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing both files and collections
	 * @throws FileNotFoundException
	 *             if the given path does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException;

	/**
	 * This method is in support of applications and interfaces that need to
	 * support listing and paging of collections. This method returns a simple
	 * value object that contains information about paging for each object, such
	 * as record count, and whether this is the last record. This method will
	 * list objects that are direct children underneath the given parent.
	 * <p/>
	 * Note that this collection is composed of a collection of objects for
	 * child collections, and a collection of objects for child data objects
	 * (subdirectories versus files). There are separate counts and
	 * 'isLastEntry' values for each type, discriminated by the
	 * <code>CollectionAndDataObjectListingEntry.objectType</code>. In usage,
	 * this method would be called for the parent directory under which the
	 * subdirectories and files should be listed. The response will include the
	 * sum of both files and subdirectories, and each type may have more
	 * results. Once this result is returned, the
	 * <code>listDataObjectsUnderPath</code> and
	 * <code>listCollectionsUnderPath</code> methods may be called separately
	 * with a partial start index value as appropriate. It is up to the caller
	 * to determine which types need paging.
	 * <p/>
	 * Soft links are supported with this method. The listing entry returned
	 * will indicate the request parent collection of a given file or data
	 * object, and internally will hold the canonical directory that is the
	 * parent, and reflect that it is a special collection.
	 * <p/>
	 * This variant of the files and collections listing entries wraps the
	 * resulting listing in a <code>PagingAwareCollectionListing</code> that
	 * contains information about the paging status of the separate collection
	 * and data object listings.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @return {@link PagingAwareCollectionListing} that contains both
	 *         collections and data objects in a mixed partial listing, along
	 *         with hints on the state of the listing. This metadata can be used
	 *         to compute a paging strategy for subsequent data.
	 * @throws FileNotFoundException
	 *             if the given path does not exist
	 * @throws JargonException
	 */
	PagingAwareCollectionListing listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(
			String absolutePathToParent) throws FileNotFoundException,
			JargonException;

	/**
	 * This method is in support of applications and interfaces that need to
	 * support listing and paging of collections. This method returns a simple
	 * count of the children (data objects and collections) underneath this
	 * directory, and includes all children.
	 * <p/>
	 * Soft links are supported with this method. The listing entry returned
	 * will indicate the count by inspecting the canonical directory that is the
	 * parent.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. The
	 *            parent must be a collection or an error is thrown
	 * @return <code>int</code> with a count of the files that are children of
	 *         the parent.
	 * @throws FileNotFoundException
	 *             if the given absolutePathToParent does not exist
	 * @throws JargonException
	 */
	int countDataObjectsAndCollectionsUnderPath(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException;

	/**
	 * Provides a search capability to search for any collections that have a
	 * match on the search term. The typical case would be a search box on a
	 * form to find all collections that have the given string.
	 * <p/>
	 * Note that this will do a genquery like:
	 * 
	 * <pre>
	 * COL_COLL_NAME like '%thepathyougiveforsearch%'
	 * </pre>
	 * 
	 * @param searchTerm
	 *            <code>String</code> that is the path search term, note that
	 *            the "%" is added in the method and should not be provided as a
	 *            parameter.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing collections that match the search term * @throws
	 *         JargonException
	 */
	List<CollectionAndDataObjectListingEntry> searchCollectionsBasedOnName(
			String searchTerm) throws JargonException;

	/**
	 * Provides a search capability to search for any collections that have a
	 * match on the search term. The typical case would be a search box on a
	 * form to find all collections that have the given string.
	 * <p/>
	 * Note that this will do a genquery like:
	 * <p/>
	 * 
	 * <pre>
	 * COL_COLL_NAME like '%thepathyougiveforsearch%'
	 * </pre>
	 * 
	 * @param searchTerm
	 *            <code>String</code> that is the path search term, note that
	 *            the "%" is added in the method and should not be provided as a
	 *            parameter.
	 * @param <code>int</code> with a partial start index of 0 or greater that
	 *        indicates the offset into the returned results, suitable for
	 *        paging.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing collections that match the search term * @throws
	 *         JargonException
	 */
	List<CollectionAndDataObjectListingEntry> searchCollectionsBasedOnName(
			String searchTerm, int partialStartIndex) throws JargonException;

	/**
	 * Provides a search capability to search for any data objects that have a
	 * match on the given search term. The typical case would be a search box on
	 * a form to find all data objects that have the given string in the name.
	 * <p/>
	 * Note that this will do a genquery like:
	 * <p/>
	 * 
	 * <pre>
	 * WHERE DATA_NAME LIKE '%searchTerm%'
	 * </pre>
	 * 
	 * @param searchTerm
	 *            <code>String</code> that is the path search term, note that
	 *            the "%" is added in the method and should not be provided as a
	 *            parameter.
	 * @param partialStartIndex
	 *            <code>int</code> with an offset into the results.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing data objects that match the search term
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> searchDataObjectsBasedOnName(
			String searchTerm, int partialStartIndex) throws JargonException;

	/**
	 * Provides a search capability to search for any data objects that have a
	 * match on the given search term. The typical case would be a search box on
	 * a form to find all data objects that have the given string in the name.
	 * <p/>
	 * Note that this will do a genquery like:
	 * <p/>
	 * 
	 * <pre>
	 * WHERE DATA_NAME LIKE '%searchTerm%'
	 * </pre>
	 * 
	 * @param searchTerm
	 *            <code>String</code> that is the path search term, note that
	 *            the "%" is added in the method and should not be provided as a
	 *            parameter.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing data objects that match the search term
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> searchDataObjectsBasedOnName(
			String searchTerm) throws JargonException;

	/**
	 * Provides a search capability to search for any collections and data
	 * objects that have a match on the given search term. The typical case
	 * would be a search box on a form to find all data objects that have the
	 * given string in the name. This method creates the union of a search on
	 * both data objects and collections. Note that for data objects and
	 * collections, there might separately be further paging available. In
	 * typical usage, this method can be called. The result collections can be
	 * inspected using the methods defined in
	 * {@link org.irods.jargon.core.pub.domain.IRODSDomainObject} to see if more
	 * results are available.
	 * <p/>
	 * Note that this will do a genquery like:
	 * <p/>
	 * 
	 * <pre>
	 * WHERE DATA_NAME LIKE '%searchTerm%'
	 * </pre>
	 * 
	 * <p/>
	 * Note that this method will compensate if strict ACL's are in place
	 * 
	 * @param searchTerm
	 *            <code>String</code> that is the path search term, note that
	 *            the "%" is added in the method and should not be provided as a
	 *            parameter.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing data objects that match the search term
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> searchCollectionsAndDataObjectsBasedOnName(
			String searchTerm) throws JargonException;

	/**
	 * Handy method will get the full domain object, {@link DataObject} or
	 * {@link Collection}, based on the given absolute path. This can be handy
	 * for display in interfaces or other applications that are concerned with
	 * retrieving 'info' about a given path.
	 * 
	 * @param objectAbsolutePath
	 *            <code>String</code> with the absolute path to the given data
	 *            object or collection.
	 * @return <code>Object</code> that will be either a <code>DataObject</code>
	 *         or <code>Collection</code> object based on the object at the
	 *         given absolute path in iRODS.
	 * @throws FileNotFoundException
	 *             , if no data object or collection found for the given path.
	 *             The method does not return null in this case
	 * @throws FileNotFoundException
	 *             if the given objectAbsolutePath does not exist
	 * @throws JargonException
	 */
	Object getFullObjectForType(String objectAbsolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * Retrieve a list of collections (not data objects) underneath a given
	 * parent path, with the user ACL permissions displayed. This is equivalent
	 * to the ls -la results. The returned
	 * <code>CollectionAndDataObjectListingEntry</code> objects will have a
	 * collection of <code>UserFilePermission</code> objects that detail the
	 * permissions.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset from which to start returning
	 *            results.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         including file permissions
	 * @throws FileNotFoundException
	 *             if the given absolutePathToParent does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listCollectionsUnderPathWithPermissions(
			String absolutePathToParent, int partialStartIndex)
			throws FileNotFoundException, JargonException;

	/**
	 * This is a method that can support listing and paging of data objects in a
	 * collection, including ACL information. This is suitable for creating
	 * interfaces that need to handle paging of large collections. Note that
	 * this method returns a simple value object that contains information about
	 * paging for each object. Clients of this method can inspect the returned
	 * results to determine the position of each result and whether there are
	 * more records to display.
	 * <p/>
	 * This method is not a search method, it simply lists.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @param partialStartIndex
	 *            <code>int</code> with the offset from which to start returning
	 *            results.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         with included per-user ACL information
	 * @throws FileNotFound
	 *             exception if the given absolutePathToParent does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsUnderPathWithPermissions(
			String absolutePathToParent, int partialStartIndex)
			throws FileNotFoundException, JargonException;

	/**
	 * This method is in support of applications and interfaces that need to
	 * support listing and paging of collections. This method returns a simple
	 * value object that contains information about paging for each object, such
	 * as record count, and whether this is the last record. This method adds
	 * the user ACL information, which is derived from an extended query.
	 * <p/>
	 * Note that there is an issue with GenQuery that makes it impossible to
	 * derive user zone in this query. This is something that may be addressed
	 * by converting the GenQuery to 'specific SQL query' at a later time. If
	 * zone information is desired, it is recommended that the
	 * <code>listPermissionsForDataObject()</code> in {@link DataObjectAO} and
	 * <code>listPermissionsForCollection</code> in {@link CollectionAO} be
	 * consulted.
	 * <p/>
	 * This method is meant for listings, or building trees. As such, it does
	 * not show any information about replicas, rather, it groups the data by
	 * data object path for all replicas.
	 * <p/>
	 * Note that this collection is composed of a collection of objects for
	 * child collections, and a collection of objects for child data objects
	 * (subdirectories versus files). There are separate counts and
	 * 'isLastEntry' values for each type, discriminated by the
	 * <code>CollectionAndDataObjectListingEntry.objectType</code>. In usage,
	 * this method would be called for the parent directory under which the
	 * subdirectories and files should be listed. The response will include the
	 * sum of both files and subdirectories, and each type may have more
	 * results. Once this result is returned, the
	 * <code>listDataObjectsUnderPath</code> and
	 * <code>listCollectionsUnderPath</code> methods may be called separately
	 * with a partial start index value as appropriate. It is up to the caller
	 * to determine which types need paging.
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to the parent. If
	 *            blank, the root is used. If the path is really a file, the
	 *            method will list from the parent of the file.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing both files and collections
	 * @throws FileNotFoundException
	 *             if the given absolutePathToParent does not exist
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPathWithPermissions(
			String absolutePathToParent) throws FileNotFoundException,
			JargonException;

	/**
	 * Retrieve the <code>ObjStat</code> for a collection or data object at the
	 * given absolute path in iRODS. This is the result of a call to rsObjStat.
	 * Note that a <code>FileNotFoundException</code> results if the objStat
	 * cannot be determined. This can occur based on issues with ACL's.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection or data object.
	 * @return {@link ObjStat} with object data. Note that a
	 *         <code>FileNotFoundException<code> will occur if the objStat cannot
	 * be found
	 * @throws FileNotFoundException
	 *             if the file is not found
	 * @throws JargonException
	 */
	ObjStat retrieveObjectStatForPath(String irodsAbsolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * Return the <code>CollectionAndDataObjectListingEntry</code> that is
	 * associated with the given iRODS absolute path. This is equivalent to
	 * doing an 'objStat' on the given path, and in fact, this is how the data
	 * is retrieved from iRODS.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection or data object.
	 * @return {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 *         containing information on the given file or directory at the
	 *         given absolute path.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	CollectionAndDataObjectListingEntry getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(
			String absolutePath) throws FileNotFoundException, JargonException;

	/**
	 * Retrieve the <code>ObjStat</code> for a collection or data object at the
	 * given absolute path in iRODS. This is the result of a call to rsObjStat.
	 * Note that a <code>FileNotFoundException</code> results if the objStat
	 * cannot be determined. This can occur based on issues with ACL's.
	 * 
	 * @param parentPath
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection
	 * @param fileName
	 *            <code>String</code> with the data object name for which the
	 *            <code>ObjStat</code> will be returned.
	 * @return {@link ObjStat} with object data. Note that a
	 *         <code>FileNotFoundException<code> will occur if the objStat cannot
	 * be found
	 * @throws FileNotFoundException
	 *             if the file is not found
	 * @throws JargonException
	 */
	ObjStat retrieveObjectStatForPathAndDataObjectName(String parentPath,
			String fileName) throws FileNotFoundException, JargonException;

	/**
	 * Count the number of collections (directories) under the given path. This
	 * does not account for data objects
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection
	 * @return <code>int</code> with the count of collections under the given
	 *         path
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	int countCollectionsUnderPath(String absolutePathToParent)
			throws FileNotFoundException, JargonException;

	/**
	 * Count the number of data objects (files) under the given path. This does
	 * not account for collections
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection
	 * @return <code>int</code> with the count of data objects under the given
	 *         path
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	int countDataObjectsUnderPath(String absolutePathToParent)
			throws FileNotFoundException, JargonException;

	/**
	 * List data objects and collections found underneath the parent described
	 * in the given objStat
	 * 
	 * @param objStat
	 *            {@link ObjStat} that describes the file
	 * @return <code>List</code> of         {@CollectionAndDataObjectListingEntry
	 * 
	 * 
	 * 
	 * 
	 * 
	 * } that represents the
	 *         collections and data objects beneath the parent
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	List<CollectionAndDataObjectListingEntry> listDataObjectsAndCollectionsUnderPath(
			ObjStat objStat) throws FileNotFoundException, JargonException;

	/**
	 * Given a parent path, get a total of the data sizes underneath that path
	 * 
	 * @param absolutePathToParent
	 *            <code>String</code> with the path to the parent collection,
	 *            children data objects are totaled
	 * @return <code>long</code> with a total size
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	long totalDataObjectSizesUnderPath(String absolutePathToParent)
			throws FileNotFoundException, JargonException;

	/**
	 * Retrieve an <code>ObjStat</code> for a given path. This version of
	 * ObjStat will avoid some file not found exceptions when strict acls
	 * preclude obtaining an objStat. Path guessing will return fake
	 * <code>ObjStat</code> for root, zone, and /zone/home directories if asked,
	 * and if jargon properties allow this.
	 * <p/>
	 * Note that the returned <code>ObjStat</code> has an indicator if a
	 * 'stand-in' objStat was returned.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path.
	 * 
	 * @return {@link ObjStat} associated witha path
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws JargonException
	 */
	ObjStat retrieveObjectStatForPathWithHeuristicPathGuessing(
			final String irodsAbsolutePath) throws FileNotFoundException,
			JargonException;

}