package org.irods.jargon.core.query;

import junit.framework.Assert;
import org.junit.Test;

public class IRODSGenQueryBuilderTest {

	@Test
	public void testBuildQueryWithUpperCase() throws Exception {
		IRODSGenQueryBuilder irodsGenQueryBuilder = new IRODSGenQueryBuilder(true,true,null);
		irodsGenQueryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_PATH);
		IRODSGenQueryFromBuilder query = irodsGenQueryBuilder.exportIRODSQueryFromBuilder(100);
		Assert.assertTrue("should be distinct", query.getIrodsGenQueryBuilderData().isDistinct());
		Assert.assertTrue("should be case-insensitive", query.getIrodsGenQueryBuilderData().isUpperCase());
	}
	
	@Test
	public void testBuildQueryWithoutUpperCaseSig() throws Exception {
		IRODSGenQueryBuilder irodsGenQueryBuilder = new IRODSGenQueryBuilder(true,null);
		irodsGenQueryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_PATH);
		IRODSGenQueryFromBuilder query = irodsGenQueryBuilder.exportIRODSQueryFromBuilder(100);
		Assert.assertTrue("should be distinct", query.getIrodsGenQueryBuilderData().isDistinct());
		Assert.assertFalse("should not be case-insensitive", query.getIrodsGenQueryBuilderData().isUpperCase());
	}


}
