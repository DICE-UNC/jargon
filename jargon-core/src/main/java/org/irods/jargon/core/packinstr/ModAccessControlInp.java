/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Packing instruction to modify access permissions for a file. This is an
 * immutable and thread-safe object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ModAccessControlInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "modAccessControlInp_PI";
	public static final int MOD_ACESS_CONTROL_API_NBR = 707;
	public static final String RECURSIVE_FLAG = "recursiveFlag";
	public static final String ACCESS_LEVEL = "accessLevel";
	public static final String USER_NAME = "userName";
	public static final String ZONE = "zone";
	public static final String PATH = "path";

	public static final String READ_PERMISSION = "read";
	public static final String WRITE_PERMISSION = "write";
	public static final String OWN_PERMISSION = "own";

	private final boolean recursive;
	private final String zone;
	private final String absolutePath;
	private final String userName;
	private final String permission;

	/**
	 * Create an instance of the packing instruction to change permissions on
	 * the file.
	 * 
	 * @param recursive
	 *            <code>boolean</code> that indicates whether this is a
	 *            recursive operation
	 * @param zone
	 *            <code>String</code> that gives an optional zone id. Leave
	 *            blank if not used.
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection.
	 * @param userName
	 *            <code>String</code> with the iRODS user name to set
	 *            permissions for.
	 * @param permission
	 *            <code>String</code> of value read, write, or own to describe
	 *            the permission.
	 * @return
	 */
	public static ModAccessControlInp instance(final boolean recursive,
			final String zone, final String absolutePath,
			final String userName, final String permission) {
		return new ModAccessControlInp(MOD_ACESS_CONTROL_API_NBR, recursive,
				zone, absolutePath, userName, permission);
	}

	/**
	 * Private constructor for creating a version of the packing instruction to
	 * set permissions on a collection or data object.
	 * 
	 * @param apiNumber
	 * @param recursive
	 * @param zone
	 * @param absolutePath
	 * @param userName
	 * @param permission
	 */
	private ModAccessControlInp(final int apiNumber, final boolean recursive,
			final String zone, final String absolutePath,
			final String userName, final String permission) {
		super();

		if (apiNumber <= 0) {
			throw new IllegalArgumentException(
					"apiNumber is less than or equal to zero");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolute path");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (permission == null || permission.isEmpty()) {
			throw new IllegalArgumentException("null or empty permission");
		}
		
		if (permission.equals(OWN_PERMISSION) || permission.equals(READ_PERMISSION) || permission.equals(WRITE_PERMISSION)) {
			// OK
		} else {
			throw new IllegalArgumentException("invalid permission");
		}

		this.setApiNumber(apiNumber);
		this.recursive = recursive;
		this.zone = zone;
		this.absolutePath = absolutePath;
		this.userName = userName;
		this.permission = permission;

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

		int recursiveVal;
		if (recursive) {
			recursiveVal = 1;
		} else {
			recursiveVal = 0;
		}

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(RECURSIVE_FLAG, recursiveVal),
				new Tag(ACCESS_LEVEL, permission),
				new Tag(USER_NAME, userName), new Tag(ZONE, zone),
				new Tag(PATH, absolutePath) });

		return message;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public String getZone() {
		return zone;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public String getUserName() {
		return userName;
	}

	public String getPermission() {
		return permission;
	}

}
