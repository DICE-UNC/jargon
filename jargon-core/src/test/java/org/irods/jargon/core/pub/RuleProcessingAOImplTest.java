package org.irods.jargon.core.pub;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.core.pub.domain.DelayedRuleExecution;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RuleProcessingAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RuleProcessingAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new org.irods.jargon.testutils.AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void cleanUpIrods() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	@Before
	public void before() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testExecuteRule() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		String ruleString = "ListAvailableMS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertEquals("irodsRule did not have original string",
				ruleString, result.getIrodsRule().getRuleAsOriginalText());
		Assert.assertNotNull("did not get exec out", execOut.length() > 0);

	}

	@Test
	public void testExecuteRuleNewSyntax() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		String ruleString = "HelloWorld { \n writeLine(\"stdout\", \"Hello, world!\");\n}\nINPUT null\nOUTPUT ruleExecOut\n";

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);
		Assert.assertNotNull("null result from rule execution", result);

	}

	/**
	 * [#768] -1202000 error executing rule via jargon
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRuleNewSyntaxWithWriteLine() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		StringBuilder sb = new StringBuilder();
		sb.append("myTestRule {\n");
		sb.append(" writeString(*Where, *StringIn);\n");
		sb.append("writeLine(*Where,\"cheese\");\n");
		sb.append("}\n");
		sb.append("INPUT *Where=\"stdout\", *StringIn=\"string\"\n");
		sb.append("OUTPUT ruleExecOut");
		String ruleString = sb.toString();

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);
		Assert.assertNotNull("null result from rule execution", result);

	}

	/**
	 * [#768] -1202000 error executing rule via jargon
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRuleNewSyntaxWithWriteLineV2() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		StringBuilder sb = new StringBuilder();
		sb.append("myTestRule {\n");
		sb.append("writeString(\"stdout\", *StringIn);\n");
		sb.append("}\n");
		sb.append("INPUT *StringIn=\"1\"\n");
		sb.append("OUTPUT ruleExecOut");
		String ruleString = sb.toString();

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);
		Assert.assertNotNull("null result from rule execution", result);

	}

	@Test
	public void testExecuteRuleFromResourceFileNullParmOverrides()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		String ruleFile = "/rules/rulemsiGetIcatTime.r";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				ruleFile, null, RuleProcessingType.EXTERNAL);
		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);

	}

	@Test
	public void testExecuteRuleFromResourceWithOverrides() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		String ruleFile = "/rules/rulemsiDataObjChksum.r";

		// place a test file to checksum

		String testFileName = "testExecuteRuleFromResourceWithOverrides.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(targetIrodsFile);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		// override the file name for *dataObject

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*dataObject", '"' + destFile
				.getAbsolutePath() + '"'));

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				ruleFile, inputOverrides, RuleProcessingType.EXTERNAL);
		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);

	}

	@Test
	public void testExecuteRuleFromIrodsFileNullParmOverrides()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		String ruleFile = "/rules/rulemsiGetIcatTime.r";
		String irodsRuleFile = "rulemsiGetIcatTime.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);
		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();
		IRODSFileWriter irodsFileWriter = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFileWriter(
						targetIrodsCollection + "/" + irodsRuleFile);
		char[] buff = new char[1024];
		StringReader reader = new StringReader(ruleString);

		int len = 0;
		while ((len = reader.read(buff)) > -1) {
			irodsFileWriter.write(buff, 0, len);
		}

		irodsFileWriter.close();
		reader.close();

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromIRODSFile(
				irodsRuleFileAsFile.getAbsolutePath(), null,
				RuleProcessingType.EXTERNAL);
		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);

	}

	/**
	 * [#547] error running old style rule via irods file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteOldStyleRuleFromIrodsFileNullParmOverrides()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		String ruleFile = "/rules/ruleListAvailableMSOldStyle.r";
		String irodsRuleFile = "testExecuteOldStyleRuleFromIrodsFileNullParmOverrides.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);
		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();
		IRODSFileWriter irodsFileWriter = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFileWriter(
						targetIrodsCollection + "/" + irodsRuleFile);
		char[] buff = new char[1024];
		StringReader reader = new StringReader(ruleString);

		int len = 0;
		while ((len = reader.read(buff)) > -1) {
			irodsFileWriter.write(buff, 0, len);
		}

		irodsFileWriter.close();
		reader.close();

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromIRODSFile(
				irodsRuleFileAsFile.getAbsolutePath(), null,
				RuleProcessingType.CLASSIC);
		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);

	}

	@Test
	public void testExecuteRuleWithDelay() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		// test is only valid for 3.1
		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.1")) {
			irodsFileSystem.closeAndEatExceptions();
			return;
		}

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		String ruleString = "ListAvailableMS||delayExec(<PLUSET>1m</PLUSET>,msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \"),nop)|nop\n*A=hello\n ruleExecOut";

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertEquals("irodsRule did not have original string",
				ruleString, result.getIrodsRule().getRuleAsOriginalText());
		Assert.assertNotNull("did not get exec out", execOut.length() > 0);

	}

	@Test
	public void testRuleContainsConditionWithEqualsInAttrib() throws Exception {

		// put a collection out to do a checksum on
		String testFileName = "testRuleChecksum1.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSFile targetCollectionFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		targetCollectionFile.mkdirs();
		File sourceFile = new File(fileNameAndPath.toString());

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetCollectionFile, null, null);

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");

		ruleBuilder.append(targetIrodsCollection);
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		Assert.assertNotNull("did not get a response", result);

		Assert.assertEquals("did not get results for each output parameter", 6,
				result.getOutputParameterResults().size());

		String conditionValue = (String) result.getOutputParameterResults()
				.get("*Condition").getResultObject();

		String expectedCondition = "COLL_NAME = '" + targetIrodsCollection
				+ "'";
		Assert.assertEquals("condition not found", expectedCondition,
				conditionValue);

	}

	@Test
	public void testExecuteRequestClientActionPut() throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testExecuteRequestClientActionPut.txt";
		String testResultFileName = "testExecuteRequestClientActionPutResult.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 1);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testResultFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(");
		builder.append(targetIrodsFileName);
		builder.append(",null,");
		builder.append(putFileName);
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(builder
				.toString());

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());

		irodsFileSystem.close();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

	@Test
	public void testExecuteRequestClientActionParallelPut() throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testExecuteRequestClientActionParallelPut.txt";
		String testResultFileName = "testExecuteRequestClientActionParallelPutResult.txt";
		long length = 32 * 1024 * 1024;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, length);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testResultFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(");
		builder.append(targetIrodsFileName);
		builder.append(",null,");
		builder.append("\"localPath=");
		builder.append(putFileName);
		builder.append("\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(builder
				.toString());

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());
		Assert.assertEquals("irods file does not have correct length", length,
				putFile.length());

		irodsFileSystem.close();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

	/**
	 * File put by client action in rule, should be parallel, rule says no
	 * parallel [#630] execute msiDataObjGet via Jargon
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRequestClientActionParallelPutNoThreading()
			throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testExecuteRequestClientActionParallelPutNoThreading.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 33 * 1024 * 1024);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileName);
		File sourceFile = new File(putFileName);

		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, tcb);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(\"");
		builder.append(targetIrodsFileName);
		builder.append("\",null,");
		builder.append("\"localPath=");
		builder.append(putFileName);
		builder.append("++++destRescName=");
		builder.append(irodsAccount.getDefaultStorageResource());
		builder.append("++++numThreads=-1++++forceFlag=\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(builder
				.toString());

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());

		irodsFileSystem.close();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

	@Test
	public void testExecuteRequestClientActionPutWithOverwriteFileExists()
			throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testExecuteRequestClientActionPutWithOverwriteFileExists.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 1);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileName);
		File sourceFile = new File(putFileName);

		// put the file first to set up the overwrite
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(");
		builder.append(targetIrodsFileName);
		builder.append(",null,");
		builder.append("\"localPath=");
		builder.append(putFileName);
		builder.append("++++forceFlag=\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(builder
				.toString());

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());

		irodsFileSystem.close();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

	@Test(expected = JargonFileOrCollAlreadyExistsException.class)
	public void testExecuteRequestClientActionPutWithNoOverwriteFileExists()
			throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testExecuteRequestClientActionPutWithNoOverwriteFileExists.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 1);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileName);
		File sourceFile = new File(putFileName);

		// put the file first to set up the overwrite
		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(");
		builder.append(targetIrodsFileName);
		builder.append(",null,");
		builder.append("\"localPath=");
		builder.append(putFileName);
		builder.append("\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

	}

	@Test(expected = JargonException.class)
	public void testExecuteRequestClientActionPutLocalFileNotExists()
			throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileName = "testClientAction.txt";
		scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = "/a/bogus/dir/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjPut(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append("testExecuteRequestClientActionPutLocalFileNotExists.txt,null,");
		builder.append(putFileName);
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

	}

	@Test
	public void testExecuteRequestClientActionGetFile() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
		String testFileName = "testExecuteRequestClientActionGetFile.txt";
		String testFileGetName = "testExecuteRequestClientActionGetFileClient.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		File localTarget = new File(absPath, testFileGetName);
		localTarget.delete();

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// put the file

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||writeString(\"stdout\", \"hi there before\")##msiDataObjGet(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append(testFileName);
		builder.append(",");
		builder.append(absPath);
		builder.append(testFileGetName);
		builder.append(",*status)##writeString(\"stdout\",\"hi there after\")|nop\n");
		builder.append("*A=null\n");
		builder.append("ruleExecOut");

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + testFileGetName, 100);

	}

	/**
	 * Bug [#1641] [iROD-Chat:10574] Fwd: Porting rules to Jargon
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRequestMultipleClientActionGetFileBug1641()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
		String testFileName = "testExecuteRequestMultipleClientActionGetFileBug1641.txt";
		String testFileGetName = "testExecuteRequestMultipleClientActionGetFileBug1641Get.txt";
		String testFileName1 = "testExecuteRequestMultipleClientActionGetFileBug1641F2.txt";
		String testFileGetName1 = "testExecuteRequestMultipleClientActionGetFileBug1641GetF2.txt";

		String absPathReturn = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testFileGetName;
		String absPathReturn1 = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testFileGetName1;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);
		String absPath1 = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath1 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath1, testFileName1,
						100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		// put the file

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, tcb);

		File sourceFile1 = new File(scratchFileAbsolutePath1);
		IRODSFile targetFile1 = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName1);

		dto.putOperation(sourceFile1, targetFile1, null, tcb);

		String ruleFile = "/rules/testRuleBug1641MutlipleGets.r";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		// override the file name for *dataObject

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*SourceFile1",
				'"' + targetFile.getAbsolutePath() + '"'));
		inputOverrides.add(new IRODSRuleParameter("*localPath1",
				'"' + absPathReturn + '"'));
		inputOverrides.add(new IRODSRuleParameter("*SourceFile2",
				'"' + targetFile1.getAbsolutePath() + '"'));
		inputOverrides.add(new IRODSRuleParameter("*localPath2",
				'"' + absPathReturn1 + '"'));

		ruleProcessingAO.executeRuleFromResource(ruleFile, inputOverrides,
				RuleProcessingType.EXTERNAL);

		File return1File = new File(absPathReturn);
		Assert.assertTrue("didn't get first file", return1File.exists());

		File return2File = new File(absPathReturn1);
		Assert.assertTrue("didn't get second file", return2File.exists());

	}

	/**
	 * Bug [#1641] [iROD-Chat:10574] Fwd: Porting rules to Jargon
	 * 
	 * @throws Exception
	 */
	@Test
	// ignore and test as possible irods bug
	public void testPutGetInterleavedBug1641() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getEnvironmentalInfoAO(
						irodsAccount);
		IRODSServerProperties props = environmentalInfoAO
				.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();
		String testFileName = "testPutGetInterleavedBug1641.txt";
		String testFileGetName = "testPutGetInterleavedBug1641Get.txt";
		String testFileName1 = "testPutGetInterleavedBug1641F2.txt";
		String testFileGetName1 = "testPutGetInterleavedBug1641GetF2.txt";
		String testPutFileName = "testPutGetInterleavedBug1641PutFile.txt";

		String absPathReturn = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testFileGetName;
		String absPathReturn1 = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testFileGetName1;
		String absPathPut = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testPutFileName;

		FileGenerator.generateFileOfFixedLengthGivenName(scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH),
				testPutFileName, 32);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024 * 1024);
		String absPath1 = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath1 = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath1, testFileName1,
						100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String putTargetFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testPutFileName;

		TransferControlBlock tcb = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, tcb);

		File sourceFile1 = new File(scratchFileAbsolutePath1);
		IRODSFile targetFile1 = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName1);

		dto.putOperation(sourceFile1, targetFile1, null, tcb);

		String ruleFile = "/rules/testRuleBug1641MutlipleGetsAndPuts.r";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		// override the file name for *dataObject

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*SourceFile1",
				'"' + targetFile.getAbsolutePath() + '"'));
		inputOverrides.add(new IRODSRuleParameter("*localPath1",
				'"' + absPathReturn + '"'));
		inputOverrides.add(new IRODSRuleParameter("*SourceFile2",
				'"' + targetFile1.getAbsolutePath() + '"'));
		inputOverrides.add(new IRODSRuleParameter("*localPath2",
				'"' + absPathReturn1 + '"'));
		inputOverrides.add(new IRODSRuleParameter("*DestFile",
				'"' + putTargetFile + '"'));
		inputOverrides.add(new IRODSRuleParameter("*putLocalSource",
				'"' + absPathPut + '"'));
		inputOverrides.add(new IRODSRuleParameter("*DestResource",
				'"' + irodsAccount.getDefaultStorageResource() + '"'));

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				ruleFile, inputOverrides, RuleProcessingType.EXTERNAL);

		File return1File = new File(absPathReturn);
		Assert.assertTrue("didn't get first file", return1File.exists());

		File return2File = new File(absPathReturn1);
		Assert.assertTrue("didn't get second file", return2File.exists());

		IRODSFile putFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(putTargetFile);
		Assert.assertTrue("did not find put file", putFile.exists());

		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();

		Assert.assertNotNull("null execOut", execOut);

		String destFile = result.getOutputParameterResults().get("*DestFile")
				.getResultObject().toString();

		Assert.assertNotNull("null destFile", destFile);
	}

	@Test
	public void testExecuteRequestClientActionGetFileParallel()
			throws Exception {
		String testFileName = "testExecuteRequestClientActionGetFileParallel.txt";
		String testFileGetName = "testExecuteRequestClientActionGetFileClientParallel.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024 * 1024);

		File getTargetFile = new File(absPath, testFileGetName);
		getTargetFile.delete();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// put the file

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjGet(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append(testFileName);
		builder.append(",\"numThreads=0++++localPath=");
		builder.append(absPath);
		builder.append(testFileGetName);
		builder.append("++++rescName=");
		builder.append(irodsAccount.getDefaultStorageResource());
		builder.append("++++forceFlag=\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);

	}

	@Test
	public void testExecuteRequestClientActionGetFileParallelBug1641AddMessages()
			throws Exception {
		String testFileName = "testExecuteRequestClientActionGetFileParallelBug1641AddMessages.txt";
		String testFileGetName = "testExecuteRequestClientActionGetFileParallelBug1641AddMessagesGet.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024 * 1024);

		File getTargetFile = new File(absPath, testFileGetName);
		getTargetFile.delete();
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPathReturn = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH)
				+ testFileGetName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// put the file

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dto.putOperation(sourceFile, targetFile, null, tcb);

		String ruleFile = "/rules/testParallelGetWithStdoutBug1641.r";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		// override the file name for *dataObject

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*SourceFile", '"'
				+ targetFile.getAbsolutePath() + "/" + testFileName + '"'));
		inputOverrides.add(new IRODSRuleParameter("*localPath",
				'"' + absPathReturn + '"'));

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				ruleFile, inputOverrides, RuleProcessingType.EXTERNAL);

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);

		String execOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();

		Assert.assertNotNull("null execOut", execOut);

		String errorOut = result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_ERROR_OUT)
				.getResultObject().toString();

		Assert.assertNotNull("null execErrorOut", errorOut);

	}

	/**
	 * [#630] execute msiDataObjGet via Jargon Test a get operation with a file
	 * above the parallel txfr max with -1 in num threads
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRequestClientActionGetFileParallelNoThreadsIndicated()
			throws Exception {
		String testFileName = "testExecuteRequestClientActionGetFileParallelNoThreadsIndicated.txt";
		String testFileGetName = "testExecuteRequestClientActionGetFileParallelNoThreadsIndicatedReturned.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		File scratchDir = new File(absPath);
		scratchDir.delete();
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						32 * 1024 * 1024);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// put the file

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjGet(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append(testFileName);
		builder.append(",\"numThreads=-1++++localPath=");
		builder.append(absPath);
		builder.append(testFileGetName);
		builder.append("++++rescName=");
		builder.append(irodsAccount.getDefaultStorageResource());
		builder.append("++++forceFlag=\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);

	}

	@Test
	public void testExecuteRequestClientActionGetFileOverwrite()
			throws Exception {
		String testFileName = "testExecuteRequestClientActionGetFileOverwrite.txt";
		String testFileGetName = "testExecuteRequestClientActionGetFileOverwrite.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		// put the file to get it

		File sourceFile = new File(scratchFileAbsolutePath);
		IRODSFile targetFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(sourceFile, targetFile, null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjGet(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append(testFileName);
		builder.append(",\"numThreads=0++++localPath=");
		builder.append(absPath);
		builder.append(testFileGetName);
		builder.append("++++forceFlag=\"");
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + testFileGetName, 100);

	}

	/*
	 * per bug report [#181] rule requests transfer from unknown protocol error
	 * executing iRule rule sample:
	 * https://www.irods.org/index.php/Complex_Rule_Samples NOTE: requires the
	 * placing of the acObjPutWithDateAndChksumAsAVUs in core.irb. Commented out
	 * in normal testing to keep custom testing setups down
	 */
	@Ignore
	public void testExecuteRequestClientActionPutBug181() throws Exception {
		String testFileName = "test2.txt";
		String irodsFileName = "test2.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 100);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + irodsFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		StringBuilder builder = new StringBuilder();
		builder.append("myiput||acObjPutWithDateAndChksumAsAVUs(*rodsPath,*mainResource,*localFilePath,*inputChecksum,*outstatus)|nop\n");
		builder.append("*rodsPath=");
		builder.append(targetIrodsFileName);
		builder.append("%*mainResource=");
		builder.append(testingProperties
				.get(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		builder.append("%*localFilePath=");
		builder.append(putFileName);
		builder.append("%*inputChecksum=");
		builder.append(LocalFileUtils
				.computeCRC32FileCheckSumViaAbsolutePath(putFileName));
		builder.append("\n");

		builder.append("*ruleExecOut");
		String ruleString = builder.toString();

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

	/*
	 * [#182] Rule works on command line, does not execute via Jargon - NOTE:
	 * parked for now Might be useful later, or delete after 182 issue resolved.
	 * This is looking a like rule issue, not a Jargon issue
	 */
	@Ignore
	public void testExecuteRuleBug182() throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		// String testFileSubdir = "dataforbug181/danrw/AIP2archive/";
		String testFileName = "testExecuteRuleBug182.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 100);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + testFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(putFileName, targetIrodsFileName, "", null, null);

		StringBuilder builder = new StringBuilder();
		builder.append("sparAudit||assign(*rodsPath,");
		builder.append(targetIrodsFileName);
		builder.append(")");
		builder.append("##acGetValueForDataObjMetaAttribute(*storedChecksumCondition,*objStoredChksum)");
		builder.append("##acGetDataObjLocations(*locationsCondition,*matchingObjects)");
		builder.append("##forEachExec(*matchingObjects,msiGetValByKey(*matchingObjects,RESC_LOC,*objReplicaHost)");
		builder.append("##msiGetValByKey(*matchingObjects,DATA_PATH,*objPhysicalPath)");
		builder.append("##msiGetValByKey(*matchingObjects,RESC_NAME,*currRescName)");
		builder.append("##remoteExec(*objReplicaHost,null,acGetPhysicalDataObjMD5SUM(*objPhysicalPath,*objReplicaHost,*objPhysicalMD5),nop)");
		builder.append("##writeLine(stdout,\"Checksum of *rodsPath at *objReplicaHost on *currRescName (*objPhysicalPath) is *objPhysicalMD5\")");
		builder.append("##ifExec(*objStoredChksum == *objPhysicalMD5,writeLine(stdout,\"input and computed MD5 checksums match\" )");
		builder.append(",writeLine(stdout,\"if recov - actual comparison failed:(\"),writeLine(stdout,\"replace policy is : *replacePolicy\")");
		builder.append("##acPolicyBasedReplicaReplacement(*rodsPath,*currRescName,*replacePolicy),writeLine(stdout,\"repair schedule placeholder - recovery\")),nop)");
		builder.append("##writeLine(stdout,\"getting *rodsName to *stagePath/*rodsName\")");
		builder.append("#msiDataObjGet(*rodsPath,*stagePath/*rodsName,*getStatus)|nop##nop\n");
		// input vars
		builder.append("*rodsName=");
		builder.append(testFileName);
		builder.append("%*stagePath=");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append("*replacePolicy='eager'%*locationsCondition=DATA_NAME = '*rodsName'");
		builder.append("%*storedChecksumCondition=*locationsCondition AND META_DATA_ATTR_NAME = 'MD5SUM'\n");
		// output vars
		builder.append("*ruleExecOut");
		String ruleString = builder.toString();

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());
		Assert.assertNotNull("did not get a response", result);
	}

	@Ignore
	public void testListAllDelayedRuleExecutions() throws Exception {

		// TODO: purge, add 2
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		RuleProcessingAO ruleProcessingAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getRuleProcessingAO(irodsAccount);

		List<DelayedRuleExecution> delayedRuleExecutions = ruleProcessingAO
				.listAllDelayedRuleExecutions(0);
		irodsFileSystem.close();
		Assert.assertTrue("did not find delayedRuleExecutions",
				delayedRuleExecutions.size() > 0);
	}

	@Ignore
	public void testPurgeAllDelayedRuleExecutions() throws Exception {

		// TODO: purge, add1, purge, test

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		RuleProcessingAO ruleProcessingAO = irodsFileSystem
				.getIRODSAccessObjectFactory()
				.getRuleProcessingAO(irodsAccount);

		int countPurged = ruleProcessingAO.purgeAllDelayedExecQueue();
		Assert.assertTrue("nothing purged", countPurged > 0);
		List<DelayedRuleExecution> delayedRuleExecutions = ruleProcessingAO
				.listAllDelayedRuleExecutions(0);
		Assert.assertTrue("should be no delayedRuleExecutions after purge",
				delayedRuleExecutions.isEmpty());

	}

	/**
	 * Bug [#914] rule error : could not find name and val separated by an '='
	 * sign in input attribute
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteRuleWithComplexInputArgBug914() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String ruleFile = "/rules/ruleBug914.r";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				ruleFile, null, RuleProcessingType.CLASSIC);

		String out = (String) result.getOutputParameterResults().get("*out")
				.getResultObject();
		Assert.assertNotNull("null out", out);

	}

}
