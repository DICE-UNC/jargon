/**
 *
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.UnixFileRenameException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.Overheaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private IRODSFileSystem irodsFileSystem;

	public static final Logger log = LoggerFactory
			.getLogger(IRODSTestSetupUtilities.class);

	public IRODSTestSetupUtilities() throws TestingUtilsException {
		testingPropertiesHelper = new TestingPropertiesHelper();
		testingProperties = testingPropertiesHelper.getTestProperties();
		try {
			irodsFileSystem = IRODSFileSystem.instance();
		} catch (JargonException e) {
			throw new TestingUtilsException("cannot create IRODSFileSystem", e);
		}
	}

	/**
	 * Remove the scratch directory from irods based on the testing.properties
	 * file
	 * 
	 * @throws TestingUtilsException
	 */
	@Overheaded
	// [#1628] intermittent -528036 errors on delete of collections
	public void clearIrodsScratchDirectory() throws TestingUtilsException {

		try {
			IRODSAccount irodsAccount = testingPropertiesHelper
					.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, "");
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsCollection);

			try {
				testScratchFile.delete();
			} catch (Exception e) {
				// ignore for now
			}
			// testScratchFile.deleteWithForceOption();
		} catch (JargonRuntimeException e) {
			if (e.getCause() instanceof UnixFileRenameException) {
				log.error(
						"rename exception, overheaded per bug  [#1628] intermittent -528036 errors on delete of collections",
						e);
				return;
			} else {
				throw new TestingUtilsException(
						"error clearing irods scratch dir", e);
			}
		} catch (Exception e) {

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

		try {
			IRODSAccount irodsAccount = testingPropertiesHelper
					.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, testingProperties
									.getProperty(IRODS_SCRATCH_DIR_KEY));
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsCollection);

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

		try {
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
