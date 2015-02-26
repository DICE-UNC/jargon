/**
 *
 */
package org.irods.jargon.core.pub.domain;

import java.util.Date;

/**
 * This class is a domain object that represents an IRODS zone. This object is
 * mutable for ease of use, so it is not thread-safe. Jargon will not retain
 * references once returned from a method, and will not alter a domain object
 * passed to it
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public class Zone extends IRODSDomainObject {

	private String zoneId = "";
	private String zoneName = "";
	private String zoneType = ""; // TODO: enum?
	private String zoneConnection = "";
	private String zoneComment = "";
	private Date zoneCreateTime = new Date();
	private Date zoneModifyTime = new Date();
	private String host = "";
	private int port = 1247;

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(final String zoneId) {
		this.zoneId = zoneId;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(final String zoneName) {
		this.zoneName = zoneName;
	}

	public String getZoneType() {
		return zoneType;
	}

	public void setZoneType(final String zoneType) {
		this.zoneType = zoneType;
	}

	public String getZoneConnection() {
		return zoneConnection;
	}

	public void setZoneConnection(final String zoneConnection) {
		this.zoneConnection = zoneConnection;
	}

	public String getZoneComment() {
		return zoneComment;
	}

	public void setZoneComment(final String zoneComment) {
		this.zoneComment = zoneComment;
	}

	public Date getZoneCreateTime() {
		return zoneCreateTime;
	}

	public void setZoneCreateTime(final Date zoneCreateTime) {
		this.zoneCreateTime = zoneCreateTime;
	}

	public Date getZoneModifyTime() {
		return zoneModifyTime;
	}

	public void setZoneModifyTime(final Date zoneModifyTime) {
		this.zoneModifyTime = zoneModifyTime;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Zone [");
		if (zoneId != null) {
			builder.append("zoneId=");
			builder.append(zoneId);
			builder.append(", ");
		}
		if (zoneName != null) {
			builder.append("zoneName=");
			builder.append(zoneName);
			builder.append(", ");
		}
		if (zoneType != null) {
			builder.append("zoneType=");
			builder.append(zoneType);
			builder.append(", ");
		}
		if (zoneConnection != null) {
			builder.append("zoneConnection=");
			builder.append(zoneConnection);
			builder.append(", ");
		}
		if (zoneComment != null) {
			builder.append("zoneComment=");
			builder.append(zoneComment);
			builder.append(", ");
		}
		if (zoneCreateTime != null) {
			builder.append("zoneCreateTime=");
			builder.append(zoneCreateTime);
			builder.append(", ");
		}
		if (zoneModifyTime != null) {
			builder.append("zoneModifyTime=");
			builder.append(zoneModifyTime);
			builder.append(", ");
		}
		if (host != null) {
			builder.append("host=");
			builder.append(host);
			builder.append(", ");
		}
		builder.append("port=");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

}
