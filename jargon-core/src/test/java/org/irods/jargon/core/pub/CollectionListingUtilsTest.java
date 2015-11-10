package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionListingUtilsTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionListingUtilsTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static JargonProperties jargonOriginalProperties = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
		.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		jargonOriginalProperties = settableJargonProperties;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		// be sure that normal parallel stuff is set up
		irodsFileSystem.getIrodsSession().setJargonProperties(
				jargonOriginalProperties);
	}

	@Test
	public void testHandleNoObjStatUnderRootOrHomeByLookingForPublicAndHome()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		CollectionListingUtils listingUtils = new CollectionListingUtils(
				collectionAndDataObjectListAndSearchAO);

		String path = "/";
		ObjStat objStat = listingUtils
				.handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(path);
		Assert.assertNotNull(objStat);
		Assert.assertTrue(objStat.isStandInGeneratedObjStat());
		Assert.assertEquals(path, objStat.getAbsolutePath());

	}

	@Test
	public void testHandleNoObjStatUnderRootOrHomeByLookingForPublicAndHomeZoneNameValid()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		CollectionListingUtils listingUtils = new CollectionListingUtils(
				collectionAndDataObjectListAndSearchAO);

		String path = "/" + irodsAccount.getZone();
		ObjStat objStat = listingUtils
				.handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(path);
		Assert.assertNotNull(objStat);
		Assert.assertTrue(objStat.isStandInGeneratedObjStat());
		Assert.assertEquals(path, objStat.getAbsolutePath());

	}

	@Test
	public void testHandleNoObjStatUnderRootOrHomeByLookingForPublicAndHomeZoneAndHome()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		CollectionListingUtils listingUtils = new CollectionListingUtils(
				collectionAndDataObjectListAndSearchAO);

		String path = "/" + irodsAccount.getZone() + "/home";
		ObjStat objStat = listingUtils
				.handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(path);
		Assert.assertNotNull(objStat);
		Assert.assertTrue(objStat.isStandInGeneratedObjStat());
		Assert.assertEquals(path, objStat.getAbsolutePath());

	}

	@Test(expected = FileNotFoundException.class)
	public void testHandleNoObjStatUnderRootOrHomeByLookingForPublicAndHomeZoneNameInvalid()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		props.setDefaultToPublicIfNothingUnderRootWhenListing(true);
		irodsFileSystem.getIrodsSession().setJargonProperties(props);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		CollectionListingUtils listingUtils = new CollectionListingUtils(
				collectionAndDataObjectListAndSearchAO);

		String path = "/" + irodsAccount.getZone() + "imnotazonebub";
		ObjStat objStat = listingUtils
				.handleNoObjStatUnderRootOrHomeByLookingForPublicAndHome(path);
		Assert.assertNotNull(objStat);
		Assert.assertTrue(objStat.isStandInGeneratedObjStat());
		Assert.assertEquals(path, objStat.getAbsolutePath());

	}
}
