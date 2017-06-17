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
	 * @return {@link User} that is the result for updates
	 * @throws JargonException
	 * @throws DuplicateDataException
	 *             thrown if the user already exists.
	 */
	User addUser(User user) throws JargonException, DuplicateDataException;

	/**
	 * List all users.
	 *
	 * @return {@code List} of
	 *         {@link org.irods.jargon.core.pub.domain.User}
	 * @throws JargonException
	 */
	List<User> findAll() throws JargonException;

	/**
	 * Query users and return the {@code User} object with the given user
	 * name. Note that user names may be given in user#zone format, and that
	 * federated user registered on the current zone will be returned.
	 * <p>
	 * For example, if I have zone1 and zone2, and zone1 has registered
	 * user1#zone2 as a user in zone1, then this method will get the information
	 * that zone1 has on the user name user#zone2.
	 * <p>
	 * This is distinct from going to zone2, and asking for information on the
	 * user user1#zone2.
	 *
	 * @param name
	 *            {@code String} with the name of the user to query.
	 * @return {@link org.irods.jargon.core.pub.domain.User} that is the result
	 *         of the query
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if user does not exist.
	 */
	User findByName(String name) throws JargonException, DataNotFoundException;

	/**
	 * Query users by the unique id assigned by iRODS (database unique key).
	 * This will default to searching the current zone
	 *
	 * @param userId
	 *            {@code String} with the unique database key for the user.
	 * @return {@link org.irods.jargon.core.pub.domain.User}
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if the user does not exist
	 */
	User findById(final String userId) throws JargonException,
	DataNotFoundException;

	/**
	 * Query users by the unique id assigned by iRODS (database unique key) in a
	 * zone.
	 * <p>
	 * This will, if the given zone is not the same as the current zone,
	 * initiate a cross-zone query and retrieve the information from the given
	 * zone name.
	 *
	 * @param userId
	 *            {@code String} with the unique database key for the user.
	 * @return {@link org.irods.jargon.core.pub.domain.User}
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             if the user does not exist
	 */
	User findByIdInZone(String userId, String zone) throws JargonException,
	DataNotFoundException;

	/**
	 * Query the AVU metadata associated with the given user by Id.
	 *
	 * @param userId
	 *            {@code String} with the unique database key for the user.
	 * @return {@code List} of
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
	 *            {@code String} with the user name for the user.
	 * @return {@code List} of
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
	 *            {@code String} with the iRODS user name to be removed.
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
	 *            {@code String} containing iquest syntax for a where
	 *            statement, does not include the actual {@code Where}
	 * @return {@code List<User>} containing users that match the given
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
	 *            {@code String} with the name of the user.
	 * @param currentPassword
	 *            {@code String} with the password that currently exists.
	 * @param newPassword
	 *            {@code String} with the new password value.
	 * @throws JargonException
	 */
	void changeAUserPasswordByThatUser(String userName, String currentPassword,
			String newPassword) throws JargonException;

	/**
	 * Change the password for a given user. This method is used by an admin
	 * setting the password for an arbitrary user. For a user changing their own
	 * password, use the {@code changeAPasswordByThatUser(String, String)}
	 * method
	 *
	 * @param userName
	 *            {@code String} with the user name whose password will
	 *            change.
	 * @param newPassword
	 *            {@code String} with the password to set for the given
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
	 *            {@code String} with the user name to whom the AVU
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
	 *            {@code String} with the user name from whom the AVU
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
	 *            {@code String} with the user name from whom the AVU
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
	 * need to do quick user name lookups. If actual {@code User} domain
	 * objects are needed, the {@code findWhere()} method provides an easy
	 * shortcut for obtaining extended user data. This method will do a 'LIKE'
	 * query and add a '%' wild card to the provided term
	 *
	 * @param userName
	 * @return {@code List<String>} that are the user names that match the
	 *         partial query
	 * @throws JargonException
	 */
	List<String> findUserNameLike(String userName) throws JargonException;

	/**
	 * Generate a temporary password for the connected user. Password validity
	 * times and number of connections will be set by the iRODS server.
	 *
	 * @return {@code String} with the temporary password
	 * @throws JargonException
	 */
	String getTemporaryPasswordForConnectedUser() throws JargonException;

	/**
	 * Generate a temporary password for another user. Password validity times
	 * and number of connections will be set by the iRODS server.
	 * <p>
	 * This is a rodsadmin only function, and was added post iRODS 3.0.
	 *
	 * @param targetUserName
	 *            {@code String} (required) with the user name for which
	 *            the temporary password will be issued
	 * @return {@code String} with the temporary password
	 * @throws JargonException
	 */
	String getTemporaryPasswordForASpecifiedUser(String targetUserName)
			throws JargonException;

	/**
	 * Given a unique numeric user ID, retrieve the user's distinguished name.
	 * Note that the various list methods do not retrieve the DN by default, as
	 * it causes unnecessary GenQueries to be issued per user. This method can
	 * retrieve that data as needed.
	 * <p>
	 * The methods that retrieve an individual user do retrieve the DN by
	 * default.
	 *
	 * @param userId
	 *            {@code String} with the iRODS user id (not name)
	 * @return {@code String} with the user DN, or {@code null} if the
	 *         DN does not exist for the user
	 * @throws JargonException
	 */
	String retriveUserDNByUserId(String userId) throws JargonException;

	/**
	 * Update the user info field for the user as a discrete operation.
	 *
	 * @param userName
	 *            {@code String} with the name of the user
	 * @param userInfo
	 *            {@code String} with the info to set, it can be blank, but
	 *            not {@code null}
	 * @throws DataNotFoundException
	 *             if the user is not found
	 * @throws JargonException
	 */
	void updateUserInfo(String userName, String userInfo)
			throws DataNotFoundException, JargonException;

	/**
	 * Update the DN for the given user
	 *
	 * @param userName
	 *            userName {@code String} with the name of the user
	 * @param userDN
	 *            {@code DN} to add to the user
	 * @throws InvalidUserException
	 *             if the user does not exist
	 * @throws JargonException
	 */
	void updateUserDN(String userName, String userDN)
			throws InvalidUserException, JargonException;

	/**
	 * Remove the DN for the given user. If the user or DN does not exist, it
	 * will silently ignore the command
	 *
	 * @param userName
	 *            userName {@code String} with the name of the user
	 * @param userDN
	 *            {@code DN} to remove from the user
	 * @throws JargonException
	 */
	void removeUserDN(String userName, String userDN) throws JargonException;

}
