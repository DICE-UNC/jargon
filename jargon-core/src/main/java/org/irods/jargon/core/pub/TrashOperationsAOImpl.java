/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of trash operations
 * 
 * @author conwaymc
 *
 */
public class TrashOperationsAOImpl extends IRODSGenericAO {

	public static final Logger log = LoggerFactory.getLogger(TrashOperationsAOImpl.class);

	/**
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 */
	public TrashOperationsAOImpl(IRODSSession irodsSession, IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/**
	 * Empty the trash can for the logged in user, with an optional (blank or null)
	 * zone. This defaults to a recursive operation to remove all trash
	 * 
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @throws JargonException
	 */
	public void emptyTrash(final String irodsZone) throws JargonException {
		log.info("emptyTrash()");
		String operativeZone = irodsZone;
		if (operativeZone == null || operativeZone.isEmpty()) {
			operativeZone = this.getIRODSAccount().getZone();
		}
		log.info("operativeZone:{}", operativeZone);

		emptyTrash(operativeZone, MiscIRODSUtils.buildTrashHome(this.getIRODSAccount().getUserName(), operativeZone),
				true);

	}

	/**
	 * Empty the trash can for the logged in user, with an optional (blank or null)
	 * zone. This defaults to a recursive operation to remove all trash.
	 * <p/>
	 * The caller must properly format the username and zone name appropriately.
	 * 
	 * @param userName
	 *            <code>String</code> that will have trash emptied
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone final TrashOptions trashOptions including recursive
	 *            and delete orphan collections
	 * @throws JargonException
	 */
	public void emptyTrashAdminMode(final String userName, final String irodsZone) throws JargonException {

	}

	/**
	 * Empty the trash can for the logged in user with the given iRODS absolute
	 * path, allowing the setting of options.
	 * 
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param irodsPath
	 *            <code>String</code> with the iRODS absolute path to a collection
	 *            or data object to remove.
	 * @param recursive
	 *            {@code boolean} that indicates the operation is recursive
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	public void emptyTrash(final String irodsZone, final String irodsPath, final boolean recursive)
			throws FileNotFoundException, JargonException {

		log.info("emptTrash()");
		String operativeZone = irodsZone;
		if (operativeZone == null || operativeZone.isEmpty()) {
			operativeZone = this.getIRODSAccount().getZone();
		}

		String operativePath = irodsPath;
		if (irodsPath == null || irodsPath.isEmpty()) {
			operativePath = MiscIRODSUtils.buildTrashHome(this.getIRODSAccount().getUserName(), operativeZone);
		}

		log.info("operativeZone:{}", operativeZone);
		log.info("operativePath:{}", operativePath);
		log.info("recursive?:{}", recursive);

		boolean remote = false;
		if (!operativeZone.equals(this.getIRODSAccount().getZone())) {
			remote = true;
			log.info("treating as remote");
		}

		log.debug("getting objStat on trash path");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(operativePath);

		log.debug("objStat:{}", objStat);

		if (objStat.isSomeTypeOfCollection()) {
			IRODS

		} else {

		}

	}

	/**
	 * Empty the trash can for the logged in user with the given iRODS absolute
	 * path, allowing the setting of options.
	 * 
	 * @param userName
	 *            <code>String</code> that will have trash emptied. Note that
	 *            federated zone user identities must be provided, so a cross zone
	 *            user would be user#zone
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param irodsPath
	 *            <code>String</code> with the iRODS absolute path to a collection
	 *            or data object to remove.
	 * @param recursive
	 *            {@code boolean} that indicates the operation is recursive
	 * @throws JargonException
	 */
	public void emptyTrashAdminMode(final String userName, final String irodsZone, final String irodsPath,
			final boolean recursive) throws JargonException {

	}

	protected String determineUserName(final String userName, final String zoneName) {
		if (userName.contains("#")) {

		}
	}

}
