package org.irods.jargon.core.packinstr;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class OpenedDataObjInpTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstanceForFileSeek() throws Exception {
		OpenedDataObjInp openedDataObjInp = OpenedDataObjInp.instanceForFileSeek(1024, 3,
				OpenedDataObjInp.SEEK_CURRENT);
		StringBuilder sb = new StringBuilder();
		sb.append("<OpenedDataObjInp_PI>");
		sb.append("<l1descInx>3</l1descInx>\n");
		sb.append("<len>0</len>\n");
		sb.append("<whence>1</whence>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<offset>1024</offset>\n");
		sb.append("<bytesWritten>0</bytesWritten>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</OpenedDataObjInp_PI>\n");
		Assert.assertEquals("did not generate expected XML", sb.toString(), openedDataObjInp.getParsedTags());
	}

	@Test
	public final void testInstanceForFileWrite() throws Exception {
		OpenedDataObjInp openedDataObjInp = OpenedDataObjInp.instanceForFilePut(3, 4194304L);
		StringBuilder sb = new StringBuilder();
		sb.append("<OpenedDataObjInp_PI>");
		sb.append("<l1descInx>3</l1descInx>\n");
		sb.append("<len>4194304</len>\n");
		sb.append("<whence>0</whence>\n");
		sb.append("<oprType>0</oprType>\n");
		sb.append("<offset>0</offset>\n");
		sb.append("<bytesWritten>0</bytesWritten>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</OpenedDataObjInp_PI>\n");
		Assert.assertEquals("did not generate expected XML", sb.toString(), openedDataObjInp.getParsedTags());
	}

	@Test
	public final void testGetApiNumber() throws Exception {
		OpenedDataObjInp openedDataObjInp = OpenedDataObjInp.instanceForFileSeek(1024, 3,
				OpenedDataObjInp.SEEK_CURRENT);
		Assert.assertEquals("did not get correct api number", 674, openedDataObjInp.getApiNumber());

	}

}
