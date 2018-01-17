/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.IOException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.CollInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.CollInpForEmptyTrash.TrashOperationMode;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of trash operations
 * 
 * @author conwaymc
 *
 */
public class TrashOperationsAOImpl extends IRODSGenericAO implements TrashOperationsAO {

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

	@Override
	public IRODSFile getTrashHomeForLoggedInUser() throws JargonException {
		log.info("getTrashHomeForLoggedInUser())");
		log.info("for user:{}", this.getIRODSAccount());
		String trashHomePath = MiscIRODSUtils.buildTrashHome(getIRODSAccount().getUserName(),
				getIRODSAccount().getZone());
		log.info("getting file at:{}", trashHomePath);
		IRODSFile trashFile = this.getIRODSAccessObjectFactory().getIRODSFileFactory(getIRODSAccount())
				.instanceIRODSFile(trashHomePath);
		return trashFile;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.TrashOperationsAO#emptyTrashForLoggedInUser(java.
	 * lang.String, int)
	 */
	@Override
	public void emptyTrashForLoggedInUser(final String irodsZone, final int age) throws JargonException {
		log.info("emptyTrash()");
		String operativeZone = irodsZone;
		if (operativeZone == null || operativeZone.isEmpty()) {
			operativeZone = this.getIRODSAccount().getZone();
		}

		TrashOptions trashOptions = new TrashOptions();
		trashOptions.setAgeInMinutes(age);
		trashOptions.setRecursive(true);
		trashOptions.setTrashOperationMode(TrashOperationMode.USER);
		log.info("operativeZone:{}", operativeZone);
		log.info("trashOptions:{}", trashOptions);

		emptyTrash(operativeZone, MiscIRODSUtils.buildTrashHome(this.getIRODSAccount().getUserName(), operativeZone),
				trashOptions);

	}

	/**
	 * Empty the trash can for the logged in user, with an optional (blank or null)
	 * zone. This defaults to a recursive operation to remove all trash.
	 * <p/>
	 * The caller must properly format the username and zone name appropriately.
	 * 
	 * @param userName
	 *            <code>String</code> that will have trash emptied. If left null or
	 *            blank, will delete trash for all users
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone final TrashOptions trashOptions including recursive
	 *            and delete orphan collections
	 * @param trashOptions
	 *            {@link TrashOptions} that control details of the processing
	 * @throws JargonException
	 */
	public void emptyTrashAdminMode(final String userName, final String irodsZone, final TrashOptions trashOptions)
			throws JargonException {

	}

	/**
	 * Empty the trash can for the logged in user with the given iRODS absolute
	 * path, allowing the setting of options. This method expects proper resolution
	 * of all of the parameters, and these parameters are set by the varous public
	 * method signatures
	 * 
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param irodsPath
	 *            <code>String</code> with the iRODS absolute path to a collection
	 *            or data object to remove. At this point the path should be
	 *            properly resolved for the various operations, such as user home or
	 *            orphan files
	 * @param trashOptions
	 *            {@link TrashOptions} that control details of the processing
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	private void emptyTrash(final String irodsZone, final String irodsPath, final TrashOptions trashOptions)
			throws FileNotFoundException, JargonException {

		log.info("emptyTrash()");
		String operativeZone = irodsZone;
		if (operativeZone == null || operativeZone.isEmpty()) {
			operativeZone = this.getIRODSAccount().getZone();
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			throw new IllegalArgumentException("null irodsPath");
		}

		if (trashOptions == null) {
			throw new IllegalArgumentException("null trashOptions");
		}

		log.info("trashOptions:{}", trashOptions);
		log.info("operativeZone:{}", operativeZone);
		log.info("irodsPath:{}", irodsPath);

		if (!operativeZone.equals(this.getIRODSAccount().getZone())) {
			operativeZone = "";
		}

		log.debug("getting objStat on trash path");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(irodsPath);

		log.debug("objStat:{}", objStat);

		if (objStat.isSomeTypeOfCollection()) {
			log.info("deleting as a collection");
			CollInpForEmptyTrash collInp = new CollInpForEmptyTrash(trashOptions, irodsPath, operativeZone);
			Tag response = getIRODSProtocol().irodsFunction(IRODSConstants.RODS_API_REQ, collInp.getParsedTags(),
					CollInp.RMDIR_API_NBR);
			processClientStatusMessages(response);
			log.info("deletion successful");
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
	 * @param trashOptions
	 *            {@link TrashOptions} that control details of the processing
	 * @throws JargonException
	 */
	public void emptyTrashAdminMode(final String userName, final String irodsZone, final String irodsPath,
			final TrashOptions trashOptions) throws JargonException {

	}

	/**
	 * Respond to client status messages for an operation until exhausted.
	 *
	 * @param reply
	 *            {@code Tag} containing status messages from IRODS
	 * @throws IOException
	 */
	private void processClientStatusMessages(final Tag reply) throws JargonException {

		boolean done = false;
		Tag ackResult = reply;

		while (!done) {
			if (ackResult.getLength() > 0) {
				if (ackResult.getName().equals(IRODSConstants.CollOprStat_PI)) {
					// formulate an answer status reply

					// if the total file count is 0, then I will continue and
					// send
					// the coll stat reply, otherwise, just ignore and
					// don't send the reply.

					// int fileCount = Integer.parseInt((String)
					// fileCountTag.getValue());
					Tag msgHeaderTag = ackResult.getTag("MsgHeader_PI");
					;
					if (msgHeaderTag == null) {
						done = true;
					} else {
						getIRODSProtocol().sendInNetworkOrderWithFlush(IRODSConstants.SYS_CLI_TO_SVR_COLL_STAT_REPLY);
						ackResult = getIRODSProtocol().readMessage();
					}
				}
			}
		}

	}

}
