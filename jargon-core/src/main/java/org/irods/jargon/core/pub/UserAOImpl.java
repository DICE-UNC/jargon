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
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.packinstr.UserAdminInp;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.security.IRODSPasswordUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to access underlying user information in IRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class UserAOImpl extends IRODSGenericAO implements UserAO {

	public static final String ERROR_IN_USER_QUERY = "error in user query";

	public static final int DEFAULT_REC_COUNT = 500;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private static final char COMMA = ',';
	private static final String AND = " AND ";
	private static final String EQUALS = " = ";

	protected UserAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#listUserMetadata(java.lang.String)
	 */
	@Override
	public List<AvuData> listUserMetadata(final String userId)
			throws JargonException {
		if (userId == null || userId.isEmpty()) {
			throw new JargonException("null or empty userId");
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
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					0);
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
	 * org.irods.jargon.core.accessobject.UserAO#addUser(org.irods.jargon.core
	 * .domain.User)
	 */
	@Override
	public void addUser(final User user) throws JargonException,
			DuplicateDataException {

		if (log.isDebugEnabled()) {
			log.debug("adding a user:" + user);
		}

		if (user == null) {
			throw new JargonException("cannot add null user");
		}

		updatePreChecks(user);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForAddUser(user);
		log.debug("executing admin PI");

		try {

			getIRODSProtocol().irodsFunction(adminPI);

		} catch (JargonException je) {
			log.info("jargon exception, check for duplicate condition");

			if (je.getMessage().indexOf("-1018000") > -1) {
				throw new DuplicateDataException("user already exists");
			} else {
				log.error("jargon exception on add user", je);
				throw je;
			}

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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#findAll()
	 */
	@Override
	public List<User> findAll() throws JargonException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();
		userQuery.append(buildUserSelects());

		String userQueryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user query:" + userQueryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		List<User> users = new ArrayList<User>();
		User user;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			user = buildUserFromResultSet(row);
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
			throw new JargonException("null where statement");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder sb = new StringBuilder();
		sb.append(buildUserSelects());

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
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		List<User> users = new ArrayList<User>();
		User user;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			user = buildUserFromResultSet(row);
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
	public User findById(final String userId) throws JargonException,
			DataNotFoundException {
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();

		userQuery.append(buildUserSelects());
		userQuery.append(" where ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(" = '");
		userQuery.append(userId);
		userQuery.append("'");
		userQuery.append(AND);
		userQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		userQuery.append(EQUALS);
		userQuery.append("'");
		userQuery.append(this.getIRODSAccount().getZone());
		userQuery.append("'");

		String userQueryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user query:" + userQueryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + userQueryString, e);
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

		IRODSQueryResultRow row = resultSet.getResults().get(0);
		User user = buildUserFromResultSet(row);

		return user;

	}

	private User buildUserFromResultSet(final IRODSQueryResultRow row)
			throws JargonException {
		User user = new User();
		user.setId(row.getColumn(0));
		user.setName(row.getColumn(1));
		user.setUserType(UserTypeEnum.findTypeByString(row.getColumn(2)));
		user.setZone(row.getColumn(3));
		user.setInfo(row.getColumn(4));
		user.setComment(row.getColumn(5));
		// FIXME: do dates
		String userDn = findUserDnIfExists(user.getId());
		if (userDn != null) {
			user.setUserDN(userDn);
		}
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
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();

		userQuery.append(buildUserSelects());
		userQuery.append(" where ");
		userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		userQuery.append(" = '");
		userQuery.append(userName.trim());
		userQuery.append("'");
		userQuery.append(AND);
		userQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		userQuery.append(EQUALS);
		userQuery.append("'");
		userQuery.append(this.getIRODSAccount().getZone());
		userQuery.append("'");

		String userQueryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user query:" + userQueryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					DEFAULT_REC_COUNT);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + userQueryString, e);
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
		User user = buildUserFromResultSet(row);

		return user;
	}

	/**
	 * Handy method will build the select portion of a gen query that accesses
	 * user data.
	 * 
	 * @return <code>String</code> with genquery select statement that obtains
	 *         user data.
	 */
	public String buildUserSelects() {
		StringBuilder userQuery = new StringBuilder();
		userQuery.append("select ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_INFO.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_COMMENT.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_CREATE_TIME.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_MODIFY_TIME.getName());
		return userQuery.toString();
	}

	private String findUserDnIfExists(final String userId)
			throws JargonException {
		String dn = null;
		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder userQuery = new StringBuilder();

		userQuery.append("select ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_DN.getName());

		userQuery.append(" where ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(" = '");
		userQuery.append(userId);
		userQuery.append("'");

		String userQueryString = userQuery.toString();
		if (log.isInfoEnabled()) {
			log.info("user dn query:" + userQueryString);
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString,
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					DEFAULT_REC_COUNT);
		} catch (JargonQueryException e) {
			log.error("query exception for user dn query:" + userQueryString, e);
			throw new JargonException(ERROR_IN_USER_QUERY);
		}

		if (resultSet.getResults().size() == 0) {
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user dn found for id:");
			messageBuilder.append(userId);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user DN

		IRODSQueryResultRow row = resultSet.getResults().get(0);
		dn = row.getColumn(1);
		return dn;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserAO#deleteUser(java.lang.String)
	 */
	@Override
	public void deleteUser(final String userName) throws JargonException {
		if (userName == null || userName.isEmpty()) {
			throw new JargonException("null or empty user name");
		}
		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForDeleteUser(userName);
		log.debug("executing admin PI");

		try {

			getIRODSProtocol().irodsFunction(adminPI);

		} catch (JargonException je) {
			log.info("jargon exception, check for delete of non-existent user");

			if (je.getMessage().indexOf("-1018000") > -1) {
				log.debug("user does not exist, just behave as if deleted");
			} else {
				log.error("jargon exception on delete user", je);
				throw je;
			}

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
			throw new JargonException("user is null");
		}

		if (user.getId() == null || user.getId().isEmpty()) {
			throw new JargonException("user id is null or empty, cannot update");
		}

		updatePreChecks(user);

		User currentUser = this.findById(user.getId());

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
	 * org.irods.jargon.core.pub.UserAO#changeUserPassword(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void changeAUserPasswordByThatUser(final String userName,
			final String currentPassword, final String newPassword)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("userName is null or missing");
		}

		if (currentPassword == null || currentPassword.isEmpty()) {
			throw new JargonException("currentPassword is null or missing");
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new JargonException("newPassword is null or missing");
		}

		log.info("changeAUserPasswordByThatUser for user:{}", userName);

		String obfuscatedPassword = IRODSPasswordUtilities
				.obfuscateIRODSPassword(newPassword, currentPassword);
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
		// FIXME: not yet implemented, needs obfuscation of user password

		// see clientLogin.c and iadmin.c(line 807) for details yet to be
		// implemented
		if (userName == null || userName.isEmpty()) {
			throw new JargonException("userName is null or missing");
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new JargonException("newPassword is null or missing");
		}

		log.info("changeAUserPasswordByAnAdmin for user:{}", userName);
		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForModifyUserPassword(userName, newPassword);
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

}
