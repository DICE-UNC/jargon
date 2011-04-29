package org.irods.jargon.core.pub;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuleProcessingAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RuleProcessingAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static org.irods.jargon.testutils.AssertionHelper assertionHelper = null;

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
	}

	@Test
	public void testExecuteRule() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);
		String ruleString = "List Available MS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";
		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);
		irodsSession.closeSession();

		String execOut = (String) result.getOutputParameterResults()
				.get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject();
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

		// put scratch file into irods in the right place
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		iputCommand.setLocalFileName(fileNameAndPath.toString());
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		StringBuilder ruleBuilder = new StringBuilder();
		ruleBuilder
				.append("myTestRule||acGetIcatResults(*Action,*Condition,*B)##forEachExec(*B,msiGetValByKey(*B,RESC_LOC,*R)##remoteExec(*R,null,msiDataObjChksum(*B,*Operation,*C),nop)##msiGetValByKey(*B,DATA_NAME,*D)##msiGetValByKey(*B,COLL_NAME,*E)##writeLine(stdout,CheckSum of *E/*D at *R is *C),nop)|nop##nop\n");
		ruleBuilder.append("*Action=chksumRescLoc%*Condition=COLL_NAME = '");
		ruleBuilder.append(iputCommand.getIrodsFileName());
		ruleBuilder.append("'%*Operation=ChksumAll\n");
		ruleBuilder.append("*Action%*Condition%*Operation%*C%ruleExecOut");
		String ruleString = ruleBuilder.toString();

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);
		irodsSession.closeSession();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for each output parameter", 5,
				result.getOutputParameterResults().size());

		String conditionValue = (String) result.getOutputParameterResults()
				.get("*Condition").getResultObject();
		String expectedCondition = "COLL_NAME = '"
				+ iputCommand.getIrodsFileName() + "'";
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

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);
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
		irodsSession.closeSession();
	}

	@Test
	public void testExecuteRequestClientActionGetFile() throws Exception {
		String testFileName = "testClientActionGetFile.txt";
		String testFileGetName = "testClientActionGetFileAtClient.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String scratchFileAbsolutePath = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IputCommand iputCommand = new IputCommand();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		iputCommand.setLocalFileName(scratchFileAbsolutePath);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setForceOverride(true);

		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();

		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		IRODSAccessObjectFactory accessObjectFactory = IRODSAccessObjectFactoryImpl
				.instance(irodsSession);

		StringBuilder builder = new StringBuilder();
		builder.append("testClientAction||msiDataObjGet(");
		builder.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH));
		builder.append('/');
		builder.append(testFileName);
		builder.append(",");
		builder.append(absPath);
		builder.append(testFileGetName);
		builder.append(",*status)|nop\n");
		builder.append("*A=null\n");
		builder.append("*ruleExecOut");

		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		ruleProcessingAO.executeRule(builder.toString());
		irodsSession.closeSession();

		assertionHelper.assertLocalFileExistsInScratch(IRODS_TEST_SUBDIR_PATH
				+ '/' + testFileGetName);
		assertionHelper.assertLocalScratchFileLengthEquals(
				IRODS_TEST_SUBDIR_PATH + '/' + testFileGetName, 100);

	}

	/*
	 * per bug report [#181] rule requests transfer from unknown protocol error
	 * executing iRule rule sample:
	 * https://www.irods.org/index.php/Complex_Rule_Samples
	 */
	@Test
	public void testExecuteRequestClientActionPutBug181() throws Exception {
		// create a local file to put
		// put a collection out to do a checksum on
		String testFileSubdir = "dataforbug181/danrw/AIP2archive/";
		String testFileName = "test2.txt";
		String irodsFileName = "da-nrw/home/rods/test2.txt";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String putFileName = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath  + testFileSubdir, testFileName, 100);

		String targetIrodsFileName = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH)
				+ "/" + irodsFileName;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
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
				.computeFileCheckSumViaAbsolutePath(putFileName));
		builder.append("\n");

		builder.append("*ruleExecOut");
		String ruleString = builder.toString();
		
		RuleProcessingAO ruleProcessingAO = accessObjectFactory
				.getRuleProcessingAO(irodsAccount);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString);

		IRODSFile putFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFileName);

		Assert.assertTrue("file does not exist", putFile.exists());

		irodsFileSystem.close();

		Assert.assertNotNull("did not get a response", result);
		Assert.assertEquals("did not get results for client side operation", 1,
				result.getOutputParameterResults().size());

	}

}
