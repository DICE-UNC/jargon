/**
 *
 */
package org.irods.jargon.core.pub.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private String resourceClass = "";
	private String location = "";
	private String vaultPath = "";
	private long freeSpace = 0;
	private Date freeSpaceTime = new Date();
	private String info = "";
	private String comment = "";
	Date createTime = new Date();
	Date modifyTime = new Date();
	private String status = "";
	private String parentId = "";
	private Resource parentResource = null;
	private String parentName = "";
	private List<String> immediateChildren = new ArrayList<String>();

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
		contextString = type;
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

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("Resource [");
		if (id != null) {
			builder.append("id=").append(id).append(", ");
		}
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}
		if (type != null) {
			builder.append("type=").append(type).append(", ");
		}
		if (contextString != null) {
			builder.append("contextString=").append(contextString).append(", ");
		}
		if (resourceClass != null) {
			builder.append("resourceClass=").append(resourceClass).append(", ");
		}
		if (location != null) {
			builder.append("location=").append(location).append(", ");
		}
		if (vaultPath != null) {
			builder.append("vaultPath=").append(vaultPath).append(", ");
		}
		builder.append("freeSpace=").append(freeSpace).append(", ");
		if (freeSpaceTime != null) {
			builder.append("freeSpaceTime=").append(freeSpaceTime).append(", ");
		}
		if (info != null) {
			builder.append("info=").append(info).append(", ");
		}
		if (comment != null) {
			builder.append("comment=").append(comment).append(", ");
		}
		if (createTime != null) {
			builder.append("createTime=").append(createTime).append(", ");
		}
		if (modifyTime != null) {
			builder.append("modifyTime=").append(modifyTime).append(", ");
		}
		if (status != null) {
			builder.append("status=").append(status).append(", ");
		}
		if (parentId != null) {
			builder.append("parentId=").append(parentId).append(", ");
		}
		if (parentResource != null) {
			builder.append("parentResource=").append(parentResource).append(", ");
		}
		if (parentName != null) {
			builder.append("parentName=").append(parentName).append(", ");
		}
		if (immediateChildren != null) {
			builder.append("immediateChildren=")
					.append(immediateChildren.subList(0, Math.min(immediateChildren.size(), maxLen)));
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
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @return the parentName
	 */
	public String getParentName() {
		return parentName;
	}

	/**
	 * @param parentName
	 *            the parentName to set
	 */
	public void setParentName(final String parentName) {
		this.parentName = parentName;
	}

	/**
	 * @return the immediateChildren
	 */
	public List<String> getImmediateChildren() {
		return immediateChildren;
	}

	/**
	 * @param immediateChildren
	 *            the immediateChildren to set
	 */
	public void setImmediateChildren(final List<String> immediateChildren) {
		this.immediateChildren = immediateChildren;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Resource getParentResource() {
		return parentResource;
	}

	public void setParentResource(Resource parentResource) {
		this.parentResource = parentResource;
	}

}
