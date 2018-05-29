package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.junit.Assert;
import org.junit.Test;

public class DiffTreePostProcessorTest {

	@Test
	public void testTreeWithMultipleLevelsMixOfLeafAndDirNodesWithDiffs() throws Exception {
		CollectionAndDataObjectListingEntry rootEntry = new CollectionAndDataObjectListingEntry();
		rootEntry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		rootEntry.setParentPath("/root");
		rootEntry.setPathOrName("/root/rootChild");
		FileTreeDiffEntry root = FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, rootEntry, "xxx");
		FileTreeNode rootNode = new FileTreeNode(root);

		FileTreeModel model = new FileTreeModel(rootNode);

		// add two child dirs to that root as no diffs

		CollectionAndDataObjectListingEntry c1Entry = new CollectionAndDataObjectListingEntry();
		c1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c1Entry.setParentPath("/root/rootChild");
		c1Entry.setPathOrName("/root/rootChild/c1");
		FileTreeDiffEntry c1DiffEntry = FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c1Entry, "xxx");
		FileTreeNode c1Node = new FileTreeNode(c1DiffEntry);
		rootNode.add(c1Node);

		CollectionAndDataObjectListingEntry c2Entry = new CollectionAndDataObjectListingEntry();
		c2Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c2Entry.setParentPath("/root/rootChild");
		c2Entry.setPathOrName("/root/rootChild/c2");
		FileTreeDiffEntry c2DiffEntry = FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c2Entry, "xx");
		FileTreeNode c2Node = new FileTreeNode(c2DiffEntry);
		rootNode.add(c2Node);

		// add a node that will have some diffs

		CollectionAndDataObjectListingEntry c3Entry = new CollectionAndDataObjectListingEntry();
		c3Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c3Entry.setParentPath("/root/rootChild");
		c3Entry.setPathOrName("/root/rootChild/c3");
		FileTreeDiffEntry c3DiffEntry = FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c3Entry, "xx");
		FileTreeNode c3Node = new FileTreeNode(c3DiffEntry);
		rootNode.add(c3Node);

		// add a file diff to c3

		CollectionAndDataObjectListingEntry c3f1Entry = new CollectionAndDataObjectListingEntry();
		c3f1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
		c3f1Entry.setParentPath("/root/rootChild/c3");
		c3f1Entry.setPathOrName("/root/rootChild/c3/d1.txt");
		FileTreeDiffEntry c3f1DiffEntry = FileTreeDiffEntry.instance(DiffType.FILE_OUT_OF_SYNCH, c3f1Entry, "xx");
		FileTreeNode c3f1Node = new FileTreeNode(c3f1DiffEntry);
		c3Node.add(c3f1Node);

		// add a child subdir of c3 that will have 2 diff files

		CollectionAndDataObjectListingEntry c3c1Entry = new CollectionAndDataObjectListingEntry();
		c3c1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c3c1Entry.setParentPath("/root/rootChild/c3");
		c3c1Entry.setPathOrName("/root/rootChild/c3/c1");
		FileTreeDiffEntry c3c1DiffEntry = FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c3c1Entry, "xx");
		FileTreeNode c3c1Node = new FileTreeNode(c3c1DiffEntry);
		c3Node.add(c3c1Node);

		// add 2 file diffs to c3c1

		CollectionAndDataObjectListingEntry c3c1f1Entry = new CollectionAndDataObjectListingEntry();
		c3c1f1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
		c3c1f1Entry.setParentPath("/root/rootChild/c3/c1");
		c3c1f1Entry.setPathOrName("/root/rootChild/c3/c1/d1.txt");
		FileTreeDiffEntry c3c1f1DiffEntry = FileTreeDiffEntry.instance(DiffType.FILE_OUT_OF_SYNCH, c3c1f1Entry, "xx");
		FileTreeNode c3c1f1Node = new FileTreeNode(c3c1f1DiffEntry);
		c3c1Node.add(c3c1f1Node);

		CollectionAndDataObjectListingEntry c3c1f2Entry = new CollectionAndDataObjectListingEntry();
		c3c1f2Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
		c3c1f2Entry.setParentPath("/root/rootChild/c3/c1");
		c3c1f2Entry.setPathOrName("/root/rootChild/c3/c1/d2.txt");
		FileTreeDiffEntry c3c1f2DiffEntry = FileTreeDiffEntry.instance(DiffType.FILE_OUT_OF_SYNCH, c3c1f2Entry, "xx");
		FileTreeNode c3c1f2Node = new FileTreeNode(c3c1f2DiffEntry);
		c3c1Node.add(c3c1f2Node);

		// post proc the tree

		DiffTreePostProcessor processor = new DiffTreePostProcessor();
		processor.postProcessFileTreeModel(model);

		// check out the tree

		Assert.assertEquals("root node needs 3 diffs", 3, root.getCountOfDiffsInChildren());

		Assert.assertEquals("c1 under root no diffs", 0, c1DiffEntry.getCountOfDiffsInChildren(), 0);

		Assert.assertEquals("c3 under root 3 diffs", 3, c3DiffEntry.getCountOfDiffsInChildren());

		Assert.assertEquals("c1 under c3 2 diffs", 2, c3c1DiffEntry.getCountOfDiffsInChildren());

	}
}
