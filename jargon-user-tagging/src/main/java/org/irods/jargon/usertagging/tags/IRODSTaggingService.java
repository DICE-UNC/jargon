package org.irods.jargon.usertagging.tags;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.usertagging.domain.IRODSTagValue;

/**
 * Service interface for processing IRODS free tags. This method provides
 * services on top of Jargon to maintain user-defined tags on iRODS domain
 * objects.
 *
 * Note that tags are by user. Various signatures within this service either
 * default to the logged-in user, or utilize the user passed in as part of the
 * method parameters. Please note carefully the comments for each method to
 * ensure that this is appropriately controlled. This service does not attempt
 * to do any edits of which user is updating which tag.
 *
 * The {@code FreeTaggingService} is appropriate for end-user interfaces, and
 * does ensure that tag query/maintenance operations are done as the logged-in
 * user. Generally, the caller of this lower level service is responsible for
 * allowing or preventing updates on behalf of other users.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface IRODSTaggingService {

	/**
	 * Add a tag to a data object in iRODS for the user.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 *            that should be tagged.
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be added,
	 *            including user name.
	 * @throws DataNotFoundException
	 *             if the target data object is not found
	 * @throws DuplicateDataException
	 *             if the tag already exists
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public void addTagToDataObject(final String dataObjectAbsolutePath, final IRODSTagValue irodsTagValue)
			throws DataNotFoundException, JargonException;

	/**
	 * Remove the given tag from the data object.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to the iRODS data object
	 *            that has the tag to be removed.
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be removed,
	 *            including user name.
	 * @throws JargonException
	 *             {@link JargonException}
	 * @throws DataNotFoundException
	 *             {@link DataNotFoundException}
	 * @throws DuplicateDataException
	 *             {@link DuplicateDataException}
	 */
	public void deleteTagFromDataObject(final String dataObjectAbsolutePath, final IRODSTagValue irodsTagValue)
			throws JargonException, DataNotFoundException, DuplicateDataException;

	/**
	 * For a given data object, retrieve the tag values for the logged in user.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the path to the data object for which the tags
	 *            will be retrieved.
	 * @return {@code List} of {@link IRODSTagValue} with the tag information for
	 *         the data object
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public List<IRODSTagValue> getTagsOnDataObject(String dataObjectAbsolutePath) throws JargonException;

	/**
	 * Add a tag to a collection in iRODS for the user. The user with the account in
	 * the tag value is used.
	 *
	 * @param collectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS collection that
	 *            should be tagged.
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be added.
	 * @throws DataNotFoundException
	 *             if the target data object is not found
	 * @throws DuplicateDataException
	 *             if the tag already exists
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public void addTagToCollection(String collectionAbsolutePath, IRODSTagValue irodsTagValue)
			throws JargonException, DataNotFoundException, DuplicateDataException;

	/**
	 * Retrieve the user tags associated with a given iRODS collection. This method
	 * will default to tags set by the user reflected in the {@code IRODSAccount}
	 * object associated with the user.
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the given iRODS
	 *            Collection.
	 * @return {@code List} of {@link IRODSTagValue} with the tag information for
	 *         the collection.
	 * @throws DataNotFoundException
	 *             if no collection found
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public List<IRODSTagValue> getTagsOnCollection(String irodsAbsolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * Delete a user tag from a given iRODS Collection.
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with iRODS absolute path.
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be deleted,
	 *            including the user name. * @throws DataNotFoundException if the
	 *            target data object is missing
	 *
	 * @throws JargonException
	 *             {@link JargonException}
	 * @throws DataNotFoundException
	 *             {@link DataNotFoundException}
	 */
	public void deleteTagFromCollection(String irodsAbsolutePath, IRODSTagValue irodsTagValue)
			throws DataNotFoundException, JargonException;

	/**
	 * List tags for the object identified by the given unique name (e.g. absolute
	 * path for a collection or data object), using the given {@code MetadataDomain}
	 * value to determine the type of object that was tagged. This method uses the
	 * logged-in user.
	 *
	 * @param metadataDomain
	 *            {@code MetaDataAndDomainData.MetadataDomain} enum value that
	 *            describes the iRODS domain being tagged.
	 * @param domainUniqueName
	 *            {@code String} with the unique name for the domain object, such as
	 *            absolute path for a collection or data object.
	 * @return {@code List} of {@link IRODSTagValue} with the tag information for
	 *         the iRODS domain object. * @throws DataNotFoundException if the
	 *         target data object is not found
	 * @throws JargonException
	 *             {@link JargonException} if error in iRODS or the domain type is
	 *             not supported.
	 */
	public List<IRODSTagValue> getTagsBasedOnMetadataDomain(MetadataDomain metadataDomain, String domainUniqueName)
			throws JargonException;

	/**
	 * Add a tag to the given domain object within iRODS, using the user supplied in
	 * the {@code IRODSTagValue}
	 *
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be added,
	 *            including the user.
	 * @param metadataDomain
	 *            {@code MetaDataAndDomainData.MetadataDomain} enum value that
	 *            describes the iRODS domain being tagged.
	 * @param domainUniqueName
	 *            {@code String} with the unique name for the domain object, such as
	 *            absolute path for a collection or data object.
	 * @throws DataNotFoundException
	 *             if the target data object is not found
	 * @throws DuplicateDataException
	 *             if the tag already exists
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public void addTagToGivenDomain(IRODSTagValue irodsTagValue, MetadataDomain metadataDomain, String domainUniqueName)
			throws JargonException, DuplicateDataException, DataNotFoundException;

	/**
	 * Remove the tag from the given domain, using the user supplied in the
	 * {@code IRODSTagValue}
	 *
	 * @param irodsTagValue
	 *            {@link IRODSTagValue} with the tag information to be removed,
	 *            including the user.
	 * @param metadataDomain
	 *            {@code MetaDataAndDomainData.MetadataDomain} enum value that
	 *            describes the iRODS domain being tagged.
	 * @param domainUniqueName
	 *            {@code String} with the unique name for the domain object, such as
	 *            absolute path for a collection or data object.
	 * @throws DataNotFoundException
	 *             if the target collection or data object is missing
	 * @throws JargonException
	 *             {@link JargonException} if any iRODS error, or if the tag domain
	 *             is not supported.
	 */
	public void removeTagFromGivenDomain(IRODSTagValue irodsTagValue, MetadataDomain metadataDomain,
			String domainUniqueName) throws DataNotFoundException, JargonException;

	/**
	 * Add a description for a data object, using the user supplied in the
	 * {@code IRODSTagValue}.
	 * <p>
	 * Note that, if the description is blank, an attempt will be made to delete any
	 * description information currently stored.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to a data object that will
	 *            have the specified description added for the user
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the text value of a description that
	 *            will be added to a data object for the specified user
	 * @throws DataNotFoundException
	 *             if the target data object is not found
	 * @throws JargonException
	 *             {@link JargonException}
	 *
	 */
	void addDescriptionToDataObject(String dataObjectAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws JargonException, DataNotFoundException;

	/**
	 * Remove a description from a data object, using the user supplied in the
	 * {@code IRODSTagValue}
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to a data object that will
	 *            have the specified description removed for the user
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the text value of a description that
	 *            will be removed from a data object for the specified user
	 * @throws DataNotFoundException
	 *             if the target data object is missing
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	void deleteDescriptionFromDataObject(String dataObjectAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws DataNotFoundException, JargonException;

	/**
	 * Remove a description from a collection, using the user supplied in the
	 * {@code IRODSTagValue}
	 *
	 * @param collectionAbsolutePath
	 *            {@code String} with the absolute path to a collection that will
	 *            have the specified description removed for the user
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the text value of a description that
	 *            will be removed from a collection for the specified user
	 * @throws DataNotFoundException
	 *             if the target data object is missing
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	void deleteDescriptionFromCollection(String collectionAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws DataNotFoundException, JargonException;

	/**
	 * Retrieve the description value for a data object, using the logged-in user.
	 * Note that this method returns null if no description is found.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to a data object that will
	 *            have the specified description removed for the user
	 * @return {@link IRODSTagValue}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	IRODSTagValue getDescriptionOnDataObjectForLoggedInUser(String dataObjectAbsolutePath) throws JargonException;

	/**
	 * Add a description to the collection using the user in the provided
	 * {@code IRODSTagValue}
	 * <p>
	 * Note that adding a blank description will delete any description AVU data in
	 * iRODS. Adding a description when one already exists will replace the previous
	 * value.
	 *
	 * @param collectionAbsolutePath
	 *            {@code String} with the absolute path to the iRODS collection
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the value of a description that will be
	 *            added to the collection
	 * @throws DataNotFoundException
	 *             if the target collection is not found
	 * @throws JargonException
	 *             {@link JargonException}
	 * @throws DuplicateDataException
	 *             {@link DuplicateDataException}
	 */
	void addDescriptionToCollection(String collectionAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws JargonException, DataNotFoundException, DuplicateDataException;

	/**
	 * Get the description for the collection as specified by the logged-in user.
	 * This method will return a {@code null} if no description is provided.
	 *
	 * @param collectionAbsolutePath
	 *            {@code String} with the absolute path to the collection
	 * @return {@link IRODSTagValue} containing the description for the given
	 *         collection, or {@code null} if no AVU data found
	 * @throws JargonException
	 *             {@link JargonException}
	 * @throws DataNotFoundException
	 *             if the collection is not found
	 */
	IRODSTagValue getDescriptionOnCollectionForLoggedInUser(String collectionAbsolutePath)
			throws DataNotFoundException, JargonException;

	/**
	 * Method wil return a description for the Collection or Data Object for the
	 * logged in user. This method will return {@code null} if no description was
	 * found.
	 *
	 * @param metadataDomain
	 *            {@code MetaDataAndDomainData.MetadataDomain} enum value that
	 *            describes the iRODS domain being tagged.
	 * @param domainUniqueName
	 *            {@code String} with the unique name for the domain object, such as
	 *            absolute path for a collection or data object.
	 * @return {@code List} of {@link IRODSTagValue} with the tag information for
	 *         the iRODS domain object.
	 * @throws DataNotFoundException
	 *             if the collection is not found
	 * @throws JargonException
	 *             {@link JargonException} if error in iRODS or the domain type is
	 *             not supported.
	 */
	IRODSTagValue getDescriptionBasedOnMetadataDomain(MetadataDomain metadataDomain, String domainUniqueName)
			throws DataNotFoundException, JargonException;

	/**
	 * Method that takes the currently stored description data and compares it to
	 * the desired data. Any necessary updates are handled by looking at the
	 * difference, e.g. it will delete if description removed, add if not currently
	 * present, etc.
	 *
	 * @param dataObjectAbsolutePath
	 *            {@code String} with the absolute path to a data object that will
	 *            have the specified description removed for the user
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the text value of a description that
	 *            will be removed from a data object for the logged-in user, or
	 *            {@code null} if no description is available
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	void checkAndUpdateDescriptionOnDataObject(String dataObjectAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws JargonException;

	/**
	 * Method that takes the currently stored description data and compares it to
	 * the desired data. Any necessary updates are handled by looking at the
	 * difference, e.g. it will delete if description removed, add if not currently
	 * present, etc.
	 *
	 * @param collectionAbsolutePath
	 *            {@code String} with the absolute path to a data object that will
	 *            have the specified description removed for the user
	 * @param irodsDescriptionValue
	 *            {@link IRODSTagValue} with the text value of a description that
	 *            will be removed from a data object for the logged-in user, or
	 *            {@code null} if no description is available
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	void checkAndUpdateDescriptionOnCollection(String collectionAbsolutePath, IRODSTagValue irodsDescriptionValue)
			throws JargonException;

}
