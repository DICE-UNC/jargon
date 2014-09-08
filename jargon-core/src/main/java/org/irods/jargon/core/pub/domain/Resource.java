/**
 * 
 */
package org.irods.jargon.core.pub.domain;

import java.util.Date;

/**
 * This class is a domain object that represents an IRODS resource. This object
 * is mutable for ease of use, so it is not thread-safe. Jargon will not retain
 * references once returned from a method, and will not alter a domain object
 * passed to it
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class Resource extends IRODSDomainObject {

	private String id = "";
	private String name = "";
	private Zone zone = new Zone();
	private String type = "";
	private String contextString = "";
	private String resourceClass = ""; // TODO: enum?
	private String location = "";
	private String vaultPath = "";
	private long freeSpace = 0;
	private Date freeSpaceTime = new Date();
	private String info = "";
	private String comment = "";
	Date createTime = new Date();
	Date modifyTime = new Date();
	private String status = "";

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(final Zone zone) {
		this.zone = zone;
	}

	public String getContextString() {
		return contextString;
	}

	public void setContextString(final String type) {
		this.contextString = type;
	}

	public String getResourceClass() {
		return resourceClass;
	}

	public void setResourceClass(final String resourceClass) {
		this.resourceClass = resourceClass;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public String getVaultPath() {
		return vaultPath;
	}

	public void setVaultPath(final String vaultPath) {
		this.vaultPath = vaultPath;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(final long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public Date getFreeSpaceTime() {
		return freeSpaceTime;
	}

	public void setFreeSpaceTime(final Date freeSpaceTime) {
		this.freeSpaceTime = freeSpaceTime;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(final String info) {
		this.info = info;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(final Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(final Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Resource [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (zone != null) {
			builder.append("zone=");
			builder.append(zone);
			builder.append(", ");
		}
		if (contextString != null) {
			builder.append("contextString=");
			builder.append(contextString);
			builder.append(", ");
		}
		if (resourceClass != null) {
			builder.append("resourceClass=");
			builder.append(resourceClass);
			builder.append(", ");
		}
		if (location != null) {
			builder.append("location=");
			builder.append(location);
			builder.append(", ");
		}
		if (vaultPath != null) {
			builder.append("vaultPath=");
			builder.append(vaultPath);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}

		builder.append("freeSpace=");
		builder.append(freeSpace);
		builder.append(", ");
		if (freeSpaceTime != null) {
			builder.append("freeSpaceTime=");
			builder.append(freeSpaceTime);
			builder.append(", ");
		}
		if (info != null) {
			builder.append("info=");
			builder.append(info);
			builder.append(", ");
		}
		if (comment != null) {
			builder.append("comment=");
			builder.append(comment);
			builder.append(", ");
		}
		if (createTime != null) {
			builder.append("createTime=");
			builder.append(createTime);
			builder.append(", ");
		}
		if (modifyTime != null) {
			builder.append("modifyTime=");
			builder.append(modifyTime);
			builder.append(", ");
		}
		if (status != null) {
			builder.append("status=");
			builder.append(status);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
