/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Captures the content negotiation process in the client given a connection and
 * other information.
 * 
 * @author Mike Conway - DICE
 *
 */
class ContentNegotiationService {

	private final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol;
	private final IRODSAccount irodsAccount;
	private final StartupResponseData startupResponseData;

	/**
	 * @param irodsMidLevelProtocol
	 * @param irodsAccount
	 * @param startupResponseData
	 */
	ContentNegotiationService(
			AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			IRODSAccount irodsAccount, StartupResponseData startupResponseData) {
		super();
		this.irodsMidLevelProtocol = irodsMidLevelProtocol;
		this.irodsAccount = irodsAccount;
		this.startupResponseData = startupResponseData;
	}

	/**
	 * @return the irodsMidLevelProtocol
	 */
	AbstractIRODSMidLevelProtocol getIrodsMidLevelProtocol() {
		return irodsMidLevelProtocol;
	}

	/**
	 * @return the irodsAccount
	 */
	IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @return the startupResponseData
	 */
	StartupResponseData getStartupResponseData() {
		return startupResponseData;
	}

	/**
	 * According to defaults and configuration, should I use an SSL connection?
	 * 
	 * @return <code>boolean</code> of <code>true</code> if SSL should be used
	 * @throws JargonException
	 */
	boolean shouldIUseSsl() throws JargonException {
		return false;
	}

}
