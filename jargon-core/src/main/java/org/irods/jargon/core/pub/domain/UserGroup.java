/**
 * 
 */
package org.irods.jargon.core.pub.domain;

/**
 * * A user group in the IRODS system. This class is not immutable, and should
 * not be shared between threads.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserGroup extends IRODSDomainObject {
	private String userGroupId = "";
	private String userGroupName = "";

	public UserGroup() {

	}

	public String getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(final String userGroupId) {
		this.userGroupId = userGroupId;
	}

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(final String userGroupName) {
		this.userGroupName = userGroupName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("user group\n");
		sb.append("    id:");
		sb.append(userGroupId);
		sb.append("\n    name:");
		sb.append(userGroupName);
		return sb.toString();
	}

}
