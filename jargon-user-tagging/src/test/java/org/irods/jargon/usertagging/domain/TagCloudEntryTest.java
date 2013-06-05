package org.irods.jargon.usertagging.domain;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TagCloudEntryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testInstance() throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag", "user");
		TagCloudEntry tagCloudEntry = new TagCloudEntry(irodsTagValue, 1, 1);
		Assert.assertEquals(irodsTagValue, tagCloudEntry.getIrodsTagValue());
		Assert.assertEquals(1, tagCloudEntry.getCountOfFiles());
		Assert.assertEquals(1, tagCloudEntry.getCountOfCollections());
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullIrodsTagValue() throws Exception {
		IRODSTagValue irodsTagValue = null;
		new TagCloudEntry(irodsTagValue, 1, 1);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNegativeCountFiles() throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag", "user");
		new TagCloudEntry(irodsTagValue, -1, 1);
	}

	@Test(expected = JargonException.class)
	public void testInstanceNegativeCountCollections() throws Exception {
		IRODSTagValue irodsTagValue = new IRODSTagValue("tag", "user");
		new TagCloudEntry(irodsTagValue, 1, -1);
	}

}
