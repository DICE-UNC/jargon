package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface followed by {@link org.irods.jargon.core.pub.io.IRODSFileImpl}. The
 * class <code>IRODSFileImpl</code> extends the <code>java.io.File</code> class.
 * This interface is available to allow easier testing and potentially other
 * implementations.
 * 
 * The <code>IRODSFile</code>, and the <code>IRODSFileImpl</code> implementation
 * class are meant to strictly follow the <code>java.io.File</code> interface,
 * with a minimum of iRODS-specific methods. The
 * <code>org.irods.jargon.core.pub.io.</code> classes provide familiar file
 * operations. In older versions of Jargon, various iRODS operations were mixed
 * in with the <code>java.io.*</code> implementation classes, and these have
 * been refactored to a set of access objects found in the
 * <code>org.irods.jargon.core.pub.*</code> classes. There you will find
 * facilities to manipulate the metadata catalog entities, do AVU operations,
 * transfers of various sorts, queries, and other iRODS operations.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSFile {

	public enum DataType {
		GENERIC, DIRECTORY, UNKNOWN
	};

	public enum PathNameType {
		UNKNOWN, FILE, DIRECTORY
	}

	public static final char PATH_SEPARATOR_CHAR = '/';
	public static final String PATH_SEPARATOR = "/";
	public static final int PATH_IS_UNKNOWN = 0;
	public static final int PATH_IS_FILE = 1;
	public static final int PATH_IS_DIRECTORY = 2;
	public static final int READ_PERMISSIONS = 1050;
	public static final int WRITE_PERMISSIONS = 1120;
	public static final String IRODS_ROOT = "/";

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract boolean createNewFile() throws IOException;

	/**
	 * Delete the given iRODS file. Note that, by default, the data is moved to
	 * the trash (no force option). Note that for a collection, the iRODS
	 * collection is recursively deleted.
	 * 
	 * @return <code>boolean<code> with the success of the delete operation.
	 */
	public abstract boolean delete();

	public abstract void deleteOnExit();

	@Override
	public abstract boolean equals(Object obj);

	public abstract boolean exists();

	public abstract File getAbsoluteFile();

	public abstract String getAbsolutePath();

	public abstract File getCanonicalFile() throws IOException;

	public abstract String getCanonicalPath() throws IOException;

	public abstract long getFreeSpace();

	public abstract File getParentFile();

	public abstract long getTotalSpace();

	public abstract long getUsableSpace();

	@Override
	public abstract int hashCode();

	public abstract boolean isAbsolute();

	public abstract boolean isDirectory();

	public abstract boolean isFile();

	public abstract String getPath();

	public abstract boolean isHidden();

	public abstract long lastModified();

	public abstract long length();

	public abstract String[] list();

	public abstract String[] list(FilenameFilter filter);

	public abstract File[] listFiles();

	public abstract File[] listFiles(FileFilter filter);

	public abstract File[] listFiles(FilenameFilter filter);

	public abstract boolean mkdir();

	public abstract boolean mkdirs();

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 */
	public abstract boolean setExecutable(boolean executable, boolean ownerOnly);

	/**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 */
	public abstract boolean setExecutable(boolean executable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	public abstract boolean setLastModified(long time);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	public abstract boolean setReadable(boolean readable, boolean ownerOnly);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	public abstract boolean setReadable(boolean readable);

	/**
	 * This method is not implemented for IRODS, and will throw an
	 * <code>UnsupportedOperationException</code> if called.
	 */
	public abstract boolean setReadOnly();

	public abstract boolean setWritable(boolean writable, boolean ownerOnly);

	public abstract boolean setWritable(boolean writable);

	@Override
	public abstract String toString();

	public abstract URI toURI();

	public abstract String getResource() throws JargonException;

	public abstract void setResource(String resource);

	public String getName();

	public String getParent();

	public int getFileDescriptor();

	/**
	 * Open the iRODS file (obtaining a file descriptor from iRODS). This method
	 * will open the file in read/write mode.
	 * 
	 * @return <code>int</code> with the irods file descriptor.
	 * @throws JargonException
	 */
	public int open() throws JargonException;

	/**
	 * Open the iROD file (obtaining a fiel desriptor from iRODS). This method
	 * will open the file in read-only mode.
	 * 
	 * @return <code>int</code> with the irods file descriptor.
	 * @throws JargonException
	 */
	public int openReadOnly() throws JargonException;

	public void close() throws JargonException;

	public int compareTo(IRODSFile irodsFile2);

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
	public boolean deleteWithForceOption();

}