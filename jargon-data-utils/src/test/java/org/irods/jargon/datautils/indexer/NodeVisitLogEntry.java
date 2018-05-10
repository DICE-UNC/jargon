/**
 * 
 */
package org.irods.jargon.datautils.indexer;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.query.MetaDataAndDomainData;

/**
 * @author conwaymc
 *
 */
public class NodeVisitLogEntry {

	public enum VisitTypeEnum {
		ENTER, LEAVE, VISIT_LEAF
	}

	private VisitTypeEnum visitType;
	private String nodeAbsolutePath;
	private String nodeName;
	private List<MetaDataAndDomainData> metadataThisLevel = new ArrayList<MetaDataAndDomainData>();
	private boolean resultOfVisit;

	/**
	 * 
	 */
	public NodeVisitLogEntry() {
	}

	public VisitTypeEnum getVisitType() {
		return visitType;
	}

	public void setVisitType(VisitTypeEnum visitType) {
		this.visitType = visitType;
	}

	public String getNodeAbsolutePath() {
		return nodeAbsolutePath;
	}

	public void setNodeAbsolutePath(String nodeAbsolutePath) {
		this.nodeAbsolutePath = nodeAbsolutePath;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public List<MetaDataAndDomainData> getMetadataThisLevel() {
		return metadataThisLevel;
	}

	public void setMetadataThisLevel(List<MetaDataAndDomainData> metadataThisLevel) {
		this.metadataThisLevel = metadataThisLevel;
	}

	public boolean isResultOfVisit() {
		return resultOfVisit;
	}

	public void setResultOfVisit(boolean resultOfVisit) {
		this.resultOfVisit = resultOfVisit;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("NodeVisitLogEntry [");
		if (visitType != null) {
			builder.append("visitType=").append(visitType).append(", ");
		}
		if (nodeAbsolutePath != null) {
			builder.append("nodeAbsolutePath=").append(nodeAbsolutePath).append(", ");
		}
		if (nodeName != null) {
			builder.append("nodeName=").append(nodeName).append(", ");
		}
		if (metadataThisLevel != null) {
			builder.append("metadataThisLevel=")
					.append(metadataThisLevel.subList(0, Math.min(metadataThisLevel.size(), maxLen))).append(", ");
		}
		builder.append("resultOfVisit=").append(resultOfVisit).append("]");
		return builder.toString();
	}

}
