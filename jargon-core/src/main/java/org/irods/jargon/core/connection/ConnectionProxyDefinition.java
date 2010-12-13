/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * For a given role (or IRODS User Group) define the IRODSAccount to use for
 * that role when handling proxy connections. In usage, a list of these
 * definitions can be kept by a connection manager, and obtained based on the
 * role of the proxied account.
 * 
 * This class is immutable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConnectionProxyDefinition {

	private final String role;
	private final IRODSAccount irodsAccount;

	public static ConnectionProxyDefinition instance(final String role,
			final IRODSAccount irodsAccount) throws JargonException {
		return new ConnectionProxyDefinition(role, irodsAccount);
	}

	private ConnectionProxyDefinition(final String role,
			final IRODSAccount irodsAccount) throws JargonException {
		if (role == null || role.isEmpty()) {
			throw new JargonException("role is null or empty");
		}

		if (irodsAccount == null) {
			throw new JargonException("irodsAccount is null");
		}

		this.role = role;
		this.irodsAccount = irodsAccount;
	}

	public String getRole() {
		return role;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}
