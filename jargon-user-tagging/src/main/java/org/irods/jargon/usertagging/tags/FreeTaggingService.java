package org.irods.jargon.usertagging.tags;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.usertagging.domain.IRODSTagGrouping;
import org.irods.jargon.usertagging.domain.TagQuerySearchResult;

public interface FreeTaggingService {

	/**
	 * For a data object, return a set of free tags in the form of a
	 * space-delimited <code>String</code>.
	 * 
	 * @param dataObjectAbsolutePath
	 *            <code>String</code> with the absolute path to the data object
	 *            for which the tag string will be produced.
	 * 
	 *            The user in the account used to initialize the service is used
	 *            by default.
	 * 
	 * @return {@link org.irods.jargon.usertagging.domain.IRODSTagGrouping} with
	 *         the data object tags in free tag form.
	 * @throws JargonException
	 */
	IRODSTagGrouping getTagsForDataObjectInFreeTagForm(
			String dataObjectAbsolutePath) throws JargonException;

	/**
	 * For an iRODS collection, return a set of free tags in the form of a
	 * space-delimited <code>String</code>. The user in the account used to
	 * initialize the service is used by default.
	 * 
	 * @param collectionAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS
	 *            collection that has free tags associated.
	 * @return {@link org.irods.jargon.usertagging.domain.IRODSTagGrouping} with
	 *         the collection tags in free tag form.
	 * @throws JargonException
	 */
	IRODSTagGrouping getTagsForCollectionInFreeTagForm(
			String collectionAbsolutePath) throws JargonException;

	/**
	 * Given a string of free tags included in the <code>IRODSTagValue</code>,
	 * generate a delta between the desired and current set of tags on the iRODS
	 * domain object described in the <code>IRODSTagValue</code>, which
	 * describes the type of object being tagged. Note that this method
	 * validates that the user associated with the tags is the logged-in user
	 * depicted in the <code>IRODSAccount</code>, and will throw a
	 * JargonException if they are different.
	 * <p>
	 * In the future, additional signatures can be added that allow 'override'
	 * of the user, but this default behavior prevents tagging on behalf of an
	 * arbitrary user. This is not a severe issue, but sensible defaulting will
	 * help prevent confusion.
	 * 
	 * @param irodsTagGrouping
	 *            {@link org.irods.jargon.usertagging.IRODSTagGrouping} that
	 *            describes a set of free tags.
	 * @throws JargonException
	 *             for any errors in maintaining the tags, including attempting
	 *             to add tags for a user other than the logged-in user.
	 */
	void updateTags(final IRODSTagGrouping irodsTagGrouping)
			throws JargonException;

	/**
	 * Given a string of free tags, generate a query that will return
	 * collections and data objects that match the combination of tags. Note
	 * that these results are page-able, and the result objects contain
	 * information on the existence of more records.
	 * <p>
	 * There are methods in this class that allow paging of the individual data
	 * object and collection domain types that can be utilized to page the
	 * different types of results. Each result entry has a value that indicates
	 * the particular domain (collection and data object) the entry is for.
	 * 
	 * @param searchTags
	 *            <code>String</code> with free space-delimited tags. These tags
	 *            will be AND-d together.
	 * @return {@link org.irods.jargon.usertagging.domain.TagQuerySearchResult}
	 *         with the results of the query.
	 * @throws JargonException
	 */
	TagQuerySearchResult searchUsingFreeTagString(final String searchTags)
			throws JargonException;

	/**
	 * Shortcut method for updating tags on a data object or collection. This
	 * method can determine the type of file based on absolute path. Given a
	 * free tag String and a user, the proper updates to the underlying tags
	 * will be accomplished.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with absolute path to the iRODS data
	 *            object or collection
	 * @param userName
	 *            <code>String</code> with the user name to associate with the
	 *            tag. The method does not check whether this is the logged in
	 *            user, so this type of validation is the responsibility of the
	 *            calling client.
	 * @param tags
	 *            <code>String</code> which is the space-delimited free form tag
	 *            set
	 * @throws JargonException
	 */
	void updateTagsForUserForADataObjectOrCollection(String irodsAbsolutePath,
			String userName, String tags) throws JargonException;

}
