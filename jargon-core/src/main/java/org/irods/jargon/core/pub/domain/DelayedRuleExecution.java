/**
 * 
 */
package org.irods.jargon.core.pub.domain;

/**
 * TODO: work in progress, subject to change Represents an entry in the rule
 * engine delayed execution queue
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DelayedRuleExecution extends IRODSDomainObject {

	private int id = 0;
	private String name = "";
	private String userName = "";
	private String address = "";
	private String execTime = "";
	private String frequency = "";
	private String priority = "";
	private String estimatedExecTime = "";
	private String notificationAddress = "";
	private String lastExecTime = "";
	private String execStatus = "";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nDelayedRuleExecution:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   userName:");
		sb.append(userName);
		sb.append("\n   address:");
		sb.append(address);
		sb.append("\n   execTime:");
		sb.append(execTime);
		sb.append("\n   frequency:");
		sb.append(frequency);
		sb.append("\n   priority:");
		sb.append(priority);
		sb.append("\n   estimatedExecTime:");
		sb.append(estimatedExecTime);
		sb.append("\n   notificationAddress:");
		sb.append(notificationAddress);
		sb.append("\n   lastExecTime:");
		sb.append(lastExecTime);
		sb.append("\n   notificationAddress:");
		sb.append(notificationAddress);
		sb.append("\n   execStatus:");
		sb.append(execStatus);
		return sb.toString();
	}

	public synchronized int getId() {
		return id;
	}

	public synchronized void setId(final int id) {
		this.id = id;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(final String name) {
		this.name = name;
	}

	public synchronized String getUserName() {
		return userName;
	}

	public synchronized void setUserName(final String userName) {
		this.userName = userName;
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized void setAddress(final String address) {
		this.address = address;
	}

	public synchronized String getExecTime() {
		return execTime;
	}

	public synchronized void setExecTime(final String execTime) {
		this.execTime = execTime;
	}

	public synchronized String getFrequency() {
		return frequency;
	}

	public synchronized void setFrequency(final String frequency) {
		this.frequency = frequency;
	}

	public synchronized String getPriority() {
		return priority;
	}

	public synchronized void setPriority(final String priority) {
		this.priority = priority;
	}

	public synchronized String getEstimatedExecTime() {
		return estimatedExecTime;
	}

	public synchronized void setEstimatedExecTime(final String estimatedExecTime) {
		this.estimatedExecTime = estimatedExecTime;
	}

	public synchronized String getNotificationAddress() {
		return notificationAddress;
	}

	public synchronized void setNotificationAddress(
			final String notificationAddress) {
		this.notificationAddress = notificationAddress;
	}

	public synchronized String getLastExecTime() {
		return lastExecTime;
	}

	public synchronized void setLastExecTime(final String lastExecTime) {
		this.lastExecTime = lastExecTime;
	}

	public synchronized String getExecStatus() {
		return execStatus;
	}

	public synchronized void setExecStatus(final String execStatus) {
		this.execStatus = execStatus;
	}

}
