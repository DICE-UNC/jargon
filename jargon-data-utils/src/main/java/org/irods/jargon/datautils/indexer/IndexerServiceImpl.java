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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service to run an indexer
 *
 * @author conwaymc
 *
 */
public class IndexerServiceImpl extends AbstractJargonService {

	public static final Logger log = LogManager.getLogger(IndexerServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public IndexerServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public IndexerServiceImpl() {
	}

	/**
	 * Start the indexing process
	 *
	 * @param startingCollectionPath
	 *            {@code String} with starting point
	 * @param visitor
	 *            {@link AbstractIndexerVisitor} to receive callbacks
	 * @throws DataNotFoundException
	 *             {@link DataNotFoundException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
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

		IRODSFileImpl startingPoint = (IRODSFileImpl) getIrodsAccessObjectFactory()
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
