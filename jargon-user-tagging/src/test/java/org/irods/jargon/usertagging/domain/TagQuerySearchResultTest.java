package org.irods.jargon.usertagging.domain;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TagQuerySearchResultTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstance() throws Exception {
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		TagQuerySearchResult result = TagQuerySearchResult.instance("tags",
				entries);
		Assert.assertNotNull(result);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNoStrings() throws Exception {
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		TagQuerySearchResult.instance("", entries);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullStrings() throws Exception {
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		TagQuerySearchResult.instance(null, entries);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullEntries() throws Exception {
		TagQuerySearchResult.instance("xxxx", null);
	}

}
