/**
 *
 */
package org.irods.jargon.datautils.indexer;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.datautils.visitor.AbstractIrodsVisitorComponent;
import org.irods.jargon.datautils.visitor.HierComponent;
import org.irods.jargon.datautils.visitor.HierComposite;
import org.irods.jargon.datautils.visitor.HierLeaf;
import org.irods.jargon.datautils.visitor.IrodsVisitedComposite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract superclass for a filtering, metadata aware visitor used for
 * indexing. This adapter introduces automatically maintained metadata as well
 * as the notion of a filter and 'control rod' to tune the behavior of the
 * indexer.
 *
 * @author conwaymc
 *
 */
public abstract class AbstractIndexerVisitor extends AbstractIrodsVisitorComponent {

	public static final Logger log = LogManager.getLogger(AbstractIndexerVisitor.class);
	private final CollectionAO collectionAO;
	private final DataObjectAO dataObjectAO;
	/**
	 * Set to <code>true</code> to finish all processing in a normal fashion by
	 * skipping all remaining files/children
	 */
	private boolean aborted = false;

	/**
	 * Filter for deciding whether to index a collection or file. Maybe be left
	 * <code>null</code> for no filtering
	 */
	private ConfigurableIndexerFilter indexerFilter = null;

	/**
	 * Wraps a stack of metadata from the current node up to the root
	 */
	private MetadataRollup metadataRollup = new MetadataRollup();

	/**
	 * Optional control that can sleep or halt an indexing run, can be used to limit
	 * load caused by indexing. Can be left <code>null</code> for no throttling
	 */
	private ControlRod controlRod = null;

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public AbstractIndexerVisitor(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsAccessObjectFactory, irodsAccount);
		collectionAO = irodsAccessObjectFactory.getCollectionAO(getIrodsAccount());
		dataObjectAO = irodsAccessObjectFactory.getDataObjectAO(getIrodsAccount());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.visitor.AbstractIrodsVisitor#visitEnter(org.irods.
	 * jargon.datautils.visitor.HierComposite)
	 */
	@Override
	public boolean visitEnter(final HierComposite node) {
		log.info("visitEnter()");
		if (node == null) {
			throw new IllegalArgumentException("null node");
		}

		log.debug("checking control rod");
		aborted = checkControlRod(node);

		if (aborted) {
			log.info("aborted!");
			return false;
		}

		log.info("obtaining metadata for:{}", node);
		try {
			List<MetaDataAndDomainData> metadata = collectionAO.findMetadataValuesForCollection(node.getAbsolutePath(),
					0);
			metadataRollup.getMetadata().push(metadata);
			log.info(
					"pushed metadata in the stack...now filter and then delegate to visitEnterWithMetadata() in the impl class to make any determinations");

			/*
			 * On a collection, a non indexable result from a filter cases a short circuit
			 * so that siblings are not processed
			 */
			if (!checkIfIndexable(node, metadataRollup)) {
				log.info("not indexable by filter, short circuit");
				return false;
			}

			boolean shortCircuit = visitEnterWithMetadata(node, metadataRollup);
			// even if short circuited the visitLeave will be called and the metadata will
			// be popped back off of the stack
			return shortCircuit;
		} catch (JargonException | JargonQueryException e) {
			log.error("error in obtaining metadata", e);
			throw new JargonRuntimeException("error getting metadata", e);
		}

	}

	/**
	 * Indicate whether the visitor should enter a collection and process its
	 * children. This processes the collection before any children are processed.
	 * <p>
	 * Alternately, the implementation can wait until all children are processed in
	 * the visitLeaveWithMetadata() method
	 *
	 * To be extended by the indexer, this will call visitEnter with the
	 * already-obtained metadata values rolled up to the parent
	 *
	 * @param node
	 *            {@link HierComposite} with the parent node to enter
	 * @param metadataRollup
	 *            {@link MetadataRollup} with the metadata from the root down to the
	 *            current node
	 * @return {@code boolean} with a return of <code>true</code> if the visitor
	 *         should enter the collection
	 */
	public abstract boolean visitEnterWithMetadata(HierComposite node, MetadataRollup metadataRollup);

	@Override
	public boolean visitLeave(final HierComposite node, final boolean wasEntered) {

		log.info("visitLeave()");
		if (node == null) {
			throw new IllegalArgumentException("null node");
		}

		if (aborted) {
			return false;
		}

		log.info("visitLeave for node:{}", node);

		log.info("delegating to visit leave before popping metadata off the stack");
		boolean shortCircuit = visitLeaveWithMetadata(node, metadataRollup, wasEntered);
		metadataRollup.getMetadata().pop();
		return shortCircuit;
	}

	/**
	 * To be extended by the particular indexer, indicates a visitor is leaving a
	 * collection and has processed its children.
	 *
	 * To be extended by the indexer, this will map to visitLeave with the
	 * already-obtained metadata values rolled up to the parent
	 *
	 * @param node
	 *            {@link HierComposite} with the parent node to enter
	 * @param metadataRollup
	 *            {@link MetadataRollup} with the metadata from the root down to the
	 *            current node
	 * @param visitorEntered
	 *            {@code boolean} indicating that the operation had been short
	 *            circuited (returning <code>false</code>). This indicates that the
	 *            indexer may want to ignore any further indexing action on the
	 *            visitLeave(). This is indexer-dependent. Note that the current
	 *            metadata stack has been popped to remove the metadata at this
	 *            child level.
	 *
	 * @return {@code boolean} with a return of <code>true</code> if the visitor
	 *         should enter the collection
	 */
	public abstract boolean visitLeaveWithMetadata(HierComposite node, MetadataRollup metadataRollup,
			boolean visitorEntered);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.visitor.AbstractIrodsVisitor#visit(org.irods.
	 * jargon.datautils.visitor.HierLeaf)
	 */
	@Override
	public boolean visit(final HierLeaf node) {
		log.info("visit()");
		if (node == null) {
			throw new IllegalArgumentException("null node");
		}

		log.debug("checking control rod");
		aborted = checkControlRod(node);

		if (aborted) {
			return false;
		}

		log.info("obtaining metadata for:{}", node);
		try {
			List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesForDataObject(node.getAbsolutePath());
			metadataRollup.getMetadata().push(metadata);
			log.info(
					"pushed metadata in the stack...filter and then delegate to visitMetadata() in the impl class to make any determinations");

			/*
			 * On a leaf don't short circuit just because I don't index this leaf (it could
			 * be based on file type, etc)
			 */
			if (!checkIfIndexable(node, metadataRollup)) {
				log.info("not indexable by filter but don't short circuit, keep processing other siblings");
				metadataRollup.getMetadata().pop();
				return true;
			}

			log.info("passes filter");
			boolean shortCircuit = visitWithMetadata(node, metadataRollup);
			// now pop the data back off the stack for the next child
			metadataRollup.getMetadata().pop();
			return shortCircuit;
		} catch (JargonException e) {
			log.error("error in obtaining metadata", e);
			throw new JargonRuntimeException("error getting metadata", e);
		}

	}

	/**
	 * Base launch method will start the visitor process. This is most likely
	 * implemented outside of the visitor itself for a particular application.
	 * 
	 * @param startingCollectionPath
	 *            {@code String} with the starting absolute path of a parent
	 *            collection.
	 */
	public void launch(final String startingCollectionPath) {
		log.info("launch");
		if (startingCollectionPath == null || startingCollectionPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty startingCollectionPath");
		}
		log.info("startingCollectionPath:{}", startingCollectionPath);
		log.info("beginning the crawl...");

		IRODSFileImpl startingPoint;
		try {
			startingPoint = (IRODSFileImpl) getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(startingCollectionPath);
			if (!startingPoint.isDirectory()) {
				log.info("starting point is not a leaf node:{}", startingPoint);
				throw new JargonException("cannot start a crawl on a leaf node!");
			}

			IrodsVisitedComposite startingComposite = new IrodsVisitedComposite(startingPoint);
			startingComposite.accept(this);
			log.info("....crawl complete!");
		} catch (JargonException e) {
			log.error("error in obtaining metadata", e);
			throw new JargonRuntimeException("error lauching visitor", e);
		}

	}

	/**
	 * To be extended by the particular indexer, this method is delegated from the
	 * <code>visit()</code> method and adds the metadata associated with the given
	 * leaf in the hierarchy.
	 *
	 * @param hierLeaf
	 *            {@link HierLeaf} with the leaf node (File) being visited
	 * @param metadataRollup
	 *            {@link MetadataRollup} with the metadata from the root down to the
	 *            current node
	 * @return {@code boolean} with a return of <code>true</code> if the visitor
	 *         should short circuit the rest of the siblings of the node
	 */
	public abstract boolean visitWithMetadata(HierLeaf hierLeaf, MetadataRollup metadataRollup);

	public ConfigurableIndexerFilter getIndexerFilter() {
		return indexerFilter;
	}

	public void setIndexerFilter(final ConfigurableIndexerFilter indexerFilter) {
		this.indexerFilter = indexerFilter;
	}

	public ControlRod getControlRod() {
		return controlRod;
	}

	public void setControlRod(final ControlRod controlRod) {
		this.controlRod = controlRod;
	}

	/*
	 * Give a space for a pause or end of processing
	 *
	 */
	private boolean checkControlRod(final HierComponent component) {

		if (controlRod == null) {
			return false;
		} else {
			return controlRod.checkControlRod(component);
		}

	}

	/*
	 * Returns <code>true</code> if indexable otherwise it should be ignored
	 */
	private boolean checkIfIndexable(final HierComponent component, final MetadataRollup metadataRollup) {
		log.info("checkIfFiltered()");
		if (indexerFilter == null) {
			log.info("no filter present");
			return true;
		} else {
			if (component instanceof HierLeaf) {
				log.info("filter a leaf");
				return indexerFilter.isIndexable((HierLeaf) component, metadataRollup);
			} else {
				log.info("filter a collection");
				return indexerFilter.isIndexable((HierComposite) component, metadataRollup);
			}
		}
	}

	public MetadataRollup getMetadataRollup() {
		return metadataRollup;
	}

	public void setMetadataRollup(final MetadataRollup metadataRollup) {
		this.metadataRollup = metadataRollup;
	}

}
