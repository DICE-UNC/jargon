package org.irods.jargon.datautils.shoppingcart;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileShoppingCartTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test
	public void testInstance() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		Assert.assertEquals("irodsAccount not set in new cart", irodsAccount,
				fileShoppingCart.getIrodsAccount());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullIRODSAccount() {
		FileShoppingCart.instance(null);
	}

	@Test
	public void testHasItemsNoItems() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		Assert.assertFalse("cart should have no items",
				fileShoppingCart.hasItems());
	}

	@Test
	public void testHasItemsWithItems() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		Assert.assertTrue("cart should have  items",
				fileShoppingCart.hasItems());
	}
	
	@Test
	public void testClear() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		fileShoppingCart.clearCart();
		Assert.assertFalse("cart should not have items after being cleared",
				fileShoppingCart.hasItems());
	}
	
	@Test
	public void testRemove() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		fileShoppingCart.removeAnItem(file);
		Assert.assertFalse("cart should not have items after delete",
				fileShoppingCart.hasItems());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRemoveNull() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		fileShoppingCart.removeAnItem(null);
	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRemoveBlank() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		fileShoppingCart.removeAnItem(null);
	
	}
	
	public void testRemoveNotInCart() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		fileShoppingCart.removeAnItem("this is not in the cart");
	}
	
	@Test
	public void testGetFilesInCartWithEntries() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		String file = "file";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(file);
		fileShoppingCart.addAnItem(entry);
		List<String> fileNames = fileShoppingCart.getShoppingCartFileList();
		TestCase.assertEquals("should be one file in cart", 1, fileNames.size());
		TestCase.assertEquals("wrong file name", file, fileNames.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNullEntryToCart() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		FileShoppingCart fileShoppingCart = FileShoppingCart
				.instance(irodsAccount);
		fileShoppingCart.addAnItem(null);
	}

}
