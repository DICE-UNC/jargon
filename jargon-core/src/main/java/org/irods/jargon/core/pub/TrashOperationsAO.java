package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Handle trash operations for both users and admins
 *
 * @author conwaymc
 *
 */
public interface TrashOperationsAO {

	/**
	 * Empty the trash can for the logged in user, with an optional (blank or null)
	 * zone. This defaults to a recursive operation to remove all trash
	 *
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param age
	 *            with a minimum age in minutes, set to 0 or -1 if all files are to
	 *            be deleted
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyTrashForLoggedInUser(String irodsZone, int age) throws JargonException;

	/**
	 * Get a handle to the top level of a user's trash
	 *
	 * @return {@link IRODSFile} that is the top of the logged in user's trash
	 * @throws JargonException
	 *             for an iRODS error
	 */
	IRODSFile getTrashHomeForLoggedInUser() throws JargonException;

	/**
	 * Get a reference to the orphan trash path in the zone
	 *
	 * @param zone
	 *            <code>String</code> with a zone for which the trash will be
	 *            emptied. defaults to the current logged in zone
	 * @return {@link IRODSFile} that is the top of the orphan trash collection
	 * @throws JargonException
	 *             for an iRODS error
	 */
	IRODSFile getOrphanTrashHome(final String zone) throws JargonException;

	/**
	 * Empty the trash can for the provided user, with an optional (blank or null)
	 * zone. This operation is done as an administrator
	 * <p>
	 * The caller must properly format the username and zone name appropriately.
	 *
	 * @param userName
	 *            <code>String</code> that will have trash emptied. If left null or
	 *            blank, will delete trash for all users
	 * @param zone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param age
	 *            {@link int} with a minimum age in minutes, set to 0 or -1 if all
	 *            files are to be deleted
	 *
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyTrashAdminMode(final String userName, final String zone, final int age) throws JargonException;

	/**
	 * Empty the trash can for all users. This operation is done as an administrator
	 *
	 *
	 * @param zone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param age
	 *            {@link int} with a minimum age in minutes, set to 0 or -1 if all
	 *            files are to be deleted
	 *
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyAllTrashAsAdmin(final String zone, final int age) throws JargonException;

	/**
	 * Get the trash home dir for all users
	 *
	 * @param zone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @return {@link IRODSFile} that is the top of the trash for all users in the
	 *         given zone
	 *
	 * @throws JargonException
	 *             for an iRODS error
	 */
	IRODSFile getTrashHome(final String zone) throws JargonException;

	/**
	 * Empty the trash with the given absolute path (data object or collection) as
	 * the logged in user.
	 *
	 * @param irodsPath
	 *            {@link String} absolute path to the trash item
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param age
	 *            {@link int} with a minimum age in minutes, set to 0 or -1 if all
	 *            files are to be deleted
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyTrashAtPathForLoggedInUser(final String irodsPath, final String irodsZone, final int age)
			throws JargonException;

	/**
	 * Empty the trash with the given absolute path (data object or collection) as
	 * the irods admin for the specified user
	 *
	 * @param irodsPath
	 *            {@link String} absolute path to the trash item
	 * @param userName
	 *            <code>String</code> that will have trash emptied.
	 * @param zone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param age
	 *            {@link int} with a minimum age in minutes, set to 0 or -1 if all
	 *            files are to be deleted
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyTrashAtPathAdminMode(String irodsPath, String userName, String zone, int age) throws JargonException;

	/**
	 * Clear out the /zone/trash/orphan collection as admin
	 *
	 * @param zone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @throws JargonException
	 *             for an iRODS error
	 */
	void emptyOrphanTrash(final String zone) throws JargonException;

}