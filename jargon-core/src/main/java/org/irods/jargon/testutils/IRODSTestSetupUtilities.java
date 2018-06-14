/**
 *
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.UnixFileRenameException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
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
	private CollectionAO collectionAO;
	private DataObjectAO dataObjectAO;

	public static final Logger log = LoggerFactory.getLogger(IRODSTestSetupUtilities.class);

	public IRODSTestSetupUtilities() throws TestConfigurationException {
		testingPropertiesHelper = new TestingPropertiesHelper();
		testingProperties = testingPropertiesHelper.getTestProperties();
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		try {
			irodsFileSystem = IRODSFileSystem.instance();
			dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
			collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		} catch (JargonException e) {
			throw new TestConfigurationException("cannot create IRODSFileSystem", e);
		}
	}

	/**
	 * Remove the scratch directory from irods based on the testing.properties file
	 *
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	@Overheaded
	// [#1628] intermittent -528036 errors on delete of collections
	public void clearIrodsScratchDirectory() throws TestConfigurationException {

		try {
			IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, "");
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(targetIrodsCollection);

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
				throw new TestConfigurationException("error clearing irods scratch dir", e);
			}
		} catch (Exception e) {
			throw new TestConfigurationException("error clearing scratch dir", e);
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
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public void initializeIrodsScratchDirectory() throws TestConfigurationException {
		clearIrodsScratchDirectory();

		try {
			IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

			String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
					testingProperties, testingProperties.getProperty(IRODS_SCRATCH_DIR_KEY));
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(targetIrodsCollection);

			testScratchFile.mkdirs();
		} catch (Exception e) {
			throw new TestConfigurationException("error clearing irods scratch dir", e);
		} finally {
			if (irodsFileSystem != null) {
				irodsFileSystem.closeAndEatExceptions();
			}
		}
	}

	/**
	 * Create a directory under scratch with the given name, which is typically a
	 * name assigned per Junit test class
	 *
	 * @param testingDirectory
	 *            {@code String} with a directory to go underneath scratch, do not
	 *            supply leading '/'
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public void initializeDirectoryForTest(final String testingDirectory) throws TestConfigurationException {
		StringBuilder scratchDir = new StringBuilder();
		scratchDir.append(
				testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, ""));
		scratchDir.append('/');
		scratchDir.append(testingDirectory);

		try {
			IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

			testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
					testingProperties.getProperty(IRODS_SCRATCH_DIR_KEY));
			IRODSFile testScratchFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(scratchDir.toString());

			testScratchFile.mkdirs();
		} catch (Exception e) {
			throw new TestConfigurationException("error clearing irods scratch dir", e);
		} finally {
			if (irodsFileSystem != null) {
				irodsFileSystem.closeAndEatExceptions();
			}
		}
	}

	/**
	 * Add randomly from a pool of Avu metadata to a nested collection of files and
	 * folders. Each list of candidates is tested with a random number and added at
	 * that point based on a threshold value
	 * 
	 * @param irodsAbsolutePath
	 *            {@code String} with a parent path
	 * @param candidateAvusForData
	 *            {@link AvuData} in a {@code List} that will be added to data
	 *            objects if a random number is above a threshold
	 * @param candidateAvusForCollections
	 *            {@link AvuData} in a {@code List} that will be added to
	 *            collections if a random number is above a threshold
	 * @param thresholdToAdd
	 *            {@code int} to compare to a random number to indicate that the
	 *            given avu should be added at the given node or leaf (0-99)
	 * @throws Exception
	 *             for any error
	 */
	public void decorateDirWithMetadata(final String irodsAbsolutePath, final List<AvuData> candidateAvusForData,
			final List<AvuData> candidateAvusForCollections, final int thresholdToAdd) throws Exception {

		log.info("decorateDirWithMetadata()");
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				testingProperties.getProperty(IRODS_SCRATCH_DIR_KEY));
		IRODSFile rootFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsAbsolutePath);
		Random rand = new Random();

		processCollectionToAddMetadata(rootFile, candidateAvusForData, candidateAvusForCollections, thresholdToAdd,
				rand);

	}

	private void processCollectionToAddMetadata(IRODSFile dir, List<AvuData> candidateAvusForData,
			List<AvuData> candidateAvusForCollections, int thresholdToAdd, Random random) throws JargonException {
		log.info("processCollectionToAddMetadata()");
		log.info("rootFile:{}", dir.getAbsolutePath());

		// for each candidate dir avu see if I add it

		for (AvuData avu : candidateAvusForCollections) {
			if (random.nextInt(100) > thresholdToAdd) {
				log.debug("adding avu:{}", avu);
				log.debug("     to collection:{}", dir);
				collectionAO.addAVUMetadata(dir.getAbsolutePath(), avu);
			}
		}

		// now process children
		IRODSFile irodsFile;

		for (File child : dir.listFiles()) {
			irodsFile = (IRODSFile) child;
			if (irodsFile.isDirectory()) {
				processCollectionToAddMetadata(irodsFile, candidateAvusForData, candidateAvusForCollections,
						thresholdToAdd, random);
			} else {
				processFileToAddMetadata(irodsFile, candidateAvusForData, thresholdToAdd, random);
			}
		}

	}

	private void processFileToAddMetadata(IRODSFile irodsFile, List<AvuData> candidateAvusForData, int thresholdToAdd,
			Random random) throws JargonException {
		log.info("processFileToAddMetadata()");
		log.info("irodsFile:{}", irodsFile);
		// for each candidate file avu see if I add it

		for (AvuData avu : candidateAvusForData) {
			if (random.nextInt(100) > thresholdToAdd) {
				log.debug("adding avu:{}", avu);
				log.debug("     to data obj:{}", irodsFile);
				dataObjectAO.addAVUMetadata(irodsFile.getAbsolutePath(), avu);
			}
		}

	}

}
