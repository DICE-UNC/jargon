/**
 * 
 */
package org.irods.jargon.datautils.sharing;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy methods to
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AnonymousAccessServiceImpl extends AbstractJargonService implements
		AnonymousAccessService {

	public static final Logger log = LoggerFactory
			.getLogger(AnonymousAccessServiceImpl.class);
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;
	private final DataObjectAO dataObjectAO;
	private final CollectionAO collectionAO;

	/**
	 * User name for anonymous, can be modified by injection to other user name
	 */
	private String anonymousUserName = IRODSAccount.PUBLIC_USERNAME;

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public AnonymousAccessServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);

		try {
			collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
					.getCollectionAndDataObjectListAndSearchAO(
							getIrodsAccount());
			dataObjectAO = getIrodsAccessObjectFactory().getDataObjectAO(
					getIrodsAccount());
			collectionAO = getIrodsAccessObjectFactory().getCollectionAO(
					getIrodsAccount());
		} catch (JargonException e) {
			log.error("error in constructor, throw runtime exception", e);
			throw new JargonRuntimeException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.sharing.AnonymousAccessService#
	 * isAnonymousAccessSetUp(java.lang.String)
	 */
	@Override
	public boolean isAnonymousAccessSetUp(final String irodsAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("isUserHasAccess()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (this.anonymousUserName == null || this.anonymousUserName.isEmpty()) {
			throw new JargonException(
					"anonymous user name is null or empty, and likely improperly set when configuring this service");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		// get an objStat to discriminate between file and collection
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);
		log.info("got objStat:{}", objStat);
		boolean hasPermission = false;

		if (objStat.isSomeTypeOfCollection()) {
			log.info("its a collection");
			hasPermission = collectionAO.isUserHasAccess(irodsAbsolutePath,
					getAnonymousUserName());
		} else {
			log.info("its a data object");
			hasPermission = dataObjectAO.isUserHasAccess(irodsAbsolutePath,
					getAnonymousUserName());
		}

		log.info("has permission? {}", hasPermission);
		return hasPermission;

	}

	@Override
	public void permitAnonymousToFileOrCollectionSettingCollectionAndDataObjectProperties(
			final String irodsAbsolutePath,
			final FilePermissionEnum filePermissionForTargetPath,
			final FilePermissionEnum optionalFilePermissionForParentCollection)
			throws FileNotFoundException, JargonException {

		log.info("permitAnonymousToFileOrCollectionSettingCollectionAndDataObjectProperties()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (filePermissionForTargetPath == null) {
			throw new IllegalArgumentException(
					"null filePermissionForTargetPath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("filePermissionForTargetPath:{}", filePermissionForTargetPath);
		if (log.isInfoEnabled()) {
			if (optionalFilePermissionForParentCollection == null) {
				log.info("optionalFilePermissionForParentCollection is null");
			} else {
				log.info("optionalFilePermissionForParentCollection:{}",
						optionalFilePermissionForParentCollection);
			}
		}

		// FIXME: this needs refactoring and tests built after the demo!

		// get the objStat for the target path
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);
		log.info("got objStat:{}", objStat);

		if (objStat.isSomeTypeOfCollection()) {
			log.info("is a collection");
			// if there is not access to the collection, set it to the provided
			// file permission
			UserFilePermission userFilePermission = collectionAO
					.getPermissionForUserName(irodsAbsolutePath,
							getAnonymousUserName());
			if (userFilePermission == null
					|| userFilePermission.getFilePermissionEnum()
					.getPermissionNumericValue() < filePermissionForTargetPath
					.getPermissionNumericValue()) {
				log.info("replace permission value with higher given value");

				if (filePermissionForTargetPath == FilePermissionEnum.NONE
						|| filePermissionForTargetPath == FilePermissionEnum.NULL) {
					throw new IllegalArgumentException(
							"cannot set permission to null or none here");
				} else if (filePermissionForTargetPath == FilePermissionEnum.READ) {
					collectionAO.setAccessPermissionRead(
							irodsAccount.getZone(), objStat.getAbsolutePath(),
							this.getAnonymousUserName(), true);
				} else if (filePermissionForTargetPath == FilePermissionEnum.WRITE) {
					collectionAO.setAccessPermissionWrite(
							irodsAccount.getZone(), objStat.getAbsolutePath(),
							this.getAnonymousUserName(), true);
				} else if (filePermissionForTargetPath == FilePermissionEnum.OWN) {
					collectionAO.setAccessPermissionOwn(irodsAccount.getZone(),
							objStat.getAbsolutePath(),
							this.getAnonymousUserName(), true);
				}
			}

		} else {
			log.info("is a data object");

			// if no permission to the data object, set to the provided
			UserFilePermission userFilePermission = dataObjectAO
					.getPermissionForDataObjectForUserName(irodsAbsolutePath,
							getAnonymousUserName());
			if (userFilePermission == null
					|| userFilePermission.getFilePermissionEnum()
					.getPermissionNumericValue() < filePermissionForTargetPath
					.getPermissionNumericValue()) {
				log.info("replace permission value with higher given value");

				if (filePermissionForTargetPath == FilePermissionEnum.NONE
						|| filePermissionForTargetPath == FilePermissionEnum.NULL) {
					throw new IllegalArgumentException(
							"cannot set permission to null or none here");
				} else if (filePermissionForTargetPath == FilePermissionEnum.READ) {
					dataObjectAO.setAccessPermissionRead(
							irodsAccount.getZone(), objStat.getAbsolutePath(),
							this.getAnonymousUserName());
				} else if (filePermissionForTargetPath == FilePermissionEnum.WRITE) {
					dataObjectAO.setAccessPermissionWrite(
							irodsAccount.getZone(), objStat.getAbsolutePath(),
							this.getAnonymousUserName());
				} else if (filePermissionForTargetPath == FilePermissionEnum.OWN) {
					dataObjectAO.setAccessPermissionOwn(irodsAccount.getZone(),
							objStat.getAbsolutePath(),
							this.getAnonymousUserName());
				}

				// if optional permission for target not specified, make it
				// read, if
				// specified set the permission to that specified

				FilePermissionEnum operativeFilePermission = FilePermissionEnum.READ;
				if (optionalFilePermissionForParentCollection != null) {
					if (filePermissionForTargetPath == FilePermissionEnum.NONE
							|| filePermissionForTargetPath == FilePermissionEnum.NULL) {
						throw new IllegalArgumentException(
								"cannot set permission to null or none here");
					} else {
						operativeFilePermission = optionalFilePermissionForParentCollection;
					}
				}

				log.info("file permission for parent collection set to:{}",
						operativeFilePermission);
				IRODSFile dataObjectFile = this.getIrodsAccessObjectFactory()
						.getIRODSFileFactory(getIrodsAccount())
						.instanceIRODSFile(objStat.getAbsolutePath());
				String parentPath = dataObjectFile.getParent();

				if (operativeFilePermission == FilePermissionEnum.READ) {
					dataObjectAO.setAccessPermissionRead(
							irodsAccount.getZone(), parentPath,
							this.getAnonymousUserName());
				} else if (operativeFilePermission == FilePermissionEnum.WRITE) {
					dataObjectAO.setAccessPermissionWrite(
							irodsAccount.getZone(), parentPath,
							this.getAnonymousUserName());
				} else if (operativeFilePermission == FilePermissionEnum.OWN) {
					dataObjectAO.setAccessPermissionOwn(irodsAccount.getZone(),
							parentPath, this.getAnonymousUserName());
				}
			}
		}

		log.info("permissions now set on collection");

	}

	/**
	 * Get the anonymous user name for use in comparisons and setting. May be
	 * modified by setting a variant.
	 * 
	 * @return the anonymousUserName
	 */
	@Override
	public String getAnonymousUserName() {
		return anonymousUserName;
	}

	/**
	 * Set (by injection) the user name to use as 'anonymous'. Defaults to the
	 * setting in {@link IRODSAccount}
	 * 
	 * @param anonymousUserName
	 *            the anonymousUserName to set
	 */
	@Override
	public void setAnonymousUserName(final String anonymousUserName) {
		this.anonymousUserName = anonymousUserName;
	}

}
