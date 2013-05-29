package org.irods.jargon.datautils.tree;

import static org.junit.Assert.*;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.junit.Test;

public class DiffTreePostProcessorTest {

	@Test
	public void testTreeWithMultipleLevelsMixOfLeafAndDirNodesWithDiffs() throws Exception {
		CollectionAndDataObjectListingEntry rootEntry = new CollectionAndDataObjectListingEntry();
		rootEntry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		rootEntry.setParentPath("/root");
		rootEntry.setPathOrName("/root/rootChild");
		FileTreeDiffEntry root =  FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, rootEntry);
		FileTreeNode rootNode = new FileTreeNode(root);
		
		FileTreeModel model = new FileTreeModel(rootNode);
		
		
		// add two child dirs to that root as no diffs
		
		CollectionAndDataObjectListingEntry c1Entry = new CollectionAndDataObjectListingEntry();
		c1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c1Entry.setParentPath("/root/rootChild");
		c1Entry.setPathOrName("/root/rootChild/c1");
		FileTreeDiffEntry c1DiffEntry =  FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c1Entry);
		FileTreeNode c1Node = new FileTreeNode(c1DiffEntry);
		rootNode.add(c1Node);
		
		CollectionAndDataObjectListingEntry c2Entry = new CollectionAndDataObjectListingEntry();
		c2Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c2Entry.setParentPath("/root/rootChild");
		c2Entry.setPathOrName("/root/rootChild/c2");
		FileTreeDiffEntry c2DiffEntry =  FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c2Entry);
		FileTreeNode c2Node = new FileTreeNode(c2DiffEntry);
		rootNode.add(c2Node);
		
		// add a node that will have some diffs
		
		CollectionAndDataObjectListingEntry c3Entry = new CollectionAndDataObjectListingEntry();
		c3Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
		c3Entry.setParentPath("/root/rootChild");
		c3Entry.setPathOrName("/root/rootChild/c3");
		FileTreeDiffEntry c3DiffEntry =  FileTreeDiffEntry.instance(DiffType.DIRECTORY_NO_DIFF, c3Entry);
		FileTreeNode c3Node = new FileTreeNode(c3DiffEntry);
		rootNode.add(c3Node);
		
		// add a file diff to c3
		
		CollectionAndDataObjectListingEntry c3f1Entry = new CollectionAndDataObjectListingEntry();
		c3f1Entry.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT);
		c3f1Entry.setParentPath("/root/rootChild/c3");
		c3f1Entry.setPathOrName("/root/rootChild/c3/d1.txt");
		FileTreeDiffEntry c3f1DiffEntry =  FileTreeDiffEntry.instance(DiffType.FILE_OUT_OF_SYNCH, c3f1Entry);
		FileTreeNode c3f1Node = new FileTreeNode(c3f1DiffEntry);
		c3Node.add(c3f1Node);
		
		// add a child subdir of c3 that will have 2 diff files
		
		
		
		
		
		
		
	}

}
