package org.irods.jargon.core.pub.apiplugin.domain;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AtomicMetadataInputTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "AtomicMetadataInputTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;
	private static ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testCreateJsonRequest() throws Exception {
		String entityName = "entityName";
		String entityType = "collection";
		AtomicMetadataInput atomicMetadataInput = new AtomicMetadataInput();
		atomicMetadataInput.setEntityName(entityName);
		atomicMetadataInput.setEntityType(entityType);

		AtomicMetadataOperation op1 = new AtomicMetadataOperation();
		op1.setAttribute("a1");
		op1.setValue("v1");
		op1.setUnits("u1");
		op1.setOperation("add");

		atomicMetadataInput.getOperations().add(op1);
		String actual = mapper.writeValueAsString(atomicMetadataInput);
		Assert.assertNotNull("null atomicMetadataInput", atomicMetadataInput);

		AtomicMetadataInput remapped = mapper.readValue(actual, AtomicMetadataInput.class);
		Assert.assertNotNull("did not get remapped object back", remapped);

	}

}
