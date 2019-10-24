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
	/**
	 * iCat unique id for the group
	 */
	private String userGroupId = "";
	/**
	 * group name (sans the # zone decoration for groups originating in a federated
	 * zone)
	 */
	private String userGroupName = "";
	/**
	 * Zone of the user that comprises the 'user#zone' format. This denotes a user
	 * in a foreign zone who has been granted access in this icat zone.
	 */
	private String zone = "";
	/**
	 * Denotes the zone perspective of this user group. This is not the same as the
	 * {@code zone} in this same structure. The {@code icatZone} gives the zone that
	 * was queried to get the user. This allows for groups to be brought together by
	 * querying multiple zones.
	 */
	private String icatZone = "";

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
		StringBuilder builder = new StringBuilder();
		builder.append("UserGroup [");
		if (userGroupId != null) {
			builder.append("userGroupId=").append(userGroupId).append(", ");
		}
		if (userGroupName != null) {
			builder.append("userGroupName=").append(userGroupName).append(", ");
		}
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}
		if (icatZone != null) {
			builder.append("icatZone=").append(icatZone);
		}
		builder.append("]");
		return builder.toString();
	}

	public String getZone() {
		return zone;
	}

	public void setZone(final String zone) {
		this.zone = zone;
	}

	public String getIcatZone() {
		return icatZone;
	}

	public void setIcatZone(String icatZone) {
		this.icatZone = icatZone;
	}

}
