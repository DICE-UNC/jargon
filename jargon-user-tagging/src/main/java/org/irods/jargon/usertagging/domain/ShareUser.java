/**
 * 
 */
package org.irods.jargon.usertagging.domain;

import java.io.Serializable;

import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;

/**
 * Represents a share for a user, which consists of the user name, user zone,
 * and permission type desired
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ShareUser extends IRODSDomainObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4660577038224822489L;

	/**
	 * User name to share with
	 */
	private final String userName;

	/**
	 * Zone to share with, a blank will denote the current zone
	 */
	private final String zone;

	/**
	 * Permission associated with the given user
	 */
	private final FilePermissionEnum filePermission;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\tShareUser");
		sb.append("\n\tuserName:");
		sb.append(userName);
		sb.append("\n\tzone:");
		sb.append(zone);
		sb.append("\n\tfilePermission:");
		sb.append(filePermission);
		return sb.toString();
	}

	/**
	 * Constructor for an immutable share permission
	 * 
	 * @param userName
	 *            <code>String</code> with the userName to share with
	 * @param zone
	 *            <code>String</code> with the zone (can be blank to denote
	 *            current zone user)
	 * @param filePermission
	 *            {@link FilePermissionEnum with desired permission to set}
	 */
	public ShareUser(final String userName, final String zone,
			final FilePermissionEnum filePermission) {
		super();
		this.userName = userName;
		this.zone = zone;
		this.filePermission = filePermission;
	}

	public String getUserName() {
		return userName;
	}

	public String getZone() {
		return zone;
	}

	public FilePermissionEnum getFilePermission() {
		return filePermission;
	}

}
