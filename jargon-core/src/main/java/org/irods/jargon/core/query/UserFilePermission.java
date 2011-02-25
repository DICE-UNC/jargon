package org.irods.jargon.core.query;

import org.irods.jargon.core.protovalues.FilePermissionEnum;

/**
 * Represents a user's permission on a file or collection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserFilePermission {

	private String userName = "";
	private String userId = "";
	private FilePermissionEnum filePermissionEnum;
	
	/**
	 * Public values constructor.
	 * @param userName <code>String</code> with the name of the user
	 * @param userId <code>String</code> with the id of the user
	 * @param filePermissionEnum <code>FilePermission</code> for the given user
	 */
	public UserFilePermission(String userName, String userId,
			FilePermissionEnum filePermissionEnum) {
		
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		
		if (userId == null || userId.isEmpty()) {
			throw new IllegalArgumentException("null or empty userId");
		}
		
		if (filePermissionEnum == null) {
			throw new IllegalArgumentException("null filePermissionEnum");
		}
		
		this.userName = userName;
		this.userId = userId;
		this.filePermissionEnum = filePermissionEnum;
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
		return sb.toString();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public FilePermissionEnum getFilePermissionEnum() {
		return filePermissionEnum;
	}

	public void setFilePermissionEnum(FilePermissionEnum filePermissionEnum) {
		this.filePermissionEnum = filePermissionEnum;
	}

}
