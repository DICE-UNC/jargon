/**
 * 
 */
package org.irods.jargon.spring.security;

import org.irods.jargon.core.exception.JargonException;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * A Spring Security authority
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSUserGroupAuthority extends GrantedAuthorityImpl {

	private static final long serialVersionUID = -1913521481109095653L;

	public static IRODSUserGroupAuthority instance(final String userGroupName)
			throws JargonException {
		return new IRODSUserGroupAuthority(userGroupName);
	}

	private IRODSUserGroupAuthority(final String userGroupName)
			throws JargonException {
		super(userGroupName);
	}

}
