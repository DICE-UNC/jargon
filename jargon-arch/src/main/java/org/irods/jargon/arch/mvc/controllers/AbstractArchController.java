/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.utils.ArchServiceFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base for controllers in the arch application
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class AbstractArchController {
	
	private IRODSAccessObjectFactoryI irodsAccessObjectFactory = null;
	private ArchServiceFactory archServiceFactory = null;
	public static final String ERROR_MESSAGE_MODEL = "actionError";
	public static final String JAVASCRIPT_ERROR_MESSAGE_FLAG="javaScriptErrorMessageFlag:";
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public AbstractArchController() {
		
	}

	public IRODSAccessObjectFactoryI getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactoryI irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
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
			throw new JargonException(
					"no irods account found in authentication");
		}
		return irodsAccount;

	}

	/**
	 * Retrieve the service factory for Arch objects
	 * @return {@link org.irods.jargon.arch.utils.ArchServiceFactory}
	 */
	public ArchServiceFactory getArchServiceFactory() {
		return archServiceFactory;
	}

	/**
	 * Set the factory that will create the various Arch services
	 * @param archServiceFactory  {@link org.irods.jargon.arch.utils.ArchServiceFactory}
	 */
	public void setArchServiceFactory(ArchServiceFactory archServiceFactory) {
		this.archServiceFactory = archServiceFactory;
	}

	protected void checkControllerInjectedContracts() throws ArchException {
		if (archServiceFactory == null) {
			throw new ArchException("archServiceFactory not available");
		}
		
		if (irodsAccessObjectFactory == null) {
			throw new ArchException("irodsAccessObjectFactory is null");
		}
		
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
