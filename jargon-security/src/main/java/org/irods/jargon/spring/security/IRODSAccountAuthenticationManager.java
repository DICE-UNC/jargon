/**
 * 
 */
package org.irods.jargon.spring.security;

import java.net.UnknownHostException;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private IRODSAccessObjectFactory irodsAccessObjectFactory = null;

	public IRODSAccountAuthenticationManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.security.authentication.AuthenticationManager#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException {

		if (LOG.isInfoEnabled()) {
			LOG.info("authenticating:" + authentication);
		}

		if (authentication == null) {
			LOG.error("the authentication passed to the method is null");
			throw new BadCredentialsException("null authentication");
		}

		if (!(authentication instanceof IRODSAuthenticationToken)) {
			String msg = "the authentication token passed in is not an instance of IRODSAuthenticationToken";
			LOG.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (irodsAccessObjectFactory == null) {
			throw new AuthenticationServiceException(
					"irods access object factory is null");
		}

		IRODSAuthenticationToken irodsAuthToken = (IRODSAuthenticationToken) authentication;

		LOG.debug("doing authentication call to irods for account: {}",
				irodsAuthToken.getIrodsAccount());

		try {
			irodsAccessObjectFactory
					.getUserAO(irodsAuthToken.getIrodsAccount());
		} catch (JargonException e) {
			LOG.error("unable to authenticate, JargonException", e);
			e.printStackTrace();

			if (e.getCause() == null) {
				if (e.getMessage().indexOf("-826000") > -1) {
					LOG.warn("invalid user/password");

					throw new BadCredentialsException(
							"Unknown user id/password", e);
				} else {
					LOG.error("authentication service exception", e);

					throw new AuthenticationServiceException(
							"unable to authenticate", e);
				}
			} else if (e.getCause() instanceof UnknownHostException) {
				LOG.warn("cause is invalid host");

				throw new BadCredentialsException("The host is unknown", e);
			} else if (e.getCause().getMessage().indexOf("refused") > -1) {
				LOG.error("cause is refused or invalid port");

				throw new BadCredentialsException(
						"The host/port is unknown or refusing connection", e);
			} else {
				LOG.error("authentication service exception", e);

				throw new AuthenticationServiceException(
						"unable to authenticate", e);
			}
		}

		LOG.info("authenticated");
		authentication.setAuthenticated(true);

		// get roles
		LOG.info("getting role information for {}", irodsAuthToken
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
				LOG.debug("added authority for {}",
						userGroup.getUserGroupName());
			}
		} catch (JargonException e) {
			LOG.error("JargonException when getting user groups for user", e);
			e.printStackTrace();
			throw new AuthenticationServiceException(
					"exception getting user groups for user", e);
		} finally {
			try {
				irodsAccessObjectFactory.closeSession();
			} catch (JargonException e) {
				LOG.warn(
						"exception closing session after auth, logged and bypassed",
						e);
			}
		}

		LOG.debug("authorities added");

		return authentication;

	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * Factory to create access objects used to obtain necessary IRODS data used
	 * in authentication and authorization.
	 * 
	 * @param irodsAccessObjectFactory
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
