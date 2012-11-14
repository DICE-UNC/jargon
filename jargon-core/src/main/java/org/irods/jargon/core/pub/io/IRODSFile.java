package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.pub.domain.ObjStat;

/**
 * Interface followed by {@link org.irods.jargon.core.pub.io.IRODSFileImpl}. The
 * class <code>IRODSFileImpl</code> extends the <code>java.io.File</code> class.
 * This interface is available to allow easier testing and potentially other
 * implementations.
 * <p/>
 * The <code>IRODSFile</code>, and the <code>IRODSFileImpl</code> implementation
 * class are meant to strictly follow the <code>java.io.File</code> interface,
 * with a minimum of iRODS-specific methods.
 * <p/>
 * The <code>org.irods.jargon.core.pub.io.</code> classes provide familiar file
 * operations. In older versions of Jargon, various iRODS operations were mixed
 * in with the <code>java.io.*</code> implementation classes, and these have
 * been refactored to a set of access objects found in the
 * <code>org.irods.jargon.core.pub.*</code> classes. There you will find
 * facilities to manipulate the metadata catalog entities, do AVU operations,
 * transfers of various sorts, queries, and other iRODS operations.
 * <p/>
 * This code handles soft linked files and collections as expected. You may
 * operate on canoncial paths or soft-linked paths.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSFile {

	public static final char PATH_SEPARATOR_CHAR = '/';
	public static final String PATH_SEPARATOR = "/";
	public static final int PATH_IS_UNKNOWN = 0;
	public static final int PATH_IS_FILE = 1;
	public static final int PATH_IS_DIRECTORY = 2;
	public static final int READ_PERMISSIONS = 1050;
	public static final int WRITE_PERMISSIONS = 1120;
	public static final int OWN_PERMISSIONS = 1200;
	public static final String IRODS_ROOT = "/";

	boolean canRead();

	boolean canWrite();

	boolean canExecute();

	boolean createNewFile() throws IOException;

	/**
	 * Delete the given iRODS file. Note that, by default, the data is moved to
	 * the trash (no force option). Note that for a collection, the iRODS
	 * collection is recursively deleted.
	 * 
	 * @return <code>boolean<code> with the success of the delete operation.
	 */
	boolean delete();

	void deleteOnExit();

	@Override
	boolean equals(Object obj);

	boolean exists();

	File getAbsoluteFile();

	String getAbsolutePath();

	File getCanonicalFile() throws IOException;

	String getCanonicalPath() throws IOException;

	long getFreeSpace();

	File getParentFile();

	long getTotalSpace();

	long getUsableSpace();

	@Override
	int hashCode();

	boolean isAbsolute();

	boolean isDirectory();

	boolean isFile();

	String getPath();

	boolean isHidden();

	long lastModified();

	long length();

	String[] list();

	String[] list(FilenameFilter filter);

	File[] listFiles();

	File[] listFiles(FileFilter filter);

	File[] listFiles(FilenameFilter filter);

	boolean mkdir();

	boolean mkdirs();

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 */
	boolean setExecutable(boolean executable, boolean ownerOnly);

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 */
	boolean setExecutable(boolean executable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	boolean setLastModified(long time);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	boolean setReadable(boolean readable, boolean ownerOnly);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	boolean setReadable(boolean readable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	boolean setReadOnly();

	boolean setWritable(boolean writable, boolean ownerOnly);

	boolean setWritable(boolean writable);

	@Override
	String toString();

	URI toURI();

	/**
	 * Get the resource (if set by the user) associated with the file. Note that
	 * this does not inquire to the iCAT for the resource for this particular
	 * file, instead, this is used by any Jargon methods that have
	 * <code>IRODSFile</code> as a paramenter to tell iRODS what resoruce to
	 * operate with.
	 * 
	 * @return
	 * @throws JargonException
	 */
	String getResource() throws JargonException;

	/**
	 * Set the resource (if set by the user) associated with the file. Note that
	 * this does not inquire to the iCAT for the resource for this particular
	 * file, instead, this is used by any Jargon methods that have
	 * <code>IRODSFile</code> as a paramenter to tell iRODS what resoruce to
	 * operate with.
	 * 
	 * @param resource
	 */
	void setResource(String resource);

	String getName();

	String getParent();

	int getFileDescriptor();

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). This method
	 * will open the file in read/write mode.
	 * 
	 * @return <code>int</code> with the irods file descriptor.
	 * @throws JargonException
	 */
	int open() throws JargonException;

	/**
	 * Open the iROD file (obtaining a fiel desriptor from iRODS). This method
	 * will open the file in read-only mode.
	 * 
	 * @return <code>int</code> with the irods file descriptor.
	 * @throws JargonException
	 */
	int openReadOnly() throws JargonException;

	void close() throws JargonException;

	int compareTo(IRODSFile irodsFile2);

	boolean renameTo(IRODSFile dest);

	/**
	 * Delete the given file, and use the iRODS force option. File will not be
	 * moved to trash, and metadata will be deleted. Note that if the given
	 * iRODS file is a collection, the delete will be automatically recursive.
	 * This particular method is not part of the standard
	 * <code>java.io.File</code> contracts.
	 * 
	 * @return <code>boolean</code> with success of operation.
	 */
	boolean deleteWithForceOption();

	/**
	 * Reset cached data about the file (exists, type, length) so it can be
	 * accessed again
	 */
	void reset();

	/**
	 * Special form of close that can take a file descriptor to close. This has
	 * special uses for narrow cases in Jargon, and should not typically be
	 * used. In normal usage, the <code>IRODSFile</code> keeps track of its file
	 * descriptor.
	 * 
	 * @param fd
	 *            <code>int<code> with the file descriptor associated with this file that should be closed in iRODS.
	 * @throws JargonException
	 */
	void closeGivenDescriptor(int fd) throws JargonException;

	/**
	 * Initialize the cached <code>ObjStat</code> for this
	 * <code>IRODSFile</code>. The cached stat describes the file information,
	 * and is used in determining length, whether it is a collection, and other
	 * file basics. It also establishes the zone for the file for use in
	 * querying things like file permissions. Calling this method will set the
	 * <code>objStat</code> instance variable, and as such, it is synchronized.
	 * 
	 * @return {@link ObjStat} describing the iRODS file
	 * @throws FileNotFoundException
	 *             if the file <code>ObjStat</code> cannot be found
	 * @throws JargonException
	 */
	ObjStat initializeObjStatForFile() throws FileNotFoundException,
			JargonException;

	/**
	 * Create a new file, and detect errors where no default storage resource is
	 * available. This is an iRODS oriented variation on the
	 * <code>java.io.File createNewFile()</code> method to handle cases where no
	 * defautl storage resource is found
	 * 
	 * @return <code>boolean</code> of <code>true</code> if the file could be
	 *         created
	 * @throws NoResourceDefinedException
	 *             if no default storage resource is defined, and no default
	 *             rule is installed in iRODS
	 * @throws JargonException
	 */
	boolean createNewFileCheckNoResourceFound()
			throws NoResourceDefinedException, JargonException;

}