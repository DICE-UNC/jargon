/**
 * 
 */
package org.irods.jargon.mdquery.service;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Implementation of a service to generate metadata queries on iRODS. These are
 * actually GenQuery under the covers, but have been abstracted to make higher
 * level services simpler, and to centralize functional testing (e.g. iRODS gen
 * query limitiations) at this layer. Thus this class can enforce any necessary
 * restrictions on queries (number of elements, etc)
 * 
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryServiceImpl extends AbstractJargonService {

	/**
	 * Constructor takes dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create various connected
	 *            services
	 * @param irodsAccount
	 *            {@link IRODSAccount} with authentication credentials
	 */
	public MetadataQueryServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public MetadataQueryServiceImpl() {
	}

}
