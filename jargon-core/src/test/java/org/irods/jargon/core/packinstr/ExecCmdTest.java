package org.irods.jargon.core.packinstr;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.irods.jargon.core.exception.*;

public class ExecCmdTest {

	@Test
	public void testInstance() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd.instanceWithCommand(cmd, args);
		Assert
				.assertNotNull(
						"basic check fails, null returned from PI initializer",
						execCmd);
		Assert.assertEquals("api number not set", ExecCmd.API_NBR, execCmd
				.getApiNumber());
	}
	
	@Test
	public void testInstanceIndicatingStreaming() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd.instanceWithCommandAllowingStreamingForLargeResults(cmd, args);
		Assert
				.assertNotNull(
						"basic check fails, null returned from PI initializer",
						execCmd);
		Assert.assertEquals("api number not set", ExecCmd.API_NBR, execCmd
				.getApiNumber());
	}

	@Test(expected=JargonException.class)
	public void testInstanceBlankCommand() throws Exception {
		String cmd = "";
		String args = "";

		ExecCmd.instanceWithCommand(cmd, args);
	}
	
	@Test(expected=JargonException.class)
	public void testInstanceNullCommand() throws Exception {
		String cmd = null;
		String args = "";

		ExecCmd.instanceWithCommand(cmd, args);
	}
	
	@Test(expected=JargonException.class)
	public void testInstanceNullArgs() throws Exception {
		String cmd = "hello";
		String args = null;

		ExecCmd.instanceWithCommand(cmd, args);
	}

	@Test
	public void testGetParsedTags() throws Exception {
		String cmd = "hello";
		String args = "";
		
		ExecCmd execCmd = ExecCmd.instanceWithCommand(cmd, args);		
		String actualXML = execCmd.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecCmd_PI><cmd>hello</cmd>\n");
		sb.append("<cmdArgv></cmdArgv>\n");
		sb.append("<execAddr></execAddr>\n");
		sb.append("<hintPath></hintPath>\n");
		sb.append("<addPathToArgv>0</addPathToArgv>\n");
		sb.append("<KeyValPair_PI><ssLen>0</ssLen>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</ExecCmd_PI>\n");
		
		String expectedXML = sb.toString();
		
		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				actualXML);
	}
	
	@Test
	public void testGetParsedTagsWithStreaming() throws Exception {
		String cmd = "hello";
		String args = "";
		
		ExecCmd execCmd = ExecCmd.instanceWithCommandAllowingStreamingForLargeResults(cmd, args);		
		String actualXML = execCmd.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecCmd_PI><cmd>hello</cmd>\n");
		sb.append("<cmdArgv></cmdArgv>\n");
		sb.append("<execAddr></execAddr>\n");
		sb.append("<hintPath></hintPath>\n");
		sb.append("<addPathToArgv>0</addPathToArgv>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>streamStdout</keyWord>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</ExecCmd_PI>\n");
		
		String expectedXML = sb.toString();
		
		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				actualXML);
		
	}

	@Test
	public void testInstanceWithAllParms() throws Exception {
		String cmd = "hello";
		String args = "";
		String host = "host";
		String absPath = "/an/abs/path";

		ExecCmd execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParameters(cmd, args, host, absPath);
		Assert
				.assertNotNull(
						"basic check fails, null returned from PI initializer",
						execCmd);
		Assert.assertEquals("api number not set", ExecCmd.API_NBR, execCmd
				.getApiNumber());
	}
	
	@Test
	public void testInstanceWithAllParmsBlankHost() throws Exception {
		String cmd = "cmd";
		String args = "";
		String host = "";
		String absPath = "/an/abs/path";

		ExecCmd execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParameters(cmd, args, host, absPath);
		Assert
				.assertNotNull(
						"basic check fails, null returned from PI initializer",
						execCmd);
		Assert.assertEquals("api number not set", ExecCmd.API_NBR, execCmd
				.getApiNumber());
	}


}
