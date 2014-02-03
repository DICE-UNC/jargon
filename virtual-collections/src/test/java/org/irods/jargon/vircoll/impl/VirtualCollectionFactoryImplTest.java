package org.irods.jargon.vircoll.impl;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.VirtualCollectionContext;
import org.irods.jargon.vircoll.VirtualCollectionContextImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class VirtualCollectionFactoryImplTest {

	@Test
	public void testListDefaultUserCollectionsTest() throws Exception {
		String userName = "userName";
		String zone = "zone";
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247,
				userName, "xxx", "", zone, "");
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionContext virtualCollectionContext = new VirtualCollectionContextImpl(
				irodsAccessObjectFactory, irodsAccount);

		VirtualCollectionFactory virtualCollectionFactory = new VirtualCollectionFactoryImpl(
				virtualCollectionContext);
		List<AbstractVirtualCollection> virtualCollections = virtualCollectionFactory
				.listDefaultUserCollections();
		Assert.assertNotNull(virtualCollections);
		Assert.assertEquals("should be 3", 3, virtualCollections.size());
		AbstractVirtualCollection vc1 = virtualCollections.get(0);
		Assert.assertTrue(vc1 instanceof CollectionBasedVirtualCollection);
		CollectionBasedVirtualCollection cbvc1 = (CollectionBasedVirtualCollection) vc1;
		Assert.assertEquals("wrong path for root coll", "/",
				cbvc1.getCollectionParentAbsolutePath());

		AbstractVirtualCollection vc2 = virtualCollections.get(1);
		Assert.assertTrue(vc2 instanceof CollectionBasedVirtualCollection);
		CollectionBasedVirtualCollection cbvc2 = (CollectionBasedVirtualCollection) vc2;
		Assert.assertEquals("wrong path for home coll", "/" + zone + "/home/"
				+ userName, cbvc2.getCollectionParentAbsolutePath());

		AbstractVirtualCollection vc3 = virtualCollections.get(2);
		Assert.assertTrue(vc3 instanceof StarredFoldersVirtualCollection);
		StarredFoldersVirtualCollection cbvc3 = (StarredFoldersVirtualCollection) vc3;
		Assert.assertEquals("not set to starred",
				"virtual.collections.starred", cbvc3.getDescription());

	}
}
