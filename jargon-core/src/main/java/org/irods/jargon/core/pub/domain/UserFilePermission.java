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
	 * Public values constructor.
	 * 
	 * @param userName
	 *            <code>String</code> with the name of the user
	 * @param userId
	 *            <code>String</code> with the id of the user
	 * @param filePermissionEnum
	 *            {@link FilePermissionEnum} for the given user
	 * @param userType
	 *            {@link UserTypeEnum} value for user
	 */
	public UserFilePermission(final String userName, final String userId,
			final FilePermissionEnum filePermissionEnum,
			final UserTypeEnum userType) {

		this(userName, userId, filePermissionEnum, userType, "");
	}

	/**
	 * Public values constructor.
	 * 
	 * @param userName
	 *            <code>String</code> with the name of the user
	 * @param userId
	 *            <code>String</code> with the id of the user
	 * @param filePermissionEnum
	 *            {@link FilePermissionEnum} for the given user
	 * @param userType
	 *            {@link UserTypeEnum} value for user
	 * @param userZone
	 *            <code>String</code> with an optional zone name, set to blank
	 *            if not used
	 */
	public UserFilePermission(final String userName, final String userId,
			final FilePermissionEnum filePermissionEnum,
			final UserTypeEnum userType, final String userZone) {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (userId == null || userId.isEmpty()) {
			throw new IllegalArgumentException("null or empty userId");
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
	 * Get the name of the user
	 * 
	 * @return <code>String</code> with the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set the name of the user
	 * 
	 * @param userName
	 *            <code>String</code> with the name of the user
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * Get the unique user id database key
	 * 
	 * @return <code>String</code> with the user id primary key value
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the unique user id primary key
	 * 
	 * @param userId
	 *            <code>String</code> user id to set
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Get the file permission for this user
	 * 
	 * @return {@link FilePermissionEnum} value with the current permission for
	 *         the user
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
	public void setFilePermissionEnum(
			final FilePermissionEnum filePermissionEnum) {
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

	public void setUserZone(String userZone) {
		this.userZone = userZone;
	}

	/**
	 * Get the user name in user#zone format if from another zone. An optional
	 * <code>homeZoneName</code> parameter can be supplied so that users in the
	 * home zone do not have the #zone appended. This is appropriate for display
	 * in interfaces.
	 * 
	 * @param homeZoneName
	 *            <code>String</code> with the home zone used to filter the
	 *            formatted zone display. This may be <code>null</code> or
	 *            blank, which results in all users having the #zone appended.
	 * @return <code>String</code> with the format of user#zone
	 */
	public String getConcatenatedUserAndZone(final String homeZoneName) {
		StringBuilder sb = new StringBuilder(userName);
		if (!userZone.isEmpty()) {
			if (homeZoneName == null || homeZoneName.isEmpty()) {
				sb.append('#');
				sb.append(userZone);
			} else {
				if (!homeZoneName.equals(userZone)) {
					sb.append('#');
					sb.append(userZone);
				}
			}
		}
		return sb.toString();
	}

}
