package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.protovalues.AuditActionEnum;

/**
 * Represents an entry in the iRODS audit system, reflecting an event within the
 * grid.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AuditedAction extends IRODSDomainObject {

	private int objectId = 0;
	private String domainObjectUniqueName = "";
	private int userId = 0;
	private String userName = "";
	private String comment = "";
	private AuditActionEnum auditActionEnum;
	private Date createdAt = new Date();
	private Date updatedAt = new Date();
	private String timeStampInIRODSFormat = "";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AuditedAction:");
		sb.append("\n   objectId:");
		sb.append(objectId);
		sb.append("\n   domainObjectUniqueName:");
		sb.append(domainObjectUniqueName);
		sb.append("\n   userId:");
		sb.append(userId);
		sb.append("\n   userName:");
		sb.append(userName);
		sb.append("\n   comment:");
		sb.append(comment);
		sb.append("\n   auditActionEnum:");
		sb.append(auditActionEnum);
		sb.append("\n   createdAt:");
		sb.append(createdAt);
		sb.append("\n   updatedAt:");
		sb.append(updatedAt);
		sb.append("\n   timeStampInIRODSFormat:");
		sb.append(timeStampInIRODSFormat);
		return sb.toString();
	}

	/**
	 * @return the objectId
	 */
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 *            the objectId to set
	 */
	public void setObjectId(final int objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the domainObjectUniqueName
	 */
	public String getDomainObjectUniqueName() {
		return domainObjectUniqueName;
	}

	/**
	 * @param domainObjectUniqueName
	 *            the domainObjectUniqueName to set
	 */
	public void setDomainObjectUniqueName(final String domainObjectUniqueName) {
		this.domainObjectUniqueName = domainObjectUniqueName;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(final int userId) {
		this.userId = userId;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * @return the auditActionEnum
	 */
	public AuditActionEnum getAuditActionEnum() {
		return auditActionEnum;
	}

	/**
	 * @param auditActionEnum
	 *            the auditActionEnum to set
	 */
	public void setAuditActionEnum(final AuditActionEnum auditActionEnum) {
		this.auditActionEnum = auditActionEnum;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt
	 *            the updatedAt to set
	 */
	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * The time stamp (createdDate) in iRODS format in the database. This can be
	 * used to query for an individual audit event
	 *
	 * @return the timeStampInIRODSFormat {@code String} with the time
	 *         stamp of the audit event, used to create a 'key' thtat can find
	 *         an audit event
	 */
	public String getTimeStampInIRODSFormat() {
		return timeStampInIRODSFormat;
	}

	/**
	 * The time stamp (createdDate) in iRODS format in the database. This can be
	 * used to query for an individual audit event
	 *
	 * @param timeStampInIRODSFormat
	 *            the timeStampInIRODSFormat to set
	 */
	public void setTimeStampInIRODSFormat(final String timeStampInIRODSFormat) {
		this.timeStampInIRODSFormat = timeStampInIRODSFormat;
	}

}
