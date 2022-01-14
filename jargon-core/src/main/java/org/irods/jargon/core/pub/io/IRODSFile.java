package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;

/**
 * Interface followed by {@link org.irods.jargon.core.pub.io.IRODSFileImpl}. The
 * class {@code IRODSFileImpl} extends the {@code java.io.File} class. This
 * interface is available to allow easier testing and potentially other
 * implementations.
 * <p>
 * The {@code IRODSFile}, and the {@code IRODSFileImpl} implementation class are
 * meant to strictly follow the {@code java.io.File} interface, with a minimum
 * of iRODS-specific methods.
 * <p>
 * The {@code org.irods.jargon.core.pub.io.} classes provide familiar file
 * operations. In older versions of Jargon, various iRODS operations were mixed
 * in with the {@code java.io.*} implementation classes, and these have been
 * refactored to a set of access objects found in the
 * {@code org.irods.jargon.core.pub.*} classes. There you will find facilities
 * to manipulate the metadata catalog entities, do AVU operations, transfers of
 * various sorts, queries, and other iRODS operations.
 * <p>
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
	 * Delete the given iRODS file. Note that, by default, the data is moved to the
	 * trash (no force option). Note that for a collection, the iRODS collection is
	 * recursively deleted.
	 *
	 * @return {@code boolean} with the success of the delete operation.
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
	 * Marks the file or directory named by this abstract pathname so that only read
	 * operations are allowed. After invoking this method the file or directory is
	 * guaranteed not to change until it is either deleted or marked to allow write
	 * access. Whether or not a read-only file or directory may be deleted depends
	 * upon the underlying system.
	 *
	 * @param executable {@code boolean} with the desired execute state
	 * @param ownerOnly  {@code boolean} if exec by owner only
	 * @return {@code boolean} indicating success
	 */
	boolean setExecutable(boolean executable, boolean ownerOnly);

	/**
	 * Marks the file or directory named by this abstract pathname so that only read
	 * operations are allowed. After invoking this method the file or directory is
	 * guaranteed not to change until it is either deleted or marked to allow write
	 * access. Whether or not a read-only file or directory may be deleted depends
	 * upon the underlying system.
	 *
	 * @param executable {@code boolean} with executable state
	 * @return {@code boolean} indicating success
	 */
	boolean setExecutable(boolean executable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * {@code UnsupportedOperationException} if called.
	 *
	 * @param time {@code long} with the last modified date
	 * @return {@code boolean} indicating success
	 */
	boolean setLastModified(long time);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * {@code UnsupportedOperationException} if called.
	 *
	 * @param readable  {@code boolean} indicating the readable state to set
	 * @param ownerOnly {@code boolean} indicating whether this is set for the owner
	 *                  only
	 * @return {@code boolean} indicating success
	 */
	boolean setReadable(boolean readable, boolean ownerOnly);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * {@code UnsupportedOperationException} if called.
	 *
	 * @param readable {@code boolean} indicating desired read state
	 * @return {@code boolean} indicating success
	 */
	boolean setReadable(boolean readable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * {@code UnsupportedOperationException} if called.
	 *
	 * @return {@code boolean} indicating success
	 */
	boolean setReadOnly();

	boolean setWritable(boolean writable, boolean ownerOnly);

	boolean setWritable(boolean writable);

	@Override
	String toString();

	URI toURI();

	/**
	 * Get the resource (if set by the user) associated with the file. Note that
	 * this does not inquire to the iCAT for the resource for this particular file,
	 * instead, this is used by any Jargon methods that have {@code IRODSFile} as a
	 * parameter to tell iRODS what resoruce to operate with.
	 *
	 * @return {@code String} with the resource name
	 * @throws JargonException for iRODS error
	 */
	String getResource() throws JargonException;

	/**
	 * Set the resource (if set by the user) associated with the file. Note that
	 * this does not inquire to the iCAT for the resource for this particular file,
	 * instead, this is used by any Jargon methods that have {@code IRODSFile} as a
	 * paramenter to tell iRODS what resoruce to operate with.
	 *
	 * @param resource {@code String} with the resource name
	 */
	void setResource(String resource);

	String getName();

	String getParent();

	int getFileDescriptor();

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). This method
	 * will open the file in read/write mode.
	 *
	 * @return {@code int} with the iRODS file descriptor.
	 * @throws JargonException for iRODS error
	 */
	int open() throws JargonException;

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). THis method
	 * will open the file according to the provided flags
	 *
	 * @param openFlags {@link OpenFlags} enum value that will dictate the open
	 *                  behavior
	 * @return {@code int} with the iRODS file descriptor value
	 * @throws JargonException for iRODS error
	 */
	int open(final OpenFlags openFlags) throws JargonException;

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). THis method
	 * will open the file according to the provided flags
	 *
	 * @param openFlags   {@link OpenFlags} enum value that will dictate the open
	 *                    behavior
	 * @param coordinated {@code boolean} indicating that Jargon should coordinate
	 *                    replica token caching when multiple streams are opened
	 * @return {@code int} with the iRODS file descriptor value
	 * @throws JargonException for iRODS error
	 */
	int open(final OpenFlags openFlags, final boolean coordinated) throws JargonException;

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). This method
	 * will open the file in read-only mode.
	 *
	 * @return {@code int} with the irods file descriptor.
	 * @throws JargonException for iRODS error
	 * @deprecated use the @{code open(OpenFlags)} method
	 */
	@Deprecated
	int openReadOnly() throws JargonException;

	void close() throws JargonException;

	/**
	 * Call a close on the file (that may underlie a stream). This close is only
	 * used for iRODS versions that support replica tokens and will case an
	 * {@link UnsupportedOperationException} if invoked on a version of iRODS that
	 * does not support this style of close
	 * 
	 * @param updateSize                {@code boolean} update size in catalog
	 * @param updateStatus              {@code boolean} update status in catalog
	 * @param computeChecksum           {@code boolean} compute the checksum
	 * @param sendNotifications         {@code boolean} send notifications
	 * @param preserveReplicaStateTable {@code boolean} preserve replica state table
	 * @throws JargonException {@link JargonException}
	 */
	void close(boolean updateSize, boolean updateStatus, boolean computeChecksum, boolean sendNotifications,
			boolean preserveReplicaStateTable) throws JargonException;

	int compareTo(IRODSFile irodsFile2);

	/**
	 * Rename to a new target location
	 *
	 * @param dest {@link IRODSFile} that is the target of the rename
	 * @return {@code boolean} if successful
	 *
	 */
	boolean renameTo(IRODSFile dest);

	/**
	 * Rename to a new target location
	 *
	 * @param dest {@link File} that is the target. This must be an
	 *             {@link IRODSFile} and will be cast as such in the implementation,
	 *             other types will cause an exception. This method conforms more
	 *             closely to the {@link File} API.
	 * @return {@code boolean} if successful
	 */
	boolean renameTo(File dest);

	/**
	 * Delete the given file, and use the iRODS force option. File will not be moved
	 * to trash, and metadata will be deleted. Note that if the given iRODS file is
	 * a collection, the delete will be automatically recursive. This particular
	 * method is not part of the standard {@code java.io.File} contracts.
	 *
	 * @return {@code boolean} with success of operation.
	 */
	boolean deleteWithForceOption();

	/**
	 * Special form of close that can take a file descriptor to close. This has
	 * special uses for narrow cases in Jargon, and should not typically be used. In
	 * normal usage, the {@code IRODSFile} keeps track of its file descriptor.
	 *
	 * @param fd {@code int} with the file descriptor associated with this file that
	 *           should be closed in iRODS.
	 * @throws JargonException for iRODS error
	 */
	void closeGivenDescriptor(int fd) throws JargonException;

	/**
	 * Create a new file, and detect errors where no default storage resource is
	 * available. This is an iRODS oriented variation on the
	 * {@code java.io.File createNewFile()} method to handle cases where no defautl
	 * storage resource is found
	 *
	 * @param openFlags {@link OpenFlags} for create
	 *
	 * @return {@code boolean} of {@code true} if the file could be created
	 * @throws NoResourceDefinedException if no default storage resource is defined,
	 *                                    and no default rule is installed in iRODS
	 * @throws JargonException            for iRODS error
	 * @throws NoResourceDefinedException when resource is missing
	 */
	boolean createNewFileCheckNoResourceFound(OpenFlags openFlags) throws NoResourceDefinedException, JargonException;

	/**
	 * Handy method to return a file:// based URL from the iRODS file, instead of
	 * the irods:// protocol format
	 *
	 * @return {@link URL} in file:// format
	 */
	public abstract URL toFileBasedURL();

	public abstract void setOpenFlags(OpenFlags openFlags);

	public abstract OpenFlags getOpenFlags();

	/**
	 * Get the resource token that may have been obtained when the file was opened.
	 * This is only present in later versions of iRODS.
	 * 
	 * @return {@code String} with the resource token, {code null} indicates that no
	 *         token exists
	 */
	String getReplicaToken();

	/**
	 * Set the replica token if one is available (dependent on iRODS version)
	 * 
	 * @param replicaToken {@code String} with the replica token value, if
	 *                      available
	 */
	void setReplicaToken(String replicaToken);

}
