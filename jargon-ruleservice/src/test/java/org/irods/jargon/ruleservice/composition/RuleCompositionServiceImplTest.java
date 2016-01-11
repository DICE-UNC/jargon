package org.irods.jargon.ruleservice.composition;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuleCompositionServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RuleCompositionServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
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
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void cleanUpIrods() throws Exception {
		irodsFileSystem.closeAndEatExceptions();

	}

	@Test
	public void testParseIrodsFileIntoRule() throws Exception {
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
		String irodsRuleFile = "testParseIrodsFileIntoRule.r";
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

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService
				.loadRuleFromIrods(targetIrodsCollection + "/" + irodsRuleFile);

		Assert.assertNotNull("null rule", rule);

		Assert.assertFalse("empty rule body", rule.getRuleBody().isEmpty());
		Assert.assertFalse("empty input parms", rule.getInputParameters()
				.isEmpty());
		Assert.assertFalse("empty output parms", rule.getOutputParameters()
				.isEmpty());

	}

	@Test
	public void testStoreRuleFromParts() throws Exception {
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
		String irodsRuleFile = "testStoreRuleFromParts.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		List<String> inputParameters = new ArrayList<String>();
		List<String> outputParameters = new ArrayList<String>();

		StringBuilder sb;

		for (IRODSRuleParameter parm : rule.getInputParameters()) {
			sb = new StringBuilder();
			sb.append(parm.getUniqueName());
			sb.append("=");
			sb.append(parm.retrieveStringValue());
			inputParameters.add(sb.toString());
		}

		for (IRODSRuleParameter parm : rule.getOutputParameters()) {
			outputParameters.add(parm.getUniqueName());
		}
		Rule returnedFromStore = ruleCompositionService.storeRuleFromParts(
				irodsRuleFileAsFile.getAbsolutePath(), rule.getRuleBody(),
				inputParameters, outputParameters);

		Assert.assertNotNull("null rule returned", returnedFromStore);

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
		Assert.assertEquals(rule.getRuleBody(), actual.getRuleBody());

		Assert.assertEquals("unequal number of input params", rule
				.getInputParameters().size(), actual.getInputParameters()
				.size());
		Assert.assertEquals("unequal number of output params", rule
				.getOutputParameters().size(), actual.getOutputParameters()
				.size());

		for (int i = 0; i < rule.getInputParameters().size(); i++) {
			Assert.assertEquals("unmatched input parm", rule
					.getInputParameters().get(i).getUniqueName(), actual
					.getInputParameters().get(i).getUniqueName());
			Assert.assertEquals("unmatched input value", rule
					.getInputParameters().get(i).retrieveStringValue(), actual
					.getInputParameters().get(i).retrieveStringValue());
		}
	}

	@Test
	public void testStoreRuleFromString() throws Exception {
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
		String irodsRuleFile = "testStoreRuleFromParts.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule returnedFromStore = ruleCompositionService.storeRule(
				irodsRuleFileAsFile.getAbsolutePath(), ruleString);

		Assert.assertNotNull("null rule returned", returnedFromStore);

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
	}

	@Test
	public void testExecuteRuleFromParts() throws Exception {
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

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		List<String> inputParameters = new ArrayList<String>();
		List<String> outputParameters = new ArrayList<String>();

		StringBuilder sb;

		for (IRODSRuleParameter parm : rule.getInputParameters()) {
			sb = new StringBuilder();
			sb.append(parm.getUniqueName());
			sb.append("=");
			sb.append(parm.retrieveStringValue());
			inputParameters.add(sb.toString());
		}

		for (IRODSRuleParameter parm : rule.getOutputParameters()) {
			outputParameters.add(parm.getUniqueName());
		}
		IRODSRuleExecResult execResult = ruleCompositionService
				.executeRuleFromParts(rule.getRuleBody(), inputParameters,
						outputParameters);

		Assert.assertNotNull("null result returned", execResult);

	}

	@Test
	public void testExecuteRuleAsRawString() throws Exception {
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

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSRuleExecResult execResult = ruleCompositionService
				.executeRuleAsRawString(ruleString);

		Assert.assertNotNull("null result returned", execResult);

	}

	@Test
	public void testStoreRule() throws Exception {
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
		String irodsRuleFile = "testStoreRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		Rule returnedFromStore = ruleCompositionService.storeRule(
				irodsRuleFileAsFile.getAbsolutePath(), rule);

		Assert.assertNotNull("null rule returned", returnedFromStore);

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
		Assert.assertEquals(rule.getRuleBody(), actual.getRuleBody());

		Assert.assertEquals("unequal number of input params", rule
				.getInputParameters().size(), actual.getInputParameters()
				.size());
		Assert.assertEquals("unequal number of output params", rule
				.getOutputParameters().size(), actual.getOutputParameters()
				.size());

		for (int i = 0; i < rule.getInputParameters().size(); i++) {
			Assert.assertEquals("unmatched input parm", rule
					.getInputParameters().get(i).getUniqueName(), actual
					.getInputParameters().get(i).getUniqueName());
			Assert.assertEquals("unmatched input value", rule
					.getInputParameters().get(i).retrieveStringValue(), actual
					.getInputParameters().get(i).retrieveStringValue());
		}
	}

	@Test
	public void testStoreRuleOverwrite() throws Exception {
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
		String irodsRuleFile = "testStoreRuleOverwrite.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		Rule returnedFromStore = ruleCompositionService.storeRule(
				irodsRuleFileAsFile.getAbsolutePath(), rule);
		rule.setRuleBody("hello");
		returnedFromStore = ruleCompositionService.storeRule(
				irodsRuleFileAsFile.getAbsolutePath(), rule);

		Assert.assertNotNull("null rule returned", returnedFromStore);

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
		Assert.assertTrue("hello should be in rule body", actual.getRuleBody()
				.indexOf("hello") > -1);

	}

	@Test
	public void testDeleteInputParameterFromRule() throws Exception {
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
		String irodsRuleFile = "testDeleteInputParameterFromRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);

		// delete first input parm

		ruleCompositionService.deleteInputParameterFromRule(
				irodsRuleFileAsFile.getAbsolutePath(), "*dataObject");

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
		Assert.assertEquals(rule.getRuleBody(), actual.getRuleBody());

		Assert.assertEquals("unequal number of input params", rule
				.getInputParameters().size() - 1, actual.getInputParameters()
				.size());
		Assert.assertEquals("unequal number of output params", rule
				.getOutputParameters().size(), actual.getOutputParameters()
				.size());

	}

	@Test
	public void testDeleteOutputParameterFromRule() throws Exception {
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

		String ruleFile = "/rules/testRuleBug1641MutlipleGetsAndPuts.r";
		String irodsRuleFile = "testDeleteOutputParameterFromRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);

		// delete ruleExec parm

		ruleCompositionService.deleteOutputParameterFromRule(
				irodsRuleFileAsFile.getAbsolutePath(), "ruleExecOut");

		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());

		Assert.assertTrue("rule file not stored (does not exist)",
				irodsRuleFileAsFile.exists());

		Assert.assertNotNull("rule not reloaded from iRODS", actual);
		Assert.assertEquals(rule.getRuleBody(), actual.getRuleBody());

		Assert.assertEquals("unequal number of output params", rule
				.getOutputParameters().size() - 1, actual.getOutputParameters()
				.size());

	}

	@Test
	public void testLoadRuleAsString() throws Exception {
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
		String irodsRuleFile = "testLoadRuleAsString.r";
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

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);
		String actual = ruleCompositionService
				.loadRuleFromIrodsAsString(irodsRuleFileAsFile
						.getAbsolutePath());
		Assert.assertNotNull(actual);

	}

	@Test
	public void testReadARuleFromFileWithOuputParams() throws Exception {
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

		String ruleFile = "/rules/translateTextToRule.r";

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		Assert.assertEquals("unequal number of output params", 1, rule
				.getOutputParameters().size());

		IRODSRuleParameter outputParam = rule.getOutputParameters().get(0);
		Assert.assertEquals("did not get *DestFile", "*DestFile",
				outputParam.getUniqueName());

	}

	@Test(expected = FileNotFoundException.class)
	public void testDeleteInputParameterFromRuleNotExists() throws Exception {
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

		String irodsRuleFile = "testDeleteInputParameterFromRuleNotExists.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		ruleCompositionService.deleteInputParameterFromRule(
				irodsRuleFileAsFile.getAbsolutePath(), "*dataObject");
	}

	@Test(expected = JargonException.class)
	public void testParseIrodsFileIntoRuleMissing() throws Exception {
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

		String irodsRuleFile = "testParseIrodsFileIntoRuleMissing.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		ruleCompositionService.loadRuleFromIrods(targetIrodsCollection + "/"
				+ irodsRuleFile);

	}

	@Test
	public void testParseStringIntoRule() throws Exception {
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

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		Assert.assertNotNull("null rule", rule);

		Assert.assertFalse("empty rule body", rule.getRuleBody().isEmpty());
		Assert.assertFalse("empty input parms", rule.getInputParameters()
				.isEmpty());
		Assert.assertFalse("empty output parms", rule.getOutputParameters()
				.isEmpty());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseBlankStringIntoRule() throws Exception {
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

		String ruleString = "";

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		ruleCompositionService.parseStringIntoRule(ruleString);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseNullStringIntoRule() throws Exception {
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

		String ruleString = null;

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		ruleCompositionService.parseStringIntoRule(ruleString);

	}

	@Test
	public void testAddInputParameterToRule() throws Exception {
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
		String irodsRuleFile = "testAddInputParameterToRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);

		String newParamName = "*NewParam";
		String newParamValue = "1";

		int nbrBefore = rule.getInputParameters().size();

		Rule actual = ruleCompositionService.addInputParameterToRule(
				irodsRuleFileAsFile.getAbsolutePath(), newParamName,
				newParamValue);

		Assert.assertNotNull("null rule returned", actual);

		Assert.assertEquals("expected 1 new param", nbrBefore + 1, actual
				.getInputParameters().size());

		boolean foundNew = false;

		for (int i = 0; i < actual.getInputParameters().size(); i++) {
			if (actual.getInputParameters().get(i).getUniqueName()
					.equals(newParamName)) {
				foundNew = true;
				break;
			}
		}

		Assert.assertTrue("did not find new parameter", foundNew);

	}

	@Test(expected = DuplicateDataException.class)
	public void testAddDuplicateInputParameterToRule() throws Exception {
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
		String irodsRuleFile = "testAddDuplicateInputParameterToRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);

		String newParamName = "*NewParam";
		String newParamValue = "1";

		ruleCompositionService.addInputParameterToRule(
				irodsRuleFileAsFile.getAbsolutePath(), newParamName,
				newParamValue);
		ruleCompositionService.addInputParameterToRule(
				irodsRuleFileAsFile.getAbsolutePath(), newParamName,
				newParamValue);

	}

	@Test
	public void testAddOutputParameterToRule() throws Exception {
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
		String irodsRuleFile = "testAddOutputParameterToRule.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils
				.getClasspathResourceFileAsString(ruleFile);

		IRODSFile irodsRuleFileAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection + "/" + irodsRuleFile);
		irodsRuleFileAsFile.deleteWithForceOption();

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService.parseStringIntoRule(ruleString);

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);

		String newParamName = "*NewParam";

		int nbrBefore = rule.getOutputParameters().size();

		Rule actual = ruleCompositionService.addOutputParameterToRule(
				irodsRuleFileAsFile.getAbsolutePath(), newParamName);

		Assert.assertNotNull("null rule returned", actual);

		Assert.assertEquals("expected 1 new param", nbrBefore + 1, actual
				.getOutputParameters().size());

		boolean foundNew = false;

		for (int i = 0; i < actual.getOutputParameters().size(); i++) {
			if (actual.getOutputParameters().get(i).getUniqueName()
					.equals(newParamName)) {
				foundNew = true;
				break;
			}
		}

		Assert.assertTrue("did not find new parameter", foundNew);

	}

	@Test
	public void testParseMsiAclPolicyRule() throws Exception {
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

		String ruleFile = "/rules/rulemsiAclPolicy.r";
		String irodsRuleFile = "testParseMsiAclPolicyRule.r";
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

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService
				.loadRuleFromIrods(targetIrodsCollection + "/" + irodsRuleFile);

		Assert.assertNotNull("null rule", rule);

		Assert.assertFalse("empty rule body", rule.getRuleBody().isEmpty());
		Assert.assertTrue("not empty input parms", rule.getInputParameters()
				.isEmpty());
		Assert.assertTrue("not empty output parms", rule.getOutputParameters()
				.isEmpty());

	}

	@Test
	public void testParseQueensRule() throws Exception {
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

		String ruleFile = "/rules/nqueens.r";
		String irodsRuleFile = "testParseQueensRule.r";
		String irodsRuleFile2 = "testParseQueensRule2.r";

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

		RuleCompositionService ruleCompositionService = new RuleCompositionServiceImpl(
				accessObjectFactory, irodsAccount);

		Rule rule = ruleCompositionService
				.loadRuleFromIrods(targetIrodsCollection + "/" + irodsRuleFile);

		Assert.assertNotNull("null rule", rule);

		Assert.assertFalse("empty rule body", rule.getRuleBody().isEmpty());
		Assert.assertTrue("expected empty input parms", rule
				.getInputParameters().isEmpty());
		Assert.assertFalse("empty output parms", rule.getOutputParameters()
				.isEmpty());

		// round trip

		irodsRuleFileAsFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(
				targetIrodsCollection + "/" + irodsRuleFile2);
		irodsRuleFileAsFile.deleteWithForceOption();

		ruleCompositionService.storeRule(irodsRuleFileAsFile.getAbsolutePath(),
				rule);
		Rule actual = ruleCompositionService
				.loadRuleFromIrods(irodsRuleFileAsFile.getAbsolutePath());
		Assert.assertNotNull("could not reload rule after round trip", actual);

	}

}
