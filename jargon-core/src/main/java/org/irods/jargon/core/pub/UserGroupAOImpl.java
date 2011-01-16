/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access operations for IRODS user groups.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class UserGroupAOImpl extends IRODSGenericAO implements
		UserGroupAO {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static final char COMMA = ',';

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected UserGroupAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
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

		log.info("finding user group with id: {}", userGroupId);

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		query.append(" = '");
		query.append(userGroupId.trim());
		query.append("'");

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQuery(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
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
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findByName(java.lang.String)
	 */
	@Override
	public UserGroup findByName(final String userGroupName)
			throws JargonException {

		if (userGroupName == null || userGroupName.length() == 0) {
			throw new JargonException("null or missing userGroupName");
		}

		log.info("finding user group with name: {}", userGroupName);
		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(" = '");
		query.append(userGroupName.trim());
		query.append("'");

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQuery(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		if (resultSet.getResults().size() == 0) {
			log.info("no user group found");
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user group found for name:");
			messageBuilder.append(userGroupName);
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
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findWhere(java.lang.String)
	 */
	@Override
	public List<UserGroup> findWhere(final String whereClause)
			throws JargonException, JargonQueryException {

		if (whereClause == null || whereClause.length() == 0) {
			throw new JargonException("null or missing where clause");
		}

		log.info("find user group with provided where clause: {}", whereClause);

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(whereClause);

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQuery(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findUserGroupsForUser(java
	 * .lang.String)
	 */
	@Override
	public List<UserGroup> findUserGroupsForUser(final String userName)
			throws JargonException {
		if (userName == null || userName.length() == 0) {
			throw new JargonException("null or missing userName");
		}

		log.info("find user group with user name: {}", userName);

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" WHERE "); // FIXME: no where causes NPE
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(" = '");
		query.append(userName.trim());
		query.append("'");
		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQuery(irodsQuery,
					0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	private String buildUserGroupSelects() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		return query.toString();
	}

	private UserGroup buildUserGroupFromResultSet(final IRODSQueryResultRow row)
			throws JargonException {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(row.getColumn(0));
		userGroup.setUserGroupName(row.getColumn(1));

		return userGroup;
	}

}
