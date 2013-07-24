package org.irods.jargon.usertagging.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserTagCloudViewTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstanceFromTreeMap() throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag", "user");
		TagCloudEntry tagCloudEntry = new TagCloudEntry(irodsTagValue, 1, 0);

		TreeMap<IRODSTagValue, TagCloudEntry> tagCloudEntries = new TreeMap<IRODSTagValue, TagCloudEntry>();
		tagCloudEntries.put(irodsTagValue, tagCloudEntry);

		UserTagCloudView view = UserTagCloudView.instance("user",
				tagCloudEntries);
		Assert.assertEquals("user", view.getUserName());
		Assert.assertEquals(1, tagCloudEntries.keySet().size());
	}

	@Test
	public void testInstanceFromListOfTwoTagsWithAFileAndCollectionEach()
			throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag1", "user");
		TagCloudEntry entryFile1 = new TagCloudEntry(irodsTagValue, 1, 0);

		IRODSTagValue irodsTagValue2 = new IRODSTagValue("tag2", "user");
		TagCloudEntry entryFile2 = new TagCloudEntry(irodsTagValue2, 1, 0);

		TagCloudEntry entryCollection1 = new TagCloudEntry(irodsTagValue, 0, 1);

		TagCloudEntry entryCollection2 = new TagCloudEntry(irodsTagValue2, 0, 1);

		List<TagCloudEntry> fileTags = new ArrayList<TagCloudEntry>();
		fileTags.add(entryFile1);
		fileTags.add(entryFile2);

		List<TagCloudEntry> collTags = new ArrayList<TagCloudEntry>();
		collTags.add(entryCollection1);
		collTags.add(entryCollection2);

		UserTagCloudView userTagCloudView = UserTagCloudView.instance("user",
				fileTags, collTags);
		Assert.assertEquals("user", userTagCloudView.getUserName());
		Assert.assertEquals(2, userTagCloudView.getTagCloudEntries().keySet()
				.size());

	}

	@Test
	public void testSortingOfThreeDifferentFileTags() throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag1", "user");
		TagCloudEntry entryFile1 = new TagCloudEntry(irodsTagValue, 1, 0);

		IRODSTagValue irodsTagValue2 = new IRODSTagValue("tag2", "user");
		TagCloudEntry entryFile2 = new TagCloudEntry(irodsTagValue2, 1, 0);

		IRODSTagValue irodsTagValue3 = new IRODSTagValue("tag3", "user");
		TagCloudEntry entryFile3 = new TagCloudEntry(irodsTagValue3, 1, 0);

		List<TagCloudEntry> fileTags = new ArrayList<TagCloudEntry>();
		fileTags.add(entryFile1);
		fileTags.add(entryFile3);
		fileTags.add(entryFile2);

		List<TagCloudEntry> collTags = new ArrayList<TagCloudEntry>();

		UserTagCloudView userTagCloudView = UserTagCloudView.instance("user",
				fileTags, collTags);
		Assert.assertEquals("user", userTagCloudView.getUserName());
		Assert.assertEquals(3, userTagCloudView.getTagCloudEntries().keySet()
				.size());

		// test sorting by getting 3 tags in turn
		Set<IRODSTagValue> actualEntries = userTagCloudView
				.getTagCloudEntries().keySet();

		Iterator<IRODSTagValue> testIter = actualEntries.iterator();
		IRODSTagValue actual1 = testIter.next();
		IRODSTagValue actual2 = testIter.next();
		IRODSTagValue actual3 = testIter.next();

		Assert.assertEquals(irodsTagValue, actual1);
		Assert.assertEquals(irodsTagValue2, actual2);
		Assert.assertEquals(irodsTagValue3, actual3);
	}

}
