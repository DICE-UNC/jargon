/**
 * 
 */
package org.irods.jargon.lingo.web.security;

import org.irods.jargon.core.exception.JargonException;
import org.springframework.security.core.GrantedAuthority;

/**
 * A Spring Security authority 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSUserGroupAuthority implements GrantedAuthority {
	
	private static final long serialVersionUID = -1913521481109095653L;
	private final String userGroupName;
	
	public static IRODSUserGroupAuthority instance(String userGroupName) throws JargonException {
		return new IRODSUserGroupAuthority(userGroupName);
	}

	private IRODSUserGroupAuthority(String userGroupName) throws JargonException {
		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new JargonException("userGroupName is null or empty");
		}
		this.userGroupName = userGroupName;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.security.core.GrantedAuthority#getAuthority()
	 */
	@Override
	public String getAuthority() {
		return userGroupName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GrantedAuthority o) {
		return (o.getAuthority().compareTo(userGroupName));
	}

}
