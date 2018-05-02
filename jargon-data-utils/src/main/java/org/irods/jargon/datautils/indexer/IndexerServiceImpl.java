/**
 * 
 */
package org.irods.jargon.datautils.indexer;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.datautils.visitor.IrodsVisitedComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to run an indexer
 * 
 * @author conwaymc
 *
 */
public class IndexerServiceImpl extends AbstractJargonService {

	public static final Logger log = LoggerFactory.getLogger(IndexerServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public IndexerServiceImpl(IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public IndexerServiceImpl() {
	}

	public void launch(final String startingCollectionPath, final AbstractIndexerVisitor visitor)
			throws DataNotFoundException, JargonException {
		log.info("launch");
		if (startingCollectionPath == null || startingCollectionPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty startingCollectionPath");
		}
		log.info("startingCollectionPath:{}", startingCollectionPath);
		if (visitor == null) {
			throw new IllegalArgumentException("null visitor");
		}

		log.info("beginning the crawl...east to west...north to south...");

		IRODSFileImpl startingPoint = (IRODSFileImpl) this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(startingCollectionPath);

		if (!startingPoint.isDirectory()) {
			log.info("starting point is not a leaf node:{}", startingPoint);
			throw new JargonException("cannot start a crawl on a leaf node!");
		}

		IrodsVisitedComposite startingComposite = new IrodsVisitedComposite(startingPoint);
		startingComposite.accept(visitor);

		log.info("....crawl!");

	}

}
