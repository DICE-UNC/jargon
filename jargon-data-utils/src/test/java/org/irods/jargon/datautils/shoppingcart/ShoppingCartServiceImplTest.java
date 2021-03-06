package org.irods.jargon.datautils.shoppingcart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactory;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShoppingCartServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@Test
	public final void testSerializeShoppingCartAsLoggedInUser() throws Exception {
		String key = "key";
		String expectedPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath));
		String actual = shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		Assert.assertNotNull("null path returned, no serializing of cart", actual);

	}

	@Test
	public final void testSerializeAndRetrieveEmptyShoppingCartAsLoggedInUser() throws Exception {
		String key = "key";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String actual = shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		Assert.assertNotNull("null path returned, no serializing of cart", actual);
		FileShoppingCart actualCart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertNotNull("actual cart is null", actualCart);

	}

	@Test
	public final void testSerializeEmptyShoppingCartAsLoggedInUser() throws Exception {
		String key = "key";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);

	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public final void testSerializeCartNoDataCacheService() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = null;
		new ShoppingCartServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);

	}

	@Test
	public final void testSerializeDeserializeShoppingCartAsLoggedInUser() throws Exception {
		String key = "key";
		String expectedPath1 = "/a/path";
		String expectedPath2 = "/a/path2";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath1));
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath2));
		shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		FileShoppingCart cart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertTrue("no files in cart", cart.hasItems());

	}

	@Test
	public final void testRemoveItemsFromCart() throws Exception {
		String key = "key";
		String expectedPath1 = "/a/path";
		String expectedPath2 = "/a/path2";
		String expectedPath3 = "/a/path3";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath1));
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath2));
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath3));
		shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);

		// remove items 1 and 3

		List<String> itemsToRemove = new ArrayList<>();
		itemsToRemove.add(expectedPath1);
		itemsToRemove.add(expectedPath3);
		shoppingCartService.removeSpecifiedItemsFromShoppingCart(key, itemsToRemove);

		FileShoppingCart cart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertEquals("did not delete expected items", 1, cart.getShoppingCartFileList().size());
		String itemLeft = cart.getShoppingCartFileList().get(0);
		Assert.assertEquals("did not get expected item", expectedPath2, itemLeft);
	}

	@Test
	public final void testAddItemToCartWithADuplicate() throws Exception {
		String key = "key";
		String expectedPath1 = "/a/path";
		String expectedPath2 = "/a/path2";
		String expectedPath3 = "/a/path3";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath1));
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath2));
		shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		String[] fileList = { expectedPath3, expectedPath3 };
		shoppingCartService.appendToShoppingCart(key, new ArrayList<String>(Arrays.asList(fileList)));

		FileShoppingCart cart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertTrue("no files in cart", cart.hasItems());
		Assert.assertEquals("should be 3 files", 3, cart.getShoppingCartFileList().size());

	}

	/**
	 * Create a valid cart for another user as rods admin and then attempt to access
	 * as that target user.
	 *
	 * @throws Exception
	 */
	@Test
	public final void testSerializeShoppingCartAsSpecifiedUserAsRodsadmin() throws Exception {
		String testUserName = testingProperties.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);
		String key = "key";
		String expectedPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 3.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount, dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath));
		String tempPassword = shoppingCartService.serializeShoppingCartAsSpecifiedUser(fileShoppingCart, key,
				testUserName);
		// log in as the other user with the temp password and retrieve
		IRODSAccount tempUserAccount = IRODSAccount.instance(irodsAccount.getHost(), irodsAccount.getPort(),
				testUserName, tempPassword, "", irodsAccount.getZone(), irodsAccount.getDefaultStorageResource());

		shoppingCartService = new ShoppingCartServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(),
				tempUserAccount, dataCacheServiceFactory);
		FileShoppingCart cart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertTrue("no files in cart", cart.hasItems());

	}

}
