package org.irods.jargon.core.utils;

import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_HAAW;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_LINK;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_MOUNT;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_TAR;
import static org.irods.jargon.core.utils.IRODSDataConversionUtil.getCollectionTypeFromIRODSValue;
import static org.junit.Assert.assertEquals;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.junit.Test;

public final class IRODSDataConversionUtilTest {

	@Test
	public void testGetLinkedCollFromIRODSValue() {
		assertEquals("linked collection maps incorrectly",
				SpecColType.LINKED_COLL,
				getCollectionTypeFromIRODSValue(COLL_TYPE_LINK));
	}

	@Test
	public void testGetMountedCollFromIRODSValue() {
		assertEquals("mounted collection maps incorrectly",
				SpecColType.MOUNTED_COLL,
				getCollectionTypeFromIRODSValue(COLL_TYPE_MOUNT));
	}

	@Test
	public void testGetHAAWCollFromIRODSValue() {
		assertEquals("HAAW file maps incorrectly",
				SpecColType.STRUCT_FILE_COLL,
				getCollectionTypeFromIRODSValue(COLL_TYPE_HAAW));
	}

	@Test
	public void testGetTARCollFromIRODSValue() {
		assertEquals("TAR file maps incorrectly", SpecColType.STRUCT_FILE_COLL,
				getCollectionTypeFromIRODSValue(COLL_TYPE_TAR));
	}
}
