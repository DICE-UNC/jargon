/**
 * 
 */
package org.irods.jargon.core.pub.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	String zoneId = "";
	String zoneName = "";
	String zoneType = ""; // TODO: enum?
	String zoneConnection = "";
	String zoneComment = "";
	Date zoneCreateTime = new Date();
	Date zoneModifyTime = new Date(); // TODO: util to convert IRODS date values
										// to java Dates needed
	List<Resource> resources = new ArrayList<Resource>();

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

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Zone:\n");
		stringBuilder.append("  zoneId:");
		stringBuilder.append(zoneId);
		stringBuilder.append('\n');
		stringBuilder.append("  zoneName:");
		stringBuilder.append(zoneName);
		stringBuilder.append('\n');
		stringBuilder.append("  zoneType:");
		stringBuilder.append(zoneType);
		stringBuilder.append('\n');
		stringBuilder.append("  zoneConnection:");
		stringBuilder.append(zoneConnection);
		stringBuilder.append('\n');
		stringBuilder.append("  zoneComment:");
		stringBuilder.append(zoneComment);
		stringBuilder.append('\n');
		stringBuilder.append("  createTime:");
		stringBuilder.append(zoneCreateTime);
		stringBuilder.append('\n');
		stringBuilder.append("  zoneModifyTime:");
		stringBuilder.append(zoneModifyTime);
		stringBuilder.append('\n');

		return stringBuilder.toString();
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(final List<Resource> resources) {
		this.resources = resources;
	}

}
