/**
 * 
 */
package org.irods.jargon.usertagging;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.usertagging.domain.IRODSTagValue;
import org.irods.jargon.usertagging.domain.TagCloudEntry;
import org.irods.jargon.usertagging.domain.UserTagCloudView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for query and processing of a user tag cloud.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class UserTagCloudServiceImpl extends AbstractIRODSTaggingService
		implements UserTagCloudService {

	public static final Logger log = LoggerFactory
			.getLogger(UserTagCloudServiceImpl.class);

	public static final String COMMA_SPACE = ", ";
	public static final String EQUAL_QUOTE = " = '";
	public static final String QUOTE_SPACE = "' ";
	public static final String AND = " AND ";
	public static final String QUOTE = "'";

	/**
	 * Static initializer used to create instances of the service.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 * @return instance of the <code>IRODSTaggingServiceImpl</code>
	 */
	public static UserTagCloudService instance(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		return new UserTagCloudServiceImpl(irodsAccessObjectFactory,
				irodsAccount);
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
	 */
	private UserTagCloudServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount)  {
		super(irodsAccessObjectFactory, irodsAccount);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.UserTagCloudService#getTagCloud()
	 */
	@Override
	public UserTagCloudView getTagCloud() throws JargonException {
		UserTagCloudView userTagCloudView = UserTagCloudView.instance(
				irodsAccount.getUserName(), buildTagCloudEntryListForDataObjects(""),
				buildTagCloudEntryListForCollections(""));
		return userTagCloudView;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.usertagging.UserTagCloudService#getTagCloudForDataObjects
	 * ()
	 */
	@Override
	public UserTagCloudView getTagCloudForDataObjects() throws JargonException {
		
        log.info("getTagCloudForDataObjects");
		UserTagCloudView userTagCloudView = UserTagCloudView.instance(
				irodsAccount.getUserName(), buildTagCloudEntryListForDataObjects(""),
				new ArrayList<TagCloudEntry>());
		return userTagCloudView;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.usertagging.UserTagCloudService#getTagCloudForCollections
	 * ()
	 */
	@Override
	public UserTagCloudView getTagCloudForCollections() throws JargonException {

		log.info("getTagCloudForCollections");
		List<TagCloudEntry> collectionTagCloudEntries = buildTagCloudEntryListForCollections("");
		UserTagCloudView userTagCloudView = UserTagCloudView.instance(
				irodsAccount.getUserName(), new ArrayList<TagCloudEntry>(),
				collectionTagCloudEntries);
		return userTagCloudView;

	}

	/**
	 * Shared method to build a tag cloud list for collections.  This will default to the user name in the given
	 * iRODS account
	 * @return
	 * @throws JargonException
	 */
	private List<TagCloudEntry> buildTagCloudEntryListForCollections(final String searchTagName)
			throws JargonException {
		log.info("buildTagCloudEntryListForCollections, user={}", irodsAccount
				.getUserName());
		
		if (searchTagName == null) {
			throw new IllegalArgumentException("null searchTagName");
		}
		
		// create a GenQuery to get the cloud info
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(");
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(")");
		sb.append(COMMA_SPACE);
		sb.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME.getName());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS.getName());
		sb.append(EQUAL_QUOTE);
		sb.append(UserTaggingConstants.TAG_AVU_UNIT);
		sb.append(QUOTE_SPACE);
		sb.append(AND);
		sb.append(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE.getName());
		sb.append(EQUAL_QUOTE);
		sb.append(irodsAccount.getUserName());
		sb.append(QUOTE);
		
		if (!searchTagName.isEmpty()) {
			sb.append(' ');
			sb.append(AND);
			sb.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME.getName());
			sb.append(" LIKE '%");
			sb.append(searchTagName);
			sb.append("%'");
		}
		
		String cloudQuery = sb.toString();
		log.debug("cloud tag query:{}", cloudQuery);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(cloudQuery, 2000);
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("irods query error", e);
			throw new JargonException(e);
		}

		List<TagCloudEntry> tagCloudEntries = new ArrayList<TagCloudEntry>();

		IRODSTagValue irodsTagValue;

		for (IRODSQueryResultRow resultRow : resultSet.getResults()) {
			log.debug("count coll:{}", resultRow.getColumn(0));
			log.debug("tag name:{}", resultRow.getColumn(1));
			irodsTagValue = new IRODSTagValue(resultRow.getColumn(1),
					irodsAccount.getUserName());
			tagCloudEntries
					.add(new TagCloudEntry(irodsTagValue, 0,
							IRODSDataConversionUtil
									.getIntOrZeroFromIRODSValue(resultRow
											.getColumn(0))));
		}
		
		return tagCloudEntries;

	}
	/**
	 * Shared method to build a tag cloud list for data objects.  This will default to the user name in the given
	 * iRODS account
	 * 
	 * @return
	 * @throws JargonException
	 */
	private List<TagCloudEntry> buildTagCloudEntryListForDataObjects(final String searchTagName)
			throws JargonException {
		
		if (searchTagName == null) {
			throw new IllegalArgumentException("null searchTagName");
		}
		
		log.info("buildTagCloudEntryListForDataObjects, user={}", irodsAccount
				.getUserName());
		
		// create a GenQuery to get the cloud info
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(");
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(')');
		sb.append(COMMA_SPACE);
		sb.append("COUNT(");
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(")");
		sb.append(COMMA_SPACE);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());
		sb.append(EQUAL_QUOTE);
		sb.append(UserTaggingConstants.TAG_AVU_UNIT);
		sb.append(QUOTE_SPACE);
		sb.append(AND);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		sb.append(EQUAL_QUOTE);
		sb.append(irodsAccount.getUserName());
		sb.append(QUOTE);
		
		if (!searchTagName.isEmpty()) {
			sb.append(' ');
			sb.append(AND);
			sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
			sb.append(" LIKE '%");
			sb.append(searchTagName);
			sb.append("%'");
		}
		
		String cloudQuery = sb.toString();
		log.debug("cloud tag query:{}", cloudQuery);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(cloudQuery, 2000);
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
				.getIRODSGenQueryExecutor(irodsAccount);
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("irods query error", e);
			throw new JargonException(e);
		}

		List<TagCloudEntry> tagCloudEntries = new ArrayList<TagCloudEntry>();
		IRODSTagValue irodsTagValue;

		for (IRODSQueryResultRow resultRow : resultSet.getResults()) {
			log.debug("count data:{}", resultRow.getColumn(0));
			log.debug("count coll:{}", resultRow.getColumn(1));
			log.debug("tag name:{}", resultRow.getColumn(2));
			irodsTagValue = new IRODSTagValue(resultRow.getColumn(2),
					irodsAccount.getUserName());
			tagCloudEntries
					.add(new TagCloudEntry(irodsTagValue,
							IRODSDataConversionUtil
									.getIntOrZeroFromIRODSValue(resultRow
											.getColumn(0)), 0));
		}
		
		return tagCloudEntries;

	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.UserTagCloudService#searchForTagsForDataObjectsAndCollectionsUsingSearchTermForTheLoggedInUser(java.lang.String)
	 */
	@Override
	public UserTagCloudView searchForTagsForDataObjectsAndCollectionsUsingSearchTermForTheLoggedInUser(final String tagSearchTerm) throws JargonException {
		log.info("searchForTagsForDataObjectsAndCollectionsUsingSearchTermForTheLoggedInUser, user={}", irodsAccount
				.getUserName());
		log.info("tag search term:{}", tagSearchTerm);
		
		UserTagCloudView userTagCloudView = UserTagCloudView.instance(
				irodsAccount.getUserName(), buildTagCloudEntryListForDataObjects(tagSearchTerm),
				buildTagCloudEntryListForCollections(tagSearchTerm));
		return userTagCloudView;
	
	}

}
