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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class CollectionPagerAOImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@Test // FIXME: delete when ignore removed
	public void dummy() {

	}

	@Ignore // FIXME: fix test and turn of ignore
	public void testRetriveFirstPageHasTwoPagesColls() throws Exception {
		final String parentPath = "/a/path";
		final IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		final IRODSSession irodsSession = Mockito.mock(IRODSSession.class);

		final CollectionListingUtils collectionListingUtils = Mockito.mock(CollectionListingUtils.class);

		final JargonProperties jargonProperties = new SettableJargonProperties();

		Mockito.when(irodsSession.getJargonProperties()).thenReturn(jargonProperties);

		/*
		 * Collection listing should return a set that emulates a count of max
		 * and total records 2x max and not last entry
		 */

		final ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(parentPath);
		objStat.setObjectType(ObjectType.COLLECTION);
		objStat.setSpecColType(SpecColType.NORMAL);

		Mockito.when(collectionListingUtils.retrieveObjectStatForPath(parentPath)).thenReturn(objStat);

		final List<CollectionAndDataObjectListingEntry> entries = new ArrayList<>();

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

		Mockito.when(collectionListingUtils.listCollectionsUnderPath(objStat, 0)).thenReturn(entries);

		final CollectionPagerAO collectionPagerAO = new CollectionPagerAOImpl(irodsSession, irodsAccount);

		final PagingAwareCollectionListing actual = collectionPagerAO.retrieveFirstPageUnderParent(parentPath);
		Assert.assertNotNull("null PagingAwareCollectionListing", actual);
		Assert.assertFalse("collection should not be complete",
				actual.getPagingAwareCollectionListingDescriptor().isCollectionsComplete());
		Assert.assertFalse("data objects should not be complete",
				actual.getPagingAwareCollectionListingDescriptor().isDataObjectsComplete());
		Assert.assertEquals("total colls should be files and queries max",
				jargonProperties.getMaxFilesAndDirsQueryMax() * 2,
				actual.getPagingAwareCollectionListingDescriptor().getTotalRecords());

	}

}
