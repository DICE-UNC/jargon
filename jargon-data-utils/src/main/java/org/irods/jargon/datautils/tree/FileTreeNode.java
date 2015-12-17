package org.irods.jargon.datautils.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Depicts a tree of objects, useful for representing hierarchical file
 * structures for various purposes.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 6420267079565634214L;

	/**
	 * Default constructor takes a user object.
	 *
	 * @param userObject
	 *            <code>Object</code> that represents the user data to store in
	 *            the node
	 */
	public FileTreeNode(final Object userObject) {
		super(userObject);
	}

}
