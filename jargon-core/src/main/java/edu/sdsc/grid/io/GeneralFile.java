//  Copyright (c) 2005, Regents of the University of California
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//    * Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//    * Neither the name of the University of California, San Diego (UCSD) nor
//  the names of its contributors may be used to endorse or promote products
//  derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//  GeneralFile.java  -  edu.sdsc.grid.io.GeneralFile
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralFile
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract representation of file and directory pathnames. This abstract
 * class can be subclassed to create a generic file object which may refer to
 * either a remote or local file.
 * <P>
 * Shares many similarities with the java.io.File class: User interfaces and
 * operating systems use system-dependent pathname strings to name files and
 * directories. This class presents an abstract, system-independent view of
 * hierarchical pathnames. An abstract pathname has two components:
 * <P>
 * An optional system-dependent prefix string, such as a disk-drive specifier,
 * "/" for the UNIX root directory, or "\\" for a Microsoft Windows UNC
 * pathname, and <br>
 * A sequence of zero or more string names. Each name in an abstract pathname
 * except for the last denotes a directory; the last name may denote either a
 * directory or a file. The empty abstract pathname has no prefix and an empty
 * name sequence. The conversion of a pathname string to or from an abstract
 * pathname is inherently system-dependent. When an abstract pathname is
 * converted into a pathname string, each name is separated from the next by a
 * single copy of the default separator character. The default name-separator
 * character is defined by the system property file.separator, and is made
 * available in the public static fields separator and separatorChar of this
 * class. When a pathname string is converted into an abstract pathname, the
 * names within it may be separated by the default name-separator character or
 * by any other name-separator character that is supported by the underlying
 * system.
 * <P>
 * A pathname, whether abstract or in string form, may be either absolute or
 * relative. An absolute pathname is complete in that no other information is
 * required in order to locate the file that it denotes. A relative pathname, in
 * contrast, must be interpreted in terms of information taken from some other
 * pathname. By default the classes in this package always resolve relative
 * pathnames against the current user directory. This directory is named by the
 * system property user.dir, and is typically the directory in which the Java
 * virtual machine was invoked.
 * <P>
 * The prefix concept is used to handle root directories on UNIX platforms, and
 * drive specifiers, root directories and UNC pathnames on Microsoft Windows
 * platforms, as follows:
 * <P>
 * For UNIX platforms, the prefix of an absolute pathname is always "/".
 * Relative pathnames have no prefix. The abstract pathname denoting the root
 * directory has the prefix "/" and an empty name sequence.
 * <P>
 * For Microsoft Windows platforms, the prefix of a pathname that contains a
 * drive specifier consists of the drive letter followed by ":" and possibly
 * followed by "\" if the pathname is absolute. The prefix of a UNC pathname is
 * "\\"; the hostname and the share name are the first two names in the name
 * sequence. A relative pathname that does not specify a drive has no prefix.
 * <P>
 * Instances of the GeneralFile class are immutable; that is, once created, the
 * abstract pathname represented by a GeneralFile object will never change.
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 * @see java.io.File
 */
public class GeneralFile extends Object implements Comparable {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	/**
	 * Default buffer size used for copyTo and copyFrom methods.
	 * 
	 * @deprecate It is just an int, 65535. Make your own.
	 */
	public final static int BUFFER_MAX_SIZE = 65535; // arbitrary size

	public static enum Checksum {
		SYSV, MD5
	};

	/**
	 * The system-dependent default name-separator character. This field is
	 * initialized to contain the first character of the value of the system
	 * property <code>file.separator</code>. On UNIX systems the value of this
	 * field is <code>'/'</code>; on Microsoft Windows systems it is
	 * <code>'\\'</code>.
	 * 
	 * @see java.lang.System#getProperty(java.lang.String)
	 */
	public static char separatorChar = '/';

	/**
	 * The system-dependent default name-separator character, represented as a
	 * string for convenience. This string contains a single character, namely
	 * <code>{@link #separatorChar}</code>.
	 */
	public static String separator = "" + separatorChar;

	/**
	 * The system-dependent path-separator character. This field is initialized
	 * to contain the first character of the value of the system property
	 * <code>path.separator</code>. This character is used to separate filenames
	 * in a sequence of files given as a <em>path list</em>. On UNIX systems,
	 * this character is <code>':'</code>; on Microsoft Windows systems it is
	 * <code>';'</code>.
	 * 
	 * @see java.lang.System#getProperty(java.lang.String)
	 */
	public static char pathSeparatorChar = ':';

	/**
	 * The system-dependent path-separator character, represented as a string
	 * for convenience. This string contains a single character, namely
	 * <code>{@link #pathSeparatorChar}</code>.
	 */
	public static String pathSeparator = "" + pathSeparatorChar;

	/**
	 * Standard path separator character represented as a string for
	 * convenience. This string contains a single character, namely
	 * <code>{@link #PATH_SEPARATOR_CHAR}</code>.
	 * 
	 * @deprecate used a variable and name not matching the java.io package. Use
	 *            separator and pathSeparator
	 */
	public static String PATH_SEPARATOR = "/";

	/**
	 * The path separator character.
	 * 
	 * @deprecate used a variable and name not matching the java.io package. Use
	 *            separatorChar and pathSeparatorChar
	 */
	public static char PATH_SEPARATOR_CHAR = '/';

	private static Logger log = LoggerFactory.getLogger(GeneralFile.class);

	/**
	 * Holds the server information used by this file.
	 */
	protected GeneralFileSystem fileSystem;

	/**
	 * Standardized, absolute directory string
	 */
	protected Vector directory;

	/**
	 * Standardized filename
	 */
	protected String fileName;

	/**
	 * Creates a new GeneralFile from a parent pathname string and a child
	 * pathname string.
	 * <P>
	 * If parent is null then the new GeneralFile instance is created as if by
	 * invoking the single-argument GeneralFile constructor on the given child
	 * pathname string.
	 * <P>
	 * Otherwise the parent pathname string is taken to denote a directory, and
	 * the child pathname string is taken to denote either a directory or a
	 * file. If the child pathname string is absolute then it is converted into
	 * a relative pathname in a way defined by the subclass. If parent is the
	 * empty string then the new GeneralFile is created by converting child into
	 * an abstract pathname and resolving the result against the default
	 * directory of the subclass. Otherwise each pathname string is converted
	 * into an abstract pathname and the child abstract pathname is resolved
	 * against the parent.
	 * <P>
	 * 
	 * @param fileSystem
	 *            The fileSystem used by this object
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 */
	protected GeneralFile(final GeneralFileSystem fileSystem, final String dir,
			final String fileName) throws NullPointerException {
		setFileSystem(fileSystem);
		setDirectory(dir);
		setFileName(fileName);
	}

	/**
	 * Creates a new GeneralFile instance by converting the given file: URI into
	 * an abstract pathname.
	 * <P>
	 * The exact form of a file: URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * <P>
	 * For a given abstract pathname f it is guaranteed that
	 * <P>
	 * &nbsp;&nbsp;&nbsp;&nbsp;new GeneralFile( f.toURI()).equals( f)
	 * <P>
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a file: URI that is created in a virtual machine on one operating
	 * system is converted into an abstract pathname in a virtual machine on a
	 * different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI using a supported scheme, eg.
	 *            "file" or "srb".
	 * @throws NullPointerException
	 *             if <code>uri</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold.
	 */
	protected GeneralFile(final URI uri) throws NullPointerException,
			IllegalArgumentException {
		super();
	}

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (fileSystem != null) {
			fileSystem = null;
		}
		if (directory != null) {
			directory = null;
		}
		if (fileName != null) {
			fileName = null;
		}
	}

	/**
	 * Sets the file system used of this GeneralFile object. The file system
	 * object must be a subclass of the GeneralFileSystem matching this file
	 * object. eg. XYZFile requires XYZFileSystem.
	 * 
	 * @param fileSystem
	 *            The file system to be used.
	 * @throws IllegalArgumentException
	 *             - if the argument is null.
	 * @throws ClassCastException
	 *             - if the argument is not an object of the approriate
	 *             subclass.
	 */
	protected void setFileSystem(final GeneralFileSystem fileSystem)
			throws IllegalArgumentException, ClassCastException {
		if (fileSystem == null) {
			throw new IllegalArgumentException(
					"Illegal fileSystem, cannot be null");
		}

		this.fileSystem = fileSystem;
	}

	/**
	 * Set the file name.
	 * 
	 * @param fleName
	 *            The file name or fileName plus some or all of the directory
	 *            path.
	 */
	protected void setFileName(String filePath) {
		// used when parsing the filepath
		int index;

		// in case they used the local pathSeparator
		// in the fileName instead of the PATH_SEPARATOR.
		String localSeparator = System.getProperty("file.separator");

		if (filePath == null) {
			throw new NullPointerException("The file name cannot be null");
		}

		// replace local separators with PATH_SEPARATOR.
		if (!localSeparator.equals(PATH_SEPARATOR)) {
			index = filePath.lastIndexOf(localSeparator);
			while ((index >= 0)
					&& ((filePath.substring(index + 1).length()) > 0)) {
				filePath = filePath.substring(0, index) + PATH_SEPARATOR
						+ filePath.substring(index + 1);
				index = filePath.lastIndexOf(localSeparator);
			}
		}
		fileName = filePath;

		if (fileName.length() > 1) { // add to allow path = root "/"
			index = fileName.lastIndexOf(PATH_SEPARATOR);
			while ((index == fileName.length() - 1) && (index >= 0)) {
				// remove '/' at end of filename, if exists
				fileName = fileName.substring(0, index);
				index = fileName.lastIndexOf(PATH_SEPARATOR);
			}

			// separate directory and file
			if ((index >= 0) && ((fileName.substring(index + 1).length()) > 0)) {
				// have to run setDirectory(...) again
				// because they put filepath info in the filename
				setDirectory(fileName.substring(0, index + 1));
				fileName = fileName.substring(index + 1);
			}
		}
	}

	/**
	 * Set the directory.
	 * 
	 * @param dir
	 *            The directory path, need not be absolute.
	 */
	// though everything will be converted to a canonical path to avoid errors
	protected void setDirectory(String dir) {
		if (directory == null) {
			directory = new Vector();
		}
		if (dir == null) {
			directory.add("");
			return;
		}
		if (!dir.startsWith(PATH_SEPARATOR)) {
			directory.add("");
		}

		// in case they used the local pathSeparator
		// in the fileName instead of the PATH_SEPARATOR.
		String localSeparator = System.getProperty("file.separator");
		int index = dir.lastIndexOf(localSeparator);
		if ((index >= 0) && ((dir.substring(index + 1).length()) > 0)) {
			dir = dir.substring(0, index) + PATH_SEPARATOR
					+ dir.substring(index + 1);
			index = dir.lastIndexOf(localSeparator);
		}

		while ((directory.size() > 0) && // only if this is the dir cut from
				// fileName
				dir.startsWith(PATH_SEPARATOR))// && //strip these
		// (dir.length() > 1)) //but not if they only wanted
		{
			dir = dir.substring(1);
			// problems if dir passed from filename starts with PATH_SEPARATOR
		}

		// create directory vector
		index = dir.indexOf(PATH_SEPARATOR);

		if (index >= 0) {
			do {
				directory.add(dir.substring(0, index));
				do {
					dir = dir.substring(index + 1);
					index = dir.indexOf(PATH_SEPARATOR);
				} while (index == 0);
			} while (index >= 0);
		}

		// add the last path item
		if ((!dir.equals("")) && (dir != null)) {
			directory.add(dir);
		}
	}

	/**
	 * This abstract method gets the path separator as defined by the subclass.
	 */
	public String getPathSeparator() {
		return PATH_SEPARATOR;
	}

	/**
	 * This abstract method gets the path separator char as defined by the
	 * subclass.
	 */
	public char getPathSeparatorChar() {
		return PATH_SEPARATOR_CHAR;
	}

	/**
	 * @return fileSystem the SRBFileSystem object of this SRBFile.
	 * @throws NullPointerException
	 *             if fileSystem is null.
	 */
	public GeneralFileSystem getFileSystem() throws NullPointerException {
		if (fileSystem != null) {
			return fileSystem;
		}

		throw new NullPointerException("fileSystem is null.");
	}

	/**
	 * Returns true if the filesystem can be queried, ie. if any of the query()
	 * might return a non-null value.
	 */
	public static boolean isQueryable() {
		return false;
	}

	/**
	 * Queries metadata specific to this file object, selecting one metadata
	 * value, <code>fieldName</code>, and returns the first result of that
	 * query. Returns null if the query had no results.
	 * 
	 * @param fieldName
	 *            The string name used to form the select object.
	 * @return The metadata values for this file refered to by
	 *         <code>fieldName</code>
	 */
	public String firstQueryResult(final String fieldName) throws IOException {
		MetaDataSelect[] temp = { MetaDataSet.newSelection(fieldName) };
		MetaDataRecordList[] rl = query(temp);
		if (rl != null) {
			return rl[0].getStringValue(0);
		}
		return null;
	}

	/**
	 * Queries the file server to find all files that match a set of conditions.
	 * For all those that match, the fields indicated in the select array are
	 * returned in the result object.
	 * <P>
	 * While condition and select array objects have all been checked for
	 * self-consistency during their construction, there are additional problems
	 * that must be detected at query time:
	 * <P>
	 * <ul>
	 * <li>Redundant selection fields
	 * <li>Redundant query fields
	 * <li>Fields incompatible with a file server
	 * </ul>
	 * <P>
	 * For instance, it is possible to build a condition object appropriate for
	 * the SRB, then pass that object in a local file system query. That will
	 * find that the condition is incompatible and generate a mismatch
	 * exception.
	 * <P>
	 * Query is implemented by the file-server-specific classes, like that for
	 * the SRB, FTP, etc. Those classes must re-map condition and select field
	 * names and operator codes to those required by a particular file server
	 * and protocol version. Once re-mapped, they issue the query and get
	 * results. The results are then mapped back to the standard public field
	 * names of the MetaDataGroups. So, if a MetaDataGroup uses a name like
	 * "file path", but the SRB calls it "data name", then query maps first from
	 * "file path" to "data name" before issuing the query, and then from
	 * "data name" back to "file path" within the results. The programmer using
	 * this API should never see the internal field names.
	 */
	public MetaDataRecordList[] query(MetaDataCondition[] conditions,
			final MetaDataSelect[] selects) throws IOException {
		// doesn't need to use getZoneCondition() because will always have a
		// DIRECTORY_NAME condition

		MetaDataCondition iConditions[] = null;
		String fieldName = null;
		int operator = MetaDataCondition.EQUAL;
		String value = null;
		int conditionsLength = 0;
		if (conditions == null) {
			conditions = new MetaDataCondition[0];
		} else {
			conditionsLength = conditions.length;
		}

		if (isDirectory()) {
			iConditions = new MetaDataCondition[conditionsLength + 1];

			System.arraycopy(conditions, 0, iConditions, 0, conditionsLength);

			fieldName = StandardMetaData.DIRECTORY_NAME;
			operator = MetaDataCondition.EQUAL;
			value = getAbsolutePath();
			iConditions[conditionsLength] = MetaDataSet.newCondition(fieldName,
					operator, value);
		} else {
			iConditions = new MetaDataCondition[conditionsLength + 3];

			System.arraycopy(conditions, 0, iConditions, 0, conditionsLength);

			fieldName = StandardMetaData.DIRECTORY_NAME;
			operator = MetaDataCondition.EQUAL;
			value = getParent();
			iConditions[conditionsLength] = MetaDataSet.newCondition(fieldName,
					operator, value);

			fieldName = StandardMetaData.FILE_NAME;
			value = fileName;
			iConditions[conditionsLength + 1] = MetaDataSet.newCondition(
					fieldName, operator, value);

			// else last condition = null, will be ignored
		}

		return fileSystem.query(iConditions, selects);
	}

	public MetaDataRecordList[] query(final MetaDataSelect[] selectArray)
			throws IOException {
		return query(null, selectArray);
	}

	public MetaDataRecordList[] query(final String[] metaDataFieldNames)
			throws IOException {
		MetaDataSelect[] selectArray = new MetaDataSelect[metaDataFieldNames.length];
		for (int i = 0; i < metaDataFieldNames.length; i++) {
			selectArray[i] = MetaDataSet.newSelection(metaDataFieldNames[i]);
		}
		return query(null, selectArray);
	}

	/**
	 * Used to modify the metadata associated with this file object.
	 */
	public void modifyMetaData(final MetaDataRecordList metaDataRecordList)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Copies this file to another file. This object is the source file. The
	 * destination file is given as the argument. If the destination file, does
	 * not exist a new one will be created. Otherwise the source file will be
	 * appended to the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public void copyTo(final GeneralFile file) throws IOException {
		copyTo(file, false);
	}

	/**
	 * Copies this file to another file. This object is the source file. The
	 * destination file is given as the argument. If the destination file, does
	 * not exist a new one will be created. Otherwise the source file will be
	 * appended to the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public void copyTo(GeneralFile file, final boolean forceOverwrite)
			throws IOException {
		byte buffer[] = null;
		GeneralFileInputStream in = null;
		GeneralFileOutputStream out = null;
		int n;

		if (file == null) {
			throw new NullPointerException();
		}

		if (isDirectory()) {
			// recursive copy
			GeneralFile[] fileList = listFiles();

			file.mkdir();
			if (fileList != null) {
				for (GeneralFile element : fileList) {
					element.copyTo(
							FileFactory.newFile(file.getFileSystem(),
									file.getAbsolutePath(), element.getName()),
							forceOverwrite);
				}
			}
		} else {
			if (file.isDirectory()) {
				// change the destination from a directory to a file
				file = FileFactory.newFile(file, getName());
			}
			long ilength = length();

			in = FileFactory.newFileInputStream(this);
			out = FileFactory.newFileOutputStream(file);

			if (file.exists()) {
				if (forceOverwrite) {
					// eh, probably the best
					file.delete();
					out = FileFactory.newFileOutputStream(file);
				} else {
					// better if fails
					throw new SecurityException(
							"No permissions to access this file.");
				}
			}

			if (ilength > BUFFER_MAX_SIZE) {
				buffer = new byte[BUFFER_MAX_SIZE];
				do {
					n = in.read(buffer);
					if (n >= 0) {
						ilength -= n;
					} else {
						throw new EOFException();
					}
					out.write(buffer, 0, n);
				} while (ilength > BUFFER_MAX_SIZE);
			}
			buffer = new byte[(int) ilength];
			do {
				n = in.read(buffer);
				if (n >= 0) {
					ilength -= n;
				} else {
					throw new EOFException();
				}
				out.write(buffer, 0, n);
			} while (ilength > 0);

			in.close();
			out.close();
		}
	}

	/**
	 * Copies this file to another file. This object is the source file. The
	 * destination file is given as the argument. If the destination file, does
	 * not exist a new one will be created. Otherwise the source file will be
	 * appended to the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public void copyFrom(final GeneralFile file) throws IOException {
		copyFrom(file, false);
	}

	/**
	 * Copies this file to another file. This object is the source file. The
	 * destination file is given as the argument. If the destination file, does
	 * not exist a new one will be created. Otherwise the source file will be
	 * appended to the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public void copyFrom(final GeneralFile file, final boolean forceOverwrite)
			throws IOException {
		/*
		 * I didn't really want to have a .copyFrom(...), but without the
		 * copyFrom, new packages writers which don't have access to the JARGON
		 * source can't optimize for upload. Such as, localFile.copyTo( srbFile
		 * ) wouldn't be able to use the parallel upload.
		 */
		byte buffer[] = null;
		GeneralRandomAccessFile in = null;
		GeneralRandomAccessFile out = null;

		if (file == null) {
			throw new NullPointerException();
		}

		if (file.isDirectory()) {
			// recursive copy
			GeneralFile[] fileList = file.listFiles();

			mkdir();
			if (fileList != null) {
				for (GeneralFile element : fileList) {
					FileFactory.newFile(this, element.getName()).copyFrom(
							element, forceOverwrite);
				}
			}
		} else {
			if (isDirectory()) {
				// change the destination from a directory to a file
				GeneralFile subFile = FileFactory.newFile(this, file.getName());
				subFile.copyFrom(file);
				return;
			}
			long ilength = file.length();

			in = FileFactory.newRandomAccessFile(file, "r");
			out = FileFactory.newRandomAccessFile(this, "rw");

			if (forceOverwrite) {
				try {
					out.setLength(length());
				} catch (UnsupportedOperationException e) {
					// some filesystems have trouble with truncating.
					delete();
				}
			} else {
				// if file already existed, append
				out.seek(out.length());
			}

			if (ilength > BUFFER_MAX_SIZE) {
				buffer = new byte[BUFFER_MAX_SIZE];
				do {
					in.read(buffer);
					out.write(buffer);

					ilength -= BUFFER_MAX_SIZE;
				} while (ilength > BUFFER_MAX_SIZE);
			}
			buffer = new byte[(int) ilength];
			in.read(buffer);
			out.write(buffer);

			in.close();
			out.close();
		}
	}

	/**
	 * If this abstract pathname refers to a file (and not a directory,) return
	 * the checksum of the file. [Currently] uses SysV, UNIX System V to create
	 * the checksum value.
	 * 
	 * @return the checksum value. Returns null if this object is a directory.
	 */
	public String checksum() throws IOException {
		if (isFile()) {
			long value = 0;
			long length = 0;
			GeneralFileInputStream in = null;

			try {
				in = FileFactory.newFileInputStream(this);

				int n = 0;
				byte[] buffer = new byte[BUFFER_MAX_SIZE];
				while ((n = in.read(buffer)) > -1) {
					for (int i = 0; i < n; i++) {
						value += buffer[i] & 0xFF;
						length++;
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
			if ((length - length()) != 0) {
			}

			long sigh = (value & 0xffff)
					+ (((value & 0xffffffff) >> 16) & 0xffff);
			value = (sigh & 0xffff) + (sigh >> 16);

			return "" + value;
		}
		return null;
	}

	protected String checksumMD5() throws IOException {
		GeneralFileInputStream in = FileFactory.newFileInputStream(this);
		byte[] input = new byte[BUFFER_MAX_SIZE];
		long length = this.length();
		int read = 0;
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			SecurityException se = new SecurityException(
					"MD5 Algorithm unavailable");
			se.initCause(e);
			throw se;
		}

		while (length > 0) {
			read = in.read(input);
			if (read > 0) {
				length -= read;
				if (length < 0) {
					// end of transfer, check the extra bit starts with 9999

					// subtract the status message, an int = 9999, and a bunch
					// of 0's
					md5.update(input, 0, read + (int) length);
				} else {
					md5.update(input, 0, read);
				}
			} else if (read < 0) {
				log.warn("read value less then zero, logged and ignored..value:"
						+ read);
			}
		}

		StringBuffer checksum = new StringBuffer();
		for (byte b : md5.digest()) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) {
				checksum.append('0');
			}

			checksum.append(hex);
		}

		return checksum.toString();
	}

	public String checksum(final Checksum type) throws IOException {
		switch (type) {
		case MD5:
			return checksumMD5();
		default:
			return checksum();
		}
	}

	/**
	 * Get the permissions of the current user for this file: write, read, all,
	 * or null.
	 * <P>
	 * 
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public String getPermissions() throws IOException {
		String permission = null;
		if (canRead()) {
			permission = "read";
		}
		if (canWrite()) {
			if (permission != null) {
				permission = "all";
			} else {
				permission = "write";
			}
		}

		return permission;
	}

	// ----------------------------------------------------------------------
	// java.io.File Methods
	// ----------------------------------------------------------------------
	/**
	 * Tests whether the application can read the file denoted by this abstract
	 * pathname.
	 * 
	 * @return <code>true</code> if and only if the file specified by this
	 *         abstract pathname exists <em>and</em> can be read; otherwise
	 *         <code>false</code>.
	 */
	public boolean canRead() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests whether the application can modify to the file denoted by this
	 * abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the file system actually
	 *         contains a file denoted by this abstract pathname <em>and</em>
	 *         the application is allowed to write to the file; otherwise
	 *         <code>false</code>.
	 */
	public boolean canWrite() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Compares two abstract pathnames lexicographically.
	 * 
	 * @param pathname
	 *            The abstract pathname to be compared to this abstract pathname
	 * 
	 * @return Zero if the argument is equal to this abstract pathname, a value
	 *         less than zero if this abstract pathname is lexicographically
	 *         less than the argument, or a value greater than zero if this
	 *         abstract pathname is lexicographically greater than the argument
	 */
	public int compareTo(final GeneralFile pathName) {
		return (pathName.getAbsolutePath()).compareTo(getAbsolutePath());
	}

	/**
	 * Compares this abstract pathname to another object. If the other object is
	 * an abstract pathname, then this function behaves like <code>{@link
	 * #compareTo(GeneralFile)}</code> . Otherwise, it throws a
	 * <code>ClassCastException</code>, since abstract pathnames can only be
	 * compared to abstract pathnames.
	 * 
	 * @param o
	 *            The <code>Object</code> to be compared to this abstract
	 *            pathname
	 * 
	 * @return If the argument is an abstract pathname, returns zero if the
	 *         argument is equal to this abstract pathname, a value less than
	 *         zero if this abstract pathname is lexicographically less than the
	 *         argument, or a value greater than zero if this abstract pathname
	 *         is lexicographically greater than the argument
	 *         <P>
	 * @throws ClassCastException
	 *             - if the argument is not an abstract pathname
	 */
	@Override
	public int compareTo(final Object o) throws ClassCastException {
		return compareTo((GeneralFile) o);
	}

	/**
	 * Atomically creates a new, empty file named by this abstract pathname if
	 * and only if a file with this name does not yet exist.
	 */
	public boolean createNewFile() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates an empty file in the default temporary-file directory, using the
	 * given prefix and suffix to generate its name. Invoking this method is
	 * equivalent to invoking <code>{@link #createTempFile(java.lang.String,
	 * java.lang.String, GeneralFile)
	 * createTempFile(prefix,&nbsp;suffix,&nbsp;null)}</code> .
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * @deprecated Doesn't always work without existing GeneralFile. Instead use
	 *             GeneralFile.createTempFile(String, String, GeneralFile)
	 * 
	 * @return An abstract pathname denoting a newly-created empty file
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 */
	@Deprecated
	public static GeneralFile createTempFile(final String prefix,
			final String suffix) throws IOException, IllegalArgumentException {
		return createTempFile(prefix, suffix, null);
	}

	/**
	 * <p>
	 * Creates a new empty file in the specified directory, using the given
	 * prefix and suffix strings to generate its name. If this method returns
	 * successfully then it is guaranteed that:
	 * 
	 * <ol>
	 * <li>The file denoted by the returned abstract pathname did not exist
	 * before this method was invoked, and
	 * <li>Neither this method nor any of its variants will return the same
	 * abstract pathname again in the current invocation of the virtual machine.
	 * </ol>
	 * 
	 * This method provides only part of a temporary-file facility. To arrange
	 * for a file created by this method to be deleted automatically, use the
	 * <code>{@link #deleteOnExit}</code> method.
	 * 
	 * <p>
	 * The <code>prefix</code> argument must be at least three characters long.
	 * It is recommended that the prefix be a short, meaningful string such as
	 * <code>"hjb"</code> or <code>"mail"</code>. The <code>suffix</code>
	 * argument may be <code>null</code>, in which case the suffix
	 * <code>".tmp"</code> will be used.
	 * 
	 * <p>
	 * To create the new file, the prefix and the suffix may first be adjusted
	 * to fit the limitations of the underlying platform. If the prefix is too
	 * long then it will be truncated, but its first three characters will
	 * always be preserved. If the suffix is too long then it too will be
	 * truncated, but if it begins with a period character (<code>'.'</code>)
	 * then the period and the first three characters following it will always
	 * be preserved. Once these adjustments have been made the name of the new
	 * file will be generated by concatenating the prefix, five or more
	 * internally-generated characters, and the suffix.
	 * 
	 * <p>
	 * If the <code>directory</code> argument is <code>null</code> then the
	 * system-dependent default temporary-file directory will be used. The
	 * default temporary-file directory is specified by the system property
	 * <code>java.io.tmpdir</code>. On UNIX systems the default value of this
	 * property is typically <code>"/tmp"</code> or <code>"/var/tmp"</code>; on
	 * Microsoft Windows systems it is typically <code>"c:\\temp"</code>. A
	 * different value may be given to this system property when the Java
	 * virtual machine is invoked, but programmatic changes to this property are
	 * not guaranteed to have any effect upon the the temporary directory used
	 * by this method.
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * @param directory
	 *            The directory in which the file is to be created, or
	 *            <code>null</code> if the default temporary-file directory is
	 *            to be used
	 * 
	 * @return An abstract pathname denoting a newly-created empty file
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 */
	public static GeneralFile createTempFile(final String prefix,
			final String suffix, final GeneralFile directory)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname. If this
	 * pathname denotes a directory, then the directory must be empty in order
	 * to be deleted.
	 */
	public boolean delete() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Requests that the file or directory denoted by this abstract pathname be
	 * deleted when the virtual machine terminates. Deletion will be attempted
	 * only for normal termination of the virtual machine, as defined by the
	 * Java Language Specification.
	 * 
	 * <p>
	 * Once deletion has been requested, it is not possible to cancel the
	 * request. This method should therefore be used with care.
	 * 
	 * <P>
	 * Note: this method should <i>not</i> be used for file-locking, as the
	 * resulting protocol cannot be made to work reliably. T
	 */
	public void deleteOnExit() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests this abstract pathname for equality with the given object. Returns
	 * <code>true</code> if and only if the argument is not <code>null</code>
	 * and is an abstract pathname that denotes the same file or directory as
	 * this abstract pathname.
	 * 
	 * @param obj
	 *            The object to be compared with this abstract pathname
	 * 
	 * @return <code>true</code> if and only if the objects are the same;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests whether the file denoted by this abstract pathname exists.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this
	 *         abstract pathname exists; <code>false</code> otherwise
	 */
	public boolean exists() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the absolute form of this abstract pathname. Equivalent to
	 * <code>new&nbsp;GeneralFile(this.{@link #getAbsolutePath}())</code>.
	 * 
	 * @return The absolute abstract pathname denoting the same file or
	 *         directory as this abstract pathname
	 */
	public GeneralFile getAbsoluteFile() {
		return FileFactory.newFile(fileSystem, getAbsolutePath());
	}

	/**
	 * Returns the absolute pathname string of this abstract pathname.
	 * 
	 * @return The absolute pathname denoting the same file or directory as this
	 *         abstract pathname
	 */
	public String getAbsolutePath() {
		if ((directory != null) && (!directory.isEmpty())) {
			int size = directory.size();
			String path = (String) directory.firstElement();
			int i = 1;

			while (i < size) {
				path += getPathSeparator() + directory.get(i);
				i++;
			}

			return path + getPathSeparator() + getName();
		}

		String name = getName();
		if (name == null || name.equals("")) {
			// just in case the dir and name are empty, return root.
			name = fileSystem.getRootDirectories()[0];
		}
		return name;
	}

	/**
	 * Returns the canonical form of this abstract pathname. Equivalent to
	 * <code>new&nbsp;GeneralFile(this.{@link #getCanonicalPath}())</code>.
	 * 
	 * @return The canonical abstract pathname denoting the same file or
	 *         directory as this abstract pathname
	 * 
	 * @throws IOException
	 *             If an I/O error occurs, which is possible because the
	 *             construction of the canonical pathname may require filesystem
	 *             queries
	 */
	public GeneralFile getCanonicalFile() throws IOException {
		return FileFactory.newFile(fileSystem, getCanonicalPath());
	}

	/**
	 * Returns the canonical pathname string of this abstract pathname.
	 * 
	 * @return The canonical pathname string denoting the same file or directory
	 *         as this abstract pathname
	 * 
	 * @throws IOException
	 *             If an I/O error occurs, which is possible because the
	 *             construction of the canonical pathname may require filesystem
	 *             queries
	 */
	public String getCanonicalPath() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The name of the file or directory denoted by this abstract
	 *         pathname.
	 */
	public String getName() {
		return fileName;
	}

	/**
	 * @return The pathname string of this abstract pathname's parent, or null
	 *         if this pathname does not name a parent directory.
	 */
	public String getParent() {
		if ((directory != null) && (!directory.isEmpty())) {
			int size = directory.size();
			String path = (String) directory.firstElement();
			int i = 1;

			while (i < size) {
				path += separator + directory.get(i);
				i++;
			}

			return path;
		}

		return null;
	}

	/**
	 * Returns the abstract pathname of this abstract pathname's parent, or
	 * <code>null</code> if this pathname does not name a parent directory.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's
	 * prefix, if any, and each name in the pathname's name sequence except for
	 * the last. If the name sequence is empty then the pathname does not name a
	 * parent directory.
	 * 
	 * @return The abstract pathname of the parent directory named by this
	 *         abstract pathname, or <code>null</code> if this pathname does not
	 *         name a parent
	 */
	public GeneralFile getParentFile() {
		return FileFactory.newFile(fileSystem, getParent());
	}

	/**
	 * @return This abstract pathname as a pathname string.
	 */
	public String getPath() {
		return getAbsolutePath();
	}

	/**
	 * Tests whether this abstract pathname is absolute in a system-dependant
	 * way.
	 * 
	 * @return <code>true</code> if this abstract pathname is absolute,
	 *         <code>false</code> otherwise
	 */
	public boolean isAbsolute() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this
	 *         abstract pathname exists <em>and</em> is a directory;
	 *         <code>false</code> otherwise
	 */
	public boolean isDirectory() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a normal
	 * file.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this
	 *         abstract pathname exists <em>and</em> is a normal file;
	 *         <code>false</code> otherwise
	 */
	public boolean isFile() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests whether the file named by this abstract pathname is a hidden file.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this
	 *         abstract pathname is hidden.
	 */
	public boolean isHidden() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the time that the file denoted by this abstract pathname was last
	 * modified.
	 * 
	 * @return A <code>long</code> value representing the time the file was last
	 *         modified, measured in system-dependent way.
	 */
	public long lastModified() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the length of the file denoted by this abstract pathname. The
	 * return value is unspecified if this pathname denotes a directory.
	 * 
	 * @return The length, in bytes, of the file denoted by this abstract
	 *         pathname, or <code>0L</code> if the file does not exist
	 */
	public long length() {
		long length = 0;
		String result = null;
		try {
			result = firstQueryResult(GeneralMetaData.SIZE);
		} catch (IOException e) {
			log.warn("IO Exception, logged and ignored");
			return 0;
		}

		if (result != null) {
			length = Long.parseLong(result);
		}

		return length;
	}

	/**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname.
	 * <P>
	 * There is no guarantee that the name strings in the resulting array will
	 * appear in any specific order; they are not, in particular, guaranteed to
	 * appear in alphabetical order.
	 * <P>
	 * If this GeneralFile object denotes a file, the results are unspecified.
	 * 
	 * @return An array of strings naming the files and directories in the
	 *         directory denoted by this abstract pathname.
	 */
	public String[] list() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname and which match a query
	 * formed using these <code>conditions</code>, on those filesystems which
	 * allow querying.
	 * 
	 * @see list()
	 */
	public String[] list(final MetaDataCondition[] conditions) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname that satisfy the specified
	 * filter.
	 */
	/*
	 * public String[] list(GeneralFilenameFilter filter) { String names[] =
	 * list(); if ((names == null) || (filter == null)) { return names; }
	 * 
	 * ArrayList v = new ArrayList(); for (int i = 0 ; i < names.length ; i++) {
	 * if (filter.accept(this, names[i])) { v.add(names[i]); } }
	 * 
	 * return (String[])(v.toArray(new String[0])); }
	 */

	/**
	 * Returns an array of abstract pathnames denoting the files in the
	 * directory denoted by this abstract pathname.
	 * 
	 * <p>
	 * An array of <code>GeneralFile</code> objects is returned, one for each
	 * file or directory in the directory. Pathnames denoting the directory
	 * itself is included in the result. Each resulting abstract pathname is
	 * constructed from this abstract pathname using the
	 * <code>GeneralFile(GeneralFile,&nbsp;String)</code> constructor.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will
	 * appear in any specific order; they are not, in particular, guaranteed to
	 * appear in alphabetical order.
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty.
	 */
	public GeneralFile[] listFiles() {
		String[] list = list();
		if (list == null) {
			return null;
		}

		int length = list.length;
		GeneralFile[] files = new GeneralFile[length];

		for (int i = 0; i < length; i++) {
			files[i] = FileFactory.newFile(this, list[i]);
		}

		return files;
	}

	/**
	 * List the available filesystem roots.
	 * <p>
	 * It is guaranteed that the canonical pathname of any file physically
	 * present in the system will begin with one of the roots returned by this
	 * method.
	 * 
	 * @deprecated Doesn't work reliably without pre-existing fileSystem. use
	 *             <code>listRoots( GeneralFileSystem )</code>
	 * 
	 * @return An array of <code>GeneralFile</code> objects denoting the
	 *         available filesystem root.
	 */
	@Deprecated
	public static GeneralFile[] listRoots() {
		throw new UnsupportedOperationException();
	}

	/**
	 * List the available filesystem roots. The SRB has a single root directory,
	 * namely <code>"/"</code>.
	 * <p>
	 * It is guaranteed that the canonical pathname of any file physically
	 * present in the SRB will begin with one of the roots returned by this
	 * method.
	 * 
	 * @return An array of <code>GeneralFile</code> objects denoting the SRB
	 *         filesystem root, namely <code>"/"</code>.
	 */
	public static GeneralFile[] listRoots(final GeneralFileSystem fileSystem) {
		String[] roots = GeneralFileSystem.roots;
		GeneralFile[] list = new GeneralFile[roots.length];
		for (int i = 0; i < list.length; i++) {
			list[i] = FileFactory.newFile(fileSystem, roots[i]);
		}
		return list;
	}

	/**
	 * Creates the directory named by this abstract pathname.
	 */
	public boolean mkdir() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates the directory named by this abstract pathname, including any
	 * necessary but nonexistent parent directories. Note that if this operation
	 * fails it may have succeeded in creating some of the necessary parent
	 * directories.
	 */
	public boolean mkdirs() {
		if (!isDirectory()) {
			int dirNum = directory.size();
			GeneralFile dir = FileFactory.newFile(fileSystem, directory.get(0)
					.toString());

			for (int i = 1; i < dirNum; i++) {
				dir = FileFactory.newFile(dir, directory.get(i).toString());
				if (!dir.exists()) {
					dir.mkdir();
				}
			}
			return mkdir();
		}

		return false;
	}

	/**
	 * Renames the file denoted by this abstract pathname. Will attempt to
	 * overwrite existing files with the same name at the destination.
	 * <P>
	 * Whether or not this method can move a file from one filesystem to another
	 * is platform-dependent. The return value should always be checked to make
	 * sure that the rename operation was successful.
	 * 
	 * After an unsuccessful attempt, some errors may cause a whole/partial copy
	 * of this file/directory to be left at <code>dest</code>.
	 * 
	 * After a successful move, this file object is no longer valid, only the
	 * <code>dest</code> file object should be used.
	 * 
	 * @param dest
	 *            The new abstract pathname for the named file
	 * @throws NullPointerException
	 *             - If dest is null
	 */
	public boolean renameTo(final GeneralFile dest)
			throws IllegalArgumentException, NullPointerException {
		// should I do this? It isn't really rename.
		try {
			copyTo(dest, true);
			// don't delete unless successful
			delete();
			return true;
		} catch (IOException e) {
			log.warn("io exception, logged and ignored", e);
			return false;
		}
	}

	/**
	 * Sets the last-modified time of the file or directory named by this
	 * abstract pathname.
	 * 
	 * @param time
	 *            The new last-modified time, measured in a system-dependent
	 *            way.
	 * @throws IllegalArgumentException
	 *             - If the argument is negative
	 */
	public boolean setLastModified(final long time)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 */
	public boolean setReadOnly() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the pathname string of this abstract pathname.
	 */
	@Override
	public String toString() {
		return getPath();
	}

	/**
	 * Constructs a <tt>file:</tt> URI that represents this abstract pathname.
	 * 
	 * <p>
	 * The exact form of the URI is system-dependent. If it can be determined
	 * that the file denoted by this abstract pathname is a directory, then the
	 * resulting URI will end with a slash.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i>, it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new {@link #GeneralFile(java.net.URI) GeneralFile}
	 * (</tt><i>&nbsp;f</i><tt>.toURI()).equals(</tt><i>&nbsp;f</i><tt>)
	 * </tt> </blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. However, this relationship typically does not hold
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @return An absolute, hierarchical URI with a scheme equal to
	 *         <tt>"file"</tt>, a path representing this abstract pathname, and
	 *         undefined authority, query, and fragment components
	 * 
	 * @see #GeneralFile(java.net.URI)
	 * @see java.net.URI
	 * @see java.net.URI#toURL()
	 */
	public URI toURI() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Converts this abstract pathname into a <code>file:</code> URL. The exact
	 * form of the URL is system-dependent. If it can be determined that the
	 * file denoted by this abstract pathname is a directory, then the resulting
	 * URL will end with a slash.
	 * 
	 * <p>
	 * <b>Usage note:</b> This method does not automatically escape characters
	 * that are illegal in URLs. It is recommended that new code convert an
	 * abstract pathname into a URL by first converting it into a URI, via the
	 * {@link #toURI() toURI} method, and then converting the URI into a URL via
	 * the {@link java.net.URI#toURL() URI.toURL} method.
	 * 
	 * @return A URL object representing the equivalent file URL
	 * 
	 * @throws MalformedURLException
	 *             If the path cannot be parsed as a URL
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @see java.net.URI#toURL()
	 * @see java.net.URL
	 */
	public URL toURL() throws MalformedURLException {
		throw new UnsupportedOperationException();
	}
}
