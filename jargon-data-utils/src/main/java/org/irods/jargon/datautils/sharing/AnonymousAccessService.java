package org.irods.jargon.datautils.sharing;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;

/**
 * Interface to a service object that presents handy methods for managing
 * anonymous users and their access
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
@Deprecated

public interface AnonymousAccessService {

	/**
	 * Checks to see whether anonymous access is allowed for the given iRODS
	 * absolute path. This can be either a file or a collection.
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with an absolute path to an iRODS file or
	 *            collection
	 * @return {@code boolean} if anonymous has at least read access
	 * @throws FileNotFoundException
	 *             if the given path does not exist
	 * @throws JargonException
	 */
	boolean isAnonymousAccessSetUp(String irodsAbsolutePath) throws FileNotFoundException, JargonException;

	/**
	 * Get the anonymous user name for use in comparisons and setting. May be
	 * modified by setting a variant.
	 *
	 * @return the anonymousUserName
	 */
	abstract String getAnonymousUserName();

	/**
	 * Set (by injection) the user name to use as 'anonymous'. Defaults to the
	 * setting in {@link IRODSAccount}
	 *
	 * @param anonymousUserName
	 *            the anonymousUserName to set
	 */
	abstract void setAnonymousUserName(String anonymousUserName);

	/**
	 * Given an iRODS absolute path to a file or collection, set the anonymous user
	 * to be able to at least list the collection contents. If the given path
	 * represents a file (data object) set the permissions on the file and set the
	 * containing collection's access to at least read. If the
	 * {@code optionalFilePermissionForParentCollection} is specified, that
	 * permission will be used for the parent collection.
	 *
	 * @param irodsAbsolutePath
	 *            {@code String} with the absolute path to the iRODS collection or
	 *            data object for which the permissions will be set
	 * @param filePermissionForTargetPath
	 *            {@link FilePermissionEnum} that is the permission to set for the
	 *            anonymous account for the given iRODS path
	 * @param optionalFilePermissionForParentCollection
	 *            {@link FilePermissionEnum} that can be set to {@code null}. If
	 *            this is set, and the target of the path is a data object, the
	 *            parent collection will be set to this value. If no such permission
	 *            is provided, and the target is a data object, the parent
	 *            collection will have read permissions set for the anonymous user
	 * @throws FileNotFoundException
	 *             if the give iRODS absolute path does not exist
	 * @throws JargonException
	 */
	void permitAnonymousToFileOrCollectionSettingCollectionAndDataObjectProperties(String irodsAbsolutePath,
			FilePermissionEnum filePermissionForTargetPath,
			FilePermissionEnum optionalFilePermissionForParentCollection) throws FileNotFoundException, JargonException;

}