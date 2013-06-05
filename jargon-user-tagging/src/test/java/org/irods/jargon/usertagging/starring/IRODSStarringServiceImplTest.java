package org.irods.jargon.usertagging.starring;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class IRODSStarringServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSAccount irodsAccount;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindStarredForAbsolutePathWhenCollection() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.COLLECTION);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", absolutePath,
						description, irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						Matchers.anyList(), Matchers.anyString())).thenReturn(
				metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		IRODSStarredFileOrCollection actual = irodsStarringService
				.findStarredForAbsolutePath(absolutePath);

		Assert.assertNotNull("did not return a starredFile", actual);
		Assert.assertEquals("did not get description", description,
				actual.getDescription());
		Assert.assertEquals("did not get user", irodsAccount.getUserName(),
				actual.getUserName());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindStarredForAbsolutePathWhenDataObject() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);
		Mockito.when(irodsFileFactory.instanceIRODSFile(absolutePath))
				.thenReturn(irodsFile);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.DATA_OBJECT);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", absolutePath, description,
						irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(
						Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenReturn(metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		IRODSStarredFileOrCollection actual = irodsStarringService
				.findStarredForAbsolutePath(absolutePath);

		Assert.assertNotNull("did not return a starredFile", actual);
		Assert.assertEquals("did not get description", description,
				actual.getDescription());
		Assert.assertEquals("did not get user", irodsAccount.getUserName(),
				actual.getUserName());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindStarredForAbsolutePathWhenDataObjectNoData()
			throws Exception {
		String absolutePath = "/absolutePath";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);
		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);
		Mockito.when(irodsFileFactory.instanceIRODSFile(absolutePath))
				.thenReturn(irodsFile);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.DATA_OBJECT);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();

		Mockito.when(
				dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(
						Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenReturn(metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		IRODSStarredFileOrCollection actual = irodsStarringService
				.findStarredForAbsolutePath(absolutePath);

		Assert.assertNull("should be null..no data", actual);

	}

	/**
	 * The AVU has the wrong unit, it's not a 'starred'
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = IllegalArgumentException.class)
	public void testFindStarredForAbsolutePathAvuTypeIncorrect()
			throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.COLLECTION);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", absolutePath,
						description, irodsAccount.getUserName(),
						UserTaggingConstants.TAG_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						Matchers.anyList(), Matchers.anyString())).thenReturn(
				metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.findStarredForAbsolutePath(absolutePath);

	}

	/**
	 * the file does not exist
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = FileNotFoundException.class)
	public void testFindStarredForAbsolutePathNotFound() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenThrow(
				new FileNotFoundException("not found"));

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", absolutePath,
						description, irodsAccount.getUserName(),
						UserTaggingConstants.TAG_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						Matchers.anyList(), Matchers.anyString())).thenReturn(
				metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.findStarredForAbsolutePath(absolutePath);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindStarredForAbsolutePathNullAbsPath() throws Exception {
		String absolutePath = null;

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.findStarredForAbsolutePath(absolutePath);

	}

	@Test
	public void testStarFileOrCollectionWhenCollection() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.COLLECTION);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.starFileOrCollection(absolutePath, description);

		AvuData avuData = AvuData.instance(description,
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);
		Mockito.verify(collectionAO).addAVUMetadata(absolutePath, avuData);

	}

	@Test(expected = FileNotFoundException.class)
	public void testStarFileOrCollectionWhenCollectionNotFound()
			throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenThrow(
				new FileNotFoundException("file not found"));

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.starFileOrCollection(absolutePath, description);

	}

	@Test
	public void testStarFileOrCollectionWhenDataObject() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);
		Mockito.when(irodsFileFactory.instanceIRODSFile(absolutePath))
				.thenReturn(irodsFile);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.DATA_OBJECT);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.starFileOrCollection(absolutePath, description);

		AvuData avuData = AvuData.instance(description,
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);
		Mockito.verify(dataObjectAO).addAVUMetadata(absolutePath, avuData);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStarFileOrCollectionWhenDataObjectAsAnUpdate()
			throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";
		String oldDescription = "oldDescription";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);
		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);
		Mockito.when(irodsFileFactory.instanceIRODSFile(absolutePath))
				.thenReturn(irodsFile);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.DATA_OBJECT);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", absolutePath,
						oldDescription, irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(
						Matchers.anyList(), Matchers.anyString(),
						Matchers.anyString())).thenReturn(metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.starFileOrCollection(absolutePath, description);

		AvuData oldAvu = AvuData.instance(oldDescription,
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);
		AvuData avuData = AvuData.instance(description,
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);
		Mockito.verify(dataObjectAO).modifyAVUMetadata(absolutePath, oldAvu,
				avuData);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUnstarFileOrCollectionWhenCollection() throws Exception {
		String absolutePath = "/absolutePath";
		String description = "description";

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);

		ObjStat objStat = new ObjStat();
		objStat.setAbsolutePath(absolutePath);
		objStat.setObjectType(ObjectType.COLLECTION);

		Mockito.when(
				collectionAndDataObjectListAndSearchAO
						.retrieveObjectStatForPath(absolutePath)).thenReturn(
				objStat);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", absolutePath,
						description, irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQueryForCollection(
						Matchers.anyList(), Matchers.anyString())).thenReturn(
				metadataList);

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		irodsStarringService.unstarFileOrCollection(absolutePath);
		AvuData avuData = AvuData.instance(description,
				irodsAccount.getUserName(), UserTaggingConstants.STAR_AVU_UNIT);
		Mockito.verify(collectionAO).deleteAVUMetadata(absolutePath, avuData);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testListStarredCollections() throws Exception {
		String absolutePath = "/absolute/path/to/coll";
		String description = "description";
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.COLLECTION, "1", absolutePath,
						description, irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				collectionAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metadataList);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);
		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<IRODSStarredFileOrCollection> collections = irodsStarringService
				.listStarredCollections(0);
		Assert.assertEquals("did not find the one metadata value", 1,
				collections.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testListStarredDataObjects() throws Exception {
		String absolutePath = "/absolute/path/to/dataobj.txt";
		String description = "description";
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = Mockito
				.mock(CollectionAndDataObjectListAndSearchAO.class);
		DataObjectAO dataObjectAO = Mockito.mock(DataObjectAO.class);
		CollectionAO collectionAO = Mockito.mock(CollectionAO.class);

		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData
				.instance(MetadataDomain.DATA, "1", absolutePath, description,
						irodsAccount.getUserName(),
						UserTaggingConstants.STAR_AVU_UNIT);
		List<MetaDataAndDomainData> metadataList = new ArrayList<MetaDataAndDomainData>();
		metadataList.add(metadataAndDomainData);

		Mockito.when(
				dataObjectAO.findMetadataValuesByMetadataQuery(Matchers
						.anyList())).thenReturn(metadataList);

		Mockito.when(
				irodsAccessObjectFactory
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount))
				.thenReturn(collectionAndDataObjectListAndSearchAO);
		Mockito.when(irodsAccessObjectFactory.getCollectionAO(irodsAccount))
				.thenReturn(collectionAO);
		Mockito.when(irodsAccessObjectFactory.getDataObjectAO(irodsAccount))
				.thenReturn(dataObjectAO);
		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		List<IRODSStarredFileOrCollection> collections = irodsStarringService
				.listStarredDataObjects(0);
		Assert.assertEquals("did not find the one metadata value", 1,
				collections.size());

	}

}
