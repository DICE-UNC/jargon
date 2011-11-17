package org.irods.jargon.core.pub.domain;

/**
 * Represents information about remote commands in the iRODS server/cmd/bin directory.  These commands
 * are executable by the {@link RemoteExecutionOfCommandsAO}.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RemoteCommandInformation {
	private String hostName = "";
	private String zone = "";
	private String command = "";
	private String rawData = "";
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RemoteCommandInformation:");
		sb.append("\n   host:");
		sb.append(hostName);
		sb.append("\n   zone:");
		sb.append(zone);
		sb.append("\n   command:");
		sb.append(command);
		sb.append("\n   rawData:");
		sb.append(rawData);
		return sb.toString();
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}
	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	/**
	 * @return the rawData
	 */
	public String getRawData() {
		return rawData;
	}
	/**
	 * @param rawData the rawData to set
	 */
	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
}
