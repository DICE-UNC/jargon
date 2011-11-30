/**
 * 
 */
package org.irods.jargon.core.utils;

import java.util.Arrays;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Misc utils for dealing with iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class MiscIRODSUtils {

	/**
	 * Private constructor, don't create instances
	 */
	private MiscIRODSUtils() {
	}

	/**
	 * Given an iRODS path to a file, and an iRODS account, get a default storage resource name from the iRODS account if
	 * that path is in the same zone as an iRODS account, otherwise, return an empty resource.
	 * <p/>
	 * This is used in places in Jargon so that a default storage resource in an iRODS account is not propagated to the wrong zone.
	 * 
	 * @param irodsAbsolutePath
	 * @param irodsAccount
	 * @return
	 */
	public static String getDefaultIRODSResourceFromAccountIfFileInZone(final String irodsAbsolutePath, final IRODSAccount irodsAccount) {
		
		String defaultResource = "";
		
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsAbsolutePath");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		List<String> pathComponents = breakIRODSPathIntoComponents(irodsAbsolutePath);

		boolean inZone = isFirstPartOfPathInZone(irodsAccount, pathComponents);
		
		if (inZone) {
			defaultResource = irodsAccount.getDefaultStorageResource();
		}
		
		return defaultResource;

	}
	
	/**
	 * Determine if the given iRODS absolute path is in the zone of the given
	 * <code>IRODSAccount</code>. This is done by inspecting the path for first
	 * zone part, and doing a string comparison with the zone in the
	 * <code>IRODSAccount</code>.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with an iRODS absolute path.
	 * @param irodsAccount
	 *            {@link IRODSAccount} for the zone in question.
	 * @return <code>true</code> if the file path is in the given zone. This
	 *         does not determine if the path actually exists.
	 */
	public static boolean isFileInThisZone(final String irodsAbsolutePath,
			final IRODSAccount irodsAccount) {

		boolean inZone = true;

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsAbsolutePath");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		List<String> pathComponents = breakIRODSPathIntoComponents(irodsAbsolutePath);

		inZone = isFirstPartOfPathInZone(irodsAccount, pathComponents);

		return inZone;

	}

	/**
	 * @param irodsAccount
	 * @param inZone
	 * @param pathComponents
	 * @return
	 */
	private static boolean isFirstPartOfPathInZone(
			final IRODSAccount irodsAccount, 
			List<String> pathComponents) {
		boolean inZone = true;
		if (pathComponents.isEmpty() || pathComponents.size() == 1) {
			// inZone will remain true, this should be the 'root' directory
		} else {
			inZone = pathComponents.get(1).equals(irodsAccount.getZone());
		}
		return inZone;
	}

	/**
	 * Handy method to break an iRODS absolute path into the component
	 * directories.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with an iRODS absolute path.
	 * @return <code>List<String></code> with the component path elements (the /
	 *         path separator will be removed).
	 */
	public static List<String> breakIRODSPathIntoComponents(
			final String irodsAbsolutePath) {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		String[] components = irodsAbsolutePath.split("/");
		return Arrays.asList(components);

	}

}
