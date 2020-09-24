package org.irods.jargon.core.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.SettableJargonPropertiesMBean;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IrodsRuleFactoryTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsRuleFactoryTest";
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
		SettableJargonPropertiesMBean settableJargonProperties = new SettableJargonProperties(
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
	public void testCreateOtherRuleForQuotaAutoDetect() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/rules/logicalQuotaRule.json";

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setRuleEngineSpecifier("otherRuleEngineBreh");
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.OTHER);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.OTHER,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}

	@Test
	public void testCreatePythonBasedRuleAutoDetect() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

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

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*Condition", "COLL_NAME like '" + targetIrodsCollection + +'\''));
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}

	@Test
	public void testCreatePythonBasedRuleWithExplicitType() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
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

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*Condition", "COLL_NAME like '" + targetIrodsCollection + +'\''));
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}

	@Test
	public void testCreateIrodsRuleWithAutoDetectIrodsAnnotation() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/rules/ruleIrodsWithAnnotation.r";

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}

	@Test
	public void testCreatePythonRuleWithAutoDetectPythonAnnotation() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/python-rules/pyfilecountwithannotation.py";

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.PYTHON,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}

	@Test
	public void testCreateIrodsRuleWithAutoDetect() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		IRODSServerProperties props = environmentalInfoAO.getIRODSServerPropertiesFromIRODSServer();

		if (!props.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4.2")) {
			return;
		}

		String ruleFile = "/rules/rulemsiDataObjChksum.r";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String ruleString = LocalFileUtils.getClasspathResourceFileAsString(ruleFile);

		List<IRODSRuleParameter> inputOverrides = new ArrayList<IRODSRuleParameter>();
		inputOverrides.add(new IRODSRuleParameter("*Condition", "COLL_NAME like '" + targetIrodsCollection + +'\''));
		RuleInvocationConfiguration ruleInvocationConfiguration = new RuleInvocationConfiguration();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.AUTO_DETECT);

		IrodsRuleFactory irodsRuleFactory = new IrodsRuleFactory(irodsFileSystem.getIRODSAccessObjectFactory(),
				irodsAccount);
		IRODSRule irodsRule = irodsRuleFactory.instanceIrodsRule(ruleString, inputOverrides,
				ruleInvocationConfiguration);
		Assert.assertNotNull("did not produce rule", irodsRule);
		Assert.assertEquals(IrodsRuleInvocationTypeEnum.IRODS,
				irodsRule.getRuleInvocationConfiguration().getIrodsRuleInvocationTypeEnum());

	}
}
