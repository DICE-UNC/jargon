package org.irods.jargon.core.protovalues;

public enum RequestTypes {

	RODS_API_REQ("RODS_API_REQ"), RODS_API_REPLY("RODS_API_REPLY"), RODS_CONNECT(
			"RODS_CONNECT"), RODS_VERSION("RODS_VERSION"), RODS_DISCONNECT(
			"RODS_DISCONNECT"), RODS_REAUTH("RODS_REAUTH"), RODS_RECONNECT(
			"RODS_RECONNECT");

	private String requestType;

	RequestTypes(final String requestType) {
		this.requestType = requestType;
	}

	public String getRequestType() {
		return requestType;
	}
}
