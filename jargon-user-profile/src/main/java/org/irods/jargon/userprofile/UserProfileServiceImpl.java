/**
 * 
 */
package org.irods.jargon.userprofile;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the storage and retrieval of <code>UserProfile</code> information in
 * iRODS.
 * <p/>
 * This implementation uses a convention of a .profile file in the user home dir
 * with the values attached as AVU's, and a .protected file in the user home dir
 * with the values in the file as properties.
 * <p/>
 * This scheme is necessary as AVU's are publicly query-able. Having some data
 * in AVU's will help in that they are more easily queried.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UserProfileServiceImpl extends AbstractJargonService implements UserProfileService {

	public static final Logger log = LoggerFactory
			.getLogger(UserProfileServiceImpl.class);

	public static final String PUBLIC_PROFILE_FILE_NAME = ".profile";

	/**
	 * Constructs a user profile service with references to objects necessary to
	 * construct jargon access objects and i/o objects
	 * 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public UserProfileServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.userprofile.UserProfileService#addProfileForUser(java
	 * .lang.String, org.irods.jargon.userprofile.UserProfile)
	 */
	@Override
	public void addProfileForUser(final String irodsUserName,
			final UserProfile userProfile)
			throws UserProfileValidationException, DuplicateDataException,
			JargonException {

		log.info("addProfileForUser()");

		if (irodsUserName == null || irodsUserName.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsUserName");
		}

		if (userProfile == null) {
			throw new IllegalArgumentException("null userProfile");
		}

		log.info("userName:{}", irodsUserName);
		log.info("UserProfile:{}", userProfile);

		// see if a user profile already exists for this user
		String userHomeDir = MiscIRODSUtils.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(getIrodsAccount(), irodsUserName);
		log.info("looking for profile in userHomeDir:{}", userHomeDir);
		
		IRODSFile userProfileFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(userHomeDir, PUBLIC_PROFILE_FILE_NAME);

		if (userProfileFile.exists()) {
			log.error("cannot add, user profile already exists for user:{}",
					irodsUserName);
			throw new DuplicateDataException("user profile already exists");
		}

	}


}
