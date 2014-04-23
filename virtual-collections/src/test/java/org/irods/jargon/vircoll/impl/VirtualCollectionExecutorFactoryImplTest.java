package org.irods.jargon.vircoll.impl;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.vircoll.VirtualCollectionExecutorFactory;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;
import org.junit.Test;
import org.mockito.Mockito;

public class VirtualCollectionExecutorFactoryImplTest {

	@Test
	public void testExecutorFromCollectionBased() throws Exception {

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 147, "test",
				"test", "", "zone", "");
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionExecutorFactory factory = new VirtualCollectionExecutorFactoryImpl(
				irodsAccessObjectFactory, irodsAccount);
		CollectionBasedVirtualCollection collectionBasedVirtualCollection = new CollectionBasedVirtualCollection(
				"blah", "blah");
		CollectionBasedVirtualCollectionExecutor actual = (CollectionBasedVirtualCollectionExecutor) factory
				.instanceExecutorBasedOnVirtualCollection(collectionBasedVirtualCollection);
		Assert.assertNotNull(actual);

	}

	@Test
	public void testExecutorFromStarredBased() throws Exception {

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 147, "test",
				"test", "", "zone", "");
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		VirtualCollectionExecutorFactory factory = new VirtualCollectionExecutorFactoryImpl(
				irodsAccessObjectFactory, irodsAccount);
		StarredFoldersVirtualCollection virtualCollection = new StarredFoldersVirtualCollection();
		StarredFoldersVirtualCollectionExecutor actual = (StarredFoldersVirtualCollectionExecutor) factory
				.instanceExecutorBasedOnVirtualCollection(virtualCollection);
		Assert.assertNotNull(actual);

	}

}
