/**
 *
 */
package org.irods.jargon.datautils.uploads;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation to help manage an 'uploads' directory, used by
 * convention in multiple interfaces as a generic location to upload data.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UploadsServiceImpl extends AbstractJargonService implements
UploadsService {

	public static final Logger log = LoggerFactory
			.getLogger(UploadsServiceImpl.class);

	/**
	 * Constructor with information needed to connect to iRODS
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public UploadsServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.uploads.UploadsService#getUploadsDirectory()
	 */
	@Override
	public IRODSFile getUploadsDirectory() throws JargonException {
		log.info("getUploadsDirectory()");
		String homeDirectory = getUploadsDirName();
		log.info("home directory is set to:{}", homeDirectory);
		IRODSFile homeDirFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						homeDirectory);
		log.info("making uploads directory if it does not exist");
		if (!homeDirFile.exists()) {
			homeDirFile.mkdirs();
		}
		return homeDirFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.uploads.UploadsService#deleteUploadsDirectory
	 * ()
	 */
	@Override
	public void deleteUploadsDirectory() throws JargonException {
		log.info("deleteUploadsDirectory()");
		String homeDirectory = getUploadsDirName();
		log.info("home directory is set to:{}", homeDirectory);
		IRODSFile homeDirFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						homeDirectory);
		homeDirFile.deleteWithForceOption();
		log.info("deleted");
	}

	/**
	 * Get the uploads dir name under the user home directory
	 *
	 * @return
	 */
	private String getUploadsDirName() {

		StringBuilder homeDirectory = new StringBuilder(
				MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(getIrodsAccount()));
		homeDirectory.append('/');
		homeDirectory.append(UploadsService.UPLOADS_DIR_DEFAULT_NAME);
		return homeDirectory.toString();
	}

}
