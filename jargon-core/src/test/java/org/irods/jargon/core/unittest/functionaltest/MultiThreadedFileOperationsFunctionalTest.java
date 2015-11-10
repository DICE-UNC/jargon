package org.irods.jargon.core.unittest.functionaltest;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Various multi-threaded access to iRODS files and streams. Right now this is
 * not incorporated into the functional test suite, and was more useful to
 * manually replicate this reported issue interactively.
 *
 * @author Mike Conway - DICE
 *
 */
public class MultiThreadedFileOperationsFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "MultiThreadedFileOperationsFunctionalTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
		.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!testingPropertiesHelper.isTestFileSystemMount(testingProperties)) {
			return;
		}

		irodsFileSystem.closeAndEatExceptions();

	}

	/**
	 * https://github.com/DICE-UNC/jargon/issues/65
	 */
	@Test
	public void testCreateFileMultipleThreadsBug65() throws Exception {
		final String testFileName = "testCreateFileMultipleThreadsBug65.xls";

		final String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		final IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		for (int j = 0; j < 1000; j++) {
			final int finalJ = j;
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						irodsFileSystem.getIRODSFileFactory(irodsAccount)
						.instanceIRODSFile(
								targetIrodsCollection + "/"
										+ testFileName + finalJ);
					} catch (Exception e) {
						e.printStackTrace();
						Assert.fail("exception:" + e);
					}
				}
			};

			thread.start();
			thread.join();
		}

	}
}
