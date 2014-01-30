package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.vircoll.VirtualCollectionContext;
import org.irods.jargon.vircoll.VirtualCollectionContextImpl;
import org.junit.Test;
import org.mockito.Mockito;

public class CollectionBasedVirtualCollectionTest {

	@Test
	public void testQueryAll() throws Exception {
		String testPath = "/a/collection/here";
		String subColl = "subcoll";
		String dataName = "data.txt";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionContext virtualCollectionContext = new VirtualCollectionContextImpl(
				irodsAccessObjectFactory, irodsAccount);

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection(
				testPath);
		virColl.setContext(virtualCollectionContext);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(testPath);
		entry.setPathOrName(testPath + "/" + subColl);
		entry.setTotalRecords(1);
		entries.add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setParentPath(testPath);
		entry.setPathOrName(dataName);
		entry.setTotalRecords(1);

		entries.add(entry);
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.listDataObjectsAndCollectionsUnderPath(testPath))
				.thenReturn(entries);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		List<CollectionAndDataObjectListingEntry> actual = virColl.queryAll(0);

		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.isEmpty());
		Assert.assertEquals(2, actual.size());
	}

	@Test
	public void testQueryCollections() throws Exception {
		String testPath = "/a/collection/here";
		String subColl = "subcoll";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionContext virtualCollectionContext = new VirtualCollectionContextImpl(
				irodsAccessObjectFactory, irodsAccount);

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection(
				testPath);
		virColl.setContext(virtualCollectionContext);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(testPath);
		entry.setPathOrName(testPath + "/" + subColl);
		entry.setTotalRecords(1);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.listCollectionsUnderPath(testPath, 0)).thenReturn(
				entries);
		entries.add(entry);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		List<CollectionAndDataObjectListingEntry> actual = virColl
				.queryCollections(0);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.isEmpty());

	}

	@Test
	public void testQueryDataObjects() throws Exception {
		String testPath = "/a/collection/here";
		String dataName = "data.txt";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionContext virtualCollectionContext = new VirtualCollectionContextImpl(
				irodsAccessObjectFactory, irodsAccount);

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection(
				testPath);
		virColl.setContext(virtualCollectionContext);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setParentPath(testPath);
		entry.setPathOrName(dataName);
		entry.setTotalRecords(1);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		entries.add(entry);
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.listDataObjectsUnderPath(testPath, 0)).thenReturn(
				entries);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		List<CollectionAndDataObjectListingEntry> actual = virColl
				.queryDataObjects(0);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.isEmpty());

	}

}
