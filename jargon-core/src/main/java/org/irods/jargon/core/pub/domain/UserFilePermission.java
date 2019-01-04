package org.irods.jargon.core.pub.domain;

import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;

/**
 * Represents a user's permission on a file or collection. This is a POJO domain
 * object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserFilePermission {

	private String userName = "";
	private String userZone = "";
	private String userId = "";
	private UserTypeEnum userType = UserTypeEnum.RODS_UNKNOWN;
	private FilePermissionEnum filePermissionEnum;

	/**
	 * Default (no-values) constructor
	 */
	public UserFilePermission() {

	}

	/**
	 * Public values constructor.
	 *
	 * @param userName
	 *            {@code String} with the name of the user (no #zone)
	 * @param userId
	 *            {@code String} with the id of the user
	 * @param filePermissionEnum
	 *            {@link FilePermissionEnum} for the given user
	 * @param userType
	 *            {@link UserTypeEnum} value for user
	 * @param userZone
	 *            {@code String} with an optional zone name, set to blank if not
	 *            used
	 */
	public UserFilePermission(final String userName, final String userId, final FilePermissionEnum filePermissionEnum,
			final UserTypeEnum userType, final String userZone) {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (filePermissionEnum == null) {
			throw new IllegalArgumentException("null filePermissionEnum");
		}

		if (userType == null) {
			throw new IllegalArgumentException("null userType");
		}

		if (userZone == null) {
			throw new IllegalArgumentException("null userZone");
		}

		if (userId.indexOf('#') > -1) {
			throw new IllegalArgumentException(
					"this constructor does not take user#zone format, a separate parameter takes the zone");
		}

		this.userName = userName;
		this.userId = userId;
		this.filePermissionEnum = filePermissionEnum;
		this.userType = userType;
		this.userZone = userZone;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nUserFilePermission");
		sb.append("\n    userName:");
		sb.append(userName);
		sb.append("\n    userId:");
		sb.append(userId);
		sb.append("\n    filePermissionEnum:");
		sb.append(filePermissionEnum);
		sb.append("\n   userType:");
		sb.append(userType);
		sb.append("\n   userZone:");
		sb.append(userZone);
		return sb.toString();
	}

	/**
	 * Get the name of the user in user#zone format
	 *
	 * @return {@code String} with the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set the name of the user
	 *
	 * @param userName
	 *            {@code String} with the name of the user
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * Get the user name in the standard user#zone format. This is the standard for
	 * displays of user names in iRODS user interfaces.
	 *
	 * @return {@code String} with the extended zone
	 */
	public String getNameWithZone() {
		StringBuilder sb = new StringBuilder();
		sb.append(userName);
		sb.append('#');
		sb.append(userZone);
		return sb.toString();
	}

	/**
	 * Get the unique user id database key
	 *
	 * @return {@code String} with the user id primary key value
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the unique user id primary key
	 *
	 * @param userId
	 *            {@code String} user id to set
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Get the file permission for this user
	 *
	 * @return {@link FilePermissionEnum} value with the current permission for the
	 *         user
	 */
	public FilePermissionEnum getFilePermissionEnum() {
		return filePermissionEnum;
	}

	/**
	 * Set the file permission for the user
	 *
	 * @param filePermissionEnum
	 *            {@link FilePermissionEnum} value
	 */
	public void setFilePermissionEnum(final FilePermissionEnum filePermissionEnum) {
		this.filePermissionEnum = filePermissionEnum;
	}

	/**
	 * Get the type of user for this user
	 *
	 * @return {@link UserTypeEnum}
	 */
	public synchronized UserTypeEnum getUserType() {
		return userType;
	}

	/**
	 * Set the type of user for this user
	 *
	 * @param userType
	 *            {@link UserTypeEnum}
	 */
	public synchronized void setUserType(final UserTypeEnum userType) {
		this.userType = userType;
	}

	public String getUserZone() {
		return userZone;
	}

	public void setUserZone(final String userZone) {
		this.userZone = userZone;
	}

}
