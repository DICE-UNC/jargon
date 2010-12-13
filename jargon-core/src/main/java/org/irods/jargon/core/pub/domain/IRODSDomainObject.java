/**
 * 
 */
package org.irods.jargon.core.pub.domain;

/**
 * Base class for iRODS domain objects, holding information that is used when
 * doing queries, to allow paging
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSDomainObject {

	private int count = 0;
	private boolean lastResult = false;

	public boolean isLastResult() {
		return lastResult;
	}

	public void setLastResult(final boolean lastResult) {
		this.lastResult = lastResult;
	}

	public int getCount() {
		return count;
	}

	public void setCount(final int count) {
		this.count = count;
	}

}
