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
		Assert.assertFalse("collection should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertFalse("data objects should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertEquals("total colls should be files and queries max",
				jargonProperties.getMaxFilesAndDirsQueryMax() * 2, actual
						.getPagingAwareCollectionListingDescriptor()
						.getTotalRecords());

	}

	@Test
	public void testPageForwardCols() throws Exception {
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
		entry.setTotalRecords(20000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(5000);
		entry.setTotalRecords(20000);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing firstPage = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);

		// now set up to page forward in collections
		Assert.assertNotNull("null first page", firstPage);
		// now take that data and page forward

		entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		entry = new CollectionAndDataObjectListingEntry();

		entry.setCount(5001);
		entry.setTotalRecords(20000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(10000);
		entry.setTotalRecords(20000);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 5000))
				.thenReturn(entries);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveNextPage(firstPage
						.getPagingAwareCollectionListingDescriptor());

		Assert.assertNotNull("null actual ", actual);
		Assert.assertFalse("collection should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertFalse("data objects should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertEquals("total colls", 20000, actual
				.getPagingAwareCollectionListingDescriptor().getTotalRecords());
		Assert.assertEquals("count", 10000, actual
				.getPagingAwareCollectionListingDescriptor().getCount());
		Assert.assertEquals("offset", 5001, actual
				.getPagingAwareCollectionListingDescriptor().getOffset());

	}

	@Test
	public void testPageForwardColsThenDataObjs() throws Exception {
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
		entry.setTotalRecords(15000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(4999);
		entry.setTotalRecords(15000);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing firstPage = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);

		// now set up to page forward in collections
		Assert.assertNotNull("null first page", firstPage);
		// now take that data and page forward

		entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		entry = new CollectionAndDataObjectListingEntry();

		entry.setCount(5000);
		entry.setTotalRecords(15000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(9999);
		entry.setTotalRecords(15000);
		entry.setLastResult(true);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 4999))
				.thenReturn(entries);

		List<CollectionAndDataObjectListingEntry> entries2 = new ArrayList<CollectionAndDataObjectListingEntry>();

		entry = new CollectionAndDataObjectListingEntry();

		entry.setCount(1);
		entry.setTotalRecords(5000);
		entry.setLastResult(false);
		entries2.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(5001);
		entry.setTotalRecords(5000);
		entry.setLastResult(false);
		entries2.add(entry);

		Mockito.when(
				collectionListingUtils.listDataObjectsUnderPath(objStat, 0))
				.thenReturn(entries2);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveNextPage(firstPage
						.getPagingAwareCollectionListingDescriptor());

		Assert.assertNotNull("null actual ", actual);

		actual = collectionPagerAO.retrieveNextPage(actual
				.getPagingAwareCollectionListingDescriptor());
		Assert.assertNotNull("data objects page was missing", actual);

	}

	@Test
	public void testPageForwardColsIntoDataObjects() throws Exception {
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
		entry.setTotalRecords(10000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(5000);
		entry.setTotalRecords(10000);
		entry.setLastResult(false);
		entries.add(entry);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(
				irodsSession, irodsAccount, collectionListingUtils);

		PagingAwareCollectionListing firstPage = collectionPagerAO
				.retrieveFirstPageUnderParent(parentPath);

		// now set up to page forward in collections
		Assert.assertNotNull("null first page", firstPage);
		// now take that data and page forward

		entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(5001);
		entry.setTotalRecords(10000);
		entry.setLastResult(false);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(10000);
		entry.setTotalRecords(10000);
		entry.setLastResult(true);
		entries.add(entry);

		List<CollectionAndDataObjectListingEntry> dataObjEntries = new ArrayList<CollectionAndDataObjectListingEntry>();
		CollectionAndDataObjectListingEntry dataObjEntry = new CollectionAndDataObjectListingEntry();

		dataObjEntry = new CollectionAndDataObjectListingEntry();
		dataObjEntry.setObjectType(ObjectType.DATA_OBJECT);
		dataObjEntry.setCount(1);
		dataObjEntry.setTotalRecords(2);
		dataObjEntry.setLastResult(false);
		dataObjEntries.add(dataObjEntry);

		dataObjEntry = new CollectionAndDataObjectListingEntry();
		dataObjEntry.setObjectType(ObjectType.DATA_OBJECT);
		dataObjEntry.setCount(2);
		dataObjEntry.setTotalRecords(2);
		dataObjEntry.setLastResult(true);
		dataObjEntries.add(dataObjEntry);

		Mockito.when(
				collectionListingUtils.listDataObjectsUnderPath(objStat, 0))
				.thenReturn(dataObjEntries);

		Mockito.when(
				collectionListingUtils.listCollectionsUnderPath(objStat, 0))
				.thenReturn(entries);

		PagingAwareCollectionListing actual = collectionPagerAO
				.retrieveNextPage(firstPage
						.getPagingAwareCollectionListingDescriptor());

		Assert.assertNotNull("null actual ", actual);
		Assert.assertTrue("collection should  be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());

		Assert.assertTrue("data objects should  be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());

	}

	@Test
	public void testRetriveFirstPageHasTwoPagesCollsSpecialColl()
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
		objStat.setSpecColType(SpecColType.MOUNTED_COLL);

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
		Assert.assertFalse("collection should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertFalse("data objects should not be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertEquals("total colls should be files and queries max",
				jargonProperties.getMaxFilesAndDirsQueryMax() * 2, actual
						.getPagingAwareCollectionListingDescriptor()
						.getTotalRecords());

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
		Assert.assertTrue("collection should be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertTrue("data objects should  be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());
		Assert.assertEquals("total data objs this page", 2, actual
				.getPagingAwareCollectionListingDescriptor()
				.getDataObjectsCount());
		Assert.assertEquals("total data objs should be 2", 2, actual
				.getPagingAwareCollectionListingDescriptor()
				.getDataObjectsTotalRecords());

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
		Assert.assertTrue("collection should be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isComplete());
		Assert.assertFalse("colls should not be empty", actual
				.getPagingAwareCollectionListingDescriptor().getCount() == 0);
		Assert.assertTrue("data objects should  be complete", actual
				.getPagingAwareCollectionListingDescriptor()
				.isDataObjectsComplete());

	}

}
