/**
 *
 */
package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to run a crawl given a visitor and configuration. This is an example
 * of how the crawl can be initiated. It is likey a specific implementation
 * would differ.
 *
 * @author conwaymc
 *
 */
public class HierVisitorCrawlerServiceImpl extends AbstractJargonService {

	public static final Logger log = LoggerFactory.getLogger(HierVisitorCrawlerServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public HierVisitorCrawlerServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public HierVisitorCrawlerServiceImpl() {
	}

	public void launch(final String startingCollectionPath, final HierVisitor visitor)
			throws DataNotFoundException, JargonException {
		log.info("launch");
		if (startingCollectionPath == null || startingCollectionPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty startingCollectionPath");
		}
		log.info("startingCollectionPath:{}", startingCollectionPath);
		if (visitor == null) {
			throw new IllegalArgumentException("null visitor");
		}

		log.info("beginning the crawl...");

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
