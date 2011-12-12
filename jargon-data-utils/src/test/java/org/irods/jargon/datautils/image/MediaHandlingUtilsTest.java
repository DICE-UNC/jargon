package org.irods.jargon.datautils.image;

import junit.framework.TestCase;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MediaHandlingUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIsImageFile() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.jpg");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isImageFile(entry);
		TestCase.assertTrue("did not identify as image", actual);
	}

	@Test
	public final void testIsImageFileWhenDoc() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.doc");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isImageFile(entry);
		TestCase.assertFalse("should not identify as image", actual);
	}

	@Test
	public final void testIsImageFileWhenCollection() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.doc");
		entry.setObjectType(ObjectType.COLLECTION);
		boolean actual = MediaHandlingUtils.isImageFile(entry);
		TestCase.assertFalse("should not identify collection as image", actual);
	}

	@Test
	public final void testIsImageFileWhenNull() {
		CollectionAndDataObjectListingEntry entry = null;
		boolean actual = MediaHandlingUtils.isImageFile(entry);
		TestCase.assertFalse("should not identify collection as image", actual);
	}

	@Test
	public final void testIsMediaFileWhenDoc() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.doc");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isMediaFile(entry);
		TestCase.assertFalse("should not identify as media", actual);
	}

	@Test
	public final void testIsMediaFileQuicktime() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.mp4");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isMediaFile(entry);
		TestCase.assertTrue("did not identify as media", actual);
	}

	@Test
	public final void testIsMediaFileFlash() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.mp3");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isMediaFile(entry);
		TestCase.assertTrue("did not identify as media", actual);
	}

	@Test
	public final void testIsMediaFileWindows() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.avi");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isMediaFile(entry);
		TestCase.assertTrue("did not identify as media", actual);
	}

	@Test
	public final void testIsMediaFileIframe() {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setPathOrName("image.pdf");
		entry.setObjectType(ObjectType.DATA_OBJECT);
		boolean actual = MediaHandlingUtils.isMediaFile(entry);
		TestCase.assertTrue("did not identify as media", actual);
	}

}
