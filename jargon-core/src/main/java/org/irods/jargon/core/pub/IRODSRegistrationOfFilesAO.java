package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for an access object that can handle the registration of files to
 * iRODS. This mirrors the functionality of the ireg command
 * <p>
 * https://www.irods.org/index.php/ireg
 * <p>
 * Register a file or a directory of files and subdirectory into iRODS. The file
 * or the directory of files must already exist on the server where the resource
 * is located. The full path must be supplied for both the physicalFilePath and
 * the irodsPath.
 * <p>
 * An admin user will be able to register any Unix directory. But for a regular
 * user, he/she needs to have a UNIX account on the server with the same name as
 * his/her iRODS user account and only UNIX directories created with this
 * account can be registered by the user. Access control to the registered data
 * will be based on the access permission of the registeed collection. For
 * security reasons, file permissions are checked to make sure that the client
 * has the proper permission for the registration. The acNoChkFilePathPerm rule
 * in core.re can be used to bypass the path checking.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface IRODSRegistrationOfFilesAO {

	/**
	 * Register a collection (directory) to iRODS. This is different from a put
	 * or a copy in the sense that the file already exists on the resource
	 * server, and is not in the iRODS vault. The file remains in place and is
	 * added to the iRODS catalog. If this is a file (data object) instead of a
	 * collection, an error will result.
	 *
	 * @param physicalPath
	 *            {@code String} with the absolute path to the physical
	 *            file located on the iRODS resource server.
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the desired
	 *            iRODS location the file will be registered under.
	 * @param force
	 *            {@code boolean<code> which, if <code>true} indicates that
	 *            an overwrite of an iRODS path will occur.
	 * @param destinationResource
	 *            {@code String}, blank if the default should be used, that
	 *            indicates the the resource to store to. This can also be
	 *            specified in your environment or via a rule set up by the
	 *            administrator.
	 * @param resourceGroup
	 *            {@code String} with a resource group for the resource.
	 *            This may be set to blank, if not used. If this is specified, a
	 *            {@code destinationResource} must also be specified.
	 * @throws DataNotFoundException
	 *             if the flle to register or the target collection does not
	 *             exist
	 * @throws DuplicateDataException
	 *             if the file has already been registered, and force is not
	 *             specified
	 * @throws JargonException
	 */
	void registerPhysicalCollectionRecursivelyToIRODS(
			final String physicalPath, final String irodsAbsolutePath,
			final boolean force, final String destinationResource,
			final String resourceGroup) throws DataNotFoundException,
			DuplicateDataException, JargonException;

	/**
	 * Register a single file (data object) to iRODS. This can also, if the
	 * {@code generateChecksumInIRODS} value is {@code true}, cause an
	 * iRODS checksum to be computed and stored. Note that there is a separate
	 * method that generates a local checksum and causes it to be verified in
	 * iRODS.
	 * <p>
	 * This method is for data objects, and will cause an error if the provided
	 * paths are an iRODS collection.
	 *
	 * @param physicalPath
	 *            {@code String} with the absolute path to the physical
	 *            file located on the iRODS resource server.
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the desired
	 *            iRODS location the file will be registered under.
	 * @param destinationResource
	 *            {@code String}, blank if the default should be used, that
	 *            indicates the the resource to store to. This can also be
	 *            specified in your environment or via a rule set up by the
	 *            administrator.
	 * @param resourceGroup
	 *            {@code String} with a resource group for the resource.
	 *            This may be set to blank, if not used. If this is specified, a
	 *            {@code destinationResource} must also be specified.
	 * @param generateChecksumInIRODS
	 *            {@code boolean<code> that, if <code>true} cause iRODS to
	 *            generate a checksum value and store it in the catalog.
	 * @throws DataNotFoundException
	 *             if the flle to register or the target collection does not
	 *             exist
	 * @throws DuplicateDataException
	 *             if the file has already been registered, and force is not
	 *             specified
	 * @throws JargonException
	 */
	void registerPhysicalDataFileToIRODS(final String physicalPath,
			final String irodsAbsolutePath, final String destinationResource,
			final String resourceGroup, final boolean generateChecksumInIRODS)
					throws DataNotFoundException, DuplicateDataException,
					JargonException;

	/**
	 * Register a single file (data object) to iRODS. This method will first
	 * generate a checksum value for the local file, and then send this checksum
	 * to irods so that it can be verified and stored.
	 * <p>
	 * This method is for data objects, and will cause an error if the provided
	 * paths are an iRODS collection.
	 *
	 * @param physicalPath
	 *            {@code String} with the absolute path to the physical
	 *            file located on the iRODS resource server.
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the desired
	 *            iRODS location the file will be registered under.
	 * @param destinationResource
	 *            {@code String}, blank if the default should be used, that
	 *            indicates the the resource to store to. This can also be
	 *            specified in your environment or via a rule set up by the
	 *            administrator.
	 * @param resourceGroup
	 *            {@code String} with a resource group for the resource.
	 *            This may be set to blank, if not used. If this is specified, a
	 *            {@code destinationResource} must also be specified.
	 * @return {@code String} with the locally generated checksum value
	 *         that was sent to iRODS.
	 * @throws DataNotFoundException
	 *             if the flle to register or the target collection does not
	 *             exist
	 * @throws DuplicateDataException
	 *             if the file has already been registered, and force is not
	 *             specified
	 * @throws JargonException
	 */
	String registerPhysicalDataFileToIRODSWithVerifyLocalChecksum(
			final String physicalPath, final String irodsAbsolutePath,
			final String destinationResource, final String resourceGroup)
					throws DataNotFoundException, DuplicateDataException,
					JargonException;

	/**
	 * Remove this registered file from the iRODS catalog. Note that this method
	 * does not delete the physical file.
	 * <p>
	 * This method is analagous to calling the irm icommand with the -U flag.
	 * Please see: https://www.irods.org/index.php/irm
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the iRODS file
	 *            to be unregistered.
	 * @return {@code boolean} that indicates that the unregister was
	 *         successful (e.g. a data not found exception on delete is not
	 *         returned as an exception, but the return will be a
	 *         {@code false}
	 * @throws JargonException
	 */
	boolean unregisterDataObject(final String irodsAbsolutePath)
			throws JargonException;

	/**
	 * Register a single file (data object) to iRODS as a replica of the given
	 * iRODS data object. This can also, if the
	 * {@code generateChecksumInIRODS} value is {@code true}, cause an
	 * iRODS checksum to be computed and stored. Note that there is a separate
	 * method that generates a local checksum and causes it to be verified in
	 * iRODS.
	 * <p>
	 * This method is for data objects, and will cause an error if the provided
	 * paths are an iRODS collection.
	 *
	 * @param physicalPath
	 *            {@code String} with the absolute path to the physical
	 *            file located on the iRODS resource server.
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the desired
	 *            iRODS location the file will be registered under.
	 * @param destinationResource
	 *            {@code String}, blank if the default should be used, that
	 *            indicates the the resource to store to. This can also be
	 *            specified in your environment or via a rule set up by the
	 *            administrator.
	 * @param resourceGroup
	 *            {@code String} with a resource group for the resource.
	 *            This may be set to blank, if not used. If this is specified, a
	 *            {@code destinationResource} must also be specified.
	 * @param generateChecksumInIRODS
	 *            {@code boolean<code> that, if <code>true} cause iRODS to
	 *            generate a checksum value and store it in the catalog.
	 * @throws DataNotFoundException
	 *             if the flle to register or the target collection does not
	 *             exist. If this is the first version (instead of a replica)
	 *             you will also get this exception.
	 * @throws DuplicateDataException
	 *             if the file has already been registered, and force is not
	 *             specified
	 * @throws JargonException
	 */
	void registerPhysicalDataFileToIRODSAsAReplica(final String physicalPath,
			final String irodsAbsolutePath, final String destinationResource,
			final String resourceGroup, final boolean generateChecksumInIRODS)
					throws DataNotFoundException, DuplicateDataException,
					JargonException;

	/**
	 * Remove this registered collection from the iRODS catalog. Note that this
	 * method does not delete the physical files, rather it removes them from
	 * the catalog but leavess
	 * <p>
	 * This method is analagous to calling the irm icommand with the -U flag.
	 * Please see: https://www.irods.org/index.php/irm
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the iRODS
	 *            collection
	 * @param recursive
	 *            {@code boolean} that indicates that the unregister
	 *            operation should descend child directories
	 * @return {@code boolean} that will be {@code true} if the
	 *         unregister operation was successful.
	 * @throws CollectionNotEmptyException
	 *             if the collection is not empty, and recursion is not
	 *             specified
	 * @throws JargonException
	 */
	boolean unregisterCollection(String irodsAbsolutePath, boolean recursive)
			throws CollectionNotEmptyException, JargonException;

}
