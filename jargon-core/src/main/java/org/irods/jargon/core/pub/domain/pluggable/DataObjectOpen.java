
package org.irods.jargon.core.pub.domain.pluggable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataObjectOpen {

	@JsonProperty("replica_token")
	private String replicaToken = "";

	private int fileId = 0;

	/**
	 * @return the replicaToken
	 */
	public String getReplicaToken() {
		return replicaToken;
	}

	/**
	 * @param replicaToken the replicaToken to set
	 */
	public void setReplicaToken(String replicaToken) {
		this.replicaToken = replicaToken;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataObjectOpen [");
		if (replicaToken != null) {
			builder.append("replicaToken=").append(replicaToken).append(", ");
		}
		builder.append("fileId=").append(fileId).append("]");
		return builder.toString();
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

}
