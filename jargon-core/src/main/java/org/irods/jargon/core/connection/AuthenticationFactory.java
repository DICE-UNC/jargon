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
	 * {@code String} to determine the proper {@code AuthMechanism} implementation
	 * to return.
	 * <p>
	 * In the default representation, the mechanism is the {@link IRODSAccount} enum
	 * value contained in the {@code IRODSAccount} as returned by the
	 * {@code getName()} method. Custom implementations of this factory may use
	 * other schemes.
	 * <p>
	 * Note that this factory will defaut to a standard iRODS auth when the public
	 * (anonymous) account is supplied.
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} account containing desired auth scheme
	 * @return {@link AuthMechanism} that is created based on the {@code authScheme}
	 *         in the given {@code IRODSAccount}
	 * @throws AuthUnavailableException
	 *             if the given {@code authScheme} is not supported by the factory
	 *             implementation.
	 * @throws JargonException
	 *             on iRODS error
	 */
	AuthMechanism instanceAuthMechanism(IRODSAccount irodsAccount) throws AuthUnavailableException, JargonException;

}
