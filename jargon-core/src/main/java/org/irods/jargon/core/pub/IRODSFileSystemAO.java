package org.irods.jargon.core.pub;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileImpl;

public interface IRODSFileSystemAO extends IRODSAccessObject {

	/**
	 * Can this file be read?
	 * 
	 * @param irodsFile
	 *            <code>IRODSFile</code> that will have permissions checked.
	 * @return <code>boolean</code> that will be true if the file can be read.
	 * @throws JargonException
	 */
	boolean isFileReadable(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check write file permissions.
	 * 
	 * @param irodsFile
	 *            <code>IRODSFile</code> that will have permissions checked.
	 * @return <code>boolean</code> that is <code>true</code> if the file can be
	 *         written.
	 * @throws JargonException
	 */
	boolean isFileWriteable(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check if the given file exists in iRODS.
	 * 
	 * @param irodsFile
	 *            <code>IRODSFile</code> that is to be checked.
	 * @return <code>boolean</code> that is true if the given file exists in
	 *         iRODS.
	 * @throws JargonException
	 */
	boolean isFileExists(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check if the given path is an iRODS Collection.
	 * 
	 * @param irodsFile
	 *            <code>IRODSFile</code> to be checked.
	 * @return <code>boolean</code> that will be true if the given
	 *         <code>IRODSFile</code> is an iRODS Collection.
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             thrown if the given file does not exist in iRODS.
	 */
	boolean isDirectory(IRODSFile irodsFile) throws JargonException,
			DataNotFoundException;

	/**
	 * Get the modification date of the file.
	 * 
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	long getModificationDate(IRODSFile irodsFile) throws JargonException,
			DataNotFoundException;

	/**
	 * Get the length of the file.
	 * 
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	long getLength(IRODSFile irodsFile) throws JargonException,
			DataNotFoundException;

	/**
	 * Get a list of irodsFIles that are in the Collection. If this file is a
	 * DataObject, the files in the parent collection are given.
	 * 
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             Thrown if the file does not exist in iRODS
	 */
	List<String> getListInDir(IRODSFile irodsFile) throws JargonException,
			DataNotFoundException;

	/**
	 * Apply a filter implementation that will select result files.
	 * 
	 * @param irodsFile
	 *            <code>IRODSFile</code>
	 * @param fileNameFilter
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	List<String> getListInDirWithFilter(IRODSFile irodsFile,
			FilenameFilter fileNameFilter) throws JargonException,
			DataNotFoundException;

	/**
	 * Apply a <code>FileFilter</code> to select files in a given directory.
	 * 
	 * @param irodsFile
	 * @param fileFilter
	 * @return
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	List<File> getListInDirWithFileFilter(IRODSFile irodsFile,
			FileFilter fileFilter) throws JargonException,
			DataNotFoundException;

	/**
	 * Get the iRODS file type for the given file
	 * 
	 * @param irodsFile
	 * @return <code>IRODSFileImpl.DataType</code> enum value that is the file
	 *         type in the iRODS catalog.
	 * @throws JargonException
	 */
	IRODSFileImpl.DataType getFileDataType(final IRODSFile irodsFile)
			throws JargonException;

	int createFile(String absolutePath, DataObjInp.OpenFlags openFlags,
			int createMode) throws JargonException,
			JargonFileOrCollAlreadyExistsException;

	int createFileInResource(String absolutePath,
			DataObjInp.OpenFlags openFlags, int createMode, String resource)
			throws JargonException, JargonFileOrCollAlreadyExistsException;

	/**
	 * Create the directories in IRODS as specified by the given
	 * <code>IRODSFileImpl</code> object.
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFileImpl} describing
	 *            the desired directory path.
	 * @param recursiveOpr
	 *            <code>boolean</code> indicates whether parent directories
	 *            should also be created
	 * @throws JargonException
	 */
	void mkdir(IRODSFile irodsFile, boolean recursiveOpr)
			throws JargonException;

	/**
	 * Close the iRODS File
	 * 
	 * @param fileDescriptor
	 *            <code>int<code> with the file descriptor assigned when the file was opened or created in iRODS.
	 * @throws JargonException
	 */
	void fileClose(int fileDescriptor) throws JargonException;

	/**
	 * Delete the given directory, and do not move the file to trash. This
	 * removes the file and metadata completely from iRODS.
	 * 
	 * @param irodsFile
	 * @throws JargonException
	 */
	void directoryDeleteForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Delete the given data object. Do not move the object to trash, rather
	 * remove the file and metadata completely.
	 * 
	 * @param irodsFile
	 * @throws JargonException
	 */
	void fileDeleteForce(IRODSFile irodsFile) throws JargonException;

	// TODO: add noforce options, make default in IRODSFileImpl, and provide
	// deleteNoForce methods in IRODSFileImpl

	/**
	 * Rename the iRODS file from one path to another. This method also detects
	 * a file being moved to another iRODS resource, and if necessary will do a
	 * physical move.
	 */
	void renameFile(IRODSFile fromFile, IRODSFile toFile)
			throws JargonException;

	/**
	 * Rename the iRODS directory from one path to another. This method also
	 * detects a file being moved to another iRODS resource, and if necessary
	 * will do a physical move.
	 */
	void renameDirectory(IRODSFile fromFile, IRODSFile toFile)
			throws JargonException;

	/**
	 * Transfer a file between iRODS resources
	 * 
	 * @param fromFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} describing the
	 *            file to physically move.
	 * @param targetResource
	 *            <code>String</code> with the target resource name iRODS.
	 * @throws JargonException
	 */
	void physicalMove(IRODSFile fromFile, String targetResource)
			throws JargonException;

	/**
	 * Get the name of the first resource that stores this
	 * <code>IRODSFile</code>
	 * 
	 * @param irodsFile
	 * @return
	 * @throws JargonException
	 */
	String getResourceNameForFile(IRODSFile irodsFile) throws JargonException;

	/**
	 * Open the given file in iRODS. This will assign a file id number.
	 * 
	 * @param irodsFile
	 * @param openFlags
	 *            <code>DataObjInp.OpenFlags</code> enum value which describes
	 *            the open options.
	 * @return <code>int</code> with the internal iRODS identifier for the file.
	 * @throws JargonException
	 */
	int openFile(IRODSFile irodsFile, DataObjInp.OpenFlags openFlags)
			throws JargonException;

	/**
	 * Transfer a file between iRODS resources
	 * 
	 * @param absolutePathToSourceFile
	 *            <code>String</code> with the absolute path to the source file
	 *            in iRODS.
	 * @param absolutePathToTargetFile
	 *            <code>String</code> with the absolute path to the target file
	 *            in iRODS.
	 * @param targetResource
	 *            <code>String</code> with the target resource name iRODS.
	 * @throws JargonException
	 */
	void physicalMove(final String absolutePathToSourceFile,
			final String targetResource) throws JargonException;

	/**
	 * Delete the given data object, and move the deleted objects to the iRODS
	 * trash. Note, for a directory, that the operation is recursive by default.
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *            file/collection to be deleted
	 * @throws JargonException
	 */
	void directoryDeleteNoForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Delete the given iRODS data object, using the no force option to move the
	 * deleted file to the trash
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *            file/collection to be deleted
	 * @throws JargonException
	 */
	void fileDeleteNoForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Returns the iRODS encoded value that reflects the highest file
	 * permissions for the given iRODS collection. Note that a separate
	 * <code>getFilePermissions()</code> method is available that can retrieve
	 * the permissions for a data object. This method will get the permissions
	 * associated with the logged-in user.
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *            collection to be checked for permissions
	 * @return <code>int</code> with the iRODS encoded permissions value
	 * @throws JargonException
	 */
	int getDirectoryPermissions(IRODSFile irodsFile) throws JargonException;

	/**
	 * Returns the iRODS encoded value that reflects the highest file
	 * permissions for the given iRODS data object. Note that a separate
	 * <code>getDirectoryPermissions()</code> method is available that can
	 * retrieve the permissions for a collection. This method will get the
	 * permissions associated with the logged in user.
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} which is a data
	 *            object to be checked for permissions
	 * @return <code>int</code> with the iRODS encoded permissions value
	 * @throws JargonException
	 */
	int getFilePermissions(IRODSFile irodsFile) throws JargonException;

	/**
	 * Retrieve permission value for the given user name
	 * 
	 * @param irodsFile
	 * @param userName
	 * @return
	 * @throws JargonException
	 */
	int getDirectoryPermissionsForGivenUser(IRODSFile irodsFile, String userName)
			throws JargonException;

	/**
	 * Retrive the permission value for the given user name
	 * 
	 * @param irodsFile
	 * @param userName
	 * @return
	 * @throws JargonException
	 */
	int getFilePermissionsForGivenUser(IRODSFile irodsFile, String userName)
			throws JargonException;

	/**
	 * Check if the data object (must exist) has an executable bit set
	 * @param irodsFile {@link IRODSFile} to test
	 * @return <code>boolean</code> that is <code>true</code> if the file is executable
	 * @throws JargonException
	 */
	boolean isFileExecutable(IRODSFile irodsFile) throws JargonException;

}