package org.irods.jargon.vircoll.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.vircoll.VirtualCollectionContext;
import org.irods.jargon.vircoll.VirtualCollectionContextImpl;
import org.junit.Test;
import org.mockito.Mockito;

public class StarredFoldersVirtualCollectionImplTest {

	@Test
	public void testQueryCollections() throws Exception {
		/*
		String testPath = "/a/collection/here";
		String descr = "test";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionContext virtualCollectionContext = new VirtualCollectionContextImpl(
				irodsAccessObjectFactory, irodsAccount);

		CollectionBasedVirtualCollection virColl = new CollectionBasedVirtualCollection();
		virColl.setContext(virtualCollectionContext);
		virColl.setCollectionParentAbsolutePath("/");

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);

		List<IRODSStarredFileOrCollection> results = new ArrayList<IRODSStarredFileOrCollection>();
		IRODSStarredFileOrCollection starred = new IRODSStarredFileOrCollection(
				MetadataDomain.COLLECTION, testPath, descr, "bob");
		results.add(starred);
		
		Mockito.when(collection)
*/
	}
}
