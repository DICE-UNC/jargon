/**
 * 
 */
package org.irods.jargon.userprofile;

/**
 * Represents profile information kept about iRODS users. This is divided into
 * public and protected fields. It is up to the profile service to enforce the
 * necessary protection levels for this data.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserProfile {

	private String zone = "";
	private String userName = "";
	private UserProfilePublicFields userProfilePublicFields = new UserProfilePublicFields();
	private UserProfileProtectedFields userProfileProtectedFields = new UserProfileProtectedFields();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserProfile:");
		sb.append("\n   zone:");
		sb.append(zone);
		sb.append("\n   userName:");
		sb.append(userName);
		sb.append(userProfilePublicFields);
		sb.append(userProfileProtectedFields);
		return sb.toString();
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(final String zone) {
		this.zone = zone;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userProfilePublicFields
	 */
	public UserProfilePublicFields getUserProfilePublicFields() {
		return userProfilePublicFields;
	}

	/**
	 * @param userProfilePublicFields
	 *            the userProfilePublicFields to set
	 */
	public void setUserProfilePublicFields(
			final UserProfilePublicFields userProfilePublicFields) {
		this.userProfilePublicFields = userProfilePublicFields;
	}

	/**
	 * @return the userProfileProtectedFields
	 */
	public UserProfileProtectedFields getUserProfileProtectedFields() {
		return userProfileProtectedFields;
	}

	/**
	 * @param userProfileProtectedFields
	 *            the userProfileProtectedFields to set
	 */
	public void setUserProfileProtectedFields(
			final UserProfileProtectedFields userProfileProtectedFields) {
		this.userProfileProtectedFields = userProfileProtectedFields;
	}

}
