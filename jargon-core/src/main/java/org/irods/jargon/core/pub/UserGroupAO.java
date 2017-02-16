package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.JargonQueryException;

/**
 * Interface for an access object dealing with iRODS user groups. Includes
 * methods to obtain information on, and to manage iRODS user groups.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
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
	 * @return <code>List</code> of {@link UserGroup}
	 * @throws JargonException
	 */
	List<UserGroup> findUserGroupsForUser(String userName)
			throws JargonException;

	/**
	 * Add the given user group to iRODS
	 *
	 * @param userGroup
	 *            {@link UserGroup} to add
	 * @throws DuplicateDataException
	 *             if user already exists
	 * @throws JargonException
	 */
	void addUserGroup(UserGroup userGroup) throws DuplicateDataException,
			JargonException;

	/**
	 * Remove the given user group from iRODS. Note that if the user group is
	 * not found, a warning is logged, and the exception is ignored.
	 *
	 * @param userGroup
	 *            {@link UserGroup} to remove
	 * @throws JargonException
	 */
	void removeUserGroup(UserGroup userGroup) throws JargonException;

	/**
	 * List the <code>User</code>s that are members of an iRODS
	 * <code>UserGroup</code>.
	 *
	 * @param userGroupName
	 *            <code>String<code> with the name of an iRODS user group
	 * @return <code>List<code> of {@link User} with the group membership. This
	 *         will be an empty <code>List</code> if the group has no members.
	 * @throws JargonException
	 */
	List<User> listUserGroupMembers(String userGroupName)
			throws JargonException;

	/**
	 * Add the given user to the iRODS user group
	 *
	 * @param userGroupName
	 *            <code>String</code> with the name of the iRODS user group.
	 *            This group must exist.
	 * @param userName
	 *            <code>String</code> with the name of the iRODS user to add to
	 *            the group. This user must exist.
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone for the
	 *            user. This is optional and may be set to blank or
	 *            <code>null</code> if not needed.
	 * @throws DuplicateDataException
	 *             if the user is already a group member
	 * @throws InvalidGroupException
	 * @throws InvalidUserException
	 * @throws JargonException
	 */
	void addUserToGroup(String userGroupName, String userName, String zoneName)
			throws InvalidGroupException, InvalidUserException, JargonException;

	/**
	 * Remove the given user (with optional zone) from the given group. If the
	 * user is valid but not in group, the method will return normally.
	 *
	 * @param userGroupName
	 *            <code>String</code> with the name of the iRODS user group.
	 * @param userName
	 *            <code>String</code> with the name of the iRODS user to add to
	 *            the group.
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone for the
	 *            user. This is optional and may be set to blank or
	 *            <code>null</code> if not needed.
	 * @throws InvalidUserException
	 * @throws InvalidGroupException
	 * @throws JargonException
	 */
	void removeUserFromGroup(String userGroupName, String userName,
			String zoneName) throws InvalidUserException,
			InvalidGroupException, JargonException;

	/**
	 * List all user groups
	 *
	 * @return <code>List</code> of {@link UserGroup}
	 * @throws JargonException
	 */
	List<UserGroup> findAll() throws JargonException;

	/**
	 * Query the ICAT and see if the given user is in the given group
	 *
	 * @param userName
	 *            <code>String</code> with the user name
	 * @param groupName
	 *            <code>String</code> with the group name
	 * @return <code>boolean</code> which will be <code>true</code> if the user
	 *         is in the given group
	 * @throws JargonException
	 */
	boolean isUserInGroup(String userName, String groupName)
			throws JargonException;

	/**
	 * Handy method to remove a user group in the current zone by simply giving
	 * the user group name. This method will treat a non-existent group as if it
	 * had been deleted, logging this situation and proceeding.
	 *
	 * @param userGroupName
	 *            <code>String</code> with the name of the user group to delete.
	 * @throws JargonException
	 */
	void removeUserGroup(String userGroupName) throws JargonException;

}