package org.irods.jargon.userprofile;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to query and maintain user profile information.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface UserProfileService {

	public static final String AVU_UNIT_NAMESPACE = "irods:UserProfileServicePublicAttribute";

	/**
	 * Add a profile for the given user.
	 * 
	 * @param irodsUserName
	 *            <code>String</code> with the name of the iRODS user in the
	 *            given zone for which a profile will be added
	 * @param userProfile
	 *            {@link UserProfile} that will be added
	 * @throws UserProfileValidationException
	 *             if the data in the profile is invalid
	 * @throws DuplicateDataException
	 *             if the profile already exists
	 * @throws JargonException
	 *             general exception
	 */
	void addProfileForUser(final String irodsUserName,
			final UserProfile userProfile)
			throws UserProfileValidationException, DuplicateDataException,
			JargonException;

	/**
	 * Get the configuration for the user profile service
	 * 
	 * @return {@link UserProfileServiceConfiguration}
	 */
	UserProfileServiceConfiguration getUserProfileServiceConfiguration();

	/**
	 * Set the configuration for the user profile service
	 * 
	 * @param userProfileServiceConfiguration
	 *            {@link UserProfileServiceConfiguration}
	 */
	void setUserProfileServiceConfiguration(
			UserProfileServiceConfiguration userProfileServiceConfiguration);

	/**
	 * Remove the public and protected user profile information
	 * 
	 * @param irodsUserName
	 *            <code>String</code> with the name of the iRODS user in the
	 *            given zone for which a profile will be removed
	 * @throws JargonException
	 */
	void removeProfileInformation(String irodsUserName) throws JargonException;

	/**
	 * Given an iRODS user name, retrive the user profile if it exists
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name
	 * @return {@link UserProfile} with available information (may depend on
	 *         ACL's)
	 * @throws DataNotFoundException
	 *             if the user profile information does not exist
	 * @throws JargonException
	 */
	UserProfile retrieveUserProfile(String userName)
			throws DataNotFoundException, JargonException;

	/**
	 * Return the calculated path to the user profile directory based on the
	 * user name and the {@link UserProfileServiceConfiguration} information.
	 * 
	 * @param userName
	 *            <code>String</code> with the iRODS user name for which the
	 *            profile information is kept
	 * @return <code>String</code> with the absolute path to the iRODS
	 *         collection where user profile information is kept.
	 */
	String getUserProfileDir(String userName);

	/**
	 * Update the user profile information for the given user
	 * 
	 * @param userProfile
	 *            {@link UserProfile} containing the desired state of the user's
	 *            information
	 * @throws JargonException
	 */
	void updateUserProfile(UserProfile userProfile) throws JargonException;

}