package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;

/**
 * Represents a packing instruction for iRODS general admin functionality. These
 * functions are equivalent to operations done using the iadmin icommands.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GeneralAdminInp extends AbstractIRODSPackingInstruction {

	private static final String PI_TAG = "generalAdminInp_PI";

	/**
	 * Protocol API identifier for gen admin operations
	 */
	public static final int GEN_ADMIN_INP_API_NBR = 701;

	private static final String ARG0 = "arg0";
	private static final String ARG1 = "arg1";
	private static final String ARG2 = "arg2";
	private static final String ARG3 = "arg3";
	private static final String ARG4 = "arg4";
	private static final String ARG5 = "arg5";
	private static final String ARG6 = "arg6";
	private static final String ARG7 = "arg7";
	private static final String ARG8 = "arg8";
	private static final String ARG9 = "arg9";
	private static final String BLANK = "";

	private String arg0 = "";
	private String arg1 = "";
	private String arg2 = "";
	private String arg3 = "";
	private String arg4 = "";
	private String arg5 = "";
	private String arg6 = "";
	private String arg7 = "";
	private String arg8 = "";
	private String arg9 = "";

	/**
	 * Generate the packing instruction suitable for adding the given user to
	 * iRODS.
	 * 
	 * @param user
	 *            {@link org.irods.jargon.core.pub.domain.User} to be added to
	 *            iRODS.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForAddUser(final User user)
			throws JargonException {

		if (user == null) {
			throw new JargonException("null user");
		}

		if (user.getName().isEmpty()) {
			throw new JargonException("blank user name");
		}

		if (user.getUserType() == UserTypeEnum.RODS_UNKNOWN) {
			throw new JargonException("unknown user type");
		}

		return new GeneralAdminInp("add", "user", user.getName(), user
				.getUserType().getTextValue(), "", user.getUserDN(), BLANK,
				BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the comment
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param comment
	 *            <code>String<code> with the data to be stored in the user comment.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserComment(
			final String userName, final String comment) throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("null or missing user name");
		}

		if (comment == null) {
			throw new JargonException("null comment");
		}

		return new GeneralAdminInp("modify", "user", userName, "comment",
				comment, BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the info
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param info
	 *            <code>String<code> with the data to be stored in the user info.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserInfo(
			final String userName, final String info) throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("null or missing user name");
		}

		if (info == null) {
			throw new JargonException("null comment");
		}

		return new GeneralAdminInp("modify", "user", userName, "info", info,
				BLANK, BLANK, BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for removing a user from iRODS.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name to be removed.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForDeleteUser(final String userName)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("user name is null or empty");
		}

		return new GeneralAdminInp("rm", "user", userName, BLANK, BLANK, BLANK,
				BLANK, BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the packing instruction to set user quota total for a user
	 * 
	 * @param userName
	 *            <code>String</code> with the user name
	 * @param quotaValue
	 *            <code>long</code> with the total (across resources) quota
	 *            value
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForSetUserQuotaTotal(
			final String userName, final long quotaValue)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException(
					"quota value is less than or equal to zero");
		}

		return new GeneralAdminInp("set-quota", "user", userName, "total",
				String.valueOf(quotaValue), BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Set the 'total' quota for a user group
	 * 
	 * @param userGroupName
	 *            <code>String</code> with the user group name
	 * @param quotaValue
	 *            <code>long</code> with the quota value for the given resource
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForSetUserGroupQuotaTotal(
			final String userGroupName, final long quotaValue)
			throws JargonException {

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException(
					"quota value is less than or equal to zero");
		}

		return new GeneralAdminInp("set-quota", "group", userGroupName,
				"total", String.valueOf(quotaValue), BLANK, BLANK, BLANK,
				BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the packing instruction to set the user quota for a given resource
	 * 
	 * @param userName
	 *            <code>String</code> with the user name
	 * @param resourceName
	 * @param quotaValue
	 *            <code>long</code> with the quota value for the given resource
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForSetUserQuotaForResource(
			final String userName, final String resourceName,
			final long quotaValue) throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException(
					"quota value is less than or equal to zero");
		}

		return new GeneralAdminInp("set-quota", "user", userName, resourceName,
				String.valueOf(quotaValue), BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the packing instruction to set the user group quota for a given
	 * resource
	 * 
	 * @param userGroupName
	 *            <code>String</code> with the user group name
	 * @param resourceName
	 * @param quotaValue
	 *            <code>long</code> with the quota value for the given resource
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForSetUserGroupQuotaForResource(
			final String userGroupName, final String resourceName,
			final long quotaValue) throws JargonException {

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException(
					"quota value is less than or equal to zero");
		}

		return new GeneralAdminInp("set-quota", "group", userGroupName,
				resourceName, String.valueOf(quotaValue), BLANK, BLANK, BLANK,
				BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the command to cause quota usage to be calculated
	 * 
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForCalculateQuotaUsage()
			throws JargonException {
		return new GeneralAdminInp("calculate-usage", BLANK, BLANK, BLANK,
				BLANK, BLANK, BLANK, BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the zone
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param zone
	 *            <code>String<code> with the user's zone.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserZone(
			final String userName, final String zone) throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("user name is null or empty");
		}

		if (zone == null) {
			throw new JargonException("zone is null");
		}

		return new GeneralAdminInp("modify", "user", userName, "zone", zone,
				BLANK, BLANK, BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the password
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param password
	 *            <code>String<code> with the user's password.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserPasswordByAdmin(
			final String userName, final String password)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("user name is null or empty");
		}

		if (password == null || password.isEmpty()) {
			throw new JargonException("password is null or empty");
		}

		return new GeneralAdminInp("modify", "user", userName, "password",
				password, BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the password
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param password
	 *            <code>String<code> with the user's password.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserPassword(
			final String userName, final String password)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("user name is null or empty");
		}

		if (password == null || password.isEmpty()) {
			throw new JargonException("password is null or empty");
		}

		return new GeneralAdminInp("modify", "user", userName, "password",
				password, BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create a packing instruction to add the given iRODS user group to the
	 * zone
	 * 
	 * @param userGroup
	 *            {@link UserGroup} to add
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForAddUserGroup(
			final UserGroup userGroup) throws JargonException {
		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}
		return new GeneralAdminInp("add", "user", userGroup.getUserGroupName(),
				"rodsgroup", userGroup.getZone(), BLANK, BLANK, BLANK, BLANK,
				BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the packing instruction to add a user to a given iRODS user group
	 * 
	 * @param userGroupName
	 *            <code>String</code> with the user group name to which the user
	 *            will be added
	 * @param userName
	 *            <code>String</code> user name to add to the group
	 * @param zoneName
	 *            <code>String</code> that is optional (set to blank or
	 *            <code>null</code> if not applicable, that sets the zone for
	 *            the user
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForAddUserToGroup(
			final String userGroupName, final String userName,
			final String zoneName) throws JargonException {

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		StringBuilder userNameBuilder = new StringBuilder();
		userNameBuilder.append(userName.trim());
		if (zoneName != null && zoneName.length() > 0) {
			userNameBuilder.append('#');
			userNameBuilder.append(zoneName.trim());
		}

		return new GeneralAdminInp("modify", "group", userGroupName.trim(),
				"add", userNameBuilder.toString(), BLANK, BLANK, BLANK, BLANK,
				BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create the packing instruction to remove a user from a group
	 * 
	 * @param userGroupName
	 *            <code>String</code> with the user group name from which the
	 *            user will be removed
	 * @param userName
	 *            <code>String</code> user name to remove
	 * @param zoneName
	 *            <code>String</code> that is optional (set to blank or
	 *            <code>null</code> if not applicable, that sets the zone for
	 *            the user
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForRemoveUserFromGroup(
			final String userGroupName, final String userName,
			final String zoneName) throws JargonException {

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		StringBuilder userNameBuilder = new StringBuilder();
		userNameBuilder.append(userName.trim());
		if (zoneName != null && zoneName.length() > 0) {
			userNameBuilder.append('#');
			userNameBuilder.append(zoneName.trim());
		}

		return new GeneralAdminInp("modify", "group", userGroupName.trim(),
				"remove", userNameBuilder.toString(), BLANK, BLANK, BLANK,
				BLANK, BLANK, GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Create a packing instruction to remove a given iRODS user group
	 * 
	 * @param userGroup
	 *            {@link UserGroup} to remove
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForRemoveUserGroup(
			final UserGroup userGroup) throws JargonException {
		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}
		return new GeneralAdminInp("rm", "user", userGroup.getUserGroupName(),
				userGroup.getZone(), BLANK, BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	/**
	 * Generate the packing instruction suitable for modifying the type
	 * associated with the given user.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name.
	 * @param userType
	 *            {@link org.irods.jargon.core.protovalues.UserTypeEnum} value
	 *            for the user.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static GeneralAdminInp instanceForModifyUserType(
			final String userName, final UserTypeEnum userType)
			throws JargonException {
		if (userName == null || userName.isEmpty()) {
			throw new JargonException("user name is null or empty");
		}

		if (userType == null) {
			throw new JargonException("user type is null");
		}

		if (userType == UserTypeEnum.RODS_UNKNOWN) {
			throw new JargonException("user type is null");
		}

		return new GeneralAdminInp("modify", "user", userName, "type",
				userType.getTextValue(), BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);
	}

	private GeneralAdminInp(final String arg0, final String arg1,
			final String arg2, final String arg3, final String arg4,
			final String arg5, final String arg6, final String arg7,
			final String arg8, final String arg9, final int apiNumber)
			throws JargonException {
		super();

		if (apiNumber <= 0) {
			throw new JargonException("api type is <= zero");
		}

		this.setApiNumber(apiNumber);

		this.arg0 = arg0;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
		this.arg5 = arg5;
		this.arg6 = arg6;
		this.arg7 = arg7;
		this.arg8 = arg8;
		this.arg9 = arg9;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg0() {
		return arg0;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg1() {
		return arg1;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg2() {
		return arg2;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg3() {
		return arg3;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg4() {
		return arg4;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg5() {
		return arg5;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg6() {
		return arg6;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg7() {
		return arg7;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg8() {
		return arg8;
	}

	/**
	 * Get the argument
	 * 
	 * @return <code>String</code> with the argument at this position
	 */
	public String getArg9() {
		return arg9;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(ARG0, getArg0()),
				new Tag(ARG1, getArg1()), new Tag(ARG2, getArg2()),
				new Tag(ARG3, getArg3()), new Tag(ARG4, getArg4()),
				new Tag(ARG5, getArg5()), new Tag(ARG6, getArg6()),
				new Tag(ARG7, getArg7()), new Tag(ARG8, getArg8()),
				new Tag(ARG9, getArg9()) });

		return message;
	}

}
