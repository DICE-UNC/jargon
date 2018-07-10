/**
 *
 */
package org.irods.jargon.datautils.tree;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-processor for a diff tree will roll up counts of diffs in children and
 * augment the diff tree
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DiffTreePostProcessor {

	private static Logger log = LoggerFactory.getLogger(DiffTreePostProcessor.class);

	/**
	 *
	 */
	public DiffTreePostProcessor() {

	}

	/**
	 *
	 * Given a completed diff tree, post process it to roll up the counts of diffs
	 * in children. This is useful for display to users
	 *
	 * @param fileTreeModel
	 *            {@link FileTreeModel} that represents the outcome of a diff
	 *            process. This model will have the nodes in the tree updated with
	 *            cumulative counts of diffs in children nodes
	 */
	public void postProcessFileTreeModel(final FileTreeModel fileTreeModel) {

		log.info("postProcessFileTreeModel()");

		if (fileTreeModel == null) {
			throw new IllegalArgumentException("null fileTreeModel");
		}

		FileTreeNode node = (FileTreeNode) fileTreeModel.getRoot();
		if (node == null) {
			return;
		}

		int countThisNode = countDiffsInChildren(node);
		FileTreeDiffEntry entry = (FileTreeDiffEntry) node.getUserObject();
		entry.setCountOfDiffsInChildren(countThisNode);

		log.info("processing complete, root node entry:{}", entry);

	}

	private int countDiffsInChildren(final FileTreeNode fileTreeNode) {
		log.info("processing node:{}", fileTreeNode);

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> nodeEnum = fileTreeNode.children();
		int count = 0;

		FileTreeNode childNode;
		FileTreeDiffEntry childEntry;

		while (nodeEnum.hasMoreElements()) {
			childNode = (FileTreeNode) nodeEnum.nextElement();
			log.info("childNode:{}", childNode);
			childEntry = (FileTreeDiffEntry) childNode.getUserObject();

			if (childNode.isLeaf()) {
				log.info("leaf node");
				if (childEntry.isCountAsDiff()) {
					count++;
				}
			} else {
				log.info("node is a dir, descend and count it up...");
				count += countDiffsInChildren(childNode);
			}
		}
		FileTreeDiffEntry thisEntry = (FileTreeDiffEntry) fileTreeNode.getUserObject();
		thisEntry.setCountOfDiffsInChildren(count);

		log.info("this entry count updated:{}", thisEntry);
		return count;
	}

}
