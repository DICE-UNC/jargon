package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Represent an iRODS tree as a recursively iteratable node. This node allows
 * filtering based on a passed-in filter.
 * <p/>
 * NOTE: work-in-progress to support Akubra
 * -add URI scheme object
 * -add filter object
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileTreeUriIteratorNode {

	private final IRODSFile parentFile;
	private int currentChild = 0;

	public FileTreeUriIteratorNode(final IRODSFile parentFile) {
		if (parentFile == null) {
			throw new IllegalArgumentException("null parentFile");
		}
		this.parentFile = parentFile;
	}

}
