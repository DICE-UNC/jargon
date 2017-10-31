package org.irods.jargon.datautils.synchproperties;

import java.util.Map;

/**
 * Describes synch configuration for a given iRODS user.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserSynchProfile {

	/**
	 * iRODS user name for this profile. This will be stored in the relevant
	 * iRODS server
	 */
	private String userName = "";

	/**
	 * Map (by device name assigned by user) of devices on this iRODS server
	 * that are synched
	 */
	private Map<String, UserSynchDeviceProfile> userSynchDevices;

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public Map<String, UserSynchDeviceProfile> getUserSynchDevices() {
		return userSynchDevices;
	}

	public void setUserSynchDevices(
			final Map<String, UserSynchDeviceProfile> userSynchDevices) {
		this.userSynchDevices = userSynchDevices;
	}

}
