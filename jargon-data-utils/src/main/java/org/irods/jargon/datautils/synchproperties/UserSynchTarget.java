package org.irods.jargon.datautils.synchproperties;

/**
 * Describes an iRODS Synch target for a device. A target is essentially a
 * directory on iRODS that relates to a corresponding directory on the local
 * file system of the target device. This synch information is applicable to one
 * iRODS zone.
 * <p>
 * iRODS stores information about this synch folder, including the last
 * synchronization time stamps from the perspective of both the device, and
 * iRODS, so that differences can be noted.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserSynchTarget {

	/**
	 * iRODS account user name
	 */
	private String userName = "";

	/**
	 * Name of the synchronizing device
	 */
	private String deviceName = "";

	/**
	 * Absolute path to the top level collection that represents the iRODS synch
	 * data
	 */
	private String irodsSynchRootAbsolutePath = "";

	/**
	 * Absolute path to the local directory that represents the local synch data
	 */
	private String localSynchRootAbsolutePath = "";

	/**
	 * Time stamp from the local clock with the last synchronization point, or 0
	 * if no synch has been done
	 */
	private long lastLocalSynchTimestamp = 0;

	/**
	 * Time stamp with the irods clock with the last synchronization point, or 0
	 * if no synch has been done
	 */
	private long lastIRODSSynchTimestamp = 0;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nUserSynchTarget");
		sb.append("\n   userName:");
		sb.append(userName);
		sb.append("\n    deviceName:");
		sb.append(deviceName);
		sb.append("\n   irodsAbsPath:");
		sb.append(irodsSynchRootAbsolutePath);
		sb.append("\n   localSynchRootAbsolutePath:");
		sb.append(localSynchRootAbsolutePath);
		sb.append("\n   lastIrodsSynchTimeStamp:");
		sb.append(lastIRODSSynchTimestamp);
		sb.append("\n   lastLocalSynchTimeStamp:");
		sb.append(lastLocalSynchTimestamp);
		return sb.toString();
	}

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

	public String getIrodsSynchRootAbsolutePath() {
		return irodsSynchRootAbsolutePath;
	}

	public void setIrodsSynchRootAbsolutePath(
			final String irodsSynchRootAbsolutePath) {
		this.irodsSynchRootAbsolutePath = irodsSynchRootAbsolutePath;
	}

	public String getLocalSynchRootAbsolutePath() {
		return localSynchRootAbsolutePath;
	}

	public void setLocalSynchRootAbsolutePath(
			final String localSynchRootAbsolutePath) {
		this.localSynchRootAbsolutePath = localSynchRootAbsolutePath;
	}

	public long getLastLocalSynchTimestamp() {
		return lastLocalSynchTimestamp;
	}

	public void setLastLocalSynchTimestamp(final long lastLocalSynchTimestamp) {
		this.lastLocalSynchTimestamp = lastLocalSynchTimestamp;
	}

	public long getLastIRODSSynchTimestamp() {
		return lastIRODSSynchTimestamp;
	}

	public void setLastIRODSSynchTimestamp(final long lastIRODSSynchTimestamp) {
		this.lastIRODSSynchTimestamp = lastIRODSSynchTimestamp;
	}

}
