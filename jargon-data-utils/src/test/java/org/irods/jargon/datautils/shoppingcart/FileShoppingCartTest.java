package org.irods.jargon.datautils.shoppingcart;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

public class FileShoppingCartTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testInstance() {
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		Assert.assertNotNull("null shopping cart", fileShoppingCart);
	}

	@Test
	public void testHasItemsNoItems() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		Assert.assertFalse("cart should have no items",
				fileShoppingCart.hasItems());
	}

	@Test
	public void testHasItemsWithItems() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		Assert.assertTrue("cart should have  items",
				fileShoppingCart.hasItems());
	}

	@Test
	public void testClear() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		fileShoppingCart.clearCart();
		Assert.assertFalse("cart should not have items after being cleared",
				fileShoppingCart.hasItems());
	}

	@Test
	public void testRemove() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		fileShoppingCart.removeAnItem(file);
		Assert.assertFalse("cart should not have items after delete",
				fileShoppingCart.hasItems());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveNull() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.removeAnItem(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveBlank() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.removeAnItem(null);

	}

	public void testRemoveNotInCart() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.removeAnItem("this is not in the cart");
	}

	@Test
	public void testGetFilesInCartWithEntries() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		List<String> fileNames = fileShoppingCart.getShoppingCartFileList();
		Assert.assertEquals("should be one file in cart", 1, fileNames.size());
		Assert.assertEquals("wrong file name", file, fileNames.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNullEntryToCart() {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(null);
	}

	@Test
	public void testSerializeDeserializeCartWithEntries() throws Exception {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String file1 = "/a/path/to/file1";
		String file2 = "/another/path/to/file2";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file1);
		fileShoppingCart.addAnItem(entry);
		entry = ShoppingCartEntry.instance(file2);
		fileShoppingCart.addAnItem(entry);
		String serialized = fileShoppingCart
				.serializeShoppingCartContentsToStringOneItemPerLine();
		TestCase.assertFalse("null or empty serialized", serialized == null
				|| serialized.isEmpty());
		FileShoppingCart actual = FileShoppingCart
				.instanceFromSerializedStringRepresentation(serialized);
		TestCase.assertEquals("should be 2 files in cart", 2, actual
				.getShoppingCartFileList().size());

		TestCase.assertEquals("first file not found", file1, actual
				.getShoppingCartFileList().get(0));
		TestCase.assertEquals("second file not found", file2, actual
				.getShoppingCartFileList().get(1));

	}

	@Test
	public void testSerializeDeserializeEmptyCart() throws Exception {

		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String serialized = fileShoppingCart
				.serializeShoppingCartContentsToStringOneItemPerLine();

		FileShoppingCart actual = FileShoppingCart
				.instanceFromSerializedStringRepresentation(serialized);
		TestCase.assertFalse("should be empty cart", actual.hasItems());

	}

}
