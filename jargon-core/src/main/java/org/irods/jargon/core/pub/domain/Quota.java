package org.irods.jargon.core.pub.domain;

import java.util.Date;

/**
 * Represents information on a user or user group quota in iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class Quota {

	/**
	 * Name of the user or group to which the quota applies
	 */
	private String userName = "";

	/**
	 * Zone for which quota applies
	 */
	private String zoneName = "";

	/**
	 * Resource to which quota applies
	 */
	private String resourceName = "";

	/**
	 * The limit of the quota
	 */
	private long quotaLimit = 0L;

	/**
	 * Amount over the quota limit.
	 */
	private long quotaOver = 0L;

	/**
	 * Last modified time.
	 */
	private Date updatedAt = new Date();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("quota:");
		sb.append("\n   userName:");
		sb.append(userName);
		sb.append("\n   zoneName:");
		sb.append(zoneName);
		sb.append("\n   resourceName:");
		sb.append(resourceName);
		sb.append("\n   quotaLimit:");
		sb.append(quotaLimit);
		sb.append("\n   quotaOver:");
		sb.append(quotaOver);
		sb.append("\n   updatedAt:");
		sb.append(updatedAt);
		return sb.toString();

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
	 * @return the zoneName
	 */
	public String getZoneName() {
		return zoneName;
	}

	/**
	 * @param zoneName
	 *            the zoneName to set
	 */
	public void setZoneName(final String zoneName) {
		this.zoneName = zoneName;
	}

	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName
	 *            the resourceName to set
	 */
	public void setResourceName(final String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return the quotaLimit
	 */
	public long getQuotaLimit() {
		return quotaLimit;
	}

	/**
	 * @param quotaLimit
	 *            the quotaLimit to set
	 */
	public void setQuotaLimit(final long quotaLimit) {
		this.quotaLimit = quotaLimit;
	}

	/**
	 * @return the quotaOver
	 */
	public long getQuotaOver() {
		return quotaOver;
	}

	/**
	 * @param quotaOver
	 *            the quotaOver to set
	 */
	public void setQuotaOver(final long quotaOver) {
		this.quotaOver = quotaOver;
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

}
