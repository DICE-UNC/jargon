package org.irods.jargon.core.pub.domain.pluggable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for replica close pluggable api call
 * 
 * see:
 * https://github.com/irods/irods/blob/6784fbb26fc703212f02e170d1bb51e799ffc1ac/plugins/api/src/replica_close.cpp
 * 
 * @author conwaymc
 *
 */
public class ReplicaClose {

	private int fd = 0;
	@JsonProperty("update_size")
	private boolean updateSize = true;
	@JsonProperty("update_status")
	private boolean updateStatus = true;
	@JsonProperty("compute_checksum")
	private boolean computeChecksum = false;
	@JsonProperty("send_notifications")
	private boolean sendNotifications = true;
	@JsonProperty("preserve_replica_state_table")
	private boolean preserveReplicaStateTable = false;

	public int getFd() {
		return fd;
	}

	public void setFd(int fd) {
		this.fd = fd;
	}

	public boolean isUpdateSize() {
		return updateSize;
	}

	public void setUpdateSize(boolean updateSize) {
		this.updateSize = updateSize;
	}

	public boolean isUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(boolean updateStatus) {
		this.updateStatus = updateStatus;
	}

	public boolean isComputeChecksum() {
		return computeChecksum;
	}

	public void setComputeChecksum(boolean computeChecksum) {
		this.computeChecksum = computeChecksum;
	}

	public boolean isSendNotifications() {
		return sendNotifications;
	}

	public void setSendNotifications(boolean sendNotifications) {
		this.sendNotifications = sendNotifications;
	}

	public boolean isPreserveReplicaStateTable() {
		return preserveReplicaStateTable;
	}

	public void setPreserveReplicaStateTable(boolean preserveReplicaStateTable) {
		this.preserveReplicaStateTable = preserveReplicaStateTable;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReplicaClose [fd=").append(fd).append(", updateSize=").append(updateSize)
				.append(", updateStatus=").append(updateStatus).append(", computeChecksum=").append(computeChecksum)
				.append(", sendNotifications=").append(sendNotifications).append(", preserveReplicaStateTable=")
				.append(preserveReplicaStateTable).append("]");
		return builder.toString();
	}

}
