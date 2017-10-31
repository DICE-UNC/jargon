package org.irods.jargon.datautils.tree;

import javax.swing.tree.DefaultTreeModel;

/**
 * Model of a hierarchical tree of nodes, useful for representing hierarchies of
 * collections and data objects.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -4326229056412362449L;

	/**
	 * Creates a tree specifying whether any node can have children, or whether
	 * only certain nodes can have children.
	 *
	 * @param root
	 *            {@link FileTreeNode} that is the root of the file tree
	 * @param asksAllowsChildren
	 *            {@code boolean}, false if any node can have children,
	 *            true if each node is asked to see if it can have children
	 */
	public FileTreeModel(final FileTreeNode root,
			final boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}

	/**
	 * Creates a tree in which any node can have children.
	 *
	 * @param root
	 *            {@link FileTreeNode} that is the root of the file tree
	 */
	public FileTreeModel(final FileTreeNode root) {
		super(root);
	}

}
