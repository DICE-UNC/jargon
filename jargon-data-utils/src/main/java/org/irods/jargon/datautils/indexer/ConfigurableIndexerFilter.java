/**
 * 
 */
package org.irods.jargon.datautils.indexer;

import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.datautils.visitor.HierComposite;
import org.irods.jargon.datautils.visitor.HierLeaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default indexer filter that in this implementation can be configured to index unless
 * told not to. This behavior is controlled by standard index control AVUs that will appear in the metadata stack.
 * <p>
 * Index control AVUS can have either a blank AVU value, or contain a value that will be matched to the configured name of this indexer.
 * 
 * 
 * @author conwaymc
 *
 */
public class ConfigurableIndexerFilter implements IndexerFilterInterface {

	public static final Logger log = LoggerFactory.getLogger(ConfigurableIndexerFilter.class);

	/**
	 * Standard AVU attribute that permits indexing on a collection and children
	 */
	public static final String STANDARD_PERMIT_ATTRIBUTE = "DOINDEX";

	/**
	 * Standard AVU attribute that denies indexing on a collection and children
	 */

	public static final String STANDARD_DENY_ATTRIBUTE = "DONOTINDEX";
	
	/**
	 * Default name given to an indexer
	 */
	public static final String DEFAULT_INDEXER_NAME = "generic";

	/**
	 * Standard AVU unit for identifying an indexing control indicator
	 */
	public static final String STANDARD_INDEXING_UNIT = "irods:indexing";

	/**
	 * Control property will ignore adding an index on a data object with no
	 * directly attached avu attributes
	 */
	private boolean indexIfNoAvuOnDataObject = false;

	/**
	 * Control property that will ignore adding an index on a collection with no
	 * directly attached avu attributes
	 */
	private boolean indexIfNoAvuOnCollection = false;

	/**
	 * expected AVU unit for indexing control attributes
	 */
	private String indexIndicatorAvuUnit = STANDARD_INDEXING_UNIT;

	/**
	 * Indexer name to be used for matching with index control AVUs. This is for
	 * future expansion, but would allow individual indexers to be independently
	 * controlled based on a name or other selector in the AVU value. Currently will
	 * have no effect.
	 */
	private String indexerName = DEFAULT_INDEXER_NAME;

	
	@Override
	public boolean isIndexable(HierComposite node, MetadataRollup metadataRollup) {
		log.info("isIndexable()");

		/*
		 * I have not been blocked so far, so just see if avus at this level indicate to
		 * go no further
		 */
		boolean indexit = true;
		for (MetaDataAndDomainData metadata : metadataRollup.getMetadata().peek()) {
			if (metadata.getAvuUnit().equals(indexIndicatorAvuUnit)) {
				
				log.info("found an indexer control, see if it applies to this indexer!");
				if (metadata.getAvuValue().isEmpty()) {
					log.info("indicator is for all indexers");
				} else if (this.getIndexerName().equals(metadata.getAvuValue())) {
					log.info("matched indexer control attribute for:{}", this.getIndexerName());
				} else {
					log.info("unmatched indexer control");
					continue;
				}
				
				// matched indexer control attribute
			
				if (metadata.getAvuAttribute().equals(STANDARD_DENY_ATTRIBUTE)) {
					log.info("found a do not index attribute, do not index!: {}", metadata);
					indexit = false;
				}
				
				break;
			}
		}
		
		log.info("determined filter response:{}", indexit);
		return indexit;
	}

	
	@Override
	public boolean isIndexable(HierLeaf leafNode, MetadataRollup metadataRollup) {
		/*
		 * Right now we're not looking or honoring index attributes on data objects, maybe overridden later in specific implementations?
		 * Kept right now for possible expansion.
		 */
		return true;
	}

	public boolean isIndexIfNoAvuOnDataObject() {
		return indexIfNoAvuOnDataObject;
	}

	public void setIndexIfNoAvuOnDataObject(boolean indexIfNoAvuOnDataObject) {
		this.indexIfNoAvuOnDataObject = indexIfNoAvuOnDataObject;
	}

	public boolean isIndexIfNoAvuOnCollection() {
		return indexIfNoAvuOnCollection;
	}

	public void setIndexIfNoAvuOnCollection(boolean indexIfNoAvuOnCollection) {
		this.indexIfNoAvuOnCollection = indexIfNoAvuOnCollection;
	}

	public String getIndexIndicatorAvuUnit() {
		return indexIndicatorAvuUnit;
	}

	public void setIndexIndicatorAvuUnit(String indexIndicatorAvuUnit) {
		this.indexIndicatorAvuUnit = indexIndicatorAvuUnit;
	}

	public String getIndexerName() {
		return indexerName;
	}

	public void setIndexerName(String indexerName) {
		this.indexerName = indexerName;
	}

}
