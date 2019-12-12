package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoMoreRulesException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.packinstr.UserAdminInp;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.aohelper.UserGroupAOHelper;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operations for IRODS user groups.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class UserGroupAOImpl extends IRODSGenericAO implements UserGroupAO {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final char COMMA = ',';
	private static final String RODS_GROUP = "rodsgroup";
	private IRODSGenQueryExecutor irodsGenQueryExecutor = null;

	/**
	 * @param irodsSession {@link IRODSSession}
	 * @param irodsAccount {@link IRODSAccount}
	 * @throws JargonException for iRODS error
	 */
	protected UserGroupAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	@Override
	public void addUserGroup(final UserGroup userGroup) throws DuplicateDataException, JargonException {

		log.info("addUserGroup()");

		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		if (userGroup.getUserGroupName() == null || userGroup.getUserGroupName().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no userGroupName");
		}

		if (userGroup.getZone() == null || userGroup.getZone().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no zone");
		}

		log.info("user group:{}", userGroup);

		if (!userGroup.getZone().equals(this.getIRODSAccount().getZone())) {
			log.error("cannot create a group with a different zone");
			throw new JargonException("cannot create a cross-zone group");
		}

		try {
			GeneralAdminInp adminPI = GeneralAdminInp.instanceForAddUserGroup(userGroup);
			log.debug("executing admin PI");
			getIRODSProtocol().irodsFunction(adminPI);

		} catch (NoMoreRulesException nmr) {
			log.warn(
					"no more rules exception will be treated as duplicate user to normalize behavior for pre-2.5 iRODS servers");
			throw new DuplicateDataException("no more rules exception interpreted as duplicate user", nmr);
		}
	}

	@Override
	public void addUserGroupAsGroupAdmin(final UserGroup userGroup) throws DuplicateDataException, JargonException {

		log.info("addUserGroupAsGroupAdmin()");

		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		if (userGroup.getUserGroupName() == null || userGroup.getUserGroupName().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no userGroupName");
		}

		if (userGroup.getZone() == null || userGroup.getZone().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no zone");
		}

		log.info("user group:{}", userGroup);

		if (!userGroup.getZone().equals(this.getIRODSAccount().getZone())) {
			log.error("cannot create a group with a different zone");
			throw new JargonException("cannot create a cross-zone group");
		}

		try {

			UserAdminInp adminPI = UserAdminInp.instanceForAddUserGroup(userGroup);
			log.debug("executing user admin PI");
			getIRODSProtocol().irodsFunction(adminPI);

		} catch (NoMoreRulesException nmr) {
			log.warn(
					"no more rules exception will be treated as duplicate user to normalize behavior for pre-2.5 iRODS servers");
			throw new DuplicateDataException("no more rules exception interpreted as duplicate user", nmr);
		}
	}

	@Override
	public void removeUserGroup(final String userGroupName) throws JargonException {

		log.info("removeUserGroup()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty user group name");
		}

		if (userGroupName.contains("#")) {
			throw new IllegalArgumentException("cannot remove cross-zone group");
		}

		log.info("userGroupName:{}", userGroupName);

		UserGroup userGroup = findByName(userGroupName);
		if (userGroup == null) {
			log.info("userGroup not found, treat as deleted");
			return;
		}

		removeUserGroup(userGroup);

	}

	@Override
	public void removeUserGroup(final UserGroup userGroup) throws JargonException {
		log.info("removeUserGroup()");
		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		if (userGroup.getUserGroupName() == null || userGroup.getUserGroupName().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no userGroupName");
		}

		if (userGroup.getZone() == null || userGroup.getZone().isEmpty()) {
			userGroup.setZone(getIRODSAccount().getZone());
		}

		if (!userGroup.getZone().equals(this.getIRODSAccount().getZone())) {
			log.error("cannot remove a group with a different zone");
			throw new JargonException("cannot remove a cross-zone group");
		}

		log.info("user group:{}", userGroup);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForRemoveUserGroup(userGroup);
		log.debug("executing admin PI");

		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (DataNotFoundException dnf) {
			log.warn("user group does not exist, ignoring remove");
		} catch (InvalidUserException e) {
			log.warn("user group {} does not exist, ignoring remove", userGroup);
		} catch (NoMoreRulesException nmr) {
			log.debug("no more rules exception interpereted as user does not exist, just behave as if deleted");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.IRODSUserGroupAO#find(java.lang.String)
	 */
	@Override
	public UserGroup find(final String userGroupId) throws JargonException {

		if (userGroupId == null || userGroupId.length() == 0) {
			throw new JargonException("null or missing userGroupId");
		}

		// non numeric values cause an sql exception in iRODS, catch early and
		// with better message
		try {
			Integer.parseInt(userGroupId);
		} catch (NumberFormatException nfe) {
			log.error("user group not an integer: {}", userGroupId);
			throw new IllegalArgumentException("user group not numeric");
		}

		log.info("finding user group with id: {}", userGroupId);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		try {
			this.buildUserGroupSelects(builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception for query:{}", builder, e);
			throw new JargonException("error building query", e);
		}
		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_GROUP_ID, QueryConditionOperators.EQUAL,
				userGroupId.trim());
		IRODSGenQueryFromBuilder irodsQuery = null;
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(5000);
		} catch (GenQueryBuilderException e1) {
			log.error("query builder exception for query:{}", irodsQuery, e1);
			throw new JargonException("error building query", e1);
		}

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + irodsQuery, e);
			throw new JargonException("error in user group query");
		}

		if (resultSet.getResults().size() == 0) {
			log.info("no user group found");
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user group found for id:");
			messageBuilder.append(userGroupId);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user group

		IRODSQueryResultRow row;
		try {
			row = resultSet.getFirstResult();
		} catch (DataNotFoundException e) {
			return null;
		}
		return buildUserGroupFromResultSet(row);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.UserGroupAO#findAll()
	 */
	@Override
	public List<UserGroup> findAll() throws JargonException {

		log.info("findAll()");

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		try {
			this.buildUserGroupSelects(builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception for query:{}", builder, e);
			throw new JargonException("error building query", e);
		}
		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_TYPE, QueryConditionOperators.EQUAL,
				UserTypeEnum.RODS_GROUP.getTextValue());

		IRODSGenQueryFromBuilder irodsQuery = null;
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(5000);
		} catch (GenQueryBuilderException e1) {
			log.error("query builder exception for query:{}", irodsQuery, e1);
			throw new JargonException("error building query", e1);
		}

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", irodsQuery, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;

	}

	@Override
	public UserGroup findByName(final String userGroupName) throws JargonException {

		if (userGroupName == null || userGroupName.length() == 0) {
			throw new JargonException("null or missing userGroupName");
		}

		AbstractIRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE).addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_USER_GROUP_NAME, QueryConditionOperators.EQUAL, userGroupName.trim());

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);
		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}

		if (resultSet.getResults().size() == 0) {
			log.info("no user group found");
			return null;
		}

		// I know I have just one user group
		IRODSQueryResultRow row;
		try {
			row = resultSet.getFirstResult();
		} catch (DataNotFoundException e) {
			return null;
		}
		return buildUserGroupFromResultSet(row);

	}

	@Override
	public List<User> listUserGroupMembers(final String userGroupName) throws JargonException {

		log.info("listUserGroupMembers()");

		return listUserGroupMembers(userGroupName, "");
	}

	@Override
	public List<User> listUserGroupMembers(final String userGroupName, final String targetZone) throws JargonException {

		log.info("listUserGroupMembers()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (targetZone == null) {
			throw new IllegalArgumentException("null or empty targetZone");
		}

		List<User> users = new ArrayList<>();

		// create query for users in group

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_TYPE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_INFO)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_COMMENT)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_CREATE_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_MODIFY_TIME).addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_USER_GROUP_NAME, QueryConditionOperators.EQUAL, userGroupName.trim());

			IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(
					builder.exportIRODSQueryFromBuilder(
							getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()),
					0, targetZone);

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				// build user, do not retrieve the user DN (too expensive)
				if (row.getColumn(3).equals(userGroupName)) {
					continue;
				}
				users.add(buildUserFromGroupMembersResultSet(row, irodsGenQueryExecutor));
			}
		} catch (GenQueryBuilderException | JargonQueryException e) {
			log.error("error querying for groups", e);
			throw new JargonException("error in group query", e);
		}

		return users;
	}

	private User buildUserFromGroupMembersResultSet(IRODSQueryResultRow row,
			IRODSGenQueryExecutor irodsGenQueryExecutor) throws JargonException {
		User user = new User();
		user.setCount(row.getRecordCount());
		user.setLastResult(row.isLastResult());
		user.setId(row.getColumn(2));
		user.setName(row.getColumn(3));
		user.setZone(row.getColumn(4));

		user.setUserType(UserTypeEnum.findTypeByString(row.getColumn(5)));

		user.setInfo(row.getColumn(6));
		user.setComment(row.getColumn(7));
		user.setCreateTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(8)));
		user.setModifyTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(9)));
		log.info("user built:{}", user);
		return user;
	}

	@Override
	public List<UserGroup> findUserGroups(final String userGroupName) throws JargonException {
		/*
		 * Delegate to case-sensitive search method to preserve prior API
		 */
		return findUserGroups(userGroupName, false);
	}

	@Override
	public List<UserGroup> findUserGroupsForUserInZone(final String userName, final String targetZone)
			throws JargonException {

		log.info("findUserGroupsForUserInZone()");

		if (userName == null || userName.length() == 0) {
			throw new JargonException("null or missing userName");
		}

		if (targetZone == null) {
			throw new IllegalArgumentException("null targetZone");
		}

		log.info("userName:{}", userName);
		log.info("targetZone:{}", targetZone);

		if (targetZone.isEmpty()) {
			return findUserGroups(userName);
		}

		String zoneFromUserName = MiscIRODSUtils.getZoneInUserName(userName);
		String userFromUserName = MiscIRODSUtils.getUserInUserName(userName);

		log.debug("zoneFromUserName:{}", zoneFromUserName);
		log.debug("userFromUserName:{}", userFromUserName);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		/*
		 * If user is entered in user#zone format separate out into distinct query
		 * elements as user name and zone are different database fields
		 */

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_NAME, QueryConditionOperators.EQUAL,
				userFromUserName);
		if (!zoneFromUserName.isEmpty()) {
			log.debug("adding zone to query");
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_ZONE, QueryConditionOperators.EQUAL,
					zoneFromUserName);
		}
		IRODSGenQueryFromBuilder irodsQuery = null;

		try {
			this.buildUserGroupSelects(builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception for query:{}", builder, e);
			throw new JargonException("error building query", e);
		}
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(5000);
		} catch (GenQueryBuilderException e1) {
			log.error("query builder exception for query:{}", irodsQuery, e1);
			throw new JargonException("error building query", e1);
		}

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, targetZone);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", irodsQuery, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			if (row.getColumn(0).equals(userName)) {
				continue;
			}
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	@Override
	public List<UserGroup> findUserGroupsForUser(final String userName) throws JargonException {
		if (userName == null || userName.length() == 0) {
			throw new JargonException("null or missing userName");
		}

		log.info("find user group with user name: {}", userName);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_NAME, QueryConditionOperators.EQUAL,
				userName.trim());
		IRODSGenQueryFromBuilder irodsQuery = null;
		try {
			this.buildUserGroupSelects(builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception for query:{}", builder, e);
			throw new JargonException("error building query", e);
		}
		try {
			irodsQuery = builder.exportIRODSQueryFromBuilder(5000);
		} catch (GenQueryBuilderException e1) {
			log.error("query builder exception for query:{}", irodsQuery, e1);
			throw new JargonException("error building query", e1);
		}

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", irodsQuery, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			if (row.getColumn(0).equals(userName)) {
				continue;
			}
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	@Override
	public boolean isUserInGroup(final String userName, final String groupName) throws JargonException {

		log.info("isUserinGroup()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or empty");
		}

		if (groupName == null || groupName.isEmpty()) {
			throw new IllegalArgumentException("groupName is null or empty");
		}

		log.info("userName:{}", userName);
		log.info("groupName:{}", groupName);

		boolean inGroup = false;

		AbstractIRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_GROUP_NAME, QueryConditionOperators.EQUAL,
							groupName.trim())
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_NAME,

							QueryConditionOperators.EQUAL, userName.trim());

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);

			if (resultSet.getResults().isEmpty()) {
				inGroup = false;
			} else {
				inGroup = true;
			}

			return inGroup;

		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}
	}

	@Override
	public void addUserToGroup(final String userGroupName, final String userName, final String zoneName)
			throws DuplicateDataException, InvalidGroupException, InvalidUserException, JargonException {

		log.info("addUserToGroup()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userName");
		}

		log.info("userName:{}", userName);
		log.info("userGroupName:{}", userGroupName);

		if (zoneName != null) {
			log.info("zoneName:{}", zoneName);
		}

		// call the iadmin iRODS service

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForAddUserToGroup(userGroupName, userName, zoneName);

		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);
	}

	@Override
	public void addUserToGroupAsGroupAdmin(final String userGroupName, final String userName, final String zoneName)
			throws DuplicateDataException, InvalidGroupException, InvalidUserException, JargonException {

		log.info("addUserToGroupAsGroupAdmin()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userName");
		}

		log.info("userName:{}", userName);
		log.info("userGroupName:{}", userGroupName);

		if (zoneName != null) {
			log.info("zoneName:{}", zoneName);
		}

		// call the iadmin iRODS service

		UserAdminInp adminPI = UserAdminInp.instanceForAddUserToGroup(userGroupName, userName, zoneName);

		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);
	}

	@Override
	public void removeUserFromGroup(final String userGroupName, final String userName, final String zoneName)

			throws InvalidUserException, InvalidGroupException, JargonException {

		log.info("removeUserFromGroup()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userName");
		}

		log.info("userName:{}", userName);
		log.info("userGroupName:{}", userGroupName);

		if (zoneName != null) {
			log.info("zoneName:{}", zoneName);
		}

		// call the iadmin iRODS service

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForRemoveUserFromGroup(userGroupName, userName, zoneName);

		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);

	}

	private IRODSGenQueryBuilder buildUserGroupSelects(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {
		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_ID);
		return builder;
	}

	private UserGroup buildUserGroupFromResultSet(final IRODSQueryResultRow row) throws JargonException {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(row.getColumn(1));
		userGroup.setUserGroupName(row.getColumn(0));
		userGroup.setZone(this.getIRODSAccount().getZone());
		return userGroup;
	}

	private IRODSGenQueryExecutor getGenQueryExecutor() throws JargonException {
		if (irodsGenQueryExecutor == null) {
			irodsGenQueryExecutor = getIRODSAccessObjectFactory().getIRODSGenQueryExecutor(getIRODSAccount());
		}

		return irodsGenQueryExecutor;

	}

	@Override
	public List<UserGroup> findUserGroups(String userGroupName, boolean caseInsensitive) throws JargonException {
		log.info("findUserGroups()");

		if (userGroupName == null) {
			throw new IllegalArgumentException("null userGroupName");
		}

		log.info("caseInsensitive:{}", caseInsensitive);

		log.info("for user group name:{}", userGroupName);

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, caseInsensitive, null);
		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		try {
			UserGroupAOHelper.buildSelectsByAppendingToBuilder(builder);
		} catch (GenQueryBuilderException e) {
			log.error("query builder exception building user group query", e);
			throw new JargonException("unable to build user group query", e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(userGroupName.trim());
		sb.append('%');

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_GROUP_NAME, QueryConditionOperators.LIKE,
				sb.toString())
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_TYPE, QueryConditionOperators.EQUAL, RODS_GROUP);
		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	@Override
	public List<String> findUserGroupNames(String userGroupName, boolean caseInsensitive) throws JargonException {
		log.info("findUserGroups()");

		if (userGroupName == null) {
			throw new IllegalArgumentException("null userGroupName");
		}

		log.info("caseInsensitive:{}", caseInsensitive);

		log.info("for user group name:{}", userGroupName);

		List<String> userGroups = new ArrayList<String>();

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, caseInsensitive, null);
		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME);
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("query builder error", e);
		}
		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		StringBuilder sb = new StringBuilder();
		sb.append(userGroupName.trim());
		sb.append('%');

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_GROUP_NAME, QueryConditionOperators.LIKE,
				sb.toString())
				.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_TYPE, QueryConditionOperators.EQUAL, RODS_GROUP);
		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties().getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(row.getColumn(0));
		}

		return userGroups;
	}

}
