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
	private String type = ""; // TODO: enum here
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

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Resource:\n");
		b.append("  id:");
		b.append(id);
		b.append('\n');
		b.append("  name:");
		b.append(name);
		b.append('\n');
		b.append("  zone:");
		b.append(zone);
		b.append('\n');
		b.append("  type:");
		b.append(type);
		b.append('\n');
		b.append("  class:");
		b.append(resourceClass);
		b.append('\n');
		b.append("  location:");
		b.append(location);
		b.append('\n');
		b.append("  vault path:");
		b.append(vaultPath);
		b.append('\n');
		b.append("  freeSpace:");
		b.append(freeSpace);
		b.append('\n');
		b.append("  freeSpaceTime:");
		b.append(freeSpaceTime);
		b.append('\n');
		b.append("  info:");
		b.append(info);
		b.append('\n');
		b.append("  comment:");
		b.append(comment);
		b.append('\n');
		b.append("\n   status:");
		b.append(status);
		b.append("\n   createTime:");
		b.append(createTime);
		b.append('\n');
		b.append("  modifyTime:");
		b.append(modifyTime);
		b.append('\n');
		return b.toString();
	}

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

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
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

}
