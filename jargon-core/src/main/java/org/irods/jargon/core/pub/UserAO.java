package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;

/**
 * Interface for an object to interact with user data in IRODS.
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public interface UserAO extends IRODSAccessObject {

	/**
	 * Add the given user to iRODS
	 * 
	 * @param user
	 *            {@link org.irods.jargon.core.pub.domain.User} with information
	 *            on the user to be added.
	 * @throws JargonException
	 * @throws DuplicateDataException
	 *             thrown if the user already exists.
	 */
	void addUser(User user) throws JargonException, DuplicateDataException;

	/**
	 * List all users.
	 * 
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.User}
	 * @throws JargonException
	 */
	List<User> findAll() throws JargonException;

	/**
	 * Query users and return the <code>User</code> object with the given user
	 * name
	 * 
	 * @param name
	 *            <code>String</code> with the name of the user to query.
	 * @return {@link org.irods.jargon.core.pub.domain.User} that is the result
	 *         of the query
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if user does not exist.
	 */
	User findByName(String name) throws JargonException, DataNotFoundException;

	/**
	 * Query users by the unique id assigned by iRODS (database unique key).
	 * 
	 * @param userId
	 *            <code>String</code> with the unique database key for the user.
	 * @return {@link org.irods.jargon.core.pub.domain.User}
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if the user does not exist
	 */
	User findById(final String userId) throws JargonException,
			DataNotFoundException;

	/**
	 * Query the AVU metadata associated with the given user by Id.
	 * 
	 * @param userId
	 *            <code>String</code> with the unique database key for the user.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.AvuData} with query
	 *         results.
	 * @throws JargonException
	 */
	List<AvuData> listUserMetadataForUserId(String userId)
			throws JargonException;

	/**
	 * Query the AVU metadata associated with the given user by user name.
	 * 
	 * @param userName
	 *            <code>String</code> with the user name for the user.
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.AvuData} with query
	 *         results.
	 * @throws JargonException
	 */
	List<AvuData> listUserMetadataForUserName(String userName)
			throws JargonException;

	/**
	 * Remove the user from iRODS.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name to be removed.
	 * @throws InvalidUserException
	 *             if the user is not in iRODS
	 * @throws JargonException
	 */
	void deleteUser(String userName) throws InvalidUserException,
			JargonException;

	/**
	 * Update the user data. Note that this method only updates certain
	 * accessible fields. The method will compare the provided user data with
	 * the current data within iRODS, and will update the deltas.
	 * 
	 * @param user
	 *            {@link org.irods.jargon.core.pub.domain.User} with the updated
	 *            data.
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if user is not found.
	 */
	void updateUser(User user) throws JargonException, DataNotFoundException;

	/**
	 * Select user objects given an arbitrary 'where' clause.
	 * 
	 * @param whereStatement
	 *            <code>String</code> containing iquest syntax for a where
	 *            statement, does not include the actual <code>Where</code>
	 * @return <code>List<User></code> containing users that match the given
	 *         query
	 * @throws JargonException
	 */
	List<User> findWhere(String whereStatement) throws JargonException;

	/**
	 * Change the password for the given user. This method is used for a user
	 * changing their own password. Jargon will use an obfuscation routine to
	 * send the new password to iRODS.
	 * 
	 * @param userName
	 *            <code>String</code> with the name of the user.
	 * @param currentPassword
	 *            <code>String</code> with the password that currently exists.
	 * @param newPassword
	 *            <code>String</code> with the new password value.
	 * @throws JargonException
	 */
	void changeAUserPasswordByThatUser(String userName, String currentPassword,
			String newPassword) throws JargonException;

	/**
	 * Change the password for a given user. This method is used by an admin
	 * setting the password for an arbitrary user. For a user changing their own
	 * password, use the <code>changeAPasswordByThatUser(String, String)</code>
	 * method
	 * 
	 * @param userName
	 *            <code>String</code> with the user name whose password will
	 *            change.
	 * @param newPassword
	 *            <code>String</code> with the password to set for the given
	 *            user.
	 * @throws JargonException
	 */
	void changeAUserPasswordByAnAdmin(String userName, String newPassword)
			throws JargonException;

	/**
	 * Add the AVU metadata for the given user. This is only possible when a
	 * rods admin.
	 * 
	 * @param userName
	 *            <code>String</code> with the user name to whom the AVU
	 *            metadata will be added
	 * @param avuData
	 *            {@link AvuData} to be added for the user
	 * @throws JargonException
	 */
	void addAVUMetadata(String userName, AvuData avuData)
			throws JargonException;

	/**
	 * Remove the given AVU metadata from the user. This is only possible when a
	 * rods admin.
	 * 
	 * @param userName
	 *            <code>String</code> with the user name from whom the AVU
	 *            metadata will be removed
	 * @param avuData
	 *            {@link AvuData} to be removed from the user
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	void deleteAVUMetadata(String userName, AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * Modify the given AVU metadata from the user. This is only possible when a
	 * rods admin.
	 * 
	 * @param userName
	 *            <code>String</code> with the user name from whom the AVU
	 *            metadata will be removed
	 * @param avuData
	 *            {@link AvuData} to be modified
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	void modifyAVUMetadata(String userName, AvuData avuData)
			throws DataNotFoundException, JargonException;

	/**
	 * For a given partial user name, return the user names that are like that
	 * one. This is handy for creating auto-complete data entry components that
	 * need to do quick user name lookups. If actual <code>User</code> domain
	 * objects are needed, the <code>findWhere()</code> method provides an easy
	 * shortcut for obtaining extended user data. This method will do a 'LIKE'
	 * query and add a '%' wild card to the provided term
	 * 
	 * @param userName
	 * @return <code>List<String></code> that are the user names that match the
	 *         partial query
	 * @throws JargonException
	 */
	List<String> findUserNameLike(String userName) throws JargonException;

	/**
	 * Generate a temporary password for the connected user
	 * 
	 * @return <code>String</code> with the temporary password
	 * @throws JargonException
	 */
	String getTemporaryPasswordForConnectedUser() throws JargonException;

	/**
	 * Given a unique numeric user ID, retrieve the user's distinguished name.
	 * Note that the various list methods do not retrieve the DN by default, as
	 * it causes unnecessary GenQueries to be issued per user. This method can
	 * retrieve that data as needed.
	 * <p/>
	 * The methods that retrieve an individual user do retrieve the DN by
	 * default.
	 * 
	 * @param userId
	 *            <code>String</code> with the iRODS user id (not name)
	 * @return <code>String</code> with the user DN, or <code>null</code> if the
	 *         DN does not exist for the user
	 * @throws JargonException
	 */
	String retriveUserDNByUserId(String userId) throws JargonException;

}
