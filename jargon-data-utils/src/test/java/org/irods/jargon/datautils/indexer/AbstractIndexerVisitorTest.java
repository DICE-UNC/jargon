package org.irods.jargon.datautils.indexer;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractIndexerVisitorTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "AbstractIndexerVisitorTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testVisitEnter() {
		fail("Not yet implemented");
	}

	@Test
	public void testVisitLeave() {
		fail("Not yet implemented");
	}

	@Test
	public void testVisitEnterWithMetadata() {
		fail("Not yet implemented");
	}

	@Test
	public void testVisitLeaveWithMetadata() {
		fail("Not yet implemented");
	}

	@Test
	public void testVisitWithMetadata() {
		fail("Not yet implemented");
	}

}
