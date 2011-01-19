/**
 * 
 */
package org.irods.jargon.spring.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * IRODS specific implementation of a Spring Security
 * <code>Authentication</code> interface that represents an authentication
 * request for an IRODS Principal. In this case, the Principal is an
 * <code>IRODSAccount</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAuthenticationToken implements Authentication {

	private static final long serialVersionUID = 2901482087811219572L;
	private List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
	private IRODSAccount irodsAccount = null;
	private boolean authenticated = false;

	public IRODSAuthenticationToken(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new JargonRuntimeException("irodsAccount is null");
		}
		this.irodsAccount = irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getAuthorities()
	 */
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return grantedAuthorities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return irodsAccount.getPassword();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getDetails()
	 */
	@Override
	public Object getDetails() {
		return irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return irodsAccount.getUserName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#isAuthenticated()
	 */
	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.core.Authentication#setAuthenticated(boolean
	 * )
	 */
	@Override
	public void setAuthenticated(final boolean authenticated)
			throws IllegalArgumentException {
		this.authenticated = authenticated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Principal#getName()
	 */
	@Override
	public String getName() {
		return irodsAccount.getUserName();
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	protected void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}
