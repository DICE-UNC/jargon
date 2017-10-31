package org.irods.jargon.core.packinstr;

import org.junit.Assert;

import org.irods.jargon.core.packinstr.ExecCmd.PathHandlingMode;
import org.junit.Test;

public class ExecCmdTest {

	@Test
	public void testInstance() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd.instanceWithCommandPriorTo25(cmd, args);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", execCmd);
		Assert.assertEquals("api number not set",
				ExecCmd.STANDARD_EXEC_ENCAPSULATE_DATA_IN_RESPONSE_API_NBR,
				execCmd.getApiNumber());
	}

	@Test
	public void testInstanceIndicatingStreaming() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd
				.instanceWithCommandAllowingStreamingForLargeResultsPost25(cmd,
						args);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", execCmd);
		Assert.assertEquals("api number not set",
				ExecCmd.EXEC_AND_USE_ENHANCED_STREAM, execCmd.getApiNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankCommand() throws Exception {
		String cmd = "";
		String args = "";

		ExecCmd.instanceWithCommandPost25(cmd, args);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullCommand() throws Exception {
		String cmd = null;
		String args = "";

		ExecCmd.instanceWithCommandPriorTo25(cmd, args);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullArgs() throws Exception {
		String cmd = "hello";
		String args = null;

		ExecCmd.instanceWithCommandPriorTo25(cmd, args);
	}

	@Test
	public void testGetParsedTags() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd.instanceWithCommandPriorTo25(cmd, args);
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
	public void testGetParsedTagsNewerAPI() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd.instanceWithCommandPost25(cmd, args);
		String actualXML = execCmd.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecCmd_PI><cmd>hello</cmd>\n");
		sb.append("<cmdArgv></cmdArgv>\n");
		sb.append("<execAddr></execAddr>\n");
		sb.append("<hintPath></hintPath>\n");
		sb.append("<addPathToArgv>0</addPathToArgv>\n");
		sb.append("<dummy>0</dummy>\n");
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
	public void testGetParsedTagsWithStreaming() throws Exception {
		String cmd = "hello";
		String args = "";

		ExecCmd execCmd = ExecCmd
				.instanceWithCommandAllowingStreamingForLargeResultsPost25(cmd,
						args);
		String actualXML = execCmd.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ExecCmd_PI><cmd>hello</cmd>\n");
		sb.append("<cmdArgv></cmdArgv>\n");
		sb.append("<execAddr></execAddr>\n");
		sb.append("<hintPath></hintPath>\n");
		sb.append("<addPathToArgv>0</addPathToArgv>\n");
		sb.append("<dummy>0</dummy>\n");
		sb.append("<KeyValPair_PI><ssLen>1</ssLen>\n");
		sb.append("<keyWord>streamStdout</keyWord>\n");
		sb.append("<svalue></svalue>\n");
		sb.append("</KeyValPair_PI>\n");
		sb.append("</ExecCmd_PI>\n");

		String expectedXML = sb.toString();

		Assert.assertEquals("did not get expected XML from PI", expectedXML,
				actualXML);

		Assert.assertEquals("did not set streaming api nbr",
				ExecCmd.EXEC_AND_USE_ENHANCED_STREAM, execCmd.getApiNumber());

	}

	@Test
	public void testInstanceWithAllParmsPre25() throws Exception {
		String cmd = "hello";
		String args = "";
		String host = "host";
		String absPath = "/an/abs/path";

		ExecCmd execCmd = ExecCmd
				.instanceWithHostAndArgumentsToPassParametersPriorTo25(cmd,
						args, host, absPath, PathHandlingMode.NONE);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", execCmd);
		Assert.assertEquals("api number not set",
				ExecCmd.STANDARD_EXEC_ENCAPSULATE_DATA_IN_RESPONSE_API_NBR,
				execCmd.getApiNumber());
	}

	@Test
	public void testInstanceWithAllParmsPost25() throws Exception {
		String cmd = "hello";
		String args = "";
		String host = "host";
		String absPath = "/an/abs/path";

		ExecCmd execCmd = ExecCmd
				.instanceWithHostAndArgumentsToPassParametersPost25(cmd, args,
						host, absPath, PathHandlingMode.NONE);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", execCmd);
		Assert.assertEquals("api number not set",
				ExecCmd.EXEC_AND_USE_ENHANCED_STREAM, execCmd.getApiNumber());
	}

	@Test
	public void testInstanceWithAllParmsBlankHost() throws Exception {
		String cmd = "cmd";
		String args = "";
		String host = "";
		String absPath = "/an/abs/path";

		ExecCmd execCmd = ExecCmd
				.instanceWithHostAndArgumentsToPassParametersPost25(cmd, args,
						host, absPath, PathHandlingMode.NONE);
		Assert.assertNotNull(
				"basic check fails, null returned from PI initializer", execCmd);
		Assert.assertEquals("api number not set",
				ExecCmd.EXEC_AND_USE_ENHANCED_STREAM, execCmd.getApiNumber());
	}

}
