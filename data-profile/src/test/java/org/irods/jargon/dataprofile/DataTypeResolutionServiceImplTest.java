package org.irods.jargon.dataprofile;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataTypeResolutionServiceImplTest {

	@Test
	public void testresolveDataTypeWithProvidedAvuAndDataObjectValueFromAVU()
			throws Exception {

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

		String actual = resolutionService
				.resolveDataTypeWithProvidedAvuAndDataObject(dataObject, avus);
		Assert.assertEquals("didn't get mime type from AVU", "application/xml",
				actual);

	}

	@Test
	public void testresolveDataTypeWithProvidedAvuAndDataObjectValueNoAVU()
			throws Exception {

		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DataTypeResolutionService resolutionService = new DataTypeResolutionServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		DataObject dataObject = new DataObject();
		String dataName = "file.txt";
		dataObject.setDataName(dataName);

		List<MetaDataAndDomainData> avus = new ArrayList<MetaDataAndDomainData>();

		String actual = resolutionService
				.resolveDataTypeWithProvidedAvuAndDataObject(dataObject, avus);
		Assert.assertEquals("didn't get mime type from tika", "text/plain",
				actual);

	}

	@Test
	public void testGetDataTypeFromExtension() throws Exception {
		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		DataTypeResolutionService resolutionService = new DataTypeResolutionServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		DataObject dataObject = new DataObject();
		String dataName = "file.txt";
		dataObject.setDataName(dataName);

		String actual = resolutionService.determineMimeTypeViaTika(dataObject);
		Assert.assertNotNull("null data type", actual);
		Assert.assertFalse("no data type", actual.isEmpty());

	}
}
