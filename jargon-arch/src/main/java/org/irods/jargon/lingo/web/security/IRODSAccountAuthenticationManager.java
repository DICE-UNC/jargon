/**
 * 
 */
package org.irods.jargon.lingo.web.security;

import java.net.UnknownHostException;
import java.util.List;

import org.irods.jargon.core.connection.IRODSManagedConnection;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Implementation of a Spring Security <code>AuthenticationManager</code>
 * interface. This manager will authenticate a user to an IRODS zone with a
 * given IRODSAccount.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAccountAuthenticationManager implements AuthenticationManager {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private IRODSProtocolManager irodsProtocolManager = null;
	private IRODSAccessObjectFactoryI irodsAccessObjectFactory = null;

	public IRODSAccountAuthenticationManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.security.authentication.AuthenticationManager#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication)
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
					.getIrodsAccount());
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
				log.debug("added authority for {}", userGroup
						.getUserGroupName());
			}
		} catch (JargonException e) {
			log.error("JargonException when getting user groups for user", e);
			e.printStackTrace();
			throw new AuthenticationServiceException(
					"exception getting user groups for user", e);
		} finally {
			try {
				irodsAccessObjectFactory.closeSession();
			} catch (JargonException e) {
				log.warn("exception closing session after auth, logged and bypassed", e);
			}
		}

		log.debug("authorities added");
		
		
		return authentication;

	}

	public IRODSProtocolManager getIrodsProtocolManager() {
		return irodsProtocolManager;
	}

	/**
	 * Authentication is done by logging the user into IRODS with the given
	 * account. This protocol manager will hand out the connection to the user
	 * based on the given account, and then may be thrown away, or cached,
	 * depending on the particular protocol manager provided.
	 * 
	 * @param irodsProtocolManager
	 *            <code>IRODSPrococolManager</code> implementation used to log
	 *            the user in as a test of authentication.
	 */
	public void setIrodsProtocolManager(
			IRODSProtocolManager irodsProtocolManager) {
		this.irodsProtocolManager = irodsProtocolManager;
	}

	public IRODSAccessObjectFactoryI getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * Factory to create access objects used to obtain necessary IRODS data used
	 * in authentication and authorization.
	 * 
	 * @param irodsAccessObjectFactory
	 */
	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactoryI irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
