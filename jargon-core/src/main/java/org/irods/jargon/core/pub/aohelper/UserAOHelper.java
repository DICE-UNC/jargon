/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;

/**
 * General helper methods for users and user groups.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserAOHelper {

	private static final char COMMA = ',';

	/**
	 * Handy method will build the select portion of a gen query that accesses
	 * user data.
	 * 
	 * @return <code>String</code> with genquery select statement that obtains
	 *         user data.
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
	 * Given a query build from the <code>buildUserSelects</code>, create a user
	 * for a given result row
	 * 
	 * @param row
	 *            {@link IRODSQueryResultRow}
	 * @param irodsGenQueryExecutor
	 *            {@link IRODSGenQueryExecutor} access object to do various
	 *            queries
	 * @param retrieveDN
	 *            <code>boolean</code> that causes an additional lookup to
	 *            retrieve the Distinguished Name. <code>false</code> avoids an
	 *            additional lookup per user.
	 * @return {@link User} built from that row result
	 * @throws JargonException
	 */
	public static User buildUserFromResultSet(final IRODSQueryResultRow row,
			final IRODSGenQueryExecutor irodsGenQueryExecutor,
			final boolean retrieveDN) throws JargonException {
		String homeZone = irodsGenQueryExecutor.getIRODSAccount().getZone();
		User user = new User();
		user.setId(row.getColumn(2));
		user.setName(row.getColumn(1));
		user.setUserType(UserTypeEnum.findTypeByString(row.getColumn(3)));
		user.setZone(row.getColumn(0));
		user.setInfo(row.getColumn(4));
		user.setComment(row.getColumn(5));
		user.setCreateTime(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(6)));
		user.setModifyTime(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(7)));

		if (user.getZone().equals(homeZone)) {
			user.setNameWithZone(user.getName());
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(user.getName());
			sb.append('#');
			sb.append(user.getZone());
			user.setNameWithZone(sb.toString());
		}

		// do add'l lookup of DN if requested
		if (retrieveDN) {
			String userDn = findUserDnIfExists(user.getId(),
					irodsGenQueryExecutor);
			if (userDn != null) {
				user.setUserDN(userDn);
			}
		}

		return user;
	}

	/**
	 * Locate the user distinguished name, if it exists
	 * 
	 * @param userId
	 * @param irodsGenQueryExecutor
	 * @return <code>String</code> with the distinguished name for the user, if
	 *         it exists, otherwise, <code>null</code> will be returned
	 * @throws JargonException
	 */
	public static String findUserDnIfExists(final String userId,
			final IRODSGenQueryExecutor irodsGenQueryExecutor)
			throws JargonException {
		String dn = null;

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

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(userQueryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 500);
		} catch (JargonQueryException e) {
			throw new JargonException(e);
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
	 * Given a user name that might be in the format user#zone, get the user
	 * part
	 * 
	 * @param userName
	 *            <code>String</code> with the user name, possibly in user#zone
	 *            format
	 * @return <code>String</code> with only the user name, not the zone
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
	 * Given a user name that might be in the format user#zone, get the zone
	 * part
	 * 
	 * @param userName
	 *            <code>String</code> with the user name, possibly in user#zone
	 *            format
	 * @return <code>String</code> with only the zone name, not the user
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
