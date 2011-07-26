package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

public class UserAdminInp extends AbstractIRODSPackingInstruction {

	/**
	 * $packlets=array('arg0' => 'userpw', 'arg1' => $user, 'arg2' =>
	 * 'password', 'arg3' => $pwdBuf, 'arg4' => '', 'arg5' => '', 'arg6' => '',
	 * 'arg7' => '', "arg8" => '', 'arg9' => '' );
	 * parent::__construct("userAdminInp_PI",$packlets);
	 */

	public static final String PI_TAG = "userAdminInp_PI";
	public static final int USER_ADMIN_INP_API_NBR = 714;

	public static final String USER_PW = "userpw";
	public static final String PASSWORD = "password";
	public static final String BLANK = "";

	public static final String ARG0 = "arg0";
	public static final String ARG1 = "arg1";
	public static final String ARG2 = "arg2";
	public static final String ARG3 = "arg3";
	public static final String ARG4 = "arg4";
	public static final String ARG5 = "arg5";
	public static final String ARG6 = "arg6";
	public static final String ARG7 = "arg7";
	public static final String ARG8 = "arg8";
	public static final String ARG9 = "arg9";

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
	 * Static initializer for a user admin packing instruction used to change a
	 * user password.
	 * 
	 * @param userName
	 *            <code>String</code> with the user name that will have the
	 *            password changed.
	 * @param obfuscatedPassword
	 *            <code>String</code> with the properly obfuscated password. See
	 *            {@link org.irods.jargon.core.security.IRODSPasswordUtilities}
	 *            for obfuscation routines.
	 * @return instance of <code>userAdminInp</code> packing instruction to
	 *         change the password.
	 * @throws JargonException
	 */
	public static UserAdminInp instanceForChangeUserPassword(
			final String userName, final String obfuscatedPassword)
			throws JargonException {

		if (userName == null || userName.isEmpty()) {
			throw new JargonException("userName is null or missing");
		}

		if (obfuscatedPassword == null || obfuscatedPassword.isEmpty()) {
			throw new JargonException("obfuscatedPassword is null or missing");
		}

		return new UserAdminInp(USER_PW, userName, PASSWORD,
				obfuscatedPassword, BLANK, BLANK, BLANK, BLANK, BLANK, BLANK,
				USER_ADMIN_INP_API_NBR);
	}

	private UserAdminInp(final String arg0, final String arg1,
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

	public String getArg0() {
		return arg0;
	}

	public String getArg1() {
		return arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public String getArg3() {
		return arg3;
	}

	public String getArg4() {
		return arg4;
	}

	public String getArg5() {
		return arg5;
	}

	public String getArg6() {
		return arg6;
	}

	public String getArg7() {
		return arg7;
	}

	public String getArg8() {
		return arg8;
	}

	public String getArg9() {
		return arg9;
	}

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
