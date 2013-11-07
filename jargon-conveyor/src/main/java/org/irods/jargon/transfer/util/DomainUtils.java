/**
 * 
 */
package org.irods.jargon.transfer.util;

import java.util.Date;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.GridAccount;

/**
 * Utilities used by the various domain objects
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DomainUtils {

	/**
	 * Create a skeleton <code>GridAccount</code> based on the contents of a
	 * given <code>IRODSAccount</code>
	 * <p/>
	 * Note that the given password is not encrypted by pass phrase, so that
	 * might need to be addressed by the caller.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @return {@link GridAccount}
	 */
	public static GridAccount gridAccountFromIRODSAccount(
			final IRODSAccount irodsAccount) {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		GridAccount gridAccount = new GridAccount();
		gridAccount.setAuthScheme(irodsAccount.getAuthenticationScheme());
		gridAccount.setComment("hello");
		gridAccount.setCreatedAt(new Date());
		gridAccount.setDefaultPath("/a/path");
		gridAccount
				.setDefaultResource(irodsAccount.getDefaultStorageResource());
		gridAccount.setHost(irodsAccount.getHost());
		gridAccount.setPassword(irodsAccount.getPassword());
		gridAccount.setPort(irodsAccount.getPort());
		gridAccount.setUpdatedAt(new Date());
		gridAccount.setUserName(irodsAccount.getUserName());
		gridAccount.setZone(irodsAccount.getZone());
		return gridAccount;

	}
	
	/**
	 * Create an <code>IRODSAccount</code> given a <code>GridAccount</code>
	 * instance.
	 * 
	 * @param gridAccount
	 *            {@link GridAccount}
	 * @return {@link IRODSAccount}
	 * @throws JargonException
	 */
	public static IRODSAccount irodsAccountFromGridAccount(
			final GridAccount gridAccount) throws JargonException {
		
		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}
		
		IRODSAccount irodsAccount = IRODSAccount.instance(
				gridAccount.getHost(),
				gridAccount.getPort(), 
				gridAccount.getUserName(),
				gridAccount.getPassword(), 
				gridAccount.getDefaultPath(),
				gridAccount.getZone(), 
				gridAccount.getDefaultResource());
		irodsAccount.setAuthenticationScheme(gridAccount.getAuthScheme());
		return irodsAccount;
	}

}
