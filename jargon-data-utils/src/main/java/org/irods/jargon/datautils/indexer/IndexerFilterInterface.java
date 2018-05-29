/**
 *
 */
package org.irods.jargon.datautils.indexer;

import org.irods.jargon.datautils.visitor.HierComposite;
import org.irods.jargon.datautils.visitor.HierLeaf;

/**
 * Interface representing the filtering of collections and files as indexed or
 * not indexed according to some local policy.
 * <p>
 * This interface expects the availability of the roll-up of hierarchical AVU
 * metadata as an input to this decision-making process
 *
 * @author conwaymc
 *
 */
public interface IndexerFilterInterface {

	/**
	 * Given a directory node, indicate whether it is indexable.
	 *
	 * @param node
	 *            {@link HierComposite} that is a visit-able directory
	 * @param metadataRollup
	 *            {@link MetadataRollup} with a stack of AVUs up to a parent
	 * @return {@link boolean} of <code>true</code> if it should be indexed
	 */
	boolean isIndexable(HierComposite node, MetadataRollup metadataRollup);

	/**
	 * Given a leaf (File) node, indicate whether it is indexable
	 *
	 * @param leafNode
	 *            {@link HierLeaf} with the visited node
	 * @param metadataRollup
	 *            {@link MetadataRollup} with a stack of AVUs up to a parent
	 * @return {@link boolean} of <code>true</code> if it should be indexed
	 */
	boolean isIndexable(HierLeaf leafNode, MetadataRollup metadataRollup);

}
