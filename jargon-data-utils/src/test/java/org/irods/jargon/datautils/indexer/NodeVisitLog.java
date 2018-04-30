/**
 * 
 */
package org.irods.jargon.datautils.indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a record of each visit action, params, etc
 * 
 * @author conwaymc
 *
 */
public class NodeVisitLog {

	private List<NodeVisitLogEntry> logEntries = new ArrayList<NodeVisitLogEntry>();

	/**
	 * 
	 */
	public NodeVisitLog() {
	}

	public void add(NodeVisitLogEntry entry) {
		logEntries.add(entry);
	}

	public List<NodeVisitLogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<NodeVisitLogEntry> logEntries) {
		this.logEntries = logEntries;
	}

}
