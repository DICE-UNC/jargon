/**
 * 
 */
package org.irods.jargon.userprofile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
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
public class UserProfileServiceImpl extends AbstractJargonService implements
		UserProfileService {

	public static final Logger log = LoggerFactory
			.getLogger(UserProfileServiceImpl.class);

	private UserProfileServiceConfiguration userProfileServiceConfiguration = new UserProfileServiceConfiguration();
	private final DataObjectAO dataObjectAO;

	/**
	 * Constructs a user profile service with references to objects necessary to
	 * construct jargon access objects and i/o objects
	 * 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public UserProfileServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		try {
			dataObjectAO = irodsAccessObjectFactory
					.getDataObjectAO(getIrodsAccount());
		} catch (JargonException e) {
			log.error("cannot create dataObjectAO", e);
			throw new JargonRuntimeException(
					"error creating DataObjectAO reference", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.userprofile.UserProfileService#updateUserProfile(org
	 * .irods.jargon.userprofile.UserProfile)
	 */
	@Override
	public void updateUserProfile(final UserProfile userProfile)
			throws JargonException {

		log.info("updateUserProfile()");
		if (userProfile == null) {
			throw new IllegalArgumentException("null userProfile");
		}
		log.info("user profile:{}", userProfile);

		// TODO do some sort of versioned back-up
		log.info("remove old...");
		this.removeProfileInformation(userProfile.getUserName());
		log.info("add new...");
		this.addProfileForUser(userProfile.getUserName(), userProfile);
		log.info("profile updated");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.userprofile.UserProfileService#retrieveUserProfile(java
	 * .lang.String)
	 */
	@Override
	public UserProfile retrieveUserProfile(final String userName)
			throws DataNotFoundException,
			JargonException {

		log.info("retrieveUserProfile()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(userName);
		userProfile.setZone(irodsAccount.getZone());

		log.info("retreiving the public profile");

		IRODSFile publicProfileFile = retrieveUserPublicProfileFile(userName);
		
		if (!publicProfileFile.exists()) {
			log.warn("no public profile found at:{}",
					publicProfileFile.getAbsolutePath());
			throw new DataNotFoundException("no public profile found");
		}

		log.info("getting profile AVUs...");

		List<AVUQueryElement> query = new ArrayList<AVUQueryElement>();

		try {
			query.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					AVUQueryOperatorEnum.EQUAL,
					UserProfileService.AVU_UNIT_NAMESPACE));
		} catch (JargonQueryException e) {
			log.error("error building AVU query", e);
			throw new JargonException("error querying for AVUs", e);
		}

		List<MetaDataAndDomainData> metadataValues = dataObjectAO
				.findMetadataValuesForDataObject(publicProfileFile
						.getAbsolutePath());

		for (MetaDataAndDomainData metadata : metadataValues) {
			log.info("metadata value:{}", metadata);
			
			if (metadata.getAvuAttribute().equals(UserProfileConstants.DESCRIPTION)) {
				userProfile.getUserProfilePublicFields().setDescription(metadata.getAvuValue());
				continue;
			}
			
			if (metadata.getAvuAttribute().equals(
					UserProfileConstants.NICK_NAME)) {
				userProfile.getUserProfilePublicFields().setNickName(metadata.getAvuValue());
				continue;
			}
			
			if (metadata.getAvuAttribute().equals(UserProfileConstants.ZONE)) {
				userProfile.setZone(metadata.getAvuValue());
				continue;
			}

			/*
			 * right now, quietly log and ignore property that is not
			 * anticipated
			 */
			log.warn("property not recognized: {}", metadata);
			
		}

		log.info("look for protected profile file info...");
		String userHomeDir = getUserProfileDir(userName);

		IRODSFile protectedProfileFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getProtectedProfileFileName());

		// tolerate no protected profile info
		if (!protectedProfileFile.exists()) {
			log.warn(
					"no protected profile info, ignore and return public info:{}",
					userProfile);
			return userProfile;
		}

		InputStream userProfileInputStream = new BufferedInputStream(this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFileInputStream(protectedProfileFile));
		Properties protectedProperties = new Properties();
		try {
			protectedProperties.load(userProfileInputStream);
		} catch (IOException e) {
			log.error("error loading protected properties from stream:{}",
					userProfileInputStream, e);
			throw new JargonException("error loading protected properties", e);
		} finally {
			try {
				userProfileInputStream.close();
			} catch (Exception e) {
			}
		}
		
		if (protectedProperties.get(UserProfileConstants.EMAIL) != null) {
			userProfile.getUserProfileProtectedFields().setMail(
					(String) protectedProperties
							.get(UserProfileConstants.EMAIL));
		}
		
		log.info("completed user profile:{}", userProfile);
		return userProfile;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.userprofile.UserProfileService#removeProfileInformation
	 * (java.lang.String)
	 */
	@Override
	public void removeProfileInformation(final String irodsUserName)
			throws JargonException {
		log.info("removeProfileInformation()");

		if (irodsUserName == null || irodsUserName.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsUserName");
		}

		log.info("irodsUserName:{}", irodsUserName);

		String userHomeDir = getUserProfileDir(irodsUserName);

		log.info("user home dir:{}", userHomeDir);

		IRODSFile userProfileFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getPublicProfileFileName());

		// delete the actual public profile file and associated AVU's
		deletePublicProfile(irodsUserName, userProfileFile);

		IRODSFile protectedProfileFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getProtectedProfileFileName());

		log.info("deleting the protected profile file:{}",
				protectedProfileFile.getAbsolutePath());

		protectedProfileFile.deleteWithForceOption();
		log.info("delete completed");

	}

	/**
	 * @param irodsUserName
	 * @param userProfileFile
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	private void deletePublicProfile(final String irodsUserName,
			final IRODSFile userProfileFile) throws JargonException,
			DataNotFoundException {
		if (!userProfileFile.exists()) {
			log.info(
					"user profile file does not exist, just ignore and proceed:{}",
					irodsUserName);
		} else {
			log.info("delete all avu's associated with profile",
					userProfileFile.getAbsolutePath());
			List<AVUQueryElement> queryList = new ArrayList<AVUQueryElement>();
			try {
				queryList.add(AVUQueryElement.instanceForValueQuery(
						AVUQueryPart.UNITS, AVUQueryOperatorEnum.EQUAL,
						AVU_UNIT_NAMESPACE));
				List<MetaDataAndDomainData> metadataList = dataObjectAO
						.findMetadataValuesByMetadataQuery(queryList);
				AvuData avuData;

				for (MetaDataAndDomainData metadataAndDomainData : metadataList) {
					avuData = AvuData.instance(
							metadataAndDomainData.getAvuAttribute(),
							metadataAndDomainData.getAvuValue(),
							metadataAndDomainData.getAvuUnit());
					dataObjectAO.deleteAVUMetadata(
							userProfileFile.getAbsolutePath(), avuData);
				}

				log.info("avus were deleted..now delete the public profile file");
				userProfileFile.deleteWithForceOption();

			} catch (JargonQueryException e) {
				log.info("Jargon query exeception querying for AVU metadata", e);
				throw new JargonException(e);
			}
		}
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
		String userHomeDir = getUserProfileDir(irodsUserName);
		log.info("looking for profile in userHomeDir:{}", userHomeDir);

		IRODSFile userProfileFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getPublicProfileFileName());

		if (userProfileFile.exists()) {
			log.error("cannot add, user profile already exists for user:{}",
					irodsUserName);
			throw new DuplicateDataException("user profile already exists");
		}

		log.info("creating the public profile");

		try {
			userProfileFile.createNewFile();
			// public can read the profile
			// if so indicated in the config, give a special group write access
			// to the public profile
			if (userProfileServiceConfiguration
					.getProtectedProfileReadWriteGroup() == null
					|| userProfileServiceConfiguration
							.getProtectedProfileReadWriteGroup().isEmpty()) {
				log.info("no permissions set for protected profile group in user profile service config");
			} else {
				if (userProfileServiceConfiguration
						.isProtectedProfileGroupHasWriteAccessToPublic()) {
					log.info("adding WRITE acl to public profile");
					dataObjectAO.setAccessPermissionWrite(irodsAccount
							.getZone(), userProfileFile.getAbsolutePath(),
							userProfileServiceConfiguration
									.getProtectedProfileReadWriteGroup());
					log.info("adding public read access");
					dataObjectAO.setAccessPermissionRead(
							irodsAccount.getZone(),
							userProfileFile.getAbsolutePath(), "public");
				}
			}

		} catch (IOException e) {
			log.error("unable to create user profile file:{}", userProfileFile);
			throw new JargonException("error creating user profile file", e);
		}

		log.info("creating the protected profile file");

		IRODSFile protectedProfileFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getProtectedProfileFileName());

		log.info("creating the protected profile file:{}",
				protectedProfileFile.getAbsolutePath());

		if (protectedProfileFile.exists()) {
			log.error(
					"cannot add, protected user profile already exists for user:{}",
					irodsUserName);
			throw new DuplicateDataException(
					"protected user profile already exists");
		}

		log.info("creating the protected profile");

		try {
			protectedProfileFile.createNewFile();
		} catch (IOException e) {
			log.error("unable to create protected user profile file:{}",
					userProfileFile);
			throw new JargonException(
					"error creating protected user profile file", e);
		}

		// if a user or group is specified as having read/write access to the
		// profile, do an acl to that user

		if (userProfileServiceConfiguration.getProtectedProfileReadWriteGroup() == null
				|| userProfileServiceConfiguration
						.getProtectedProfileReadWriteGroup().isEmpty()) {
			log.info("no permissions set for protected profile group in user profile service config");
		} else {
			log.info("adding WRITE acl to protected profile");
			dataObjectAO.setAccessPermissionWrite(irodsAccount.getZone(),
					protectedProfileFile.getAbsolutePath(),
					userProfileServiceConfiguration
							.getProtectedProfileReadWriteGroup());
		}

		// provision the public profile with the given profile information by
		// hanging AVU off of the file

		UserProfilePublicFields userProfilePublicFields = userProfile
				.getUserProfilePublicFields();

		addAVUIfDataPresent(userProfileFile.getAbsolutePath(),
				UserProfileConstants.ZONE, userProfile.getZone());

		addAVUIfDataPresent(userProfileFile.getAbsolutePath(),
				UserProfileConstants.USER_NAME,
 userProfile.getUserName());

		addAVUIfDataPresent(userProfileFile.getAbsolutePath(),
				UserProfileConstants.DESCRIPTION,
				userProfilePublicFields.getDescription());

		addAVUIfDataPresent(userProfileFile.getAbsolutePath(),
				UserProfileConstants.NICK_NAME,
				userProfilePublicFields.getNickName());

		// provision the protected profile information, first make into a
		// Properties object

		Properties protectedProperties = new Properties();
		if (!userProfile.getUserProfileProtectedFields().getMail().isEmpty()) {
			protectedProperties.put(UserProfileConstants.EMAIL, userProfile
					.getUserProfileProtectedFields().getMail());

		}

		log.info("protected properties before serialization:{}",
				protectedProperties);

		OutputStream protectedPropertiesOutputStream = new BufferedOutputStream(
				this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
						.instanceIRODSFileOutputStream(protectedProfileFile));

		log.info("output stream created, store to properties file");

		try {
			protectedProperties.store(protectedPropertiesOutputStream,
					"saved protected properties");
			protectedPropertiesOutputStream.close();
			log.info("properties stored and stream closed");
		} catch (IOException e) {
			log.error("io exeption storing properties file", e);
			throw new JargonException(
					"error serializing protected properties to output stream",
					e);
		}

	}

	/**
	 * Add the AVU data based on the provided parameters. Note that if the value
	 * of the AVU is blank, it won't be added. Only set fields are preserved
	 * 
	 * @param irodsDataObjectAbsolutePath
	 * @param proposedAttribute
	 * @param proposedValue
	 * @throws JargonException
	 */
	private void addAVUIfDataPresent(final String irodsDataObjectAbsolutePath,
			final String proposedAttribute, final String proposedValue)
			throws JargonException {

		log.info("addAVUIfDataPresent()");

		if (proposedAttribute == null || proposedAttribute.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty proposedAttribute");
		}

		if (proposedValue == null) {
			throw new IllegalArgumentException("null proposedValue");
		}

		if (proposedValue.isEmpty()) {
			log.info("ignore avu, value is blank");
			return;
		}

		// valid data if I got here, add the AVU

		AvuData avuData;
		avuData = AvuData.instance(proposedAttribute, proposedValue,
				AVU_UNIT_NAMESPACE);
		dataObjectAO.addAVUMetadata(irodsDataObjectAbsolutePath, avuData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.userprofile.UserProfileService#
	 * getUserProfileServiceConfiguration()
	 */
	@Override
	public UserProfileServiceConfiguration getUserProfileServiceConfiguration() {
		return userProfileServiceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.userprofile.UserProfileService#
	 * setUserProfileServiceConfiguration
	 * (org.irods.jargon.userprofile.UserProfileServiceConfiguration)
	 */
	@Override
	public void setUserProfileServiceConfiguration(
			final UserProfileServiceConfiguration userProfileServiceConfiguration) {
		this.userProfileServiceConfiguration = userProfileServiceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.userprofile.UserProfileService#getUserProfileDir(java
	 * .lang.String)
	 */
	@Override
	public String getUserProfileDir(final String userName) {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		String userHomeDir = MiscIRODSUtils
				.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
						getIrodsAccount(), userName);

		log.info("user home dir:{}", userHomeDir);

		if (!userProfileServiceConfiguration.getProfileSubdirName().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(userHomeDir);
			sb.append("/");
			sb.append(userProfileServiceConfiguration.getProfileSubdirName());
			userHomeDir = sb.toString();
		}

		return userHomeDir;
	}

	/**
	 * Get the file that corresponds to the expected public profile. Note that
	 * this method does not evaluate whether the file actually exists
	 * 
	 * @param userName
	 * @return
	 * @throws JargonException
	 */
	private IRODSFile retrieveUserPublicProfileFile(final String userName)
			throws JargonException {

		String userHomeDir = getUserProfileDir(userName);

		return this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeDir,
						userProfileServiceConfiguration
								.getPublicProfileFileName());
	}

}
