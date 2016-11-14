package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This access object exposes a direct connection to iRODS such that protocol
 * operations can be invoked. This can be used to extend the core jargon API for
 * special sets of operations (e.g. the Ticket administration system, extended
 * iRODS administrative functions) that may be encapsulated in other libraries
 * outside of the core jargon API.
 * <p/>
 * The primary motivation is to create a plug-in point such that Jargon core can
 * remain focused on basic functionality.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ProtocolExtensionPointImpl extends IRODSGenericAO implements
		ProtocolExtensionPoint {

	private static Logger log = LoggerFactory
			.getLogger(ProtocolExtensionPointImpl.class);

	protected ProtocolExtensionPointImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.ProtocolExtensionPoint#irodsFunction(org.irods
	 * .jargon.core.packinstr.IRodsPI)
	 */
	@Override
	public Tag irodsFunction(final IRodsPI irodsPI) throws JargonException {

		if (irodsPI == null) {
			String err = "null irodsPI";
			log.error(err);
			throw new IllegalArgumentException(err);
		}

		return getIRODSProtocol().irodsFunction(IRODSConstants.RODS_API_REQ,
				irodsPI.getParsedTags(), irodsPI.getApiNumber());
	}

}
