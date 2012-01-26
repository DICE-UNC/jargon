package org.irods.jargon.usertagging;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.usertagging.domain.IRODSTagGrouping;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.irods.jargon.usertagging.domain.TagQuerySearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object is a bridge between the typical user interface depiction of tags
 * as a free string of space-delimited tag values, and the underlying AVU
 * structures in iRODS.
 * <p/>
 * This service can compare a set of free tags to existing tag data for an iRODS
 * domain object and generate the appropriate add and delete AVU operations to
 * reflect the delta between existing and desired tags.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class FreeTaggingServiceImpl extends AbstractIRODSTaggingService
		implements FreeTaggingService {

	public static final Pattern PARSE_FREE_TAGS_PATTERN = Pattern
			.compile("[,\\s]+");
	public final char COMMA = ',';
	public final String AND_VALUE = " AND ";
	public final String EQUALS_QUOTE = " = '";
	public final char QUOTE = '\'';

	public static final Logger log = LoggerFactory
			.getLogger(FreeTaggingServiceImpl.class);

	private final IRODSTaggingService irodsTaggingService;

	/**
	 * Static initializer that initializes the service with access to objects
	 * that interact with iRODS.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 * @return <code>FreeTaggingService</code> implementation instance.
	 */
	public static FreeTaggingService instance(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		return new FreeTaggingServiceImpl(irodsAccessObjectFactory,
				irodsAccount, null);
	}

	/**
	 * Static initializer that allows a <code>IRODSTaggingService</code>
	 * implementation to be passed in at construction time. Otherwise a new,
	 * default tagging service is initialized.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 * @param irodsTaggingService
	 *            {@link org.irods.jargon.usertagging.IRODSTaggingService}
	 *            implementation that will handle CRUD operations on the
	 *            underlying tags as AVU's in iRODS.
	 * @return <code>FreeTaggingService</code> implementation instance.
	 * @throws JargonException
	 */
	public static FreeTaggingService instanceProvidingATagUpdateService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final IRODSTaggingService irodsTaggingService)
			throws JargonException {
		return new FreeTaggingServiceImpl(irodsAccessObjectFactory,
				irodsAccount, irodsTaggingService);
	}

	/**
	 * Private constructor that initializes the service with access to objects
	 * that interact with iRODS.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 * @param irodsTaggingService
	 *            {@link org.irods.jargon.usertagging.IRODSTaggingService}
	 *            implementation that will provide CRUD operations to iRODS
	 *            tags. This may be left as null, and a default service will be
	 *            initialized.
	 * @throws JargonException
	 */
	private FreeTaggingServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final IRODSTaggingService irodsTaggingService) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (irodsTaggingService == null) {
			this.irodsTaggingService = IRODSTaggingServiceImpl.instance(
					irodsAccessObjectFactory, irodsAccount);
		} else {
			this.irodsTaggingService = irodsTaggingService;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.usertagging.FreeTaggingService#
	 * updateTagsForUserForADataObjectOrCollection(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void updateTagsForUserForADataObjectOrCollection(
			final String irodsAbsolutePath, final String userName,
			final String tags) throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (tags == null) {
			throw new IllegalArgumentException("null tags");
		}

		log.info("updateTagsForUser, irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("userName:{}", userName);
		log.info("tags:{}", tags);

		// decide if file or collection

		IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile fileToTag = irodsFileFactory
				.instanceIRODSFile(irodsAbsolutePath);

		if (!fileToTag.exists()) {
			log.error("file to tag does not exist at absolute irods path:{}",
					irodsAbsolutePath);
			throw new JargonException("file to tag does not exist in irods");
		}

		boolean isDirectory = fileToTag.isDirectory();

		MetadataDomain metadataDomain;

		if (isDirectory) {
			metadataDomain = MetadataDomain.COLLECTION;
			log.debug("treating as a collection");
		} else {
			metadataDomain = MetadataDomain.DATA;
		}

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				metadataDomain, irodsAbsolutePath, tags, userName);
		updateTags(irodsTagGrouping);
		log.info("tags update");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.usertagging.FreeTaggingService#
	 * getTagsForDataObjectInFreeTagForm(java.lang.String)
	 */
	@Override
	public IRODSTagGrouping getTagsForDataObjectInFreeTagForm(
			final String dataObjectAbsolutePath) throws JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectAbsolutePath");
		}

		log.info("getTagsForDataObjectInFreeTagForm for:{}",
				dataObjectAbsolutePath);
		log.info("for user:{}", irodsAccount.getUserName());

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsOnDataObject(dataObjectAbsolutePath);

		StringBuilder sb = new StringBuilder();

		for (IRODSTagValue irodsTagValue : irodsTagValues) {
			sb.append(irodsTagValue.getTagData());
			sb.append(' ');
		}

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.DATA, dataObjectAbsolutePath, sb.toString()
						.trim(), irodsAccount.getUserName());
		log.debug("irodsTagGrouping:{}", irodsTagGrouping);

		return irodsTagGrouping;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.usertagging.FreeTaggingService#
	 * getTagsForCollectionInFreeTagForm(java.lang.String)
	 */
	@Override
	public IRODSTagGrouping getTagsForCollectionInFreeTagForm(
			final String collectionAbsolutePath) throws JargonException {

		if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty collectionAbsolutePath");
		}

		log.info("getTagsForCollectionInFreeTagForm for:{}",
				collectionAbsolutePath);
		log.info("for user:{}", irodsAccount.getUserName());

		List<IRODSTagValue> irodsTagValues = irodsTaggingService
				.getTagsOnCollection(collectionAbsolutePath);

		StringBuilder sb = new StringBuilder();

		for (IRODSTagValue irodsTagValue : irodsTagValues) {
			sb.append(irodsTagValue.getTagData());
			sb.append(' ');
		}

		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(
				MetadataDomain.COLLECTION, collectionAbsolutePath, sb
						.toString().trim(), irodsAccount.getUserName());
		log.debug("irodsTagGrouping:{}", irodsTagGrouping);
		return irodsTagGrouping;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.usertagging.FreeTaggingService#updateTags(org.irods.
	 * jargon.usertagging.domain.IRODSTagValue)
	 */
	@Override
	public void updateTags(final IRODSTagGrouping irodsTagGrouping)
			throws JargonException {
		log.info("updateTags() based on free tag input:{}", irodsTagGrouping);

		// only allow updates as logged in user
		if (!(irodsTagGrouping.getUserName().equals(irodsAccount.getUserName()))) {
			log.error(
					"attempting to update for user:{} not allowed, must be same as logged in user",
					irodsTagGrouping.getUserName());
			throw new JargonException(
					"attempt to update user tags using user name not equal to logged in user");
		}

		// will also process commas
		String[] userTags = extractIndividualTagsFromFreeTagString(irodsTagGrouping
				.getSpaceDelimitedTagsForDomain());
		// split desired tags up
		// String[] userTags =
		// irodsTagGrouping.getSpaceDelimitedTagsForDomain().split("\s*");

		// gather user tags
		List<IRODSTagValue> currentTags = irodsTaggingService
				.getTagsBasedOnMetadataDomain(
						irodsTagGrouping.getMetadataDomain(),
						irodsTagGrouping.getDomainUniqueName());
		log.debug("current user tags:{}", currentTags);

		/*
		 * loop through desired tags, use presence or absence in current tags as
		 * a delta and appropriately add/remove NOTE: this is not a perfect
		 * solution, as there is a possibility of multiple updates (no database
		 * locking is available). This is an issue that can be further addressed
		 * by pushing this code up to a microservice in later releases, and
		 * Jargon can only make a best-effort here.
		 */

		if (irodsTagGrouping.getSpaceDelimitedTagsForDomain().trim().isEmpty()) {
			log.info("I desire no tags, so delete any tags that exist");

			for (IRODSTagValue irodsTagValue : currentTags) {
				log.debug("deleting current tag:{}", currentTags);
				irodsTaggingService.removeTagFromGivenDomain(irodsTagValue,
						irodsTagGrouping.getMetadataDomain(),
						irodsTagGrouping.getDomainUniqueName());
			}
		} else {
			for (String desiredTag : userTags) {
				log.debug("processing desiredTag:{}", desiredTag);
				processSuppliedTagAgainstCurrentTags(desiredTag, currentTags,
						irodsTagGrouping);
			}
		}

		/*
		 * Loop thru current tags, if they are no longer desired (not in the
		 * free tag area), then remove them from iRODS
		 */

		for (IRODSTagValue currentTag : currentTags) {
			processCurrentTagAgainstDesiredTags(currentTag, userTags,
					irodsTagGrouping);
		}

		log.debug("updates complete");

	}

	/**
	 * @param irodsTagGrouping
	 * @return
	 */
	private String[] extractIndividualTagsFromFreeTagString(
			final String tagString) {

		String[] userTags = PARSE_FREE_TAGS_PATTERN.split(tagString);
		return userTags;
	}

	/**
	 * For a given tag in iRODS, see if it is in the free tagging area. If not,
	 * it is no longer desired and can be removed from iRODS.
	 * 
	 * @param currentTag
	 * @param userTags
	 * @param irodsTagGrouping
	 */
	private void processCurrentTagAgainstDesiredTags(
			final IRODSTagValue currentTag, final String[] userTags,
			final IRODSTagGrouping irodsTagGrouping) throws JargonException {

		log.info("looking to see if iRODS tag still desired:{}", currentTag);

		boolean isDesired = false;

		for (String userTag : userTags) {
			if (userTag.equals(currentTag.getTagData())) {
				isDesired = true;
				break;
			}
		}

		if (!isDesired) {
			log.info("removing tag from iRODS, no longer desired:{}",
					currentTag);
			irodsTaggingService.removeTagFromGivenDomain(currentTag,
					irodsTagGrouping.getMetadataDomain(),
					irodsTagGrouping.getDomainUniqueName());
		}

	}

	/**
	 * For a given tag specified in a free tagging area, see if it is currently
	 * in iRODS, and add if not.
	 * 
	 * @param desiredTag
	 * @param currentTags
	 * @param irodsTagGrouping
	 * @throws JargonException
	 */
	private void processSuppliedTagAgainstCurrentTags(final String desiredTag,
			final List<IRODSTagValue> currentTags,
			final IRODSTagGrouping irodsTagGrouping) throws JargonException {

		// process adds by comparing desired to current, add desired not in
		// current
		log.info("looking for desired not in current for adds");
		boolean inCurrentTags = false;
		IRODSTagValue irodsTagValue = null;

		for (int i = 0; i < currentTags.size(); i++) {
			irodsTagValue = currentTags.get(i);
			if (desiredTag.equals(irodsTagValue.getTagData())) {
				inCurrentTags = true;
				break;
			}
		}

		if (!inCurrentTags) {
			log.debug("desired tag not in current, will add: {}", desiredTag);
			irodsTagValue = new IRODSTagValue(desiredTag,
					irodsAccount.getUserName());
			irodsTaggingService.addTagToGivenDomain(irodsTagValue,
					irodsTagGrouping.getMetadataDomain(),
					irodsTagGrouping.getDomainUniqueName());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.usertagging.FreeTaggingService#searchUsingFreeTagString
	 * (java.lang.String)
	 */
	@Override
	public TagQuerySearchResult searchUsingFreeTagString(final String searchTags)
			throws JargonException {

		if (searchTags == null || searchTags.isEmpty()) {
			throw new IllegalArgumentException("null or empty searchTags");
		}

		log.info("searching on terms:{}", searchTags);

		String[] searchTagValues = extractIndividualTagsFromFreeTagString(searchTags);

		if (searchTagValues.length == 0) {
			throw new JargonException("no searchTags were found");
		}

		List<CollectionAndDataObjectListingEntry> resultEntries = new ArrayList<CollectionAndDataObjectListingEntry>();

		DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());

		// do data objects first

		StringBuilder wherePart = new StringBuilder();

		wherePart.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());
		wherePart.append(EQUALS_QUOTE);
		wherePart.append(UserTaggingConstants.TAG_AVU_UNIT);
		wherePart.append(QUOTE);

		for (String searchTag : searchTagValues) {
			log.debug("searchTag to add to query:{}", searchTag);
			wherePart.append(AND_VALUE);
			wherePart
					.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
			wherePart.append(EQUALS_QUOTE);
			wherePart.append(searchTag);
			wherePart.append(QUOTE);
		}

		log.debug("where part of data object query = {}", wherePart);

		List<DataObject> dataObjects = dataObjectAO.findWhere(wherePart
				.toString());

		log.info(
				"retrieved {} data objects based on query, converting to query result entries",
				dataObjects.size());

		CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = null;

		for (DataObject dataObject : dataObjects) {
			collectionAndDataObjectListingEntry = new CollectionAndDataObjectListingEntry();
			collectionAndDataObjectListingEntry.setCount(dataObject.getCount());
			collectionAndDataObjectListingEntry.setCreatedAt(dataObject
					.getCreatedAt());
			collectionAndDataObjectListingEntry.setDataSize(dataObject
					.getDataSize());
			collectionAndDataObjectListingEntry.setId(dataObject.getId());
			collectionAndDataObjectListingEntry.setLastResult(dataObject
					.isLastResult());
			collectionAndDataObjectListingEntry.setModifiedAt(dataObject
					.getUpdatedAt());
			collectionAndDataObjectListingEntry
					.setObjectType(ObjectType.DATA_OBJECT);
			collectionAndDataObjectListingEntry.setParentPath(dataObject
					.getCollectionName());
			collectionAndDataObjectListingEntry.setPathOrName(dataObject
					.getDataName());
			resultEntries.add(collectionAndDataObjectListingEntry);
		}

		// now find collections

		CollectionAO collectionAO = getIrodsAccessObjectFactory()
				.getCollectionAO(getIrodsAccount());

		wherePart = new StringBuilder();

		wherePart.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS.getName());
		wherePart.append(EQUALS_QUOTE);
		wherePart.append(UserTaggingConstants.TAG_AVU_UNIT);
		wherePart.append(QUOTE);

		for (String searchTag : searchTagValues) {
			log.debug("searchTag to add to query:{}", searchTag);
			wherePart.append(AND_VALUE);
			wherePart
					.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME.getName());
			wherePart.append(EQUALS_QUOTE);
			wherePart.append(searchTag);
			wherePart.append(QUOTE);
		}

		log.debug("where part of collection query = {}", wherePart);

		List<Collection> collections = collectionAO.findWhere(
				wherePart.toString(), 0);

		for (Collection collection : collections) {
			collectionAndDataObjectListingEntry = new CollectionAndDataObjectListingEntry();
			collectionAndDataObjectListingEntry.setCount(collection.getCount());
			collectionAndDataObjectListingEntry.setCreatedAt(collection
					.getCreatedAt());
			collectionAndDataObjectListingEntry.setDataSize(0);
			collectionAndDataObjectListingEntry.setId(collection
					.getCollectionId());
			collectionAndDataObjectListingEntry.setLastResult(collection
					.isLastResult());
			collectionAndDataObjectListingEntry.setModifiedAt(collection
					.getModifiedAt());
			collectionAndDataObjectListingEntry
					.setObjectType(ObjectType.COLLECTION);
			collectionAndDataObjectListingEntry.setParentPath(collection
					.getCollectionParentName());
			collectionAndDataObjectListingEntry.setPathOrName(collection
					.getCollectionName());
			resultEntries.add(collectionAndDataObjectListingEntry);
		}

		log.info(
				"retrieved {} collections based on query, converting to query result entries",
				collections.size());

		return TagQuerySearchResult.instance(searchTags, resultEntries);

	}

}
