package org.irods.jargon.dataprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAOImpl;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataProfileServiceImplTest {

	@Test
	public void testRetrieveDataProfileDataObject() throws Exception {

		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DataTypeResolutionService resolutionService = new DataTypeResolutionServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		DataObject dataObject = new DataObject();
		String dataName = "file.txt";
		dataObject.setDataName(dataName);

		MetaDataAndDomainData metaDataAndDamainData = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", "blah", 0,
						"application/xml", "",
						UserTaggingConstants.MIME_TYPE_AVU_UNIT);
		List<MetaDataAndDomainData> avus = new ArrayList<MetaDataAndDomainData>();
		avus.add(metaDataAndDamainData);

		MetaDataAndDomainData starred = MetaDataAndDomainData.instance(
				MetadataDomain.DATA, "1", "blah", 0, "l",
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);

		avus.add(starred);

		MetaDataAndDomainData shared = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", "blah", 0, "l",
						irodsAccount.getUserName(),
						UserTaggingConstants.SHARE_AVU_UNIT);

		avus.add(shared);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAOImpl.class);

		ObjStat objStat = new ObjStat();
		objStat.setObjectType(ObjectType.DATA_OBJECT);
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(dataName)).thenReturn(
				objStat);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);

		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		Mockito.when(dataObjectAO.findByAbsolutePath(dataName)).thenReturn(
				dataObject);

		Mockito.when(dataObjectAO.findMetadataValuesForDataObject(dataName))
				.thenReturn(avus);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		DataProfileService dataProfileService = new DataProfileServiceImpl(
				irodsAccessObjectFactory, irodsAccount, resolutionService);

		@SuppressWarnings("unchecked")
		DataProfile<DataObject> actual = dataProfileService
				.retrieveDataProfile(dataName);
		Assert.assertNotNull("null dataProfle", actual);
		Assert.assertNotNull("no data object", actual.getDomainObject());
		Assert.assertTrue("should be file", actual.isFile());
		Assert.assertTrue("shoudl be starred", actual.isStarred());
		Assert.assertTrue("should be shared", actual.isShared());

	}

	@Test
	public void testRetrieveDataProfileDataObjectWithTags() throws Exception {

		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DataTypeResolutionService resolutionService = new DataTypeResolutionServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		DataObject dataObject = new DataObject();
		String dataName = "file.txt";
		dataObject.setDataName(dataName);

		MetaDataAndDomainData metaDataAndDamainData = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", "blah", 0, "hi",
						irodsAccount.getUserName(),
						UserTaggingConstants.TAG_AVU_UNIT);
		List<MetaDataAndDomainData> avus = new ArrayList<MetaDataAndDomainData>();
		avus.add(metaDataAndDamainData);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAOImpl.class);

		ObjStat objStat = new ObjStat();
		objStat.setObjectType(ObjectType.DATA_OBJECT);
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(dataName)).thenReturn(
				objStat);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);

		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		Mockito.when(dataObjectAO.findByAbsolutePath(dataName)).thenReturn(
				dataObject);

		Mockito.when(dataObjectAO.findMetadataValuesForDataObject(dataName))
				.thenReturn(avus);

		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		DataProfileService dataProfileService = new DataProfileServiceImpl(
				irodsAccessObjectFactory, irodsAccount, resolutionService);

		@SuppressWarnings("unchecked")
		DataProfile<DataObject> actual = dataProfileService
				.retrieveDataProfile(dataName);
		Assert.assertNotNull("null dataProfle", actual);
		Assert.assertNotNull("no data object", actual.getDomainObject());
		Assert.assertTrue("should be file", actual.isFile());
		Assert.assertFalse("should not be starred", actual.isStarred());
		Assert.assertFalse("should not be shared", actual.isShared());
		Assert.assertFalse("should have tags", actual.getIrodsTagValues()
				.isEmpty());

	}

	@Test
	public void testRetrieveDataProfileCollection() throws Exception {

		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DataTypeResolutionService resolutionService = new DataTypeResolutionServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		Collection collection = new Collection();
		String collName = "/a/collection";
		collection.setCollectionName(collName);

		List<MetaDataAndDomainData> avus = new ArrayList<MetaDataAndDomainData>();

		MetaDataAndDomainData starred = MetaDataAndDomainData.instance(
				MetadataDomain.COLLECTION, "1", "blah", 0, "l",
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);

		avus.add(starred);

		MetaDataAndDomainData shared = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", "blah", 0, "l",
						irodsAccount.getUserName(),
						UserTaggingConstants.SHARE_AVU_UNIT);

		avus.add(shared);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAOImpl.class);

		ObjStat objStat = new ObjStat();
		objStat.setObjectType(ObjectType.COLLECTION);
		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(collName)).thenReturn(
				objStat);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);

		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		Mockito.when(collectionAO.findByAbsolutePath(collName)).thenReturn(
				collection);

		Mockito.when(collectionAO.findMetadataValuesForCollection(collName))
				.thenReturn(avus);

		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);

		DataProfileService dataProfileService = new DataProfileServiceImpl(
				irodsAccessObjectFactory, irodsAccount, resolutionService);

		@SuppressWarnings("unchecked")
		DataProfile<Collection> actual = dataProfileService
				.retrieveDataProfile(collName);
		Assert.assertNotNull("null dataProfle", actual);
		Assert.assertNotNull("no collection", actual.getDomainObject());
		Assert.assertFalse("should not be file", actual.isFile());
		Assert.assertTrue("shoudl be starred", actual.isStarred());
		Assert.assertTrue("should be shared", actual.isShared());

	}

}
