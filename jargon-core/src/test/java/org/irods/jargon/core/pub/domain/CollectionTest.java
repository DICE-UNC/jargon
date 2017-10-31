package org.irods.jargon.core.pub.domain;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testGetCollectionLastPathComponent() {
		String path = "a/path/to/a/collection";
		Collection collection = new Collection();
		collection.setCollectionName(path);
		Assert.assertEquals("collection",
				collection.getCollectionLastPathComponent());
	}

}
