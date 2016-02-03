/**
 * 
 */
package org.irods.jargon.mdquery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A POJO that represents a metadata (AVU) query in the same sort of manner as
 * imeta. This can be used as a basis for a virtual collection type.
 * 
 * @author Mike Conway - DICE
 * 
 *
 */
@XmlRootElement
public class MetadataQuery {

	/**
	 * Query should return data objects, collection, or both
	 *
	 */
	public enum QueryType {
		DATA, COLLECTIONS, BOTH
	}

	private QueryType queryType = QueryType.BOTH;
	/**
	 * Path hint (proposed) to cue
	 */
	private String pathHint = "";

	private List<MetadataQueryElement> metadataQueryElements = new ArrayList<MetadataQueryElement>();

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public String getPathHint() {
		return pathHint;
	}

	public void setPathHint(String pathHint) {
		this.pathHint = pathHint;
	}

	public List<MetadataQueryElement> getMetadataQueryElements() {
		return metadataQueryElements;
	}

	public void setMetadataQueryElements(
			List<MetadataQueryElement> metadataQueryElements) {
		this.metadataQueryElements = metadataQueryElements;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataQuery [");
		if (queryType != null) {
			builder.append("queryType=").append(queryType).append(", ");
		}
		if (pathHint != null) {
			builder.append("pathHint=").append(pathHint).append(", ");
		}
		if (metadataQueryElements != null) {
			builder.append("metadataQueryElements=").append(
					metadataQueryElements.subList(0,
							Math.min(metadataQueryElements.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

}
