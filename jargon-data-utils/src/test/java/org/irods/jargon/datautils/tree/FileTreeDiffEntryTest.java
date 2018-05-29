package org.irods.jargon.datautils.tree;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FileTreeDiffEntryTest {

	@Test
	public void testInstance() throws Exception {
		CollectionAndDataObjectListingEntry entry = Mockito.mock(CollectionAndDataObjectListingEntry.class);
		FileTreeDiffEntry diffEntry = FileTreeDiffEntry.instance(DiffType.LEFT_HAND_PLUS, entry, "xxx", 0, 0);
		Assert.assertNotNull("null diffEntry", diffEntry);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullDiffType() throws Exception {
		CollectionAndDataObjectListingEntry entry = Mockito.mock(CollectionAndDataObjectListingEntry.class);
		FileTreeDiffEntry.instance(null, entry, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullCollEntry() throws Exception {
		FileTreeDiffEntry.instance(DiffType.RIGHT_HAND_PLUS, null, "");
	}

}
