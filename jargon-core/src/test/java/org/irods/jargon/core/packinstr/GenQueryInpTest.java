package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryTranslator;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.TranslatedIRODSGenQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class GenQueryInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForClose() throws Exception {
		TranslatedIRODSGenQuery translatedIRODSQuery = Mockito.mock(TranslatedIRODSGenQuery.class);
		GenQueryInp genQueryInp = GenQueryInp.instanceForCloseQuery(translatedIRODSQuery, 2);
		Assert.assertEquals("did not correctly set continuation", 2, genQueryInp.getContinueIndex());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForCloseNoContinuation() throws Exception {
		TranslatedIRODSGenQuery translatedIRODSQuery = Mockito.mock(TranslatedIRODSGenQuery.class);
		GenQueryInp.instanceForCloseQuery(translatedIRODSQuery, 0);
	}

	@Test
	public final void testGetParsedTagsClose() throws Exception {
		String queryString = "select " + RodsGenQueryEnum.COL_D_COLL_ID.getName() + " ,"
				+ RodsGenQueryEnum.COL_COLL_ACCESS_COLL_ID.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName() + " = " + "'2'";

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(props);
		TranslatedIRODSGenQuery translatedIRODSQuery = irodsQueryTranslator.getTranslatedQuery(irodsQuery);
		GenQueryInp genQueryInp = GenQueryInp.instanceForCloseQuery(translatedIRODSQuery, 2);
		String response = genQueryInp.getParsedTags();
		Assert.assertNotNull("no tags generated", response);

		StringBuilder sb = new StringBuilder();
		sb.append("<GenQueryInp_PI><maxRows>-1</maxRows>\n");
		sb.append("<continueInx>2</continueInx>\n");
		sb.append("<partialStartIndex>0</partialStartIndex>\n");
		sb.append("<options>0</options>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("<InxIvalPair_PI><iiLen>2</iiLen>\n");
		sb.append("<inx>402</inx>\n");
		sb.append("<inx>714</inx>\n");
		sb.append("<ivalue>1</ivalue>\n");
		sb.append("<ivalue>1</ivalue>\n");
		sb.append("</InxIvalPair_PI>\n");
		sb.append("<InxValPair_PI><isLen>1</isLen>\n");
		sb.append("<inx>710</inx>\n");
		sb.append("<svalue> = '2' </svalue>\n");
		sb.append("</InxValPair_PI>\n");
		sb.append("</GenQueryInp_PI>\n");
		String tagVal = sb.toString();
		Assert.assertEquals("improper tags returned", tagVal, response);
	}

	@Test
	public final void testGenQueryInp() throws Exception {
		String queryString = "select " + RodsGenQueryEnum.COL_D_COLL_ID.getName() + " ,"
				+ RodsGenQueryEnum.COL_COLL_ACCESS_COLL_ID.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName() + " = " + "'2'";

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(props);
		TranslatedIRODSGenQuery translatedIRODSQuery = irodsQueryTranslator.getTranslatedQuery(irodsQuery);

		GenQueryInp genQueryInp = GenQueryInp.instance(translatedIRODSQuery, 0, null);

		Assert.assertNotNull(genQueryInp.getParsedTags());
	}

	@Test
	public final void testGetParsedTags() throws Exception {
		String queryString = "select " + RodsGenQueryEnum.COL_D_COLL_ID.getName() + " ,"
				+ RodsGenQueryEnum.COL_COLL_ACCESS_COLL_ID.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName() + " = " + "'2'";

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);
		IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(props);
		TranslatedIRODSGenQuery translatedIRODSQuery = irodsQueryTranslator.getTranslatedQuery(irodsQuery);
		GenQueryInp genQueryInp = GenQueryInp.instance(translatedIRODSQuery, 0, null);
		String tagData = genQueryInp.getParsedTags();
		Assert.assertTrue("did not find select field",
				tagData.indexOf(String.valueOf(RodsGenQueryEnum.COL_D_COLL_ID.getNumericValue())) > -1);
		Assert.assertTrue("did not find select field",
				tagData.indexOf(String.valueOf(RodsGenQueryEnum.COL_COLL_ACCESS_COLL_ID.getNumericValue())) > -1);

	}

	@Test
	public final void testGetParsedTagsWithZone() throws Exception {
		String zoneName = "zoneNameHere";
		String queryString = "select " + RodsGenQueryEnum.COL_D_COLL_ID.getName() + " ,"
				+ RodsGenQueryEnum.COL_COLL_ACCESS_COLL_ID.getName() + " where "
				+ RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName() + " = " + "'2'";

		IRODSServerProperties props = IRODSServerProperties.instance(IRODSServerProperties.IcatEnabled.ICAT_ENABLED,
				100, "rods2.2", "d", "zone");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);
		IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(props);
		TranslatedIRODSGenQuery translatedIRODSQuery = irodsQueryTranslator.getTranslatedQuery(irodsQuery);
		GenQueryInp genQueryInp = GenQueryInp.instance(translatedIRODSQuery, 0, zoneName);
		String response = genQueryInp.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<GenQueryInp_PI><maxRows>500</maxRows>\n");
		sb.append("<continueInx>0</continueInx>\n");
		sb.append("<partialStartIndex>0</partialStartIndex>\n");
		sb.append("<options>0</options>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>zone</keyWord>\n");
		sb.append("<svalue>zoneNameHere</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("<InxIvalPair_PI><iiLen>2</iiLen>\n");
		sb.append("<inx>402</inx>\n");
		sb.append("<inx>714</inx>\n");
		sb.append("<ivalue>1</ivalue>\n");
		sb.append("<ivalue>1</ivalue>\n");
		sb.append("</InxIvalPair_PI>\n");
		sb.append("<InxValPair_PI><isLen>1</isLen>\n");
		sb.append("<inx>710</inx>\n");
		sb.append("<svalue> = '2' </svalue>\n");
		sb.append("</InxValPair_PI>\n");
		sb.append("</GenQueryInp_PI>\n");

		String tagVal = sb.toString();
		Assert.assertEquals("improper tags returned", tagVal, response);

	}

}
