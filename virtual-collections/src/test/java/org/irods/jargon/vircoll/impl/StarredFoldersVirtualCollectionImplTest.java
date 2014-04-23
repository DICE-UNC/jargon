package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;
import org.junit.Test;
import org.mockito.Mockito;

public class StarredFoldersVirtualCollectionImplTest {

	@Test
	public void testQueryCollections() throws Exception {

		String testPath = "/a/collection/here";
		String descr = "test";
		IRODSAccount irodsAccount = Mockito.mock(IRODSAccount.class);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		JargonProperties jargonProperties = new SettableJargonProperties();
		Mockito.when(irodsAccessObjectFactory.getJargonProperties())
				.thenReturn(jargonProperties);

		IRODSStarringService irodsStarringService = Mockito
				.mock(IRODSStarringService.class);

		StarredFoldersVirtualCollection virColl = new StarredFoldersVirtualCollection();

		StarredFoldersVirtualCollectionExecutor executor = new StarredFoldersVirtualCollectionExecutor(
				virColl, irodsAccessObjectFactory, irodsAccount,
				irodsStarringService);

		List<IRODSStarredFileOrCollection> results = new ArrayList<IRODSStarredFileOrCollection>();
		IRODSStarredFileOrCollection starred = new IRODSStarredFileOrCollection(
				MetadataDomain.COLLECTION, testPath, descr, "bob");
		results.add(starred);

		Mockito.when(irodsStarringService.listStarredCollections(0))
				.thenReturn(results);

		PagingAwareCollectionListing actual = executor.queryAll(0);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.getCollectionAndDataObjectListingEntries()
				.isEmpty());
		CollectionAndDataObjectListingEntry actualEntry = actual
				.getCollectionAndDataObjectListingEntries().get(0);
		CollectionAndPath cp = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(testPath);
		Assert.assertEquals(cp.getCollectionParent(),
				actualEntry.getParentPath());
		Assert.assertEquals(testPath, actualEntry.getPathOrName());
		Assert.assertEquals(
				CollectionAndDataObjectListingEntry.ObjectType.COLLECTION,
				actualEntry.getObjectType());

	}
}
