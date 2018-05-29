package org.irods.jargon.core.protovalues;

/**
 * iRODS audit system types, reflecting various auditable actions.
 *
 * Based on https://irods.sdsc.edu/index.php/Audit_Records
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum AuditActionEnum {

	ACCESS_GRANTED(1000, "Access granted", "Access is being granted to a data object to modify its metadata",
			"audit.access.granted", "Access Level"), REGISTER_DATA_OBJECT(2010, "Register data object",
					"A data object is being created", "audit.register.data.object", ""), REGISTER_DATA_REPLICA(2011,
							"Register data replica", "A data object replica is being registered",
							"audit.register.data.replica", "Replica Number"), UNREGISTER_DATA_OBJ(2012,
									"Unregister data object", "A data object is being removed",
									"audit.unregister.data.object", ""), REGISTER_DELAYED_RULE(2020,
											"Register delayed rule", "Register a delayed rule",
											"audit.register.delayed.rule", "Rule Name"), MODIFY_DELAYED_RULE(2021,
													"Modify delayed rule", "Modify a delayed rule",
													"audit.modify.delayed.rule", ""), DELETE_DELAYED_RULE(2022,
															"Delete delayed rule", "Delete a delayed rule",
															"audit.delete.delayed.rule", ""), REGISTER_RESOURCE(2030,
																	"Register resource", "Register a resource",
																	"audit.register.resource",
																	"Resource Name"), DELETE_RESOURCE(2031,
																			"Delete resource", "Delete resource",
																			"audit.delete.resource",
																			"Resource Name"), DELETE_USER_RE(2040,
																					"Delete user via re",
																					"Delete a user (Rule Engine version)",
																					"audit.delete.user.re",
																					"User Name"), REGISTER_COLL_BY_ADMIN(
																							2050,
																							"Register coll by admin",
																							"Register a collection by the administrator",
																							"audit.register.collection.admin",
																							"Client User Name"), REGISTER_COLL(
																									2051,
																									"Register collection",
																									"Register collection",
																									"audit.register.collection",
																									"Collection Name"), DELETE_COLL_BY_ADMIN(
																											2060,
																											"Delete coll by admin",
																											"Delete a collection by the administrator",
																											"audit.delete.collection.admin",
																											"Collection Name"), DELETE_COLL(
																													2061,
																													"Delete collection",
																													"Delete a collection",
																													"audit.delete.collection",
																													"Collection Name"), DELETE_ZONE(
																															2062,
																															"Delete zone",
																															"Delete a zone (note: the object id is 0)",
																															"audit.delete.zone",
																															"Zone Name"), REGISTER_ZONE(
																																	2064,
																																	"Register zone",
																																	"Register zone (note: the object id is 0)",
																																	"audit.register.zone",
																																	"Zone Name"), MOD_USER_TYPE(
																																			2071,
																																			"Mod user type",
																																			"Modify a user type",
																																			"audit.modify.user.type",
																																			"New Type"), MOD_USER_ZONE(
																																					2072,
																																					"Mod user zone",
																																					"Modify a user zone",
																																					"audit.modify.user.zone",
																																					"New Zone"), MOD_USER_DN(
																																							2073,
																																							"Mod user DN",
																																							"Modify a user DN (Kerberos or GSI)",
																																							"audit.modify.user.dn",
																																							"New DN"), MOD_USER_INFO(
																																									2074,
																																									"Mod user info",
																																									"Modify a user info field",
																																									"audit.modify.user.info",
																																									"New Info"), MOD_USER_COMMENT(
																																											2075,
																																											"Mod user comment",
																																											"Modify a user comment",
																																											"audit.modify.user.comment",
																																											"New Comment"), MOD_USER_PASSWORD(
																																													2076,
																																													"Mod user password",
																																													"Modify a user password",
																																													"audit.modify.password",
																																													""), MOD_GROUP(
																																															2080,
																																															"Mod group",
																																															"Modify a group",
																																															"audit.modify.group",
																																															"Option and Id"), MOD_RESC(
																																																	2090,
																																																	"Mod resource",
																																																	"Modify a resource",
																																																	"audit.modify.resource",
																																																	"Option and Value"), MOD_RESC_FREE_SPACE(
																																																			2091,
																																																			"Mod resc free space",
																																																			"Modify resource free space",
																																																			"audit.modify.resource.free.space",
																																																			"New Value"), MOD_RESC_GROUP(
																																																					2092,
																																																					"Mod resc group",
																																																					"Modify resource group",
																																																					"audit.modify.resource.group",
																																																					"Option and Value"), MOD_ZONE(
																																																							2093,
																																																							"Mod zone",
																																																							"Modify a zone",
																																																							"audit.modify.zone",
																																																							"Description of Modification"), REGISTER_USER_RE(
																																																									2100,
																																																									"Register a user re",
																																																									"Register a user (Rule Engine version)",
																																																									"audit.modify.user.re",
																																																									"User Zone"), ADD_AVU_METADATA(
																																																											2110,
																																																											"Add AVU metadata",
																																																											"Add AVU Metadata (User defined)",
																																																											"audit.add.avu",
																																																											"Object Type"), DELETE_AVU_METADATA(
																																																													2111,
																																																													"Delete AVU metadata",
																																																													"Delete AVU Metadata (User defined)",
																																																													"audit.delete.avu",
																																																													"Object Type"), COPY_AVU_METADATA(
																																																															2112,
																																																															"Copy AVU metadata",
																																																															"Copy AVU metadata",
																																																															"audit.copy.avu",
																																																															"Object AVUs copied to"), MOD_ACCESS_CONTROL_OBJ(
																																																																	2120,
																																																																	"Mod access control obj",
																																																																	"Modify access control",
																																																																	"audit.modify.access.control.object",
																																																																	"Access Level"), MOD_ACCESS_CONTROL_COLL(
																																																																			2121,
																																																																			"Mod access control col",
																																																																			"Modify access control",
																																																																			"audit.modify.access.control.collection",
																																																																			"Access Level"), MOD_ACCESS_CONTROL_COLL_RECURSIVE(
																																																																					2122,
																																																																					"Mod access control recursive",
																																																																					"Mod access control recursive",
																																																																					"audit.modify.access.control.collection.recursive",
																																																																					"Access Level"), RENAME_DATA_OBJ(
																																																																							2130,
																																																																							"Rename data obj",
																																																																							"Rename a data object",
																																																																							"audit.rename.data.object",
																																																																							"New Name"), RENAME_COLLECTION(
																																																																									2131,
																																																																									"Rename collection",
																																																																									"Rename a collection",
																																																																									"audit.rename.collection",
																																																																									"New Name"), MOVE_DATA_OBJ(
																																																																											2140,
																																																																											"Move data object",
																																																																											"Move a data object",
																																																																											"audit.move.data.object",
																																																																											"Destination Collection Id"), MOVE_COLL(
																																																																													2141,
																																																																													"Move collection",
																																																																													"Move a collection",
																																																																													"audit.move.collection",
																																																																													"Destination Collection Name"), REG_TOKEN(
																																																																															2150,
																																																																															"Register token",
																																																																															"Register a token",
																																																																															"audit.register.token",
																																																																															"Token Name"), DEL_TOKEN(
																																																																																	2151,
																																																																																	"Delete token",
																																																																																	"Delete a token",
																																																																																	"audit.delete.token",
																																																																																	"Token Name");

	private int auditCode;
	private String textValue;
	private String meaning;
	private String meaningCode;
	private String commentContent;

	/**
	 * Default constructor for enum entry
	 *
	 * @param auditCode
	 * @param textValue
	 * @param meaning
	 * @param meaningCode
	 * @param commentContent
	 */
	AuditActionEnum(final int auditCode, final String textValue, final String meaning, final String meaningCode,
			final String commentContent) {
		this.auditCode = auditCode;
		this.textValue = textValue;
		this.meaning = meaning;
		this.meaningCode = meaningCode;
		this.commentContent = commentContent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AuditActionEnum:");
		sb.append("\n   auditCode:");
		sb.append(auditCode);
		sb.append("\n   textValue:");
		sb.append(textValue);
		sb.append("\n  meaning:");
		sb.append(meaning);
		sb.append("\n   meaningCode:");
		sb.append(meaningCode);
		sb.append("\n   commentContent:");
		sb.append(commentContent);
		return sb.toString();
	}

	/**
	 * Based on the audit type code in the ICAT, return the enumeration type
	 *
	 * @param i
	 *            {@code int} with the icat value
	 * @return {@code AuditActionEnum} value that corresponds
	 */
	public static AuditActionEnum valueOf(final int i) {

		switch (i) {
		case 1000:
			return ACCESS_GRANTED;
		case 2010:
			return REGISTER_DATA_OBJECT;
		case 2011:
			return REGISTER_DATA_REPLICA;
		case 2012:
			return UNREGISTER_DATA_OBJ;
		case 2020:
			return REGISTER_DELAYED_RULE;
		case 2021:
			return MODIFY_DELAYED_RULE;
		case 2022:
			return DELETE_DELAYED_RULE;
		case 2030:
			return REGISTER_RESOURCE;
		case 2031:
			return DELETE_RESOURCE;
		case 2040:
			return DELETE_USER_RE;
		case 2050:
			return REGISTER_COLL_BY_ADMIN;
		case 2051:
			return REGISTER_COLL;
		case 2060:
			return DELETE_COLL_BY_ADMIN;
		case 2061:
			return DELETE_COLL;
		case 2062:
			return DELETE_ZONE;
		case 2064:
			return REGISTER_ZONE;
		case 2071:
			return MOD_USER_TYPE;
		case 2072:
			return MOD_USER_ZONE;
		case 2073:
			return MOD_USER_DN;
		case 2074:
			return MOD_USER_INFO;
		case 2075:
			return MOD_USER_COMMENT;
		case 2076:
			return MOD_USER_PASSWORD;
		case 2080:
			return MOD_GROUP;
		case 2090:
			return MOD_RESC;
		case 2091:
			return MOD_RESC_FREE_SPACE;
		case 2092:
			return MOD_RESC_GROUP;
		case 2093:
			return MOD_ZONE;
		case 2100:
			return REGISTER_USER_RE;
		case 2110:
			return ADD_AVU_METADATA;
		case 2111:
			return DELETE_AVU_METADATA;
		case 2112:
			return COPY_AVU_METADATA;
		case 2120:
			return MOD_ACCESS_CONTROL_OBJ;
		case 2121:
			return MOD_ACCESS_CONTROL_COLL;
		case 2122:
			return MOD_ACCESS_CONTROL_COLL_RECURSIVE;
		case 2130:
			return RENAME_DATA_OBJ;
		case 2131:
			return RENAME_COLLECTION;
		case 2140:
			return MOVE_DATA_OBJ;
		case 2141:
			return MOVE_COLL;
		case 2150:
			return REG_TOKEN;
		case 2151:
			return DEL_TOKEN;
		default:
			throw new IllegalArgumentException("unexpected int: " + i);
		}

	}

	public String getTextValue() {
		return textValue;
	}

	/**
	 * @return the auditCode that represents the action id in iRODS
	 */
	public int getAuditCode() {
		return auditCode;
	}

	/**
	 * @param auditCode
	 *            the auditCode to set, representing the action id in iRODS
	 */
	public void setAuditCode(final int auditCode) {
		this.auditCode = auditCode;
	}

	/**
	 * @return the meaning, a {@code String} with a textual description of the
	 *         meaning of the action
	 */
	public String getMeaning() {
		return meaning;
	}

	/**
	 * @param meaning
	 *            the meaning to set
	 */
	public void setMeaning(final String meaning) {
		this.meaning = meaning;
	}

	/**
	 * @return the meaningCode {@code String} with a message code suitable for i18n
	 */
	public String getMeaningCode() {
		return meaningCode;
	}

	/**
	 * @param meaningCode
	 *            the meaningCode to set
	 */
	public void setMeaningCode(final String meaningCode) {
		this.meaningCode = meaningCode;
	}

	/**
	 * @return the commentContent {@code String} with the meaning of the
	 *         {@code r_comment} entry in the ICAT. The content of the comment
	 *         changes based on the type of action
	 */
	public String getCommentContent() {
		return commentContent;
	}

	/**
	 * @param commentContent
	 *            the commentContent to set
	 */
	public void setCommentContent(final String commentContent) {
		this.commentContent = commentContent;
	}

	/**
	 * @param textValue
	 *            the textValue to set
	 */
	public void setTextValue(final String textValue) {
		this.textValue = textValue;
	}
}
