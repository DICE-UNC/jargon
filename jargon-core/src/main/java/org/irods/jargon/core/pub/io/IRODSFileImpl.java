/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.irods.jargon.core.apiplugin.ApiPluginConstants;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.exception.ResourceHierarchyException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.ApiPluginExecutor;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.pluggable.ReplicaClose;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Describes a file or collection on the IRODS data grid. Note that
 * {@code IRODSFileImpl} is a variant of an
 * {@link org.irods.jargon.core.pub.IRODSAccessObject IRODSAccessObject}, and
 * internally holds a connection to IRODS.
 * <p>
 * This object is not thread-safe, and cannot be shared between threads. This
 * File object has a connection associated with the thread which created it.
 * There are methods in {@link org.irods.jargon.core.pub.io.IRODSFileFactory
 * IRODSFileFactory} that allow an {@code IRODSFileImpl} to be attached to
 * another Thread and connection.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public class IRODSFileImpl extends File implements IRODSFile {
	static Logger log = LoggerFactory.getLogger(IRODSFileImpl.class);

	private IRODSFileSystemAO irodsFileSystemAO = null;

	private String fileName = "";
	private String resource = "";
	private int fileDescriptor = -1;
	private List<String> directory = new ArrayList<String>();
	private OpenFlags openFlags = null;
	private String resourceToken = null; // FIXME: change to replicaToken
	private boolean coordinated = false;

	private static final long serialVersionUID = -6986662136294659059L;

	protected IRODSFileImpl(final String pathName, final IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		this("", pathName, irodsFileSystemAO);
	}

	protected IRODSFileImpl(final String parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {

		super(parent, child);

		if (irodsFileSystemAO == null) {
			throw new IllegalArgumentException("irodsFileSystemAO is null");
		}

		if (parent == null) {
			throw new IllegalArgumentException("null or missing parent name");
		}

		if (parent.isEmpty() && child.isEmpty()) {
			throw new IllegalArgumentException("both parent and child names are empty");
		}

		MiscIRODSUtils.checkPathSizeForMax(parent, child);

		this.irodsFileSystemAO = irodsFileSystemAO;
		setDirectory(MiscIRODSUtils.normalizeIrodsPath(parent));
		setFileName(child);
		makePathCanonical(parent);
	}

	protected IRODSFileImpl(final File parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {

		this(parent.getAbsolutePath(), child, irodsFileSystemAO);
	}

	/**
	 * @param dir Used to determine if the path is absolute.
	 */

	private void makePathCanonical(String dir) {
		int i = 0; // where to insert into the Vector
		boolean absolutePath = false;
		String canonicalTest = null;

		if (dir == null) {
			dir = "";
		}

		// In case this abstract path is supposed to be root
		if ((fileName.equals(IRODS_ROOT) || fileName.equals("")) && dir.equals("")) {
			return;
		}

		// In case this abstract path is supposed to be the home directory
		if (fileName.equals("") && dir.equals("")) {
			String home = irodsFileSystemAO.getIRODSAccount().getHomeDirectory();
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
			String home = irodsFileSystemAO.getIRODSAccount().getHomeDirectory();
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
			if (!home.equals("")) {
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
	 * @param dir The directory path, need not be absolute.
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
			dir = dir.substring(0, index) + PATH_SEPARATOR_CHAR + dir.substring(index + 1);
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
		if (!dir.equals("")) {
			directory.add(dir);
		}
	}

	/**
	 * Set the file name.
	 *
	 * @param filePath The file name or fileName plus some or all of the directory
	 *                 path.
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
			while ((index >= 0) && ((filePath.substring(index + 1).length()) > 0)) {
				filePath = filePath.substring(0, index) + PATH_SEPARATOR_CHAR + filePath.substring(index + 1);
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
	public boolean canRead() {
		boolean canRead = false;

		try {
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
	public boolean canWrite() {
		boolean canWrite = false;

		try {
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
	public boolean createNewFile() throws IOException {
		try {
			fileDescriptor = irodsFileSystemAO.createFile(getAbsolutePath(), DataObjInp.OpenFlags.READ_WRITE, 0600);
			log.debug("file descriptor from new file create: {}", fileDescriptor);
		} catch (JargonFileOrCollAlreadyExistsException e) {
			return false;
		} catch (ResourceHierarchyException rhe) {
			return false;
		} catch (JargonException e) {
			String msg = "JargonException caught and rethrown as IOException:" + e.getMessage();
			log.error(msg, e);
			throw new IOException(e);
		}
		return true;
	}

	@Override
	public boolean createNewFileCheckNoResourceFound(final OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException {
		try {
			fileDescriptor = irodsFileSystemAO.createFile(getAbsolutePath(), openFlags, DataObjInp.DEFAULT_CREATE_MODE);

			log.debug("file descriptor from new file create: {}", fileDescriptor);

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
	public boolean delete() {
		boolean successful = true;
		if (!exists()) {
			successful = true;
		} else {
			try {
				if (isFile()) {
					irodsFileSystemAO.fileDeleteNoForce(this);
				} else if (isDirectory()) {
					irodsFileSystemAO.directoryDeleteNoForce(this);
				}
			} catch (FileNotFoundException dnf) {
				log.info("file not found, treat as unsuccessful");
				successful = false;
			} catch (JargonException e) {

				if (e.getUnderlyingIRODSExceptionCode() == -528002) {
					log.warn("underlying rename error...delete with force option ");
					return deleteWithForceOption();
				} else {

					log.error(
							"irods error occurred on delete, this was not a data not found exception, rethrow as unchecked",
							e);
					throw new JargonRuntimeException("exception occurred on delete", e);
				}

			}
		}
		fileDescriptor = -1;
		return successful;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#deleteWithForceOption()
	 */
	@Override
	public boolean deleteWithForceOption() {
		boolean successful = true;
		try {
			if (isFile()) {
				irodsFileSystemAO.fileDeleteForce(this);
			} else if (isDirectory()) {
				irodsFileSystemAO.directoryDeleteForce(this);
			}
		} catch (FileNotFoundException fnf) {
			log.info("file not found, treat as unsuccessful");
			successful = false;
		} catch (JargonException e) {
			String msg = "JargonException caught and logged on delete:" + e.getMessage();
			log.error(msg, e);
			throw new JargonRuntimeException(msg, e);
		}
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
			return temp.getAbsolutePath().equals(getAbsolutePath());
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
	public boolean exists() {

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
			return new IRODSFileImpl(getAbsolutePath(), irodsFileSystemAO);
		} catch (JargonException e) {
			String msg = "JargonException caught and rethrown as JargonRuntimeException:" + e.getMessage();
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
					pathBuilder.append("/");
				}
				pathBuilder.append(element);
				firstPath = false;
			}

			pathBuilder.append("/");
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
			return new IRODSFileImpl(canonicalPath, irodsFileSystemAO);
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
			return new IRODSFileImpl(parentPath, irodsFileSystemAO);
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
	public boolean isDirectory() {
		log.info("isDirectory() for path:{}", getAbsolutePath());
		boolean isDir = false;
		try {

			ObjStat objStat = irodsFileSystemAO.getObjStat(getAbsolutePath());

			if (objStat.getObjectType() == ObjectType.COLLECTION || objStat.getObjectType() == ObjectType.LOCAL_DIR) {
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
	public boolean isFile() {

		log.info("isFile() for path:{}", getAbsolutePath());
		boolean isFile = false;

		try {
			ObjStat objStat = irodsFileSystemAO.getObjStat(getAbsolutePath());

			if (objStat == null) {

				log.info("looking up objStat, not cached in file");
				objStat = irodsFileSystemAO.getObjStat(getAbsolutePath());
			}

			if (objStat.getObjectType() == ObjectType.DATA_OBJECT || objStat.getObjectType() == ObjectType.LOCAL_FILE) {
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
		return getAbsolutePath();
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
	public long lastModified() {
		log.info("lastModified() for path:{}", getAbsolutePath());
		long lastMod = 0L;
		try {
			ObjStat objStat = irodsFileSystemAO.getObjStat(getAbsolutePath());
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
	public long length() {

		log.info("length() for path:{}", getAbsolutePath());

		long length = 0L;

		try {
			ObjStat objStat = irodsFileSystemAO.getObjStat(getAbsolutePath());
			length = objStat.getObjSize();
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return length of 0", e);
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
	public String[] list() {
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
	public String[] list(final FilenameFilter filter) {
		return super.list(filter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#listFiles()
	 */
	@Override
	public File[] listFiles() {

		try {
			List<String> result = irodsFileSystemAO.getListInDir(this);
			IRODSFileImpl[] a = new IRODSFileImpl[result.size()];
			IRODSFileImpl irodsFile;
			int i = 0;
			for (String fileName : result) {
				// result has just the subdir under this file, need to create
				// the absolute path to create a file
				irodsFile = new IRODSFileImpl(getAbsolutePath(), fileName, irodsFileSystemAO);
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
	public File[] listFiles(final FileFilter filter) {
		try {
			List<File> result = irodsFileSystemAO.getListInDirWithFileFilter(this, filter);
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
	 * @see org.irods.jargon.core.pub.io.IRODSFile#listFiles(java.io.FilenameFilter)
	 */
	@Override
	public File[] listFiles(final FilenameFilter filter) {
		try {
			List<String> result = irodsFileSystemAO.getListInDirWithFilter(this, filter);
			IRODSFileImpl[] a = new IRODSFileImpl[result.size()];
			IRODSFileImpl irodsFile;
			int i = 0;
			for (String fileName : result) {
				irodsFile = new IRODSFileImpl(fileName, irodsFileSystemAO);
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
		} catch (JargonFileOrCollAlreadyExistsException e) {
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
	public boolean renameTo(final IRODSFile dest) {
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
			log.info("renaming:" + getAbsolutePath() + " to:" + destIRODSFile.getAbsolutePath());
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
				irodsFileSystemAO.physicalMove(this, destIRODSFile.getResource());
				success = true;
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		}
		return success;
	}

	@Override
	public boolean renameTo(final File dest) {
		log.info("renameTo()");
		if (dest == null) {
			throw new IllegalArgumentException("dest is null");
		}

		if (!(dest instanceof IRODSFile)) {
			log.error("dest is not an IRODSFile");
			throw new IllegalArgumentException("dest is not an IRODSFile");
		}

		return renameTo((IRODSFile) dest);
	}

	/**
	 * @param destIRODSFile
	 * @throws JargonRuntimeException
	 */
	void renameFileOrDirectory(final IRODSFile destIRODSFile) throws JargonRuntimeException {
		if (isDirectory()) {
			log.info("paths different, and a directory is being renamed");
			try {
				irodsFileSystemAO.renameDirectory(this, destIRODSFile);
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		} else if (isFile()) {
			log.info("paths different, and a file is being renamed");
			try {
				irodsFileSystemAO.renameFile(this, destIRODSFile);
			} catch (JargonException e) {
				log.error("jargon exception, rethrow as unchecked", e);
				throw new JargonRuntimeException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#setExecutable(boolean, boolean)
	 */
	@Override
	public boolean setExecutable(final boolean executable, final boolean ownerOnly) {
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

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("IRODSFileImpl [");
		if (irodsFileSystemAO != null) {
			builder.append("irodsFileSystemAO=").append(irodsFileSystemAO).append(", ");
		}
		if (fileName != null) {
			builder.append("fileName=").append(fileName).append(", ");
		}
		if (resource != null) {
			builder.append("resource=").append(resource).append(", ");
		}
		builder.append("fileDescriptor=").append(fileDescriptor).append(", ");
		if (directory != null) {
			builder.append("directory=").append(directory.subList(0, Math.min(directory.size(), maxLen))).append(", ");
		}
		if (openFlags != null) {
			builder.append("openFlags=").append(openFlags).append(", ");
		}
		if (resourceToken != null) {
			builder.append("resourceToken=").append(resourceToken).append(", ");
		}
		builder.append("coordinated=").append(coordinated).append("]");
		return builder.toString();
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
				uri = new URI("irods", irodsFileSystemAO.getIRODSAccount().getUserName(),
						irodsFileSystemAO.getIRODSAccount().getHost(), irodsFileSystemAO.getIRODSAccount().getPort(),
						getAbsolutePath(), null, null);
			} else {
				uri = new URI("irods", irodsFileSystemAO.getIRODSAccount().getUserName(),
						irodsFileSystemAO.getIRODSAccount().getHost(), irodsFileSystemAO.getIRODSAccount().getPort(),
						getAbsolutePath(), null, null);
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
	 * @see org.irods.jargon.core.pub.io.IRODSFile#toFileBasedURL()
	 */
	@Override
	public URL toFileBasedURL() {
		log.info("toFileBasedURL()");
		StringBuilder sb = new StringBuilder();
		sb.append("file://");
		sb.append(getAbsolutePath());

		try {
			return new URL(sb.toString());
		} catch (MalformedURLException e) {
			log.error("malformedURL", e);
			throw new JargonRuntimeException(e);
		}

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
	public int getFileDescriptor() {
		return fileDescriptor;
	}

	/**
	 * Set the iRODS file descriptor value. This will be set internally by Jargon.
	 *
	 * @param fileDescriptor {@code int} for the file descriptor
	 */
	protected void setFileDescriptor(final int fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	private int openWithMode(final DataObjInp.OpenFlags openFlags) throws JargonException {

		if (log.isInfoEnabled()) {
			log.info("opening irodsFile:" + getAbsolutePath());
		}

		if (getFileDescriptor() > 0) {
			log.info("file is already open, use the given descriptor");
			return fileDescriptor;
		}

		int fileDescriptor = irodsFileSystemAO.openFile(this, openFlags);

		if (log.isDebugEnabled()) {
			log.debug("opened file with descriptor of:" + fileDescriptor);
		}

		this.fileDescriptor = fileDescriptor;
		this.openFlags = openFlags;
		return fileDescriptor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#openReadOnly()
	 */
	@Override
	@Deprecated
	public int openReadOnly() throws JargonException {
		log.info("openReadOnly()");
		return openWithMode(DataObjInp.OpenFlags.READ);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#open()
	 */
	@Override
	public int open() throws JargonException {
		log.info("open()");
		return openWithMode(DataObjInp.OpenFlags.READ_WRITE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFile#open(org.irods.jargon.core.packinstr
	 * .DataObjInp.OpenFlags)
	 */
	@Override
	public int open(final OpenFlags openFlags) throws JargonException {
		log.info("open()");
		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		this.openFlags = openFlags;

		log.info("openFlags:{}", openFlags);
		return openWithMode(openFlags);
	}

	@Override
	public int open(final OpenFlags openFlags, final boolean coordinated) throws JargonException {
		log.info("open()");

		// FIXME: should I still mess w/replica tokens when a file is opened for read,
		// etc?

		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		this.openFlags = openFlags;
		this.coordinated = coordinated;

		log.info("openFlags:{}", openFlags);
		log.info("coordinated:{}", coordinated);
		return openWithMode(openFlags, coordinated);
	}

	public int openWithMode(OpenFlags openFlags, boolean coordinated) throws JargonException {
		log.info("openWithMode()");

		log.info("openFlags:{}", openFlags);
		log.info("coordinated:{}", coordinated);

		if (getFileDescriptor() > 0) {
			log.info("file is already open, use the given descriptor");
			return fileDescriptor;
		}

		int fileDescriptor = irodsFileSystemAO.openFile(this, openFlags, coordinated);

		if (log.isDebugEnabled()) {
			log.debug("opened file with descriptor of:" + fileDescriptor);
		}

		this.fileDescriptor = fileDescriptor;
		this.openFlags = openFlags;
		this.coordinated = coordinated;

		return fileDescriptor;
	}

	@Override
	public void close(final boolean updateSize, final boolean updateStatus, final boolean computeChecksum,
			final boolean sendNotifications, final boolean preserveReplicaStateTable) throws JargonException {

		log.info("close() with flags");

		/*
		 * If I'm calling this on iRODS when it doesn't support replica tokens, then
		 * this is a usage error
		 * 
		 * if we're not holding a resource token we will go to the normal close,
		 * otherwise we will process with the provided flags. This is a relaxed
		 * interpretation and we can covert this if it creates unexpected behaviors
		 * 
		 */

		if (!this.getIrodsFileSystemAO().getIRODSServerProperties().isSupportsReplicaTokens()) {
			log.error("iRODS does not support replica tokens");
			throw new UnsupportedOperationException("This version of iRODS does not support replica tokens");
		}

		// check if I have a replica token, in which case you will do a replica close
		if (this.getReplicaToken() != null && this.coordinated) {
			log.info("close with a replica token, see if this is the last close");

			Lock replicaLock = null;
			try {
				replicaLock = IRODSSession.replicaTokenCacheManager.obtainReplicaTokenLock(this.getAbsolutePath());
				replicaLock.tryLock(); // TODO: add timeout>

				boolean isFinalReplicaClose = IRODSSession.replicaTokenCacheManager
						.closeReplicaToken(this.getAbsolutePath());
				log.info("is this is the final replica close?:{}", isFinalReplicaClose);

				ReplicaClose replicaClose = new ReplicaClose();
				replicaClose.setFd(this.fileDescriptor);

				if (isFinalReplicaClose) {

					// TODO: what of these options are indicated on final close?

					replicaClose.setPreserveReplicaStateTable(preserveReplicaStateTable);
					replicaClose.setSendNotifications(sendNotifications);
					replicaClose.setUpdateSize(updateSize);
					replicaClose.setUpdateStatus(updateStatus);
					replicaClose.setComputeChecksum(computeChecksum);
				} else {
					replicaClose.setPreserveReplicaStateTable(false);
					replicaClose.setSendNotifications(false);
					replicaClose.setUpdateSize(false);
					replicaClose.setUpdateStatus(false);
					replicaClose.setComputeChecksum(false);
				}

				try {
					String replicaCloseString = IRODSSession.objectMapper.writeValueAsString(replicaClose);
					ApiPluginExecutor apiPluginExecutor = this.getIrodsFileSystemAO().getIRODSAccessObjectFactory()
							.getApiPluginExecutor(this.getIrodsFileSystemAO().getIRODSAccount());
					apiPluginExecutor.callPluggableApi(ApiPluginConstants.REPLICA_CLOSE_APN, replicaCloseString);

				} catch (JsonProcessingException e) {
					log.error("error writing json", e);
					throw new JargonException("error writing replica close", e);
				}

			} finally {
				if (replicaLock != null) {
					replicaLock.unlock();
				}
			}

			/*
			 * determine if this is the final replica from the cache this is only done when
			 * the file was open in the 'coordinated' mode
			 */

		} else {

			if (openFlags == OpenFlags.WRITE || openFlags == OpenFlags.WRITE_FAIL_IF_EXISTS
					|| openFlags == OpenFlags.WRITE_TRUNCATE) {
				log.info("closing with putOpr, check if i need to use a replica close (4.2.9+");
				irodsFileSystemAO.fileClose(getFileDescriptor(), true);
			} else {
				irodsFileSystemAO.fileClose(getFileDescriptor(), false);

			}
		}

		setFileDescriptor(-1);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#close()
	 */
	@Override
	public void close() throws JargonException {

		this.close(true, true, this.irodsFileSystemAO.getJargonProperties().isComputeChecksumAfterTransfer(), true,
				true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#closeGivenDescriptor(int)
	 */
	@Override
	public void closeGivenDescriptor(final int fd) throws JargonException {
		if (log.isInfoEnabled()) {
			log.info("closing irodsFile given descriptor:" + fd);
		}

		if (fd <= 0) {
			log.info("file is not open, silently ignore");
			setFileDescriptor(-1);
			return;
		}

		irodsFileSystemAO.fileClose(fd, false);
		setFileDescriptor(-1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFile#compareTo(java.io.File)
	 */
	@Override
	public int compareTo(final IRODSFile pathname) {
		return (getAbsolutePath().compareTo(pathname.getAbsolutePath()));
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
	 * @see java.io.File#canExecute()
	 */
	@Override
	public boolean canExecute() {

		boolean canExecute = false;
		try {
			canExecute = irodsFileSystemAO.isFileExecutable(this);
		} catch (FileNotFoundException e) {
			log.warn("file not found exception, return false", e);

		} catch (JargonException e) {
			log.error("jargon exception, rethrow as unchecked", e);
			throw new JargonRuntimeException(e);
		}
		return canExecute;
	}

	/**
	 * @return the openFlags
	 */
	@Override
	public OpenFlags getOpenFlags() {
		return openFlags;
	}

	/**
	 * @param openFlags the openFlags to set
	 */
	@Override
	public void setOpenFlags(final OpenFlags openFlags) {
		this.openFlags = openFlags;
	}

	public IRODSFileSystemAO getIrodsFileSystemAO() {
		return irodsFileSystemAO;
	}

	public void setIrodsFileSystemAO(final IRODSFileSystemAO irodsFileSystemAO) {
		this.irodsFileSystemAO = irodsFileSystemAO;
	}

	/**
	 * Get the resource token that may have been obtained when the file was opened.
	 * This is only present in later versions of iRODS.
	 * 
	 * @return {@code String} with the resource token, {code null} indicates that no
	 *         token exists
	 */
	@Override
	public String getReplicaToken() {
		return resourceToken;
	}

	/**
	 * Set the resource token if one is available (dependant on iRODS version)
	 * 
	 * @param resourceToken {@code String} with the resource token value, if
	 *                      available
	 */
	@Override
	public void setReplicaToken(String resourceToken) {
		this.resourceToken = resourceToken;
	}

	public boolean isCoordinated() {
		return coordinated;
	}

	public void setCoordinated(boolean coordinated) {
		this.coordinated = coordinated;
	}

}
