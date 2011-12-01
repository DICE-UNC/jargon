package org.irods.jargon.core.pub.domain;

import java.util.Date;

import org.irods.jargon.core.protovalues.UserTypeEnum;

/**
 * A user in the IRODS system. This class is not immutable, and should not be
 * shared between threads.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class User extends IRODSDomainObject {

	private String name = "";
	private String id = "";
	private String zone = "";
	private String info = "";
	private String comment = "";
	private Date createTime = null;
	private Date modifyTime = null;
	private UserTypeEnum userType = UserTypeEnum.RODS_UNKNOWN;
	private String userDN = "";

	/**
	 *
	 */
	public User() {
	}

	/**
	 * Name of user
	 * 
	 * @return <code>String</code> with name of user.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of user
	 * 
	 * @param name
	 *            <code>String</code> with the name of the user.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the type of user
	 * 
	 * @return <code>UserTypeEnum</code> value with type of user
	 */
	public UserTypeEnum getUserType() {
		return userType;
	}

	/**
	 * Set the type of user
	 * 
	 * @param userType
	 *            <code>UserTypeEnum</code> value with type of user
	 */
	public void setUserType(final UserTypeEnum userType) {
		this.userType = userType;
	}

	/**
	 * Get the distinguished name, if applicable. Note that, by default, the
	 * distinguished name is not retrieved unless asked for in the various query
	 * methods.
	 * 
	 * @return <code>String</code> with the distinguished name
	 */
	public String getUserDN() {
		return userDN;
	}

	/**
	 * Set the distinguished name of the user, if applicable
	 * 
	 * @param userDN
	 *            <code>String<code> with the distinguished name of the user.
	 */
	public void setUserDN(final String userDN) {
		this.userDN = userDN;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("User:\n");
		stringBuilder.append("  id:");
		stringBuilder.append(id);
		stringBuilder.append("  name:");
		stringBuilder.append(name);
		stringBuilder.append('\n');
		stringBuilder.append("  userType:");
		stringBuilder.append(userType.getTextValue());
		stringBuilder.append('\n');
		stringBuilder.append("  userDn:");
		stringBuilder.append(userDN);
		stringBuilder.append('\n');
		stringBuilder.append("  zone:");
		stringBuilder.append(zone);
		stringBuilder.append('\n');
		stringBuilder.append("  info:");
		stringBuilder.append(info);
		stringBuilder.append('\n');
		stringBuilder.append("  comment:");
		stringBuilder.append(comment);
		return stringBuilder.toString();
	}

	/**
	 * Get the unique id of the user in the catalog
	 * 
	 * @return <code>int</code> with the unique user id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the unique id of the user in the catalog
	 * 
	 * @param id
	 *            <code>int</code> with the unique userid
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Get the zone name for the user
	 * 
	 * @return <code>String</code> with the zone name for the user
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * Set the user zone
	 * 
	 * @param zone
	 *            <code>String</code> with the zone
	 */
	public void setZone(final String zone) {
		this.zone = zone;
	}

	/**
	 * Get misc info for the user
	 * 
	 * @return <code>String</code> with the user misc info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Set the misc info for the user
	 * 
	 * @param info
	 *            <code>String</code> with the user misc info
	 */
	public void setInfo(final String info) {
		this.info = info;
	}

	/**
	 * Get the comment for the user
	 * 
	 * @return <code>String</code> with the user comment info
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set the comment for the user
	 * 
	 * @param comment
	 *            <code>String</code> with the comment for the user
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * Get the create time for the user
	 * 
	 * @return {@link Date} user was created
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * Set the create time for the user
	 * 
	 * @param date
	 *            {@link Date} user was created
	 */
	public void setCreateTime(final Date date) {
		this.createTime = date;
	}

	/**
	 * Get the last modified date for the user
	 * 
	 * @return {@link Date} user was modified
	 */
	public Date getModifyTime() {
		return modifyTime;
	}

	/**
	 * Set the last modified date for the user
	 * 
	 * @param modifyTime
	 *            {@link Date} user was modified
	 */
	public void setModifyTime(final Date modifyTime) {
		this.modifyTime = modifyTime;
	}

}
