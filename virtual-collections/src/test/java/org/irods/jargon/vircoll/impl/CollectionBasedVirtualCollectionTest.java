package org.irods.jargon.vircoll.impl;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
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

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection(
				"blah", testPath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setParentPath(testPath);
		entry.setPathOrName(testPath + "/" + subColl);
		entry.setTotalRecords(1);
		listing.getCollectionAndDataObjectListingEntries().add(entry);

		entry = new CollectionAndDataObjectListingEntry();
		entry.setCount(1);
		entry.setLastResult(true);
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setParentPath(testPath);
		entry.setPathOrName(dataName);
		entry.setTotalRecords(1);

		listing.getCollectionAndDataObjectListingEntries().add(entry);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(testPath))
				.thenReturn(listing);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);

		CollectionBasedVirtualCollectionExecutor executor = new CollectionBasedVirtualCollectionExecutor(
				virColl, irodsAccessObjectFactory, irodsAccount);

		PagingAwareCollectionListing actual = executor.queryAll(0);

		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.getCollectionAndDataObjectListingEntries()
				.isEmpty());

	}

}
