package org.irods.jargon.usertagging.domain;


import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSTagGroupingTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void createInstanceTest() throws Exception {
		
		String expectedUser = "user";
		String expectedDomainName = "/a/domain/name";
		String expectedTags = "tag1 tag2 tag3:tag3sub tag4 himom";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
		TestCase.assertEquals(expectedUser, irodsTagGrouping.getUserName());
		TestCase.assertEquals(expectedDomainName, irodsTagGrouping.getDomainUniqueName());
		TestCase.assertEquals(expectedTags, irodsTagGrouping.getSpaceDelimitedTagsForDomain());
		TestCase.assertEquals(expectedMetadataDomain, irodsTagGrouping.getMetadataDomain());
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createInstanceBlankUserTest() throws Exception {
		
		String expectedUser = "";
		String expectedDomainName = "/a/domain/name";
		String expectedTags = "tag1 tag2 tag3:tag3sub tag4 himom";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createInstanceNullUserTest() throws Exception {
		
		String expectedUser = null;
		String expectedDomainName = "/a/domain/name";
		String expectedTags = "tag1 tag2 tag3:tag3sub tag4 himom";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createInstanceBlankDomainTest() throws Exception {
		
		String expectedUser = "user";
		String expectedDomainName = "";
		String expectedTags = "tag1 tag2 tag3:tag3sub tag4 himom";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createInstanceNullDomainTest() throws Exception {
		
		String expectedUser = "user";
		String expectedDomainName = null;
		String expectedTags = "tag1 tag2 tag3:tag3sub tag4 himom";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}

	@Test
	public void createInstanceBlankTagsTest() throws Exception {
		
		String expectedUser = "user";
		String expectedDomainName = "domain";
		String expectedTags = "";
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}
	
	@Test(expected=JargonException.class)
	public void createInstanceNullTagsTest() throws Exception {
		
		String expectedUser = "user";
		String expectedDomainName = "domain";
		String expectedTags = null;
		MetadataDomain expectedMetadataDomain = MetadataDomain.DATA;
		
		new IRODSTagGrouping(expectedMetadataDomain, expectedDomainName, expectedTags, expectedUser);
		
	}


}
