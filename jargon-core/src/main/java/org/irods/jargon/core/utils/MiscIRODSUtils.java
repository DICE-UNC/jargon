/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.PathTooLongException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

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
	 * @return <code>String</code> with the zone name, or blank if the zone name
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
			return "";
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

	/**
	 * Get a displayable byte value from a long value
	 * 
	 * @param bytes
	 * @return
	 */
	public static String humanReadableByteCount(long bytes) {
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		char pre = ("KMGTPE").charAt(exp - 1);
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Create an MD5 Hash of a string value
	 * 
	 * @param input
	 *            <code>String</code> with the value to be converted to an MD5
	 *            Hash
	 * @return <code>String<code> which is the MD5 has of the string.
	 */

	public static String computeMD5HashOfAStringValue(final String input)
			throws JargonException {

		if (input == null || input.isEmpty()) {
			throw new IllegalArgumentException("null or empty input");
		}

		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(input.getBytes());
			byte[] md5 = algorithm.digest();
			return LocalFileUtils.digestByteArrayToString(md5);
		} catch (NoSuchAlgorithmException ex) {
			throw new JargonException(
					"exception creating MD5 Hash of the given string", ex);
		}
	}

	/**
	 * Compute a home directory path in /zone/home/username format given an
	 * <code>IRODSAccount</code>
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return <code>String</code> with a computed home directory path
	 */
	public static String computeHomeDirectoryForIRODSAccount(
			final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(irodsAccount.getZone());
		sb.append("/home/");
		sb.append(irodsAccount.getUserName());
		return sb.toString();
	}

	/**
	 * Compute a home directory path in /zone/home/username format given an
	 * <code>IRODSAccount</code> that describes the zone, and a user name for
	 * the target user.
	 * <p/>
	 * This variant is meant to allow the computation of a home directory for an
	 * arbitrary user based on the zone I'm logged into.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return <code>String</code> with a computed home directory path
	 */
	public static String computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
			final IRODSAccount irodsAccount, final String irodsUserName) {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsUserName == null || irodsUserName.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsUserName");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(irodsAccount.getZone());
		sb.append("/home/");
		sb.append(irodsUserName);
		return sb.toString();
	}

	/**
	 * Helper method for the convention of having a '/zone/home/public'
	 * directory, especially for use by 'anonymous' accounts. Compute a path to
	 * that directory
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} for the logged in user (probably
	 *            anonymous)
	 * @return <code>String</code> in '/zone/home/public' format
	 */
	public static String computePublicDirectory(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(irodsAccount.getZone());
		sb.append("/home/public");
		return sb.toString();
	}

	/**
	 * Utility method to get the last part of the given absolute path
	 * 
	 * @return <code>String</code> with the last component of the absolute path
	 */
	public static String getLastPathComponentForGiveAbsolutePath(
			final String collectionPath) {

		if (collectionPath == null || collectionPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection path");
		}

		String[] paths = collectionPath.split("/");
		if (paths.length == 0) {
			return "";
		}

		return paths[paths.length - 1];

	}

	/**
	 * See if jargon supports the given status
	 * 
	 * @param objStat
	 * @throws JargonException
	 */
	public static void evaluateSpecCollSupport(final ObjStat objStat)
			throws JargonException {
		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			// OK
		} else if (objStat.getSpecColType() == SpecColType.NORMAL) {
			// OK
		} else if (objStat.getSpecColType() == SpecColType.STRUCT_FILE_COLL) {
			// OK
		} else if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			// OK
		} else {
			throw new JargonException(
					"unsupported object type, support not yet available for:"
							+ objStat.getSpecColType());
		}
	}

	/**
	 * Build an absolute path combining the collection parent and the data name,
	 * accounting for the presence or absence of a trailing slash in the
	 * collection path.
	 * 
	 * @param collectionPath
	 *            <code>String</code> with path of the parent collection
	 * @param dataName
	 *            <code>String</code> with file or collection child name
	 * @return <code>String</code> with a properly concatenated file path
	 */
	public static String buildAbsolutePathFromCollectionParentAndFileName(
			final String collectionPath, final String dataName) {

		if (collectionPath == null) {
			throw new IllegalArgumentException("null or empty collectionPath");
		}

		if (dataName == null) {
			throw new IllegalArgumentException("null or empty dataName");
		}

		StringBuilder pathBuilder = new StringBuilder();

		if (collectionPath.isEmpty() && dataName.isEmpty()) {
			pathBuilder.append("/");
		} else {
			pathBuilder.append(collectionPath);
			if (!collectionPath.isEmpty()
					&& collectionPath.charAt(collectionPath.length() - 1) != '/') {
				pathBuilder.append('/');
			}
			pathBuilder.append(dataName);
		}

		return pathBuilder.toString();
	}

	/**
	 * Based on the collection type, determine the absolute path used to query
	 * iRODS. This is meant to handle special collections, such as soft links,
	 * where the iCAT data may be associated with the canonical path
	 * 
	 * @param objStat
	 *            {@link ObjStat} with information on the given iRODS object
	 * @return <code>String</code> with the canonical iRODS path
	 */
	public static String determineAbsolutePathBasedOnCollTypeInObjectStat(
			final ObjStat objStat) {
		String effectiveAbsolutePath = null;

		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			/*
			 * StringBuilder sb = new StringBuilder();
			 * sb.append(objStat.getObjectPath()); sb.append("/");
			 * sb.append(MiscIRODSUtils
			 * .getLastPathComponentForGiveAbsolutePath(objStat
			 * .getAbsolutePath())); effectiveAbsolutePath = sb.toString();
			 */
			effectiveAbsolutePath = objStat.getObjectPath();
		} else {
			effectiveAbsolutePath = objStat.getAbsolutePath();
		}
		return effectiveAbsolutePath;
	}

	/**
	 * Given an absolue path, split it into a parent name and a child name
	 * 
	 * @param filePath
	 *            <code>String</code> with an absolute iRODS path to a
	 *            collection or data object
	 * @return {@link CollectionAndPath} value object
	 */
	public static CollectionAndPath separateCollectionAndPathFromGivenAbsolutePath(
			final String filePath) {

		// used when parsing the filepath
		int index;

		if (filePath == null) {
			throw new NullPointerException("The file name cannot be null");
		}

		String fileName = filePath;// .trim();
		String directory = "";

		if (fileName.length() > 1) { // add to allow path = root "/"
			index = fileName.lastIndexOf('/');
			while ((index == fileName.length() - 1) && (index >= 0)) {
				// remove '/' at end of filename, if exists
				fileName = fileName.substring(0, index);
				index = fileName.lastIndexOf('/');
			}

			// separate directory and file
			if ((index >= 0) && ((fileName.substring(index + 1).length()) > 0)) {
				// have to run setDirectory(...) again
				// because they put filepath info in the filename
				directory = fileName.substring(0, index);
				fileName = fileName.substring(index + 1);
			}
		}

		return new CollectionAndPath(directory, fileName);
	}

	/**
	 * Get a <code>List</code> based on the values of the provided enum. Handy
	 * for creating lists in interfaces and for other purposes
	 * 
	 * @param enumClass
	 *            java <code>enum</code>
	 * @return <code>List<String></code> of enum values
	 * @throws JargonException
	 */
	public static <T extends Enum<T>> List<String> getDisplayValuesFromEnum(
			final Class<T> enumClass) throws JargonException {
		try {
			T[] items = enumClass.getEnumConstants();
			Method accessor = enumClass.getMethod("toString");

			ArrayList<String> names = new ArrayList<String>(items.length);
			for (T item : items) {
				names.add(accessor.invoke(item).toString());
			}

			return names;
		} catch (Exception ex) {
			throw new JargonException("error getting enum vals", ex);
		}
	}

	/**
	 * Count occurrances of a character in a string
	 * 
	 * @param stringToCountOccurrancesIn
	 *            <code>String</code> to count occurrances in
	 * @param characterToCount
	 *            <code>char</code> whose occurrances will be counted
	 * @return <code>int</code> with the count of the given character
	 */
	public static int countCharsInString(
			final String stringToCountOccurrancesIn, final char characterToCount) {

		if (stringToCountOccurrancesIn == null) {
			throw new IllegalArgumentException("null s");
		}

		final char[] chars = stringToCountOccurrancesIn.toCharArray();
		int count = 0;
		for (char c : chars) {
			if (c == characterToCount) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Pad a given string to the right with spaces to a given length
	 * 
	 * @param s
	 *            <code>String</code> to pad
	 * @param n
	 *            <code>int</code> with the amount to pad
	 * @return <code>String</code> padded to specification
	 */
	public static String padRight(final String s, final int n) {
		return String.format("%1$-" + n + "s", s);
	}

	/**
	 * Pad a given string to the left with spaces to a given length
	 * 
	 * @param s
	 *            <code>String</code> to pad
	 * @param n
	 *            <code>int</code> with the amount to pad
	 * @return <code>String</code> padded to specification
	 */
	public static String padLeft(final String s, final int n) {
		return String.format("%1$" + n + "s", s);
	}

	/**
	 * Checks the given path for a length violation
	 * 
	 * @param path
	 *            <code>String</code> with an iRODS path to check
	 * @throws PathTooLongException
	 *             thrown if the path is too long
	 */
	public static void checkPathSizeForMax(final String path)
			throws PathTooLongException {
		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		if (path.length() > ConnectionConstants.MAX_PATH_SIZE) {
			throw new PathTooLongException("Path is too long");
		}

	}

	/**
	 * build a user home directory path (with no trailing slash) based on the
	 * common /zone/home/userName scheme given an iRODS account
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAcocunt} for the given user
	 * @return <code>String</code> with the iRODS user home directory path
	 */
	public static String buildIRODSUserHomeForAccountUsingDefaultScheme(
			final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		StringBuilder sb = new StringBuilder();
		sb.append('/');
		sb.append(irodsAccount.getZone());
		sb.append("/home/");
		sb.append(irodsAccount.getUserName());
		return sb.toString();

	}

	/**
	 * Checks the given parent and child path for a length violation
	 * 
	 * @param parentPath
	 *            <code>String</code> with a parent path
	 * @param childPath
	 *            <code>String</code> with a child path
	 * @throws PathTooLongException
	 *             if the combined path is too long
	 */
	public static void checkPathSizeForMax(final String parentPath,
			final String childPath) throws PathTooLongException {
		if (parentPath == null) {
			throw new IllegalArgumentException("null parentPath");
		}

		if (childPath == null) {
			throw new IllegalArgumentException("null childPath");
		}

		if (childPath.length() + childPath.length() > ConnectionConstants.MAX_PATH_SIZE) {
			throw new PathTooLongException("Path is too long");
		}

	}

	/*
	 * Compare the reported version of the server to a test version number to
	 * see if the actual server is at least at the rev of the test version
	 * 
	 * @param actualVersionOfServer <code>String</code> that represents the
	 * actual version of a server in question, such as is reported by the
	 * startup pack
	 * 
	 * @param thisReleaseVersion <code>String</code> that represents a test
	 * version number, asking if the reported server version is at least this
	 * version
	 * 
	 * @return <code>boolean</code> that would be <code>true</code>
	 */
	public static boolean isTheIrodsServerAtLeastAtTheGivenReleaseVersion(
			final String actualVersionOfServer, final String thisReleaseVersion) {
		if (thisReleaseVersion == null || thisReleaseVersion.length() == 0) {
			throw new IllegalArgumentException(
					"null or empty thisReleaseVersion");
		}

		if (actualVersionOfServer == null
				|| actualVersionOfServer.length() == 0) {
			throw new IllegalArgumentException(
					"null or empty actualVersionOfServer");
		}

		// The result is a negative integer if this String object
		// lexicographically precedes the argument string.
		int compValue = actualVersionOfServer
				.compareToIgnoreCase(thisReleaseVersion);
		return compValue >= 0;

	}

	/**
	 * Create a truncated file name suitable for display in interfaces
	 * 
	 * @param fileName
	 * @return
	 */
	public static final String abbreviateFileName(final String fileName) {

		if (fileName == null) {
			throw new IllegalArgumentException("null fileName");
		}

		StringBuilder sb = new StringBuilder();
		if (fileName.length() < 100) {
			sb.append(fileName);
		} else {
			// gt 100 bytes, redact
			sb.append(fileName.substring(0, 50));
			sb.append(" ... ");
			sb.append(fileName.substring(fileName.length() - 50));
		}

		return sb.toString();

	}

	/**
	 * Wrap the given string in " characters and return it
	 * 
	 * @param string
	 * @return
	 */
	public static final String wrapStringInQuotes(final String string) {
		if (string == null) {
			throw new IllegalArgumentException("null string");
		}

		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("\"");
		sBuilder.append(string);
		sBuilder.append("\"");
		return sBuilder.toString();
	}

}
