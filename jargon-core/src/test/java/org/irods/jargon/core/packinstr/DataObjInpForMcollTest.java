package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjInpForMcollTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForSoftLinkMount() {
		DataObjInpForMcoll actual = DataObjInpForMcoll
				.instanceForSoftLinkMount("source", "target", "resc");
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());
	}

	@Test
	public final void testInstanceForMSSOMount() {
		DataObjInpForMcoll actual = DataObjInpForMcoll.instanceForMSSOMount(
				"source", "target", "resc");
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForFileSystemMountNullLocal()
			throws Exception {
		String localFile = null;
		String mount = "/target/irods";
		String resc = "resc";
		DataObjInpForMcoll.instanceForFileSystemMount(localFile, mount, resc);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForFileSystemMountBlankLocal()
			throws Exception {
		String localFile = "";
		String mount = "/target/irods";
		String resc = "resc";
		DataObjInpForMcoll.instanceForFileSystemMount(localFile, mount, resc);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForFileSystemMountNullIrods()
			throws Exception {
		String localFile = "xxx";
		String mount = null;
		String resc = "resc";
		DataObjInpForMcoll.instanceForFileSystemMount(localFile, mount, resc);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForFileSystemMountBlankIrods()
			throws Exception {
		String localFile = "xxx";
		String mount = "";
		String resc = "resc";
		DataObjInpForMcoll.instanceForFileSystemMount(localFile, mount, resc);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForFileSystemMountNullResc() throws Exception {
		String localFile = "xxx";
		String mount = "yyy";
		String resc = null;
		DataObjInpForMcoll.instanceForFileSystemMount(localFile, mount, resc);

	}

	@Test
	public final void testInstanceForFileSystemMount() throws Exception {
		String localFile = "/a/local/file";
		String mount = "/target/irods";
		String resc = "resc";
		DataObjInpForMcoll actual = DataObjInpForMcoll
				.instanceForFileSystemMount(localFile, mount, resc);
		Assert.assertEquals("wrong api number", DataObjInpForMcoll.MCOLL_AN,
				actual.getApiNumber());

		String actualTag = actual.getParsedTags();
		StringBuilder sb = new StringBuilder();
		sb.append("<DataObjInp_PI><objPath>/target/irods</objPath>\n");
		sb.append("<createMode>0</createMode>\n");
		sb.append("<openFlags>0</openFlags>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<dataSize>0</dataSize>\n");
		sb.append("<numThreads>0</numThreads>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<KeyValPair_PI><ssLen>3</ssLen>\n");
		sb.append("<keyWord>collectionType</keyWord>\n");
		sb.append("<keyWord>destRescName</keyWord>\n");
		sb.append("<keyWord>filePath</keyWord>\n");
		sb.append("<svalue>mountPoint</svalue>\n");
		sb.append("<svalue>resc</svalue>\n");
		sb.append("<svalue>/a/local/file</svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</DataObjInp_PI>\n");
		Assert.assertEquals("invalid tag generated", sb.toString(), actualTag);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullSource() {
		DataObjInpForMcoll.instanceForSoftLinkMount(null, "target", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountBlankSource() {
		DataObjInpForMcoll.instanceForSoftLinkMount("", "target", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullTarget() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", null, "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountBlankTarget() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "", "resc");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceForSoftLinkMountNullResc() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "target", null);
	}

	@Test
	public final void testInstanceForSoftLinkMountBlankResc() {
		DataObjInpForMcoll.instanceForSoftLinkMount("source", "target", "");
	}

}
