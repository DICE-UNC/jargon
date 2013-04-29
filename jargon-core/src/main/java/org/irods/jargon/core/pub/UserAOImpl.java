/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoMoreRulesException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.packinstr.GetTempPasswordForOther;
import org.irods.jargon.core.packinstr.GetTempPasswordIn;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.packinstr.UserAdminInp;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.aohelper.UserAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.security.IRODSPasswordUtilities;
import org.irods.jargon.core.utils.FederationEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to access underlying user information in IRODS.
 * <p/>
 * Note that, currently, user DN information requires an additional GenQuery
 * call per retrieved user, so this is off by default for list methods, and on
 * by default when retrieving an individual user. There is a method to retrieve
 * the user DN if required.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class UserAOImpl extends IRODSGenericAO implements UserAO {

	private static final String ERROR_IN_USER_QUERY = "error in user query";

	private static final int DEFAULT_REC_COUNT = 500;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private static final char COMMA = ',';
	private static final String AND = " AND ";
	private static final String EQUALS = " = ";
	private static final String STRING_TO_HASH_WITH = "stringToHashWith";
	private IRODSGenQueryExecutor irodsGenQueryExecutor = null;

	protected UserAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#listUserMetadataForUserId(java.lang.
	 * String)
	 */
	@Override
	public List<AvuData> listUserMetadataForUserId(final String userId)
			throws JargonException {

		if (userId == null || userId.isEmpty()) {
			throw new IllegalArgumentException("null or empty userId");
		}
		log.info("list user metadata for {}", userId);

		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_VALUE.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_UNITS.getName());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_USER_ID.getName());
		sb.append(" = '");
		sb.append(userId);
		sb.append("'");
		log.debug("user avu list query: {}", sb.toString());
		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query: " + sb.toString(), e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		final List<AvuData> avuDatas = new ArrayList<AvuData>();
		AvuData avuData = null;

		if (resultSet.getNumberOfResultColumns() != 3) {
			final String msg = "number of results for avu query should be 3, was:"
					+ resultSet.getNumberOfResultColumns();
			log.error(msg);
			throw new JargonException(msg);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			avuData = AvuData.instance(row.getColumn(0), row.getColumn(1),
					row.getColumn(2));
			avuDatas.add(avuData);
			if (log.isDebugEnabled()) {
				log.debug("found avu for user:" + avuData);
			}
		}

		return avuDatas;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#listUserMetadataForUserName(java.lang
	 * .String)
	 */
	@Override
	public List<AvuData> listUserMetadataForUserName(final String userName)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		log.info("list user metadata for user name: {}", userName);

		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_VALUE.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_USER_ATTR_UNITS.getName());
		sb.append(" WHERE ");
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" = '");
		sb.append(userName);
		sb.append("'");
		log.debug("user avu list query: {}", sb.toString());
		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{} ", sb.toString(), e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		final List<AvuData> avuDatas = new ArrayList<AvuData>();
		AvuData avuData = null;

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			avuData = AvuData.instance(row.getColumn(0), row.getColumn(1),
					row.getColumn(2));
			avuDatas.add(avuData);
			log.debug("found avu for user:{}", avuData);

		}

		return avuDatas;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.UserAO#addUser(org.irods.jargon.core
	 * .domain.User)
	 */
	@Override
	public User addUser(final User user) throws JargonException,
			DuplicateDataException {

		if (log.isDebugEnabled()) {
			log.debug("adding a user:{}", user);
		}

		if (user == null) {
			throw new IllegalArgumentException("cannot add null user");
		}

		updatePreChecks(user);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForAddUser(user);
		log.debug("executing admin PI");

		try {

			getIRODSProtocol().irodsFunction(adminPI);

		} catch (DuplicateDataException dde) {
			throw dde;
		} catch (NoMoreRulesException nmr) {
			log.warn(
					"no more rules exception caught, will throw as duplicate data for backwards compatibility",
					nmr);
			throw new DuplicateDataException(
					"no more rules interpereted as duplicate data exception for backwards compatibility");
		}

		log.debug("user added, now process other fields");

		if (!user.getComment().isEmpty()) {
			log.debug("comment has changed");
			updateUserComment(user);
		}

		if (!user.getInfo().isEmpty()) {
			log.debug("info has changed");
			updateUserInfo(user);
		}

		return findByName(user.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findAll()
	 */
	@Override
	@FederationEnabled
	public List<User> findAll() throws JargonException {

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		IRODSQueryResultSet resultSet = null;
		try {
			UserAOHelper.addUserSelectsToBuilder(builder);
			builder.addOrderByGenQueryField(RodsGenQueryEnum.COL_USER_NAME,
					OrderByType.ASC).addOrderByGenQueryField(
					RodsGenQueryEnum.COL_USER_ZONE, OrderByType.ASC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for user", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for user", e);
		}

		List<User> users = new ArrayList<User>();
		User user;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			user = UserAOHelper.buildUserFromResultSet(row,
					getGenQueryExecutor(), false);
			user.setTotalRecords(resultSet.getTotalRecords());
			users.add(user);
		}

		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findWhere(java.lang.String)
	 */
	@Override
	public List<User> findWhere(final String whereStatement)
			throws JargonException {

		if (whereStatement == null) {
			throw new IllegalArgumentException("null where statement");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(UserAOHelper.buildUserSelects());

		if (!whereStatement.isEmpty()) {
			sb.append(" WHERE ");
			sb.append(whereStatement);
		} else {
			log.debug("no where statement given, so will do plain select");
		}

		String userQueryString = sb.toString();
		if (log.isInfoEnabled()) {
			log.info("user query: " + userQueryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query: {}", userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		List<User> users = new ArrayList<User>();
		User user;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			user = UserAOHelper.buildUserFromResultSet(row,
					getGenQueryExecutor(), false);
			user.setTotalRecords(resultSet.getTotalRecords());
			users.add(user);
		}

		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findById(java.lang.String)
	 */
	@Override
	@FederationEnabled
	public User findById(final String userId) throws JargonException,
			DataNotFoundException {
		return findByIdInZone(userId, getIRODSAccount().getZone());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findByIdInZone(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@FederationEnabled
	public User findByIdInZone(final String userId, final String zone)
			throws JargonException, DataNotFoundException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();

		userQuery.append(UserAOHelper.buildUserSelects());
		userQuery.append(" where ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(" = '");
		userQuery.append(userId);
		userQuery.append("'");

		String userQueryString = userQuery.toString();

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		if (resultSet.getResults().size() == 0) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("user not found for id:");
			messageBuilder.append(userId);
			String message = messageBuilder.toString();
			log.warn(message);
			throw new DataNotFoundException(message);
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user found for id:");
			messageBuilder.append(userId);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user

		IRODSQueryResultRow row = resultSet.getFirstResult();
		User user = UserAOHelper.buildUserFromResultSet(row,
				getGenQueryExecutor(), true);

		return user;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findByName(java.lang.String)
	 */
	@Override
	public User findByName(final String userName) throws JargonException,
			DataNotFoundException {

		/*
		 * See if there is a zone component
		 */

		String theUser = UserAOHelper.getUserNameFromUserPoundZone(userName);
		String theZone = UserAOHelper.getZoneFromUserPoundZone(userName);
		if (theZone.isEmpty()) {
			theZone = getIRODSAccount().getZone();
		}

		StringBuilder userQuery = new StringBuilder();

		userQuery.append(UserAOHelper.buildUserSelects());
		userQuery.append(" where ");
		userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		userQuery.append(" = '");
		userQuery.append(theUser);
		userQuery.append("'");
		userQuery.append(AND);
		userQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		userQuery.append(EQUALS);
		userQuery.append("'");
		userQuery.append(theZone);
		userQuery.append("'");

		String userQueryString = userQuery.toString();
		log.info("user query:{}", userQueryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:{}", userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		if (resultSet.getResults().size() == 0) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("user not found for name:");
			messageBuilder.append(userName);
			String message = messageBuilder.toString();
			log.warn(message);
			throw new DataNotFoundException(message);
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user found for name:");
			messageBuilder.append(userName);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user

		IRODSQueryResultRow row = resultSet.getResults().get(0);
		User user = UserAOHelper.buildUserFromResultSet(row,
				getGenQueryExecutor(), true);

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findUserNameLike(java.lang.String)
	 */
	@Override
	public List<String> findUserNameLike(final String userName)
			throws JargonException {

		if (userName == null) {
			throw new IllegalArgumentException("null userName");
		}

		log.info("findUserNameLike {}", userName);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		StringBuilder userQuery = new StringBuilder();
		userQuery.append(userName.trim());
		userQuery.append("%");

		IRODSQueryResultSet resultSet = null;
		try {
			UserAOHelper.addUserSelectsToBuilder(builder);
			builder.addOrderByGenQueryField(RodsGenQueryEnum.COL_USER_NAME,
					OrderByType.ASC)
					.addOrderByGenQueryField(RodsGenQueryEnum.COL_USER_ZONE,
							OrderByType.ASC)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_USER_NAME,
							QueryConditionOperators.LIKE,
							userQuery.toString().trim());
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = getGenQueryExecutor().executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for user", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for user", e);
		}

		List<String> results = new ArrayList<String>();
		StringBuilder name = new StringBuilder();
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			name = new StringBuilder();
			name.append(row.getColumn(1));
			name.append('#');
			name.append(row.getColumn(0));
			results.add(name.toString());
		}

		log.debug("user list:{}", results);
		return results;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#retriveUserDNByUserId(java.lang.String)
	 */
	@Override
	public String retriveUserDNByUserId(final String userId)
			throws JargonException {
		return UserAOHelper.findUserDnIfExists(userId, getGenQueryExecutor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#deleteUser(java.lang.String)
	 */
	@Override
	public void deleteUser(final String userName) throws InvalidUserException,
			JargonException {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty user name");
		}
		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForDeleteUser(userName);
		log.debug("executing admin PI");

		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (InvalidUserException iue) {
			log.debug("user does not exist, just behave as if deleted");
		} catch (NoMoreRulesException nmr) {
			log.debug("no more rules exception interpereted as user does not exist, just behave as if deleted");
		}

		log.info("user {} removed", userName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#updateUser(org.irods.jargon.core.pub
	 * .domain.User)
	 */
	@Override
	public void updateUser(final User user) throws JargonException,
			DataNotFoundException {

		if (user == null) {
			throw new IllegalArgumentException("user is null");
		}

		if (user.getId() == null || user.getId().isEmpty()) {
			throw new IllegalArgumentException(
					"user id is null or empty, cannot update");
		}

		updatePreChecks(user);

		User currentUser = findById(user.getId());

		if (!user.getComment().equals(currentUser.getComment())) {
			log.debug("comment has changed");
			updateUserComment(user);
		}

		if (!user.getInfo().equals(currentUser.getInfo())) {
			log.debug("info has changed");
			updateUserInfo(user);
		}

		if (!user.getZone().equals(currentUser.getZone())) {
			log.debug("zone has changed");
			updateUserZone(user);
		}

		if (!user.getUserType().equals(currentUser.getUserType())) {
			log.debug("user type has changed");
			updateUserType(user);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#getTemporaryPasswordForConnectedUser()
	 */
	@Override
	public String getTemporaryPasswordForConnectedUser() throws JargonException {
		GetTempPasswordIn getPasswordInPI = GetTempPasswordIn.instance();
		log.debug("executing getPasswordInPI");

		Tag response = getIRODSProtocol().irodsFunction(getPasswordInPI);

		String responseHashCode = response.getTag(STRING_TO_HASH_WITH)
				.getStringValue();
		log.info("hash value:{}", responseHashCode);
		String tempPassword = IRODSPasswordUtilities.getHashedPassword(
				responseHashCode, getIRODSAccount());

		return tempPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#getTemporaryPasswordForASpecifiedUser
	 * (java.lang.String)
	 */
	@Override
	public String getTemporaryPasswordForASpecifiedUser(
			final String targetUserName) throws JargonException {

		log.info("getTemporaryPasswordForASpecifiedUser()");

		// test is only valid for 3.1+
		if (!getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			throw new UnsupportedOperationException(
					"temp password generation implemented in iRODS 3.1+ only");
		}

		// parm checks done in packing instruction
		GetTempPasswordForOther getTempPasswordForOtherPI = GetTempPasswordForOther
				.instance(targetUserName);
		Tag response = getIRODSProtocol().irodsFunction(
				getTempPasswordForOtherPI);

		String responseHashCode = response.getTag(STRING_TO_HASH_WITH)
				.getStringValue();
		log.info("hash value:{}", responseHashCode);
		String tempPassword = IRODSPasswordUtilities.getHashedPassword(
				responseHashCode, getIRODSAccount());

		return tempPassword;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#changeUserPassword(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void changeAUserPasswordByThatUser(final String userName,
			final String currentPassword, final String newPassword)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or missing");
		}

		if (currentPassword == null || currentPassword.isEmpty()) {
			throw new IllegalArgumentException(
					"currentPassword is null or missing");
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new IllegalArgumentException("newPassword is null or missing");
		}

		log.info("changeAUserPasswordByThatUser for user:{}", userName);

		String obfuscatedPassword = IRODSPasswordUtilities.obfEncodeByKey(
				newPassword, currentPassword, true);
		UserAdminInp userAdminIn = UserAdminInp.instanceForChangeUserPassword(
				userName, obfuscatedPassword);
		getIRODSProtocol().irodsFunction(userAdminIn);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserAO#changeAUserPasswordByAnAdmin(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void changeAUserPasswordByAnAdmin(final String userName,
			final String newPassword) throws JargonException {

		// see clientLogin.c and iadmin.c(line 807) for irods equivalent
		// functions
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or missing");
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new IllegalArgumentException("newPassword is null or missing");
		}

		String randPaddedNewPassword = IRODSPasswordUtilities
				.padPasswordWithRandomStringData(newPassword);

		String key2 = getIRODSProtocol().getAuthResponse().getChallengeValue();
		String derivedChallenge = IRODSPasswordUtilities
				.deriveHexSubsetOfChallenge(key2);
		String myKey2 = IRODSPasswordUtilities
				.obfuscateIRODSPasswordForAdminPasswordChange(
						randPaddedNewPassword, getIRODSAccount().getPassword(),
						derivedChallenge);

		log.info("changeAUserPasswordByAnAdmin for user:{}", userName);
		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForModifyUserPassword(userName, myKey2);
		getIRODSProtocol().irodsFunction(adminPI);

	}

	private void updatePreChecks(final User user) throws JargonException {
		if (user.getName() == null || user.getName().isEmpty()) {
			throw new JargonException("user name is null or missing");
		}

		if (user.getComment() == null) {
			throw new JargonException("user comment is null");
		}

		if (user.getInfo() == null) {
			throw new JargonException("user info is null");
		}

		if (user.getZone() == null) {
			throw new JargonException("user zone is null");
		}

		if (user.getUserType() == null
				|| user.getUserType() == UserTypeEnum.RODS_UNKNOWN) {
			throw new JargonException("null or unknown user type");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String userName, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to user: {}", avuData);
		log.info("userName {}", userName);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddUserMetadata(userName, avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-827000") > -1) {
				throw new DataNotFoundException(
						"User was not found, could not add AVU");
			}

			log.error("jargon exception adding AVU metadata", je);
			throw je;
		}

		log.debug("metadata added");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#modifyAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAVUMetadata(final String userName, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		throw new UnsupportedOperationException("need to implement");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#deleteAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String userName, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("deleting avu metadata for user: {}", avuData);
		log.info("userName {}", userName);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteUserMetadata(userName, avuData);

		log.debug("sending avu request");
		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-827000") > -1) {
				throw new DataNotFoundException(
						"User was not found, could not delete AVU");
			}

			log.error("jargon exception adding AVU metadata", je);
			throw je;
		}
		log.debug("metadata deleted");

	}

	@Override
	public void updateUserInfo(final String userName, final String userInfo)
			throws DataNotFoundException, JargonException {
		log.info("updateUserInfo()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (userInfo == null) {
			throw new IllegalArgumentException("null userInfo");
		}

		log.info("userName:{}", userName);
		log.info("userInfo:{}", userInfo);

		User user = findByName(userName);

		log.info("looked up user:{}", user);
		user.setInfo(userInfo);
		this.updateUserInfo(user);
		log.info("updated info");
	}

	private void updateUserType(final User user) throws JargonException {
		GeneralAdminInp adminPI = GeneralAdminInp.instanceForModifyUserType(
				user.getName(), user.getUserType());
		getIRODSProtocol().irodsFunction(adminPI);
	}

	private void updateUserZone(final User user) throws JargonException {
		GeneralAdminInp adminPI = GeneralAdminInp.instanceForModifyUserZone(
				user.getName(), user.getZone());
		getIRODSProtocol().irodsFunction(adminPI);
	}

	private void updateUserComment(final User user) throws JargonException {
		GeneralAdminInp adminPI = GeneralAdminInp.instanceForModifyUserComment(
				user.getName(), user.getComment());
		getIRODSProtocol().irodsFunction(adminPI);
	}

	private void updateUserInfo(final User user) throws JargonException {
		GeneralAdminInp adminPI = GeneralAdminInp.instanceForModifyUserInfo(
				user.getName(), user.getInfo());
		getIRODSProtocol().irodsFunction(adminPI);
	}

	private IRODSGenQueryExecutor getGenQueryExecutor() throws JargonException {
		if (irodsGenQueryExecutor == null) {
			irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());
		}

		return irodsGenQueryExecutor;

	}

}
