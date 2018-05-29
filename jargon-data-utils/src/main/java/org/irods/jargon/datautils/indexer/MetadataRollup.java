/**
 *
 */
package org.irods.jargon.datautils.indexer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.irods.jargon.core.query.MetaDataAndDomainData;

/**
 * Represents a Deque containing the rollup of metadata from the current node up
 * to its parents
 *
 * @author conwaymc
 *
 */
public class MetadataRollup {

	private Deque<List<MetaDataAndDomainData>> metadata = new ArrayDeque<List<MetaDataAndDomainData>>();

	/**
	 *
	 */
	public MetadataRollup() {
	}

	public Deque<List<MetaDataAndDomainData>> getMetadata() {
		return metadata;
	}

	public void setMetadata(final Deque<List<MetaDataAndDomainData>> metadata) {
		this.metadata = metadata;
	}

}
