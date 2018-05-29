package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Packing instruction to request a temporary password as the designated user.
 * This capability is only available to rodsadmin, and is a post iRODS 3.0
 * enhancement.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class GetTempPasswordForOther extends AbstractIRODSPackingInstruction {

	public static final int GET_TEMP_PASSWORD_FOR_OTHER_API_NBR = 724;
	public static final String PI_TAG = "getTempPasswordForOtherInp_PI";
	private final String targetUserName;

	/**
	 * Instance method creates a request to generate a temporary password.
	 *
	 * @param targetUserName
	 *            {@code String} (required) with the user name for which a temporary
	 *            password will be generated.
	 * @return {@code GetTempPasswordIn} instance
	 */
	public static GetTempPasswordForOther instance(final String targetUserName) {
		return new GetTempPasswordForOther(targetUserName);
	}

	/**
	 * Private constructor
	 *
	 * @param targetUserName
	 *            {@code String} (required) with the user name for which a temporary
	 *            password will be generated.
	 */
	private GetTempPasswordForOther(final String targetUserName) {
		super();
		if (targetUserName == null || targetUserName.isEmpty()) {
			throw new IllegalArgumentException("targetUserName is null or empty");
		}
		this.targetUserName = targetUserName;
		setApiNumber(GET_TEMP_PASSWORD_FOR_OTHER_API_NBR);
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
		final Tag message = new Tag(PI_TAG,
				new Tag[] { new Tag("targetUser", targetUserName.trim()), new Tag("unused") });
		return message;
	}

	/**
	 * Get the user name for whom the temporary password will be generated.
	 *
	 * @return {@code String} with the user name for which the temporary password
	 *         will be issued.
	 */
	public String getTargetUserName() {
		return targetUserName;
	}

}
