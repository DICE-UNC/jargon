/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a file or collection on the IRODS data grid. Note that
 * <code>IRODSFileImpl</code> is a variant of an
 * {@link org.irogs.jargon.core.pub.IRODSAccessObject IRODSAccessObject}, and
 * internally holds a connection to IRODS.
 * <p/>
 * This object is not thread-safe, and cannot be shared between threads. This
 * File object has a connection associated with the thread which created it.
 * There are methods in {@link org.irods.jargon.core.pub.io.IRODSFileFactory
 * IRODSFileFactory} that allow an <code>IRODSFileImpl</code> to be attached to
 * another Thread and connection.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public final class IRODSFileImpl extends File implements IRODSFile {
	static Logger log = LoggerFactory.getLogger(IRODSFileImpl.class);

	private IRODSFileSystemAO irodsFileSystemAO = null;
	/**
	 * ObjStat is a cached object (synchronized access) that is obtained from
	 * iRODS to describe the file
	 */
	private transient ObjStat objStat = null;

	private String fileName = "";
	private String resource = "";
	private int fileDescriptor = -1;
	private List<String> directory = new ArrayList<String>();

	private static final long serialVersionUID = -6986662136294659059L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#reset()
	 */
	@Override
	public synchronized void reset() {
		this.objStat = null;
	}

	protected IRODSFileImpl(final String pathName,
			final IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		this("", pathName, irodsFileSystemAO);
		if (pathName == null || pathName.isEmpty()) {
			throw new JargonException("path name is null or empty");
		}

		MiscIRODSUtils.checkPathSizeForMax(pathName);
	}

	protected IRODSFileImpl(final String parent, final String child,
			final IRODSFileSystemAO irodsFileSystemAO) throws JargonException {

		super(parent, child);

		if (irodsFileSystemAO == null) {
			throw new IllegalArgumentException("irodsFileSystemAO is null");
		}

		if (parent == null) {
			throw new IllegalArgumentException("null or missing parent name");
		}

		if (child == null) {
			throw new IllegalArgumentException("null child name");
		}

		if (parent.isEmpty() && child.isEmpty()) {
			throw new IllegalArgumentException(
					"both parent and child names are empty");
		}

		MiscIRODSUtils.checkPathSizeForMax(parent, child);

		this.irodsFileSystemAO = irodsFileSystemAO;
		String trimmedParent = parent.trim();
		setDirectory(trimmedParent);
		setFileName(child.trim());
		makePathCanonical(trimmedParent);
	}

	protected IRODSFileImpl(final File parent, final String child,
			final IRODSFileSystemAO irodsFileSystemAO) throws JargonException {

		this(parent.getAbsolutePath().trim(), child.trim(), irodsFileSystemAO);
	}

	/**
	 * @param dir
	 *            Used to determine if the path is absolute.
	 */

	private void makePathCanonical(String dir) {
		int i = 0; // where to insert into the Vector
		boolean absolutePath = false;
		String canonicalTest = null;

		if (dir == null) {
			dir = "";
		}

		// In case this abstract path is supposed to be root
		if ((fileName.equals(IRODS_ROOT) || fileName.equals(""))
				&& dir.equals("")) {
			return;
		}

		// In case this abstract path is supposed to be the home directory
		if (fileName.equals("") && dir.equals("")) {
			String home = irodsFileSystemAO.getIRODSAccount()
					.getHomeDirectory();
			int index = home.lastIndexOf(PATH_SEPARATOR);
			setDirectory(home.substring(0, index));
			setFileName(home.substring(index + 1));
			return;
		}

		// if dir not absolute
		if (dir.startsWith(IRODS_ROOT)) {
			absolutePath = true;
		}

		// if directory not already absolute
		if (directory.size() > 0) {
			if (directory.get(0).toString().length() == 0) {
				// The /'s were all striped when the vector was created
				// so if the first element of the vector is null
				// but the vector isn't null, then the first element
				// is really a /.
				absolutePath = true;
			}
		}
		if (!absolutePath) {
			String home = irodsFileSystemAO.getIRODSAccount()
					.getHomeDirectory();
			int index = home.indexOf(PATH_SEPARATOR);
			// allow the first index to = 0,
			// because otherwise separator won't get added in front.
			if (index >= 0) {
				do {
					directory.add(i, home.substring(0, index));
					home = home.substring(index + 1);
					index = home.indexOf(PATH_SEPARATOR);
					i++;
				} while (index > 0);
			}
			if ((!home.equals("")) && (home != null)) {
				directory.add(i, home);
			}
		}

		// first, made absolute, then canonical
		for (i = 0; i < directory.size(); i++) {
			canonicalTest = directory.get(i).toString();
			if (canonicalTest.equals(".")) {
				directory.remove(i);
				i--;
			} else if ((canonicalTest.equals("..")) && (i >= 2)) {
				directory.remove(i);
				directory.remove(i - 1);
				i--;
				if (i > 0) {
					i--;
				}
			} else if (canonicalTest.equals("..")) {
				// at root, just remove the ..
				directory.remove(i);
				i--;
			} else if (canonicalTest.startsWith(separator)) {
				// if somebody put filepath as /foo//bar or /foo////bar
				do {
					canonicalTest = canonicalTest.substring(1);
				} while (canonicalTest.startsWith(PATH_SEPARATOR));
				directory.remove(i);
				directory.add(i, canonicalTest);
			}
		}
		// also must check fileName
		if (fileName.equals(".")) {
			fileName = directory.get(directory.size() - 1).toString();
			directory.remove(directory.size() - 1);
		} else if (fileName.equals("..")) {
			if (directory.size() > 1) {
				fileName = directory.get(directory.size() - 2).toString();
				directory.remove(directory.size() - 1);
				directory.remove(directory.size() - 1);
			} else {
				// at root
				fileName = PATH_SEPARATOR;
				directory.remove(directory.size() - 1);
			}
		}
	}

	/**
	 * Set the directory.
	 * 
	 * @param dir
	 *            The directory path, need not be absolute.
	 */
	private void setDirectory(String dir) {
		if (directory == null) {
			directory = new ArrayList<String>();
		}

		// in case they used the local pathSeparator
		// in the fileName instead of the iRODS PATH_SEPARATOR.
		String localSeparator = System.getProperty("file.separator");
		int index = dir.lastIndexOf(localSeparator);
		if ((index >= 0) && ((dir.substring(index + 1).length()) > 0)) {
			dir = dir.substring(0, index) + PATH_SEPARATOR_CHAR
					+ dir.substring(index + 1);
			index = dir.lastIndexOf(localSeparator);
		}

		while ((directory.size() > 0) && dir.startsWith(PATH_SEPARATOR)) {
			dir = dir.substring(1);
			// problems if dir passed from filename starts with PATH_SEPARATOR
		}

		// create directory
		index = dir.indexOf(PATH_SEPARATOR_CHAR);

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
	 * Set the file name.
	 * 
	 * @param fleName
	 *            The file name or fileName plus some or all of the directory
	 *            path.
	 */
	private void setFileName(String filePath) {

		// used when parsing the filepath
		int index;

		// in case they used the local pathSeparator
		// in the fileName instead of the iRODS PATH_SEPARATOR.
		String localSeparator = System.getProperty("file.separator");

		if (filePath == null) {
			throw new NullPointerException("The file name cannot be null");
		}

		log.info("setting file name, given path = {}", filePath);
		log.info("detected local separator = {}", localSeparator);

		// replace local separators with iRODS separators.
		if (!localSeparator.equals(PATH_SEPARATOR)) {
			index = filePath.lastIndexOf(localSeparator);
			while ((index >= 0)
					&& ((filePath.substring(index + 1).length()) > 0)) {
				filePath = filePath.substring(0, index) + PATH_SEPARATOR_CHAR
						+ filePath.substring(index + 1);
				index = filePath.lastIndexOf(localSeparator);
			}
		}
		fileName = filePath;

		if (fileName.length() > 1) { // add to allow path = root "/"
			index = fileName.lastIndexOf(PATH_SEPARATOR_CHAR);
			while ((index == fileName.length() - 1) && (index >= 0)) {
				// remove '/' at end of filename, if exists
				fileName = fileName.substring(0, index);
				index = fileName.lastIndexOf(PATH_SEPARATOR_CHAR);
			}

			// separate directory and file
			if ((index >= 0) && ((fileName.substring(index + 1).length()) > 0)) {
				// have to run setDirectory(...) again
				// because they put filepath info in the filename
				setDirectory(fileName.substring(0, index + 1));
				fileName = fileName.substring(index + 1);
			}
		}

		log.info("file name was set as: {}", fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#canRead()
	 */
	@Override
	public synchronized boolean canRead() {
		boolean canRead = false;

		try {
			initializeObjStatForFile();
			canRead = irodsFileSystemAO.isFileReadable(this);
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}

		return canRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#canWrite()
	 */
	@Override
	public synchronized boolean canWrite() {
		boolean canWrite = false;

		try {
			initializeObjStatForFile();
			canWrite = irodsFileSystemAO.isFileWriteable(this);
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}

		return canWrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#createNewFile()
	 */
	@Override
	public synchronized boolean createNewFile() throws IOException {
		try {
			fileDescriptor = irodsFileSystemAO.createFile(
					this.getAbsolutePath(), DataObjInp.OpenFlags.READ_WRITE,
					DataObjInp.DEFAULT_CREATE_MODE);

			log.debug("file descriptor from new file create: {}",
					fileDescriptor);
			// TODO: clean up after tests
			// in irods the file must be closed, then opened when doing a create
			// new
			// this.close();
			// this.openKnowingExists();
			// log.debug("file now closed");
		} catch (JargonFileOrCollAlreadyExistsException e) {
			return false;

		} catch (JargonException e) {
			String msg = "JargonException caught and rethrown as IOException:"
					+ e.getMessage();
			log.error(msg, e);
			throw new IOException(e);
		}
		return true;
	}

	@Override
	public synchronized boolean createNewFileCheckNoResourceFound()
			throws NoResourceDefinedException, JargonException {
		try {
			fileDescriptor = irodsFileSystemAO.createFile(
					this.getAbsolutePath(), DataObjInp.OpenFlags.READ_WRITE,
					DataObjInp.DEFAULT_CREATE_MODE);

			log.debug("file descriptor from new file create: {}",
					fileDescriptor);
			//TODO: clean up after tests
			// in irods the file must be closed, then opened when doing a create
			// new
			// this.close();
			// this.openKnowingExists();
			// log.debug("file now closed");
		} catch (JargonFileOrCollAlreadyExistsException e) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#delete()
	 */
	@Override
	public synchronized boolean delete() {
		boolean successful = true;
		if (!exists()) {
			successful = true;
		} else {
			try {
				if (this.isFile()) {
					irodsFileSystemAO.fileDeleteNoForce(this);
				} else if (this.isDirectory()) {
					irodsFileSystemAO.directoryDeleteNoForce(this);
				}
			} catch (FileNotFoundException dnf) {
				log.info("file not found, treat as unsuccessful");
				successful = false;
			} catch (JargonException e) {

				if (e.getUnderlyingIRODSExceptionCode() == -528002) {
					log.warn("underlying rename error logged and ignored on delete");
				} else {

					log.error(
							"irods error occurred on delete, this was not a data not found exception, rethrow as unchecked",
							e);
					throw new JargonRuntimeException(
							"exception occurred on delete", e);
				}

			}
		}
		objStat = null;
		fileDescriptor = -1;
		return successful;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#deleteWithForceOption()
	 */
	@Override
	public synchronized boolean deleteWithForceOption() {
		boolean successful = true;
		try {
			if (this.isFile()) {
				irodsFileSystemAO.fileDeleteForce(this);
			} else if (this.isDirectory()) {
				irodsFileSystemAO.directoryDeleteForce(this);
			}
		} catch (FileNotFoundException fnf) {
			log.info("file not found, treat as unsuccessful");
			successful = false;
		} catch (JargonException e) {
			String msg = "JargonException caught and logged on delete:"
					+ e.getMessage();
			log.error(msg, e);
			throw new JargonRuntimeException(msg, e);
		}
		objStat = null;
		fileDescriptor = -1;
		return successful;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#deleteOnExit()
	 */
	@Override
	public void deleteOnExit() {
		throw new JargonRuntimeException(
				"delete on exit is not supported for IRODS Files, please explicitly delete the file");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {

		if (obj instanceof File) {
			File temp = (File) obj;
			return temp.getAbsolutePath().equals(this.getAbsolutePath());
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#exists()
	 */
	@Override
	public synchronized boolean exists() {

		boolean isExists = false;

		try {
			isExists = irodsFileSystemAO.isFileExists(this);
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
		return isExists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getAbsoluteFile()
	 */
	@Override
	public File getAbsoluteFile() {

		try {
			return new IRODSFileImpl(getAbsolutePath(), this.irodsFileSystemAO);
		} catch (JargonException e) {
			String msg = "JargonException caught and rethrown as JargonRuntimeException:"
					+ e.getMessage();
			log.error(msg, e);
			throw new JargonRuntimeException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getAbsolutePath()
	 */
	@Override
	public String getAbsolutePath() {
		StringBuilder pathBuilder = new StringBuilder();
		String builtPath = "";
		if ((directory != null) && (!directory.isEmpty())) {
			boolean firstPath = true;

			for (String element : directory) {
				if (!firstPath) {
					pathBuilder.append(PATH_SEPARATOR);
				}
				pathBuilder.append(element);
				firstPath = false;
			}

			pathBuilder.append(PATH_SEPARATOR);
			pathBuilder.append(getName());
			builtPath = pathBuilder.toString();
		} else {
			String name = getName();
			if (name == null || name.equals("")) {
				// just in case the dir and name are empty, return root.
				builtPath = IRODS_ROOT;
			} else {
				if (name.equals("/")) {
					builtPath = name;
				}
			}
		}
		return builtPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getCanonicalFile()
	 */
	@Override
	public File getCanonicalFile() throws IOException {
		String canonicalPath = getCanonicalPath();
		try {
			return new IRODSFileImpl(canonicalPath, this.irodsFileSystemAO);
		} catch (JargonException e) {
			String msg = "jargon exception in file method, rethrown as IOException to match method signature"
					+ e.getMessage();
			log.error(msg, e);
			throw new IOException(msg, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getCanonicalPath()
	 */
	@Override
	public String getCanonicalPath() throws IOException {
		if ((directory != null) && (!directory.isEmpty())) {
			int size = directory.size();
			int i = 1;
			StringBuilder path = new StringBuilder();
			path.append(directory.get(0));

			while (i < size) {
				path.append('/');
				path.append(directory.get(i));
				i++;
			}

			path.append('/');
			path.append(fileName);
			return path.toString();
		}

		return fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getFreeSpace()
	 */
	@Override
	public long getFreeSpace() {
		// TODO: implement via quotas
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getParentFile()
	 */
	@Override
	public File getParentFile() {
		String parentPath = getParent();

		if (parentPath == null) {
			return null;
		}

		try {
			return new IRODSFileImpl(parentPath, this.irodsFileSystemAO);
		} catch (JargonException e) {
			String msg = "jargon exception in file method, rethrown as JargonRuntimeException to match method signature"
					+ e.getMessage();
			log.error(msg, e);
			throw new JargonRuntimeException(msg, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getTotalSpace()
	 */
	@Override
	public long getTotalSpace() {
		// TODO: implement via quotas
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getUsableSpace()
	 */
	@Override
	public long getUsableSpace() {
		// TODO: implement via quotas
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#hashCode()
	 */
	@Override
	public int hashCode() {
		return getAbsolutePath().toLowerCase().hashCode() ^ 1234321;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#isAbsolute()
	 */
	@Override
	public boolean isAbsolute() {
		// all path names are made absolute at construction.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#isDirectory()
	 */
	@Override
	public synchronized boolean isDirectory() {
		log.info("isDirectory() for path:{}", this.getAbsolutePath());
		boolean isDir = false;
		try {
			if (objStat == null) {
				log.info("looking up objStat, not cached in file");
				objStat = irodsFileSystemAO.getObjStat(this.getAbsolutePath());
			}

			if (getObjStat().getObjectType() == ObjectType.COLLECTION
					|| getObjStat().getObjectType() == ObjectType.LOCAL_DIR) {
				isDir = true;
			}
		} catch (FileNotFoundException fnf) {
			log.info("file not found");
		} catch (JargonException je) {
			log.error("jargon exception, rethrow as unchecked", je);
			throw new JargonRuntimeException(je);
		}

		return isDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#isFile()
	 */
	@Override
	public synchronized boolean isFile() {

		log.info("isFile() for path:{}", this.getAbsolutePath());
		boolean isFile = false;

		try {
			if (objStat == null) {

				log.info("looking up objStat, not cached in file");
				objStat = irodsFileSystemAO.getObjStat(this.getAbsolutePath());
			}

			if (getObjStat().getObjectType() == ObjectType.DATA_OBJECT
					|| getObjStat().getObjectType() == ObjectType.LOCAL_FILE) {
				isFile = true;
			}
		} catch (FileNotFoundException fnf) {
			log.info("file not found");
		} catch (JargonException je) {
			log.error("jargon exception, rethrow as unchecked", je);
			throw new JargonRuntimeException(je);
		}

		return isFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getPath()
	 */
	@Override
	public String getPath() {
		return this.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#isHidden()
	 */
	@Override
	public boolean isHidden() {
		return super.isHidden();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#lastModified()
	 */
	@Override
	public synchronized long lastModified() {
		log.info("lastModified() for path:{}", this.getAbsolutePath());
		long lastMod = 0L;

		try {
			initializeObjStatForFile();
			lastMod = objStat.getModifiedAt().getTime();
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return 0L", e);
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
		return lastMod;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#length()
	 */
	@Override
	public synchronized long length() {

		log.info("length() for path:{}", this.getAbsolutePath());

		long length = 0L;

		try {
			initializeObjStatForFile();
			length = objStat.getObjSize();
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}

		return length;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#list()
	 */
	@Override
	public synchronized String[] list() {
		try {
			List<String> result = irodsFileSystemAO.getListInDir(this);
			String[] a = new String[result.size()];
			return result.toArray(a);
		} catch (DataNotFoundException e) {
			return new String[] {};
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#list(java.io.FilenameFilter)
	 */
	@Override
	public synchronized String[] list(final FilenameFilter filter) {
		return super.list(filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#listFiles()
	 */
	@Override
	public synchronized File[] listFiles() {

		try {
			List<String> result = irodsFileSystemAO.getListInDir(this);
			IRODSFileImpl[] a = new IRODSFileImpl[result.size()];
			IRODSFileImpl irodsFile;
			int i = 0;
			for (String fileName : result) {
				// result has just the subdir under this file, need to create
				// the absolute path to create a file
				irodsFile = new IRODSFileImpl(this.getAbsolutePath(), fileName,
						this.irodsFileSystemAO);
				a[i++] = irodsFile;

			}
			return a;
		} catch (DataNotFoundException e) {
			return new IRODSFileImpl[] {};
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#listFiles(java.io.FileFilter)
	 */
	@Override
	public synchronized File[] listFiles(final FileFilter filter) {
		try {
			List<File> result = irodsFileSystemAO.getListInDirWithFileFilter(
					this, filter);
			File[] resArray = new File[result.size()];
			return result.toArray(resArray);
		} catch (DataNotFoundException e) {
			return new IRODSFileImpl[] {};
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			e.printStackTrace();
			throw new JargonRuntimeException(e);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFile#listFiles(java.io.FilenameFilter)
	 */
	@Override
	public synchronized File[] listFiles(final FilenameFilter filter) {
		try {
			List<String> result = irodsFileSystemAO.getListInDirWithFilter(
					this, new IRODSAcceptAllFileNameFilter());
			IRODSFileImpl[] a = new IRODSFileImpl[result.size()];
			IRODSFileImpl irodsFile;
			int i = 0;
			for (String fileName : result) {
				irodsFile = new IRODSFileImpl(fileName, this.irodsFileSystemAO);
				a[i++] = irodsFile;

			}
			return a;
		} catch (DataNotFoundException e) {
			return new IRODSFileImpl[] {};
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#mkdir()
	 */
	@Override
	public boolean mkdir() {

		try {
			irodsFileSystemAO.mkdir(this, false);
		} catch (DuplicateDataException e) {
			log.info("duplicate data exception, return false from mkdir", e);
			return false;
		} catch (CatNoAccessException e) {
			log.error("no access to create the given collection, false will be returned from method");
			return false;
		} catch (JargonException e) {
			// check if this means that it already exists, and call that a
			// 'false' instead of an error
			if (e.getMessage().indexOf("-809000") > -1) {
				log.warn("directory already exists");
				return false;
			}
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#mkdirs()
	 */
	@Override
	public boolean mkdirs() {
		try {
			irodsFileSystemAO.mkdir(this, true);
		} catch (CatNoAccessException e) {
			log.error("no access to create the given collection, false will be returned from method");
			return false;
		} catch (DuplicateDataException e) {
			log.info("duplicate data exception, return false from mkdir", e);
			return false;
		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#renameTo(java.io.File)
	 */
	@Override
	public synchronized boolean renameTo(final IRODSFile dest) {
		boolean success = false;
		if (dest == null) {
			String msg = "dest file is null";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}

		if (!(dest instanceof IRODSFileImpl)) {
			String msg = "provided dest file is not an instance of IRODSFileImpl, cannot rename";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}

		IRODSFile destIRODSFile = dest;

		if (log.isInfoEnabled()) {
			log.info("renaming:" + this.getAbsolutePath() + " to:"
					+ destIRODSFile.getAbsolutePath());
		}

		// if the path is different
		if (!getAbsolutePath().equals(dest.getAbsolutePath())) {
			renameFileOrDirectory(destIRODSFile);
			success = true;
		} else {
			// paths are the same, move to the new resource described by the
			// dest file
			log.info("doing a physical move");
			try {
				this.irodsFileSystemAO.physicalMove(this,
						destIRODSFile.getResource());
				success = true;
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		}
		objStat = null;
		return success;
	}

	/**
	 * @param destIRODSFile
	 * @throws JargonRuntimeException
	 */
	void renameFileOrDirectory(final IRODSFile destIRODSFile)
			throws JargonRuntimeException {
		if (isDirectory()) {
			log.info("paths different, and a directory is being renamed");
			try {
				this.irodsFileSystemAO.renameDirectory(this, destIRODSFile);
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		} else if (isFile()) {
			log.info("paths different, and a file is being renamed");
			try {
				this.irodsFileSystemAO.renameFile(this, destIRODSFile);
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setExecutable(boolean,
	 * boolean)
	 */
	@Override
	public boolean setExecutable(final boolean executable,
			final boolean ownerOnly) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setExecutable(boolean)
	 */
	@Override
	public boolean setExecutable(final boolean executable) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setLastModified(long)
	 */
	@Override
	public boolean setLastModified(final long time) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setReadable(boolean, boolean)
	 */
	@Override
	public boolean setReadable(final boolean readable, final boolean ownerOnly) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setReadable(boolean)
	 */
	@Override
	public boolean setReadable(final boolean readable) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setReadOnly()
	 */
	@Override
	public boolean setReadOnly() {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setWritable(boolean, boolean)
	 */
	@Override
	public boolean setWritable(final boolean writable, final boolean ownerOnly) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setWritable(boolean)
	 */
	@Override
	public boolean setWritable(final boolean writable) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#toString()
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("irods://");
		s.append(this.irodsFileSystemAO.getIRODSAccount().getUserName());
		s.append('@');
		s.append(this.irodsFileSystemAO.getIRODSAccount().getHost());
		s.append(':');
		s.append(this.irodsFileSystemAO.getIRODSAccount().getPort());
		s.append(getAbsolutePath());
		return s.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#toURI()
	 */
	@Override
	public URI toURI() {
		URI uri = null;

		try {
			if (isDirectory()) {
				uri = new URI("irods", this.irodsFileSystemAO.getIRODSAccount()
						.getUserName(), this.irodsFileSystemAO
						.getIRODSAccount().getHost(), this.irodsFileSystemAO
						.getIRODSAccount().getPort(), getAbsolutePath(), null,
						null);
			} else {
				uri = new URI("irods", this.irodsFileSystemAO.getIRODSAccount()
						.getUserName(), this.irodsFileSystemAO
						.getIRODSAccount().getHost(), this.irodsFileSystemAO
						.getIRODSAccount().getPort(), getAbsolutePath(), null,
						null);
			}
		} catch (URISyntaxException e) {
			log.error("URISyntaxException, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}

		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getResource()
	 */
	@Override
	public String getResource() throws JargonException {
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setResource(java.lang.String)
	 */
	@Override
	public void setResource(final String resource) {
		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#getFileDescriptor()
	 */
	@Override
	public synchronized int getFileDescriptor() {
		return fileDescriptor;
	}

	/**
	 * Set the iRODS file descriptor value. This will be set internally by
	 * Jargon.
	 * 
	 * @param fileDescriptor
	 */
	protected synchronized void setFileDescriptor(final int fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	private int openWithMode(final DataObjInp.OpenFlags openFlags,
			final boolean checkExists) throws JargonException {

		if (log.isInfoEnabled()) {
			log.info("opening irodsFile:" + this.getAbsolutePath());
		}

		if (checkExists && !this.exists()) {
			throw new JargonException(
					"this file does not exist, so it cannot be opened.  The file should be created first!");
		}

		if (getFileDescriptor() > 0) {
			log.info("file is already open, use the given descriptor");
			return fileDescriptor;
		}

		int fileDescriptor = this.irodsFileSystemAO.openFile(this, openFlags);

		if (log.isDebugEnabled()) {
			log.debug("opened file with descriptor of:" + fileDescriptor);
		}

		this.fileDescriptor = fileDescriptor;
		return fileDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#openReadOnly()
	 */
	@Override
	public synchronized int openReadOnly() throws JargonException {
		return openWithMode(DataObjInp.OpenFlags.READ, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#open()
	 */
	@Override
	public synchronized int open() throws JargonException {
		return openWithMode(DataObjInp.OpenFlags.READ_WRITE, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#close()
	 */
	@Override
	public synchronized void close() throws JargonException {
		if (log.isInfoEnabled()) {
			log.info("closing irodsFile:{}", this.getAbsolutePath());
		}

		this.reset();

		if (this.getFileDescriptor() <= 0) {
			log.info("file is not open, silently ignore");
			this.setFileDescriptor(-1);
			return;
		}

		this.irodsFileSystemAO.fileClose(this.getFileDescriptor());
		this.setFileDescriptor(-1);
		this.objStat = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#closeGivenDescriptor(int)
	 */
	@Override
	public synchronized void closeGivenDescriptor(final int fd)
			throws JargonException {
		if (log.isInfoEnabled()) {
			log.info("closing irodsFile given descriptor:" + fd);
		}

		if (fd <= 0) {
			log.info("file is not open, silently ignore");
			this.setFileDescriptor(-1);
			return;
		}

		this.irodsFileSystemAO.fileClose(fd);
		this.setFileDescriptor(-1);
		this.objStat = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#compareTo(java.io.File)
	 */
	@Override
	public int compareTo(final IRODSFile pathname) {
		return (this.getAbsolutePath().compareTo(pathname.getAbsolutePath()));
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public String getParent() {
		StringBuilder pathBuilder = new StringBuilder();
		if ((directory != null) && (!directory.isEmpty())) {
			int size = directory.size();
			pathBuilder.append(directory.get(0));
			int i = 1;

			while (i < size) {
				pathBuilder.append(PATH_SEPARATOR);
				pathBuilder.append(directory.get(i));
				i++;
			}

			// parent is /
			if (pathBuilder.length() == 0) {
				pathBuilder.append("/");
			}

			return pathBuilder.toString();
		} else {

			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFile#initializeObjStatForFile()
	 */
	@Override
	public synchronized ObjStat initializeObjStatForFile()
			throws FileNotFoundException, JargonException {
		if (objStat == null) {
			objStat = irodsFileSystemAO.getObjStat(this.getAbsolutePath());
		}
		return objStat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#canExecute()
	 */
	@Override
	public synchronized boolean canExecute() {

		boolean canExecute = false;
		try {
			initializeObjStatForFile();
			canExecute = irodsFileSystemAO.isFileExecutable(this);

		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);

		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
		return canExecute;
	}

	public synchronized ObjStat getObjStat() {
		return objStat;
	}

	public synchronized void setObjStat(final ObjStat objStat) {
		this.objStat = objStat;
	}

}
