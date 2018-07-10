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

	/**
	 * Zone hint, may be left blank. Queries will
	 * <ul>
	 * <li>Check and use zone hint in actual genquery</li>
	 * <li>Check the zone from any path hint</li>
	 * <li>Use the zone of the iRODS account</li>
	 * </ul>
	 *
	 * In that priority order
	 */
	private String targetZone = "";

	private QueryType queryType = QueryType.BOTH;
	/**
	 * Path hint (proposed) to cue
	 */
	private String pathHint = "";

	private List<MetadataQueryElement> metadataQueryElements = new ArrayList<MetadataQueryElement>();

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(final QueryType queryType) {
		this.queryType = queryType;
	}

	public String getPathHint() {
		return pathHint;
	}

	public void setPathHint(final String pathHint) {
		this.pathHint = pathHint;
	}

	public List<MetadataQueryElement> getMetadataQueryElements() {
		return metadataQueryElements;
	}

	public void setMetadataQueryElements(final List<MetadataQueryElement> metadataQueryElements) {
		this.metadataQueryElements = metadataQueryElements;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataQuery [");
		if (targetZone != null) {
			builder.append("targetZone=").append(targetZone).append(", ");
		}
		if (queryType != null) {
			builder.append("queryType=").append(queryType).append(", ");
		}
		if (pathHint != null) {
			builder.append("pathHint=").append(pathHint).append(", ");
		}
		if (metadataQueryElements != null) {
			builder.append("metadataQueryElements=")
					.append(metadataQueryElements.subList(0, Math.min(metadataQueryElements.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	public String getTargetZone() {
		return targetZone;
	}

	public void setTargetZone(final String targetZone) {
		this.targetZone = targetZone;
	}

}
