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
//  LocalFile.java  -  edu.sdsc.grid.io.local.LocalFile
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralFile
//              |
//              +-.LocalFile
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.local;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.RemoteFile;

/**
 * The LocalFile class is a wrapper class for the java.io.File class. Use it as
 * you would a regular File object. The localFile.getFile() method will return a
 * java.io.File object for those situations where a true java.io.File is
 * required.
 * <P>
 * LocalFile includes dummy methods for the local file's metadata.
 * <P>
 * 
 * @see java.io.File
 * @see edu.sdsc.grid.io.GeneralFile
 * @author Lucas Gilbert
 * @since Jargon1.0
 */
public class LocalFile extends GeneralFile {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	/**
	 * The system-dependent default name-separator character. This field is
	 * initialized to contain the first character of the value of the system
	 * property <code>file.separator</code>. On UNIX systems the value of this
	 * field is <code>'/'</code>; on Microsoft Windows systems it is
	 * <code>'\\'</code>.
	 * 
	 * @see java.lang.System#getProperty(java.lang.String)
	 */
	public static final char separatorChar = File.separatorChar;

	/**
	 * The system-dependent default name-separator character, represented as a
	 * string for convenience. This string contains a single character, namely
	 * <code>{@link #separatorChar}</code>.
	 */
	public static final String separator = File.separator;

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
	public static final char pathSeparatorChar = File.pathSeparatorChar;

	/**
	 * The system-dependent path-separator character, represented as a string
	 * for convenience. This string contains a single character, namely
	 * <code>{@link #pathSeparatorChar}</code>.
	 */
	public static final String pathSeparator = File.pathSeparator;

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * Internal file object to provide LocalFile with the functionality of
	 * java.io.File
	 */
	private File wrapper;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Creates a new LocalFile instance by converting the given pathname string
	 * into an abstract pathname. If the given string is the empty string, then
	 * the result is the empty abstract pathname.
	 * 
	 * @throws NullPointerException
	 *             - If the pathname argument is null
	 */
	public LocalFile(final String filePath) {
		super(new LocalFileSystem(), filePath, "");

		wrapper = new File(filePath);
	}

	/**
	 * Creates a new LocalFile instance from a parent pathname string and a
	 * child pathname string.
	 * 
	 * @throws NullPointerException
	 *             - If child is null
	 */
	public LocalFile(final String directory, final String child) {
		// doesn't really do anything.
		super(new LocalFileSystem(), directory, child);

		wrapper = new File(directory, child);
	}

	/**
	 * Creates a new LocalFile instance from a parent abstract pathname and a
	 * child pathname string.
	 * 
	 * @throws NullPointerException
	 *             - If child is null
	 */
	public LocalFile(final LocalFile directory, final String child) {
		this(directory.getAbsolutePath(), child);

		wrapper = new File(directory.getAbsolutePath(), child);
	}

	/**
	 * Creates a new File instance from a file object.
	 * 
	 * @throws NullPointerException
	 *             - If file is null
	 */
	public LocalFile(final File file) {
		this(file.getAbsolutePath(), "");

		wrapper = file;
	}

	/**
	 * Creates a new File instance from a file object.
	 * 
	 * @throws NullPointerException
	 *             - If child is null
	 */
	public LocalFile(final File file, final String child) {
		this(file.getAbsolutePath(), child);

		wrapper = new File(file, child);
	}

	/**
	 * Creates a new LocalFile instance by converting the given file: URI into
	 * an abstract pathname. The exact form of a file: URI is system-dependent,
	 * hence the transformation performed by this constructor is also
	 * system-dependent.
	 * 
	 * @throws NullPointerException
	 *             - If uri is null
	 * @throws IllegalArgumentException
	 *             - If the preconditions on the parameter do not hold
	 */
	public LocalFile(final URI uri) {
		this(uri.getPath(), ""); // Is this right?

		wrapper = new File(uri);
	}

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 * <P>
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		wrapper = null;
	}

	// ----------------------------------------------------------------------
	// GeneralFile Methods
	// ----------------------------------------------------------------------
	/**
	 * Iterates through the directory/collection/container list.
	 * 
	 * @return Iterator
	 */
	public Iterator listIterator() throws IOException {
		// TODO what does this return?
		return null;
	}

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------
	/**
	 * Set the directory.
	 */
	@Override
	protected void setDirectory(final String dir) {
		// do nothing
		// for get use wrapper.getParent() instead
	}

	/**
	 * Set the file name.
	 */
	@Override
	protected void setFileName(final String fileName) {
		// do nothing
		// for get use wrapper.getName() instead
	}

	/**
	 * Returns the internal file object.
	 */
	public File getFile() {
		return wrapper;
	}

	/**
	 * Dummy method for local file metadata.
	 */
	public Properties getMetaData() {
		return null;
	}

	/**
	 * This method gets the path separator as defined by the local system.
	 */
	@Override
	public String getPathSeparator() {
		return PATH_SEPARATOR;
	}

	/**
	 * This method gets the path separator char as defined by the local system.
	 */
	@Override
	public char getPathSeparatorChar() {
		return PATH_SEPARATOR_CHAR;
	}

	/**
	 * Gets the home directory to be used with this LocalFile object.
	 * 
	 * @throws NullPointerException
	 *             if fileSystem is null.
	 * @return System.getProperty( "user.home" );
	 */
	protected String getHomeDirectory() {
		return System.getProperty("user.home");
	}

	// ----------------------------------------------------------------------
	// GeneralFile Methods
	// ----------------------------------------------------------------------
	/**
	 * Copies this file to another file. If the destination file does not exist,
	 * a new one will be created. Otherwise the source file will be appended to
	 * the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	@Override
	public void copyTo(final GeneralFile file) throws IOException {
		if (file instanceof RemoteFile) {
			// This allows optimization by the use of the parallel up/download
			file.copyFrom(this, false);
		} else {
			super.copyTo(file, false);
		}
	}

	/**
	 * Copies this file to another file. If the destination file does not exist,
	 * a new one will be created. Otherwise the source file will be appended to
	 * the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	@Override
	public void copyTo(final GeneralFile file, final boolean forceOverwrite)
			throws IOException {
		if (file instanceof RemoteFile) {
			// This allows optimization by the use of the parallel up/download
			file.copyFrom(this, forceOverwrite);
		} else {
			super.copyTo(file, forceOverwrite);
		}
	}

	/**
	 * Copies this file to another file. If the destination file does not exist,
	 * a new one will be created. Otherwise the source file will be appended to
	 * the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	@Override
	public void copyFrom(final GeneralFile file) throws IOException {
		if (file instanceof RemoteFile) {
			// This allows optimization by the use of the parallel up/download
			file.copyTo(this, false);
		} else {
			super.copyFrom(file, false);
		}
	}

	/**
	 * Copies this file to another file. If the destination file does not exist,
	 * a new one will be created. Otherwise the source file will be appended to
	 * the destination file. Directories will be copied recursively.
	 * 
	 * @param file
	 *            The file to receive the data.
	 * @throws NullPointerException
	 *             If file is null.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	@Override
	public void copyFrom(final GeneralFile file, final boolean forceOverwrite)
			throws IOException {
		/*
		 * I didn't really want to have a .copyFrom(...), but without the
		 * copyFrom, new packages which don't have access to the LocalFile
		 * source can't be optimized for upload. So for example, they wouldn't
		 * have a way to copy a local file to the SRB using the parallel method.
		 * Of course localFile.copyTo( newGridFile ) will still be slow, but
		 * newGridFile.copyFrom( localFile ) can be a work-around.
		 */
		if (file instanceof RemoteFile) {
			// This allows optimization by the use of the parallel up/download
			file.copyTo(this, forceOverwrite);
		} else {
			super.copyFrom(file, forceOverwrite);
		}
	}

	// ----------------------------------------------------------------------
	// java.io.File Methods
	// ----------------------------------------------------------------------
	/**
	 * Tests whether the application can read the file denoted by this abstract
	 * pathname.
	 */
	@Override
	public boolean canRead() {
		return wrapper.canRead();
	}

	/**
	 * Tests whether the application can modify to the file denoted by this
	 * abstract pathname.
	 */
	@Override
	public boolean canWrite() {
		return wrapper.canWrite();
	}

	/**
	 * Compares two abstract pathnames lexicographically.
	 */
	@Override
	public int compareTo(final GeneralFile pathName) {
		// throw ClassCastException if not a LocalFile
		return wrapper.compareTo(((LocalFile) pathName).getFile());
	}

	/**
	 * Compares this abstract pathname to another object.
	 */
	@Override
	public int compareTo(final Object o) {
		return wrapper.compareTo(((LocalFile) o).getFile());
	}

	/**
	 * Atomically creates a new, empty file named by this abstract pathname if
	 * and only if a file with this name does not yet exist.
	 */
	@Override
	public boolean createNewFile() throws IOException {
		// windows doesn't like this call on existing files?
		if (wrapper.exists()) {
			return false;
		} else {
			return wrapper.createNewFile();
		}
	}

	/**
	 * Creates an empty file in the default temporary-file directory, using the
	 * given prefix and suffix to generate its name.
	 */
	public static GeneralFile createTempFile(final String prefix,
			final String suffix) throws IOException {
		return new LocalFile(File.createTempFile(prefix, suffix));
	}

	/**
	 * Creates a new empty file in the specified directory, using the given
	 * prefix and suffix strings to generate its name.
	 */
	public static GeneralFile createTempFile(final String prefix,
			final String suffix, final GeneralFile directory)
			throws IOException {
		return new LocalFile(File.createTempFile(prefix, suffix, new File(
				directory.getAbsolutePath())));
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname.
	 */
	@Override
	public boolean delete() {
		return wrapper.delete();
	}

	/**
	 * Requests that the file or directory denoted by this abstract pathname be
	 * deleted when the virtual machine terminates.
	 */
	@Override
	public void deleteOnExit() {
		wrapper.deleteOnExit();
	}

	/**
	 * Tests this abstract pathname for equality with the given object.
	 */
	@Override
	public boolean equals(final Object obj) {
		return wrapper.equals(obj);
	}

	/**
	 * Tests whether the file denoted by this abstract pathname exists.
	 */
	@Override
	public boolean exists() {
		return wrapper.exists();
	}

	/**
	 * Returns the absolute form of this abstract pathname.
	 */
	@Override
	public GeneralFile getAbsoluteFile() {
		return new LocalFile(wrapper.getAbsoluteFile());
	}

	/**
	 * Returns the absolute pathname string of this abstract pathname.
	 */
	@Override
	public String getAbsolutePath() {
		return getParent() + PATH_SEPARATOR + getName();
	}

	/**
	 * Returns the canonical form of this abstract pathname.
	 */
	@Override
	public GeneralFile getCanonicalFile() throws IOException {
		return new LocalFile(wrapper.getCanonicalFile());
	}

	/**
	 * Returns the canonical pathname string of this abstract pathname.
	 */
	@Override
	public String getCanonicalPath() throws IOException {
		return wrapper.getCanonicalPath();
	}

	/**
	 * Returns the name of the file or directory denoted by this abstract
	 * pathname.
	 */
	@Override
	public String getName() {
		return wrapper.getName();
	}

	/**
	 * Returns the pathname string of this abstract pathname's parent, or null
	 * if this pathname does not name a parent directory.
	 */
	@Override
	public String getParent() {
		// java doesn't handle relative paths well
		String path = null;
		int index = -1;

		try {
			path = wrapper.getCanonicalPath();
		} catch (Throwable e) {
			// e.printStackTrace();
		}

		if (path == null) {
			path = wrapper.getPath();
		}

		index = path.lastIndexOf(separatorChar);
		if (index == path.length() - 1) {
			// was just a superfluous separator at the end
			index = path.lastIndexOf(separatorChar, index);
		}
		path = path.substring(0, index);

		return path;
	}

	/**
	 * Returns the abstract pathname of this abstract pathname's parent, or null
	 * if this pathname does not name a parent directory.
	 */
	@Override
	public GeneralFile getParentFile() {
		return new LocalFile(getParent());
	}

	/**
	 * Converts this abstract pathname into a pathname string.
	 */
	@Override
	public String getPath() {
		return wrapper.getPath();
	}

	/**
	 * Computes a hash code for this abstract pathname.
	 */
	@Override
	public int hashCode() {
		return wrapper.hashCode();
	}

	/**
	 * Tests whether this abstract pathname is absolute.
	 */
	@Override
	public boolean isAbsolute() {
		return wrapper.isAbsolute();
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 */
	@Override
	public boolean isDirectory() {
		return wrapper.isDirectory();
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a normal
	 * file.
	 */
	@Override
	public boolean isFile() {
		return wrapper.isFile();
	}

	/**
	 * Tests whether the file named by this abstract pathname is a hidden file.
	 */
	@Override
	public boolean isHidden() {
		return wrapper.isHidden();
	}

	/**
	 * Returns the time that the file denoted by this abstract pathname was last
	 * modified.
	 */
	@Override
	public long lastModified() {
		return wrapper.lastModified();
	}

	/**
	 * Returns the length of the file denoted by this abstract pathname.
	 */
	@Override
	public long length() {
		if (exists()) {
			return wrapper.length();
		} else {
			return 0;
		}
	}

	/**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname.
	 */
	@Override
	public String[] list() {
		return wrapper.list();
	}

	/**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname that satisfy the specified
	 * filter.
	 */
	public String[] list(final FilenameFilter filter) {
		return wrapper.list(filter);
	}

	/**
	 * Returns an array of abstract pathnames denoting the files in the
	 * directory denoted by this abstract pathname.
	 */
	@Override
	public GeneralFile[] listFiles() {
		File fileList[] = wrapper.listFiles();
		if (fileList == null) {
			return null;
		}

		LocalFile localFileList[] = new LocalFile[fileList.length];

		for (int i = 0; i < fileList.length; i++) {
			localFileList[i] = new LocalFile(fileList[i]);
		}

		return localFileList;
	}

	/**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter.
	 */
	public GeneralFile[] listFiles(final FileFilter filter) {
		File fileList[] = wrapper.listFiles(filter);
		LocalFile localFileList[] = new LocalFile[fileList.length];

		for (int i = 0; i < fileList.length; i++) {
			localFileList[i] = new LocalFile(fileList[i]);
		}

		return localFileList;
	}

	/**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter.
	 */
	public GeneralFile[] listFiles(final FilenameFilter filter) {
		File fileList[] = wrapper.listFiles(filter);
		LocalFile localFileList[] = new LocalFile[fileList.length];

		for (int i = 0; i < fileList.length; i++) {
			localFileList[i] = new LocalFile(fileList[i]);
		}

		return localFileList;
	}

	/**
	 * List the available filesystem roots.
	 */
	public static GeneralFile[] listRoots() {
		File fileRoots[] = File.listRoots();
		LocalFile localFileRoots[] = new LocalFile[fileRoots.length];
		;

		for (int i = 0; i < fileRoots.length; i++) {
			localFileRoots[i] = new LocalFile(fileRoots[i]);
		}

		return localFileRoots;
	}

	/**
	 * Creates the directory named by this abstract pathname.
	 */
	@Override
	public boolean mkdir() {
		return wrapper.mkdir();
	}

	/**
	 * Creates the directory named by this abstract pathname, including any
	 * necessary but nonexistent parent directories.
	 */
	@Override
	public boolean mkdirs() {
		return wrapper.mkdirs();
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
	 * 
	 * @throws NullPointerException
	 *             - If dest is null
	 */
	@Override
	public boolean renameTo(final GeneralFile dest) {
		if (dest instanceof LocalFile) {
			return wrapper.renameTo(new File(dest.getAbsolutePath()));
		} else {
			return super.renameTo(dest);
		}
	}

	/**
	 * Sets the last-modified time of the file or directory named by this
	 * abstract pathname.
	 */
	@Override
	public boolean setLastModified(final long time) {
		return wrapper.setLastModified(time);
	}

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed.
	 */
	@Override
	public boolean setReadOnly() {
		return wrapper.setReadOnly();
	}

	/**
	 * Returns the pathname string of this abstract pathname.
	 */
	@Override
	public String toString() {
		return wrapper.toURI().toString();
	}

	/**
	 * Constructs a file: URI that represents this abstract pathname.
	 */
	@Override
	public URI toURI() {
		return wrapper.toURI();
	}

	/**
	 * Converts this abstract pathname into a file: URL.
	 */
	@Override
	public URL toURL() throws MalformedURLException {
		return wrapper.toURL();
	}
}
