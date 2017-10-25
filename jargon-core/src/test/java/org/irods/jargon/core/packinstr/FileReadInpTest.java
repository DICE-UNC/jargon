package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.junit.Test;

public class FileReadInpTest {
	@Test
	public final void testInstance() throws Exception {
		int fd = 2;
		long length = 100L;
		FileReadInp fileReadInp = FileReadInp.instanceForReadStream(fd, length);
		Assert.assertNotNull("null fileReadInp", fileReadInp);
		Assert.assertEquals("wrong API num", FileReadInp.FILE_READ_API_NBR,
				fileReadInp.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceZeroFd() throws Exception {
		FileReadInp.instanceForReadStream(0, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNegFd() throws Exception {
		FileReadInp.instanceForReadStream(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceZeroLen() throws Exception {
		FileReadInp.instanceForReadStream(10, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInstanceNegLen() throws Exception {
		FileReadInp.instanceForReadStream(10, -1);
	}

	@Test
	public final void testGetParsedTagsForReadStream() throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("<fileReadInp_PI><fileInx>2</fileInx>\n");
		sb.append("<len>100</len>\n");
		sb.append("</fileReadInp_PI>\n");
		String expected = sb.toString();

		int fd = 2;
		long length = 100L;
		FileReadInp fileReadInp = FileReadInp.instanceForReadStream(fd, length);

		Assert.assertEquals("did not get expected xml", expected,
				fileReadInp.getParsedTags());
	}

}
