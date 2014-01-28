/**
 * 
 */
package org.irods.jargon.vircoll;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a virtual collection service that can manage virtual
 * collections
 * 
 * @author mikeconway
 * 
 */
public class VirtualCollectionServiceImpl extends AbstractJargonService {

	static Logger log = LoggerFactory
			.getLogger(VirtualCollectionServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public List<AbstractVirtualCollection> listBaseUserVirtualCollections()
			throws JargonException {

		log.info("listUserVirtualCollections()");

		List<AbstractVirtualCollection> virtualCollections = new ArrayList<AbstractVirtualCollection>();

		// Add root of grid, and user home collection

		// Add starred

		// Add shared by me

		// Add shared with me

		return virtualCollections;

	}

}
