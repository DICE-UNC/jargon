package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class CollectionPagerAOImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test
	public void testRetriveFirstPageHasTwoPagesColls() throws Exception {
		String parentPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = Mockito.mock(IRODSSession.class);

		CollectionListingUtils collectionListingUtils = Mockito
				.mock(CollectionListingUtils.class);

		JargonProperties jargonProperties = new SettableJargonProperties();

		Mockito.when(irodsSession.getJargonProperties()).thenReturn(
				jargonProperties);

		/*
		 * Collection listing should return a set that emulates a count of max
		 * and total records 2x max and not last entry
		 */

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(parentPath);
		objStat.setObjectType(ObjectType.COLLECTION);
		objStat.setSpecColType(SpecColType.NORMAL);

		Mockito.when(
				collectionListingUtils.retrieveObjectStatForPath(parentPath))
				.thenReturn(objStat);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setTotalRecords(jargonProperties.getMaxFilesAndDirsQueryMax() * 2);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(jargonProperties.getMaxFilesAndDirsQueryMax() / 2);
		entry.setTotalRecords(jargonProperties.getMaxFilesAndDirsQueryMax() * 2);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);
		Assert.assertNotNull("null PagingAwareCollectionListing", actual);
		Assert.assertFalse("collection should not be complete",
				actual.isCollectionsComplete());
		Assert.assertFalse("data objects should not be complete",
				actual.isCollectionsComplete());
		Assert.assertEquals("total colls should be files and queries max",
				jargonProperties.getMaxFilesAndDirsQueryMax() * 2,
				actual.getTotalRecords());

	}

	@Test
	public void testRetriveFirstPageNoCollsOnePageDataObjects()
			throws Exception {
		String parentPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = Mockito.mock(IRODSSession.class);

		CollectionListingUtils collectionListingUtils = Mockito
				.mock(CollectionListingUtils.class);

		JargonProperties jargonProperties = new SettableJargonProperties();

		Mockito.when(irodsSession.getJargonProperties()).thenReturn(
				jargonProperties);

		/*
		 * Collection listing should return a set that emulates a count of max
		 * and total records 2x max and not last entry
		 */

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(parentPath);
		objStat.setObjectType(ObjectType.COLLECTION);
		objStat.setSpecColType(SpecColType.NORMAL);

		Mockito.when(
				collectionListingUtils.retrieveObjectStatForPath(parentPath))
				.thenReturn(objStat);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setCount(1);
		entry.setTotalRecords(2);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setCount(2);
		entry.setTotalRecords(2);
		entry.setLastResult(true);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(
						new ArrayList<CollectionAndDataObjectListingEntry>());

		Mockito.when(
				collectionListingUtils.listDataObjectsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);
		Assert.assertNotNull("null PagingAwareCollectionListing", actual);
		Assert.assertTrue("collection should be complete",
				actual.isCollectionsComplete());
		Assert.assertTrue("data objects should  be complete",
				actual.isDataObjectsComplete());
		Assert.assertEquals("total data objs this page", 2,
				actual.getDataObjectsCount());
		Assert.assertEquals("total data objs should be 2", 2,
				actual.getDataObjectsTotalRecords());

	}

	@Test
	public void testRetriveFirstPageLessThenMaxCollsTwoPageDataObjects()
			throws Exception {
		String parentPath = "/a/path";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = Mockito.mock(IRODSSession.class);

		CollectionListingUtils collectionListingUtils = Mockito
				.mock(CollectionListingUtils.class);

		JargonProperties jargonProperties = new SettableJargonProperties();

		Mockito.when(irodsSession.getJargonProperties()).thenReturn(
				jargonProperties);

		/*
		 * Collection listing should return a set that emulates a count of max
		 * and total records 2x max and not last entry
		 */

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(parentPath);
		objStat.setObjectType(ObjectType.COLLECTION);
		objStat.setSpecColType(SpecColType.NORMAL);

		Mockito.when(
				collectionListingUtils.retrieveObjectStatForPath(parentPath))
				.thenReturn(objStat);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setTotalRecords(jargonProperties.getMaxFilesAndDirsQueryMax() - 2);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(jargonProperties.getMaxFilesAndDirsQueryMax() / 2 - 2);
		entry.setTotalRecords(jargonProperties.getMaxFilesAndDirsQueryMax() - 2);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setCount(1);
		entry.setTotalRecords(2);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setCount(2);
		entry.setTotalRecords(2);
		entry.setLastResult(true);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listDataObjectsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);
		Assert.assertNotNull("null PagingAwareCollectionListing", actual);
		Assert.assertTrue("collection should be complete",
				actual.isCollectionsComplete());
		Assert.assertFalse("colls should not be empty", actual.getCount() == 0);
		Assert.assertTrue("data objects should  be complete",
				actual.isDataObjectsComplete());

	}

}
