package org.irods.jargon.core.pub.domain;

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
	private String createTime = "";
	private String modifyTime = "";
	private UserTypeEnum userType = UserTypeEnum.RODS_UNKNOWN;
	private String userDN = "";

	/**
	 *
	 */
	public User() {
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public UserTypeEnum getUserType() {
		return userType;
	}

	public void setUserType(final UserTypeEnum userType) {
		this.userType = userType;
	}

	public String getUserDN() {
		return userDN;
	}

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

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(final String zone) {
		this.zone = zone;
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

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(final String createTime) {
		this.createTime = createTime;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(final String modifyTime) {
		this.modifyTime = modifyTime;
	}

}
