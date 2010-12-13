/**
 * 
 */
package org.irods.jargon.lingo.mvc.controllers;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.exceptions.LingoException;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base for controllers in the arch application
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractLingoController {

	private IRODSAccessObjectFactoryI irodsAccessObjectFactory = null;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public AbstractLingoController() {

	}
	
	protected void checkControllerInjectedContracts() throws LingoException {
		
		if (irodsAccessObjectFactory == null) {
			log.error("irodsAccessObjectFactory is null, this needs to be set in the spring init");
			throw new LingoException("irodsAccessObjectFactory is null");
		}
		
	}

	/**
	 * Retrieve the authentication token for this user
	 * 
	 * @return <code>IRODSAuthenticationToken</code> containing identity and
	 *         granted roles
	 * @throws JargonException
	 */
	protected IRODSAuthenticationToken getIRODSAuthenticationToken()
			throws JargonException {
		Object authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		if (authentication instanceof IRODSAuthenticationToken) {
			return (IRODSAuthenticationToken) authentication;
		} else {
			log.error("no irods authentication token found, there is an error with the spring secuity configuration");
			throw new JargonException(
					"no irods authentication token found in SecurityContext");
		}
	}

	/**
	 * Convenience method to retrieve the IRODSAccount of the logged-in user
	 * 
	 * @return <code>IRODSAccount</code> describing the logged-in user
	 * @throws JargonException
	 */
	protected IRODSAccount getAuthenticatedIRODSAccount()
			throws JargonException {
		IRODSAuthenticationToken token = getIRODSAuthenticationToken();
		IRODSAccount irodsAccount = token.getIrodsAccount();
		if (irodsAccount == null) {
			log.error("no irods account found, possible spring security configuration exception");
			throw new JargonException(
					"no irods account found in authentication");
		}
		return irodsAccount;

	}



	public IRODSAccessObjectFactoryI getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}



	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactoryI irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}
	
	protected void closeSessionIgnoringExceptions() {
		try {
			this.getIrodsAccessObjectFactory().closeSession();
		} catch (JargonException e) {
			log
					.error(
							"jargon exception on session close, logged and discarded",
							e);
		}
	}


}
