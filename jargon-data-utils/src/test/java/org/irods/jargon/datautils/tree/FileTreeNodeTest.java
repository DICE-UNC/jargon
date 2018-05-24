package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class FileTreeNodeTest {

	@Test
	public void testCreateNodeWithFileTreeDiffEntry() throws Exception {
		CollectionAndDataObjectListingEntry entry = Mockito.mock(CollectionAndDataObjectListingEntry.class);
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_PLUS, entry, "xx");
		FileTreeNode fileTreeNode = new FileTreeNode(diffEntry);
		Object userObj = fileTreeNode.getUserObject();
		boolean isFileTreeNode = (userObj instanceof FileTreeDiffEntry);
		Assert.assertTrue("user object not retrieved", isFileTreeNode);
	}

}
