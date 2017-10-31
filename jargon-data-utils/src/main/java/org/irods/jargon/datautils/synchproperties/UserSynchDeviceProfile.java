package org.irods.jargon.datautils.synchproperties;

import java.util.Map;

/**
 * Describes the synchronization profile for a device (like a laptop,
 * workstation, or other device) for a user.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserSynchDeviceProfile {

	/**
	 * iRODS user name that owns this device
	 */
	private String userName = "";

	/**
	 * User assigned device name (laptop, desktop, iphone, etc)
	 */
	private String deviceName = "";

	/**
	 * A map (by iRODS absolute path to the root collection of the synch) of
	 * synch folders on iRODS.
	 */
	private Map<String, UserSynchTarget> userSynchTargets;

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(final String deviceName) {
		this.deviceName = deviceName;
	}

	public Map<String, UserSynchTarget> getUserSynchTargets() {
		return userSynchTargets;
	}

	public void setUserSynchTargets(
			final Map<String, UserSynchTarget> userSynchTargets) {
		this.userSynchTargets = userSynchTargets;
	}

}
