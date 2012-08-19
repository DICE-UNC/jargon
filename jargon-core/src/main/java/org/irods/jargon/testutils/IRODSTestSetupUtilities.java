/**
 *
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Common utilities to prep the test irods for unit tests
 * 
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public class IRODSTestSetupUtilities {
	private TestingPropertiesHelper testingPropertiesHelper;
	private Properties testingProperties;

	public IRODSTestSetupUtilities() throws TestingUtilsException {
		testingPropertiesHelper = new TestingPropertiesHelper();
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	/**
	 * Remove the scratch directory from irods based on the testing.properties
	 * file
	 * 
	 * @throws TestingUtilsException
	 */
	public void clearIrodsScratchDirectory() throws TestingUtilsException {

		IRODSFileSystem irodsFileSystem = null;
		try {
			irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccount irodsAccount = testingPropertiesHelper
					.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, "");
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsCollection);

			testScratchFile.delete();
		} catch (Exception e) {
			throw new TestingUtilsException("error clearing irods scratch dir",
					e);
		} finally {
			if (irodsFileSystem != null) {
				irodsFileSystem.closeAndEatExceptions();
			}
		}
	}

	/**
	 * Clear and then create a fresh scratch directory in irods based on the
	 * testing.properties file
	 * 
	 * @throws TestingUtilsException
	 */
	public void initializeIrodsScratchDirectory() throws TestingUtilsException {
		clearIrodsScratchDirectory();

		IRODSFileSystem irodsFileSystem = null;
		try {
			irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccount irodsAccount = testingPropertiesHelper
					.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, testingProperties
									.getProperty(IRODS_SCRATCH_DIR_KEY));
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsCollection);

			testScratchFile.delete();
		} catch (Exception e) {
			throw new TestingUtilsException("error clearing irods scratch dir",
					e);
		} finally {
			if (irodsFileSystem != null) {
				irodsFileSystem.closeAndEatExceptions();
			}
		}
	}

	/**
	 * Create a directory under scratch with the given name, which is typically
	 * a name assigned per Junit test class
	 * 
	 * @param testingDirectory
	 *            <code>String</code> with a directory to go underneath scratch,
	 *            do not supply leading '/'
	 * @throws TestingUtilsException
	 */
	public void initializeDirectoryForTest(final String testingDirectory)
			throws TestingUtilsException {
		StringBuilder scratchDir = new StringBuilder();
		scratchDir.append(testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, ""));
		scratchDir.append('/');
		scratchDir.append(testingDirectory);

		IRODSFileSystem irodsFileSystem = null;
		try {
			irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccount irodsAccount = testingPropertiesHelper
					.buildIRODSAccountFromTestProperties(testingProperties);

			testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, testingProperties
									.getProperty(IRODS_SCRATCH_DIR_KEY));
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(scratchDir.toString());

			testScratchFile.mkdirs();
		} catch (Exception e) {
			throw new TestingUtilsException("error clearing irods scratch dir",
					e);
		} finally {
			if (irodsFileSystem != null) {
				irodsFileSystem.closeAndEatExceptions();
			}
		}
	}
}
