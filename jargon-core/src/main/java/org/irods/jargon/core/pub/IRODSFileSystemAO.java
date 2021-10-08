package org.irods.jargon.core.pub;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;

public interface IRODSFileSystemAO extends IRODSAccessObject {

	/**
	 * Can this file be read?
	 *
	 * @param irodsFile {@code IRODSFile} that will have permissions checked.
	 * @return {@code boolean} that will be true if the file can be read.
	 * @throws FileNotFoundException if the path cannot be found
	 * @throws JargonException       for iRODS error
	 */
	boolean isFileReadable(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check write file permissions.
	 *
	 * @param irodsFile {@code IRODSFile} that will have permissions checked.
	 * @return {@code boolean} that is {@code true} if the file can be written.
	 * @throws FileNotFoundException if the path cannot be found
	 * @throws JargonException       for iRODS error
	 */
	boolean isFileWriteable(IRODSFile irodsFile) throws FileNotFoundException, JargonException;

	/**
	 * Check if the given file exists in iRODS.
	 *
	 * @param irodsFile {@code IRODSFile} that is to be checked.
	 * @return {@code boolean} that is true if the given file exists in iRODS.
	 * @throws JargonException for iRODS error
	 */
	boolean isFileExists(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check if the given path is an iRODS Collection.
	 *
	 * @param irodsFile {@code IRODSFile} to be checked.
	 * @return {@code boolean} that will be true if the given {@code IRODSFile} is
	 *         an iRODS Collection, and {@code false} if not exists or not a dir.
	 * @throws JargonException for iRODS error
	 */
	boolean isDirectory(IRODSFile irodsFile) throws JargonException;

	/**
	 * Get a list of irodsFIles that are in the Collection. If this file is a
	 * DataObject, the files in the parent collection are given.
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @return {@code List<String>}
	 * @throws JargonException       for iRODS error
	 * @throws FileNotFoundException Thrown if the file does not exist in iRODS
	 */
	List<String> getListInDir(IRODSFile irodsFile) throws FileNotFoundException, JargonException;

	/**
	 * Apply a filter implementation that will select result files.
	 *
	 * @param irodsFile      {@code IRODSFile}
	 * @param fileNameFilter {@link FilenameFilter} or null
	 * @return {@code List<String>}
	 * @throws JargonException       for iRODS error
	 * @throws DataNotFoundException for missing data
	 */
	List<String> getListInDirWithFilter(IRODSFile irodsFile, FilenameFilter fileNameFilter)
			throws JargonException, DataNotFoundException;

	/**
	 * Apply a {@code FileFilter} to select files in a given directory.
	 *
	 * @param irodsFile  {@link IRODSFile}
	 * @param fileFilter {@link FileFilter} or null
	 * @return {@code List<File>}
	 * @throws JargonException       for iRODS error
	 * @throws DataNotFoundException for missing data
	 */
	List<File> getListInDirWithFileFilter(IRODSFile irodsFile, FileFilter fileFilter)
			throws JargonException, DataNotFoundException;

	/**
	 * Get the iRODS file type for the given file
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @return {@link ObjectType} enum value that is the file type in the iRODS
	 *         catalog.
	 * @throws FileNotFoundException                  if the iRODS file is not found
	 * @throws JargonFileOrCollAlreadyExistsException if the file already exists and
	 *                                                cannot be created
	 * @throws NoResourceDefinedException             if no resource is specified,
	 *                                                and this iRODS grid does not
	 *                                                have a default resource rule
	 *                                                defined
	 * @throws JargonException                        for iRODS error
	 */
	ObjectType getFileDataType(final IRODSFile irodsFile) throws FileNotFoundException, JargonException;

	/**
	 * Create a file at the given path
	 *
	 * @param absolutePath {@link String} with iRODS path
	 * @param openFlags    {@link org.irods.jargon.core.packinstr.DataObjInp.OpenFlags}
	 * @param createMode   {@code int}
	 * @return {@code int} with file id
	 * @throws JargonFileOrCollAlreadyExistsException if file exists
	 * @throws NoResourceDefinedException             if resource is missing
	 * @throws JargonException                        for iRODS error
	 */
	int createFile(String absolutePath, DataObjInp.OpenFlags openFlags, int createMode)
			throws JargonFileOrCollAlreadyExistsException, NoResourceDefinedException, JargonException;

	/**
	 * Create a file on the given resource
	 *
	 * @param absolutePath {@link String} with iRODS path
	 * @param openFlags    {@link org.irods.jargon.core.packinstr.DataObjInp.OpenFlags}
	 * @param createMode   {@code int}
	 * @param resource     {@link String}
	 * @return {@code int}
	 * @throws JargonException                        for iRODS error
	 * @throws JargonFileOrCollAlreadyExistsException if file already exists
	 */
	int createFileInResource(String absolutePath, DataObjInp.OpenFlags openFlags, int createMode, String resource)
			throws JargonException, JargonFileOrCollAlreadyExistsException;

	/**
	 * Create the directories in IRODS as specified by the given
	 * {@code IRODSFileImpl} object.
	 *
	 * @param irodsFile    {@link IRODSFile} describing the desired directory path.
	 * @param recursiveOpr {@code boolean} indicates whether parent directories
	 *                     should also be created
	 * @throws JargonException for iRODS error
	 */
	void mkdir(IRODSFile irodsFile, boolean recursiveOpr) throws JargonException;

	/**
	 * Delete the given directory, and do not move the file to trash. This removes
	 * the file and metadata completely from iRODS.
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	void directoryDeleteForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Delete the given data object. Do not move the object to trash, rather remove
	 * the file and metadata completely.
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	void fileDeleteForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Rename the iRODS file from one path to another. This method also detects a
	 * file being moved to another iRODS resource, and if necessary will do a
	 * physical move.
	 *
	 * @param fromFile {@link IRODSFile}
	 * @param toFile   {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	void renameFile(IRODSFile fromFile, IRODSFile toFile) throws JargonException;

	/**
	 * Rename the iRODS directory from one path to another. This method also detects
	 * a file being moved to another iRODS resource, and if necessary will do a
	 * physical move.
	 *
	 * @param fromFile {@link IRODSFile}
	 * @param toFile   {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	void renameDirectory(IRODSFile fromFile, IRODSFile toFile) throws JargonException;

	/**
	 * Transfer a file between iRODS resources
	 *
	 * @param fromFile       {@link IRODSFile} describing the file to physically
	 *                       move.
	 * @param targetResource {@code String} with the target resource name iRODS.
	 * @throws JargonException for iRODS error
	 */
	void physicalMove(IRODSFile fromFile, String targetResource) throws JargonException;

	/**
	 * Get the name of the first resource that stores this {@code IRODSFile}
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @return {@code String}
	 * @throws JargonException for iRODS error
	 */
	String getResourceNameForFile(IRODSFile irodsFile) throws JargonException;

	/**
	 * Open the given file in iRODS. This will assign a file id number.
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @param openFlags {@code DataObjInp.OpenFlags} enum value which describes the
	 *                  open options.
	 * @return {@code int} with the internal iRODS identifier for the file.
	 * @throws JargonException for iRODS error
	 */
	int openFile(IRODSFile irodsFile, DataObjInp.OpenFlags openFlags) throws JargonException;

	/**
	 * Open the given file in iRODS. This will assign a file id number.
	 *
	 * @param irodsFile   {@link IRODSFile}
	 * @param openFlags   {@code DataObjInp.OpenFlags} enum value which describes
	 *                    the open options.
	 * @param coordinated {@code boolean} indicating that Jargon should coordinate
	 *                    caching of replica tokens for multiple streams
	 * @return {@code int} with the internal iRODS identifier for the file.
	 * @throws JargonException for iRODS error
	 */
	int openFile(IRODSFile irodsFile, DataObjInp.OpenFlags openFlags, boolean coordinated) throws JargonException;

	/**
	 * Transfer a file between iRODS resources
	 *
	 * @param absolutePathToSourceFile {@code String} with the absolute path to the
	 *                                 source file in iRODS.
	 * @param targetResource           {@code String} with the target resource name
	 *                                 iRODS.
	 * @throws JargonException for iRODS error
	 */
	void physicalMove(final String absolutePathToSourceFile, final String targetResource) throws JargonException;

	/**
	 * Delete the given data object, and move the deleted objects to the iRODS
	 * trash. Note, for a directory, that the operation is recursive by default.
	 *
	 * @param irodsFile {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *                  file/collection to be deleted
	 * @throws JargonException for iRODS error
	 */
	void directoryDeleteNoForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Delete the given iRODS data object, using the no force option to move the
	 * deleted file to the trash
	 *
	 * @param irodsFile {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *                  file/collection to be deleted
	 * @throws JargonException for iRODS error
	 */
	void fileDeleteNoForce(IRODSFile irodsFile) throws JargonException;

	/**
	 * Returns the iRODS encoded value that reflects the highest file permissions
	 * for the given iRODS collection. Note that a separate
	 * {@code getFilePermissions()} method is available that can retrieve the
	 * permissions for a data object. This method will get the permissions
	 * associated with the logged-in user.
	 *
	 * @param irodsFile {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *                  collection to be checked for permissions
	 * @return {@code int} with the iRODS encoded permissions value
	 * @throws JargonException for iRODS error
	 */
	int getDirectoryPermissions(IRODSFile irodsFile) throws JargonException;

	/**
	 * Returns the iRODS encoded value that reflects the highest file permissions
	 * for the given iRODS data object. Note that a separate
	 * {@code getDirectoryPermissions()} method is available that can retrieve the
	 * permissions for a collection. This method will get the permissions associated
	 * with the logged in user.
	 *
	 * @param irodsFile {@link org.irods.jargon.core.pub.io.IRODSFile} which is a
	 *                  data object to be checked for permissions
	 * @return {@code int} with the iRODS encoded permissions value
	 * @throws JargonException for iRODS error
	 */
	int getFilePermissions(IRODSFile irodsFile) throws JargonException;

	/**
	 * Retrieve permission value for the given user name
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @param userName  {@link String} userName
	 * @return {@code int}
	 * @throws JargonException       for iRODS error
	 * @throws FileNotFoundException for missing file
	 */
	int getDirectoryPermissionsForGivenUser(IRODSFile irodsFile, String userName)
			throws FileNotFoundException, JargonException;

	/**
	 * Retrive the permission value for the given user name
	 *
	 * @param irodsFile {@link IRODSFile}
	 * @param userName  {@link String}
	 * @return {@code int}
	 * @throws JargonException for iRODS error
	 */
	int getFilePermissionsForGivenUser(IRODSFile irodsFile, String userName) throws JargonException;

	/**
	 * Check if the data object (must exist) has an executable bit set
	 *
	 * @param irodsFile {@link IRODSFile} to test
	 * @return {@code boolean} that is {@code true} if the file is executable
	 * @throws JargonException for iRODS error
	 */
	boolean isFileExecutable(IRODSFile irodsFile) throws JargonException;

	/**
	 * Check if the data object (must exist) is a file versus a collection or
	 * directory
	 *
	 * @param irodsFile {@link IRODSFile} to test
	 * @return {@code  true} if a data object and it exists
	 * @throws JargonException for iRODS error
	 */
	boolean isFile(IRODSFile irodsFile) throws JargonException;

	/**
	 * Handy method to return the {@code ObjStat} that represents the given iRODS
	 * file path
	 *
	 * @param irodsAbsolutePath {@code String} with the iRODS file absolute path
	 * @return {@link ObjStat}, note that a {@code FileNotFoundException} will
	 *         result if the file is not in iRODS
	 * @throws JargonException       for iRODS error
	 * @throws FileNotFoundException if file missing
	 */
	ObjStat getObjStat(String irodsAbsolutePath) throws FileNotFoundException, JargonException;

	/**
	 * Close the file
	 *
	 * @param fileDescriptor {@code int}
	 * @param putOpr         {@code boolean}
	 * @throws JargonException for iRODS error
	 */
	void fileClose(int fileDescriptor, boolean putOpr) throws JargonException;

	/**
	 * rename a directory in irods
	 * 
	 * @param irodsFile     {@link IRODSFile}
	 * @param destIRODSFile {@link IRODSFile}
	 * @param force         {@code boolean} if force
	 */
	void renameDirectory(IRODSFile irodsFile, IRODSFile destIRODSFile, boolean force) throws JargonException;

	/**
	 * rename a file in irods
	 * 
	 * @param fromFile
	 * @param toFile
	 * @param force
	 * @throws JargonException
	 */
	void renameFile(IRODSFile fromFile, IRODSFile toFile, boolean force) throws JargonException;

}