package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.IrodsRuleInvocationTypeEnum;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PythonRuleProcessingAOImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "PythonRuleProcessingAOImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static JargonProperties jargonOriginalProperties = null;

	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties(
				irodsFileSystem.getJargonProperties());
		jargonOriginalProperties = settableJargonProperties;
		irodsFileSystem.getIrodsSession().setJargonProperties(settableJargonProperties);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() throws Exception {
		irodsFileSystem.getIrodsSession().setJargonProperties(jargonOriginalProperties);

	}

	@Test
	public void testPythonRuleAsStringWithPythonRuleInvocationAuto() throws Exception {

		if (!testingPropertiesHelper.isTestPythonRules(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/python-rules/pyfilecountNoExternal.py";

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		RuleProcessingAO ruleProcessingAO = accessObjectFactory.getRuleProcessingAO(irodsAccount);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);
		ruleInvocationConfiguration.setEncodeRuleEngineInstance(true);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);

		String execOut = result.getOutputParameterResults().get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);
	}

	@Test
	public void testPythonRuleAsStringWithPythonRuleInvocationAutoByFileType() throws Exception {

		if (!testingPropertiesHelper.isTestPythonRules(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/python-rules/pyfilecountNoExternal.py";

		RuleProcessingAO ruleProcessingAO = accessObjectFactory.getRuleProcessingAO(irodsAccount);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);
		ruleInvocationConfiguration.setEncodeRuleEngineInstance(true);

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(ruleFile, inputOverrides,
				ruleInvocationConfiguration);

		String execOut = result.getOutputParameterResults().get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);
	}

	@Test
	public void testPythonRuleAsStringWithPythonRuleInvocationSetDirectlyNoExternal() throws Exception {

		if (!testingPropertiesHelper.isTestPythonRules(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/python-rules/pyfilecountNoExternal.py";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		RuleProcessingAO ruleProcessingAO = accessObjectFactory.getRuleProcessingAO(irodsAccount);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*Path", '\'' + targetIrodsCollection + '\''));
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);
		ruleInvocationConfiguration.setEncodeRuleEngineInstance(true);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);

		String execOut = result.getOutputParameterResults().get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);
	}

	@Test
	public void testPythonRuleAsStringWithPythonRuleInvocationSetDirectly() throws Exception {

		if (!testingPropertiesHelper.isTestPythonRules(testingProperties)) {
			return;
		}

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/python-rules/pyfilecount.py";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		RuleProcessingAO ruleProcessingAO = accessObjectFactory.getRuleProcessingAO(irodsAccount);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*Path", '\'' + targetIrodsCollection + '\''));
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);
		ruleInvocationConfiguration.setEncodeRuleEngineInstance(true);

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);

		String execOut = result.getOutputParameterResults().get(RuleProcessingAOImpl.RULE_EXEC_OUT).getResultObject()
				.toString();
		Assert.assertNotNull("null execOut", execOut);
	}

}
