package org.irods.jargon.usertagging.domain;


import junit.framework.Assert;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSTagValueTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCreateTag() throws Exception {
		String expectedTagVal = "tagVal";
		String expectedTagUser = "tagUser";
		IRODSTagValue actual = new IRODSTagValue(expectedTagVal, expectedTagUser);
		Assert.assertEquals(expectedTagVal, actual.getTagData());
		Assert.assertEquals(expectedTagUser, actual.getTagUser());
	}
	
	@Test
	public void testCreateTagEmptyTag() throws Exception {
		String expectedTagVal = "";
		String expectedTagUser = "tagUser";
		new IRODSTagValue(expectedTagVal, expectedTagUser);
	}
	
	@Test(expected=JargonException.class)
	public void testCreateTagNullTag() throws Exception {
		String expectedTagVal = null;
		String expectedTagUser = "tagUser";
		new IRODSTagValue(expectedTagVal, expectedTagUser);
	}
	
	@Test(expected=JargonException.class)
	public void testCreateTagEmptyTagUser() throws Exception {
		String expectedTagVal = "xxxx";
		String expectedTagUser = "";
		new IRODSTagValue(expectedTagVal, expectedTagUser);
	}
	
	@Test(expected=JargonException.class)
	public void testCreateTagNullTagUser() throws Exception {
		String expectedTagVal = "xxxx";
		String expectedTagUser = null;
		new IRODSTagValue(expectedTagVal, expectedTagUser);
	}
	
	@Test
	public void testCreateTagFromMetadataAndDomainDataValue() throws Exception {
		String expectedUser = "testuser";
		String expectedTag = "testTag";
		
		MetaDataAndDomainData metadataAndDomainData = MetaDataAndDomainData.instance(MetadataDomain.DATA, "1", "xxx", expectedTag, 
				expectedUser, UserTaggingConstants.TAG_AVU_UNIT);
		
		IRODSTagValue actual = new IRODSTagValue(metadataAndDomainData);
		
		Assert.assertEquals(expectedTag, actual.getTagData());
		Assert.assertEquals(expectedUser, actual.getTagUser());
	
	}
	
	
}
