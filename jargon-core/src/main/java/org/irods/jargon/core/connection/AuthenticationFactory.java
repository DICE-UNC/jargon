package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.auth.AuthUnavailableException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a factory that can return the appropriate authentication
 * implementation class based on the auth type.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface AuthenticationFactory {

	/**
	 * This is an abstract factory, so implementations need to use an arbitrary
	 * <code>String</code> to determine the proper <code>AuthMechanism</code>
	 * implementation to return.
	 * <p>
	 * In the default representation, the mechanism is the {@link IRODSAccount}
	 * enum value contained in the <code>IRODSAccount</code> as returned by the
	 * <code>getName()</code> method. Custom implementations of this factory may
	 * use other schemes.
	 * <p>
	 * Note that this factory will defaut to a standard iRODS auth when the
	 * public (anonymous) account is supplied.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} account containing desired auth scheme
	 * @return {@link AuthMechanism} that is created based on the
	 *         <code>authScheme</code> in the given <code>IRODSAccount</code>
	 * @throws AuthUnavailableException
	 *             if the given <code>authScheme</code> is not supported by the
	 *             factory implementation.
	 * @throws JargonException
	 */
	AuthMechanism instanceAuthMechanism(IRODSAccount irodsAccount)
			throws AuthUnavailableException, JargonException;

}
