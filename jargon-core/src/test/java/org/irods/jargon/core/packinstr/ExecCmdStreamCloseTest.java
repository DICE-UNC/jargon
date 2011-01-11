package org.irods.jargon.core.packinstr;

import junit.framework.Assert;

import org.junit.Test;

public class ExecCmdStreamCloseTest {

	@Test
	public void testInstance() throws Exception {
		ExecCmdStreamClose execCmd = ExecCmdStreamClose.instance(1);
		Assert.assertNotNull("null intance returned", execCmd);
		Assert.assertEquals("did not correctly set api number",
				ExecCmdStreamClose.STREAM_CLOSE_API_NBR, execCmd.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceZeroFileDescriptor() throws Exception {
		ExecCmdStreamClose.instance(0);

	}

	@Test
	public void testGetTagValue() throws Exception {
		ExecCmdStreamClose execCmd = ExecCmdStreamClose.instance(1);
		String actualXML = execCmd.getParsedTags();

		StringBuilder sb = new StringBuilder();

		sb.append("<ExecCmd_PI><fileInx>1</fileInx>\n");
		sb.append("</ExecCmd_PI>\n");

		String expectedXML = sb.toString();

		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				actualXML);

	}

}
