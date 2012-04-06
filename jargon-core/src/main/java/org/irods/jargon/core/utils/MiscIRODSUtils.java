/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
	 * Given an iRODS path to a file, and an iRODS account, get a default
	 * storage resource name from the iRODS account if that path is in the same
	 * zone as an iRODS account, otherwise, return an empty resource.
	 * <p/>
	 * This is used in places in Jargon so that a default storage resource in an
	 * iRODS account is not propagated to the wrong zone.
	 * 
	 * @param irodsAbsolutePath
	 * @param irodsAccount
	 * @return
	 */
	public static String getDefaultIRODSResourceFromAccountIfFileInZone(
			final String irodsAbsolutePath, final IRODSAccount irodsAccount) {

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
	 * Get the zone name from the provided iRODS absolute path
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file or
	 *            collection
	 * @return <code>String</code> with the zone name, or null if the zone name
	 *         is not in the path (e.g. if the path is just '/')
	 */
	public static String getZoneInPath(final String irodsAbsolutePath) {
		if (irodsAbsolutePath == null) {
			throw new IllegalArgumentException("null  irodsAbsolutePath");
		}

		if (irodsAbsolutePath.isEmpty()) {
			return "";
		}

		List<String> pathComponents = breakIRODSPathIntoComponents(irodsAbsolutePath);

		if (pathComponents.size() <= 1) {
			return null;
		} else {
			return pathComponents.get(1);
		}
	}

	/**
	 * @param irodsAccount
	 * @param inZone
	 * @param pathComponents
	 * @return
	 */
	private static boolean isFirstPartOfPathInZone(
			final IRODSAccount irodsAccount, final List<String> pathComponents) {
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

	/**
	 * Given a list of path components, as produced by the
	 * <code>breakIRODSPathIntoComponents</code>, re-create an iRODS absolute
	 * path by simply stringing together the path components with the iRODS '/'
	 * delimiter.
	 * 
	 * @param pathComponents
	 *            <code>List<String></code> with the iRODS path components.
	 * @param lastIndex
	 *            <code>int</code>, set to -1 if there is no limit, that
	 *            indicates the index of the last component to string together
	 *            into the path
	 * @return <code>String</code> with an iRODS absolute path built from the
	 *         given components.
	 */
	public static String buildPathFromComponentsUpToIndex(
			final List<String> pathComponents, final int lastIndex) {

		if (pathComponents == null) {
			throw new IllegalArgumentException("null pathComponents");
		}

		StringBuilder sb = new StringBuilder();
		// keep track of how many components are processed
		int i = 0;
		for (String pathComponent : pathComponents) {

			// root path will be blank, so ignore, as slashes are already
			// appended
			if (!pathComponent.isEmpty()) {
				// if i've specified a limit to the path components, respect it
				if (lastIndex >= 0) {
					if (i++ >= lastIndex) {
						break;
					}
				}
				sb.append("/");
				sb.append(pathComponent);
			}
		}

		// If I don't get a path, set it to root
		if (sb.length() == 0) {
			sb.append("/");
		}

		return sb.toString();
	}

	/**
	 * Handy method to take the given input stream and make it a String
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static String convertStreamToString(final InputStream inputStream)
			throws Exception {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(inputStream, "UTF-8");
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read > 0) {
				out.append(buffer, 0, read);
			}
		} while (read >= 0);
		String result = out.toString();
		return result;
	}

	/**
	 * Pare off a user name if the given user name is in user#zone format,
	 * there's a complementary method to just get the zone part.
	 * 
	 * @param userName
	 *            <code>String</code> with a user name that can be just a name,
	 *            or a user name in user#zone format.
	 *            <p/>
	 *            This will give you back the user name in any case, and will
	 *            return blank if given blank or null.
	 * @return <code>String</code> with the userName, with any zone info trimmed
	 */
	public static String getUserInUserName(final String userName) {

		if (userName == null || userName.isEmpty()) {
			return "";
		}

		int indexOfPound = userName.indexOf('#');

		if (indexOfPound == -1) {
			return userName;
		} else {
			return userName.substring(0, indexOfPound);
		}
	}

	/**
	 * Pare off a zone name if the given user name is in user#zone format,
	 * there's a complementary method to just get the user part.
	 * 
	 * @param userName
	 *            <code>String</code> with a user name that can be just a name,
	 *            or a user name in user#zone format.
	 *            <p/>
	 *            This will give you back the zone name in any case, and will
	 *            return blank if given blank or null.
	 * @return <code>String</code> with the zone, with any user info trimmed
	 */
	public static String getZoneInUserName(final String userName) {

		if (userName == null || userName.isEmpty()) {
			return "";
		}

		int indexOfPound = userName.indexOf('#');

		if (indexOfPound == -1) {
			return "";
		} else {
			return userName.substring(indexOfPound + 1);
		}
	}

}
