/**
 * 
 */
package org.irods.jargon.spring.security;

import java.net.UnknownHostException;
import java.util.List;

import org.irods.jargon.core.connection.IRODSManagedConnection;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * iRODS specific authentication provider
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAuthenticationProvider implements AuthenticationProvider {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private IRODSProtocolManager irodsProtocolManager = null;
	private IRODSAccessObjectFactory irodsAccessObjectFactory = null;

	/**
	 * 
	 */
	public IRODSAuthenticationProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException {
		if (log.isInfoEnabled()) {
			log.info("authenticating:" + authentication);
		}

		if (authentication == null) {
			log.error("the authentication passed to the method is null");
			throw new BadCredentialsException("null authentication");
		}

		if (!(authentication instanceof IRODSAuthenticationToken)) {
			String msg = "the authentication token passed in is not an instance of IRODSAuthenticationToken";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (irodsProtocolManager == null) {
			String msg = "the irodsProtocolManager is null.";
			log.error(msg);
			throw new AuthenticationServiceException(msg);
		}

		if (irodsAccessObjectFactory == null) {
			throw new AuthenticationServiceException(
					"irods access object factory is null");
		}

		IRODSAuthenticationToken irodsAuthToken = (IRODSAuthenticationToken) authentication;

		log.debug("doing authentication call to irods for account: {}",
				irodsAuthToken.getIrodsAccount());

		IRODSManagedConnection connection = null;
		try {
			connection = irodsProtocolManager.getIRODSProtocol(irodsAuthToken
					.getIrodsAccount(), irodsAccessObjectFactory.getIrodsSession().buildPipelineConfigurationBasedOnJargonProperties());
			irodsProtocolManager.returnIRODSConnection(connection);
		} catch (JargonException e) {
			log.error("unable to authenticate, JargonException", e);
			e.printStackTrace();

			if (e.getCause() == null) {
				if (e.getMessage().indexOf("-826000") > -1) {
					log.warn("invalid user/password");
					irodsProtocolManager
							.returnConnectionWithIoException(connection);
					throw new BadCredentialsException(
							"Unknown user id/password");
				} else {
					log.error("authentication service exception", e);
					irodsProtocolManager
							.returnConnectionWithIoException(connection);
					throw new AuthenticationServiceException(
							"unable to authenticate", e);
				}
			} else if (e.getCause() instanceof UnknownHostException) {
				log.warn("cause is invalid host");
				irodsProtocolManager
						.returnConnectionWithIoException(connection);
				throw new BadCredentialsException("The host is unknown");
			} else if (e.getCause().getMessage().indexOf("refused") > -1) {
				log.error("cause is refused or invalid port");
				irodsProtocolManager
						.returnConnectionWithIoException(connection);
				throw new BadCredentialsException(
						"The host/port is unknown or refusing connection");
			} else {
				log.error("authentication service exception", e);
				irodsProtocolManager
						.returnConnectionWithIoException(connection);
				throw new AuthenticationServiceException(
						"unable to authenticate", e);
			}
		} finally {

		}

		log.info("authenticated");
		authentication.setAuthenticated(true);

		// get roles
		log.info("getting role information for {}", irodsAuthToken
				.getIrodsAccount().toString());
		try {
			UserGroupAO userGroupAO = irodsAccessObjectFactory
					.getUserGroupAO(irodsAuthToken.getIrodsAccount());
			List<UserGroup> userGroups = userGroupAO
					.findUserGroupsForUser(irodsAuthToken.getIrodsAccount()
							.getUserName());
			for (UserGroup userGroup : userGroups) {
				authentication.getAuthorities().add(
						IRODSUserGroupAuthority.instance(userGroup
								.getUserGroupName()));
				log.debug("added authority for {}",
						userGroup.getUserGroupName());
			}
		} catch (JargonException e) {
			log.error("JargonException when getting user groups for user", e);
			e.printStackTrace();
			throw new AuthenticationServiceException(
					"exception getting user groups for user", e);
		}

		log.debug("authorities added");
		return authentication;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports
	 * (java.lang.Class)
	 */
	@Override
	public boolean supports(final Class<? extends Object> authentication) {
		return false;
	}

	public IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	public void setIrodsProtocolManager(
			final IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
