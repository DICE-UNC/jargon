package org.irods.jargon.datautils.shoppingcart;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactory;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactoryImpl;
import org.irods.jargon.testutils.TestingPropertiesHelper;
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
	public final void testSerializeShoppingCartAsLoggedInUser()
			throws Exception {
		String key = "key";
		String expectedPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath));
		String actual = shoppingCartService
				.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		Assert.assertNotNull("null path returned, no serializing of cart",
				actual);

	}

	@Test
	public final void testSerializeEmptyShoppingCartAsLoggedInUser()
			throws Exception {
		String key = "key";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		String actual = shoppingCartService
				.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		Assert.assertNotNull("null path returned, no serializing of cart",
				actual);

	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public final void testSerializeCartNoDataCacheService() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = null;
		new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);

	}

	@Test(expected = JargonRuntimeException.class)
	public final void testBuildWithDefaultConstructorNoCacheServiceSet()
			throws Exception {
		String key = "key";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl();
		shoppingCartService.setDataCacheServiceFactory(null);
		shoppingCartService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		shoppingCartService.setIrodsAccount(irodsAccount);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		shoppingCartService.serializeShoppingCartAsLoggedInUser(
				fileShoppingCart, key);

	}

	@Test
	public final void testSerializeDeserializeShoppingCartAsLoggedInUser()
			throws Exception {
		String key = "key";
		String expectedPath1 = "/a/path";
		String expectedPath2 = "/a/path2";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath1));
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath2));
		shoppingCartService.serializeShoppingCartAsLoggedInUser(
				fileShoppingCart, key);
		FileShoppingCart cart = shoppingCartService
				.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertTrue("no files in cart", cart.hasItems());

	}

	/**
	 * Create a valid cart for another user as rods admin and then attempt to
	 * access as that target user.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testSerializeShoppingCartAsSpecifiedUserAsRodsadmin()
			throws Exception {
		String testUserName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);
		String key = "key";
		String expectedPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 3.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());

		ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();
		fileShoppingCart.addAnItem(ShoppingCartEntry.instance(expectedPath));
		String tempPassword = shoppingCartService
				.serializeShoppingCartAsSpecifiedUser(fileShoppingCart, key,
						testUserName);
		// log in as the other user with the temp password and retrieve
		IRODSAccount tempUserAccount = IRODSAccount.instance(
				irodsAccount.getHost(), irodsAccount.getPort(), testUserName,
				tempPassword, "", irodsAccount.getZone(),
				irodsAccount.getDefaultStorageResource());

		shoppingCartService = new ShoppingCartServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), tempUserAccount,
				dataCacheServiceFactory);
		FileShoppingCart cart = shoppingCartService
				.retreiveShoppingCartAsLoggedInUser(key);
		Assert.assertTrue("no files in cart", cart.hasItems());

	}

}
