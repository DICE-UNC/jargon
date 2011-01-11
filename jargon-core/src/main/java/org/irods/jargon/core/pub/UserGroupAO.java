package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.JargonQueryException;

public interface UserGroupAO extends IRODSAccessObject {

	/**
	 * Given a user group id, return the <code>UserGroup</code>, or return null
	 * if not found
	 * 
	 * @param userGroupId
	 *            <code>String</code> with the numeric key for the user group
	 * @return <code>UserGroup</code> domain object
	 * @throws JargonException
	 */
	UserGroup find(final String userGroupId) throws JargonException;

	/**
	 * Given a user name, return the <code>UserGroup</code>, or return null if
	 * not found
	 * 
	 * @param userGroupName
	 *            <code>String</code> with the name of the user group
	 * @return <code>UserGroup</code> domain object
	 * @throws JargonException
	 */
	UserGroup findByName(final String userGroupName) throws JargonException;

	/**
	 * Provides a convenient way to query for <code>UserGroup</code>s using the
	 * provided 'WHERE" clause. In usage, provide an iquest compatable condition
	 * omitting the proceeding 'Where" statement. An empty <code>List</code>
	 * will be returned if no matches were found.
	 * 
	 * @param whereClause
	 *            <code>String</code> containing the iquest compatable
	 *            condition, omitting the "WHERE" token.
	 * @return <code>List<UserGroup></code> containing the UserGroups that match
	 *         the given query
	 * @throws JargonException
	 * @throws JargonQueryException
	 *             indicates some syntax exception in the provided where clause
	 */
	List<UserGroup> findWhere(String whereClause) throws JargonException,
			JargonQueryException;

	/**
	 * Given a user name, return the user groups that the given user belongs to,
	 * or an empty <code>List</code> when no user groups are found.
	 * 
	 * @param userName
	 *            <code>String</code> with an IRODS user name
	 * @return
	 * @throws JargonException
	 */
	List<UserGroup> findUserGroupsForUser(String userName)
			throws JargonException;

}