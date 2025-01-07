/**
 *
 */
package org.irods.jargon.core.pub.aohelper;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General helper methods for users and user groups.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserAOHelper {

	private static final char COMMA = ',';
	static Logger log = LogManager.getLogger(UserAOHelper.class);

	/**
	 * Handy method will build the select portion of a gen query that accesses user
	 * data.
	 *
	 * @return {@code String} with genquery select statement that obtains user data.
	 */
	public static String buildUserSelects() {
		StringBuilder userQuery = new StringBuilder();
		userQuery.append("select ");
		userQuery.append(RodsGenQueryEnum.COL_USER_ZONE.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_ID.getName());
		userQuery.append(COMMA);
		userQuery.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
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

	/**
	 * Build the selects appropriate for a user query by appending them to the
	 * provided builder
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} to be appended
	 * @throws GenQueryBuilderException
	 *             for query error
	 */
	public static void addUserSelectsToBuilder(final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_TYPE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_INFO)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_COMMENT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_MODIFY_TIME);
	}

	/**
	 * Given a query build from the {@code buildUserSelects}, create a user for a
	 * given result row
	 *
	 * @param row
	 *            {@link IRODSQueryResultRow}
	 * @param irodsGenQueryExecutor
	 *            {@link IRODSGenQueryExecutor} access object to do various queries
	 * @param retrieveDN
	 *            {@code boolean} that causes an additional lookup to retrieve the
	 *            Distinguished Name. {@code false} avoids an additional lookup per
	 *            user.
	 * @return {@link User} built from that row result
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static User buildUserFromResultSet(final IRODSQueryResultRow row,
			final IRODSGenQueryExecutor irodsGenQueryExecutor, final boolean retrieveDN) throws JargonException {
		User user = new User();
		user.setCount(row.getRecordCount());
		user.setLastResult(row.isLastResult());
		user.setId(row.getColumn(2));
		user.setName(row.getColumn(1));
		user.setZone(row.getColumn(0));

		user.setUserType(UserTypeEnum.findTypeByString(row.getColumn(3)));

		user.setInfo(row.getColumn(4));
		user.setComment(row.getColumn(5));
		user.setCreateTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(6)));
		user.setModifyTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(7)));

		// do add'l lookup of DN if requested
		if (retrieveDN) {
			String userDn = findUserDnIfExists(user.getId(), irodsGenQueryExecutor);
			if (userDn != null) {
				user.setUserDN(userDn);
			}
		}

		log.info("user built:{}", user);
		return user;
	}

	/**
	 * Locate the user distinguished name, if it exists
	 *
	 * @param userId
	 *            {@link String} with the user id
	 * @param irodsGenQueryExecutor
	 *            {@link IRODSGenQueryExecutor}
	 * @return {@code String} with the distinguished name for the user, if it
	 *         exists, otherwise, {@code null} will be returned
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static String findUserDnIfExists(final String userId, final IRODSGenQueryExecutor irodsGenQueryExecutor)
			throws JargonException {
		String dn = null;

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_DN)
					.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_ID, QueryConditionOperators.EQUAL, userId);

			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, "");
		} catch (GenQueryBuilderException e) {
			throw new JargonException("error in query", e);
		} catch (JargonQueryException e) {
			throw new JargonException("error in query", e);
		}

		if (resultSet.getResults().size() == 0) {
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user dn found for id:");
			messageBuilder.append(userId);
			String message = messageBuilder.toString();
			throw new JargonException(message);
		}

		// I know I have just one user DN

		IRODSQueryResultRow row = resultSet.getResults().get(0);
		dn = row.getColumn(1);
		return dn;

	}

	/**
	 * Given a user name that might be in the format user#zone, get the user part
	 *
	 * @param userName
	 *            {@code String} with the user name, possibly in user#zone format
	 * @return {@code String} with only the user name, not the zone
	 */
	public static String getUserNameFromUserPoundZone(final String userName) {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		int poundIdx = userName.indexOf("#");
		if (poundIdx > -1) {
			return userName.substring(0, poundIdx);
		} else {
			return userName;
		}
	}

	/**
	 * Given a user name that might be in the format user#zone, get the zone part
	 *
	 * @param userName
	 *            {@code String} with the user name, possibly in user#zone format
	 * @return {@code String} with only the zone name, not the user
	 */
	public static String getZoneFromUserPoundZone(final String userName) {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		int poundIdx = userName.indexOf("#");
		if (poundIdx > -1) {
			return userName.substring(poundIdx + 1);
		} else {
			return "";
		}
	}

}
